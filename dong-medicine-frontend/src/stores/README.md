# Stores 状态管理目录

本目录包含项目的 Pinia 状态管理模块。

## 目录结构

```
stores/
├── index.js              # Pinia 实例配置
├── user.js               # 用户状态管理
└── README.md             # 说明文档
```

## 模块说明

### index.js - Pinia 实例配置

**用途**: 创建并导出 Pinia 实例

```javascript
import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia
```

---

### user.js - 用户状态管理

**状态**:

| 状态 | 类型 | 说明 |
|------|------|------|
| `token` | string | 用户认证令牌 |
| `userId` | string | 用户 ID |
| `username` | string | 用户名 |
| `role` | string | 用户角色 (USER/ADMIN) |
| `userInfo` | object | 用户详细信息 |

**计算属性**:

| 属性 | 说明 |
|------|------|
| `isLoggedIn` | 登录状态（检查token是否存在且未过期） |
| `isAdmin` | 是否管理员（检查角色是否为ADMIN） |
| `userName` | 用户名（计算属性） |

**方法**:

| 方法 | 参数 | 说明 |
|------|------|------|
| `setAuth(data)` | data: object | 设置认证信息 |
| `clearAuth()` | - | 清除认证信息 |
| `login(credentials)` | credentials: object | 用户登录 |
| `logout()` | - | 用户登出 |
| `validateToken()` | - | 验证 Token 有效性 |
| `changePassword(data)` | data: object | 修改密码 |
| `fetchUserInfo()` | - | 获取用户信息 |
| `initializeFromStorage()` | - | 从 sessionStorage 初始化状态 |

**Token过期判断**:

```javascript
// 统一的Token过期判断函数
function isTokenExpired(token, options = {}) {
  const { useBuffer = true, bufferMs = 5 * 60 * 1000 } = options
  const expiryTime = getTokenExpiryTime(token)
  if (!expiryTime) return true
  
  const now = Date.now()
  return useBuffer ? now >= expiryTime - bufferMs : now >= expiryTime
}

// 获取Token过期时间
function getTokenExpiryTime(token) {
  if (!token) return null
  const payload = decodeJwtPayload(token)
  if (!payload || !payload.exp) return null
  return payload.exp * 1000
}

// 解码JWT Payload
function decodeJwtPayload(token) {
  try {
    const base64Url = token.split('.')[1]
    if (!base64Url) return null
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch {
    return null
  }
}
```

**使用示例**:

```javascript
import { useUserStore } from '@/stores/user'

// 在组件中使用
const userStore = useUserStore()

// 检查登录状态
if (userStore.isLoggedIn) {
  console.log('当前用户:', userStore.username)
}

// 登录
await userStore.login({ username, password })

// 登出
userStore.logout()

// 检查管理员权限
if (userStore.isAdmin) {
  // 显示管理功能
}
```

---

## 开发规范

1. **命名规范**: Store 文件使用小驼峰命名法
2. **导出方式**: 使用 `defineStore` 定义，默认导出
3. **状态持久化**: 敏感状态存储在 sessionStorage
4. **类型安全**: 使用 TypeScript 或 JSDoc 注释

---

**最后更新时间**: 2026年3月27日
