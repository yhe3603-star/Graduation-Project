# 工具函数目录

## 职责范围

工具函数提供通用的、无状态的辅助功能：

1. **数据处理**：格式化、转换、验证等
2. **媒体处理**：文件类型判断、URL处理等
3. **通用工具**：防抖、节流、深拷贝等
4. **常量定义**：全局常量和配置

## 文件列表

| 文件名 | 功能描述 |
|--------|----------|
| index.js | 工具函数统一导出 |
| media.js | 媒体处理工具函数 |
| logger.js | 日志工具函数 |
| request.js | HTTP请求封装 |

## 功能分类

### 数据处理

| 函数名 | 功能描述 |
|--------|----------|
| formatTime | 时间格式化，支持相对时间 |
| formatFileSize | 文件大小格式化 |
| extractData | API响应数据提取 |
| truncate | 文本截断 |
| deepClone | 深拷贝对象 |
| isEmpty/isNotEmpty | 空值判断 |

### 媒体处理

| 函数名 | 功能描述 |
|--------|----------|
| parseMediaList | 解析媒体列表数据 |
| stringifyMediaList | 序列化媒体列表 |
| getMediaType | 获取媒体类型 |
| separateMediaByType | 按类型分离媒体 |
| downloadMedia | 触发媒体下载 |
| getFileType | 获取文件类型 |
| getFileIcon | 获取文件图标组件 |
| getFileColor | 获取文件颜色 |

### 通用工具

| 函数名 | 功能描述 |
|--------|----------|
| debounce | 防抖函数 |
| throttle | 节流函数 |
| generateId | 生成唯一ID |
| sleep | 延迟函数 |
| retry | 重试函数 |

### 常量

| 常量名 | 值 | 用途 |
|--------|-----|------|
| PLACEHOLDER_IMG | /static/defaults/default-plant.svg | 占位图片 |
| DEFAULT_AVATAR | /static/defaults/default-avatar.svg | 默认头像 |
| DEFAULT_VIDEO_COVER | /static/defaults/default-video-cover.svg | 默认视频封面 |
| DEFAULT_DOCUMENT | /static/defaults/default-document.svg | 默认文档图标 |

## 使用规范

### 导入方式

```javascript
// 推荐方式：从index.js导入
import { formatTime, parseMediaList, debounce } from '@/utils'

// 或直接导入
import { formatTime } from '@/utils'
```

### 使用示例

```javascript
import { formatTime, extractData, debounce } from '@/utils'

// 时间格式化
const timeStr = formatTime(new Date()) // "刚刚"
const dateStr = formatTime(date, { format: 'date' }) // "2024/1/1"

// 响应数据提取
const list = extractData(response)

// 防抖
const handleSearch = debounce((keyword) => {
  // 搜索逻辑
}, 300)
```

## 开发规范

### 函数注释模板

```javascript
/**
 * 函数功能描述
 * @param {Type} paramName - 参数说明
 * @returns {Type} 返回值说明
 * @example
 * functionName(arg) // 返回结果
 */
export function functionName(paramName) {
  // 实现
}
```

### 注意事项

1. 工具函数应保持纯函数特性，无副作用
2. 复杂函数需要提供使用示例
3. 边界情况需要处理（空值、异常等）
4. 保持函数的单一职责

## 依赖关系

- 外部依赖：无或最小依赖
- 内部依赖：无（工具函数不应依赖其他内部模块）
- 被依赖：所有模块
