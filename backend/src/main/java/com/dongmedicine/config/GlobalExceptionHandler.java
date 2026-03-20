package com.dongmedicine.config;

import com.dongmedicine.common.R;
import com.dongmedicine.common.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleValidationException(Exception e) {
        String errorMsg;
        if (e instanceof MethodArgumentNotValidException) {
            errorMsg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else {
            errorMsg = ((BindException) e).getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        logger.warn("参数验证失败: {}", errorMsg);
        return R.badRequest("参数错误: " + errorMsg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        logger.warn("约束违反: {}", errorMsg);
        return R.badRequest("参数格式错误: " + errorMsg);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleParameterException(Exception e) {
        if (e instanceof MissingServletRequestParameterException) {
            String paramName = ((MissingServletRequestParameterException) e).getParameterName();
            logger.warn("缺少必要参数: {}", paramName);
            return R.badRequest("缺少必要参数: " + paramName);
        }
        logger.warn("参数类型错误: {}={}", ((MethodArgumentTypeMismatchException) e).getName(), 
                ((MethodArgumentTypeMismatchException) e).getValue());
        return R.badRequest("参数类型错误: " + ((MethodArgumentTypeMismatchException) e).getName());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<String> handleBadCredentials(BadCredentialsException e) {
        logger.warn("认证失败: {}", e.getMessage());
        return R.unauthorized("用户名或密码错误");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<String> handleAuthentication(AuthenticationException e) {
        logger.warn("认证异常: {}", e.getMessage());
        return R.unauthorized("认证失败，请重新登录");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<String> handleAccessDenied(AccessDeniedException e) {
        logger.warn("权限不足: {}", e.getMessage());
        return R.forbidden("权限不足，无法访问");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public R<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        logger.warn("数据完整性冲突: {}", e.getMessage());
        String message = "数据操作冲突";
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate")) {
                message = "数据已存在，请勿重复添加";
            } else if (e.getMessage().contains("foreign key")) {
                message = "存在关联数据，无法删除";
            }
        }
        return R.error(409, message);
    }

    @ExceptionHandler({DataAccessException.class, SQLException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleDatabaseException(Exception e) {
        logger.error("数据库异常: ", e);
        return R.error("数据库操作失败，请稍后重试");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<String> handleNotFound(NoHandlerFoundException e) {
        logger.warn("接口不存在: {}", e.getRequestURL());
        return R.notFound("接口不存在");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("参数非法: {}", e.getMessage());
        return R.badRequest(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: ", e);
        return e.getMessage() != null && !e.getMessage().isEmpty() 
                ? R.error(e.getMessage()) 
                : R.error("系统内部错误，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleGenericException(Exception e) {
        logger.error("系统异常: ", e);
        return R.error("系统内部错误，请稍后重试");
    }
}
