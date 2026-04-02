# 组件目录 (components)

本目录存放所有可复用的Vue组件，按照功能和用途进行分类组织。

## 📁 目录结构

```
components/
├── base/              # 基础组件
├── business/          # 业务组件
│   ├── admin/         # 管理后台组件
│   │   ├── dialogs/   # 后台详情对话框
│   │   └── forms/     # 后台表单对话框
│   ├── dialogs/       # 前台详情对话框
│   ├── display/       # 展示组件
│   ├── interact/      # 交互组件
│   ├── layout/        # 布局组件
│   ├── media/         # 媒体组件
│   └── upload/        # 上传组件
└── common/            # 通用组件
```

## 📦 组件分类说明

### 1. base/ - 基础组件

基础组件是最底层的组件，不包含业务逻辑，可跨项目复用。

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `ErrorBoundary.vue` | 错误边界组件 | 捕获子组件错误，防止整个应用崩溃 |
| `VirtualList.vue` | 虚拟滚动列表 | 处理大量数据的性能优化渲染 |

**使用示例:**
```vue
<template>
  <ErrorBoundary>
    <YourComponent />
  </ErrorBoundary>
</template>
```

### 2. business/ - 业务组件

业务组件包含特定业务逻辑，与项目功能紧密相关。

#### 2.1 admin/ - 管理后台组件

管理后台专用的组件，用于后台数据管理。

**dialogs/ - 详情对话框**
| 文件名 | 功能说明 |
|--------|----------|
| `CommentDetailDialog.vue` | 评论详情查看 |
| `FeedbackDetailDialog.vue` | 反馈详情查看 |
| `InheritorDetailDialog.vue` | 传承人详情查看 |
| `KnowledgeDetailDialog.vue` | 知识详情查看 |
| `LogDetailDialog.vue` | 操作日志详情查看 |
| `PlantDetailDialog.vue` | 植物详情查看 |
| `QaDetailDialog.vue` | 问答详情查看 |
| `QuizDetailDialog.vue` | 答题详情查看 |
| `ResourceDetailDialog.vue` | 资源详情查看 |
| `UserDetailDialog.vue` | 用户详情查看 |

**forms/ - 表单对话框**
| 文件名 | 功能说明 |
|--------|----------|
| `InheritorFormDialog.vue` | 传承人信息编辑表单 |
| `KnowledgeFormDialog.vue` | 知识信息编辑表单 |
| `PlantFormDialog.vue` | 植物信息编辑表单 |
| `QaFormDialog.vue` | 问答信息编辑表单 |
| `QuizFormDialog.vue` | 答题题目编辑表单 |
| `ResourceFormDialog.vue` | 资源信息编辑表单 |

**其他组件:**
| 文件名 | 功能说明 |
|--------|----------|
| `AdminDashboard.vue` | 管理员控制台面板 |
| `AdminDataTable.vue` | 通用数据表格组件 |
| `AdminSidebar.vue` | 管理后台侧边栏导航 |

#### 2.2 dialogs/ - 前台详情对话框

前台用户查看详情的对话框组件。

| 文件名 | 功能说明 |
|--------|----------|
| `InheritorDetailDialog.vue` | 传承人详情弹窗 |
| `KnowledgeDetailDialog.vue` | 知识详情弹窗 |
| `PlantDetailDialog.vue` | 植物详情弹窗 |
| `QuizDetailDialog.vue` | 答题详情弹窗 |
| `ResourceDetailDialog.vue` | 资源详情弹窗 |

#### 2.3 display/ - 展示组件

用于展示内容和数据的组件。

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `AiChatCard.vue` | AI聊天卡片 | AI智能助手对话界面 |
| `CardGrid.vue` | 卡片网格布局 | 以网格形式展示卡片列表 |
| `ChartCard.vue` | 图表卡片 | 数据可视化图表容器 |
| `PageSidebar.vue` | 页面侧边栏 | 页面辅助导航和信息展示 |
| `Pagination.vue` | 分页组件 | 数据分页导航 |
| `SearchFilter.vue` | 搜索过滤器 | 搜索框和筛选选项 |
| `StatCard.vue` | 统计卡片 | 展示关键统计指标 |
| `UpdateLogCard.vue` | 更新日志卡片 | 展示系统更新历史 |
| `UpdateLogDialog.vue` | 更新日志弹窗 | 查看详细更新日志 |

#### 2.4 interact/ - 交互组件

用户交互功能组件。

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `CaptchaInput.vue` | 验证码输入 | 登录/注册表单验证码 |
| `CommentSection.vue` | 评论区域 | 用户评论互动 |
| `InteractSidebar.vue` | 互动侧边栏 | 文化互动页面侧边栏 |
| `PlantGame.vue` | 植物识别游戏 | 植物图片识别小游戏 |
| `QuizSection.vue` | 趣味答题 | 答题互动功能 |

#### 2.5 layout/ - 布局组件

页面布局相关组件。

| 文件名 | 功能说明 |
|--------|----------|
| `AppFooter.vue` | 应用页脚 |
| `AppHeader.vue` | 应用页头 |

#### 2.6 media/ - 媒体组件

媒体资源展示组件。

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `DocumentList.vue` | 文档列表 | 展示文档资源列表 |
| `DocumentPreview.vue` | 文档预览 | 在线预览文档内容 |
| `ImageCarousel.vue` | 图片轮播 | 图片集轮播展示 |
| `MediaDisplay.vue` | 媒体展示 | 统一展示各类媒体资源 |
| `VideoPlayer.vue` | 视频播放器 | 在线视频播放 |

#### 2.7 upload/ - 上传组件

文件上传相关组件。

| 文件名 | 功能说明 | 支持格式 |
|--------|----------|----------|
| `DocumentUploader.vue` | 文档上传 | PDF, Word, PPT等 |
| `FileUploader.vue` | 通用文件上传 | 所有类型文件 |
| `ImageUploader.vue` | 图片上传 | JPG, PNG, GIF等 |
| `VideoUploader.vue` | 视频上传 | MP4, AVI等 |

### 3. common/ - 通用组件

通用组件是项目中多处使用的功能性组件。

| 文件名 | 功能说明 | 使用场景 |
|--------|----------|----------|
| `PageLoading.vue` | 页面加载动画 | 路由切换时显示加载状态 |
| `SkeletonGridCard.vue` | 卡片骨架屏 | 知识库/传承人页面加载占位 |
| `SkeletonGridImage.vue` | 图片骨架屏 | 药用植物页面加载占位 |
| `SkeletonListQa.vue` | 问答骨架屏 | 问答页面加载占位 |
| `SkeletonListResource.vue` | 资源骨架屏 | 学习资源页面加载占位 |

## 🎯 组件使用规范

### 命名规范
- 组件文件使用**大驼峰命名法** (PascalCase)
- 组件名称应具有描述性，能体现组件功能

### 组件结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 脚本内容
</script>

<style scoped>
/* 样式内容 */
</style>
```

### Props定义
```javascript
const props = defineProps({
  title: {
    type: String,
    required: true
  },
  visible: {
    type: Boolean,
    default: false
  }
})
```

### Events定义
```javascript
const emit = defineEmits(['update', 'close'])

const handleClose = () => {
  emit('close')
}
```

## 📚 扩展阅读

- [Vue 3 组件基础](https://cn.vuejs.org/guide/essentials/component-basics.html)
- [Vue 3 组件注册](https://cn.vuejs.org/guide/components/registration.html)
- [Vue 3 Props](https://cn.vuejs.org/guide/components/props.html)
