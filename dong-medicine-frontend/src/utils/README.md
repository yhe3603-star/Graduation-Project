# 工具函数目录 (utils)

本目录存放项目中使用的各种工具函数，提供通用的功能支持。

## 📁 文件列表

| 文件名 | 功能说明 |
|--------|----------|
| `index.js` | 工具函数统一导出入口 |
| `adminUtils.js` | 管理后台工具函数 |
| `cache.js` | 缓存管理工具 |
| `chartConfig.js` | 图表配置工具 |
| `logger.js` | 日志记录工具 |
| `media.js` | 媒体处理工具 |
| `request.js` | HTTP请求封装 |
| `xss.js` | XSS防护工具 |

## 📦 详细说明

### 1. request.js - HTTP请求封装

封装Axios，统一处理API请求和响应。

**主要功能:**
- 请求拦截器：添加Token、处理请求参数
- 响应拦截器：统一处理响应数据、错误处理
- 请求重试机制
- Token刷新处理

**导出内容:**
```javascript
import request from '@/utils/request'

// GET请求
const response = await request.get('/api/plants/list', { params: { page: 1 } })

// POST请求
const response = await request.post('/api/user/login', { username, password })

// PUT请求
const response = await request.put('/api/plants/update', data)

// DELETE请求
const response = await request.delete('/api/plants/1')
```

### 2. adminUtils.js - 管理后台工具函数

提供管理后台通用的数据处理功能。

**主要导出:**
| 函数名 | 功能说明 |
|--------|----------|
| `formatTime` | 格式化时间 |
| `getDifficultyText` | 获取难度文本 |
| `getDifficultyTagType` | 获取难度标签类型 |
| `TABLE_CONFIG` | 表格配置对象 |

**使用示例:**
```javascript
import { formatTime, TABLE_CONFIG } from '@/utils/adminUtils'

const formattedDate = formatTime(new Date())
const plantConfig = TABLE_CONFIG.plants
```

### 3. cache.js - 缓存管理工具

管理浏览器本地缓存。

**主要导出:**
| 函数名 | 功能说明 |
|--------|----------|
| `getCache` | 获取缓存数据 |
| `setCache` | 设置缓存数据 |
| `removeCache` | 删除缓存数据 |
| `clearCache` | 清空所有缓存 |

**使用示例:**
```javascript
import { getCache, setCache } from '@/utils/cache'

// 设置缓存（带过期时间）
setCache('user_token', token, 3600 * 1000) // 1小时后过期

// 获取缓存
const token = getCache('user_token')
```

### 4. chartConfig.js - 图表配置工具

提供ECharts图表的默认配置。

**主要导出:**
| 函数名 | 功能说明 |
|--------|----------|
| `createBarSeries` | 创建柱状图系列 |
| `createLineSeries` | 创建折线图系列 |
| `createPieSeries` | 创建饼图系列 |
| `createRadarSeries` | 创建雷达图系列 |
| `defaultOptions` | 默认图表配置 |

**使用示例:**
```javascript
import { createBarSeries, defaultOptions } from '@/utils/chartConfig'

const series = createBarSeries([10, 20, 30], '数据名称')
const options = {
  ...defaultOptions,
  series: [series]
}
```

### 5. logger.js - 日志记录工具

统一记录不同类型的错误和操作日志。

**主要导出:**
| 函数名 | 功能说明 |
|--------|----------|
| `logUploadError` | 记录上传错误 |
| `logDeleteWarn` | 记录删除警告 |
| `logAuthWarn` | 记录认证警告 |
| `logSecurityWarn` | 记录安全警告 |
| `logFetchError` | 记录请求错误 |
| `logOperationWarn` | 记录操作警告 |
| `logAutoPlayWarn` | 记录自动播放警告 |
| `logPermissionWarn` | 记录权限警告 |

**使用示例:**
```javascript
import { logFetchError, logUploadError } from '@/utils/logger'

try {
  await fetchData()
} catch (error) {
  logFetchError('获取数据', error)
}
```

### 6. media.js - 媒体处理工具

处理各种媒体资源相关的功能。

**主要导出:**
| 函数名 | 功能说明 |
|--------|----------|
| `parseMediaList` | 解析媒体列表 |
| `stringifyMediaList` | 序列化媒体列表 |
| `getMediaType` | 获取媒体类型 |
| `separateMediaByType` | 按类型分离媒体 |
| `downloadMedia` | 下载媒体文件 |
| `getImageUrl` | 获取图片URL |
| `getVideoUrl` | 获取视频URL |

**使用示例:**
```javascript
import { parseMediaList, getMediaType, downloadMedia } from '@/utils/media'

// 解析媒体列表
const mediaList = parseMediaList(jsonString)

// 获取媒体类型
const type = getMediaType('image.jpg') // 'image'

// 下载媒体
downloadMedia(url, 'filename.jpg')
```

### 7. xss.js - XSS防护工具

防止XSS攻击和SQL注入。

**主要导出:**
| 函数名 | 功能说明 |
|--------|----------|
| `sanitize` | 清理XSS攻击代码 |
| `containsXss` | 检测是否包含XSS |
| `containsSqlInjection` | 检测是否包含SQL注入 |
| `sanitizeForLog` | 清理日志中的敏感信息 |

**使用示例:**
```javascript
import { sanitize, containsXss } from '@/utils/xss'

// 清理用户输入
const cleanInput = sanitize(userInput)

// 检测XSS攻击
if (containsXss(userInput)) {
  alert('输入包含非法字符')
}
```

### 8. index.js - 统一导出入口

统一导出所有工具函数，方便使用。

**使用示例:**
```javascript
import { formatTime, getCache, setCache } from '@/utils'
```

## 🎯 使用规范

### 函数命名
- 使用小驼峰命名法
- 函数名要有描述性
- 遵循动词+名词的命名方式

### 函数结构
```javascript
/**
 * 函数说明
 * @param {Type} paramName - 参数说明
 * @returns {Type} 返回值说明
 */
export function functionName(paramName) {
  // 函数逻辑
  return result
}
```

### 最佳实践
1. **纯函数**: 工具函数应该是纯函数，不依赖外部状态
2. **单一职责**: 每个函数只做一件事
3. **参数验证**: 对参数进行类型和范围验证
4. **错误处理**: 合理处理异常情况
5. **注释完善**: 添加JSDoc注释

## 📚 扩展阅读

- [JavaScript 函数式编程](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Guide/Functions)
- [Axios 文档](https://axios-http.com/zh/docs/intro)
