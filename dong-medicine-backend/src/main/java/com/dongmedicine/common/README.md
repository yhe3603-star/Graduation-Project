# 公共模块 (common)

本目录存放项目中通用的工具类、常量定义和异常处理等。

## 目录

- [什么是公共模块？](#什么是公共模块)
- [目录结构](#目录结构)
- [统一响应封装](#统一响应封装)
- [异常处理](#异常处理)
- [工具类](#工具类)
- [常量定义](#常量定义)

---

## 什么是公共模块？

**公共模块**是项目中多个地方都会使用的通用代码。就像家里的"工具箱"——里面有各种工具，需要的时候随时可以拿来用。

### 为什么需要公共模块？

```
┌─────────────────────────────────────────────────────────────────┐
│                     没有公共模块                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Controller A ─── 自己写响应格式                                 │
│  Controller B ─── 自己写响应格式（重复代码）                      │
│  Controller C ─── 自己写响应格式（重复代码）                      │
│  Service A   ─── 自己写异常处理                                  │
│  Service B   ─── 自己写异常处理（重复代码）                       │
│                                                                 │
│  → 代码重复、难以维护、风格不统一                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      有公共模块                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Controller A ─┐                                                │
│  Controller B ─┼──→ 使用统一的 R<T> 响应格式                     │
│  Controller C ─┘                                                │
│                                                                 │
│  Service A   ─┬──→ 使用统一的异常处理                            │
│  Service B   ─┘                                                 │
│                                                                 │
│  → 代码复用、易于维护、风格统一                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
common/
│
├── constant/                          # 常量定义
│   └── RoleConstants.java             # 角色常量 (USER, ADMIN)
│
├── exception/                         # 异常处理
│   ├── BusinessException.java         # 业务异常类
│   ├── ErrorCode.java                 # 错误码定义
│   └── GlobalExceptionHandler.java    # 全局异常处理器
│
├── util/                              # 工具类
│   ├── FileCleanupHelper.java         # 文件清理助手
│   ├── FileTypeUtils.java             # 文件类型工具
│   ├── PageUtils.java                 # 分页工具
│   ├── PasswordValidator.java         # 密码验证器
│   ├── SensitiveDataUtils.java        # 敏感信息脱敏
│   └── XssUtils.java                  # XSS防护工具
│
├── R.java                             # 统一响应封装
└── SecurityUtils.java                 # 安全工具类
```

---

## 统一响应封装

### R.java - 统一响应类

所有API接口都使用 `R<T>` 封装响应，保证格式统一。

```java
/**
 * 统一响应封装类
 * @param <T> 数据类型
 */
@Data
public class R<T> {
    
    private int code;         // 状态码：200成功，其他失败
    private String msg;       // 消息
    private T data;           // 数据
    private String requestId; // 请求追踪ID
    
    // ==================== 成功响应 ====================
    
    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        r.setRequestId(generateRequestId());
        return r;
    }
    
    /**
     * 成功响应（带消息）
     */
    public static <T> R<T> ok(String msg) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setRequestId(generateRequestId());
        return r;
    }
    
    // ==================== 失败响应 ====================
    
    /**
     * 失败响应
     */
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        r.setRequestId(generateRequestId());
        return r;
    }
    
    /**
     * 失败响应（带错误码）
     */
    public static <T> R<T> fail(ErrorCode errorCode) {
        R<T> r = new R<>();
        r.setCode(errorCode.getCode());
        r.setMsg(errorCode.getMsg());
        r.setRequestId(generateRequestId());
        return r;
    }
}
```

### 使用示例

```java
@RestController
public class UserController {
    
    // 成功响应 - 返回数据
    @GetMapping("/user/{id}")
    public R<User> getUser(@PathVariable Integer id) {
        User user = userService.getById(id);
        return R.ok(user);
    }
    
    // 成功响应 - 返回消息
    @PostMapping("/user/add")
    public R<String> addUser(@RequestBody UserDTO dto) {
        userService.add(dto);
        return R.ok("添加成功");
    }
    
    // 失败响应
    @GetMapping("/user/{id}")
    public R<User> getUser(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }
        return R.ok(user);
    }
}
```

### 响应格式示例

```json
// 成功响应
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1,
        "username": "张三"
    },
    "requestId": "a1b2c3d4e5f6"
}

// 失败响应
{
    "code": 500,
    "msg": "用户不存在",
    "data": null,
    "requestId": "a1b2c3d4e5f6"
}
```

---

## 异常处理

### BusinessException - 业务异常

当业务逻辑出现错误时，抛出此异常。

```java
/**
 * 业务异常类
 * 用于业务逻辑中抛出的异常
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
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
```

### ErrorCode - 错误码定义

```java
/**
 * 错误码定义
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
    USER_BANNED(1004, "账号已被封禁"),
    
    // 业务相关
    PLANT_NOT_FOUND(2001, "植物不存在"),
    KNOWLEDGE_NOT_FOUND(2002, "知识不存在"),
    INHERITOR_NOT_FOUND(2003, "传承人不存在");
    
    private final int code;
    private final String msg;
    
    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public int getCode() { return code; }
    public String getMsg() { return msg; }
}
```

### GlobalExceptionHandler - 全局异常处理器

捕获所有异常，统一返回错误响应。

```java
/**
 * 全局异常处理器
 * 捕获所有异常，统一返回错误响应
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return R.fail(400, message);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail("系统繁忙，请稍后重试");
    }
}
```

### 使用示例

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public User login(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.findByUsername(dto.getUsername());
        
        // 2. 用户不存在 - 抛出业务异常
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 3. 密码错误 - 抛出业务异常
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        // 4. 账号被封禁
        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }
        
        return user;
    }
}
```

---

## 工具类

### SecurityUtils - 安全工具类

获取当前登录用户信息。

```java
/**
 * 安全工具类
 * 用于获取当前登录用户信息
 */
public class SecurityUtils {
    
    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ((UserDetails) auth.getPrincipal()).getId();
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }
    
    /**
     * 获取当前Token
     */
    public static String getCurrentToken() {
        // 从请求头获取Token
        HttpServletRequest request = 
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
    
    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
```

### XssUtils - XSS防护工具

防止XSS攻击，过滤危险字符。

```java
/**
 * XSS防护工具
 * 过滤危险字符，防止XSS攻击
 */
public class XssUtils {
    
    // 危险模式列表
    private static final Pattern[] DANGEROUS_PATTERNS = {
        // script标签
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
        // javascript协议
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
        // 事件处理器
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        // 更多模式...
    };
    
    /**
     * 检查字符串是否包含XSS攻击
     */
    public static boolean containsXss(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清理XSS攻击字符
     */
    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        
        String cleaned = value;
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            cleaned = pattern.matcher(cleaned).replaceAll("");
        }
        return cleaned;
    }
}
```

### PageUtils - 分页工具

```java
/**
 * 分页工具类
 */
public class PageUtils {
    
    /**
     * LIKE查询特殊字符转义
     * 防止SQL注入
     */
    public static String escapeLike(String keyword) {
        if (keyword == null) {
            return null;
        }
        return keyword
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
    
    /**
     * 构建分页对象
     */
    public static <T> Page<T> buildPage(int pageNum, int pageSize) {
        // 限制每页最大数量
        pageSize = Math.min(pageSize, 100);
        return new Page<>(pageNum, pageSize);
    }
}
```

### PasswordValidator - 密码验证器

```java
/**
 * 密码验证器
 */
public class PasswordValidator {
    
    // 密码正则：8-50位，必须包含字母和数字
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,50}$");
    
    /**
     * 验证密码格式
     */
    public static boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // 检查长度
        if (password.length() < 8 || password.length() > 50) {
            return false;
        }
        
        // 检查是否包含空格
        if (password.contains(" ")) {
            return false;
        }
        
        // 检查是否包含字母和数字
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 获取密码验证错误信息
     */
    public static String getErrorMessage() {
        return "密码长度8-50位，必须包含字母和数字，不能包含空格";
    }
}
```

---

## 常量定义

### RoleConstants - 角色常量

```java
/**
 * 角色常量
 */
public class RoleConstants {
    
    /**
     * 普通用户角色
     */
    public static final String USER = "USER";
    
    /**
     * 管理员角色
     */
    public static final String ADMIN = "ADMIN";
    
    /**
     * 角色名称映射
     */
    public static final Map<String, String> ROLE_NAMES = Map.of(
        USER, "普通用户",
        ADMIN, "管理员"
    );
}
```

---

## 最佳实践

### 1. 异常处理原则

```java
// 好的做法：使用业务异常
if (user == null) {
    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
}

// 不好的做法：直接抛出运行时异常
if (user == null) {
    throw new RuntimeException("用户不存在");
}
```

### 2. 响应封装原则

```java
// 好的做法：使用统一的响应封装
return R.ok(user);

// 不好的做法：直接返回对象
return user;
```

### 3. 工具类使用原则

```java
// 好的做法：使用工具类
String escaped = PageUtils.escapeLike(keyword);

// 不好的做法：自己写转义逻辑
String escaped = keyword.replace("%", "\\%");
```

---

**最后更新时间**：2026年4月3日
