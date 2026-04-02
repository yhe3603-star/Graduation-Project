# 自定义指令目录 (directives)

本目录存放 Vue 自定义指令，用于封装底层 DOM 操作。

## 目录

- [什么是自定义指令？](#什么是自定义指令)
- [目录结构](#目录结构)
- [自定义指令详解](#自定义指令详解)

---

## 什么是自定义指令？

### 自定义指令的概念

**自定义指令**是 Vue 提供的一种机制，用于直接操作 DOM 元素。它就像给 HTML 元素添加"超能力"——比如自动获取焦点、点击外部关闭、懒加载等。

### 为什么需要自定义指令？

```
┌─────────────────────────────────────────────────────────────────┐
│                    使用自定义指令                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  需求：输入框自动获取焦点                                        │
│                                                                 │
│  不使用指令（繁琐）：                                            │
│  <template>                                                     │
│    <input ref="inputRef" />                                     │
│  </template>                                                    │
│  <script setup>                                                 │
│    const inputRef = ref(null)                                   │
│    onMounted(() => {                                            │
│      inputRef.value.focus()                                     │
│    })                                                           │
│  </script>                                                      │
│                                                                 │
│  使用指令（简洁）：                                              │
│  <template>                                                     │
│    <input v-focus />                                            │
│  </template>                                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
directives/
│
└── index.js                           # 指令定义和导出
```

---

## 自定义指令详解

### 指令钩子函数

```javascript
const myDirective = {
  // 绑定元素的父组件挂载前
  beforeMount(el, binding, vnode, prevVnode) {},
  
  // 父组件挂载时
  mounted(el, binding, vnode, prevVnode) {},
  
  // 父组件更新前
  beforeUpdate(el, binding, vnode, prevVnode) {},
  
  // 父组件更新时
  updated(el, binding, vnode, prevVnode) {},
  
  // 父组件卸载前
  beforeUnmount(el, binding, vnode, prevVnode) {},
  
  // 父组件卸载时
  unmounted(el, binding, vnode, prevVnode) {}
}
```

### 参数说明

| 参数 | 说明 |
|------|------|
| `el` | 指令绑定的 DOM 元素 |
| `binding` | 包含指令信息的对象 |
| `binding.value` | 指令的值，如 `v-focus="true"` 中的 `true` |
| `binding.arg` | 指令的参数，如 `v-focus:input` 中的 `input` |
| `binding.modifiers` | 修饰符对象，如 `v-focus.lazy` 中的 `{ lazy: true }` |

### 常用自定义指令示例

```javascript
// directives/index.js

/**
 * v-focus - 自动获取焦点
 * 使用：<input v-focus />
 */
export const focus = {
  mounted(el) {
    el.focus()
  }
}

/**
 * v-click-outside - 点击外部触发
 * 使用：<div v-click-outside="handleClickOutside">...</div>
 */
export const clickOutside = {
  mounted(el, binding) {
    el._clickOutside = (event) => {
      // 如果点击的不是元素本身或其子元素
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el._clickOutside)
  }
}

/**
 * v-lazy - 图片懒加载
 * 使用：<img v-lazy="imageUrl" />
 */
export const lazy = {
  mounted(el, binding) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          el.src = binding.value
          observer.unobserve(el)
        }
      })
    })
    observer.observe(el)
  }
}

/**
 * v-debounce - 防抖
 * 使用：<button v-debounce:click="handleClick">点击</button>
 */
export const debounce = {
  mounted(el, binding) {
    const delay = binding.arg ? parseInt(binding.arg) : 300
    let timer = null
    
    el._debounceHandler = () => {
      clearTimeout(timer)
      timer = setTimeout(() => {
        binding.value()
      }, delay)
    }
    
    el.addEventListener('click', el._debounceHandler)
  },
  unmounted(el) {
    el.removeEventListener('click', el._debounceHandler)
  }
}

/**
 * v-permission - 权限控制
 * 使用：<button v-permission="'admin'">管理员可见</button>
 */
export const permission = {
  mounted(el, binding) {
    const { value } = binding
    const userRole = getUserRole() // 获取当前用户角色
    
    if (value !== userRole) {
      el.parentNode?.removeChild(el)
    }
  }
}

// 导出所有指令
export default {
  focus,
  clickOutside,
  lazy,
  debounce,
  permission
}
```

### 注册指令

```javascript
// main.js
import { createApp } from 'vue'
import App from './App.vue'
import directives from './directives'

const app = createApp(App)

// 全局注册所有指令
Object.keys(directives).forEach(key => {
  app.directive(key, directives[key])
})

app.mount('#app')
```

---

## 使用示例

### v-focus - 自动焦点

```vue
<template>
  <!-- 页面加载后自动获取焦点 -->
  <input v-focus placeholder="请输入搜索关键词">
</template>
```

### v-click-outside - 点击外部

```vue
<template>
  <div class="dropdown-container">
    <button @click="showDropdown = !showDropdown">显示下拉</button>
    
    <!-- 点击下拉框外部时关闭 -->
    <div v-if="showDropdown" v-click-outside="closeDropdown" class="dropdown">
      下拉内容...
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showDropdown = ref(false)

const closeDropdown = () => {
  showDropdown.value = false
}
</script>
```

### v-lazy - 图片懒加载

```vue
<template>
  <!-- 图片进入可视区域才加载 -->
  <img v-lazy="imageUrl" alt="图片">
</template>
```

### v-debounce - 防抖

```vue
<template>
  <!-- 500ms内多次点击只执行一次 -->
  <button v-debounce:500="handleSubmit">提交</button>
</template>
```

---

## 最佳实践

### 1. 指令命名使用 kebab-case

```javascript
// ✅ 好的做法
app.directive('click-outside', clickOutside)

// ❌ 不好的做法
app.directive('clickOutside', clickOutside)
```

### 2. 指令应该做简单的事情

```javascript
// ✅ 好的做法：简单的DOM操作
export const focus = {
  mounted(el) {
    el.focus()
  }
}

// ❌ 不好的做法：复杂的业务逻辑
export const submitForm = {
  mounted(el) {
    // 复杂的表单验证和提交逻辑
    // 这应该在组件中处理
  }
}
```

### 3. 记得清理事件监听

```javascript
export const clickOutside = {
  mounted(el, binding) {
    el._handler = (e) => binding.value(e)
    document.addEventListener('click', el._handler)
  },
  unmounted(el) {
    // 重要：移除事件监听，防止内存泄漏
    document.removeEventListener('click', el._handler)
  }
}
```

---

**相关文档**

- [Vue 3 自定义指令](https://cn.vuejs.org/guide/reusability/custom-directives.html)

---

**最后更新时间**：2026年4月3日
