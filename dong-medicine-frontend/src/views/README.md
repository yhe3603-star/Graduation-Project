# Views 页面组件目录

本目录包含项目的所有页面级组件，每个页面对应一个路由。

## 目录结构

```
views/
├── Home.vue              # 首页
├── Plants.vue            # 药用植物页面
├── Inheritors.vue        # 传承人页面
├── Knowledge.vue         # 知识库页面
├── Qa.vue                # 问答社区页面
├── Resources.vue         # 学习资源页面
├── Interact.vue          # 互动专区页面
├── Visual.vue            # 数据可视化页面
├── PersonalCenter.vue    # 个人中心页面
├── Admin.vue             # 管理后台页面
├── About.vue             # 关于页面
├── Feedback.vue          # 意见反馈页面
├── GlobalSearch.vue      # 全局搜索页面
└── NotFound.vue          # 404页面
```

## 页面说明

### Home.vue - 首页

**路由**: `/`

**功能**:
- 平台核心功能入口展示
- 统计数据概览（植物数、传承人数、知识条目数）
- 传承人风采轮播展示
- 快速导航卡片
- AI智能问答入口

**依赖组件**:
- `AiChatCard.vue` - AI对话卡片
- `CardGrid.vue` - 卡片网格
- `UpdateLogCard.vue` - 更新日志

---

### Plants.vue - 药用植物页面

**路由**: `/plants`

**功能**:
- 药用植物列表展示（卡片网格布局）
- 分类筛选（按药材分类）
- 用法方式筛选
- 关键词搜索
- 植物详情查看（对话框）
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `PlantDetailDialog.vue` - 植物详情对话框
- `Pagination.vue` - 分页

---

### Inheritors.vue - 传承人页面

**路由**: `/inheritors`

**功能**:
- 传承人列表展示
- 按级别筛选（国家级/省级/市级/县级）
- 关键词搜索
- 传承人详情查看
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `InheritorDetailDialog.vue` - 传承人详情对话框
- `Pagination.vue` - 分页

---

### Knowledge.vue - 知识库页面

**路由**: `/knowledge`

**功能**:
- 知识条目列表展示
- 分类筛选（疗法分类、疾病分类）
- 关键词搜索
- 知识详情查看
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `KnowledgeDetailDialog.vue` - 知识详情对话框
- `PageSidebar.vue` - 侧边栏

---

### Qa.vue - 问答社区页面

**路由**: `/qa`

**功能**:
- 常见问答列表
- 分类筛选
- 关键词搜索
- 问答详情查看

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `Pagination.vue` - 分页

---

### Resources.vue - 学习资源页面

**路由**: `/resources`

**功能**:
- 学习资源列表展示
- 分类筛选（视频/文档/图片）
- 文件类型筛选
- 资源预览（视频播放、文档预览）
- 资源下载
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `ResourceDetailDialog.vue` - 资源详情对话框
- `VideoPlayer.vue` - 视频播放
- `DocumentPreview.vue` - 文档预览

---

### Interact.vue - 互动专区页面

**路由**: `/interact`

**功能**:
- 趣味答题模块
- 植物识别游戏
- 评论交流
- 排行榜展示

**依赖组件**:
- `QuizSection.vue` - 答题组件
- `PlantGame.vue` - 植物游戏
- `CommentSection.vue` - 评论组件
- `InteractSidebar.vue` - 互动侧边栏

---

### Visual.vue - 数据可视化页面

**路由**: `/visual`

**功能**:
- 药方频次统计图表
- 疗法分类统计
- 传承人分布地图
- 平台数据趋势

**依赖组件**:
- `ChartCard.vue` - 图表卡片（ECharts）

---

### PersonalCenter.vue - 个人中心页面

**路由**: `/personal` (需登录)

**功能**:
- 用户信息展示与修改
- 我的收藏管理
- 答题记录查看
- 评论历史查看
- 密码修改

**依赖组件**:
- `usePersonalCenter.js` - 个人中心逻辑

---

### Admin.vue - 管理后台页面

**路由**: `/admin` (需管理员权限)

**功能**:
- 数据统计仪表盘
- 用户管理（列表、封禁、角色分配）
- 内容管理（植物、知识、传承人、资源、问答）
- 评论审核
- 反馈处理
- 操作日志查看

**依赖组件**:
- `AdminDashboard.vue` - 仪表盘
- `AdminDataTable.vue` - 数据表格
- `AdminSidebar.vue` - 侧边栏
- `admin/dialogs/*` - 详情对话框
- `admin/forms/*` - 表单对话框

---

### About.vue - 关于页面

**路由**: `/about`

**功能**:
- 选题背景介绍
- 平台特色说明
- 功能模块介绍
- 侗医文化背景

---

### Feedback.vue - 意见反馈页面

**路由**: `/feedback`

**功能**:
- 反馈表单提交
- 反馈类型选择
- 反馈记录查看

---

### GlobalSearch.vue - 全局搜索页面

**路由**: `/search`

**功能**:
- 跨模块统一搜索
- 搜索结果分类展示
- 搜索历史记录

---

### NotFound.vue - 404页面

**路由**: `/:pathMatch(.*)*`

**功能**:
- 404错误提示
- 返回首页链接

---

## 页面开发模板

### 基础页面模板

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <h1>{{ pageTitle }}</h1>
    </div>
    
    <div class="page-content">
      <SearchFilter
        v-model:keyword="searchParams.keyword"
        :filters="filters"
        @search="handleSearch"
      />
      
      <CardGrid
        :data="dataList"
        :loading="loading"
        :columns="3"
      >
        <template #card="{ item }">
          <ItemCard :data="item" @click="handleItemClick(item)" />
        </template>
      </CardGrid>
      
      <Pagination
        :current="pagination.page"
        :total="pagination.total"
        :page-size="pagination.size"
        @change="handlePageChange"
      />
    </div>
    
    <DetailDialog
      v-model="dialogVisible"
      :data="currentItem"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useDebounce } from '@/composables/useDebounce'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const dataList = ref([])
const dialogVisible = ref(false)
const currentItem = ref(null)

const searchParams = reactive({
  keyword: '',
  category: '',
  page: 1,
  size: 20
})

const pagination = reactive({
  page: 1,
  total: 0,
  size: 20
})

const { debouncedFn: debouncedSearch } = useDebounce(fetchData, 300)

const handleSearch = () => {
  searchParams.page = 1
  debouncedSearch()
}

const handlePageChange = (page) => {
  searchParams.page = page
  fetchData()
}

const handleItemClick = (item) => {
  currentItem.value = item
  dialogVisible.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getList(searchParams)
    dataList.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-content {
  min-height: 500px;
}
</style>
```

---

## 页面权限配置

### 路由守卫配置

```javascript
const routes = [
  {
    path: '/personal',
    component: () => import('@/views/PersonalCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ path: '/', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/403')
  } else {
    next()
  }
})
```

---

## 已知限制

| 页面 | 限制 | 影响 |
|------|------|------|
| Plants.vue | 不支持拼音搜索 | 无法用拼音查找药材 |
| Inheritors.vue | 无地图展示 | 无法查看传承人分布 |
| Knowledge.vue | 无版本历史 | 无法查看修改记录 |
| Interact.vue | 答题无分类 | 无法按类型答题 |
| Visual.vue | 数据非实时 | 图表数据有延迟 |
| Admin.vue | 无批量导入 | 大量数据需逐条添加 |
| PersonalCenter.vue | 无数据导出 | 无法导出个人数据 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **搜索功能增强**
   - 添加拼音搜索支持
   - 实现搜索建议
   - 添加搜索历史

2. **交互优化**
   - 添加骨架屏加载
   - 实现无限滚动
   - 添加快捷键支持

3. **性能优化**
   - 页面懒加载
   - 图片懒加载
   - 组件缓存

### 中期改进 (1-2月)

1. **功能增强**
   - 数据导出功能
   - 批量操作支持
   - 高级筛选

2. **用户体验**
   - 主题切换
   - 国际化支持
   - 无障碍访问

3. **移动端适配**
   - 响应式优化
   - 触摸手势
   - PWA支持

### 长期规划 (3-6月)

1. **微前端架构**
   - 模块独立部署
   - 动态加载

2. **SSR支持**
   - 服务端渲染
   - SEO优化

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 框架基础 |
| Vue Router | 4.2+ | 路由管理 |
| Pinia | 2.3+ | 状态管理 |
| Element Plus | 2.4+ | UI组件 |
| ECharts | 5.4+ | 图表展示 |

---

## 常见问题

### 1. 如何添加新页面？

```javascript
// 1. 创建页面组件
// views/NewPage.vue

// 2. 添加路由
const routes = [
  {
    path: '/new-page',
    name: 'NewPage',
    component: () => import('@/views/NewPage.vue')
  }
]

// 3. 添加导航
// 在AppHeader.vue中添加菜单项
```

### 2. 如何实现页面缓存？

```vue
<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="['Plants', 'Knowledge']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>
```

### 3. 如何处理页面加载状态？

```vue
<script setup>
import { ref } from 'vue'
import { useLoading } from '@/composables/useLoading'

const { loading, withLoading } = useLoading()

const fetchData = withLoading(async () => {
  // 异步操作
})
</script>

<template>
  <el-skeleton v-if="loading" :rows="5" animated />
  <div v-else>
    <!-- 内容 -->
  </div>
</template>
```

### 4. 如何实现页面间通信？

```javascript
// 方式1：通过Pinia Store
const useDataStore = defineStore('data', {
  state: () => ({ items: [] }),
  actions: {
    setItems(items) { this.items = items }
  }
})

// 方式2：通过路由参数
router.push({ path: '/detail', query: { id: 1 } })

// 方式3：通过事件总线
import { useEventBus } from '@/composables/useEventBus'
const bus = useEventBus()
bus.emit('data-updated', data)
```

### 5. 如何处理页面错误？

```vue
<script setup>
import { onErrorCaptured, ref } from 'vue'

const error = ref(null)

onErrorCaptured((err) => {
  error.value = err
  return false  // 阻止错误继续传播
})
</script>

<template>
  <ErrorBoundary v-if="error" :error="error" />
  <slot v-else />
</template>
```

### 6. 如何优化页面性能？

```vue
<script setup>
// 1. 组件懒加载
const HeavyComponent = defineAsyncComponent(() => 
  import('@/components/HeavyComponent.vue')
)

// 2. 数据分页加载
const loadMore = async () => {
  if (loading.value || !hasMore.value) return
  page.value++
  await fetchData()
}

// 3. 虚拟滚动
import VirtualList from '@/components/base/VirtualList.vue'
</script>
```

---

**最后更新时间**: 2026年3月28日
