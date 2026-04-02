# 页面组件目录 (views)

本目录存放页面级组件，每个组件对应一个路由页面。

## 目录

- [什么是页面组件？](#什么是页面组件)
- [目录结构](#目录结构)
- [页面列表](#页面列表)
- [页面开发规范](#页面开发规范)

---

## 什么是页面组件？

### 页面组件的概念

**页面组件**是构成网站页面的主要组件，每个页面组件对应一个URL路由。它就像一本书的"章节"——每个章节（页面）有独立的内容，读者（用户）可以通过目录（路由）跳转到不同章节。

### 页面组件与普通组件的区别

```
┌─────────────────────────────────────────────────────────────────┐
│                    页面组件 vs 普通组件                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  页面组件 (views/)                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  - 对应一个完整的页面                                    │   │
│  │  - 有对应的URL路由                                       │   │
│  │  - 包含页面级的业务逻辑                                  │   │
│  │  - 通常由多个普通组件组成                                │   │
│  │  - 示例：Home.vue, Plants.vue                           │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  普通组件 (components/)                                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  - 可复用的UI单元                                        │   │
│  │  - 没有对应的URL路由                                     │   │
│  │  - 包含组件级的逻辑                                      │   │
│  │  - 被页面组件引用                                        │   │
│  │  - 示例：CardGrid.vue, Pagination.vue                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
views/
│
├── Home.vue                           # 首页
├── Plants.vue                         # 药用植物页面
├── Inheritors.vue                     # 传承人页面
├── Knowledge.vue                      # 知识库页面
├── Qa.vue                             # 问答社区页面
├── Resources.vue                      # 学习资源页面
├── Interact.vue                       # 互动专区页面
├── Visual.vue                         # 数据可视化页面
├── PersonalCenter.vue                 # 个人中心页面
├── Admin.vue                          # 管理后台页面
├── About.vue                          # 关于页面
├── Feedback.vue                       # 意见反馈页面
├── GlobalSearch.vue                   # 全局搜索页面
└── NotFound.vue                       # 404页面
```

---

## 页面列表

| 页面 | 路由 | 功能描述 | 权限 |
|------|------|----------|------|
| Home.vue | `/` | 首页，展示平台核心功能入口、统计数据、传承人风采 | 公开 |
| Plants.vue | `/plants` | 药用植物图鉴，支持分类筛选、搜索、收藏 | 公开 |
| Inheritors.vue | `/inheritors` | 传承人风采展示，按级别筛选 | 公开 |
| Knowledge.vue | `/knowledge` | 非遗医药知识库，支持分类检索、搜索过滤 | 公开 |
| Qa.vue | `/qa` | 问答社区，侗医药知识问答 | 公开 |
| Interact.vue | `/interact` | 文化互动专区，趣味答题、植物识别游戏 | 公开 |
| Resources.vue | `/resources` | 学习资源库，支持预览、下载、收藏 | 公开 |
| Visual.vue | `/visual` | 数据可视化，统计图表展示 | 公开 |
| PersonalCenter.vue | `/personal` | 个人中心，管理收藏、答题记录、账号设置 | 需登录 |
| Admin.vue | `/admin` | 管理后台，数据管理、用户管理、评论审核 | 管理员 |
| About.vue | `/about` | 关于页面，介绍平台和侗医文化 | 公开 |
| Feedback.vue | `/feedback` | 意见反馈，用户提交功能建议 | 公开 |
| GlobalSearch.vue | `/search` | 全局搜索，跨模块统一搜索 | 公开 |
| NotFound.vue | `*` | 404页面，页面不存在时显示 | 公开 |

---

## 页面开发规范

### 1. 页面基本结构

```vue
<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <header class="page-header">
      <h1>{{ pageTitle }}</h1>
    </header>
    
    <!-- 主要内容区 -->
    <main class="page-content">
      <!-- 加载状态 -->
      <SkeletonGrid v-if="loading" />
      
      <!-- 内容展示 -->
      <template v-else>
        <!-- 内容组件 -->
      </template>
    </main>
    
    <!-- 侧边栏（可选） -->
    <aside class="page-sidebar">
      <!-- 侧边栏内容 -->
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 页面数据
const loading = ref(true)
const data = ref([])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 获取数据...
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
  /* 页面样式 */
}
</style>
```

### 2. 页面命名规范

```javascript
// 页面文件名使用 PascalCase
Home.vue
Plants.vue
PersonalCenter.vue

// 路由 name 与文件名一致
{
  path: '/personal',
  name: 'PersonalCenter',
  component: () => import('@/views/PersonalCenter.vue')
}
```

### 3. 页面组件拆分

```vue
<template>
  <div class="plants-page">
    <!-- 拆分为独立组件 -->
    <SearchFilter 
      v-model:keyword="keyword"
      v-model:category="category"
      @search="handleSearch"
    />
    
    <SkeletonGridImage v-if="loading" :count="12" />
    
    <CardGrid v-else :items="plants" @item-click="handleItemClick" />
    
    <Pagination 
      v-model:current="page"
      :total="total"
      :page-size="pageSize"
    />
    
    <PlantDetailDialog 
      v-model:visible="dialogVisible"
      :plant="selectedPlant"
    />
  </div>
</template>

<script setup>
import SearchFilter from '@/components/business/display/SearchFilter.vue'
import CardGrid from '@/components/business/display/CardGrid.vue'
import Pagination from '@/components/business/display/Pagination.vue'
import PlantDetailDialog from '@/components/business/dialogs/PlantDetailDialog.vue'
import SkeletonGridImage from '@/components/common/SkeletonGridImage.vue'

// 页面逻辑...
</script>
```

---

## 最佳实践

### 1. 页面职责清晰

```javascript
// 页面只负责：
// 1. 数据获取和管理
// 2. 页面级状态
// 3. 组件组合
// 4. 路由相关逻辑

// 不要在页面中写：
// 1. 可复用的UI组件
// 2. 通用的业务逻辑（应该放在composables）
```

### 2. 使用组合式函数

```vue
<script setup>
import { usePlants } from '@/composables/usePlants'
import { useFavorite } from '@/composables/useFavorite'

// 使用组合式函数封装逻辑
const { plants, loading, fetchPlants } = usePlants()
const { isFavorited, toggleFavorite } = useFavorite('plant')

onMounted(() => {
  fetchPlants()
})
</script>
```

### 3. 骨架屏加载

```vue
<template>
  <div>
    <!-- 加载中显示骨架屏 -->
    <SkeletonGridImage v-if="loading" :count="12" />
    
    <!-- 加载完成显示内容 -->
    <CardGrid v-else :items="plants" />
  </div>
</template>
```

---

**最后更新时间**：2026年4月3日
