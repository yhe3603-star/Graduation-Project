package com.dongmedicine.common.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(ErrorCode.TOKEN_INVALID, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ErrorCode.PERMISSION_DENIED, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_ERROR, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.DUPLICATE_OPERATION, message);
    }

    public static BusinessException userNotFound() {
        return new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    public static BusinessException userAlreadyExists() {
        return new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
    }

    public static BusinessException passwordWrong() {
        return new BusinessException(ErrorCode.PASSWORD_WRONG);
    }

    public static BusinessException passwordTooWeak() {
        return new BusinessException(ErrorCode.PASSWORD_TOO_WEAK);
    }

    public static BusinessException permissionDenied() {
        return new BusinessException(ErrorCode.PERMISSION_DENIED);
    }

    public static BusinessException systemError() {
        return new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
