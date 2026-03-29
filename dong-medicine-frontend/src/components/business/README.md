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

## 已知限制

| 组件 | 限制 | 影响 |
|------|------|------|
| ImageUploader | 单文件最大100MB | 大图片需压缩 |
| VideoUploader | 不支持断点续传 | 网络中断需重新上传 |
| DocumentPreview | 依赖kkFileView服务 | 服务不可用时无法预览 |
| PlantGame | 题目随机算法简单 | 可能出现重复题目 |
| QuizSection | 不支持题目分类筛选 | 无法按类型答题 |
| CommentSection | 不支持楼中楼 | 无法嵌套回复 |
| ChartCard | ECharts实例未自动销毁 | 频繁切换可能内存泄漏 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **上传组件优化**
   - 添加断点续传支持
   - 实现图片压缩功能
   - 添加上传进度显示

2. **交互组件增强**
   - 添加题目分类筛选
   - 实现评论楼中楼
   - 添加表情支持

3. **展示组件优化**
   - 实现虚拟滚动
   - 添加骨架屏加载
   - 优化图表性能

### 中期改进 (1-2月)

1. **组件库化**
   - 抽取为独立npm包
   - 添加TypeScript类型
   - 编写单元测试

2. **国际化支持**
   - 添加多语言支持
   - 实现RTL布局

3. **无障碍支持**
   - 添加ARIA属性
   - 支持键盘导航

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 组件框架 |
| Element Plus | 2.4+ | UI组件库 |
| ECharts | 5.4+ | 图表组件 |
| Pinia | 2.3+ | 状态管理 |

---

## 常见问题

### 1. 如何添加新的业务组件？

```vue
<template>
  <div class="my-component">
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])
const userStore = useUserStore()
</script>
```

### 2. 如何在组件中使用Composables？

```vue
<script setup>
import { useErrorHandler } from '@/composables/useErrorHandler'
import { useDebounce } from '@/composables/useDebounce'

const { error, handleError } = useErrorHandler()
const { debouncedFn } = useDebounce(search, 300)
</script>
```

### 3. 如何处理组件间通信？

```javascript
// 方式1：通过Props和Events
<ChildComponent :data="data" @update="handleUpdate" />

// 方式2：通过Pinia Store
const store = useDataStore()
store.setData(data)

// 方式3：通过Provide/Inject
provide('sharedData', data)
const sharedData = inject('sharedData')
```

---

**最后更新时间**：2026年3月30日
