# 工具函数目录说明

## 文件夹结构

本目录包含项目中使用的工具函数，提供各种通用功能。

```
utils/
├── adminUtils.js   # 管理后台工具函数
├── cache.js        # 缓存工具
├── index.js        # 工具函数导出
├── logger.js       # 日志工具
├── media.js        # 媒体处理工具
├── request.js      # 网络请求工具
├── xss.js          # XSS防护工具
└── README.md       # 工具函数说明文档
```

## 详细说明

### 1. request.js - 网络请求工具

**功能**：封装axios，提供统一的HTTP请求处理。

**配置**：
| 配置项 | 值 | 说明 |
|-------|-----|------|
| baseURL | /api | API基础路径 |
| timeout | 60000 | 请求超时时间 |

**请求拦截器功能**：
- 自动添加Authorization头（Bearer Token）
- 自动添加userId头
- 请求数据XSS过滤
- SQL注入检测
- 请求去重（取消重复请求）

**响应拦截器功能**：
- 统一错误处理
- Token自动刷新（401时）
- 请求重试机制
- 错误消息提示

**重试配置**：
```javascript
const RETRY_CONFIG = {
  maxRetries: 3,           // 最大重试次数
  retryDelay: 1000,        // 基础延迟时间
  retryableStatuses: [408, 429, 500, 502, 503, 504],
  retryableMethods: ['get', 'head', 'options']
}
```

**导出方法**：
| 方法 | 说明 |
|------|------|
| `request.get(url, config)` | GET请求 |
| `request.post(url, data, config)` | POST请求 |
| `request.put(url, data, config)` | PUT请求 |
| `request.delete(url, config)` | DELETE请求 |
| `cancelAllRequests()` | 取消所有请求 |
| `cancelRequestByUrl(url)` | 取消指定URL的请求 |

**Token刷新机制**：
- 401响应时自动尝试刷新Token
- 刷新成功后重试原请求
- 刷新失败后清除认证信息并提示重新登录

**使用示例**：
```javascript
import request from '@/utils/request'

// GET请求
const response = await request.get('/plants/list', {
  params: { page: 1, size: 10 }
})

// POST请求
const result = await request.post('/user/login', {
  username: 'admin',
  password: 'password'
})
```

### 2. adminUtils.js - 管理后台工具函数

**功能**：管理后台相关的工具函数和配置。

**配置对象**：

**TABLE_CONFIGS**：表格配置对象
```javascript
{
  users: {
    title: '用户',
    showTitle: false,
    columns: [
      { prop: 'username', label: '用户名', minWidth: 120 },
      { prop: 'role', label: '角色', type: 'tag' },
      { prop: 'status', label: '状态', slotName: 'status', width: 80 },
      { prop: 'createdAt', label: '创建时间', width: 160 }
    ],
    showAdd: false,
    showEdit: false,
    actionWidth: 250
  },
  // ... 其他表格配置
}
```

**menuTitles**：菜单标题映射
```javascript
{
  dashboard: '仪表盘',
  users: '用户管理',
  knowledge: '知识管理',
  inheritors: '传承人管理',
  plants: '植物管理',
  qa: '问答管理',
  resources: '资源管理',
  quiz: '答题管理',
  comments: '评论管理',
  feedback: '反馈管理',
  logs: '日志管理'
}
```

**工具函数**：
| 函数 | 参数 | 返回值 | 说明 |
|------|------|-------|------|
| `getLogModuleTagType(module)` | module: String | String | 获取日志模块标签类型 |
| `getLogTypeTagType(type)` | type: String | String | 获取日志类型标签类型 |
| `formatLogTime(time)` | time: String | String | 格式化日志时间 |
| `formatFileSize(bytes)` | bytes: Number | String | 格式化文件大小 |
| `getFileTypeTagType(type)` | type: String | String | 获取文件类型标签样式 |
| `getFileTypeText(type)` | type: String | String | 获取文件类型显示文本 |
| `getCorrectAnswerContent(quiz)` | quiz: Object | String | 获取正确答案内容 |

### 3. media.js - 媒体处理工具

**功能**：处理媒体文件相关的工具函数。

**主要函数**：
| 函数 | 参数 | 返回值 | 说明 |
|------|------|-------|------|
| `parseMediaList(mediaStr)` | mediaStr: String | Array | 解析媒体列表JSON字符串 |
| `getMediaType(filename)` | filename: String | String | 根据文件名获取媒体类型 |
| `formatFileSize(bytes)` | bytes: Number | String | 格式化文件大小 |
| `getImageUrl(path)` | path: String | String | 获取图片完整URL |
| `getVideoUrl(path)` | path: String | String | 获取视频完整URL |
| `getDocumentUrl(path)` | path: String | String | 获取文档完整URL |

**媒体类型判断**：
```javascript
const IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
const VIDEO_EXTENSIONS = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv']
const DOCUMENT_EXTENSIONS = ['docx', 'doc', 'pdf', 'pptx', 'ppt', 'xlsx', 'xls', 'txt']
```

### 4. cache.js - 缓存工具

**功能**：缓存管理工具，支持设置过期时间。

**主要方法**：
| 方法 | 参数 | 返回值 | 说明 |
|------|------|-------|------|
| `setCache(key, value, expire)` | key, value, expire(ms) | void | 设置缓存（支持过期时间） |
| `getCache(key)` | key | any | 获取缓存 |
| `removeCache(key)` | key | void | 移除缓存 |
| `clearCache()` | - | void | 清除所有缓存 |
| `setSessionCache(key, value)` | key, value | void | 设置会话缓存 |
| `getSessionCache(key)` | key | any | 获取会话缓存 |

### 5. logger.js - 日志工具

**功能**：提供统一的日志记录功能。

**主要方法**：
| 方法 | 说明 |
|------|------|
| `log(...args)` | 普通日志 |
| `logInfo(...args)` | 信息日志 |
| `logWarn(...args)` | 警告日志 |
| `logError(...args)` | 错误日志 |
| `logDebug(...args)` | 调试日志 |
| `logAuthWarn(...args)` | 认证警告日志 |
| `logSecurityWarn(...args)` | 安全警告日志 |

### 6. xss.js - XSS防护工具

**功能**：防止XSS（跨站脚本）攻击和SQL注入。

**主要方法**：
| 方法 | 参数 | 返回值 | 说明 |
|------|------|-------|------|
| `sanitize(input)` | input: String | String | 对输入进行HTML转义 |
| `containsXss(input)` | input: String | Boolean | 检测是否包含XSS攻击代码 |
| `containsSqlInjection(input)` | input: String | Boolean | 检测是否包含SQL注入 |
| `sanitizeForLog(input)` | input: String | String | 清理日志中的特殊字符 |

**检测的危险模式**：
- `<script>`标签
- `javascript:`协议
- 事件处理器（onclick, onerror等）
- `eval()`函数
- SQL注入关键字

### 7. index.js - 工具函数导出

**主要导出**：
| 导出项 | 说明 |
|-------|------|
| `request` | axios请求实例 |
| `extractPageData(res)` | 提取分页数据 |
| `extractData(res)` | 提取普通数据 |
| `logFetchError(context, error)` | 记录获取错误 |
| `logOperationWarn(...args)` | 记录操作警告 |

**使用方法**：
```javascript
import { request, extractPageData, logFetchError } from '@/utils'

const response = await request.get('/api/plants')
const { records, total } = extractPageData(response)
```

## 工具函数统计

| 文件 | 导出数量 | 主要用途 |
|------|---------|---------|
| request.js | 2 | 网络请求 |
| adminUtils.js | 12 | 管理后台配置和工具 |
| media.js | 6 | 媒体处理 |
| cache.js | 6 | 缓存管理 |
| logger.js | 7 | 日志记录 |
| xss.js | 4 | XSS防护 |
| index.js | 4 | 数据提取和日志 |
| **总计** | **41** | - |

## 开发规范

1. **命名规范**：函数名使用camelCase，文件名使用小写字母
2. **功能单一**：每个工具函数应该只负责一个功能
3. **参数验证**：对函数参数进行适当的验证
4. **错误处理**：包含适当的错误处理逻辑
5. **文档说明**：为每个函数添加详细的注释说明

---

**最后更新时间**：2026年3月25日
