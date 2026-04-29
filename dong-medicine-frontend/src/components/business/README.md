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
├── interact/     互动组件 —— 互动游戏区，让用户参与进来
├── media/        媒体组件 —— 多媒体播放器，展示图片/视频/文档
├── upload/       上传组件 —— 快递寄件台，把文件安全地送到服务器
├── layout/       布局组件 —— 房子的框架，天花板和地板
├── dialogs/      详情弹窗 —— 放大镜，点击卡片后弹出详细信息
└── admin/        管理组件 —— 后台管理工具箱
```

| 子目录 | 类比 | 职责 | 典型组件 |
|--------|------|------|----------|
| `display/` | 展示柜 | 展示数据，让信息一目了然 | CardGrid、SearchFilter、Pagination |
| `interact/` | 互动游戏区 | 用户参与互动，提升趣味性 | CommentSection、QuizSection、PlantGame |
| `media/` | 多媒体播放器 | 展示图片、视频、文档 | ImageCarousel、VideoPlayer、DocumentPreview |
| `upload/` | 快递寄件台 | 上传文件到服务器 | ImageUploader、VideoUploader、FileUploader |
| `layout/` | 房子框架 | 页面的顶部导航和底部版权 | AppHeader、AppFooter |
| `dialogs/` | 放大镜 | 点击后弹出详细信息 | PlantDetail、KnowledgeDetail |
| `admin/` | 工具箱 | 后台管理专用组件 | AdminDashboard、AdminDataTable |

---

## 业务组件如何与通用组件配合

```vue
<!-- 一个典型的业务组件内部会使用通用组件 -->
<template>
  <div class="plant-page">
    <!-- 通用组件：骨架屏（加载中时显示） -->
    <SkeletonCard v-if="isLoading" />

    <!-- 业务组件：药用植物卡片（加载完显示） -->
    <PlantCard
      v-else
      v-for="plant in plants"
      :key="plant.id"
      :data="plant"
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
  <PageLoading v-if="isLoading" />  <!-- 复用通用组件 -->
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

---

## 代码审查与改进建议

- [代码重复] 上传组件(ImageUploader/VideoUploader/DocumentUploader)中handleBeforeUpload、handleSuccess、handleError、handleRemove、updateModelValue等方法几乎完全相同，应抽取useFileUpload composable
- [代码重复] PlantGame.vue和QuizSection.vue中难度选择、倒计时、结果展示等UI结构大量重复，应抽取GameLayout、DifficultySelector、CountdownTimer、ResultDisplay等公共组件
- [组件设计] App.vue承担过多职责，登录/注册对话框应抽取为独立组件
