# 状态管理目录说明

## 文件夹结构

本目录包含项目的Pinia状态管理store。

```
stores/
├── user.js         # 用户状态管理
└── README.md        # 说明文档
```

## 详细说明

### user.js - 用户状态管理

**功能**：管理用户认证状态、登录登出、Token验证等。

**状态属性**：
| 属性 | 类型 | 说明 |
|------|------|------|
| token | Ref<String> | JWT令牌 |
| userId | Ref<String> | 用户ID |
| username | Ref<String> | 用户名 |
| role | Ref<String> | 用户角色 |
| userInfo | Ref<Object> | 用户详细信息 |

**计算属性**：
| 属性 | 类型 | 说明 |
|------|------|------|
| isLoggedIn | Computed<Boolean> | 是否已登录（检查Token有效性） |
| userName | Computed<String> | 用户名 |
| isAdmin | Computed<Boolean> | 是否为管理员 |

**Actions**：
| 方法 | 参数 | 返回值 | 说明 |
|------|------|-------|------|
| initialize | - | void | 初始化状态 |
| checkTokenExpiry | - | void | 检查Token是否过期 |
| setAuth | data | void | 设置认证信息 |
| clearAuth | - | void | 清除认证信息 |
| fetchUserInfo | - | Promise<Object> | 获取用户信息 |
| validateToken | - | Promise<Boolean> | 验证Token有效性 |
| login | loginData | Promise<Object> | 用户登录 |
| logout | - | Promise<void> | 用户登出 |
| changePassword | data | Promise<Object> | 修改密码 |
| initializeFromStorage | - | void | 从存储初始化 |
| getTokenRemainingTime | - | Number | 获取Token剩余时间 |

**Token过期检查**：
- 使用JWT payload中的exp字段判断过期
- 提前5分钟缓冲时间
- 过期自动清除认证信息

**存储方式**：
- 使用sessionStorage存储Token等认证信息
- 提供安全的存储方法（处理存储不可用情况）

**使用示例**：
```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 检查登录状态
if (userStore.isLoggedIn) {
  console.log('当前用户:', userStore.userName)
}

// 登录
const result = await userStore.login({
  username: 'admin',
  password: 'password'
})

if (result.success) {
  console.log('登录成功')
}

// 登出
await userStore.logout()
```

**Token验证流程**：
1. 检查本地Token是否存在
2. 解析JWT payload检查是否过期
3. 调用服务端验证接口
4. 验证失败时清除认证信息

---

**最后更新时间**：2026年3月25日
