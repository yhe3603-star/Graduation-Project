package com.dongmedicine.config;

import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("execution(* com.dongmedicine.controller.*Controller.*(..)) && " +
            "!execution(* com.dongmedicine.controller.UserController.login(..)) && " +
            "!execution(* com.dongmedicine.controller.UserController.register(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;

        try {
            return point.proceed();
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            saveLog(point, System.currentTimeMillis() - startTime, success, errorMsg);
        }
    }

    private void saveLog(ProceedingJoinPoint point, long duration, boolean success, String errorMsg) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            OperationLog log = new OperationLog();
            log.setIp(getClientIp(request));
            log.setMethod(request.getMethod() + " " + request.getRequestURI());
            log.setDuration((int) duration);
            log.setSuccess(success);
            log.setErrorMsg(errorMsg);
            log.setCreatedAt(LocalDateTime.now());
            log.setUsername(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())
                    ? auth.getName() : "anonymous");

            String className = point.getTarget().getClass().getSimpleName();
            log.setModule(getModuleFromController(className));
            log.setOperation(point.getSignature().getName());
            log.setType(getOperationType(request.getMethod()));

            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                try {
                    Map<String, Object> params = new HashMap<>();
                    for (int i = 0; i < args.length && i < 5; i++) {
                        if (args[i] != null && !args[i].getClass().getName().contains("HttpServletRequest")) {
                            params.put("arg" + i, args[i]);
                        }
                    }
                    log.setParams(objectMapper.writeValueAsString(params));
                } catch (Exception ignored) {}
            }

            logService.save(log);
        } catch (Exception ignored) {}
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getModuleFromController(String className) {
        if (className.contains("User")) return "USER";
        if (className.contains("Plant")) return "PLANT";
        if (className.contains("Knowledge")) return "KNOWLEDGE";
        if (className.contains("Inheritor")) return "INHERITOR";
        if (className.contains("Resource")) return "RESOURCE";
        if (className.contains("Qa") || className.contains("Quiz")) return "QA";
        if (className.contains("Feedback")) return "FEEDBACK";
        if (className.contains("Comment")) return "COMMENT";
        if (className.contains("Favorite")) return "FAVORITE";
        return "SYSTEM";
    }

    private String getOperationType(String method) {
        switch (method.toUpperCase()) {
            case "POST": return "CREATE";
            case "PUT":
            case "PATCH": return "UPDATE";
            case "DELETE": return "DELETE";
            case "GET": return "QUERY";
            default: return "OTHER";
        }
    }
}
