# 业务组件目录

## 职责范围

业务组件包含特定业务逻辑，按功能模块分类组织：

1. **业务相关性**：包含特定业务领域的逻辑
2. **可组合性**：可组合多个基础组件构建复杂功能
3. **状态管理**：可访问全局状态和API服务
4. **交互性**：处理用户交互和数据流转

## 目录结构

```
business/
├── display/      # 展示组件
├── interact/     # 交互组件
├── media/        # 媒体组件
├── upload/       # 上传组件
└── index.js      # 统一导出
```

## 组件分类

### 展示组件 (display/)
用于数据展示和内容呈现的组件：

| 组件名 | 功能描述 |
|--------|----------|
| MediaDisplay | 媒体展示组件 |
| CardGrid | 卡片网格组件 |
| ChartCard | 图表卡片组件 |
| SearchFilter | 搜索过滤组件 |
| PageSidebar | 页面侧边栏 |
| AiChatCard | AI对话卡片 |
| UpdateLogCard | 更新日志卡片 |

### 交互组件 (interact/)
用于用户交互和游戏功能的组件：

| 组件名 | 功能描述 |
|--------|----------|
| CommentSection | 评论组件 |
| PlantGame | 植物识别游戏 |
| QuizSection | 趣味答题组件 |

### 媒体组件 (media/)
用于媒体文件预览和展示的组件：

| 组件名 | 功能描述 |
|--------|----------|
| ImageCarousel | 图片轮播组件 |
| VideoPlayer | 视频播放组件 |
| DocumentPreview | 文档预览组件 |
| DocumentList | 文档列表组件 |

### 上传组件 (upload/)
用于文件上传的组件：

| 组件名 | 功能描述 |
|--------|----------|
| ImageUploader | 图片上传组件 |
| VideoUploader | 视频上传组件 |
| DocumentUploader | 文档上传组件 |
| FileUploader | 通用文件上传组件 |

## 使用规范

### 导入方式

```javascript
// 推荐方式：从index.js导入
import { ImageCarousel, CommentSection } from '@/components/business'

// 或按分类导入
import { ImageCarousel } from '@/components/business/media'
import { CommentSection } from '@/components/business/interact'
```

### 组件开发规范

1. 业务组件可依赖基础组件
2. 可访问Composables和Store
3. 保持组件职责单一
4. 复杂逻辑提取到Composables

## 依赖关系

- 外部依赖：Element Plus、Vue 3
- 内部依赖：基础组件、Composables、Utils
