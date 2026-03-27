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
├── admin/           # 管理后台组件
│   ├── dialogs/     # 管理后台详情对话框
│   ├── forms/       # 管理后台表单对话框
│   ├── AdminDashboard.vue
│   ├── AdminDataTable.vue
│   └── AdminSidebar.vue
├── dialogs/         # 前台详情对话框
├── display/         # 展示组件
├── interact/        # 交互组件
├── layout/          # 布局组件
├── media/           # 媒体组件
├── upload/          # 上传组件
├── index.js         # 统一导出
└── README.md        # 说明文档
```

## 组件分类

### 1. 上传组件 (upload/)

用于文件上传的组件，支持多种文件类型。

| 组件名 | 功能描述 | Props |
|--------|----------|-------|
| ImageUploader | 图片上传组件 | modelValue, maxCount, accept |
| VideoUploader | 视频上传组件 | modelValue, maxSize, accept |
| DocumentUploader | 文档上传组件 | modelValue, accept |
| FileUploader | 通用文件上传组件 | modelValue, accept, maxSize |

**使用示例**：
```vue
<template>
  <ImageUploader v-model="images" :max-count="5" />
  <VideoUploader v-model="videos" :max-size="100" />
</template>
```

### 2. 媒体组件 (media/)

用于媒体文件预览和展示的组件。

| 组件名 | 功能描述 | Props |
|--------|----------|-------|
| ImageCarousel | 图片轮播组件 | images, autoplay, interval |
| VideoPlayer | 视频播放组件 | src, poster, controls |
| DocumentPreview | 文档预览组件 | url, type, title |
| DocumentList | 文档列表组件 | documents, downloadable |
| MediaDisplay | 媒体展示组件 | images, videos, documents |

**使用示例**：
```vue
<template>
  <ImageCarousel :images="plant.images" />
  <VideoPlayer :src="video.url" />
  <DocumentPreview :url="doc.url" :type="doc.type" />
</template>
```

### 3. 交互组件 (interact/)

用于用户交互和游戏功能的组件。

| 组件名 | 功能描述 | Props |
|--------|----------|-------|
| CommentSection | 评论组件 | targetType, targetId |
| PlantGame | 植物识别游戏 | difficulty, questionCount |
| QuizSection | 趣味答题组件 | category, questionCount |
| InteractSidebar | 互动侧边栏 | activeTab |
| CaptchaInput | 验证码输入组件 | length, disabled |

**使用示例**：
```vue
<template>
  <CommentSection target-type="plant" :target-id="plantId" />
  <PlantGame difficulty="medium" :question-count="10" />
  <QuizSection category="all" :question-count="5" />
</template>
```

### 4. 展示组件 (display/)

用于数据展示和内容呈现的组件。

| 组件名 | 功能描述 | Props |
|--------|----------|-------|
| AiChatCard | AI对话卡片 | title, placeholder |
| UpdateLogCard | 更新日志卡片 | logs, maxItems |
| UpdateLogDialog | 更新日志对话框 | visible, logs |
| CardGrid | 卡片网格组件 | items, columns, loading |
| ChartCard | 图表卡片组件 | data, type, title |
| SearchFilter | 搜索过滤组件 | filters, keyword |
| PageSidebar | 页面侧边栏 | categories, activeCategory |
| Pagination | 分页组件 | total, page, size |

**使用示例**：
```vue
<template>
  <CardGrid :items="plants" :columns="3" :loading="loading" />
  <SearchFilter :filters="filters" v-model:keyword="keyword" />
  <Pagination :total="100" :page="1" :size="20" @change="onPageChange" />
</template>
```

### 5. 布局组件 (layout/)

用于页面布局的组件。

| 组件名 | 功能描述 | Props |
|--------|----------|-------|
| AppHeader | 应用头部 | title, showBack |
| AppFooter | 应用底部 | links, copyright |

### 6. 管理后台组件 (admin/)

用于管理后台的专用组件。

#### 主组件

| 组件名 | 功能描述 |
|--------|----------|
| AdminDashboard | 管理仪表盘，展示统计数据 |
| AdminDataTable | 数据表格，支持CRUD操作 |
| AdminSidebar | 管理侧边栏导航 |

#### 详情对话框 (admin/dialogs/)

| 组件名 | 功能描述 |
|--------|----------|
| CommentDetailDialog | 评论详情对话框 |
| FeedbackDetailDialog | 反馈详情对话框 |
| InheritorDetailDialog | 传承人详情对话框 |
| KnowledgeDetailDialog | 知识详情对话框 |
| LogDetailDialog | 操作日志详情对话框 |
| PlantDetailDialog | 药材详情对话框 |
| QaDetailDialog | 问答详情对话框 |
| QuizDetailDialog | 答题详情对话框 |
| ResourceDetailDialog | 资源详情对话框 |
| UserDetailDialog | 用户详情对话框 |

#### 表单对话框 (admin/forms/)

| 组件名 | 功能描述 |
|--------|----------|
| InheritorFormDialog | 传承人表单对话框 |
| KnowledgeFormDialog | 知识表单对话框 |
| PlantFormDialog | 药材表单对话框 |
| QaFormDialog | 问答表单对话框 |
| QuizFormDialog | 答题表单对话框 |
| ResourceFormDialog | 资源表单对话框 |

### 7. 前台详情对话框 (dialogs/)

用于前台页面展示详情的对话框组件。

| 组件名 | 功能描述 |
|--------|----------|
| InheritorDetailDialog | 传承人详情对话框 |
| KnowledgeDetailDialog | 知识详情对话框 |
| PlantDetailDialog | 药材详情对话框 |
| QuizDetailDialog | 答题详情对话框 |
| ResourceDetailDialog | 资源详情对话框 |

## 使用规范

### 导入方式

```javascript
// 推荐方式：从index.js导入
import { 
  ImageCarousel, 
  CommentSection, 
  AdminDashboard 
} from '@/components/business'

// 或按分类导入
import { ImageCarousel } from '@/components/business/media'
import { CommentSection } from '@/components/business/interact'
import { AdminDashboard } from '@/components/business/admin'
```

### 组件开发规范

1. **业务组件可依赖基础组件**
2. **可访问Composables和Store**
3. **保持组件职责单一**
4. **复杂逻辑提取到Composables**

### 组件命名规范

- 组件名使用大驼峰命名法
- 对话框组件以`Dialog`结尾
- 表单组件以`Form`结尾
- 上传组件以`Uploader`结尾

## 组件统计

| 分类 | 组件数量 | 主要用途 |
|------|---------|---------|
| upload/ | 4 | 文件上传 |
| media/ | 5 | 媒体展示 |
| interact/ | 5 | 用户交互 |
| display/ | 8 | 数据展示 |
| layout/ | 2 | 页面布局 |
| admin/ | 19 | 管理后台 |
| dialogs/ | 5 | 前台详情 |
| **总计** | **48** | - |

## 依赖关系

- **外部依赖**：Element Plus、Vue 3
- **内部依赖**：基础组件、Composables、Utils、Stores

---

**最后更新时间**：2026年3月27日
