# 组件目录说明

## 文件夹结构

本目录包含项目中所有的Vue组件，按照功能和用途进行分类。

```
components/
├── base/            # 基础组件
├── business/        # 业务组件
└── common/          # 通用组件
```

## 详细说明

### 1. base/ 目录

**用途**：存放基础组件，这些组件不依赖于具体业务逻辑，可以在多个地方复用。

**文件列表**：

- **ErrorBoundary.vue**
  - 功能：错误边界组件，用于捕获子组件的错误，防止整个应用崩溃
  - 使用场景：包裹可能出现错误的组件，提供错误提示界面
  - 技术要点：使用Vue 3的errorCaptured钩子

- **VirtualList.vue**
  - 功能：虚拟列表组件，用于高效渲染大量数据
  - 使用场景：展示长列表数据时，提高性能
  - 技术要点：只渲染可视区域内的元素

- **index.js**
  - 功能：统一导出基础组件
  - 使用方法：通过 `import { ErrorBoundary, VirtualList } from '@/components/base'` 导入

### 2. business/ 目录

**用途**：存放业务相关的组件，这些组件与具体业务逻辑相关。

**子目录**：

- **admin/**：管理后台相关组件
  - **dialogs/**：管理后台的详情对话框
  - **forms/**：管理后台的表单对话框
  - **AdminDashboard.vue**：管理仪表盘
  - **AdminDataTable.vue**：数据表格组件
  - **AdminSidebar.vue**：管理侧边栏

- **dialogs/**：业务详情对话框
  - **InheritorDetailDialog.vue**：传承人详情对话框
  - **KnowledgeDetailDialog.vue**：知识详情对话框
  - **PlantDetailDialog.vue**：药材详情对话框
  - **QuizDetailDialog.vue**：答题详情对话框
  - **ResourceDetailDialog.vue**：资源详情对话框

- **display/**：展示型组件
  - **AiChatCard.vue**：AI对话卡片
  - **CardGrid.vue**：卡片网格布局
  - **ChartCard.vue**：图表卡片
  - **PageSidebar.vue**：页面侧边栏
  - **Pagination.vue**：分页组件
  - **SearchFilter.vue**：搜索过滤组件
  - **UpdateLogCard.vue**：更新日志卡片
  - **UpdateLogDialog.vue**：更新日志对话框

- **interact/**：交互型组件
  - **CommentSection.vue**：评论区组件
  - **InteractSidebar.vue**：互动侧边栏
  - **PlantGame.vue**：植物识别游戏
  - **QuizSection.vue**：答题组件

- **layout/**：布局组件
  - **AppFooter.vue**：应用页脚
  - **AppHeader.vue**：应用头部

- **media/**：媒体相关组件
  - **DocumentList.vue**：文档列表
  - **DocumentPreview.vue**：文档预览
  - **ImageCarousel.vue**：图片轮播
  - **MediaDisplay.vue**：媒体展示
  - **VideoPlayer.vue**：视频播放器

- **upload/**：上传组件
  - **DocumentUploader.vue**：文档上传
  - **FileUploader.vue**：文件上传
  - **ImageUploader.vue**：图片上传
  - **VideoUploader.vue**：视频上传

- **index.js**：统一导出所有业务组件

### 3. common/ 目录

**用途**：存放通用组件，这些组件在多个业务场景中都会用到。

**文件列表**：

- **SkeletonGrid.vue**：骨架屏组件，用于数据加载时的占位显示

## 如何使用这些组件

### 导入组件

```javascript
// 方法1：直接导入
import AdminDataTable from '@/components/business/admin/AdminDataTable.vue'

// 方法2：通过index.js导出导入
import { AdminDataTable, SearchFilter } from '@/components/business'
```

### 组件使用示例

```vue
<template>
  <div>
    <!-- 使用搜索过滤组件 -->
    <SearchFilter
      v-model="keyword"
      placeholder="搜索传承人姓名、技艺..."
      :filters="filterConfig"
      @search="handleSearch"
      @filter="handleFilter"
    />
    
    <!-- 使用分页组件 -->
    <Pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { SearchFilter, Pagination } from '@/components/business'

const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const handleSearch = (value) => {
  console.log('搜索关键词:', value)
}

const handleFilter = (filters) => {
  console.log('筛选条件:', filters)
}

const handleSizeChange = (size) => {
  pageSize.value = size
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}
</script>
```

## 组件开发规范

1. **命名规范**：组件名称使用大驼峰命名法，文件名与组件名保持一致
2. **目录组织**：按照功能和用途分类存放
3. **代码风格**：遵循ESLint规范，使用Vue 3的Composition API
4. **文档说明**：为每个组件添加适当的注释，说明组件的功能和使用方法
5. **性能优化**：对于大型组件，考虑使用虚拟滚动、懒加载等技术

## 注意事项

- 所有组件都使用Vue 3的Composition API开发
- 组件样式使用CSS变量，定义在 `styles/variables.css` 中
- 组件中使用的图标来自Element Plus Icons
- 组件中的数据请求使用封装的 `utils/request.js`

---

**最后更新时间**：2026年3月23日