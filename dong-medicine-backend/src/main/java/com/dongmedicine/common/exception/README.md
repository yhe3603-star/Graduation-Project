# 异常处理目录 (common/exception/)

> 类比：异常就像药铺里的**意外情况** -- 本来按方抓药一切顺利，但突然发现某味药缺货了、药方写错了、顾客没带钱。异常处理就是遇到这些意外时，不慌不乱地给顾客一个清晰的回复。

## 什么是异常？

程序运行中，不是所有事情都能按计划进行。比如：
- 用户查一个不存在的植物 --> 数据找不到
- 用户输入了错误的密码 --> 验证失败
- 数据库突然断了 --> 系统故障

这些"意外情况"在 Java 中就是**异常（Exception）**。如果不处理异常，程序会崩溃并给用户显示一堆看不懂的错误信息。所以我们需要**优雅地处理异常**，给用户友好的提示。

---

## 目录结构

```
exception/
├── BusinessException.java        # 业务异常类
├── ErrorCode.java                # 错误码枚举
└── GlobalExceptionHandler.java   # 全局异常处理器
```

---

## 三个类的协作关系

```
Service 层发现问题
  |
  |  throw new BusinessException(ErrorCode.PLANT_NOT_FOUND)
  v
BusinessException 携带错误码和消息
  |
  v
GlobalExceptionHandler 捕获异常
  |
  |  转换为 R.error(ErrorCode, message)
  |  设置对应的 HTTP 状态码
  v
前端收到统一格式的错误响应
  {"code": 2002, "msg": "植物信息不存在", "data": null}
```

---

## 1. ErrorCode -- 错误码枚举

> 类比：ErrorCode 就像药铺的**故障代码表** -- 每种故障都有一个编号和说明，看到编号就知道出了什么问题。

### 错误码分类

```java
public enum ErrorCode {

    // ==================== 成功 ====================
    SUCCESS(0, "操作成功"),

    // ==================== 用户相关 1xxx ====================
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

    // ==================== 资源相关 2xxx ====================
    RESOURCE_NOT_FOUND(2001, "资源不存在"),
    PLANT_NOT_FOUND(2002, "植物信息不存在"),
    KNOWLEDGE_NOT_FOUND(2003, "知识条目不存在"),
    INHERITOR_NOT_FOUND(2004, "传承人信息不存在"),
    FEEDBACK_NOT_FOUND(2005, "反馈信息不存在"),
    COMMENT_NOT_FOUND(2006, "评论不存在"),

    // ==================== 参数相关 3xxx ====================
    PARAM_ERROR(3001, "参数错误"),
    PARAM_MISSING(3002, "缺少必要参数"),
    PARAM_FORMAT_ERROR(3003, "参数格式错误"),
    PARAM_OUT_OF_RANGE(3004, "参数超出范围"),

    // ==================== 文件相关 4xxx ====================
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4002, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),
    FILE_NOT_FOUND(4004, "文件不存在"),

    // ==================== 操作相关 5xxx ====================
    DUPLICATE_OPERATION(5001, "重复操作"),
    OPERATION_TOO_FREQUENT(5002, "操作过于频繁，请稍后再试"),
    OPERATION_FAILED(5003, "操作失败"),

    // ==================== 基础设施 6xxx ====================
    DATABASE_ERROR(6001, "数据库操作失败"),
    CACHE_ERROR(6002, "缓存操作失败"),
    NETWORK_ERROR(6003, "网络连接失败"),

    // ==================== AI服务 7xxx ====================
    AI_SERVICE_ERROR(7001, "AI服务暂时不可用"),
    AI_RESPONSE_ERROR(7002, "AI响应解析失败"),

    // ==================== 系统错误 9xxx ====================
    SYSTEM_ERROR(9001, "系统繁忙，请稍后再试"),
    UNKNOWN_ERROR(9999, "未知错误");

    private final int code;
    private final String message;
}
```

### 错误码编号规则

| 范围 | 模块 | 设计思路 |
|------|------|----------|
| 0 | 成功 | 0 表示没有错误 |
| 1xxx | 用户 | 用户是最核心的实体，排第一 |
| 2xxx | 资源 | 植物、知识、传承人等业务资源 |
| 3xxx | 参数 | 请求参数相关错误 |
| 4xxx | 文件 | 文件上传/下载相关 |
| 5xxx | 操作 | 业务操作相关（重复、频繁等） |
| 6xxx | 基础设施 | 数据库、缓存、网络 |
| 7xxx | AI服务 | DeepSeek AI 对话 |
| 9xxx | 系统 | 未知错误、系统级故障 |

**注意**：8xxx 空缺，留给未来扩展。

### ErrorCode 提供的方法

```java
// 获取错误码数值
int code = ErrorCode.PLANT_NOT_FOUND.getCode();     // 2002

// 获取错误消息
String msg = ErrorCode.PLANT_NOT_FOUND.getMessage(); // "植物信息不存在"

// 根据错误码反查枚举
ErrorCode error = ErrorCode.getByCode(2002);         // PLANT_NOT_FOUND
ErrorCode unknown = ErrorCode.getByCode(8888);       // UNKNOWN_ERROR（找不到时返回这个）
```

---

## 2. BusinessException -- 业务异常类

> 类比：BusinessException 就像药铺里的**标准告示牌** -- 每种意外情况都有对应的告示牌，药师遇到问题时举起对应的牌子，顾客一看就知道怎么回事。

### 为什么需要自定义异常？

Java 自带的异常（如 `RuntimeException`、`NullPointerException`）太笼统了，无法表达业务含义。比如：
- `RuntimeException("用户不存在")` -- 没有错误码，前端无法根据类型做不同处理
- `BusinessException(ErrorCode.USER_NOT_FOUND)` -- 有明确错误码 1001，前端可以精准处理

### 构造方式

```java
// 方式1：使用错误码（最常用）
throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
// 错误码: 2002, 消息: "植物信息不存在"

// 方式2：使用错误码 + 自定义消息
throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");
// 错误码: 3001, 消息: "搜索关键词不能为空"（覆盖默认消息）

// 方式3：使用错误码 + 自定义消息 + 原始异常
throw new BusinessException(ErrorCode.DATABASE_ERROR, "查询失败", sqlException);
// 保留原始异常的堆栈信息，方便调试
```

### 静态工厂方法（推荐使用）

BusinessException 提供了语义更清晰的静态方法：

```java
// 资源未找到
throw BusinessException.notFound("该植物信息不存在");

// 未授权
throw BusinessException.unauthorized("请先登录");

// 权限不足
throw BusinessException.forbidden("只有管理员才能执行此操作");

// 参数错误
throw BusinessException.badRequest("页码不能为负数");

// 资源冲突
throw BusinessException.conflict("该用户名已被注册");

// 快捷方式
throw BusinessException.userNotFound();          // 用户不存在
throw BusinessException.userAlreadyExists();     // 用户已存在
throw BusinessException.passwordWrong();         // 密码错误
throw BusinessException.passwordTooWeak();       // 密码太弱
throw BusinessException.permissionDenied();      // 权限不足
throw BusinessException.systemError();           // 系统错误
```

---

## 3. GlobalExceptionHandler -- 全局异常处理器

> 类比：GlobalExceptionHandler 就像药铺的**前台接待** -- 不管哪个岗位出了问题，最终都由前台统一给顾客回复，保证回复格式一致、态度友好。

### 工作原理

`@RestControllerAdvice` 注解告诉 Spring：这个类会拦截所有 Controller 抛出的异常。`@ExceptionHandler` 注解标记每个方法处理哪种异常。

### 异常处理优先级

Spring 会匹配**最具体**的异常类型。比如抛出了 `ExpiredJwtException`，它会优先匹配 `ExpiredJwtException` 的处理器，而不是 `JwtException` 或 `Exception` 的处理器。

```
异常抛出
  |
  v
Spring 寻找匹配的 @ExceptionHandler
  |
  |-- BusinessException              --> handleBusinessException()
  |-- MethodArgumentNotValidException --> handleValidationException()
  |-- ExpiredJwtException            --> handleExpiredJwtException()
  |-- AccessDeniedException          --> handleAccessDeniedException()
  |-- DataAccessException            --> handleDatabaseException()
  |-- NullPointerException           --> handleNullPointerException()
  |-- RuntimeException               --> handleRuntimeException()
  |-- Exception (兜底)               --> handleException()
```

### 核心处理方法详解

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 业务异常 -- 我们主动抛出的，最常见
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        // 根据ErrorCode映射HTTP状态码（如 NOT_FOUND -> 404, UNAUTHORIZED -> 401）
        HttpStatus status = mapBusinessErrorCode(e.getErrorCode());
        return ResponseEntity.status(status)
                .body(R.error(e.getErrorCode(), e.getMessage()));
    }

    // 2. 参数验证异常 -- @Valid 注解触发的
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        // 收集所有字段的错误消息，用逗号拼接
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        // 例: "用户名不能为空, 密码长度不能少于8位"
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, message);
    }

    // 3. JWT过期异常
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleExpiredJwtException(ExpiredJwtException e) {
        return R.error(ErrorCode.TOKEN_EXPIRED);
    }

    // 4. 权限不足异常
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        return R.error(ErrorCode.PERMISSION_DENIED);
    }

    // 5. 数据库异常 -- 生产环境只记录类名，不暴露细节
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleDatabaseException(Exception e) {
        if (isProd()) {
            log.error("数据库异常: {}", e.getClass().getSimpleName());
        } else {
            log.error("数据库异常: ", e);  // 开发环境打印完整堆栈
        }
        return R.error(ErrorCode.DATABASE_ERROR);
    }

    // 6. 兜底 -- 所有未匹配的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        log.error("未知异常: ", e);
        return R.error(ErrorCode.UNKNOWN_ERROR);
    }
}
```

### ErrorCode 到 HTTP 状态码的映射

```java
private HttpStatus mapBusinessErrorCode(ErrorCode errorCode) {
    return switch (errorCode) {
        // 404 NOT FOUND -- 资源不存在
        case USER_NOT_FOUND, PLANT_NOT_FOUND, KNOWLEDGE_NOT_FOUND,
             INHERITOR_NOT_FOUND, FEEDBACK_NOT_FOUND, COMMENT_NOT_FOUND,
             FILE_NOT_FOUND, RESOURCE_NOT_FOUND
             -> HttpStatus.NOT_FOUND;

        // 401 UNAUTHORIZED -- 未认证
        case TOKEN_EXPIRED, TOKEN_INVALID, LOGIN_REQUIRED
             -> HttpStatus.UNAUTHORIZED;

        // 403 FORBIDDEN -- 无权限
        case PERMISSION_DENIED, ACCOUNT_DISABLED
             -> HttpStatus.FORBIDDEN;

        // 409 CONFLICT -- 冲突
        case USER_ALREADY_EXISTS, DUPLICATE_OPERATION
             -> HttpStatus.CONFLICT;

        // 429 TOO MANY REQUESTS -- 请求太频繁
        case OPERATION_TOO_FREQUENT
             -> HttpStatus.TOO_MANY_REQUESTS;

        // 400 BAD REQUEST -- 参数错误
        case PARAM_ERROR, PARAM_MISSING, PARAM_FORMAT_ERROR, PARAM_OUT_OF_RANGE,
             USERNAME_TOO_SHORT, USERNAME_FORMAT_ERROR, PASSWORD_TOO_WEAK,
             FILE_SIZE_EXCEEDED, FILE_TYPE_NOT_ALLOWED, FILE_UPLOAD_ERROR
             -> HttpStatus.BAD_REQUEST;

        // 500 INTERNAL SERVER ERROR -- 服务器错误
        default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
}
```

### 生产环境 vs 开发环境的日志策略

```java
@Value("${spring.profiles.active:dev}")
private String activeProfile;

private boolean isProd() {
    return "prod".equalsIgnoreCase(activeProfile);
}

// 生产环境：只记录异常类名，不暴露堆栈细节
if (isProd()) {
    log.error("数据库异常: {}", e.getClass().getSimpleName());
}
// 开发环境：打印完整堆栈，方便调试
else {
    log.error("数据库异常: ", e);
}
```

**为什么要区分？** 生产环境的日志可能被很多人看到，完整的异常堆栈可能包含数据库连接字符串、SQL语句等敏感信息。

---

## 在 Service 中如何使用异常

### 正确示例

```java
@Service
public class PlantServiceImpl implements PlantService {

    @Autowired
    private PlantMapper plantMapper;

    public Plant getPlantById(Integer id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw BusinessException.badRequest("植物ID无效");
        }

        // 2. 查询数据
        Plant plant = plantMapper.selectById(id);

        // 3. 数据不存在 --> 抛出业务异常
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
            // 或者: throw BusinessException.notFound("植物信息不存在");
        }

        // 4. 权限检查
        if (!SecurityUtils.isAdmin() && plant.isDraft()) {
            throw BusinessException.forbidden("无权查看草稿状态的植物信息");
        }

        return plant;
    }

    public void addPlant(PlantDTO dto) {
        // 5. 业务规则校验
        if (plantMapper.existsByName(dto.getName())) {
            throw BusinessException.conflict("该植物名称已存在");
        }

        // 6. 密码强度检查
        // PasswordValidator.validate() 只返回结果，不抛异常
        // 需要我们手动判断并抛出
        PasswordValidator.ValidationResult result = PasswordValidator.validate(dto.getPassword());
        if (!result.isValid()) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK, result.getMessage());
        }

        // 7. 正常业务逻辑
        Plant plant = new Plant();
        // ... 设置属性
        plantMapper.insert(plant);
    }
}
```

### 错误示例

```java
// 错误1：用 RuntimeException 代替 BusinessException
throw new RuntimeException("植物不存在");
// 问题：没有错误码，前端无法区分不同类型的错误

// 错误2：在 Service 中返回 R.error()
public R<Plant> getPlant(Integer id) {
    Plant plant = plantMapper.selectById(id);
    if (plant == null) {
        return R.error(ErrorCode.PLANT_NOT_FOUND);  // 不应该在Service中用R
    }
    return R.ok(plant);
}
// 问题：R 是 Controller 层的响应封装，Service 层不应该知道它

// 错误3：吞掉异常
try {
    plantMapper.insert(plant);
} catch (Exception e) {
    // 什么都不做，静默失败
}
// 问题：出了问题完全不知道，排查时无从下手

// 错误4：异常信息太技术化
throw new BusinessException(ErrorCode.SYSTEM_ERROR,
    "NullPointerException at PlantServiceImpl.java:42");
// 问题：用户看不懂，而且暴露了代码结构
```

---

## 异常处理完整流程图

```
用户请求: GET /api/plants/999
  |
  v
Controller: plantController.getPlant(999)
  |
  v
Service: plantService.getPlantById(999)
  |
  v
数据库查询: SELECT * FROM plant WHERE id = 999
  |
  v
结果为 null
  |
  v
抛出异常: throw new BusinessException(ErrorCode.PLANT_NOT_FOUND)
  |
  v
GlobalExceptionHandler 捕获
  |
  v
映射HTTP状态码: PLANT_NOT_FOUND --> 404
  |
  v
构造响应: R.error(ErrorCode.PLANT_NOT_FOUND)
  |
  v
返回给前端:
  HTTP/1.1 404 Not Found
  Content-Type: application/json

  {
    "code": 2002,
    "msg": "植物信息不存在",
    "data": null,
    "requestId": "a1b2c3d4-e5f6-7890"
  }
```

---

## 常见问题

**Q: 什么时候抛 BusinessException，什么时候返回 R.error()？**
A: 简单说：**Service 层抛异常，Controller 层返回 R**。Service 层只关心业务逻辑，不应该知道响应格式。Controller 层负责把异常转换为 R。对于简单的参数校验（如判空），可以在 Controller 中直接返回 `R.badRequest()`。

**Q: 新增错误码应该放在哪个范围？**
A: 按照分类规则：用户相关放 1xxx，资源相关放 2xxx，以此类推。同一类中按顺序递增。如果现有分类不够用，8xxx 是预留的扩展区间。

**Q: 为什么有些异常返回 ResponseEntity，有些直接返回 R？**
A: 返回 `ResponseEntity` 是为了自定义 HTTP 状态码（如 BusinessException 需要根据 ErrorCode 映射不同的状态码）。直接返回 `R` 的方法用 `@ResponseStatus` 注解指定了固定的状态码。

**Q: 全局异常处理器没有捕获到异常？**
A: 检查以下几点：
1. 类上是否有 `@RestControllerAdvice` 注解
2. 异常是否在 Controller 之前就被处理了（如 Filter 中的异常）
3. 异常类型是否和 `@ExceptionHandler` 中声明的一致（注意子类关系）

---

## 代码审查与改进建议

- **[性能] ErrorCode.getByCode()线性查找效率低**：应使用`static final Map`缓存
