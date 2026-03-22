package com.dongmedicine.config;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);
        int limit = rateLimit.value();
        long windowSeconds = 1;

        String redisKey = RATE_LIMIT_PREFIX + key;
        
        Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);
        
        if (currentCount != null && currentCount == 1) {
            stringRedisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > limit) {
            logger.warn("请求过于频繁: {} (限制: {}/秒, 当前: {})", key, limit, currentCount);
            throw new BusinessException(ErrorCode.OPERATION_TOO_FREQUENT);
        }

        return joinPoint.proceed();
    }

    private String generateKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder();
        key.append(rateLimit.key().isEmpty() ? joinPoint.getSignature().getName() : rateLimit.key());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            key.append(":").append(authentication.getName());
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
}
