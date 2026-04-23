# 状态管理目录 (stores/)

> 类比：想象一个侗族村寨的**全局公告板**。村长在公告板上发布消息（更新状态），所有村民（组件）都能看到公告板上的内容（读取状态），也可以在公告板上留言（修改状态）。**Pinia store 就是这个公告板**，让所有组件共享同一份数据。

## 什么是状态管理？

在 Vue 应用中，"状态"就是**需要在不同组件之间共享的数据**。比如：
- 用户是否已登录？-- 导航栏、个人中心、收藏按钮都需要知道
- 用户名是什么？-- 导航栏要显示，评论要关联
- 用户是管理员吗？-- 管理后台入口要判断

如果没有状态管理，这些数据只能通过 props 一层层传递，非常麻烦。

## 为什么需要 Pinia？-- Props Drilling 问题

### 没有 Pinia 时的痛苦

类比：想象你要把一把钥匙从村口传到村尾，但中间隔了 5 户人家，每户都要帮你传一下。这就是 **Props Drilling（属性穿透）**。

```
App.vue（有用户数据）
  └── AppHeader.vue（需要用户名显示）
        └── UserMenu.vue（需要判断是否登录）
              └── LoginButton.vue（需要触发登录）
```

每层都要写 `props` 传递，中间的组件明明不需要这些数据，却被迫当"传话筒"：

```vue
<!-- App.vue -->
<AppHeader :username="username" :isLoggedIn="isLoggedIn" @login="handleLogin" />

<!-- AppHeader.vue -- 明明自己不用，只是传话 -->
<UserMenu :username="username" :isLoggedIn="isLoggedIn" @login="$emit('login')" />

<!-- UserMenu.vue -- 终于用到了 -->
<LoginButton v-if="!isLoggedIn" @login="$emit('login')" />
<span v-else>{{ username }}</span>
```

### 有了 Pinia 后的清爽

```vue
<!-- 任何组件，直接从 store 读取，不需要传 props -->
<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<template>
  <LoginButton v-if="!userStore.isLoggedIn" @click="userStore.login(data)" />
  <span v-else>{{ userStore.userName }}</span>
</template>
```

**一句话总结**：Pinia 让数据"一步到位"，不需要中间人传话。

---

## 文件清单

| 文件 | 职责 |
|------|------|
| `user.js` | 用户状态管理（登录、token、角色） |
| `index.js` | 创建 Pinia 实例 |

---

## user.js -- 用户状态 Store 详解

这是项目唯一的状态 store，管理用户的所有认证信息。

### Store 的三大组成部分

Pinia store 由三部分组成，类比一个公告板系统：

| 组成 | 类比 | 代码关键字 | 说明 |
|------|------|-----------|------|
| State（状态） | 公告板上的内容 | `ref()` | 存储数据的地方 |
| Getters（计算属性） | 对公告内容的解读 | `computed()` | 根据状态派生出的值 |
| Actions（操作） | 修改公告的动作 | `function` | 修改状态的方法 |

### State -- 存储什么数据？

```javascript
export const useUserStore = defineStore('user', () => {
  // 核心认证数据
  const token = ref('')       // JWT 令牌，证明"我是我"
  const userId = ref('')      // 用户 ID
  const username = ref('')    // 用户名
  const role = ref('')        // 角色（user / admin）
  const userInfo = ref(null)  // 完整用户信息对象
})
```

**为什么用 `ref()` 而不是 `reactive()`？**
- `ref()` 可以替换整个值（`token.value = 'newToken'`），`reactive()` 不行
- `ref()` 更灵活，可以存储任何类型的值
- 在模板中 `ref` 会自动解包，使用时不需要 `.value`

### Getters -- 派生计算

Getters 是根据 State 自动计算出的值，State 变了，Getter 自动更新：

```javascript
// 是否已登录？-- token 存在且未过期
const isLoggedIn = computed(() => {
  if (!token.value) return false
  return !isTokenExpired(token.value, { useBuffer: false })
})

// 用户名 -- 方便模板中使用
const userName = computed(() => username.value)

// 是否是管理员？-- token 有效且角色为 admin
const isAdmin = computed(() => {
  if (!token.value || isTokenExpired(token.value, { useBuffer: false })) return false
  const r = role.value
  return !!(r && r.toLowerCase() === 'admin')
})
```

**为什么不用 `function` 而用 `computed`？**
- `computed` 有缓存，只有依赖的 State 变了才重新计算
- `function` 每次调用都重新执行，浪费性能

### Actions -- 核心操作

#### 登录 -- login

```javascript
async function login(loginData) {
  try {
    // 调用后端登录接口
    const res = await request.post('/user/login', loginData)
    if (res.code === 200 && res.data) {
      // 登录成功，保存认证信息
      setAuth(res.data)
      return { success: true, data: res.data }
    }
    return { success: false, message: res.msg || '登录失败' }
  } catch (error) {
    return { success: false, message: error.message || '登录失败' }
  }
}
```

#### 退出登录 -- logout

```javascript
async function logout() {
  try {
    // 通知后端退出（即使失败也要清除本地状态）
    await request.post('/user/logout', {}, { skipAuthRefresh: true })
  } catch (error) {
    console.debug('退出登录请求失败:', error)
  } finally {
    // 无论后端是否成功，都清除本地登录状态
    clearAuth()
  }
}
```

#### 验证 Token -- validateToken

```javascript
async function validateToken() {
  // 1. 本地检查：token 是否存在？
  if (!token.value) {
    clearAuth()
    return false
  }

  // 2. 本地检查：token 是否过期？
  if (isTokenExpired(token.value, { useBuffer: false })) {
    clearAuth()
    return false
  }

  // 3. 远程验证：请求后端确认 token 仍然有效
  try {
    const res = await request.get('/user/validate')
    if (res.code === 200 && res.data) {
      // 更新用户信息（后端可能更新了角色等）
      userId.value = res.data.id
      username.value = res.data.username
      role.value = res.data.role || 'user'
      return true
    }
    clearAuth()
    return false
  } catch (error) {
    clearAuth()
    return false
  }
}
```

---

## JWT 处理 -- Token 的"身份证"机制

### 什么是 JWT？

类比：JWT 就像一张**电子身份证**。它包含三部分：

```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJhZG1pbiIsImV4cCI6MTcxMzg4MDAwMH0.签名部分
|--- 头部 ---|  |----------------- 载荷（用户信息）------------------|  |-- 签名 --|
```

- **头部（Header）**：说明这是什么类型的令牌
- **载荷（Payload）**：包含用户 ID、角色、过期时间等信息
- **签名（Signature）**：防止被篡改的校验码

### 解码 JWT -- decodeJwtPayload

```javascript
function decodeJwtPayload(token) {
  try {
    // JWT 的第二部分是载荷，用 . 分割后取第二段
    const base64Url = token.split('.')[1]
    if (!base64Url) return null

    // Base64URL 转 Base64（替换特殊字符）
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')

    // Base64 解码为 JSON 字符串，再解析为对象
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
    // 返回类似：{ userId: 1, role: "admin", exp: 1713880000 }
  } catch {
    return null  // 解码失败，token 无效
  }
}
```

### 判断 Token 是否过期 -- isTokenExpired

```javascript
// 过期缓冲时间：提前 5 分钟认为过期，避免临界状态
const TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000

function isTokenExpired(token, options = {}) {
  const { useBuffer = true, bufferMs = TOKEN_EXPIRY_BUFFER_MS } = options
  const expiryTime = getTokenExpiryTime(token)  // 从 JWT 中提取过期时间戳
  if (!expiryTime) return true  // 无法获取过期时间，视为已过期

  const now = Date.now()
  // useBuffer=true：提前 5 分钟认为过期（安全起见）
  // useBuffer=false：精确判断是否过期
  return useBuffer ? now >= expiryTime - bufferMs : now >= expiryTime
}
```

**为什么要提前 5 分钟？** 因为 token 可能在请求途中过期。比如 token 还剩 10 秒过期，你发了一个需要 15 秒的请求，请求到达后端时 token 已经过期了。提前 5 分钟刷新，可以避免这种边界情况。

---

## Session Storage 安全包装

`sessionStorage` 在某些情况下可能不可用（比如隐私模式），所以项目做了安全包装：

```javascript
// 安全写入：如果 sessionStorage 不可用，不会报错
function safeSetItem(key, value) {
  try {
    sessionStorage.setItem(key, value)
  } catch {
    console.warn('sessionStorage not available, falling back to memory')
  }
}

// 安全读取：如果 sessionStorage 不可用，返回 null
function safeGetItem(key) {
  try {
    return sessionStorage.getItem(key)
  } catch {
    return null
  }
}

// 安全删除：如果 sessionStorage 不可用，静默忽略
function safeRemoveItem(key) {
  try {
    sessionStorage.removeItem(key)
  } catch {
    // 忽略错误
  }
}
```

**为什么用 `sessionStorage` 而不是 `localStorage`？**
- `sessionStorage`：关闭浏览器标签页后自动清除，更安全
- `localStorage`：永久保存，即使用户关闭浏览器也不会清除
- 对于登录状态，我们希望用户关闭浏览器后自动退出，所以用 `sessionStorage`

---

## 在组件中使用 Store

### 读取状态

```vue
<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<template>
  <!-- 直接访问 state -->
  <span>{{ userStore.username }}</span>

  <!-- 访问 getter -->
  <div v-if="userStore.isLoggedIn">已登录</div>
  <div v-if="userStore.isAdmin">管理员</div>
</template>
```

### 调用 Action

```vue
<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

const handleLogin = async () => {
  const result = await userStore.login({
    username: 'admin',
    password: '123456'
  })
  if (result.success) {
    console.log('登录成功')
  } else {
    console.log('登录失败:', result.message)
  }
}
</script>
```

---

## 常见错误

### 错误 1：在 store 外部直接修改 state

```javascript
// 错误：直接修改 state，不利于追踪和调试
const userStore = useUserStore()
userStore.token = 'newToken'  // 绕过了 setAuth，不会同步到 sessionStorage

// 正确：通过 action 修改，确保 sessionStorage 同步更新
const userStore = useUserStore()
userStore.setAuth({ token: 'newToken', id: 1, username: 'admin', role: 'admin' })
```

### 错误 2：忘记解构会丢失响应性

```javascript
// 错误：解构后失去响应性，值不再随 store 更新
const { username, isLoggedIn } = useUserStore()
// username 和 isLoggedIn 是普通值，store 更新后不会自动更新

// 正确方式 1：直接使用 store
const userStore = useUserStore()
userStore.username  // 始终是最新值

// 正确方式 2：用 storeToRefs 解构（保持响应性）
import { storeToRefs } from 'pinia'
const { username, isLoggedIn } = storeToRefs(useUserStore())
```

### 错误 3：在 Pinia store 外部调用 useUserStore()

```javascript
// 错误：在 Pinia 插件安装之前调用，会报错
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()  // Pinia 还没安装！

// 正确：在 setup 函数或组件内部调用
// main.js 中先安装 Pinia
app.use(pinia)

// 然后在组件的 setup 中调用
<script setup>
const userStore = useUserStore()  // 此时 Pinia 已安装，正常工作
</script>
```

### 错误 4：混淆 token 的本地过期判断和远程验证

```javascript
// isTokenExpired：本地快速判断，不请求后端（快但可能不准）
isTokenExpired(token)  // 只检查 JWT 中的 exp 字段

// validateToken：远程验证，请求后端（慢但准确）
await userStore.validateToken()  // 后端可能已经手动让 token 失效

// 路由守卫中的正确做法：先本地判断，再远程验证
if (isTokenLocallyExpired(userStore)) {
  // 本地就过期了，不用浪费请求
  userStore.clearAuth()
} else {
  // 本地没过期，但后端可能已让 token 失效，需要远程确认
  const isValid = await userStore.validateToken()
}
```
