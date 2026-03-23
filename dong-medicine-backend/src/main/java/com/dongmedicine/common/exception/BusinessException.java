package com.dongmedicine.common.exception;

/**
 * 业务异常类
 * 
 * <p>用于在业务逻辑中抛出可预期的异常，携带错误码和错误信息。
 * 所有业务异常都会被 {@link GlobalExceptionHandler} 统一处理并返回给前端。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 使用静态工厂方法（推荐）
 * throw BusinessException.notFound("用户不存在");
 * throw BusinessException.badRequest("参数错误");
 * 
 * // 使用错误码构造
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * 
 * // 自定义错误消息
 * throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名不能为空");
 * }</pre>
 * 
 * @author dong-medicine
 * @see ErrorCode
 * @see GlobalExceptionHandler
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 使用错误码构造业务异常
     * 
     * @param errorCode 错误码枚举，包含错误码和默认错误消息
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 使用错误码和自定义消息构造业务异常
     * 
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 使用错误码、自定义消息和原因构造业务异常
     * 
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @param cause 导致此异常的原因
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码枚举
     * 
     * @return 错误码枚举对象
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误码数值
     * 
     * @return 错误码数值
     */
    public int getCode() {
        return errorCode.getCode();
    }

    /**
     * 创建资源未找到异常
     * 
     * @param message 错误消息
     * @return 业务异常实例
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    /**
     * 创建未授权异常
     * 
     * @param message 错误消息
     * @return 业务异常实例
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ErrorCode.TOKEN_INVALID, message);
    }

    /**
     * 创建权限不足异常
     * 
     * @param message 错误消息
     * @return 业务异常实例
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(ErrorCode.PERMISSION_DENIED, message);
    }

    /**
     * 创建请求参数错误异常
     * 
     * @param message 错误消息
     * @return 业务异常实例
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 创建资源冲突异常
     * 
     * @param message 错误消息
     * @return 业务异常实例
     */
    public static BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.DUPLICATE_OPERATION, message);
    }

    /**
     * 创建用户未找到异常
     * 
     * @return 业务异常实例
     */
    public static BusinessException userNotFound() {
        return new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    /**
     * 创建用户已存在异常
     * 
     * @return 业务异常实例
     */
    public static BusinessException userAlreadyExists() {
        return new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
    }

    /**
     * 创建密码错误异常
     * 
     * @return 业务异常实例
     */
    public static BusinessException passwordWrong() {
        return new BusinessException(ErrorCode.PASSWORD_WRONG);
    }

    /**
     * 创建密码强度不足异常
     * 
     * @return 业务异常实例
     */
    public static BusinessException passwordTooWeak() {
        return new BusinessException(ErrorCode.PASSWORD_TOO_WEAK);
    }

    /**
     * 创建权限拒绝异常
     * 
     * @return 业务异常实例
     */
    public static BusinessException permissionDenied() {
        return new BusinessException(ErrorCode.PERMISSION_DENIED);
    }

    /**
     * 创建系统错误异常
     * 
     * @return 业务异常实例
     */
    public static BusinessException systemError() {
        return new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
