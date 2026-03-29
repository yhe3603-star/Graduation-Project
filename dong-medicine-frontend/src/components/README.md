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

## 使用示例

### 基础组件使用

```vue
<template>
  <ErrorBoundary @error="handleError">
    <VirtualList :items="dataList" :item-height="80">
      <template #default="{ item }">
        <div class="list-item">{{ item.name }}</div>
      </template>
    </VirtualList>
  </ErrorBoundary>
</template>

<script setup>
import { ErrorBoundary, VirtualList } from '@/components/base'

const handleError = (error) => {
  console.error('组件错误:', error)
}
</script>
```

### 业务组件使用

```vue
<template>
  <CardGrid
    :data="plants"
    :loading="loading"
    :columns="3"
    @item-click="handleItemClick"
  >
    <template #card="{ item }">
      <PlantCard :plant="item" />
    </template>
  </CardGrid>
  
  <Pagination
    :current="page"
    :total="total"
    :page-size="pageSize"
    @change="handlePageChange"
  />
</template>

<script setup>
import { CardGrid, Pagination } from '@/components/business/display'
</script>
```

### 对话框组件使用

```vue
<template>
  <el-button @click="showDetail">查看详情</el-button>
  
  <PlantDetailDialog
    v-model="dialogVisible"
    :plant-id="currentPlantId"
    @favorite="handleFavorite"
  />
</template>

<script setup>
import { ref } from 'vue'
import PlantDetailDialog from '@/components/business/dialogs/PlantDetailDialog.vue'

const dialogVisible = ref(false)
const currentPlantId = ref(null)

const showDetail = () => {
  currentPlantId.value = 1
  dialogVisible.value = true
}
</script>
```

---

## 组件依赖关系

```
页面组件 (views/)
    │
    ├── 布局组件
    │   ├── AppHeader.vue
    │   └── AppFooter.vue
    │
    ├── 展示组件
    │   ├── CardGrid.vue ──┬── PlantDetailDialog.vue
    │   ├── ChartCard.vue   │
    │   └── PageSidebar.vue ┘
    │
    ├── 交互组件
    │   ├── QuizSection.vue ──── QuizDetailDialog.vue
    │   ├── PlantGame.vue
    │   └── CommentSection.vue
    │
    ├── 媒体组件
    │   ├── ImageCarousel.vue
    │   ├── VideoPlayer.vue
    │   └── DocumentPreview.vue
    │
    └── 上传组件
        ├── ImageUploader.vue
        ├── VideoUploader.vue
        └── DocumentUploader.vue
```

---

## 开发规范

1. **命名规范**: 组件使用大驼峰命名法
2. **单文件组件**: 每个组件一个 `.vue` 文件
3. **Props验证**: 使用 `defineProps` 定义并验证 props
4. **事件命名**: 使用小驼峰命名法，如 `onItemClick`
5. **样式作用域**: 使用 `<style scoped>` 避免样式污染

### 组件模板结构

```vue
<template>
  <div class="component-name">
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  data: { type: Object, required: true }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const loading = ref(false)
const userStore = useUserStore()
</script>

<style scoped>
.component-name {
}
</style>
```

---

## 已知限制

| 组件 | 限制 | 影响 |
|------|------|------|
| VirtualList.vue | 不支持动态高度的列表项 | 需要预先知道每项高度 |
| ImageCarousel.vue | 大图加载时无渐进式显示 | 可能影响用户体验 |
| DocumentPreview.vue | 依赖kkFileView服务 | 服务不可用时无法预览 |
| VideoPlayer.vue | 不支持HLS流媒体 | 无法播放直播流 |
| FileUploader.vue | 单文件最大100MB | 大文件需分片上传 |
| ChartCard.vue | ECharts实例未自动销毁 | 频繁切换可能内存泄漏 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **VirtualList.vue**
   - 添加动态高度支持
   - 实现滚动位置记忆

2. **ImageCarousel.vue**
   - 添加图片懒加载
   - 实现渐进式加载效果

3. **ChartCard.vue**
   - 添加ECharts实例自动销毁
   - 实现响应式图表大小

### 中期改进 (1-2月)

1. **组件库化**
   - 抽取为独立npm包
   - 添加TypeScript类型定义
   - 编写单元测试

2. **性能优化**
   - 实现组件级代码分割
   - 添加组件预加载
   - 优化大列表渲染

3. **功能增强**
   - 添加国际化支持
   - 实现主题切换
   - 添加无障碍支持

### 长期规划 (3-6月)

1. **微前端支持**
   - 组件独立部署
   - 跨项目复用

2. **设计系统**
   - 统一设计规范
   - 组件文档站点
   - 设计稿自动生成

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 组件框架 |
| Element Plus | 2.4+ | UI组件库 |
| ECharts | 5.4+ | 图表组件 |
| Vue Router | 4.2+ | 路由（部分组件） |
| Pinia | 2.3+ | 状态管理（部分组件） |

---

## 测试覆盖

| 组件类型 | 测试文件 | 覆盖率 |
|----------|----------|--------|
| 基础组件 | `__tests__/base/` | 待补充 |
| 业务组件 | `__tests__/business/` | 待补充 |
| 工具函数 | `__tests__/utils/` | 已覆盖 |

---

**最后更新时间**：2026年3月30日
