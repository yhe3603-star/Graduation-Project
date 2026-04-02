# 状态管理目录 (stores)

本目录存放 Pinia 状态管理模块，用于管理全局共享状态。

## 目录

- [什么是状态管理？](#什么是状态管理)
- [目录结构](#目录结构)
- [Pinia基础概念](#pinia基础概念)
- [用户状态模块](#用户状态模块)
- [最佳实践](#最佳实践)

---

## 什么是状态管理？

### 状态管理的概念

**状态管理**是用于在多个组件之间共享数据的机制。你可以把它想象成一个"公共仓库"——所有组件都可以从这里存取数据，当数据变化时，所有使用该数据的组件都会自动更新。

### 为什么需要状态管理？

```
┌─────────────────────────────────────────────────────────────────┐
│                    没有状态管理                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  组件A（用户信息）                                               │
│    ↓ 登录成功，保存用户信息                                      │
│    ↓ 需要传递给组件B、C、D...                                    │
│    ↓ 层层传递，非常繁琐                                          │
│                                                                 │
│  组件B ← 组件A传递                                               │
│  组件C ← 组件B传递                                               │
│  组件D ← 组件C传递                                               │
│                                                                 │
│  → 数据传递复杂、难以维护                                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     有状态管理                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│                    ┌─────────────┐                              │
│                    │   Store     │                              │
│                    │  (公共仓库)  │                              │
│                    └─────────────┘                              │
│                          ↑↓                                     │
│         ┌────────────────┼────────────────┐                    │
│         ↓                ↓                ↓                    │
│    ┌─────────┐     ┌─────────┐     ┌─────────┐                │
│    │ 组件A   │     │ 组件B   │     │ 组件C   │                │
│    │ 登录    │     │ 显示    │     │ 修改    │                │
│    └─────────┘     └─────────┘     └─────────┘                │
│                                                                 │
│  → 数据集中管理、自动更新、易于维护                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 什么时候需要状态管理？

| 场景 | 是否需要状态管理 |
|------|------------------|
| 用户登录状态（全局） | ✅ 需要 |
| 用户信息（全局） | ✅ 需要 |
| 主题设置（全局） | ✅ 需要 |
| 单个页面的临时数据 | ❌ 不需要（用组件内部状态） |
| 父子组件传递数据 | ❌ 不需要（用Props/Emit） |

---

## 目录结构

```
stores/
│
├── index.js                           # Pinia 实例配置
└── user.js                            # 用户状态模块
```

---

## Pinia基础概念

### 什么是Pinia？

Pinia 是 Vue 3 官方推荐的状态管理库，是 Vuex 的替代品。

**Pinia的优势：**
- 更简单的API
- 更好的TypeScript支持
- 不需要mutations
- 支持多个Store

### Store的基本结构

```javascript
// stores/counter.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 定义Store
export const useCounterStore = defineStore('counter', () => {
  // 1. State（状态）- 使用 ref
  const count = ref(0)
  
  // 2. Getters（计算属性）- 使用 computed
  const doubleCount = computed(() => count.value * 2)
  
  // 3. Actions（方法）- 普通函数
  const increment = () => {
    count.value++
  }
  
  const decrement = () => {
    count.value--
  }
  
  // 4. 返回需要暴露的内容
  return {
    count,
    doubleCount,
    increment,
    decrement
  }
})
```

### 使用Store

```vue
<template>
  <div>
    <p>计数: {{ counter.count }}</p>
    <p>双倍: {{ counter.doubleCount }}</p>
    <button @click="counter.increment">+1</button>
    <button @click="counter.decrement">-1</button>
  </div>
</template>

<script setup>
import { useCounterStore } from '@/stores/counter'

// 获取Store实例
const counter = useCounterStore()

// 直接访问状态
console.log(counter.count)

// 调用方法
counter.increment()
</script>
```

### 解构使用

```vue
<script setup>
import { storeToRefs } from 'pinia'
import { useCounterStore } from '@/stores/counter'

const counter = useCounterStore()

// 解构状态（需要使用 storeToRefs 保持响应式）
const { count, doubleCount } = storeToRefs(counter)

// 解构方法（直接解构即可）
const { increment, decrement } = counter
</script>
```

---

## 用户状态模块

### user.js - 用户状态管理

```javascript
/**
 * 用户状态管理模块
 * 管理用户登录状态、用户信息、Token等
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, logout, validateToken, getUserInfo } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // ==================== State ====================
  
  // Token
  const token = ref(localStorage.getItem('token') || '')
  
  // 用户ID
  const userId = ref(parseInt(localStorage.getItem('userId')) || null)
  
  // 用户名
  const username = ref(localStorage.getItem('username') || '')
  
  // 用户角色
  const role = ref(localStorage.getItem('role') || '')
  
  // 用户详细信息
  const userInfo = ref(null)
  
  // Token验证缓存
  const lastValidationTime = ref(0)
  const cachedValidationResult = ref(null)
  
  // ==================== Getters ====================
  
  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)
  
  // 是否是管理员
  const isAdmin = computed(() => role.value === 'ADMIN')
  
  // ==================== Actions ====================
  
  /**
   * 设置认证信息
   */
  const setAuth = (data) => {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    role.value = data.role
    
    // 持久化到localStorage
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId.toString())
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
  }
  
  /**
   * 清除认证信息
   */
  const clearAuth = () => {
    token.value = ''
    userId.value = null
    username.value = ''
    role.value = ''
    userInfo.value = null
    
    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }
  
  /**
   * 用户登录
   */
  const loginAction = async (loginData) => {
    const res = await login(loginData)
    setAuth(res.data)
    return res
  }
  
  /**
   * 用户注册
   */
  const registerAction = async (registerData) => {
    const res = await register(registerData)
    return res
  }
  
  /**
   * 用户登出
   */
  const logoutAction = async () => {
    try {
      await logout()
    } finally {
      clearAuth()
      router.push('/')
    }
  }
  
  /**
   * 验证Token有效性
   * 带缓存，60秒内不重复验证
   */
  const validateTokenAction = async () => {
    if (!token.value) {
      return false
    }
    
    // 检查缓存（60秒内有效）
    const now = Date.now()
    if (now - lastValidationTime.value < 60000 && cachedValidationResult.value !== null) {
      return cachedValidationResult.value
    }
    
    try {
      const res = await validateToken()
      cachedValidationResult.value = res.data === true
      lastValidationTime.value = now
      return cachedValidationResult.value
    } catch {
      clearAuth()
      return false
    }
  }
  
  /**
   * 获取用户详细信息
   */
  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res.data
  }
  
  // 返回需要暴露的内容
  return {
    // State
    token,
    userId,
    username,
    role,
    userInfo,
    
    // Getters
    isLoggedIn,
    isAdmin,
    
    // Actions
    setAuth,
    clearAuth,
    loginAction,
    registerAction,
    logoutAction,
    validateTokenAction,
    fetchUserInfo
  }
})
```

### 在组件中使用

```vue
<template>
  <div>
    <!-- 未登录显示登录按钮 -->
    <div v-if="!userStore.isLoggedIn">
      <button @click="handleLogin">登录</button>
    </div>
    
    <!-- 已登录显示用户信息 -->
    <div v-else>
      <span>{{ userStore.username }}</span>
      <button @click="handleLogout">退出</button>
    </div>
    
    <!-- 管理员可见 -->
    <div v-if="userStore.isAdmin">
      <button @click="goToAdmin">管理后台</button>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const handleLogin = async () => {
  await userStore.loginAction({
    username: 'admin',
    password: 'admin123'
  })
}

const handleLogout = () => {
  userStore.logoutAction()
}

const goToAdmin = () => {
  // 跳转到管理后台
}
</script>
```

### 在路由守卫中使用

```javascript
// router/index.js
import { useUserStore } from '@/stores/user'

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 需要登录的页面
  if (to.meta.requiresAuth) {
    // 验证Token
    const isValid = await userStore.validateTokenAction()
    
    if (!isValid) {
      // Token无效，跳转到首页
      next('/')
      return
    }
    
    // 需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/')
      return
    }
  }
  
  next()
})
```

---

## 最佳实践

### 1. 状态持久化

```javascript
// 使用 localStorage 持久化关键数据
const setAuth = (data) => {
  token.value = data.token
  localStorage.setItem('token', data.token)
}

// 初始化时从 localStorage 读取
const token = ref(localStorage.getItem('token') || '')
```

### 2. 解构注意事项

```javascript
import { storeToRefs } from 'pinia'

const userStore = useUserStore()

// 状态需要用 storeToRefs 保持响应式
const { username, isLoggedIn } = storeToRefs(userStore)

// 方法直接解构
const { loginAction, logoutAction } = userStore
```

### 3. 不要滥用状态管理

```javascript
// ❌ 不好的做法：所有数据都放Store
const pageData = ref({})  // 应该放在组件内部

// ✅ 好的做法：只有全局共享的数据放Store
const userInfo = ref({})  // 多个组件都需要
```

### 4. 组合式API风格

```javascript
// ✅ 推荐：使用组合式API风格
export const useUserStore = defineStore('user', () => {
  const count = ref(0)
  return { count }
})

// ❌ 不推荐：使用选项式API风格
export const useUserStore = defineStore('user', {
  state: () => ({ count: 0 }),
  getters: { ... },
  actions: { ... }
})
```

---

**相关文档**

- [Pinia 官方文档](https://pinia.vuejs.org/zh/)
- [Vue 3 状态管理](https://cn.vuejs.org/guide/scaling-up/state-management.html)

---

**最后更新时间**：2026年4月3日
