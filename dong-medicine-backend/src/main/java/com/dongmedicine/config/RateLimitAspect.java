package com.dongmedicine.config;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    private static final int LOCAL_BUCKET_CAPACITY = 100;
    private static final long LOCAL_BUCKET_REFILL_RATE_MS = 1000;
    
    private final Map<String, LocalTokenBucket> localBuckets = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = true;
    private long lastRedisCheckTime = 0;
    private static final long REDIS_CHECK_INTERVAL = 30000;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);
        int limit = rateLimit.value();
        long windowSeconds = 1;

        String redisKey = RATE_LIMIT_PREFIX + key;
        
        boolean useLocalFallback = false;
        
        if (redisAvailable && shouldTryRedis()) {
            try {
                Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);

                if (currentCount != null && currentCount == 1) {
                    stringRedisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
                }

                if (currentCount != null && currentCount > limit) {
                    logger.warn("请求过于频繁(Redis): {} (限制: {}/秒, 当前: {})", key, limit, currentCount);
                    throw new BusinessException(ErrorCode.OPERATION_TOO_FREQUENT);
                }
                
                markRedisAvailable();
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                logger.warn("Redis不可用，降级到本地限流: {} — {}", key, e.getMessage());
                markRedisUnavailable();
                useLocalFallback = true;
            }
        } else {
            useLocalFallback = true;
        }
        
        if (useLocalFallback) {
            LocalTokenBucket bucket = localBuckets.computeIfAbsent(key, k -> 
                new LocalTokenBucket(limit, LOCAL_BUCKET_REFILL_RATE_MS));
            
            if (!bucket.tryAcquire()) {
                logger.warn("请求过于频繁(本地): {} (限制: {}/秒)", key, limit);
                throw new BusinessException(ErrorCode.OPERATION_TOO_FREQUENT);
            }
        }

        return joinPoint.proceed();
    }
    
    private boolean shouldTryRedis() {
        long now = System.currentTimeMillis();
        if (!redisAvailable && (now - lastRedisCheckTime) < REDIS_CHECK_INTERVAL) {
            return false;
        }
        return true;
    }
    
    private void markRedisAvailable() {
        redisAvailable = true;
        lastRedisCheckTime = System.currentTimeMillis();
    }
    
    private void markRedisUnavailable() {
        redisAvailable = false;
        lastRedisCheckTime = System.currentTimeMillis();
    }

    private String generateKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder();
        key.append(rateLimit.key().isEmpty() ? joinPoint.getSignature().getName() : rateLimit.key());

        if (StpUtil.isLogin()) {
            key.append(":").append(StpUtil.getLoginIdAsString());
        } else {
            key.append(":").append(getClientIp());
        }

        return key.toString();
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            logger.debug("获取客户端IP失败", e);
        }
        return "unknown";
    }
    
    private static class LocalTokenBucket {
        private final int capacity;
        private final long refillRateMs;
        private final AtomicLong tokens;
        private volatile long lastRefillTime;
        
        public LocalTokenBucket(int capacity, long refillRateMs) {
            this.capacity = capacity;
            this.refillRateMs = refillRateMs;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        public synchronized boolean tryAcquire() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed >= refillRateMs) {
                long tokensToAdd = elapsed / refillRateMs;
                long newTokens = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }
    }
}
