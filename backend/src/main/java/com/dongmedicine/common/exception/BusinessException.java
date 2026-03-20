package com.dongmedicine.common.exception;

public class BusinessException extends RuntimeException {

    private static final int DEFAULT_CODE = 400;
    private static final int NOT_FOUND_CODE = 404;
    private static final int UNAUTHORIZED_CODE = 401;
    private static final int FORBIDDEN_CODE = 403;
    private static final int BAD_REQUEST_CODE = 400;
    private static final int CONFLICT_CODE = 409;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_CODE;
    }

    public int getCode() {
        return code;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(NOT_FOUND_CODE, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(UNAUTHORIZED_CODE, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(FORBIDDEN_CODE, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(BAD_REQUEST_CODE, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(CONFLICT_CODE, message);
    }
}
