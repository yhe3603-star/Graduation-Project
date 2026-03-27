package com.dongmedicine.common;

import com.dongmedicine.common.exception.ErrorCode;
import com.dongmedicine.config.RequestIdFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {

    private static final int SUCCESS = 200;
    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int ERROR = 500;

    private int code;
    private String msg;
    private T data;
    private String requestId;

    private static String getRequestId() {
        String requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY);
        return requestId != null ? requestId : "";
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS, "success", null, getRequestId());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "success", data, getRequestId());
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS, msg, data, getRequestId());
    }

    public static <T> R<T> error(String msg) {
        return new R<>(ERROR, msg, null, getRequestId());
    }

    public static <T> R<T> error(int code, String msg) {
        return new R<>(code, msg, null, getRequestId());
    }

    public static <T> R<T> error(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null, getRequestId());
    }

    public static <T> R<T> error(ErrorCode errorCode, String message) {
        return new R<>(errorCode.getCode(), message, null, getRequestId());
    }

    public static <T> R<T> unauthorized(String msg) {
        return new R<>(UNAUTHORIZED, msg, null, getRequestId());
    }

    public static <T> R<T> forbidden(String msg) {
        return new R<>(FORBIDDEN, msg, null, getRequestId());
    }

    public static <T> R<T> notFound(String msg) {
        return new R<>(NOT_FOUND, msg, null, getRequestId());
    }

    public static <T> R<T> badRequest(String msg) {
        return new R<>(BAD_REQUEST, msg, null, getRequestId());
    }

    public boolean isSuccess() {
        return this.code == SUCCESS;
    }
}
