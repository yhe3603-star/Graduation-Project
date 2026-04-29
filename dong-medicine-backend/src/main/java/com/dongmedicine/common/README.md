# 公共模块目录 (common/)

> 类比：common 模块就像药铺的**工具箱** -- 称药的秤、包药的纸、贴标签的笔，这些工具每个岗位都需要用。公共模块里放的就是整个项目通用的工具类和基础类。

## 什么是公共模块？

在一个项目中，有很多代码是**各个模块都会用到**的。比如：
- 每个接口都需要统一的返回格式
- 每个地方都可能抛出业务异常
- 每个接口都需要获取当前登录用户
- 每个输入都需要检查有没有恶意代码

如果每个模块都自己写一套，就会重复代码、风格不一致。公共模块就是把这些**通用功能**集中在一起，让大家统一使用。

---

## 目录结构

```
common/
├── constant/                    # 常量定义
│   └── RoleConstants.java       #   角色常量（user/admin）
├── exception/                   # 异常处理
│   ├── BusinessException.java   #   业务异常类
│   ├── ErrorCode.java           #   错误码枚举
│   └── GlobalExceptionHandler.java  # 全局异常处理器
├── util/                        # 工具类
│   ├── FileCleanupHelper.java   #   文件清理助手
│   ├── FileTypeUtils.java       #   文件类型检测
│   ├── PageUtils.java           #   分页工具
│   ├── PasswordValidator.java   #   密码验证器
│   ├── SensitiveDataUtils.java  #   敏感数据脱敏
│   └── XssUtils.java            #   XSS防护工具
├── R.java                       # 统一响应封装
├── SecurityUtils.java           # 安全工具类
└── README.md
```

---

## 核心类详解

### 1. R.java -- 统一响应封装

> 类比：R 就像药铺的**标准药袋** -- 不管什么药，都装在同样格式的袋子里，外面贴上标签（状态码、消息、内容），顾客一看就知道药有没有配好。

所有 Controller 接口的返回值都使用 `R<T>` 包装，保证前端收到的响应格式一致。

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {
    private int code;          // 状态码：200=成功，其他=失败
    private String msg;        // 消息：成功时"success"，失败时错误描述
    private T data;            // 数据：泛型，可以是任何类型
    private String requestId;  // 请求ID：用于追踪问题
}
```

**使用示例**：

```java
@RestController
@RequestMapping("/api/plants")
public class PlantController {

    // 成功返回（无数据）
    @DeleteMapping("/{id}")
    public R<Void> deletePlant(@PathVariable Integer id) {
        plantService.deletePlant(id);
        return R.ok();  // {"code":200, "msg":"success", "data":null}
    }

    // 成功返回（有数据）
    @GetMapping("/{id}")
    public R<Plant> getPlant(@PathVariable Integer id) {
        Plant plant = plantService.getPlant(id);
        return R.ok(plant);  // {"code":200, "msg":"success", "data":{...}}
    }

    // 成功返回（自定义消息）
    @PostMapping
    public R<Plant> addPlant(@RequestBody PlantDTO dto) {
        Plant plant = plantService.addPlant(dto);
        return R.ok("添加成功", plant);  // {"code":200, "msg":"添加成功", "data":{...}}
    }

    // 失败返回
    @GetMapping("/search")
    public R<Page<Plant>> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return R.badRequest("搜索关键词不能为空");
            // {"code":400, "msg":"搜索关键词不能为空", "data":null}
        }
        return R.ok(plantService.search(keyword));
    }

    // 使用ErrorCode返回
    public R<Plant> getPlantById(Integer id) {
        Plant plant = plantService.getPlant(id);
        if (plant == null) {
            return R.error(ErrorCode.PLANT_NOT_FOUND);
            // {"code":2002, "msg":"植物信息不存在", "data":null}
        }
        return R.ok(plant);
    }
}
```

**R 提供的静态方法一览**：

| 方法 | code | 用途 |
|------|------|------|
| `R.ok()` | 200 | 成功，无数据 |
| `R.ok(data)` | 200 | 成功，有数据 |
| `R.ok(msg, data)` | 200 | 成功，自定义消息 |
| `R.error(msg)` | 500 | 通用错误 |
| `R.error(code, msg)` | 自定义 | 自定义错误码 |
| `R.error(ErrorCode)` | ErrorCode中的code | 使用错误码枚举 |
| `R.unauthorized(msg)` | 401 | 未登录 |
| `R.forbidden(msg)` | 403 | 权限不足 |
| `R.notFound(msg)` | 404 | 资源不存在 |
| `R.badRequest(msg)` | 400 | 请求参数错误 |

**常见错误**：
- Controller 直接返回实体类而不是 `R<T>`，导致前端收到的格式不一致
- 在 Service 层使用 `R.error()` -- R 应该只在 Controller 层使用，Service 层应该抛 `BusinessException`
- 忘记检查 `R.isSuccess()`，直接取 data 导致空指针

---

### 2. ErrorCode.java -- 错误码体系

> 类比：ErrorCode 就像药铺的**故障代码表** -- 每种故障都有一个编号，看到编号就知道是什么问题。

错误码按照模块分类，方便快速定位问题：

| 范围 | 模块 | 示例 |
|------|------|------|
| 0 | 成功 | `SUCCESS(0, "操作成功")` |
| 1xxx | 用户相关 | `USER_NOT_FOUND(1001)`, `PASSWORD_WRONG(1003)` |
| 2xxx | 资源相关 | `PLANT_NOT_FOUND(2002)`, `KNOWLEDGE_NOT_FOUND(2003)` |
| 3xxx | 参数相关 | `PARAM_ERROR(3001)`, `PARAM_MISSING(3002)` |
| 4xxx | 文件相关 | `FILE_UPLOAD_ERROR(4001)`, `FILE_SIZE_EXCEEDED(4003)` |
| 5xxx | 操作相关 | `DUPLICATE_OPERATION(5001)`, `OPERATION_TOO_FREQUENT(5002)` |
| 6xxx | 基础设施 | `DATABASE_ERROR(6001)`, `CACHE_ERROR(6002)` |
| 7xxx | AI服务 | `AI_SERVICE_ERROR(7001)` |
| 9xxx | 系统错误 | `SYSTEM_ERROR(9001)`, `UNKNOWN_ERROR(9999)` |

```java
// 使用示例
// 在 Service 中抛出异常时使用 ErrorCode
throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
throw new BusinessException(ErrorCode.PARAM_ERROR, "搜索关键词不能为空");

// 根据 code 反查 ErrorCode
ErrorCode errorCode = ErrorCode.getByCode(1001);  // 返回 USER_NOT_FOUND
```

**常见错误**：
- 新增错误码时不按分类编号，随意定义，导致混乱
- 错误消息写得太技术化（如 "NullPointerException"），用户看不懂
- 同一种错误在不同地方用不同的 code，前端难以统一处理

---

### 3. GlobalExceptionHandler.java -- 全局异常处理器

> 类比：GlobalExceptionHandler 就像药铺的**应急处理中心** -- 不管哪个岗位出了问题，都统一到这里处理，保证给顾客的回复格式一致。

不用全局异常处理器时，每个 Controller 都要写 try-catch，代码又多又乱。有了全局异常处理器，所有异常都会被自动捕获并转换成统一的 `R<T>` 格式返回给前端。

```java
@RestControllerAdvice  // 告诉 Spring：这个类负责处理所有 Controller 的异常
public class GlobalExceptionHandler {

    // 处理业务异常（我们主动抛出的）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        // 根据ErrorCode映射HTTP状态码
        HttpStatus status = mapBusinessErrorCode(e.getErrorCode());
        return ResponseEntity.status(status)
                .body(R.error(e.getErrorCode(), e.getMessage()));
    }

    // 处理参数验证异常（@Valid 触发的）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return R.error(ErrorCode.PARAM_FORMAT_ERROR, message);
    }

    // 处理数据库异常
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleDatabaseException(Exception e) {
        log.error("数据库异常: ", e);
        return R.error(ErrorCode.DATABASE_ERROR);
    }

    // 兜底：处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        log.error("未知异常: ", e);
        return R.error(ErrorCode.UNKNOWN_ERROR);
    }
}
```

**异常处理流程**：

```
Controller 中抛出异常
  |
  v
Spring 捕获异常，寻找匹配的 @ExceptionHandler
  |
  v
BusinessException?        --> 返回 R.error(ErrorCode, message) + 对应HTTP状态码
NotLoginException?        --> 返回 R.error(LOGIN_REQUIRED) + 401
NotRoleException?         --> 返回 R.error(PERMISSION_DENIED) + 403
MethodArgumentNotValid?   --> 返回 R.error(PARAM_FORMAT_ERROR, 验证消息)
DataAccessException?      --> 返回 R.error(DATABASE_ERROR)
AccessDeniedException?    --> 返回 R.error(PERMISSION_DENIED)
其他 Exception?           --> 返回 R.error(UNKNOWN_ERROR)
```

**ErrorCode 到 HTTP 状态码的映射**：

| ErrorCode | HTTP 状态码 | 含义 |
|-----------|-------------|------|
| USER_NOT_FOUND, PLANT_NOT_FOUND 等 | 404 | 资源不存在 |
| TOKEN_EXPIRED, TOKEN_INVALID | 401 | 未认证 |
| PERMISSION_DENIED | 403 | 无权限 |
| USER_ALREADY_EXISTS, DUPLICATE_OPERATION | 409 | 冲突 |
| OPERATION_TOO_FREQUENT | 429 | 请求太频繁 |
| PARAM_ERROR, PARAM_MISSING 等 | 400 | 参数错误 |
| SYSTEM_ERROR, DATABASE_ERROR | 500 | 服务器内部错误 |

**常见错误**：
- 在 Controller 中用 try-catch 捕获异常后返回 R.error()，而不是让全局处理器处理 -- 这会导致代码重复
- 在全局处理器中打印了完整的异常堆栈到生产日志，可能泄露敏感信息 -- 本项目已处理：生产环境只打印类名
- 异常处理器顺序不对，具体的异常要放在通用异常前面

---

### 4. SecurityUtils.java -- 安全工具类

> 类比：SecurityUtils 就像药铺的**前台查询系统**，随时能查到"当前来访者是谁、什么身份"。

封装了 Sa-Token 的 `StpUtil` API，提供更简洁的静态方法：

```java
public final class SecurityUtils {

    // 获取当前登录用户ID（未登录返回null）
    public static Integer getCurrentUserId() {
        if (!StpUtil.isLogin()) return null;
        return StpUtil.getLoginIdAsInt();
    }

    // 获取当前登录用户名
    public static String getCurrentUsername() {
        if (!StpUtil.isLogin()) return null;
        return StpUtil.getSession().get("username").toString();
    }

    // 获取当前用户角色
    public static String getCurrentUserRole() {
        if (!StpUtil.isLogin()) return null;
        return StpUtil.getSession().get("role").toString();
    }

    // 是否已登录
    public static boolean isAuthenticated() {
        return StpUtil.isLogin();
    }

    // 是否是管理员
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "admin".equalsIgnoreCase(role);
    }
}
```

**使用示例**：

```java
@Service
public class FavoriteServiceImpl implements FavoriteService {

    public void addFavorite(Integer plantId) {
        // 不需要从Controller传userId过来，直接获取
        Integer userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        // ... 收藏逻辑
    }
}
```

**在 Controller 中的使用：**

```java
@GetMapping("/me")
@SaCheckLogin
public R<User> me() {
    Integer userId = SecurityUtils.getCurrentUserId();
    return R.ok(userService.getUserInfo(userId));
}
```

**原理**：SecurityUtils 封装了 Sa-Token 的 `StpUtil` API，登录状态由 Sa-Token 的 SaTokenFilter 管理。

**常见错误**：
- 在异步线程中调用 SecurityUtils 返回 null -- Sa-Token 的上下文默认只在请求线程中可用，异步线程需要额外配置
- 在未登录的接口中调用 getCurrentUserId() 不做 null 检查，导致空指针异常
- 在构造函数或 @PostConstruct 方法中调用 -- 此时请求还没进来，Sa-Token 上下文是空的

---

### 5. XssUtils.java -- XSS防护工具

> 类比：XssUtils 就像药铺的**毒物检测仪** -- 检查输入的内容中有没有"毒药"（恶意脚本），有的话就中和掉。

XssUtils 提供了30多种危险模式的检测，是 XssFilter 的底层工具。详见 [util/README.md](util/README.md)。

---

### 6. PasswordValidator.java -- 密码验证器

> 类比：PasswordValidator 就像药铺的**锁匠** -- 检查你选的锁够不够安全。

```java
// 使用示例
PasswordValidator.ValidationResult result = PasswordValidator.validate("Abc12345");

if (result.isValid()) {
    System.out.println("密码强度: " + result.getStrengthLabel());  // "中等"
    System.out.println("强度分数: " + result.getStrength());        // 3
} else {
    System.out.println("错误: " + result.getMessage());  // "密码长度不能少于8位"
}
```

**密码规则**：
- 最短8位，最长50位
- 必须包含字母和数字
- 不能包含空格
- 强度评分：弱/一般/中等/强/非常强

详见 [util/README.md](util/README.md)。

---

## 各模块之间的关系

```
Controller 层
  |  使用 R<T> 返回结果
  |  使用 SecurityUtils 获取当前用户
  |  使用 @RateLimit 限流
  |
  v
Service 层
  |  抛出 BusinessException(ErrorCode)
  |  使用 PasswordValidator 验证密码
  |  使用 PageUtils 处理分页
  |
  v
全局异常处理器
  |  捕获所有异常
  |  转换为 R<T> 统一格式
  |
  v
前端收到统一格式的 JSON 响应
```

---

## 常见问题

**Q: 为什么 R 中有 requestId 字段？**
A: requestId 是由 RequestIdFilter 生成的唯一标识，贯穿整个请求链路。当用户反馈问题时，提供 requestId 可以快速在日志中定位到对应的请求记录。

**Q: 什么时候用 R.error()，什么时候抛 BusinessException？**
A: 简单的参数校验可以直接在 Controller 中返回 `R.badRequest()`。但涉及业务逻辑的错误（如"用户不存在"、"密码错误"），应该在 Service 层抛出 `BusinessException`，由全局异常处理器统一转换为 `R<T>`。这样代码更清晰，异常处理更集中。

**Q: SecurityUtils.getCurrentUserId() 返回 null 怎么办？**
A: 说明当前请求没有登录。如果这个接口需要登录，应该抛出 `BusinessException(ErrorCode.LOGIN_REQUIRED)`。如果接口是可选登录的，要做 null 判断。

---

## 代码审查与改进建议

- **[安全] SecurityUtils中获取当前用户ID的方式需确保Sa-Token会话有效**
- **[结构] R类的静态工厂方法应增加更多语义化方法**：如`R.created()`、`R.noContent()`等
