package com.dongmedicine.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static <T> R<T> ok() {
        return new R<>(SUCCESS, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "success", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS, msg, data);
    }

    public static <T> R<T> error(String msg) {
        return new R<>(ERROR, msg, null);
    }

    public static <T> R<T> error(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> unauthorized(String msg) {
        return new R<>(UNAUTHORIZED, msg, null);
    }

    public static <T> R<T> forbidden(String msg) {
        return new R<>(FORBIDDEN, msg, null);
    }

    public static <T> R<T> notFound(String msg) {
        return new R<>(NOT_FOUND, msg, null);
    }

    public static <T> R<T> badRequest(String msg) {
        return new R<>(BAD_REQUEST, msg, null);
    }

    public boolean isSuccess() {
        return this.code == SUCCESS;
    }
}
