package com.dongmedicine.config;

import cn.dev33.satoken.stp.StpUtil;
import com.dongmedicine.common.util.IpUtils;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.service.OperationLogService;
import com.dongmedicine.service.RabbitMQOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService logService;
    private final RabbitMQOperationLogService rabbitMQOperationLogService;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogService logService,
                              @Autowired(required = false) RabbitMQOperationLogService rabbitMQOperationLogService,
                              ObjectMapper objectMapper) {
        this.logService = logService;
        this.rabbitMQOperationLogService = rabbitMQOperationLogService;
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* com.dongmedicine.controller.*Controller.*(..)) && " +
            "!execution(* com.dongmedicine.controller.UserController.login(..)) && " +
            "!execution(* com.dongmedicine.controller.UserController.register(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && "GET".equalsIgnoreCase(attributes.getRequest().getMethod())) {
            return point.proceed();
        }

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

            OperationLog operationLog = new OperationLog();
            operationLog.setIp(IpUtils.getClientIp(request));
            operationLog.setMethod(request.getMethod() + " " + request.getRequestURI());
            operationLog.setDuration((int) duration);
            operationLog.setSuccess(success);
            operationLog.setErrorMsg(errorMsg);
            operationLog.setUsername(StpUtil.isLogin() ? StpUtil.getSession().get("username", "anonymous").toString() : "anonymous");

            String className = point.getTarget().getClass().getSimpleName();
            operationLog.setModule(getModuleFromController(className));
            operationLog.setOperation(point.getSignature().getName());
            operationLog.setType(getOperationType(request.getMethod()));

            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                try {
                    Map<String, Object> params = new HashMap<>();
                    for (int i = 0; i < args.length && i < 5; i++) {
                        if (args[i] != null && !args[i].getClass().getName().contains("HttpServletRequest")) {
                            params.put("arg" + i, args[i]);
                        }
                    }
                    operationLog.setParams(objectMapper.writeValueAsString(params));
                } catch (Exception ignored) {}
            }

            try {
                    if (rabbitMQOperationLogService != null) {
                        rabbitMQOperationLogService.saveLogAsync(operationLog);
                    } else {
                        logService.save(operationLog);
                    }
                } catch (Exception e) {
                    log.warn("RabbitMQ 保存操作日志失败, 降级为同步保存, error={}", e.getMessage());
                    logService.save(operationLog);
                }
            } catch (Exception e) {
                log.error("保存操作日志失败, error={}", e.getMessage());
            }
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
