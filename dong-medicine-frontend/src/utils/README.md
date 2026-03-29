# Utils 工具函数目录

本目录包含项目的所有工具函数模块。

## 目录结构

```
utils/
├── index.js              # 通用工具函数入口
├── request.js            # Axios HTTP请求封装
├── xss.js                # XSS防护工具
├── logger.js             # 日志工具
├── media.js              # 媒体处理工具
├── adminUtils.js         # 管理后台工具
├── cache.js              # 缓存工具
└── README.md             # 说明文档
```

## 模块说明

### index.js - 通用工具函数

**导出函数**:

| 函数 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `formatTime(date, format)` | date: Date/string, format: string | string | 时间格式化 |
| `extractData(response)` | response: object | any | 响应数据提取 |
| `getRankClass(rank)` | rank: number | string | 获取排名样式类 |
| `formatFileSize(bytes)` | bytes: number | string | 文件大小格式化 |
| `truncate(str, length)` | str: string, length: number | string | 文本截断 |
| `debounce(fn, delay)` | fn: Function, delay: number | Function | 防抖函数 |
| `throttle(fn, delay)` | fn: Function, delay: number | Function | 节流函数 |
| `deepClone(obj)` | obj: object | object | 深拷贝 |
| `isEmpty(value)` | value: any | boolean | 空值判断 |
| `isNotEmpty(value)` | value: any | boolean | 非空判断 |
| `generateId()` | - | string | 生成唯一ID |
| `sleep(ms)` | ms: number | Promise | 延迟函数 |
| `retry(fn, times, delay)` | fn: Function, times: number, delay: number | Promise | 重试函数 |

**使用示例**:
```javascript
import { formatTime, debounce, deepClone } from '@/utils'

// 时间格式化
formatTime(new Date(), 'YYYY-MM-DD HH:mm:ss')

// 防抖
const handleSearch = debounce((keyword) => {
  console.log(keyword)
}, 300)

// 深拷贝
const cloned = deepClone(originalObject)
```

---

### request.js - Axios HTTP请求封装

**核心功能**:
- 请求/响应拦截器
- Token 自动注入
- Token 刷新机制（Promise缓存，避免竞态）
- 请求取消（防重复提交）
- 自动重试机制
- XSS/SQL 注入防护
- 统一错误处理

**配置**:
```javascript
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})
```

**Token刷新机制**:
```javascript
let refreshPromise = null

async function getOrRefreshToken() {
  if (refreshPromise) {
    return refreshPromise  // 多个401请求共享同一个刷新Promise
  }
  
  refreshPromise = refreshToken()
  try {
    return await refreshPromise
  } finally {
    refreshPromise = null
  }
}
```

**错误提示优化**:
```javascript
const errorMessages = {
  400: "请求参数有误，请检查输入",
  404: "请求的资源不存在",
  429: "请求过于频繁，请稍后重试",
  500: "服务器内部错误，请稍后重试",
  // ...
}
```

**使用示例**:
```javascript
import request from '@/utils/request'

// GET请求
const data = await request.get('/plants/list', { params: { page: 1 } })

// POST请求
const result = await request.post('/user/login', { username, password })

// 上传文件
const formData = new FormData()
formData.append('file', file)
await request.post('/upload/image', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
```

---

### xss.js - XSS防护工具

**覆盖30+危险模式**:
- script标签
- javascript/vbscript协议
- 事件处理器 (onclick, onerror等)
- HTML实体编码
- eval/expression函数
- 危险标签 (iframe, object, embed等)

**导出函数**:

| 函数 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `containsXss(input)` | input: string | boolean | 检测是否包含XSS |
| `sanitize(input)` | input: string | string | 清理XSS代码 |
| `sanitizeForLog(input)` | input: string | string | 日志专用清理 |
| `containsSqlInjection(input)` | input: string | boolean | 检测SQL注入 |

**使用示例**:
```javascript
import { containsXss, sanitize } from '@/utils/xss'

// 检测XSS
if (containsXss(userInput)) {
  alert('输入包含非法字符')
  return
}

// 清理XSS
const cleanInput = sanitize(userInput)
```

---

### logger.js - 日志工具

**导出函数**:

| 函数 | 参数 | 说明 |
|------|------|------|
| `logInfo(message, data)` | message: string, data: object | 信息日志 |
| `logWarn(message, data)` | message: string, data: object | 警告日志 |
| `logError(message, error)` | message: string, error: Error | 错误日志 |
| `logAuthWarn(message, data)` | message: string, data: object | 认证警告 |
| `logSecurityWarn(message, data)` | message: string, data: object | 安全警告 |

**使用示例**:
```javascript
import { logInfo, logError } from '@/utils/logger'

logInfo('用户登录成功', { userId: 1 })
logError('请求失败', error)
```

---

### media.js - 媒体处理工具

**导出函数**:

| 函数 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getFileIcon(filename)` | filename: string | string | 获取文件图标 |
| `isImageFile(filename)` | filename: string | boolean | 判断是否图片 |
| `isVideoFile(filename)` | filename: string | boolean | 判断是否视频 |
| `isDocumentFile(filename)` | filename: string | boolean | 判断是否文档 |
| `getMediaType(filename)` | filename: string | string | 获取媒体类型 |

**支持的文件类型**:
- 图片: jpg, jpeg, png, gif, webp, svg, bmp
- 视频: mp4, avi, mov, wmv, flv, mkv
- 文档: pdf, doc, docx, xls, xlsx, ppt, pptx, txt

---

### adminUtils.js - 管理后台工具

**导出函数**:

| 函数 | 说明 |
|------|------|
| `formatTableData(data)` | 格式化表格数据 |
| `getStatusTag(status)` | 获取状态标签配置 |
| `getLevelTag(level)` | 获取级别标签配置 |
| `exportToExcel(data, filename)` | 导出Excel |

---

### cache.js - 缓存工具

**导出函数**:

| 函数 | 参数 | 说明 |
|------|------|------|
| `setCache(key, value, ttl)` | key: string, value: any, ttl: number | 设置缓存 |
| `getCache(key)` | key: string | 获取缓存 |
| `removeCache(key)` | key: string | 移除缓存 |
| `clearCache()` | - | 清除所有缓存 |

**使用示例**:
```javascript
import { setCache, getCache } from '@/utils/cache'

// 设置缓存（10分钟过期）
setCache('user-preferences', { theme: 'dark' }, 600000)

// 获取缓存
const prefs = getCache('user-preferences')
```

---

## 开发规范

1. **命名规范**: 函数使用小驼峰命名法
2. **导出方式**: 使用命名导出 `export function xxx()`
3. **类型注释**: 复杂函数添加JSDoc注释
4. **单元测试**: 工具函数应有对应的测试用例

### JSDoc注释规范

```javascript
/**
 * 格式化时间
 * @param {Date|string|number} date - 时间对象、字符串或时间戳
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 * @example
 * formatTime(new Date(), 'YYYY-MM-DD') // '2024-01-15'
 * formatTime('2024-01-15', 'MM/DD/YYYY') // '01/15/2024'
 */
export function formatTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  // ...
}
```

---

## 已知限制

| 工具函数 | 限制 | 影响 |
|----------|------|------|
| formatTime | 不支持时区转换 | 多时区场景需额外处理 |
| deepClone | 不支持函数、Symbol、循环引用 | 特殊对象克隆失败 |
| debounce | 不支持立即执行模式 | 某些场景需要首次立即执行 |
| throttle | 不支持leading/trailing配置 | 无法精细控制首次/末次执行 |
| cache | 仅支持内存缓存 | 页面刷新后缓存丢失 |
| request | 不支持请求优先级 | 无法取消低优先级请求 |
| xss.sanitize | 可能误删合法内容 | 富文本场景需特殊处理 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **request.js**
   - 添加请求取消功能
   - 实现请求重试配置
   - 添加请求缓存

2. **cache.js**
   - 支持localStorage/sessionStorage
   - 实现缓存过期清理
   - 添加缓存大小限制

3. **xss.js**
   - 添加白名单配置
   - 支持富文本清理策略

### 中期改进 (1-2月)

1. **TypeScript支持**
   - 添加类型定义文件
   - 提供更好的IDE支持

2. **性能优化**
   - 实现lazy import
   - 添加tree-shaking支持

3. **功能增强**
   - 添加URL解析工具
   - 实现Cookie操作工具
   - 添加设备检测工具

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 响应式系统（部分函数） |
| Axios | 1.6+ | HTTP请求 |

---

## 常见问题

### 1. request.js 请求超时如何处理？

```javascript
import request from '@/utils/request'

try {
  const data = await request.get('/api/data', {
    timeout: 60000  // 覆盖默认超时时间
  })
} catch (error) {
  if (error.code === 'ECONNABORTED') {
    console.error('请求超时')
  }
}
```

### 2. 如何取消正在进行的请求？

```javascript
import axios from 'axios'

const source = axios.CancelToken.source()

request.get('/api/data', {
  cancelToken: source.token
})

// 取消请求
source.cancel('用户取消了请求')
```

### 3. debounce 和 throttle 的区别？

| 特性 | debounce | throttle |
|------|----------|----------|
| 触发时机 | 停止调用后执行 | 持续调用时定期执行 |
| 适用场景 | 搜索输入、窗口调整 | 滚动事件、拖拽 |
| 执行次数 | 最后一次调用后执行一次 | 持续调用期间多次执行 |

```javascript
// 搜索场景用debounce
const handleSearch = debounce(search, 300)

// 滚动场景用throttle
const handleScroll = throttle(updatePosition, 100)
```

### 4. 如何处理XSS误报？

```javascript
import { sanitize } from '@/utils/xss'

// 允许部分HTML标签
const cleanHtml = sanitize(userInput, {
  allowedTags: ['b', 'i', 'em', 'strong', 'a'],
  allowedAttributes: {
    'a': ['href']
  }
})
```

### 5. 缓存数据如何持久化？

```javascript
// 使用localStorage
const setPersistentCache = (key, value, ttl) => {
  const item = {
    value,
    expiry: Date.now() + ttl
  }
  localStorage.setItem(key, JSON.stringify(item))
}

const getPersistentCache = (key) => {
  const itemStr = localStorage.getItem(key)
  if (!itemStr) return null
  
  const item = JSON.parse(itemStr)
  if (Date.now() > item.expiry) {
    localStorage.removeItem(key)
    return null
  }
  return item.value
}
```

### 6. deepClone 遇到循环引用怎么办？

```javascript
// 使用专门的库处理循环引用
import { cloneDeep } from 'lodash-es'

const cloned = cloneDeep(complexObject)
```

---

## 性能建议

### 1. 避免在循环中调用工具函数

```javascript
// 不推荐
data.forEach(item => {
  item.formattedTime = formatTime(item.time)
})

// 推荐：批量处理
const formatTimes = (items) => {
  return items.map(item => ({
    ...item,
    formattedTime: formatTime(item.time)
  }))
}
```

### 2. 合理使用缓存

```javascript
// 频繁调用的函数使用缓存
const memoizedFormatTime = (() => {
  const cache = new Map()
  return (date, format) => {
    const key = `${date}-${format}`
    if (cache.has(key)) return cache.get(key)
    const result = formatTime(date, format)
    cache.set(key, result)
    return result
  }
})()
```

### 3. 按需导入

```javascript
// 不推荐：导入全部
import * as utils from '@/utils'

// 推荐：按需导入
import { formatTime, debounce } from '@/utils'
```

---

**最后更新时间**: 2026年3月30日
