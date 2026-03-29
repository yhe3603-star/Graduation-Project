# 通用模块目录说明

## 文件夹结构

本目录包含项目中使用的通用组件、工具类和异常处理等。

```
common/
├── constant/                  # 常量定义
│   └── RoleConstants.java     # 角色常量
├── exception/                 # 异常处理
│   ├── BusinessException.java # 业务异常
│   ├── ErrorCode.java         # 错误码枚举
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── util/                      # 工具类
│   ├── FileCleanupHelper.java # 文件清理助手
│   ├── FileTypeUtils.java     # 文件类型工具
│   ├── PageUtils.java         # 分页工具
│   ├── PasswordValidator.java # 密码验证器
│   ├── SensitiveDataUtils.java# 敏感数据处理工具
│   └── XssUtils.java          # XSS防护工具
├── R.java                     # 统一响应封装
├── SecurityUtils.java         # 安全工具类
└── README.md                  # 说明文档
```

## 详细说明

### 1. R.java - 统一响应封装

**功能**：封装所有API接口的统一响应格式，支持请求追踪。

**类结构**：
```java
public class R<T> {
    private int code;          // 状态码
    private String msg;        // 提示信息
    private T data;            // 响应数据
    private String requestId;  // 请求追踪ID（用于日志追踪）
}
```

**静态方法**：
| 方法 | 说明 |
|------|------|
| `R.ok()` | 返回成功响应（无数据） |
| `R.ok(T data)` | 返回成功响应（带数据） |
| `R.ok(String msg, T data)` | 返回成功响应（带消息和数据） |
| `R.error(String msg)` | 返回失败响应（默认500） |
| `R.error(int code, String msg)` | 返回失败响应（指定状态码） |
| `R.error(ErrorCode errorCode)` | 返回失败响应（带错误码枚举） |
| `R.error(ErrorCode errorCode, String message)` | 返回失败响应（自定义消息） |
| `R.unauthorized(String msg)` | 返回未授权响应（401） |
| `R.forbidden(String msg)` | 返回禁止访问响应（403） |
| `R.notFound(String msg)` | 返回资源不存在响应（404） |
| `R.badRequest(String msg)` | 返回请求错误响应（400） |

**实例方法**：
| 方法 | 说明 |
|------|------|
| `isSuccess()` | 判断响应是否成功 |

**请求追踪**：
- 每个响应自动包含`requestId`字段
- 通过`RequestIdFilter`和MDC实现请求追踪
- 便于日志排查和问题定位

**使用示例**：
```java
@GetMapping("/user/{id}")
public R<User> getUser(@PathVariable Long id) {
    User user = userService.getById(id);
    return R.ok(user);
}
```

### 2. SecurityUtils.java - 安全工具类

**功能**：提供安全相关的工具方法。

**主要方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getCurrentUserId()` | Long | 获取当前登录用户ID |
| `getCurrentUsername()` | String | 获取当前登录用户名 |
| `getCurrentUser()` | User | 获取当前登录用户对象 |
| `isAuthenticated()` | boolean | 判断用户是否已登录 |
| `isAdmin()` | boolean | 判断当前用户是否为管理员 |
| `encodePassword(String rawPassword)` | String | 加密密码 |
| `matchesPassword(String rawPassword, String encodedPassword)` | boolean | 验证密码 |

### 3. constant/RoleConstants.java - 角色常量

**功能**：定义系统中的角色常量。

**常量定义**：
```java
public class RoleConstants {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";    // 管理员角色
    public static final String ROLE_USER = "ROLE_USER";      // 普通用户角色
    public static final String ADMIN = "admin";              // 管理员标识
    public static final String USER = "user";                // 用户标识
}
```

### 4. exception/BusinessException.java - 业务异常

**功能**：自定义业务异常类，用于业务逻辑中的异常抛出。

**类结构**：
```java
public class BusinessException extends RuntimeException {
    private Integer code;      // 错误码
    private String message;    // 错误信息
}
```

**构造方法**：
| 构造方法 | 说明 |
|---------|------|
| `BusinessException(String message)` | 创建业务异常（默认错误码500） |
| `BusinessException(Integer code, String message)` | 创建业务异常（指定错误码） |
| `BusinessException(ErrorCode errorCode)` | 创建业务异常（使用错误码枚举） |

**使用示例**：
```java
public void deleteUser(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new BusinessException("用户不存在");
    }
    userMapper.deleteById(id);
}
```

### 5. exception/ErrorCode.java - 错误码枚举

**功能**：定义系统中的错误码枚举。

**枚举值**：
| 错误码 | 枚举名 | 说明 |
|-------|--------|------|
| 200 | SUCCESS | 操作成功 |
| 400 | BAD_REQUEST | 请求参数错误 |
| 401 | UNAUTHORIZED | 未授权 |
| 403 | FORBIDDEN | 禁止访问 |
| 404 | NOT_FOUND | 资源不存在 |
| 500 | INTERNAL_ERROR | 服务器内部错误 |
| 1001 | USER_NOT_FOUND | 用户不存在 |
| 1002 | USER_ALREADY_EXISTS | 用户已存在 |
| 1003 | PASSWORD_ERROR | 密码错误 |
| 1004 | ACCOUNT_DISABLED | 账号已被禁用 |
| 2001 | RESOURCE_NOT_FOUND | 资源不存在 |
| 3001 | OPERATION_FAILED | 操作失败 |

### 6. exception/GlobalExceptionHandler.java - 全局异常处理器

**功能**：统一处理系统中抛出的各种异常。

**处理的异常类型**：
| 异常类型 | 处理方式 |
|---------|---------|
| `BusinessException` | 返回业务异常信息 |
| `MethodArgumentNotValidException` | 返回参数验证错误信息 |
| `BindException` | 返回绑定错误信息 |
| `HttpRequestMethodNotSupportedException` | 返回请求方法不支持错误 |
| `HttpMediaTypeNotSupportedException` | 返回媒体类型不支持错误 |
| `MissingServletRequestParameterException` | 返回缺少请求参数错误 |
| `AccessDeniedException` | 返回权限不足错误 |
| `AuthenticationException` | 返回认证失败错误 |
| `Exception` | 返回未知错误信息 |

### 7. util/FileCleanupHelper.java - 文件清理助手

**功能**：帮助清理临时文件和无效文件。

**主要方法**：
| 方法 | 说明 |
|------|------|
| `scheduleCleanup(File file, long delayMs)` | 延迟清理指定文件 |
| `cleanupNow(File file)` | 立即清理指定文件 |
| `cleanupDirectory(File directory)` | 清理整个目录 |

### 8. util/FileTypeUtils.java - 文件类型工具

**功能**：判断和处理文件类型。

**主要方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getFileType(String fileName)` | String | 获取文件类型（image/video/document等） |
| `getMimeType(String fileName)` | String | 获取文件的MIME类型 |
| `isImage(String fileName)` | boolean | 判断是否为图片文件 |
| `isVideo(String fileName)` | boolean | 判断是否为视频文件 |
| `isDocument(String fileName)` | boolean | 判断是否为文档文件 |
| `getAllowedExtensions()` | String[] | 获取允许上传的文件扩展名 |

**支持的文件类型**：
- 图片：jpg, jpeg, png, gif, bmp, webp, svg
- 视频：mp4, avi, mov, wmv, flv, mkv
- 文档：docx, doc, pdf, pptx, ppt, xlsx, xls, txt

### 9. util/PageUtils.java - 分页工具

**功能**：处理分页相关的工具方法，包含SQL注入防护。

**主要方法**：
| 方法 | 说明 |
|------|------|
| `getPage(Integer page, Integer size)` | 创建分页对象（带边界检查：page最小1，size范围1-100） |
| `toMap(Page<?> pageResult)` | 将MyBatis-Plus分页对象转换为Map格式 |
| `escapeLike(String keyword)` | 转义LIKE查询特殊字符，防止SQL注入 |

**escapeLike方法**：
转义LIKE查询中的特殊字符（`\`、`%`、`_`），防止SQL注入攻击。

```java
// 使用示例
String safeKeyword = PageUtils.escapeLike(keyword);
wrapper.like("name", safeKeyword);
```

**分页响应格式**：
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "size": 20
}
```

### 10. util/PasswordValidator.java - 密码验证器

**功能**：验证密码强度和有效性。

**密码规则**：
- 最小长度：8位
- 最大长度：50位
- 必须包含：字母和数字
- 不能包含：空格
- 可选包含：特殊字符（!@#$%^&*等）

**密码强度等级**：
| 等级 | 分数范围 | 说明 |
|------|---------|------|
| 弱 | 0-1 | 仅满足基本要求 |
| 一般 | 2 | 满足基本要求+长度 |
| 中等 | 3 | 包含大小写字母+数字 |
| 强 | 4 | 包含大小写字母+数字+特殊字符 |
| 非常强 | 5 | 长度≥16+复杂组合 |

**主要方法**：
| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `validate(String password)` | ValidationResult | 验证密码并返回详细结果 |
| `isValid(String password)` | boolean | 判断密码是否有效 |

**ValidationResult 属性**：
- `valid`：是否有效
- `message`：验证消息
- `strength`：密码强度（1-5）
- `strengthLabel`：强度标签（弱/一般/中等/强/非常强）

### 11. util/SensitiveDataUtils.java - 敏感数据处理工具

**功能**：对敏感数据进行脱敏处理。

**敏感字段列表**：
- 密码相关：password, passwd, pwd, secret
- 令牌相关：token, accessToken, refreshToken, apiKey, apiSecret
- 身份相关：idCard, idCardNumber, idcard
- 联系方式：phone, mobile, telephone, email, mail
- 金融相关：bankCard, bankAccount
- 其他：address, realName, jwt, authorization

**主要方法**：
| 方法 | 说明 |
|------|------|
| `mask(String fieldName, String value)` | 根据字段名脱敏 |
| `isSensitiveField(String fieldName)` | 判断是否为敏感字段 |
| `maskValue(String value)` | 对值进行脱敏处理 |
| `autoMask(String text)` | 自动识别并脱敏文本中的敏感信息 |
| `maskJson(String json)` | 对JSON字符串中的敏感字段脱敏 |

**脱敏规则**：
- 邮箱：`te***@example.com`
- 手机号：`138****1234`
- 身份证：`110101********1234`
- 银行卡：`6222****1234`
- 其他：保留首尾字符，中间用*替代

### 12. util/XssUtils.java - XSS防护工具

**功能**：防止XSS（跨站脚本）攻击和SQL注入。

**主要方法**：
| 方法 | 说明 |
|------|------|
| `sanitize(String input)` | 对输入进行HTML转义 |
| `sanitizeHtml(String input)` | 清理HTML中的危险标签 |
| `containsXss(String input)` | 检测是否包含XSS攻击代码 |
| `containsSqlInjection(String input)` | 检测是否包含SQL注入 |
| `stripHtmlTags(String input)` | 移除所有HTML标签 |
| `escapeJavaScript(String input)` | 转义JavaScript特殊字符 |
| `sanitizeUrl(String url)` | 清理URL中的危险内容 |
| `sanitizeFileName(String fileName)` | 清理文件名中的危险字符 |
| `sanitizeForLog(String input)` | 清理日志中的特殊字符（限制1000字符） |
| `isSafeInput(String input, int maxLength)` | 判断输入是否安全 |

**检测的危险模式（30+）**：
- `<script>`标签（包括自闭合）
- `javascript:`、`vbscript:`协议
- 事件处理器（onclick, onerror, onload等）
- `eval()`、`expression()`函数
- `<iframe>`, `<object>`, `<embed>`标签
- `<link>`, `<meta>`, `<base>`标签
- `<form>`, `<input>`, `<button>`表单元素
- `<style>`样式标签
- `data:`协议
- `srcdoc`属性
- `xlink:href`属性
- `<svg>`, `<math>`数学标签
- `<audio>`, `<video>`, `<source>`媒体标签
- HTML实体编码（`&#x?...`）
- SQL注入关键字（select, insert, update, delete, drop等）

## 文件统计

| 目录/文件 | 文件数 | 主要用途 |
|----------|-------|---------|
| constant/ | 1 | 常量定义 |
| exception/ | 3 | 异常处理 |
| util/ | 6 | 工具类 |
| R.java | 1 | 统一响应封装 |
| SecurityUtils.java | 1 | 安全工具 |
| **总计** | **12** | - |

## 开发规范

1. **命名规范**：类名使用大驼峰命名法，方法名使用小驼峰命名法
2. **工具类规范**：工具类应为final类，私有构造方法，静态方法
3. **异常处理**：使用BusinessException抛出业务异常，由全局异常处理器统一处理
4. **安全规范**：所有用户输入都应经过XssUtils处理
5. **敏感数据**：日志中不应输出敏感数据，应使用SensitiveDataUtils脱敏

### 工具类模板

```java
public final class MyUtils {
    
    private MyUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    public static String doSomething(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.trim();
    }
}
```

### 异常处理最佳实践

```java
// 推荐：使用ErrorCode枚举
if (user == null) {
    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
}

// 推荐：自定义错误消息
if (passwordInvalid) {
    throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码必须包含字母和数字");
}

// 不推荐：直接抛出RuntimeException
throw new RuntimeException("用户不存在");  // 无法被正确处理
```

### 响应封装最佳实践

```java
// 成功响应
@GetMapping("/user/{id}")
public R<User> getUser(@PathVariable Long id) {
    User user = userService.getById(id);
    if (user == null) {
        return R.notFound("用户不存在");
    }
    return R.ok(user);
}

// 分页响应
@GetMapping("/users")
public R<Map<String, Object>> getUsers(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "20") Integer size) {
    Page<User> pageResult = userService.page(PageUtils.getPage(page, size));
    return R.ok(PageUtils.toMap(pageResult));
}

// 操作响应
@PostMapping("/user")
public R<Void> createUser(@RequestBody @Valid UserDTO dto) {
    userService.save(dto);
    return R.ok("创建成功");
}
```

---

## 已知限制

| 工具类 | 限制 | 影响 |
|--------|------|------|
| R.java | requestId依赖MDC | 未配置RequestIdFilter时为空 |
| SecurityUtils | 依赖Spring Security | 非Security环境无法使用 |
| PageUtils | 最大页码100 | 超大分页需特殊处理 |
| PasswordValidator | 不支持自定义规则 | 特殊密码策略需扩展 |
| SensitiveDataUtils | 仅支持中文场景 | 国际化需扩展敏感词库 |
| XssUtils | 可能误判合法输入 | 富文本场景需特殊处理 |
| FileTypeUtils | 基于扩展名判断 | 伪造扩展名可能绕过 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **FileTypeUtils**
   - 添加文件头魔数检测
   - 支持更多文件类型

2. **XssUtils**
   - 添加白名单配置
   - 支持富文本策略

3. **ErrorCode**
   - 添加更多业务错误码
   - 支持国际化消息

### 中期改进 (1-2月)

1. **性能优化**
   - 添加缓存机制
   - 优化正则表达式

2. **功能增强**
   - 添加IP地址工具类
   - 添加日期时间工具类
   - 添加加密解密工具类

3. **测试覆盖**
   - 编写单元测试
   - 添加边界测试

### 长期规划 (3-6月)

1. **模块化**
   - 抽取为独立模块
   - 支持独立版本管理

2. **监控集成**
   - 添加性能监控
   - 异常统计分析

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2+ | 框架基础 |
| Spring Security | 6.2+ | 安全工具类 |
| MyBatis-Plus | 3.5+ | 分页工具 |
| SLF4J | 2.0+ | 日志脱敏 |
| Jackson | 2.15+ | JSON处理 |

---

## 常见问题

### 1. 如何添加自定义错误码？

```java
// 在ErrorCode.java中添加
public enum ErrorCode {
    // 现有错误码...
    
    // 自定义错误码 (4000-4999为业务自定义区间)
    CUSTOM_ERROR(4001, "自定义错误"),
    PLANT_NOT_FOUND(4002, "药用植物不存在"),
    THERAPY_NOT_FOUND(4003, "疗法不存在");
    
    // ...
}
```

### 2. 如何扩展敏感字段列表？

```java
// 在SensitiveDataUtils中添加
private static final Set<String> SENSITIVE_FIELDS = Set.of(
    // 现有字段...
    "customSensitiveField",  // 添加自定义字段
    "medicalRecord"
);
```

### 3. 如何自定义密码验证规则？

```java
// 创建自定义验证器
public class CustomPasswordValidator {
    
    public static ValidationResult validate(String password) {
        // 自定义验证逻辑
        if (password == null || password.length() < 10) {
            return new ValidationResult(false, "密码长度至少10位", 0);
        }
        // 更多规则...
        return new ValidationResult(true, "密码有效", 4);
    }
}
```

### 4. 如何处理全局异常中的自定义异常？

```java
// 在GlobalExceptionHandler.java中添加
@ExceptionHandler(MyCustomException.class)
public R<Void> handleMyCustomException(MyCustomException e) {
    log.error("自定义异常: {}", e.getMessage());
    return R.error(e.getCode(), e.getMessage());
}
```

### 5. 如何在日志中安全输出用户数据？

```java
import static com.dongmedicine.common.util.SensitiveDataUtils.mask;

// 安全日志输出
log.info("用户登录: username={}, phone={}", 
    mask("username", username),
    mask("phone", phone)
);
// 输出: 用户登录: username=张**, phone=138****1234
```

---

**最后更新时间**：2026年3月30日
