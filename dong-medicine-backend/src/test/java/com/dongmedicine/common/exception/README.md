# 异常体系测试 (`common.exception`)

## 目录定位

本目录包含项目自定义异常体系的单元测试，验证 `com.dongmedicine.common.exception` 包下的 `BusinessException` 和 `ErrorCode` 两个核心类。异常体系是全系统错误处理的基础，被 Controller、Service、全局异常处理器广泛使用，其正确性决定了错误信息的准确传递和前端错误展示的正确性。

## 文件清单

### BusinessExceptionTest.java - 业务异常类测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testNotFound` | 验证 `BusinessException.notFound(msg)` 创建资源未找到异常，ErrorCode=RESOURCE_NOT_FOUND，code=2001 |
| `testUnauthorized` | 验证 `BusinessException.unauthorized(msg)` 创建未授权异常，ErrorCode=TOKEN_INVALID |
| `testBadRequest` | 验证 `BusinessException.badRequest(msg)` 创建参数错误异常，ErrorCode=PARAM_ERROR，code=3001 |
| `testConflict` | 验证 `BusinessException.conflict(msg)` 创建资源冲突异常，ErrorCode=DUPLICATE_OPERATION |
| `testForbidden` | 验证 `BusinessException.forbidden(msg)` 创建权限不足异常，ErrorCode=PERMISSION_DENIED |
| `testUserNotFound` | 验证 `BusinessException.userNotFound()` 使用默认消息 |
| `testUserAlreadyExists` | 验证 `BusinessException.userAlreadyExists()` 使用默认消息 |
| `testPasswordWrong` | 验证 `BusinessException.passwordWrong()` 使用默认消息 |
| `testConstructor_WithErrorCode` | 验证使用 ErrorCode 枚举构造异常，message 取自枚举默认消息 |
| `testConstructor_WithErrorCodeAndMessage` | 验证使用 ErrorCode + 自定义消息构造异常 |
| `testConstructor_WithCause` | 验证携带原始异常（cause）的构造方式 |
| `testIsRuntimeException` | 验证 BusinessException 是 RuntimeException 的子类 |

**核心测试思路**：覆盖所有静态工厂方法（notFound/unauthorized/badRequest/conflict/forbidden 等）和三种构造函数重载，验证 ErrorCode 映射、自定义消息覆盖、异常链传递和继承关系。

### ErrorCodeTest.java - 错误码枚举测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `knownCodes` | 验证 `getByCode()` 能根据已知错误码返回对应枚举（1001=USER_NOT_FOUND, 2001=RESOURCE_NOT_FOUND等） |
| `unknownCode` | 验证未知错误码返回 UNKNOWN_ERROR |
| `zeroCode` | 验证错误码0返回 UNKNOWN_ERROR |
| `successCode` | 验证错误码200返回 SUCCESS |
| `code` | 验证各枚举的 getCode() 返回正确数值 |
| `message` | 验证各枚举的 getMessage() 返回非空消息 |
| `userErrors` | 验证用户相关错误码在 1xxx 范围 |
| `resourceErrors` | 验证资源相关错误码在 2xxx 范围 |
| `paramErrors` | 验证参数相关错误码在 3xxx 范围 |
| `fileErrors` | 验证文件相关错误码在 4xxx 范围 |
| `systemErrors` | 验证系统错误码在 9xxx 范围 |
| `allCodesUnique` | 验证所有枚举的 code 值唯一，无重复 |

**核心测试思路**：验证错误码的编码规范（按业务分类分段编码）、反向查找（getByCode）、唯一性约束和消息完整性。

## 测试覆盖范围

- **BusinessException**：全部7个静态工厂方法 + 3个构造函数重载 + 继承关系
- **ErrorCode**：全部枚举值的 code/message、反向查找、分类范围、唯一性

## 错误码分类体系

| 范围 | 分类 | 示例 |
|------|------|------|
| 1xxx | 用户相关 | USER_NOT_FOUND(1001)、PASSWORD_WRONG(1003)、TOKEN_EXPIRED |
| 2xxx | 资源相关 | RESOURCE_NOT_FOUND(2001)、PLANT_NOT_FOUND |
| 3xxx | 参数相关 | PARAM_ERROR(3001) |
| 4xxx | 文件相关 | FILE_UPLOAD_ERROR(4001) |
| 9xxx | 系统相关 | SYSTEM_ERROR(9001) |

## 依赖关系

- `BusinessExceptionTest` 依赖 `ErrorCode` 枚举
- `ErrorCodeTest` 是独立测试
- 两者被 `controller/`、`service/impl/`、`integration/`、`regression/` 测试间接使用（通过 assertThrows 验证异常抛出）
