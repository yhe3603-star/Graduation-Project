# 公共组件测试 (`common`)

## 目录定位

本目录包含项目公共基础组件的单元测试，验证 `com.dongmedicine.common` 包下的核心基础设施代码。这些公共组件是整个后端系统的基石，被 Controller、Service、Integration 等各层广泛依赖，其正确性直接关系到全系统的稳定性。

## 文件清单

### RTest.java - 统一响应封装测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `ok_success` | 验证 `R.ok(data)` 返回成功响应，isSuccess=true，data正确，msg="success" |
| `ok_withList` | 验证 `R.ok(listData)` 正确封装列表数据 |
| `ok_noData` | 验证 `R.ok()` 无数据响应，data=null |
| `ok_withMsgAndData` | 验证 `R.ok(msg, data)` 自定义消息和数据 |
| `error_withMessage` | 验证 `R.error(msg)` 返回失败响应，isSuccess=false，data=null |
| `error_withCodeAndMessage` | 验证 `R.error(code, msg)` 带错误码的失败响应 |
| `unauthorized_test` | 验证 `R.unauthorized(msg)` 返回401状态码 |
| `forbidden_test` | 验证 `R.forbidden(msg)` 返回403状态码 |
| `notFound_test` | 验证 `R.notFound(msg)` 返回404状态码 |
| `badRequest_test` | 验证 `R.badRequest(msg)` 返回400状态码 |
| `isSuccess_test` | 验证成功/失败响应的 isSuccess 判断 |
| `error_withData` | 验证错误响应不携带数据，成功响应可携带数据 |

**核心测试思路**：覆盖 `R<T>` 的全部静态工厂方法（ok/error/unauthorized/forbidden/notFound/badRequest），验证 code、msg、data 三个字段的正确性，确保统一响应格式在整个项目中一致。

## 测试覆盖范围

- **R 统一响应**：所有工厂方法的返回值、状态码、消息、数据字段
- **成功/失败状态判断**：`isSuccess()` 方法在各种响应类型下的行为

## 依赖关系

- `RTest` 无外部依赖，纯单元测试
- `R<T>` 是所有 Controller 返回值的统一封装，被 `controller/` 和 `integration/` 测试间接验证
- 本目录下的 `exception/`、`util/`、`constant/` 子包测试也依赖 `R` 和 `BusinessException`
