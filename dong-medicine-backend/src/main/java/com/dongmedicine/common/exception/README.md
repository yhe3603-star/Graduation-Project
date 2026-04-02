# 异常目录 (exception)

本目录存放异常处理相关的类。

## 文件列表

| 文件 | 功能描述 |
|------|----------|
| BusinessException.java | 业务异常类 |
| ErrorCode.java | 错误码定义 |
| GlobalExceptionHandler.java | 全局异常处理器 |

---

## BusinessException.java - 业务异常

用于业务逻辑中抛出的异常。

```java
/**
 * 业务异常类
 * 当业务规则不满足时抛出
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final String message;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
```

---

## ErrorCode.java - 错误码定义

定义系统中的错误码。

```java
/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    // 通用错误
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_BANNED(1004, "账号已被封禁");
    
    private final int code;
    private final String msg;
    
    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
```

---

## GlobalExceptionHandler.java - 全局异常处理器

捕获所有异常，统一返回错误响应。

```java
/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException e) {
        return R.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e) {
        return R.fail("系统繁忙");
    }
}
```

---

## 使用示例

```java
@Service
public class UserServiceImpl implements UserService {
    
    public User login(LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());
        
        // 抛出业务异常
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        return user;
    }
}
```

---

**最后更新时间**：2026年4月3日
