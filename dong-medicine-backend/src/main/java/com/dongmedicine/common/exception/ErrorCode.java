package com.dongmedicine.common.exception;

/**
 * 错误码枚举类
 * 
 * <p>定义系统中所有业务错误码，按照模块分类：</p>
 * <ul>
 *   <li>1xxx - 用户相关错误</li>
 *   <li>2xxx - 资源相关错误</li>
 *   <li>3xxx - 参数相关错误</li>
 *   <li>4xxx - 文件相关错误</li>
 *   <li>5xxx - 操作相关错误</li>
 *   <li>6xxx - 基础设施错误</li>
 *   <li>7xxx - AI服务错误</li>
 *   <li>9xxx - 系统错误</li>
 * </ul>
 * 
 * @author dong-medicine
 * @see BusinessException
 */
public enum ErrorCode {
    
    // ==================== 成功状态 ====================
    
    /** 操作成功 */
    SUCCESS(0, "操作成功"),
    
    // ==================== 用户相关错误 1xxx ====================
    
    /** 用户不存在 */
    USER_NOT_FOUND(1001, "用户不存在"),
    
    /** 用户名已存在 */
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    
    /** 密码错误 */
    PASSWORD_WRONG(1003, "密码错误"),
    
    /** 密码强度不足 */
    PASSWORD_TOO_WEAK(1004, "密码强度不足，密码必须包含字母和数字，长度至少6位"),
    
    /** 登录已过期 */
    TOKEN_EXPIRED(1005, "登录已过期，请重新登录"),
    
    /** 无效的登录凭证 */
    TOKEN_INVALID(1006, "无效的登录凭证"),
    
    /** 权限不足 */
    PERMISSION_DENIED(1007, "权限不足"),
    
    /** 账号已被禁用 */
    ACCOUNT_DISABLED(1008, "账号已被禁用"),
    
    /** 请先登录 */
    LOGIN_REQUIRED(1009, "请先登录"),
    
    /** 用户名长度必须在3-20个字符之间 */
    USERNAME_TOO_SHORT(1010, "用户名长度必须在3-20个字符之间"),
    
    /** 用户名只能包含字母、数字和下划线 */
    USERNAME_FORMAT_ERROR(1011, "用户名只能包含字母、数字和下划线"),
    
    // ==================== 资源相关错误 2xxx ====================
    
    /** 资源不存在 */
    RESOURCE_NOT_FOUND(2001, "资源不存在"),
    
    /** 植物信息不存在 */
    PLANT_NOT_FOUND(2002, "植物信息不存在"),
    
    /** 知识条目不存在 */
    KNOWLEDGE_NOT_FOUND(2003, "知识条目不存在"),
    
    /** 传承人信息不存在 */
    INHERITOR_NOT_FOUND(2004, "传承人信息不存在"),
    
    /** 反馈信息不存在 */
    FEEDBACK_NOT_FOUND(2005, "反馈信息不存在"),
    
    /** 评论不存在 */
    COMMENT_NOT_FOUND(2006, "评论不存在"),
    
    // ==================== 参数相关错误 3xxx ====================
    
    /** 参数错误 */
    PARAM_ERROR(3001, "参数错误"),
    
    /** 缺少必要参数 */
    PARAM_MISSING(3002, "缺少必要参数"),
    
    /** 参数格式错误 */
    PARAM_FORMAT_ERROR(3003, "参数格式错误"),
    
    /** 参数超出范围 */
    PARAM_OUT_OF_RANGE(3004, "参数超出范围"),
    
    // ==================== 文件相关错误 4xxx ====================
    
    /** 文件上传失败 */
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    
    /** 文件类型不允许 */
    FILE_TYPE_NOT_ALLOWED(4002, "文件类型不允许"),
    
    /** 文件大小超出限制 */
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),
    
    /** 文件不存在 */
    FILE_NOT_FOUND(4004, "文件不存在"),
    
    // ==================== 操作相关错误 5xxx ====================
    
    /** 重复操作 */
    DUPLICATE_OPERATION(5001, "重复操作"),
    
    /** 操作过于频繁 */
    OPERATION_TOO_FREQUENT(5002, "操作过于频繁，请稍后再试"),
    
    /** 操作失败 */
    OPERATION_FAILED(5003, "操作失败"),
    
    // ==================== 基础设施错误 6xxx ====================
    
    /** 数据库操作失败 */
    DATABASE_ERROR(6001, "数据库操作失败"),
    
    /** 缓存操作失败 */
    CACHE_ERROR(6002, "缓存操作失败"),
    
    /** 网络连接失败 */
    NETWORK_ERROR(6003, "网络连接失败"),
    
    // ==================== AI服务错误 7xxx ====================
    
    /** AI服务暂时不可用 */
    AI_SERVICE_ERROR(7001, "AI服务暂时不可用"),
    
    /** AI响应解析失败 */
    AI_RESPONSE_ERROR(7002, "AI响应解析失败"),
    
    // ==================== 系统错误 9xxx ====================
    
    /** 系统繁忙 */
    SYSTEM_ERROR(9001, "系统繁忙，请稍后再试"),
    
    /** 未知错误 */
    UNKNOWN_ERROR(9999, "未知错误");

    /** 错误码 */
    private final int code;
    
    /** 错误消息 */
    private final String message;

    /**
     * 构造函数
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码数值
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息字符串
     */
    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取对应的枚举值
     * 
     * @param code 错误码数值
     * @return 对应的错误码枚举，如果未找到则返回 UNKNOWN_ERROR
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}
