package com.dongmedicine.config;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    private static final String RATE_LIMIT_LUA_SCRIPT = 
        "local current = redis.call('INCR', KEYS[1]) " +
        "if current == 1 then " +
        "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
        "end " +
        "return current";
    
    private static final int LOCAL_BUCKET_CAPACITY = 100;
    private static final long LOCAL_BUCKET_REFILL_RATE_MS = 1000;
    
    private final Map<String, LocalTokenBucket> localBuckets = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = true;
    private volatile long lastRedisCheckTime = 0;
    private static final long REDIS_CHECK_INTERVAL = 30000;
    private static final long CLEANUP_INTERVAL = 100;
    private long checkCount = 0;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);
        int limit = rateLimit.value();
        long windowSeconds = 1;

        String redisKey = RATE_LIMIT_PREFIX + key;
        
        boolean useLocalFallback = false;
        
        // 定期清理过期的本地令牌桶，防止内存泄漏
        if (++checkCount % CLEANUP_INTERVAL == 0) {
            cleanupStaleBuckets();
        }
        
        if (redisAvailable && shouldTryRedis()) {
            try {
                Long currentCount = stringRedisTemplate.execute(
                    new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, Long.class),
                    List.of(redisKey),
                    String.valueOf(windowSeconds)
                );

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

    private void cleanupStaleBuckets() {
        long now = System.currentTimeMillis();
        localBuckets.entrySet().removeIf(entry -> 
            now - entry.getValue().getLastAccessTime() > 30 * 60 * 1000L);
    }

    private String generateKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder();
        key.append(rateLimit.key().isEmpty() ? joinPoint.getSignature().getName() : rateLimit.key());

        if (StpUtil.isLogin()) {
            key.append(":").append(StpUtil.getLoginIdAsString());
        } else {
            key.append(":").append(IpUtils.getClientIp(getRequest()));
        }

        return key.toString();
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            logger.debug("获取HttpServletRequest失败", e);
        }
        return null;
    }
    
    private static class LocalTokenBucket {
        private final int capacity;
        private final long refillRateMs;
        private final AtomicLong tokens;
        private volatile long lastRefillTime;
        private volatile long lastAccessTime;
        private long leftoverMs;

        public LocalTokenBucket(int capacity, long refillRateMs) {
            this.capacity = capacity;
            this.refillRateMs = refillRateMs;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
            this.leftoverMs = 0;
        }

        public synchronized boolean tryAcquire() {
            lastAccessTime = System.currentTimeMillis();
            refill();
            long current = tokens.get();
            while (current > 0) {
                if (tokens.compareAndSet(current, current - 1)) {
                    return true;
                }
                current = tokens.get();
            }
            return false;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime + leftoverMs;
            if (elapsed >= refillRateMs) {
                long tokensToAdd = elapsed / refillRateMs;
                leftoverMs = elapsed % refillRateMs;
                long newTokens = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }
    }
}
