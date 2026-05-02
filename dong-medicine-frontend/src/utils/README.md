# 工具函数目录 (utils/)

工具函数（Utility Function）是不依赖 Vue 组件生命周期的纯 JavaScript 函数/模块，可在任何地方调用。

## 文件清单

| 文件 | 职责 | 核心导出 |
|------|------|---------|
| `index.js` | 统一导出 + 通用工具函数 | `formatTime`、`extractData`、`formatFileSize`、`debounce`、`throttle`、`deepClone` 等 |
| `request.js` | HTTP 请求封装 | `request`（Axios 实例）、`cancelAllRequests`、`cancelRequestByUrl` |
| `cache.js` | 数据缓存 | `cache`（内存 + sessionStorage 两层缓存） |
| `xss.js` | 安全防护 | `sanitize`、`sanitizeHtml`、`containsXss`、`containsSqlInjection` 等 |
| `media.js` | 媒体文件处理 | `parseMediaList`、`getMediaType`、`getFileInfo`、`downloadMedia` 等 |
| `chartConfig.js` | ECharts 图表配置 | `createBarSeries`、`createPieSeries`、`createLineSeries`、`createRadarSeries` 等 |
| `logger.js` | 日志输出 | `logger`（开发打印/生产静默）、`logFetchError`、`logUploadError` 等 |
| `adminUtils.js` | 管理后台工具 | `getStatusTag`、`getLevelTag`、`createTagGetter`、表格列配置 |
| `validators.js` | 表单验证规则 | `createPasswordValidator()` |

---

## request.js -- HTTP 请求封装

**核心导出：** `request`（默认导出，Axios 实例）

**基础配置：**

```js
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 60000
})
```

### 请求拦截器

1. **认证头注入：** 如果 config 中没有 Authorization 头，自动从 localStorage 读取 token 并注入 `Bearer <token>`

2. **XSS/SQL 防护：** `config.data` 和 `config.params` 经过 `sanitizeRequestData()` 处理：
   - 检测 XSS 攻击载荷（`<script>`、`onerror`、`javascript:` 等 28 种模式）
   - 检测 SQL 注入特征（`union select`、`OR 1=1`、`--`、`;` 等）
   - 对检测到的恶意内容进行剥离和替换

3. **重复请求取消：** 非 GET/HEAD/OPTIONS 请求自动取消前一次相同请求（通过 Axios CancelToken）

4. **重试计数初始化：** `config.__retryCount = 0`，`config._skipAuthRefresh = config.skipAuthRefresh`

### 响应拦截器（成功）

1. 移除请求的挂起记录
2. 检查 `res.data.code`：非 200 则 reject，返回 `response.data` 纯净数据

### 响应拦截器（错误）

1. **自动重试：** GET/HEAD/OPTIONS 请求在特定错误时自动重试（最多 3 次，指数退避 1s/2s/4s）
   - 可重试状态码：408、429、500、502、503、504
   - 可重试错误类型：ECONNABORTED、timeout、Network Error

2. **403 处理器：** 显示"权限不足"警告

3. **401 处理器（Token 刷新）：**
   - 跳过刷新标记（`skipAuthRefresh`）时直接 reject
   - 否则尝试 POST `/user/refresh-token` 获取新 token
   - 成功：更新 Authorization 头并重试原请求
   - 失败：清空 localStorage，触发 `auth-expired` 事件，显示"登录已过期"

4. **友好错误信息：** 根据 HTTP 状态码显示中文错误消息

| 状态码 | 提示信息 |
|--------|---------|
| 400 | 请求参数有误，请检查输入 |
| 404 | 请求的资源不存在 |
| 408 | 请求超时，请稍后重试 |
| 413 | 上传文件过大，请压缩后重试 |
| 429 | 操作过于频繁，请稍后再试 |
| 500 | 服务器内部错误，请稍后重试 |
| 502 | 网关错误，请稍后重试 |
| 503 | 服务暂时不可用，请稍后重试 |

### 使用方法

```js
// GET 请求
const data = await request.get('/plants', { params: { page: 1 } })

// POST 请求
const res = await request.post('/user/login', { username, password })

// 跳过 token 刷新
await request.post('/user/logout', {}, { skipAuthRefresh: true })

// PUT/PATCH/DELETE 同理
await request.put('/knowledge/1', formData)
await request.delete('/plants/1')
await request.patch('/user/profile', { email: 'new@email.com' })
```

---

## cache.js -- 数据缓存

**核心导出：** `cache`（默认导出）、`createCachedFetcher`、`startCacheCleanup`、`stopCacheCleanup`

**两层缓存机制：**

| 层级 | 存储方式 | 生命周期 | 特点 |
|------|---------|---------|------|
| 内存缓存 | Map | 页面刷新后失效 | 最快，适合临时数据 |
| sessionStorage | 浏览器存储 | 会话级别（关闭标签页后失效） | 持久化但不会跨 session |

**API：**

```js
import cache from '@/utils/cache'

// 写入缓存
cache.set('key', data, ttl, 'session')  // ttl 默认 5 分钟
cache.setWithConfig('key', data, 'plants')  // 使用预设配置

// 读取缓存
const data = cache.get('key', 'session')
const data2 = cache.getWithConfig('key', 'plants')

// 删除/清空
cache.remove('key')
cache.clear()
cache.clearExpired()  // 清理过期项
```

**预设缓存配置：**

| 配置键 | TTL | 存储方式 |
|--------|-----|---------|
| `plants` | 10 分钟 | sessionStorage |
| `knowledge` | 10 分钟 | sessionStorage |
| `inheritors` | 10 分钟 | sessionStorage |
| `resources` | 5 分钟 | sessionStorage |
| `categories` | 30 分钟 | sessionStorage |
| `quizQuestions` | 5 分钟 | 内存 |
| `leaderboard` | 2 分钟 | 内存 |
| `userInfo` | 30 分钟 | sessionStorage |

**缓存化的请求函数：**

```js
import { createCachedFetcher } from '@/utils/cache'

const fetchPlants = async (page) => {
  const res = await request.get('/plants', { params: { page } })
  return res.data
}

const cachedFetchPlants = createCachedFetcher(fetchPlants, 'plants')
// 调用 cachedFetchPlants(1) 会自动使用缓存
```

**自动清理：** `startCacheCleanup()` 在模块加载时自动调用，每 60 秒清理一次过期缓存。

---

## xss.js -- 安全防护

**核心导出：**

| 函数 | 说明 |
|------|------|
| `sanitize(input)` | HTML 实体编码（`<` -> `&lt;` 等 6 种） |
| `sanitizeHtml(input)` | 清除 HTML 中的危险标签和属性（`<script>`、`onerror` 等） |
| `containsXss(input)` | 检测是否包含 XSS 攻击载荷（28 种正则模式） |
| `containsSqlInjection(input)` | 检测是否包含 SQL 注入特征（`union select`、`OR 1=1` 等 5 种模式） |
| `stripHtmlTags(input)` | 移除所有 HTML 标签 |
| `escapeJavaScript(input)` | JS 字符串转义 |
| `sanitizeUrl(url)` | URL 安全检查（阻止 `javascript:`/`data:`/`vbscript:` 协议） |
| `sanitizeFileName(fileName)` | 文件名安全处理（移除非法字符，限制长度 255） |
| `sanitizeForLog(input)` | 日志脱敏（截断 1000 字符，转义控制字符） |
| `isSafeInput(input, maxLength)` | 输入安全性快速检查 |
| `validateInput(input, options)` | 完整输入验证（长度限制、XSS 检查、SQL 注入检查） |
| `sanitizeObject(obj)` | 递归清理对象中所有字符串字段 |

**XSS 检测覆盖 28 种攻击模式：** `<script>`、`javascript:`、`vbscript:`、`onerror=`、`eval()`、`expression()`、`<iframe>`、`<object>`、`<embed>`、`<style>`、`<svg>`、`<math>`、`data:`、`srcdoc=`、`xlink:href=` 等。

---

## media.js -- 媒体文件处理

**核心导出：**

**基础类型判断：**
- `getMediaType(url)` -- 根据扩展名返回 `'video'` / `'image'` / `'document'`
- `getMediaTypeByExt(ext)` -- 根据扩展名判断类型
- `getExtensionsByType(type)` -- 获取某类型的所有扩展名

**文件信息：**
- `getFileType(ext)` -- 获取文档子类型（pdf/word/excel/ppt/txt）
- `getFileTypeDisplay(ext)` -- 获取文件类型显示名
- `getFileIcon(type)` -- 获取文件类型对应的 Element Plus 图标组件
- `getFileColor(type)` -- 获取文件类型对应颜色
- `getFileName(url)` -- 从 URL 中提取文件名
- `getFileExt(url)` -- 提取文件扩展名

**媒体列表处理：**
- `parseMediaList(data)` -- 解析 JSON 字符串或逗号分隔列表为标准化文件对象数组
- `stringifyMediaList(files)` -- 序列化文件列表为 JSON 字符串
- `separateMediaByType(files)` -- 将混合文件列表按类型分为 images/videos/documents 三组

**资源文件处理：**
- `getResourceUrl(path)` -- 规范化资源 URL 路径
- `parseResourceFiles(data)` -- 解析资源文件数据
- `stringifyResourceFiles(files)` -- 序列化资源文件

**下载功能：**
- `downloadMedia(url, filename)` -- 触发媒体文件下载
- `downloadDocument(url, filename)` -- 触发文档下载

**支持的文件类型：**

| 类型 | 扩展名 |
|------|--------|
| 视频 | mp4, avi, mov, wmv, flv, mkv |
| 图片 | jpg, jpeg, png, gif, bmp, webp, svg |
| 文档 | pdf, doc, docx, xls, xlsx, ppt, pptx, txt |

**常量和图标映射：**

```js
FILE_ICONS = { pdf: DocumentCopy, doc: Document, xls: Tickets, ppt: DataBoard, txt: Document, ... }
FILE_COLORS = { pdf: '#e74c3c', doc: '#3498db', xls: '#27ae60', ppt: '#e67e22', txt: '#95a5a6' }
FILE_TYPE_NAMES = { pdf: 'PDF', doc: 'Word', xls: 'Excel', ppt: 'PPT', txt: 'TXT', ... }
```

---

## chartConfig.js -- ECharts 图表配置

提供 ECharts 图表配置的快捷工厂函数，统一项目内图表样式。

**常量：**

```js
COLOR_PALETTE = ['#1A5276', '#28B463', '#D4AC0D', '#8E44AD', '#E74C3C', '#16A085', '#2980B9', '#CA6F1E']
```

**工厂函数：**

| 函数 | 生成内容 | 参数 |
|------|---------|------|
| `createLinearGradient(startColor, endColor)` | 线性渐变对象（上到下） | 起止颜色 |
| `createBarSeries(data, gradient, barWidth)` | 柱状图 series 配置 | 数据、渐变色、柱宽 |
| `createMultiColorBarSeries(data, barWidth)` | 多色柱状图（自动从调色板取色） | 数据、柱宽 |
| `createLineSeries(data, gradient, withArea)` | 折线图 series 配置（含面积渐变） | 数据、渐变色、是否显示面积 |
| `createPieSeries(data)` | 饼图 series 配置（环形图） | 数据数组 |
| `createRadarSeries(values, gradient)` | 雷达图 series 配置 | 数据值、渐变色 |

**基础配置常量：**

| 常量 | 用途 |
|------|------|
| `baseTooltip` | 统一样式的 tooltip 配置 |
| `baseGrid` | 统一的 grid 布局配置 |
| `baseXAxis` | 统一的 X 轴样式 |
| `baseYAxis` | 统一的 Y 轴样式 |

---

## index.js -- 统一导出 + 通用函数

### 数据格式化

| 函数 | 说明 |
|------|------|
| `formatTime(time, options?)` | 时间格式化。支持相对时间（3 分钟前/2 小时前/3 天前）和绝对时间（date/time/datetime/full） |
| `formatFileSize(bytes)` | 文件大小格式化 -> "1.5 MB" |
| `truncate(str, length, suffix)` | 文本截断 -> "很长很长..." |

### 数据提取

| 函数 | 说明 |
|------|------|
| `extractData(res)` | 从 API 响应中提取数据数组。自动适配 7 种响应结构：`res.data`、`res.records`、`res.data.data`、`res.data.data.records` 等 |
| `extractPageData(res)` | 从 API 响应中提取分页数据 `{ records, total, page, size }` |

### 通用工具

| 函数 | 说明 |
|------|------|
| `getRankClass(index)` | 排名样式类（gold/silver/bronze） |
| `getScoreLevel(score)` | 分数等级（excellent/good/pass/fail） |
| `getScoreEmoji(score)` | 分数对应表情 |
| `getScoreText(score)` | 分数对应鼓励语 |
| `getImageUrl(path)` | 图片 URL 规范化（自动添加域名前缀，缺失时返回占位图） |
| `getFirstImage(images)` | 从图片列表（JSON 字符串或数组）中提取第一张 |
| `debounce(func, wait)` | 防抖函数 |
| `throttle(func, limit)` | 节流函数 |
| `deepClone(obj)` | 深拷贝 |
| `isEmpty(value)` / `isNotEmpty(value)` | 空值判断 |
| `generateId()` | 生成唯一 ID（时间戳 base36 + 随机数） |
| `sleep(ms)` | Promise 延迟函数 |
| `retry(func, retries, delay)` | 异步函数重试 |

### 常量

```js
PLACEHOLDER_IMG = "/static/defaults/default-plant.svg"
DEFAULT_AVATAR = "/static/defaults/default-avatar.svg"
DEFAULT_VIDEO_COVER = "/static/defaults/default-video-cover.svg"
DEFAULT_DOCUMENT = "/static/defaults/default-document.svg"
```

### media.js 再导出

`index.js` 通过 `export {} from './media'` 重新导出 `media.js` 的全部函数，统一入口。

---

## logger.js -- 日志输出

**核心导出：** `logger`（默认导出）、8 个专用日志函数

**logger 对象：**

```js
const logger = {
  log: (...args) => isDev && console.log('[LOG]', ...args),
  warn: (...args) => console.warn('[WARN]', ...args),
  error: (...args) => console.error('[ERROR]', ...args),
  debug: (...args) => isDev && console.debug('[DEBUG]', ...args),
  info: (...args) => isDev && console.info('[INFO]', ...args)
}
```

开发环境 (`import.meta.env.DEV`) 打印日志，生产环境自动静默（warn/error 例外）。

**专用日志函数：**

| 函数 | 说明 |
|------|------|
| `logUploadError(type, error)` | 上传失败日志 |
| `logDeleteWarn(type, path)` | 文件删除失败警告 |
| `logAuthWarn(msg)` | 权限不足提示 |
| `logSecurityWarn(key, value)` | XSS/SQL 注入检测告警 |
| `logFetchError(context, error)` | 数据获取失败日志 |
| `logOperationWarn(operation)` | 操作失败日志 |
| `logAutoPlayWarn(e)` | 自动播放失败日志 |
| `logPermissionWarn(resource)` | 资源加载权限不足日志 |

---

## adminUtils.js -- 管理后台工具

提供管理后台表格列配置、状态标签映射等工具函数。

**状态映射：**

- `STATUS_MAPS.feedback`：`pending -> { tag: 'warning', text: '待处理' }` 等
- `STATUS_MAPS.difficulty`：`easy -> { tag: 'success', text: '入门' }` 等
- `STATUS_MAPS.fileType`：`video -> { tag: 'primary', text: '视频' }` 等

**等级标签映射：**

```js
LEVEL_TAG_MAP = { "省级": "warning", "自治区级": "success", "州级": "primary", "市级": "primary" }
```

**工具函数：**

- `createTagGetter(mapName, field)` -- 创建标签获取函数工厂
- `getStatusTag(status, statusMap)` -- 获取状态对应的 Element Plus tag type
- `getLevelTag(level)` -- 获取传承人等级对应的 tag type

---

## validators.js -- 表单验证规则

**核心导出：** `createPasswordValidator(options?)`

**功能：** 共享的密码验证规则工厂，避免 `App.vue` 和 `usePersonalCenter.js` 中的重复验证逻辑。

**参数：** `{ minLength: 8, maxLength: 50 }`

**返回：**

```js
{
  password: [
    { required: true, message: '请输入密码' },
    { min: 8, max: 50, message: '密码长度为8-50位' },
    { validator: (_, value, callback) => {
        if (!/[a-zA-Z]/.test(value)) callback(new Error('密码必须包含字母'))
        else if (!/[0-9]/.test(value)) callback(new Error('密码必须包含数字'))
        else if (/\s/.test(value)) callback(new Error('密码不能包含空格'))
        else callback()
      }
    }
  ],
  confirmPassword: (formRef, fieldName = 'newPassword') => [
    { required: true, message: '请确认密码' },
    { validator: (_, value, callback) => {
        if (formRef.value && value !== formRef.value[fieldName])
          callback(new Error('两次输入的密码不一致'))
        else callback()
      }
    }
  ]
}
```

**使用示例：**

```js
const { password: passwordRules, confirmPassword: confirmPasswordRules } = createPasswordValidator()

const rules = {
  password: passwordRules,
  confirmPassword: confirmPasswordRules(formRef, 'password')
}
```
