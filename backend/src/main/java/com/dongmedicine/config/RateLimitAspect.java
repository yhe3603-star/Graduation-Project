package com.dongmedicine.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final long CLEANUP_INTERVAL_MS = 60_000;
    private static final long KEY_EXPIRE_MS = 120_000;

    private final ConcurrentHashMap<String, RequestInfo> rateLimitMap = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint, rateLimit);
        long currentTime = System.currentTimeMillis();
        int limit = rateLimit.value();
        long windowMillis = TimeUnit.SECONDS.toMillis(1);

        cleanupIfNeeded();

        RequestInfo requestInfo = rateLimitMap.computeIfAbsent(key, k -> new RequestInfo());

        synchronized (requestInfo) {
            requestInfo.requests.removeIf(time -> currentTime - time > windowMillis);

            if (requestInfo.requests.size() >= limit) {
                logger.warn("请求过于频繁: {} (限制: {}/秒)", key, limit);
                throw new RuntimeException("请求过于频繁，请稍后再试");
            }

            requestInfo.requests.add(currentTime);
        }

        return joinPoint.proceed();
    }

    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            synchronized (this) {
                if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
                    rateLimitMap.entrySet().removeIf(entry -> {
                        RequestInfo info = entry.getValue();
                        synchronized (info) {
                            info.requests.removeIf(time -> now - time > KEY_EXPIRE_MS);
                            return info.requests.isEmpty();
                        }
                    });
                    lastCleanupTime = now;
                }
            }
        }
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

    private static class RequestInfo {
        final List<Long> requests = new ArrayList<>();
    }
}
