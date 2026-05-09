# 常量类测试 (`common.constant`)

## 目录定位

本目录包含项目常量定义的单元测试，验证 `com.dongmedicine.common.constant` 包下的角色常量类。常量类是系统权限控制和角色判断的基础，其正确性直接影响用户权限分配和访问控制的准确性。

## 文件清单

### RoleConstantsTest.java - 角色常量类测试

| 测试方法 | 测试逻辑 |
|---------|---------|
| `testRoleConstants` | 验证常量值：`ROLE_USER="user"`、`ROLE_ADMIN="admin"` |
| `testIsValidUser` | 验证 `isValid("user")` 返回 true |
| `testIsValidAdmin` | 验证 `isValid("admin")` 返回 true |
| `testIsValidInvalid` | 验证无效角色（superadmin/guest/空字符串/null）返回 false |
| `testNormalizeValid` | 验证有效角色规范化：小写原样返回、大写转小写、带空格去除空格 |
| `testNormalizeInvalid` | 验证无效角色规范化后返回默认值 "user" |

**核心测试思路**：

1. **常量值验证**：确保 `ROLE_USER` 和 `ROLE_ADMIN` 的值与数据库和前端约定一致
2. **有效性判断**：`isValid()` 方法只接受 "user" 和 "admin" 两个合法值，拒绝其他任何输入
3. **规范化处理**：`normalize()` 方法实现大小写不敏感和空格容错，无效输入回退到默认角色 "user"，保证系统安全性

## 测试覆盖范围

- 角色常量值正确性
- 角色有效性判断（合法/非法/边界值）
- 角色规范化（大小写、空格、默认值回退）

## 依赖关系

- `RoleConstantsTest` 无外部依赖，纯单元测试
- `RoleConstants` 被 `controller/admin/AdminUserControllerTest` 间接使用（更新用户角色时）
- `RoleConstants.normalize()` 的默认值回退机制是系统安全防线，确保异常输入不会获得管理员权限
