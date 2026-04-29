package com.dongmedicine.config;

import com.dongmedicine.common.util.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "password", "passwordHash", "currentPassword", "newPassword", 
            "confirmPassword", "token", "authorization", "secret"
    );
    
    private static final int MAX_RESPONSE_LENGTH = 1000;

    @Pointcut("execution(* com.dongmedicine.controller.*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        HttpServletRequest request = getRequest();
        String requestInfo = getRequestInfo(request);
        String clientIp = IpUtils.getClientIp(request);
        String userId = getUserId();
        
        String argsStr = safeArgsToString(args);
        
        log.info("[{}] >>> {} {} | ip={} | user={} | method={} | args={}", 
                traceId, requestInfo, className + "." + methodName, 
                clientIp, userId, methodName, argsStr);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            String resultStr = safeResultToString(result);
            
            if (duration > 1000) {
                log.warn("[{}] <<< {} {} | duration={}ms (SLOW) | result={}", 
                        traceId, requestInfo, className + "." + methodName, 
                        duration, resultStr);
            } else {
                log.info("[{}] <<< {} {} | duration={}ms | result={}", 
                        traceId, requestInfo, className + "." + methodName, 
                        duration, resultStr);
            }
            
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("[{}] <<< {} {} | duration={}ms | error={} | msg={}", 
                    traceId, requestInfo, className + "." + methodName, duration,
                    e.getClass().getSimpleName(), e.getMessage());
            
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String getRequestInfo(HttpServletRequest request) {
        if (request != null) {
            return String.format("%s %s", request.getMethod(), request.getRequestURI());
        }
        return "UNKNOWN";
    }

    private String getUserId() {
        try {
            return MDC.get("userId") != null ? MDC.get("userId") : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String safeArgsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        try {
            List<Object> safeArgs = new ArrayList<>();
            for (Object arg : args) {
                if (arg == null) {
                    safeArgs.add(null);
                } else if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    safeArgs.add(Map.of("type", "file", "name", file.getOriginalFilename(), "size", file.getSize()));
                } else if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                    safeArgs.add(arg.getClass().getSimpleName());
                } else if (arg instanceof String) {
                    safeArgs.add(maskSensitiveData(arg.toString()));
                } else {
                    safeArgs.add(arg);
                }
            }
            String json = objectMapper.writeValueAsString(safeArgs);
            return truncate(json, MAX_RESPONSE_LENGTH);
        } catch (Exception e) {
            return "[error serializing args]";
        }
    }

    private String safeResultToString(Object result) {
        if (result == null) {
            return "null";
        }
        
        try {
            String json = objectMapper.writeValueAsString(result);
            return truncate(json, MAX_RESPONSE_LENGTH);
        } catch (Exception e) {
            return result.getClass().getSimpleName();
        }
    }

    private String maskSensitiveData(String data) {
        if (data == null) {
            return null;
        }
        String lowerData = data.toLowerCase();
        for (String sensitive : SENSITIVE_PARAMS) {
            if (lowerData.contains(sensitive.toLowerCase())) {
                return "***MASKED***";
            }
        }
        return data;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }
}
