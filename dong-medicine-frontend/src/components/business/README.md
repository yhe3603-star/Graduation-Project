# 业务组件（Business Components）

## 什么是业务组件？

类比：**专用设备**——咖啡机只能在咖啡区用，X光机只能在医院用。它们是为特定业务场景量身定制的。

业务组件和侗医药平台的功能直接绑定，包含具体的业务逻辑。比如"药用植物卡片"组件知道怎么展示植物名称、侗语名、功效；"知识问答"组件知道怎么提交答案和判断对错。

### 业务组件 vs 通用组件

```
通用组件（Common）              业务组件（Business）
┌──────────────────┐          ┌──────────────────┐
│   SkeletonCard   │          │    PlantCard     │
│                  │          │                  │
│  不知道展示什么    │          │  知道展示植物信息  │
│  只负责"占位"     │          │  名称、侗语名、功效 │
│  任何页面都能用    │          │  只在植物相关页面用 │
└──────────────────┘          └──────────────────┘
就像：通用桌椅                    就像：专用手术台
```

---

## 7 个子目录概览

```
business/
├── display/      展示组件 —— 展示柜，把商品漂亮地展示出来
│   └── ai-chat/  AI对话子组件 —— 客服中心各工位，协同完成智能对话
├── interact/     互动组件 —— 互动游戏区，让用户参与进来
├── media/        媒体组件 —— 多媒体播放器，展示图片/视频/文档
├── upload/       上传组件 —— 快递寄件台，把文件安全地送到服务器
├── layout/       布局组件 —— 房子的框架，天花板和地板
├── dialogs/      详情弹窗 —— 放大镜，点击卡片后弹出详细信息
└── admin/        管理组件 —— 后台管理工具箱
```

| 子目录 | 类比 | 职责 | 典型组件 |
|--------|------|------|----------|
| `display/` | 展示柜 | 展示数据，让信息一目了然 | CardGrid、SearchFilter、Pagination、AiChatCard、ChartCard、KnowledgeGraph、StatCard、UpdateLogCard |
| `display/ai-chat/` | 客服工位 | AI对话界面的子组件 | WelcomePanel、ChatMessageList、ChatInputArea、SessionDrawer、SessionBadge |
| `interact/` | 互动游戏区 | 用户参与互动，提升趣味性 | CommentSection、QuizSection、PlantGame、CaptchaInput |
| `media/` | 多媒体播放器 | 展示图片、视频、文档 | ImageCarousel、VideoPlayer、DocumentPreview、HerbAudio |
| `upload/` | 快递寄件台 | 上传文件到服务器 | ImageUploader、VideoUploader、FileUploader、DocumentUploader |
| `layout/` | 房子框架 | 页面的顶部导航和底部版权 | AppHeader、AppFooter |
| `dialogs/` | 放大镜 | 点击后弹出详细信息 | PlantDetail、KnowledgeDetail、InheritorDetail |
| `admin/` | 工具箱 | 后台管理专用组件 | AdminDashboard、AdminDataTable、AdminSidebar |

---

## 各子目录组件清单

### display/ -- 展示组件（10 个组件 + 1 个子目录）

| 组件 | 功能 | 关键特性 |
|------|------|----------|
| `CardGrid.vue` | 通用卡片网格布局 | 图片智能解析、徽章映射、标题回退链、transition-group 动画 |
| `SearchFilter.vue` | 搜索+筛选组合 | 400ms 防抖、标签式筛选切换、v-model 双向绑定 |
| `Pagination.vue` | 分页器 | 封装 el-pagination、v-model:page/size、响应式隐藏控件 |
| `ChartCard.vue` | ECharts 图表卡片 | 按需引入 echarts/core、deep watch option、自动 resize/销毁 |
| `StatCard.vue` | 统计数字卡片 | 彩色图标+数值+标签 |
| `KnowledgeGraph.vue` | 知识图谱可视化 | 力导向布局、三类节点、功效去重、ResizeObserver |
| `AiChatCard.vue` | AI 对话卡片（容器组件） | WebSocket 实时对话、流式输出、会话管理、5 个子组件组合 |
| `UpdateLogCard.vue` | 更新日志时间线 | el-timeline、展开/收起、编辑/删除模式 |
| `UpdateLogDialog.vue` | 日志新增/编辑弹窗 | 新增/编辑双模式、表单校验 |
| `PageSidebar.vue` | 页面侧边栏 | 统计 Grid + 热门推荐列表、金/银/铜排名 |

> `ai-chat/` 子目录包含 AiChatCard 的 5 个子组件，详见 [display/ai-chat/README.md](./display/ai-chat/README.md)

### interact/ -- 互动组件（5 个组件）

| 组件 | 功能 |
|------|------|
| `CaptchaInput.vue` | 图形验证码输入 |
| `CommentSection.vue` | 评论区（分页、回复嵌套、审核状态） |
| `QuizSection.vue` | 趣味答题（难度选择、倒计时、成绩展示、分享） |
| `PlantGame.vue` | 植物识别游戏（图片+选项、连击加分、收藏） |
| `InteractSidebar.vue` | 互动专区侧边栏统计 |

### media/ -- 媒体组件（6 个组件）

| 组件 | 功能 |
|------|------|
| `ImageCarousel.vue` | 图片轮播（缩略图导航、全屏预览） |
| `VideoPlayer.vue` | HTML5 视频播放器 |
| `HerbAudio.vue` | 草药侗语发音播放 |
| `MediaDisplay.vue` | 综合媒体展示（el-tabs 切换图片/视频/文档） |
| `DocumentList.vue` | 文档列表（预览/下载） |
| `DocumentPreview.vue` | 文档在线预览（KKFileView） |

### upload/ -- 上传组件（4 个组件）

| 组件 | 功能 | 依赖 |
|------|------|------|
| `ImageUploader.vue` | 图片上传（拖拽、排序、预览） | `useFileUpload({ type: 'image' })` |
| `VideoUploader.vue` | 视频上传（预览播放） | `useFileUpload({ type: 'video' })` |
| `DocumentUploader.vue` | 文档上传（类型图标） | `useFileUpload({ type: 'document' })` |
| `FileUploader.vue` | 通用文件上传 | `useFileUpload` 底层实现 |

### layout/ -- 布局组件（2 个组件）

| 组件 | 功能 |
|------|------|
| `AppHeader.vue` | 全局顶部导航（Logo+主导航+更多菜单+搜索+用户区+移动端汉堡菜单） |
| `AppFooter.vue` | 全局底部信息栏（三列布局+版权信息+靛蓝渐变背景） |

### dialogs/ -- 前台详情弹窗（5 个组件）

| 组件 | 对应模块 |
|------|---------|
| `PlantDetailDialog.vue` | 植物图鉴 |
| `KnowledgeDetailDialog.vue` | 知识库 |
| `InheritorDetailDialog.vue` | 传承人 |
| `QuizDetailDialog.vue` | 题目管理 |
| `ResourceDetailDialog.vue` | 学习资源 |

### admin/ -- 管理后台组件（3 个主组件 + 2 个子目录）

| 组件/目录 | 功能 |
|-----------|------|
| `AdminDashboard.vue` | 仪表盘（统计卡片+反馈列表+用户列表+图表） |
| `AdminDataTable.vue` | 通用数据表格（服务端分页+自定义列+操作按钮） |
| `AdminSidebar.vue` | 左侧导航菜单（10 个菜单项） |
| `admin/dialogs/` | 管理端详情弹窗（Comment/Feedback/Inheritor/Knowledge/Log/Plant/Qa/Quiz/Resource/User） |
| `admin/forms/` | CRUD 表单弹窗（Plant/Knowledge/Inheritor/Qa/Quiz/Resource） |

---

## 业务组件如何与通用组件配合

```vue
<!-- 一个典型的业务组件内部会使用通用组件 -->
<template>
  <div class="plant-page">
    <!-- 通用组件：骨架屏（加载中时显示） -->
    <SkeletonGridImage v-if="isLoading" :count="12" />

    <!-- 业务组件：药用植物卡片（加载完显示） -->
    <CardGrid
      v-else
      :items="plants"
      @click="handleCardClick"
    />
  </div>
</template>
```

**原则：业务组件负责"做什么"，通用组件负责"怎么做"。**

---

## 常见错误

### 错误1：在业务组件里写通用逻辑

```vue
<!-- ❌ 在业务组件里写 loading 动画逻辑，其他组件无法复用 -->
<template>
  <div v-if="isLoading" class="my-custom-loading">
    <div class="spinner" />  <!-- 自己写的 loading，别的组件用不了 -->
  </div>
</template>

<!-- ✅ 使用通用组件 PageLoading -->
<template>
  <PageLoading :visible="isLoading" />  <!-- 复用通用组件 -->
</template>
```

### 错误2：业务组件之间互相嵌套太深

```
❌ 组件嵌套太深，像俄罗斯套娃，调试困难：
PlantPage → PlantCard → PlantDetail → PlantComment → PlantReply

✅ 保持扁平结构，通过事件通信：
PlantPage
  ├── PlantCard（点击时通知 PlantPage）
  ├── PlantDetail（由 PlantPage 控制显示）
  └── PlantComment（由 PlantPage 控制显示）
```

### 错误3：在展示组件里发请求

```vue
<script setup>
// ❌ 展示组件不应该自己发请求，数据应该由父组件传入
import { ref } from 'vue'
import { getPlants } from '@/api/plant'
const plants = ref([])
getPlants().then(res => { plants.value = res.data })

// ✅ 展示组件只负责展示，数据通过 props 传入
const props = defineProps({
  items: { type: Array, default: () => [] }
})
</script>
```

---

## 代码审查与改进建议

- [代码重复] 上传组件(ImageUploader/VideoUploader/DocumentUploader)中handleBeforeUpload、handleSuccess、handleError、handleRemove、updateModelValue等方法几乎完全相同，应抽取useFileUpload composable
- [代码重复] PlantGame.vue和QuizSection.vue中难度选择、倒计时、结果展示等UI结构大量重复，应抽取GameLayout、DifficultySelector、CountdownTimer、ResultDisplay等公共组件
- [组件设计] App.vue承担过多职责，登录/注册对话框应抽取为独立组件
- [导出] `business/index.js` 中导出了 `MediaDisplay` 但实际文件位于 `media/MediaDisplay.vue`，而 `display/index.js` 也导出了 `MediaDisplay`（指向 `./MediaDisplay.vue`），存在重复导出，建议统一
