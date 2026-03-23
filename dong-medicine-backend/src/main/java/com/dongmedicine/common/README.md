# 通用组件目录说明

## 文件夹结构

本目录包含项目中使用的通用组件，包括常量定义、异常处理、工具类等。

```
common/
├── constant/       # 常量定义
├── exception/      # 异常处理
├── util/           # 工具类
├── R.java          # 统一响应类
└── SecurityUtils.java  # 安全工具类
```

## 详细说明

### 1. constant/ 目录

**用途**：定义项目中使用的常量。

**文件列表**：

- **RoleConstants.java**
  - 功能：定义角色相关常量
  - 主要常量：
    - `ROLE_ADMIN`：管理员角色
    - `ROLE_USER`：普通用户角色
    - `ROLE_GUEST`：访客角色

### 2. exception/ 目录

**用途**：处理项目中的异常。

**文件列表**：

- **BusinessException.java**
  - 功能：业务异常类，用于处理业务逻辑异常
  - 主要属性：
    - `code`：错误码
    - `message`：错误信息

- **ErrorCode.java**
  - 功能：错误码定义
  - 主要错误码：
    - `SUCCESS`：成功
    - `BAD_REQUEST`：请求参数错误
    - `UNAUTHORIZED`：未授权
    - `FORBIDDEN`：禁止访问
    - `NOT_FOUND`：资源不存在
    - `INTERNAL_SERVER_ERROR`：服务器内部错误

- **GlobalExceptionHandler.java**
  - 功能：全局异常处理器
  - 处理的异常：
    - `BusinessException`：业务异常
    - `MethodArgumentNotValidException`：参数验证异常
    - `AuthenticationException`：认证异常
    - `Exception`：其他异常

### 3. util/ 目录

**用途**：提供各种工具类。

**文件列表**：

- **FileCleanupHelper.java**
  - 功能：文件清理工具
  - 主要方法：
    - `cleanupTempFiles`：清理临时文件
    - `cleanupOldFiles`：清理旧文件

- **FileTypeUtils.java**
  - 功能：文件类型工具
  - 主要方法：
    - `getContentType`：获取文件内容类型
    - `isImageFile`：判断是否为图片文件
    - `isVideoFile`：判断是否为视频文件
    - `isDocumentFile`：判断是否为文档文件

- **PageUtils.java**
  - 功能：分页工具
  - 主要方法：
    - `getPage`：获取分页对象
    - `getLimit`：获取分页限制
    - `getOffset`：获取分页偏移量

- **PasswordValidator.java**
  - 功能：密码验证工具
  - 主要方法：
    - `validate`：验证密码强度
    - `isStrongPassword`：判断是否为强密码

- **SensitiveDataUtils.java**
  - 功能：敏感数据处理工具
  - 主要方法：
    - `maskPhone`：手机号脱敏
    - `maskEmail`：邮箱脱敏
    - `maskIdCard`：身份证号脱敏

- **XssUtils.java**
  - 功能：XSS防护工具
  - 主要方法：
    - `clean`：清理XSS攻击代码
    - `validate`：验证输入内容

### 4. R.java

**功能**：统一响应类，用于封装API响应数据。

**主要方法**：
- `ok()`：返回成功响应
- `ok(T data)`：返回带数据的成功响应
- `error()`：返回错误响应
- `error(int code, String message)`：返回带错误码和消息的错误响应
- `error(ErrorCode errorCode)`：返回带错误码的错误响应

**使用示例**：

```java
// 成功响应
return R.ok(data);

// 错误响应
return R.error(ErrorCode.BAD_REQUEST, "请求参数错误");
```

### 5. SecurityUtils.java

**功能**：安全工具类，用于处理安全相关的操作。

**主要方法**：
- `getCurrentUser`：获取当前用户
- `getCurrentUserId`：获取当前用户ID
- `getCurrentUserRole`：获取当前用户角色
- `hasRole`：检查用户是否具有指定角色
- `isAdmin`：检查用户是否为管理员

## 使用指南

### 异常处理

```java
// 抛出业务异常
if (user == null) {
    throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
}

// 全局异常处理器会自动捕获并返回统一格式的错误响应
```

### 统一响应

```java
@GetMapping("/plants")
public R<List<Plant>> getPlants() {
    List<Plant> plants = plantService.list();
    return R.ok(plants);
}

@PostMapping("/login")
public R<LoginDTO> login(@RequestBody LoginRequest request) {
    LoginDTO loginDTO = userService.login(request);
    return R.ok(loginDTO);
}
```

### 安全工具

```java
// 检查用户权限
if (!SecurityUtils.hasRole(RoleConstants.ROLE_ADMIN)) {
    return R.error(ErrorCode.FORBIDDEN, "权限不足");
}

// 获取当前用户
User currentUser = SecurityUtils.getCurrentUser();
```

## 开发规范

1. **命名规范**：类名使用大驼峰命名法，方法名和变量名使用小驼峰命名法
2. **异常处理**：业务逻辑异常应该使用`BusinessException`
3. **响应格式**：所有API响应应该使用`R`类封装
4. **安全处理**：敏感数据应该使用`SensitiveDataUtils`处理
5. **工具类**：工具类应该是静态的，提供静态方法

## 注意事项

- 常量定义应该放在`constant`目录下
- 异常处理应该使用统一的异常类和错误码
- 工具类应该遵循单一职责原则
- 安全相关的操作应该使用`SecurityUtils`
- 响应格式应该统一使用`R`类

---

**最后更新时间**：2026年3月23日