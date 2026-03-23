# 状态管理目录说明

## 文件夹结构

本目录包含项目的状态管理文件，使用Pinia进行状态管理。

```
stores/
├── index.js  # Pinia初始化
└── user.js   # 用户状态管理
```

## 详细说明

### 1. index.js

**功能**：初始化Pinia状态管理库。

**主要内容**：
- 导入Pinia
- 创建Pinia实例
- 导出Pinia实例

**使用方法**：

在 `main.js` 中导入并使用：

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

### 2. user.js

**功能**：管理用户相关的状态。

**主要状态**：
- `userInfo`：用户信息
- `token`：认证令牌
- `isLogin`：登录状态
- `permissions`：用户权限

**主要方法**：
- `login`：用户登录
- `logout`：用户登出
- `updateUserInfo`：更新用户信息
- `setToken`：设置令牌
- `clearToken`：清除令牌

**使用方法**：

```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 登录
await userStore.login({
  username: 'admin',
  password: 'password'
})

// 检查登录状态
if (userStore.isLogin) {
  console.log('用户已登录')
}

// 登出
userStore.logout()
```

**状态持久化**：

用户令牌和基本信息会存储在localStorage中，实现页面刷新后状态保持。

**权限管理**：

通过 `permissions` 状态管理用户权限，用于控制页面访问和功能权限。

## Pinia使用指南

### 创建新的Store

```javascript
// stores/example.js
import { defineStore } from 'pinia'

export const useExampleStore = defineStore('example', {
  state: () => ({
    count: 0,
    name: 'example'
  }),
  getters: {
    doubleCount: (state) => state.count * 2
  },
  actions: {
    increment() {
      this.count++
    },
    setName(name) {
      this.name = name
    }
  }
})
```

### 访问Store

```javascript
import { useExampleStore } from '@/stores/example'

const exampleStore = useExampleStore()

// 访问状态
console.log(exampleStore.count)
console.log(exampleStore.doubleCount)

// 调用方法
exampleStore.increment()
exampleStore.setName('new name')
```

### 响应式使用

```vue
<template>
  <div>
    <p>Count: {{ exampleStore.count }}</p>
    <p>Double Count: {{ exampleStore.doubleCount }}</p>
    <button @click="exampleStore.increment">Increment</button>
  </div>
</template>

<script setup>
import { useExampleStore } from '@/stores/example'

const exampleStore = useExampleStore()
</script>
```

### 组合式API风格

```javascript
// stores/composable.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useComposableStore = defineStore('composable', () => {
  const count = ref(0)
  const name = ref('composable')
  
  const doubleCount = computed(() => count.value * 2)
  
  function increment() {
    count.value++
  }
  
  function setName(newName) {
    name.value = newName
  }
  
  return {
    count,
    name,
    doubleCount,
    increment,
    setName
  }
})
```

## 开发规范

1. **命名规范**：Store名称使用camelCase，文件名为功能相关的名称
2. **状态管理**：每个Store应该专注于管理特定领域的状态
3. **方法命名**：使用camelCase命名方法
4. **状态持久化**：对于需要持久化的状态，使用localStorage或sessionStorage
5. **类型定义**：为状态和方法添加适当的注释说明

## 注意事项

- 避免在Store中直接操作DOM
- 避免在Store中进行复杂的业务逻辑，应该将业务逻辑放在composables中
- 对于异步操作，使用async/await
- 合理使用getters进行状态计算
- 避免创建过多的Store，应该按照功能模块进行组织

---

**最后更新时间**：2026年3月23日