# 状态管理目录 (stores)

本目录存放Pinia状态管理模块，用于管理应用的全局状态。

## 📖 什么是状态管理？

状态管理是一种集中管理应用数据的方式。当多个组件需要共享同一份数据时，使用状态管理可以：
- 避免组件间复杂的props传递
- 实现数据的集中管理
- 方便数据的持久化和恢复
- 提供统一的数据修改方式

## 📁 文件列表

| 文件名 | 功能说明 |
|--------|----------|
| `user.js` | 用户状态管理模块 |
| `index.js` | 状态管理统一导出入口 |

## 📦 详细说明

### user.js - 用户状态管理

管理用户登录状态、用户信息、权限等。

**状态 (State):**
| 属性名 | 类型 | 说明 |
|--------|------|------|
| `isLoggedIn` | Boolean | 是否已登录 |
| `userId` | Number | 用户ID |
| `userName` | String | 用户名 |
| `userRole` | String | 用户角色 |
| `token` | String | 登录令牌 |
| `avatar` | String | 用户头像URL |

**操作 (Actions):**
| 方法名 | 功能说明 |
|--------|----------|
| `initialize()` | 初始化用户状态（从本地存储恢复） |
| `login(userData)` | 登录，设置用户信息 |
| `logout()` | 登出，清除用户信息 |
| `updateProfile(data)` | 更新用户信息 |
| `setToken(token)` | 设置登录令牌 |

**使用示例:**
```vue
<script setup>
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()

// 获取响应式状态
const { isLoggedIn, userName, userRole } = storeToRefs(userStore)

// 调用操作方法
const handleLogin = (userData) => {
  userStore.login(userData)
}

const handleLogout = () => {
  userStore.logout()
}
</script>

<template>
  <div v-if="isLoggedIn">
    欢迎，{{ userName }}
    <button @click="handleLogout">退出登录</button>
  </div>
  <div v-else>
    <button @click="showLogin = true">登录</button>
  </div>
</template>
```

## 🎯 使用规范

### 定义Store
```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMyStore = defineStore('myStore', () => {
  // 状态
  const count = ref(0)
  
  // 计算属性
  const doubleCount = computed(() => count.value * 2)
  
  // 操作方法
  const increment = () => {
    count.value++
  }
  
  return {
    count,
    doubleCount,
    increment
  }
})
```

### 在组件中使用
```vue
<script setup>
import { useMyStore } from '@/stores/myStore'
import { storeToRefs } from 'pinia'

const myStore = useMyStore()

// 使用 storeToRefs 解构保持响应式
const { count, doubleCount } = storeToRefs(myStore)

// 方法可以直接解构
const { increment } = myStore
</script>
```

### 最佳实践
1. **单一职责**: 每个Store只管理一个领域的状态
2. **命名规范**: Store文件使用小驼峰命名，导出函数使用 `useXxxStore`
3. **类型安全**: 使用TypeScript定义状态类型
4. **持久化**: 敏感数据需要持久化到本地存储

## 📚 扩展阅读

- [Pinia 官方文档](https://pinia.vuejs.org/zh/)
- [Vue 3 状态管理](https://cn.vuejs.org/guide/scaling-up/state-management.html)
