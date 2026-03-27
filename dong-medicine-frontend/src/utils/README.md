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

---

**最后更新时间**: 2026年3月27日
