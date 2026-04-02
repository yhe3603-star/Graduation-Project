# 组件目录 (components)

本目录存放所有可复用的 Vue 组件，是前端项目的核心部分。

## 目录

- [什么是组件？](#什么是组件)
- [目录结构](#目录结构)
- [组件分类](#组件分类)
- [组件开发规范](#组件开发规范)
- [常用组件详解](#常用组件详解)

---

## 什么是组件？

### 组件的概念

**组件**是 Vue 应用程序的基本构建块。你可以把组件想象成"乐高积木"——每个积木（组件）都有特定的形状和功能，你可以把它们组合起来搭建出复杂的城堡（应用）。

### 为什么需要组件？

```
┌─────────────────────────────────────────────────────────────────┐
│                      不使用组件                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  一个巨大的页面文件，包含所有代码：                                │
│  - 几千行HTML                                                   │
│  - 几千行JavaScript                                             │
│  - 几千行CSS                                                    │
│  → 难以维护、难以复用、难以测试                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       使用组件                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  页面 = 多个小组件组合                                           │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                         │
│  │ Header  │  │ Sidebar │  │ Footer  │                         │
│  └─────────┘  └─────────┘  └─────────┘                         │
│  ┌─────────────────────────────────────┐                       │
│  │            CardGrid                 │                       │
│  │  ┌───────┐ ┌───────┐ ┌───────┐    │                       │
│  │  │ Card  │ │ Card  │ │ Card  │    │                       │
│  │  └───────┘ └───────┘ └───────┘    │                       │
│  └─────────────────────────────────────┘                       │
│  → 易于维护、可复用、可测试                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 组件的基本结构

一个 Vue 组件通常由三部分组成：

```vue
<template>
  <!-- 模板：定义组件的HTML结构 -->
  <div class="user-card">
    <img :src="avatar" :alt="name">
    <h3>{{ name }}</h3>
    <p>{{ bio }}</p>
    <button @click="handleClick">关注</button>
  </div>
</template>

<script setup>
// 脚本：定义组件的逻辑
import { ref } from 'vue'

// 定义组件接收的属性（父组件传递的数据）
const props = defineProps({
  name: {
    type: String,
    required: true
  },
  avatar: {
    type: String,
    default: '/default-avatar.png'
  },
  bio: {
    type: String,
    default: '这个人很懒，什么都没写...'
  }
})

// 定义组件发出的事件（通知父组件）
const emit = defineEmits(['follow'])

const handleClick = () => {
  emit('follow', props.name)
}
</script>

<style scoped>
/* 样式：定义组件的外观 */
.user-card {
  border: 1px solid #ddd;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.user-card img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}

.user-card h3 {
  margin: 8px 0;
  color: #333;
}

.user-card button {
  background: #1A5276;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.user-card button:hover {
  background: #154360;
}
</style>
```

---

## 目录结构

```
components/
│
├── base/                                # 基础组件
│   ├── ErrorBoundary.vue                # 错误边界组件
│   ├── VirtualList.vue                  # 虚拟列表组件
│   └── index.js                         # 导出文件
│
├── common/                              # 通用组件
│   ├── PageLoading.vue                  # 页面加载动画
│   ├── SkeletonGridCard.vue             # 卡片骨架屏
│   ├── SkeletonGridImage.vue            # 图片骨架屏
│   ├── SkeletonListQa.vue               # 问答骨架屏
│   └── SkeletonListResource.vue         # 资源骨架屏
│
└── business/                            # 业务组件
    │
    ├── layout/                          # 布局组件
    │   ├── AppHeader.vue                # 应用头部
    │   └── AppFooter.vue                # 应用底部
    │
    ├── display/                         # 展示组件
    │   ├── AiChatCard.vue               # AI对话卡片
    │   ├── CardGrid.vue                 # 卡片网格
    │   ├── ChartCard.vue                # 图表卡片
    │   ├── PageSidebar.vue              # 页面侧边栏
    │   ├── Pagination.vue               # 分页组件
    │   ├── SearchFilter.vue             # 搜索过滤
    │   ├── UpdateLogCard.vue            # 更新日志卡片
    │   ├── UpdateLogDialog.vue          # 更新日志对话框
    │   └── index.js
    │
    ├── interact/                        # 交互组件
    │   ├── CaptchaInput.vue             # 验证码输入
    │   ├── CommentSection.vue           # 评论组件
    │   ├── InteractSidebar.vue          # 互动侧边栏
    │   ├── PlantGame.vue                # 植物识别游戏
    │   ├── QuizSection.vue              # 趣味答题
    │   └── index.js
    │
    ├── media/                           # 媒体组件
    │   ├── DocumentList.vue             # 文档列表
    │   ├── DocumentPreview.vue          # 文档预览
    │   ├── ImageCarousel.vue            # 图片轮播
    │   ├── MediaDisplay.vue             # 媒体展示
    │   ├── VideoPlayer.vue              # 视频播放
    │   └── index.js
    │
    ├── upload/                          # 上传组件
    │   ├── DocumentUploader.vue         # 文档上传
    │   ├── FileUploader.vue             # 通用文件上传
    │   ├── ImageUploader.vue            # 图片上传
    │   ├── VideoUploader.vue            # 视频上传
    │   └── index.js
    │
    ├── dialogs/                         # 详情对话框
    │   ├── InheritorDetailDialog.vue
    │   ├── KnowledgeDetailDialog.vue
    │   ├── PlantDetailDialog.vue
    │   ├── QuizDetailDialog.vue
    │   └── ResourceDetailDialog.vue
    │
    └── admin/                           # 管理后台组件
        ├── dialogs/                     # 管理对话框
        ├── forms/                       # 管理表单
        ├── AdminDashboard.vue           # 管理仪表盘
        ├── AdminDataTable.vue           # 数据表格
        └── AdminSidebar.vue             # 管理侧边栏
```

---

## 组件分类

### 1. 基础组件 (base/)

基础组件是最底层的组件，不包含业务逻辑，只提供通用功能。

| 组件 | 功能 | 使用场景 |
|------|------|----------|
| ErrorBoundary | 捕获子组件错误，显示备用UI | 包裹可能出错的组件 |
| VirtualList | 虚拟滚动列表，优化大数据渲染 | 长列表渲染 |

**示例 - VirtualList 使用：**

```vue
<template>
  <VirtualList
    :items="plants"
    :item-height="200"
    :buffer="5"
  >
    <template #default="{ item }">
      <PlantCard :plant="item" />
    </template>
  </VirtualList>
</template>

<script setup>
import VirtualList from '@/components/base/VirtualList.vue'
import PlantCard from './PlantCard.vue'

const plants = ref([...]) // 1000+ 条数据
</script>
```

### 2. 通用组件 (common/)

通用组件是项目中多处使用的功能性组件。

| 组件 | 功能 | 使用场景 |
|------|------|----------|
| PageLoading | 页面加载动画 | 路由切换时显示 |
| SkeletonGridCard | 卡片骨架屏 | 卡片列表加载时显示 |
| SkeletonGridImage | 图片骨架屏 | 图片列表加载时显示 |
| SkeletonListQa | 问答骨架屏 | 问答列表加载时显示 |
| SkeletonListResource | 资源骨架屏 | 资源列表加载时显示 |

**示例 - 骨架屏使用：**

```vue
<template>
  <div class="plants-page">
    <!-- 加载中显示骨架屏 -->
    <SkeletonGridImage v-if="loading" :count="12" />
    
    <!-- 加载完成显示实际内容 -->
    <CardGrid v-else :items="plants" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import SkeletonGridImage from '@/components/common/SkeletonGridImage.vue'
import CardGrid from '@/components/business/display/CardGrid.vue'

const loading = ref(true)
const plants = ref([])

onMounted(async () => {
  plants.value = await fetchPlants()
  loading.value = false
})
</script>
```

### 3. 业务组件 (business/)

业务组件包含具体的业务逻辑，是项目特有的组件。

#### 布局组件 (layout/)

| 组件 | 功能 |
|------|------|
| AppHeader | 应用顶部导航栏，包含Logo、菜单、用户信息 |
| AppFooter | 应用底部，包含版权信息、友情链接 |

#### 展示组件 (display/)

| 组件 | 功能 |
|------|------|
| AiChatCard | AI对话卡片，集成DeepSeek智能问答 |
| CardGrid | 卡片网格布局，用于展示植物、知识等列表 |
| ChartCard | 图表卡片，封装ECharts图表 |
| PageSidebar | 页面侧边栏，展示统计数据和热门内容 |
| Pagination | 分页组件，支持页码跳转 |
| SearchFilter | 搜索过滤组件，支持关键词搜索和分类筛选 |

#### 交互组件 (interact/)

| 组件 | 功能 |
|------|------|
| CaptchaInput | 验证码输入组件，显示图形验证码 |
| CommentSection | 评论组件，支持发表评论、回复、点赞 |
| InteractSidebar | 互动侧边栏，展示排行榜和统计 |
| PlantGame | 植物识别游戏，根据图片识别药材名称 |
| QuizSection | 趣味答题组件，支持计时、评分 |

#### 媒体组件 (media/)

| 组件 | 功能 |
|------|------|
| DocumentList | 文档列表组件，展示多个文档 |
| DocumentPreview | 文档预览组件，支持PDF、Word等格式 |
| ImageCarousel | 图片轮播组件，支持自动播放 |
| MediaDisplay | 媒体展示组件，根据类型自动选择展示方式 |
| VideoPlayer | 视频播放组件，基于HTML5 video |

#### 上传组件 (upload/)

| 组件 | 功能 |
|------|------|
| DocumentUploader | 文档上传组件 |
| FileUploader | 通用文件上传组件 |
| ImageUploader | 图片上传组件，支持预览和裁剪 |
| VideoUploader | 视频上传组件，支持进度显示 |

#### 详情对话框 (dialogs/)

| 组件 | 功能 |
|------|------|
| PlantDetailDialog | 药材详情弹窗 |
| KnowledgeDetailDialog | 知识详情弹窗 |
| InheritorDetailDialog | 传承人详情弹窗 |
| ResourceDetailDialog | 资源详情弹窗 |
| QuizDetailDialog | 答题详情弹窗 |

---

## 组件开发规范

### 1. 命名规范

```
组件文件名：PascalCase（大驼峰）
例如：UserCard.vue, PlantDetailDialog.vue

组件name：PascalCase
例如：name: 'UserCard'

props名称：camelCase（小驼峰）
例如：userName, isActive

事件名称：kebab-case（短横线）
例如：@update-user, @close-dialog
```

### 2. 组件结构规范

```vue
<template>
  <!-- 1. 根元素，使用有意义的类名 -->
  <div class="component-name">
    <!-- 2. 内容区域 -->
    <div class="component-name__header">
      <!-- 头部内容 -->
    </div>
    
    <div class="component-name__body">
      <!-- 主体内容 -->
    </div>
    
    <div class="component-name__footer">
      <!-- 底部内容 -->
    </div>
  </div>
</template>

<script setup>
// 1. 导入语句
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. Props 定义
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

// 3. Emits 定义
const emit = defineEmits(['update:visible', 'confirm'])

// 4. 响应式数据
const loading = ref(false)
const data = ref([])

// 5. 计算属性
const filteredData = computed(() => {
  return data.value.filter(item => item.active)
})

// 6. 方法
const handleClick = () => {
  emit('confirm', data.value)
}

// 7. 生命周期钩子
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* 使用 scoped 限制样式作用域 */
.component-name {
  /* 组件样式 */
}

.component-name__header {
  /* 头部样式 */
}

.component-name__body {
  /* 主体样式 */
}
</style>
```

### 3. Props 规范

```javascript
// 好的实践：详细定义props
const props = defineProps({
  // 基础类型检查
  title: String,
  
  // 多种可能的类型
  value: [String, Number],
  
  // 必填字段
  id: {
    type: Number,
    required: true
  },
  
  // 带默认值
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  
  // 对象默认值
  options: {
    type: Object,
    default: () => ({
      page: 1,
      size: 10
    })
  },
  
  // 数组默认值
  items: {
    type: Array,
    default: () => []
  }
})
```

### 4. 事件规范

```javascript
// 定义事件
const emit = defineEmits({
  // 无验证
  close: null,
  
  // 带验证
  submit: (data) => {
    if (data.title) {
      return true
    }
    console.warn('标题不能为空')
    return false
  }
})

// 触发事件
const handleSubmit = () => {
  emit('submit', { title: '标题', content: '内容' })
}
```

---

## 常用组件详解

### CardGrid - 卡片网格组件

用于展示卡片列表，支持响应式布局。

```vue
<template>
  <CardGrid
    :items="plants"
    :loading="loading"
    :columns="4"
    @item-click="handleItemClick"
  >
    <template #default="{ item }">
      <PlantCard :plant="item" />
    </template>
  </CardGrid>
</template>

<script setup>
import CardGrid from '@/components/business/display/CardGrid.vue'
import PlantCard from './PlantCard.vue'

const plants = ref([...])
const loading = ref(false)

const handleItemClick = (item) => {
  console.log('点击了:', item)
}
</script>
```

### Pagination - 分页组件

用于列表分页，支持页码跳转。

```vue
<template>
  <Pagination
    v-model:current="currentPage"
    :total="total"
    :page-size="pageSize"
    @change="handlePageChange"
  />
</template>

<script setup>
import { ref } from 'vue'
import Pagination from '@/components/business/display/Pagination.vue'

const currentPage = ref(1)
const total = ref(100)
const pageSize = ref(10)

const handlePageChange = (page) => {
  fetchData(page)
}
</script>
```

### CommentSection - 评论组件

用于展示和管理评论，支持嵌套回复。

```vue
<template>
  <CommentSection
    :target-type="'plant'"
    :target-id="plantId"
    :comments="comments"
    @submit="handleSubmitComment"
    @reply="handleReply"
  />
</template>

<script setup>
import CommentSection from '@/components/business/interact/CommentSection.vue'

const plantId = ref(1)
const comments = ref([...])

const handleSubmitComment = async (content) => {
  await submitComment(content)
}

const handleReply = async (parentId, content) => {
  await replyComment(parentId, content)
}
</script>
```

### ImageUploader - 图片上传组件

用于上传图片，支持预览和裁剪。

```vue
<template>
  <ImageUploader
    v-model:image-url="imageUrl"
    :max-size="10"
    :accept="['.jpg', '.jpeg', '.png', '.gif']"
    @success="handleUploadSuccess"
    @error="handleUploadError"
  />
</template>

<script setup>
import { ref } from 'vue'
import ImageUploader from '@/components/business/upload/ImageUploader.vue'

const imageUrl = ref('')

const handleUploadSuccess = (url) => {
  console.log('上传成功:', url)
}

const handleUploadError = (error) => {
  console.error('上传失败:', error)
}
</script>
```

---

## 最佳实践

### 1. 组件拆分原则

- **单一职责**：一个组件只做一件事
- **可复用性**：考虑组件是否能在多处使用
- **可维护性**：组件代码不超过300行

### 2. 组件通信方式

| 场景 | 推荐方式 |
|------|----------|
| 父→子 | Props |
| 子→父 | Emit |
| 兄弟组件 | Pinia 状态管理 |
| 跨层级 | Provide/Inject 或 Pinia |

### 3. 组件性能优化

- 使用 `v-if` 和 `v-show` 合理控制渲染
- 大列表使用虚拟滚动
- 使用 `computed` 缓存计算结果
- 合理使用 `v-memo` 缓存子树

---

**相关文档**

- [Vue 3 组件基础](https://cn.vuejs.org/guide/essentials/component-basics.html)
- [Vue 3 组件深入](https://cn.vuejs.org/guide/components/registration.html)
- [Element Plus 组件库](https://element-plus.org/zh-CN/)

---

**最后更新时间**：2026年4月3日
