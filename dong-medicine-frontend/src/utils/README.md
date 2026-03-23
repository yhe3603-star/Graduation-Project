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

### 1. adminUtils.js

**功能**：管理后台相关的工具函数。

**主要方法**：
- `formatAdminData`：格式化管理后台数据
- `validateAdminForm`：验证管理后台表单
- `generateAdminFilters`：生成管理后台筛选条件
- `handleAdminExport`：处理管理后台数据导出

**使用场景**：管理后台页面的数据处理和表单验证。

### 2. cache.js

**功能**：缓存管理工具。

**主要方法**：
- `setCache`：设置缓存
- `getCache`：获取缓存
- `removeCache`：移除缓存
- `clearCache`：清除所有缓存
- `setSessionCache`：设置会话缓存
- `getSessionCache`：获取会话缓存

**参数**：
- `key`：缓存键名
- `value`：缓存值
- `expire`：过期时间（毫秒）

**使用场景**：缓存API响应、用户设置等数据，提高性能。

### 3. index.js

**功能**：统一导出所有工具函数。

**使用方法**：

```javascript
import { request, cache, logger } from '@/utils'
```

### 4. logger.js

**功能**：日志工具，提供统一的日志记录功能。

**主要方法**：
- `log`：普通日志
- `info`：信息日志
- `warn`：警告日志
- `error`：错误日志
- `debug`：调试日志

**使用场景**：记录应用运行状态、错误信息等。

### 5. media.js

**功能**：媒体文件处理工具。

**主要方法**：
- `getImageUrl`：获取图片URL
- `getVideoUrl`：获取视频URL
- `getDocumentUrl`：获取文档URL
- `validateImage`：验证图片文件
- `validateVideo`：验证视频文件
- `validateDocument`：验证文档文件
- `getMediaType`：获取媒体类型
- `formatFileSize`：格式化文件大小

**使用场景**：处理媒体文件的上传、验证和显示。

### 6. request.js

**功能**：网络请求工具，封装了axios。

**主要方法**：
- `get`：GET请求
- `post`：POST请求
- `put`：PUT请求
- `delete`：DELETE请求
- `upload`：文件上传

**配置**：
- 基础URL设置
- 请求超时设置
- 请求/响应拦截器
- 错误处理

**使用场景**：与后端API进行通信。

**使用示例**：

```javascript
import { request } from '@/utils'

// GET请求
const response = await request.get('/api/plants', {
  params: { page: 1, size: 10 }
})

// POST请求
const response = await request.post('/api/auth/login', {
  username: 'admin',
  password: 'password'
})

// 文件上传
const formData = new FormData()
formData.append('file', file)
const response = await request.upload('/api/upload', formData)
```

### 7. xss.js

**功能**：XSS（跨站脚本）防护工具。

**主要方法**：
- `filterXss`：过滤XSS攻击代码
- `sanitizeHtml`：清理HTML内容
- `validateInput`：验证输入内容

**使用场景**：防止用户输入的内容包含恶意脚本。

**使用示例**：

```javascript
import { xss } from '@/utils'

// 过滤用户输入
const safeContent = xss.filterXss(userInput)

// 清理HTML内容
const sanitizedHtml = xss.sanitizeHtml(htmlContent)
```

## 工具函数使用指南

### 导入方式

```javascript
// 方法1：导入整个工具模块
import * as utils from '@/utils'

// 方法2：导入指定工具
import { request, cache } from '@/utils'

// 方法3：导入单个工具
import request from '@/utils/request'
```

### 错误处理

```javascript
import { request, logger } from '@/utils'

try {
  const response = await request.get('/api/data')
  return response.data
} catch (error) {
  logger.error('API请求失败:', error)
  throw error
}
```

### 缓存使用

```javascript
import { cache } from '@/utils'

// 设置缓存，1小时过期
cache.setCache('userInfo', userData, 60 * 60 * 1000)

// 获取缓存
const userInfo = cache.getCache('userInfo')

// 移除缓存
cache.removeCache('userInfo')

// 清除所有缓存
cache.clearCache()
```

## 开发规范

1. **命名规范**：函数名使用camelCase，文件名使用小写字母，单词间用连字符分隔
2. **功能单一**：每个工具函数应该只负责一个功能
3. **参数验证**：对函数参数进行适当的验证
4. **错误处理**：包含适当的错误处理逻辑
5. **文档说明**：为每个函数添加详细的注释说明
6. **性能优化**：考虑函数的性能，避免不必要的计算

## 注意事项

- 工具函数应该是纯函数，避免修改全局状态
- 对于异步操作，应该返回Promise
- 避免在工具函数中直接操作DOM
- 合理使用缓存，避免内存泄漏
- 定期检查和更新工具函数，保持代码的可维护性

---

**最后更新时间**：2026年3月23日