# 公共模块目录 (common)

本目录存放公共组件，包括常量定义、异常处理、工具类等。

## 📁 目录结构

```
common/
├── constant/         # 常量定义
├── exception/        # 异常处理
└── util/             # 工具类
```

## 📦 子目录说明

### 1. constant/ - 常量定义

存放系统常量定义类。

| 文件名 | 功能说明 |
|--------|----------|
| `RoleConstants.java` | 角色常量 |
| `StatusConstants.java` | 状态常量 |
| `CacheConstants.java` | 缓存Key常量 |

**示例:**
```java
public class RoleConstants {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
}
```

### 2. exception/ - 异常处理

存放自定义异常和全局异常处理器。

| 文件名 | 功能说明 |
|--------|----------|
| `BusinessException.java` | 业务异常类 |
| `ErrorCode.java` | 错误码定义 |
| `GlobalExceptionHandler.java` | 全局异常处理器 |

**BusinessException.java:**
```java
@Data
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;
    
    public BusinessException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

**ErrorCode.java:**
```java
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误");
    
    private final Integer code;
    private final String message;
}
```

**GlobalExceptionHandler.java:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        return R.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "系统异常");
    }
}
```

### 3. util/ - 工具类

存放通用工具方法。

| 文件名 | 功能说明 |
|--------|----------|
| `SecurityUtils.java` | 安全工具类 |
| `PageUtils.java` | 分页工具类 |
| `FileTypeUtils.java` | 文件类型工具类 |
| `PasswordValidator.java` | 密码验证工具类 |
| `XssUtils.java` | XSS防护工具类 |
| `SensitiveDataUtils.java` | 敏感数据处理工具类 |
| `FileCleanupHelper.java` | 文件清理辅助类 |

**SecurityUtils.java:**
```java
public class SecurityUtils {
    
    /**
     * 获取当前登录用户ID
     */
    public static Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            return ((JwtUser) auth.getPrincipal()).getId();
        }
        return null;
    }
    
    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
    
    /**
     * 判断是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
```

**PageUtils.java:**
```java
public class PageUtils {
    
    /**
     * 获取分页对象
     */
    public static <T> Page<T> getPage(Integer page, Integer size) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSize = size != null && size > 0 ? size : 10;
        return new Page<>(pageNum, pageSize);
    }
}
```

## 🎯 工具类规范

### 命名规范
- 工具类以`Utils`或`Helper`结尾
- 方法名使用动词开头

### 代码规范
```java
/**
 * 工具类说明
 */
public final class ExampleUtils {
    
    // 私有构造函数，防止实例化
    private ExampleUtils() {}
    
    /**
     * 方法说明
     * @param param 参数说明
     * @return 返回值说明
     */
    public static String doSomething(String param) {
        // 实现
        return result;
    }
}
```

### 最佳实践
1. **私有构造**: 工具类应有私有构造函数
2. **静态方法**: 工具方法应为静态方法
3. **无状态**: 工具类不应有实例变量
4. **文档完善**: 添加方法注释和参数说明

## 📚 扩展阅读

- [Java 异常处理最佳实践](https://docs.oracle.com/javase/tutorial/essential/exceptions/)
- [Spring 异常处理](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc)
