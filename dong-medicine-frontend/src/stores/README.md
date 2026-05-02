# 状态管理目录 (stores/)

使用 **Pinia** 进行全局状态管理。Pinia 是 Vue 3 官方推荐的状态管理库，采用 Composition API 风格的 `defineStore` 语法。

## 文件清单

| 文件 | 用途 |
|------|------|
| `index.js` | 创建 Pinia 实例并导出 |
| `user.js` | 用户认证状态 store（token、用户名、角色、登录/退出等） |

---

## Pinia 实例 (index.js)

```js
import { createPinia } from 'pinia'
export const pinia = createPinia()
```

在 `main.js` 中通过 `app.use(pinia)` 注册。

---

## useUserStore -- 用户认证 Store

**文件：** `user.js`

**Store ID：** `'user'`

**使用方式：**

```js
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
```

### 核心状态 (State)

| 状态 | 类型 | 说明 |
|------|------|------|
| `token` | Ref<String> | JWT 认证令牌（初始化时从 localStorage 恢复） |
| `userId` | Ref<String> | 用户 ID |
| `username` | Ref<String> | 用户名 |
| `role` | Ref<String> | 用户角色（'admin' 或 'user'） |
| `userInfo` | Ref<Object\|null> | 完整用户信息对象（通过 fetchUserInfo 获取） |

### 计算属性 (Getters)

| 属性 | 类型 | 说明 |
|------|------|------|
| `isLoggedIn` | ComputedRef<Boolean> | 是否已登录。判断逻辑：1) token 存在；2) JWT payload 中的 exp 未过期 |
| `userName` | ComputedRef<String> | 用户名（同 username） |
| `isAdmin` | ComputedRef<Boolean> | 是否管理员。判断逻辑：1) JWT payload 中角色为 admin；2) 或 role 状态为 admin |

**isAdmin 判断逻辑细节：**

```js
const isAdmin = computed(() => {
  if (!token.value) return false
  const payload = decodeJwtPayload(token.value)
  if (!payload) {
    const r = role.value
    return !!(r && r.toLowerCase() === 'admin')
  }
  if (isTokenExpired(token.value, { useBuffer: false })) return false
  const r = role.value
  return !!(r && r.toLowerCase() === 'admin')
})
```

### 核心方法 (Actions)

#### initialize()

应用启动时的初始化，恢复 token 并监听 `auth-expired` 事件。

```js
function initialize() {
  initializeFromStorage()
  checkTokenExpiry()
  window.addEventListener('auth-expired', handleAuthExpired)
}
```

#### setAuth(data) -- 设置认证数据

登录成功后调用，将 token 和用户信息写入响应式状态和 localStorage。

**参数：** `data` = `{ token, id, username, role }`

```js
function setAuth(data) {
  token.value = data.token || ''
  userId.value = data.id || ''
  username.value = data.username || ''
  role.value = data.role || 'user'

  safeSetItem('token', token.value)
  safeSetItem('userId', userId.value)
  safeSetItem('userName', username.value)
  safeSetItem('role', role.value)
}
```

#### clearAuth() -- 清除认证数据

退出登录或 token 过期时调用，清空所有响应式状态和 localStorage。

#### login(loginData) -- 用户登录

**参数：** `{ username, password }`

**返回：** `{ success: Boolean, data?: AuthData, message?: String }`

调用 POST `/user/login`，成功时调用 `setAuth()`。

#### logout() -- 用户退出

POST `/user/logout`（失败不阻断），然后 `clearAuth()`。

#### validateToken() -- 验证令牌

GET `/user/validate`，服务端验证 token 有效性。有效时同步更新 userId、username、role 到最新值。

**返回：** Boolean

#### fetchUserInfo() -- 获取用户详细信息

GET `/user/me`，获取完整用户对象存入 `userInfo` ref。

**返回：** UserInfo | null

#### changePassword(data) -- 修改密码

POST `/user/change-password`，成功后自动清除认证数据（需要重新登录）。

**参数：** `{ oldPassword, newPassword }`

#### initializeFromStorage() -- 从 localStorage 恢复

尝试从 localStorage 恢复 token 和用户信息，同时检查 JWT 是否过期。

### JWT Token 工具函数

文件内定义的纯函数（不依赖 Vue 响应式系统）：

| 函数 | 说明 |
|------|------|
| `decodeJwtPayload(token)` | 解码 JWT payload（Base64 解码，含 UTF-8 字符处理） |
| `getTokenExpiryTime(token)` | 从 JWT 中提取 exp 时间戳 |
| `isTokenExpired(token, options?)` | 判断 token 是否过期。支持 buffer 模式（提前 5 分钟判定过期） |
| `getTokenRemainingTime(token)` | 获取 token 剩余有效毫秒数 |

**Token 过期判断：**

```js
// 严格模式：过期时间点后即判定过期
isTokenExpired(token, { useBuffer: false })

// Buffer 模式：提前 5 分钟判定过期（用于触发刷新）
isTokenExpired(token, { useBuffer: true, bufferMs: 5 * 60 * 1000 })
```

### localStorage 安全封装

所有 localStorage 操作通过 try-catch 包装，防止无痕模式/safari 隐私模式下抛异常：

| 函数 | 说明 |
|------|------|
| `safeSetItem(key, value)` | 安全写入 |
| `safeGetItem(key)` | 安全读取 |
| `safeRemoveItem(key)` | 安全删除 |

---

## 在路由守卫中的使用

`router/index.js` 的 `beforeEach` 守卫中使用 `useUserStore()` 进行认证检查：

1. 未登录用户访问需认证页面 -> 重定向到首页（附带 `redirect` 参数）
2. 非管理员用户访问 `/admin` -> 重定向到首页（附带 `noPermission` 参数）
3. Token 过期检查（本地解码 JWT 判断 exp）
4. Token 服务端验证（带 60 秒缓存）

---

## 在组件中的使用模式

```vue
<script setup>
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userName = computed(() => userStore.userName)
const isAdmin = computed(() => userStore.isAdmin)

function handleLogout() {
  await userStore.logout()
  router.push('/')
}
</script>
```
