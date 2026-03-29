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

## 已知限制

| 限制 | 影响 |
|------|------|
| 仅sessionStorage持久化 | 关闭浏览器后状态丢失 |
| 不支持状态快照 | 无法实现时间旅行调试 |
| 不支持模块动态注册 | 所有Store需预先定义 |
| Token验证缓存60秒 | 登出后可能短暂有效 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **状态持久化**
   - 支持localStorage持久化
   - 实现选择性持久化
   - 添加状态加密

2. **调试增强**
   - 集成Pinia DevTools
   - 添加状态日志

### 中期改进 (1-2月)

1. **功能增强**
   - 实现状态快照
   - 添加撤销/重做功能
   - 支持模块热更新

2. **性能优化**
   - 实现状态懒加载
   - 优化大型状态对象

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Pinia | 2.3+ | 状态管理 |
| Vue | 3.4+ | 响应式系统 |

---

## 常见问题

### 1. 如何创建新的Store？

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useDataStore = defineStore('data', () => {
  // 状态
  const items = ref([])
  const loading = ref(false)
  
  // 计算属性
  const itemCount = computed(() => items.value.length)
  
  // 方法
  async function fetchItems() {
    loading.value = true
    try {
      const res = await api.getItems()
      items.value = res.data
    } finally {
      loading.value = false
    }
  }
  
  function addItem(item) {
    items.value.push(item)
  }
  
  return {
    items,
    loading,
    itemCount,
    fetchItems,
    addItem
  }
})
```

### 2. 如何在组件外使用Store？

```javascript
// 在普通JS文件中使用
import { useUserStore } from '@/stores/user'

// 需要在Pinia实例创建后使用
export function checkAuth() {
  const userStore = useUserStore()
  return userStore.isLoggedIn
}
```

### 3. 如何实现状态持久化？

```javascript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: null,
    username: null
  }),
  
  persist: {
    storage: sessionStorage,
    paths: ['token', 'username']  // 只持久化这些字段
  }
})
```

### 4. 如何重置Store状态？

```javascript
const userStore = useUserStore()

// 重置为初始状态
userStore.$reset()

// 或批量更新状态
userStore.$patch({
  token: null,
  username: null
})
```

---

**最后更新时间**：2026年3月30日
