# Common 模块 -- 通用工具与基础组件

> 通用模块是项目的"公共工具箱"——统一响应格式、异常处理、安全工具、常量定义等，所有模块共享。

---

## 一、目录结构

```
common/
├── constant/
│   ├── ApiPaths.java              # API路径常量（按模块组织）
│   ├── RabbitMQConstants.java     # RabbitMQ队列/交换机/路由键常量
│   └── RoleConstants.java         # 角色常量（ROLE_USER="user", ROLE_ADMIN="admin"）
├── exception/
│   ├── BusinessException.java     # 业务异常类（30+种静态工厂方法）
│   ├── ErrorCode.java             # 错误码枚举（按模块分类：1xxx-9xxx）
│   └── GlobalExceptionHandler.java # 全局异常处理器（20+种异常类型映射）
├── util/
│   ├── FileCleanupHelper.java     # 文件清理助手（解析JSON字段中的文件路径并删除）
│   ├── FileTypeUtils.java         # 文件类型检测（MIME类型、扩展名判断）
│   ├── IpUtils.java               # 客户端IP获取（处理代理/反向代理）
│   ├── PageUtils.java             # 分页工具（getPage归一化 + escapeLike防注入 + toMap转换）
│   ├── PasswordValidator.java     # 密码强度验证器（长度/字符类型/强度评分）
│   ├── SensitiveDataUtils.java    # 敏感数据脱敏（手机号/身份证/邮箱等）
│   └── XssUtils.java              # XSS检测与清洗（30+种危险模式）
├── R.java                         # 统一响应封装（泛型类，10+种静态工厂方法）
└── SecurityUtils.java             # 安全工具类（封装Sa-Token StpUtil，提供静态方法）
```

---

## 二、R.java -- 统一响应封装

文件：`common/R.java`

项目最核心的通用类之一。所有Controller接口都返回 `R<T>`，保证前端收到的JSON格式完全一致。

```java
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

    private int code;           // 状态码
    private String msg;         // 提示信息
    private T data;             // 实际数据（泛型）
    private String requestId;   // 请求追踪ID（从MDC获取）

    // 静态工厂方法
    public static <T> R<T> ok()                        { return new R<>(200, "success", null, getRequestId()); }
    public static <T> R<T> ok(T data)                  { return new R<>(200, "success", data, getRequestId()); }
    public static <T> R<T> ok(String msg, T data)      { return new R<>(200, msg, data, getRequestId()); }
    public static <T> R<T> error(String msg)           { return new R<>(500, msg, null, getRequestId()); }
    public static <T> R<T> error(int code, String msg) { return new R<>(code, msg, null, getRequestId()); }
    public static <T> R<T> error(ErrorCode errorCode)  { return new R<>(errorCode.getCode(), errorCode.getMessage(), null, getRequestId()); }
    public static <T> R<T> error(ErrorCode ec, String msg) { return new R<>(ec.getCode(), msg, null, getRequestId()); }
    public static <T> R<T> unauthorized(String msg)    { return new R<>(401, msg, null, getRequestId()); }
    public static <T> R<T> forbidden(String msg)       { return new R<>(403, msg, null, getRequestId()); }
    public static <T> R<T> notFound(String msg)        { return new R<>(404, msg, null, getRequestId()); }
    public static <T> R<T> badRequest(String msg)      { return new R<>(400, msg, null, getRequestId()); }

    public boolean isSuccess() { return this.code == SUCCESS; }

    private static String getRequestId() {
        String requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY);
        return requestId != null ? requestId : "";
    }
}
```

**requestId来源**：由 `RequestIdFilter` 在每个请求开始时生成16位UUID，放入SLF4J MDC上下文。`R`类通过MDC获取，确保每个响应都包含可追踪的requestId。

**Typical使用**：
```java
// Controller中
return R.ok(data);                              // 200 + data
return R.ok("操作成功");                         // 200 + msg
return R.error("服务器错误");                     // 500
return R.badRequest("参数错误");                 // 400
return R.unauthorized("请先登录");               // 401
return R.forbidden("权限不足");                  // 403
return R.notFound("资源不存在");                 // 404
return R.error(ErrorCode.USER_NOT_FOUND);       // 1001 + "用户不存在"
```

---

## 三、异常体系

### 3.1 ErrorCode -- 错误码枚举

文件：`common/exception/ErrorCode.java`

按模块范围分类，共30+个错误码：

| 范围 | 模块 | 示例 |
|------|------|------|
| 200 | 成功 | SUCCESS(200, "操作成功") |
| 1xxx | 用户 | USER_NOT_FOUND(1001), USER_ALREADY_EXISTS(1002), PASSWORD_WRONG(1003), PASSWORD_TOO_WEAK(1004), TOKEN_EXPIRED(1005), TOKEN_INVALID(1006), PERMISSION_DENIED(1007), ACCOUNT_DISABLED(1008), LOGIN_REQUIRED(1009) |
| 2xxx | 资源 | RESOURCE_NOT_FOUND(2001), PLANT_NOT_FOUND(2002), KNOWLEDGE_NOT_FOUND(2003), INHERITOR_NOT_FOUND(2004) |
| 3xxx | 参数 | PARAM_ERROR(3001), PARAM_MISSING(3002), PARAM_FORMAT_ERROR(3003), PARAM_OUT_OF_RANGE(3004) |
| 4xxx | 文件 | FILE_UPLOAD_ERROR(4001), FILE_TYPE_NOT_ALLOWED(4002), FILE_SIZE_EXCEEDED(4003), FILE_NOT_FOUND(4004) |
| 5xxx | 操作 | DUPLICATE_OPERATION(5001), OPERATION_TOO_FREQUENT(5002), OPERATION_FAILED(5003) |
| 6xxx | 基础设施 | DATABASE_ERROR(6001), CACHE_ERROR(6002), NETWORK_ERROR(6003) |
| 7xxx | AI | AI_SERVICE_ERROR(7001), AI_RESPONSE_ERROR(7002) |
| 9xxx | 系统 | SYSTEM_ERROR(9001), UNKNOWN_ERROR(9999) |

### 3.2 BusinessException -- 业务异常

文件：`common/exception/BusinessException.java`

```java
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    // 构造函数
    public BusinessException(ErrorCode errorCode) { ... }
    public BusinessException(ErrorCode errorCode, String message) { ... }

    // 静态工厂方法（30+种）
    public static BusinessException notFound(String msg)   { return new BusinessException(RESOURCE_NOT_FOUND, msg); }
    public static BusinessException badRequest(String msg) { return new BusinessException(PARAM_ERROR, msg); }
    public static BusinessException unauthorized(String m) { return new BusinessException(TOKEN_INVALID, msg); }
    public static BusinessException forbidden(String msg)  { return new BusinessException(PERMISSION_DENIED, msg); }
    public static BusinessException conflict(String msg)   { return new BusinessException(DUPLICATE_OPERATION, msg); }
    public static BusinessException userNotFound()         { return new BusinessException(USER_NOT_FOUND); }
    public static BusinessException userAlreadyExists()    { return new BusinessException(USER_ALREADY_EXISTS); }
    public static BusinessException passwordWrong()        { return new BusinessException(PASSWORD_WRONG); }
    public static BusinessException passwordTooWeak()      { return new BusinessException(PASSWORD_TOO_WEAK); }
    public static BusinessException permissionDenied()     { return new BusinessException(PERMISSION_DENIED); }
    public static BusinessException systemError()          { return new BusinessException(SYSTEM_ERROR); }
}
```

### 3.3 GlobalExceptionHandler -- 全局异常处理

文件：`common/exception/GlobalExceptionHandler.java`

使用 `@RestControllerAdvice` 统一拦截所有Controller的异常，返回统一的 `R<T>` 格式。

处理的异常类型（20+种）：

| 异常类型 | HTTP状态码 | 错误码 |
|---------|-----------|--------|
| BusinessException | 根据ErrorCode映射 | 根据ErrorCode |
| NotLoginException (Sa-Token) | 401 | LOGIN_REQUIRED |
| NotRoleException (Sa-Token) | 403 | PERMISSION_DENIED |
| MethodArgumentNotValidException | 400 | PARAM_FORMAT_ERROR |
| BindException | 400 | PARAM_FORMAT_ERROR |
| ConstraintViolationException | 400 | PARAM_FORMAT_ERROR |
| MissingServletRequestParameterException | 400 | PARAM_MISSING |
| MethodArgumentTypeMismatchException | 400 | PARAM_FORMAT_ERROR |
| HttpMessageNotReadableException | 400 | PARAM_FORMAT_ERROR |
| HttpRequestMethodNotSupportedException | 405 | PARAM_FORMAT_ERROR |
| MaxUploadSizeExceededException | 400 | FILE_SIZE_EXCEEDED |
| MultipartException | 400 | FILE_UPLOAD_ERROR |
| DataIntegrityViolationException | 409 | DUPLICATE_OPERATION |
| EmptyResultDataAccessException | 404 | RESOURCE_NOT_FOUND |
| DataAccessException / SQLException | 500 | DATABASE_ERROR |
| SocketTimeoutException | 504 | NETWORK_ERROR |
| NullPointerException | 500 | SYSTEM_ERROR |
| RuntimeException | 500 | SYSTEM_ERROR |
| Exception（兜底） | 500 | UNKNOWN_ERROR |

**生产环境保护**：在生产环境(`spring.profiles.active=prod`)下，系统异常只记录异常类名，不输出完整堆栈，防止敏感信息泄露。

---

## 四、SecurityUtils -- 安全工具类

文件：`common/SecurityUtils.java`

封装 Sa-Token 的 `StpUtil` API，提供静态方法简化调用：

```java
public final class SecurityUtils {
    // 获取当前用户ID（未登录抛异常）
    public static Integer getCurrentUserId() { ... }

    // 获取当前用户ID（未登录返回null）
    public static Integer getCurrentUserIdOrNull() { ... }

    // 获取当前用户名（未登录抛异常）
    public static String getCurrentUsername() { ... }

    // 获取当前用户名（未登录返回null）
    public static String getCurrentUsernameOrNull() { ... }

    // 获取当前用户角色（未登录返回"user"）
    public static String getCurrentUserRole() { ... }

    // 是否已登录
    public static boolean isAuthenticated() { return StpUtil.isLogin(); }

    // 是否管理员
    public static boolean isAdmin() { ... }
}
```

**使用示例**：
```java
// Controller中
Integer userId = SecurityUtils.getCurrentUserId();

// Service中（获取可选登录用户）
Integer userId = SecurityUtils.getCurrentUserIdOrNull();
if (userId != null) {
    browseHistoryService.record(userId, "plant", plantId);
}
```

---

## 五、常量定义

### 5.1 RoleConstants -- 角色常量

```java
public final class RoleConstants {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static boolean isValid(String role) {
        return ROLE_USER.equals(role) || ROLE_ADMIN.equals(role);
    }
}
```

### 5.2 RabbitMQConstants -- RabbitMQ常量

```java
public class RabbitMQConstants {
    public static final String EXCHANGE_DIRECT = "dong.medicine.direct";
    public static final String EXCHANGE_TOPIC = "dong.medicine.topic";

    public static final String QUEUE_OPERATION_LOG = "operation.log.queue";
    public static final String QUEUE_FEEDBACK = "feedback.queue";
    public static final String QUEUE_FILE_PROCESS = "file.process.queue";
    public static final String QUEUE_STATISTICS = "statistics.queue";
    public static final String QUEUE_NOTIFICATION = "notification.queue";

    // 路由键 + 死信队列常量 ...
}
```

### 5.3 ApiPaths -- API路径常量

按模块组织（User/Knowledge/Inheritor/Plant/Qa/Quiz/PlantGame/Resource/Comment/Favorite/Feedback/Visual/Admin/File/Chat），但当前Controller中未统一使用该常量类。

---

## 六、工具类

### 6.1 PageUtils -- 分页工具

```java
public class PageUtils {
    // 创建标准化分页对象（page>=1, size 1-100）
    public static <T> Page<T> getPage(Integer page, Integer size) { ... }

    // 转义LIKE查询特殊字符（防止LIKE注入）
    public static String escapeLike(String keyword) {
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // 将Page结果转换为前端需要的Map格式
    public static Map<String, Object> toMap(Page<?> pageResult) {
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return data;
    }
}
```

### 6.2 XssUtils -- XSS防护工具

```java
public final class XssUtils {
    // 30+种危险模式正则：<script>, javascript:, on*=, <iframe>, eval(), expression()等
    private static final Pattern[] DANGEROUS_PATTERNS = { ... };

    // 检测是否包含XSS攻击代码
    public static boolean containsXss(String input) { ... }

    // HTML实体编码清洗（& → &amp;, < → &lt;, > → &gt;）
    public static String sanitize(String input) { ... }

    // 移除HTML标签（删除<script>...</script>等）
    public static String sanitizeHtml(String input) { ... }

    // URL安全校验（只允许http/https/相对路径）
    public static String sanitizeUrl(String url) { ... }

    // 文件名安全处理（移除路径分隔符、限制长度255）
    public static String sanitizeFileName(String fileName) { ... }

    // SQL注入检测
    public static boolean containsSqlInjection(String input) { ... }
}
```

### 6.3 PasswordValidator -- 密码强度验证

```java
public class PasswordValidator {
    public static ValidationResult validate(String password) {
        // 规则：
        // - 长度：6-50位
        // - 必须包含字母和数字
        // - 不能包含空格
        // - 强度评分：弱/一般/中等/强/非常强
    }
}
```

### 6.4 IpUtils -- 客户端IP获取

```java
public class IpUtils {
    // 从请求中获取真实客户端IP
    // 处理反向代理情况：X-Forwarded-For → X-Real-IP → RemoteAddr
    public static String getClientIp(HttpServletRequest request) { ... }
}
```

### 6.5 FileCleanupHelper -- 文件清理

```java
@Component
public class FileCleanupHelper {
    // 解析JSON字符串（如["/images/plants/1.jpg"]）中的文件路径
    // 逐个删除磁盘上的文件
    public void deleteFilesFromJson(String jsonStr) { ... }
}
```

### 6.6 FileTypeUtils -- 文件类型判断

```java
public class FileTypeUtils {
    // 根据文件扩展名获取MIME类型
    public static String getMimeType(String fileName) { ... }

    // 获取文件展示名称（资源名 + 扩展名）
    public static String getDisplayName(String fileName, String resourceTitle) { ... }
}
```
