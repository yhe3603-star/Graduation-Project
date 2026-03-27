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

---

**最后更新时间**：2026年3月27日
