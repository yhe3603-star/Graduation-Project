package com.dongmedicine.common.exception;

import com.dongmedicine.common.R;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private boolean isProd() {
        return "prod".equalsIgnoreCase(activeProfile);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        
        HttpStatus status = mapBusinessErrorCode(e.getErrorCode());
        
        return ResponseEntity
                .status(status)
                .body(R.error(e.getErrorCode(), e.getMessage()));
    }

    private HttpStatus mapBusinessErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND, RESOURCE_NOT_FOUND, PLANT_NOT_FOUND, 
                 KNOWLEDGE_NOT_FOUND, INHERITOR_NOT_FOUND, FEEDBACK_NOT_FOUND, 
                 COMMENT_NOT_FOUND, FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TOKEN_EXPIRED, TOKEN_INVALID, LOGIN_REQUIRED -> HttpStatus.UNAUTHORIZED;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case USER_ALREADY_EXISTS, DUPLICATE_OPERATION -> HttpStatus.CONFLICT;
            case OPERATION_TOO_FREQUENT -> HttpStatus.TOO_MANY_REQUESTS;
            case PARAM_ERROR, PARAM_MISSING, PARAM_FORMAT_ERROR, PARAM_OUT_OF_RANGE,
                 USERNAME_TOO_SHORT, USERNAME_FORMAT_ERROR, PASSWORD_TOO_WEAK -> HttpStatus.BAD_REQUEST;
            case FILE_SIZE_EXCEEDED, FILE_TYPE_NOT_ALLOWED, FILE_UPLOAD_ERROR -> HttpStatus.BAD_REQUEST;
            case ACCOUNT_DISABLED -> HttpStatus.FORBIDDEN;
            case SYSTEM_ERROR, DATABASE_ERROR, UNKNOWN_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数验证失败: {}", message);
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束验证失败: {}", message);
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleMissingParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少必要参数: {}", e.getParameterName());
        return R.error(ErrorCode.PARAM_MISSING, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}={}", e.getName(), e.getValue());
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, "参数 '" + e.getName() + "' 格式错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, "请求体格式错误，请检查JSON格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法: {}", e.getMethod());
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<?> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的媒体类型: {}", e.getContentType());
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, "不支持的媒体类型: " + e.getContentType());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件大小超出限制: {}", e.getMessage());
        return R.error(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleMultipartException(MultipartException e) {
        log.warn("文件上传异常: {}", e.getMessage());
        return R.error(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败: " + e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return R.error(ErrorCode.PASSWORD_WRONG);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleDisabledException(AuthenticationException e) {
        log.warn("账号状态异常: {}", e.getMessage());
        return R.error(ErrorCode.ACCOUNT_DISABLED);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        return R.error(ErrorCode.TOKEN_INVALID);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return R.error(ErrorCode.PERMISSION_DENIED);
    }

    @ExceptionHandler({ExpiredJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleExpiredJwtException(ExpiredJwtException e) {
        log.warn("JWT已过期: {}", e.getMessage());
        return R.error(ErrorCode.TOKEN_EXPIRED);
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleInvalidJwtException(JwtException e) {
        log.warn("JWT无效: {}", e.getMessage());
        return R.error(ErrorCode.TOKEN_INVALID);
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleJwtException(JwtException e) {
        log.warn("JWT异常: {}", e.getMessage());
        return R.error(ErrorCode.TOKEN_INVALID);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public R<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据完整性冲突: {}", e.getMessage());
        String message = "数据操作冲突";
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate")) {
                message = "数据已存在，请勿重复添加";
            } else if (e.getMessage().contains("foreign key")) {
                message = "存在关联数据，无法删除";
            }
        }
        return R.error(ErrorCode.DUPLICATE_OPERATION, message);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<?> handleEmptyResultDataAccessException(EmptyResultDataAccessException e) {
        log.warn("数据不存在: {}", e.getMessage());
        return R.error(ErrorCode.RESOURCE_NOT_FOUND, "请求的数据不存在");
    }

    @ExceptionHandler({DataAccessException.class, SQLException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleDatabaseException(Exception e) {
        if (isProd()) {
            log.error("数据库异常: {}", e.getClass().getSimpleName());
        } else {
            log.error("数据库异常: ", e);
        }
        return R.error(ErrorCode.DATABASE_ERROR);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<?> handleNotFound(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return R.error(ErrorCode.RESOURCE_NOT_FOUND, "接口不存在");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数非法: {}", e.getMessage());
        return R.error(ErrorCode.PARAM_ERROR, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleIllegalState(IllegalStateException e) {
        log.warn("状态非法: {}", e.getMessage());
        return R.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }

    @ExceptionHandler(SocketTimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public R<?> handleSocketTimeoutException(SocketTimeoutException e) {
        log.warn("网络超时: {}", e.getMessage());
        return R.error(ErrorCode.NETWORK_ERROR, "网络连接超时");
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<R<?>> handleCompletionException(CompletionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof BusinessException) {
            return handleBusinessException((BusinessException) cause);
        }
        if (isProd()) {
            log.error("异步操作异常: {}", e.getClass().getSimpleName());
        } else {
            log.error("异步操作异常: ", e);
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.error(ErrorCode.SYSTEM_ERROR));
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleNullPointerException(NullPointerException e) {
        if (isProd()) {
            log.error("空指针异常");
        } else {
            log.error("空指针异常: ", e);
        }
        return R.error(ErrorCode.SYSTEM_ERROR, "系统内部错误");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleRuntimeException(RuntimeException e) {
        if (isProd()) {
            log.error("运行时异常: {}", e.getClass().getSimpleName());
        } else {
            log.error("运行时异常: ", e);
        }
        return R.error(ErrorCode.SYSTEM_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        if (isProd()) {
            log.error("未知异常: {}", e.getClass().getSimpleName());
        } else {
            log.error("未知异常: ", e);
        }
        return R.error(ErrorCode.UNKNOWN_ERROR);
    }
}
