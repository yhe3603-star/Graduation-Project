# 工具函数目录 (utils)

本目录存放通用的工具函数，用于处理常见操作。

## 目录

- [什么是工具函数？](#什么是工具函数)
- [目录结构](#目录结构)
- [工具函数列表](#工具函数列表)
- [常用工具函数详解](#常用工具函数详解)

---

## 什么是工具函数？

### 工具函数的概念

**工具函数**是封装好的、可复用的功能代码。它就像一个"工具箱"——里面有各种小工具，需要的时候随时可以拿来用。

### 为什么需要工具函数？

```
┌─────────────────────────────────────────────────────────────────┐
│                    没有工具函数                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  组件A：需要格式化时间                                           │
│    → 自己写格式化代码                                           │
│                                                                 │
│  组件B：需要格式化时间                                           │
│    → 又写一遍格式化代码（重复）                                   │
│                                                                 │
│  组件C：需要格式化时间                                           │
│    → 再写一遍格式化代码（重复）                                   │
│                                                                 │
│  → 代码重复、难以维护                                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     有工具函数                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  utils/index.js：                                                │
│    export function formatTime(time) { ... }                     │
│                                                                 │
│  组件A：import { formatTime } from '@/utils'                     │
│  组件B：import { formatTime } from '@/utils'                     │
│  组件C：import { formatTime } from '@/utils'                     │
│                                                                 │
│  → 代码复用、易于维护                                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
utils/
│
├── index.js                           # 通用工具函数
├── adminUtils.js                      # 管理后台工具
├── cache.js                           # 缓存工具
├── logger.js                          # 日志工具
├── media.js                           # 媒体工具
├── request.js                         # Axios 封装
└── xss.js                             # XSS 防护
```

---

## 工具函数列表

### index.js - 通用工具

| 函数 | 说明 |
|------|------|
| `formatTime` | 时间格式化，支持相对时间 |
| `extractData` | 响应数据提取 |
| `getRankClass` | 获取排名样式类 |
| `formatFileSize` | 文件大小格式化 |
| `truncate` | 文本截断 |
| `debounce` | 防抖函数 |
| `throttle` | 节流函数 |
| `deepClone` | 深拷贝 |
| `isEmpty` | 空值判断 |
| `isNotEmpty` | 非空判断 |
| `generateId` | 生成唯一ID |
| `sleep` | 延迟函数 |
| `retry` | 重试函数 |

### request.js - Axios封装

| 功能 | 说明 |
|------|------|
| 请求拦截 | 自动注入Token |
| 响应拦截 | 统一错误处理 |
| Token刷新 | 自动刷新过期Token |
| 请求取消 | 防重复提交 |
| 自动重试 | 网络错误重试 |

### xss.js - XSS防护

| 函数 | 说明 |
|------|------|
| `containsXss` | 检测XSS攻击 |
| `cleanXss` | 清理XSS字符 |

---

## 常用工具函数详解

### formatTime - 时间格式化

```javascript
/**
 * 时间格式化
 * @param {string|Date} time - 时间
 * @param {string} format - 格式
 * @returns {string} 格式化后的时间
 */
export function formatTime(time, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 相对时间
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  
  // 绝对时间
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 使用示例
formatTime(new Date())        // "刚刚"
formatTime(Date.now() - 3600000)  // "1小时前"
formatTime('2024-01-01')      // "2024-01-01 00:00:00"
```

### debounce - 防抖函数

```javascript
/**
 * 防抖函数
 * 在事件触发后等待一段时间，如果这段时间内没有再次触发，才执行函数
 * @param {Function} fn - 要执行的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300) {
  let timer = null
  
  return function (...args) {
    clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 使用示例：搜索输入
const handleSearch = debounce((keyword) => {
  search(keyword)
}, 500)

// 用户输入时不会立即搜索，等500ms没有新输入才搜索
```

### throttle - 节流函数

```javascript
/**
 * 节流函数
 * 在一段时间内只执行一次函数
 * @param {Function} fn - 要执行的函数
 * @param {number} interval - 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(fn, interval = 300) {
  let lastTime = 0
  
  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= interval) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

// 使用示例：滚动事件
const handleScroll = throttle(() => {
  console.log('滚动位置:', window.scrollY)
}, 200)

window.addEventListener('scroll', handleScroll)
```

### deepClone - 深拷贝

```javascript
/**
 * 深拷贝
 * 创建一个对象的完全独立副本
 * @param {*} obj - 要拷贝的对象
 * @returns {*} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }
  
  if (obj instanceof Date) {
    return new Date(obj)
  }
  
  if (obj instanceof Array) {
    return obj.map(item => deepClone(item))
  }
  
  if (obj instanceof Object) {
    const copy = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        copy[key] = deepClone(obj[key])
      }
    }
    return copy
  }
}

// 使用示例
const original = { a: 1, b: { c: 2 } }
const copy = deepClone(original)
copy.b.c = 3
console.log(original.b.c)  // 2（原对象不受影响）
```

### formatFileSize - 文件大小格式化

```javascript
/**
 * 文件大小格式化
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

// 使用示例
formatFileSize(1024)       // "1 KB"
formatFileSize(1048576)    // "1 MB"
formatFileSize(1572864)    // "1.5 MB"
```

### request.js - Axios封装

```javascript
/**
 * Axios 请求封装
 * 包含请求/响应拦截、Token处理、错误处理等
 */
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 200) {
      return data
    }
    // 业务错误
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  (error) => {
    // 网络错误或服务器错误
    const message = error.response?.data?.msg || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default request

// 使用示例
import request from '@/utils/request'

// GET请求
const res = await request.get('/plants/list', { params: { page: 1 } })

// POST请求
const res = await request.post('/user/login', { username, password })
```

---

## 最佳实践

### 1. 函数单一职责

```javascript
// ✅ 好的做法：一个函数只做一件事
export function formatTime(time) { ... }
export function formatFileSize(bytes) { ... }

// ❌ 不好的做法：一个函数做多件事
export function format(time, type) {
  if (type === 'time') { ... }
  if (type === 'fileSize') { ... }
}
```

### 2. 函数命名清晰

```javascript
// ✅ 好的做法：命名清晰
export function isEmpty(value) { ... }
export function formatFileSize(bytes) { ... }

// ❌ 不好的做法：命名不清晰
export function check(v) { ... }
export function format(b) { ... }
```

### 3. 添加JSDoc注释

```javascript
/**
 * 防抖函数
 * @param {Function} fn - 要执行的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300) {
  // ...
}
```

---

**最后更新时间**：2026年4月3日
