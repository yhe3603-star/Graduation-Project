package com.dongmedicine.common.exception;

public enum ErrorCode {
    
    SUCCESS(0, "操作成功"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    PASSWORD_WRONG(1003, "密码错误"),
    PASSWORD_TOO_WEAK(1004, "密码强度不足，密码必须包含字母和数字，长度至少6位"),
    TOKEN_EXPIRED(1005, "登录已过期，请重新登录"),
    TOKEN_INVALID(1006, "无效的登录凭证"),
    PERMISSION_DENIED(1007, "权限不足"),
    ACCOUNT_DISABLED(1008, "账号已被禁用"),
    LOGIN_REQUIRED(1009, "请先登录"),
    USERNAME_TOO_SHORT(1010, "用户名长度必须在3-20个字符之间"),
    USERNAME_FORMAT_ERROR(1011, "用户名只能包含字母、数字和下划线"),
    
    RESOURCE_NOT_FOUND(2001, "资源不存在"),
    PLANT_NOT_FOUND(2002, "植物信息不存在"),
    KNOWLEDGE_NOT_FOUND(2003, "知识条目不存在"),
    INHERITOR_NOT_FOUND(2004, "传承人信息不存在"),
    FEEDBACK_NOT_FOUND(2005, "反馈信息不存在"),
    COMMENT_NOT_FOUND(2006, "评论不存在"),
    
    PARAM_ERROR(3001, "参数错误"),
    PARAM_MISSING(3002, "缺少必要参数"),
    PARAM_FORMAT_ERROR(3003, "参数格式错误"),
    PARAM_OUT_OF_RANGE(3004, "参数超出范围"),
    
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4002, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),
    FILE_NOT_FOUND(4004, "文件不存在"),
    
    DUPLICATE_OPERATION(5001, "重复操作"),
    OPERATION_TOO_FREQUENT(5002, "操作过于频繁，请稍后再试"),
    OPERATION_FAILED(5003, "操作失败"),
    
    DATABASE_ERROR(6001, "数据库操作失败"),
    CACHE_ERROR(6002, "缓存操作失败"),
    NETWORK_ERROR(6003, "网络连接失败"),
    
    AI_SERVICE_ERROR(7001, "AI服务暂时不可用"),
    AI_RESPONSE_ERROR(7002, "AI响应解析失败"),
    
    SYSTEM_ERROR(9001, "系统繁忙，请稍后再试"),
    UNKNOWN_ERROR(9999, "未知错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}
