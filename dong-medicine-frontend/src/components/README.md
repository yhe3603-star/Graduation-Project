# Components 组件目录

本目录包含项目的所有可复用组件。

## 目录结构

```
components/
├── base/                     # 基础组件
│   ├── ErrorBoundary.vue     # 错误边界
│   ├── VirtualList.vue       # 虚拟列表
│   └── index.js
│
├── common/                   # 通用组件
│   └── SkeletonGrid.vue      # 骨架屏网格
│
├── business/                 # 业务组件
│   ├── layout/               # 布局组件
│   │   ├── AppHeader.vue     # 应用头部
│   │   └── AppFooter.vue     # 应用底部
│   │
│   ├── display/              # 展示组件
│   │   ├── AiChatCard.vue    # AI对话卡片
│   │   ├── CardGrid.vue      # 卡片网格
│   │   ├── ChartCard.vue     # 图表卡片
│   │   ├── PageSidebar.vue   # 页面侧边栏
│   │   ├── Pagination.vue    # 分页组件
│   │   ├── SearchFilter.vue  # 搜索过滤
│   │   ├── UpdateLogCard.vue # 更新日志卡片
│   │   ├── UpdateLogDialog.vue
│   │   └── index.js
│   │
│   ├── interact/             # 交互组件
│   │   ├── CaptchaInput.vue  # 验证码输入
│   │   ├── CommentSection.vue# 评论组件
│   │   ├── InteractSidebar.vue
│   │   ├── PlantGame.vue     # 植物游戏
│   │   ├── QuizSection.vue   # 答题组件
│   │   └── index.js
│   │
│   ├── media/                # 媒体组件
│   │   ├── DocumentList.vue  # 文档列表
│   │   ├── DocumentPreview.vue
│   │   ├── ImageCarousel.vue # 图片轮播
│   │   ├── MediaDisplay.vue  # 媒体展示
│   │   ├── VideoPlayer.vue   # 视频播放
│   │   └── index.js
│   │
│   ├── upload/               # 上传组件
│   │   ├── DocumentUploader.vue
│   │   ├── FileUploader.vue  # 通用上传
│   │   ├── ImageUploader.vue # 图片上传
│   │   ├── VideoUploader.vue # 视频上传
│   │   └── index.js
│   │
│   ├── dialogs/              # 详情对话框
│   │   ├── InheritorDetailDialog.vue
│   │   ├── KnowledgeDetailDialog.vue
│   │   ├── PlantDetailDialog.vue
│   │   ├── QuizDetailDialog.vue
│   │   └── ResourceDetailDialog.vue
│   │
│   └── admin/                # 管理后台组件
│       ├── AdminDashboard.vue
│       ├── AdminDataTable.vue
│       ├── AdminSidebar.vue
│       ├── dialogs/          # 管理对话框
│       └── forms/            # 管理表单
│
└── README.md
```

## 组件分类说明

### base/ - 基础组件

| 组件 | 用途 |
|------|------|
| ErrorBoundary.vue | 错误边界，捕获子组件错误 |
| VirtualList.vue | 虚拟列表，优化大数据列表性能 |

### common/ - 通用组件

| 组件 | 用途 |
|------|------|
| SkeletonGrid.vue | 骨架屏，数据加载时显示占位符 |

### business/layout/ - 布局组件

| 组件 | 用途 |
|------|------|
| AppHeader.vue | 应用头部，导航栏、登录状态、搜索入口 |
| AppFooter.vue | 应用底部，版权信息、友情链接 |

### business/display/ - 展示组件

| 组件 | 用途 |
|------|------|
| AiChatCard.vue | AI对话卡片，集成智能问答 |
| CardGrid.vue | 卡片网格，通用卡片列表展示 |
| ChartCard.vue | 图表卡片，封装ECharts图表 |
| PageSidebar.vue | 页面侧边栏，展示统计和热门内容 |
| Pagination.vue | 分页组件 |
| SearchFilter.vue | 搜索过滤组件 |
| UpdateLogCard.vue | 更新日志卡片 |
| UpdateLogDialog.vue | 更新日志对话框 |

### business/interact/ - 交互组件

| 组件 | 用途 |
|------|------|
| CaptchaInput.vue | 验证码输入组件 |
| CommentSection.vue | 评论组件，支持发表评论、回复 |
| InteractSidebar.vue | 互动侧边栏，展示排行榜和统计 |
| PlantGame.vue | 植物识别游戏 |
| QuizSection.vue | 趣味答题组件 |

### business/media/ - 媒体组件

| 组件 | 用途 |
|------|------|
| DocumentList.vue | 文档列表组件 |
| DocumentPreview.vue | 文档预览组件 |
| ImageCarousel.vue | 图片轮播组件 |
| MediaDisplay.vue | 媒体展示组件 |
| VideoPlayer.vue | 视频播放组件 |

### business/upload/ - 上传组件

| 组件 | 用途 |
|------|------|
| DocumentUploader.vue | 文档上传组件 |
| FileUploader.vue | 通用文件上传组件 |
| ImageUploader.vue | 图片上传组件 |
| VideoUploader.vue | 视频上传组件 |

### business/dialogs/ - 详情对话框

| 组件 | 用途 |
|------|------|
| PlantDetailDialog.vue | 植物详情对话框 |
| KnowledgeDetailDialog.vue | 知识详情对话框 |
| InheritorDetailDialog.vue | 传承人详情对话框 |
| ResourceDetailDialog.vue | 资源详情对话框 |
| QuizDetailDialog.vue | 答题详情对话框 |

### business/admin/ - 管理后台组件

| 组件 | 用途 |
|------|------|
| AdminDashboard.vue | 管理仪表盘 |
| AdminDataTable.vue | 数据表格 |
| AdminSidebar.vue | 管理侧边栏 |

---

## 开发规范

1. **命名规范**: 组件使用大驼峰命名法
2. **单文件组件**: 每个组件一个 `.vue` 文件
3. **Props验证**: 使用 `defineProps` 定义并验证 props
4. **事件命名**: 使用小驼峰命名法，如 `onItemClick`
5. **样式作用域**: 使用 `<style scoped>` 避免样式污染

---

**最后更新时间**: 2026年3月27日
