# 通用组件目录 (common)

本目录存放项目中多处使用的功能性组件，如加载动画、骨架屏等。

## 目录

- [什么是通用组件？](#什么是通用组件)
- [组件列表](#组件列表)
- [组件详解](#组件详解)

---

## 什么是通用组件？

**通用组件**是项目中多个页面都会使用的功能性组件。它们：
- 提供通用的UI功能
- 可在多个页面复用
- 不绑定具体业务

```
┌─────────────────────────────────────────────────────────────────┐
│                    通用组件使用场景                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  首页 ────┐                                                     │
│  植物页 ──┼──→ PageLoading.vue (页面加载动画)                   │
│  知识页 ──┤                                                     │
│  ...     ─┘                                                     │
│                                                                 │
│  植物页 ──┐                                                     │
│  知识页 ──┼──→ SkeletonGridCard.vue (卡片骨架屏)                │
│  传承人页 ─┤                                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| PageLoading.vue | 页面加载动画，路由切换时显示 |
| SkeletonGridCard.vue | 卡片骨架屏，卡片列表加载时显示 |
| SkeletonGridImage.vue | 图片骨架屏，图片列表加载时显示 |
| SkeletonListQa.vue | 问答骨架屏，问答列表加载时显示 |
| SkeletonListResource.vue | 资源骨架屏，资源列表加载时显示 |

---

## 组件详解

### PageLoading.vue - 页面加载动画

路由切换时显示的加载动画，提升用户体验。

```vue
<template>
  <Transition name="page-loading">
    <div v-if="visible" class="page-loading-overlay">
      <div class="page-loading-content">
        <div class="loading-spinner">
          <div class="spinner-ring" />
          <div class="spinner-ring ring-delay" />
        </div>
        <div class="loading-text">
          <span>加载中</span>
          <span class="loading-dots">
            <i /><i /><i />
          </span>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.page-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: #1A5276;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.ring-delay {
  border-top-color: #28B463;
  animation-delay: 0.3s;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
```

**使用示例：**

```vue
<!-- App.vue -->
<template>
  <PageLoading :visible="pageLoading" />
  <router-view />
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageLoading from '@/components/common/PageLoading.vue'

const pageLoading = ref(false)
const router = useRouter()

router.beforeEach((to, from, next) => {
  pageLoading.value = true
  next()
})

router.afterEach(() => {
  setTimeout(() => {
    pageLoading.value = false
  }, 100)
})
</script>
```

### 骨架屏组件

骨架屏是在数据加载时显示的占位内容，让用户知道内容即将出现。

#### SkeletonGridImage.vue - 图片骨架屏

用于图片网格布局的骨架屏（如植物页面）。

```vue
<template>
  <div class="skeleton-grid-image">
    <div v-for="i in count" :key="i" class="skeleton-item">
      <div class="skeleton-image" />
      <div class="skeleton-title" />
      <div class="skeleton-text" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 12
  }
})
</script>

<style scoped>
.skeleton-grid-image {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.skeleton-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-title {
  height: 20px;
  margin: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.skeleton-text {
  height: 14px;
  margin: 0 12px 12px;
  width: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
```

#### SkeletonGridCard.vue - 卡片骨架屏

用于卡片网格布局的骨架屏（如知识库、传承人页面）。

```vue
<template>
  <div class="skeleton-grid-card">
    <div v-for="i in count" :key="i" class="skeleton-card">
      <div class="skeleton-header" />
      <div class="skeleton-body">
        <div class="skeleton-line" />
        <div class="skeleton-line short" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 12
  }
})
</script>
```

#### SkeletonListQa.vue - 问答骨架屏

用于问答列表的骨架屏。

#### SkeletonListResource.vue - 资源骨架屏

用于学习资源列表的骨架屏。

---

## 使用示例

### 页面中使用骨架屏

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

---

## 最佳实践

### 1. 骨架屏布局应与实际内容一致

```vue
<!-- ✅ 好的做法：骨架屏布局与实际内容一致 -->
<SkeletonGridImage v-if="loading" />
<CardGrid v-else :items="plants" />

<!-- ❌ 不好的做法：骨架屏布局与实际内容不一致 -->
<SkeletonList v-if="loading" />  <!-- 列表布局 -->
<CardGrid v-else :items="plants" />  <!-- 网格布局 -->
```

### 2. 根据页面类型选择合适的骨架屏

| 页面类型 | 推荐骨架屏 |
|---------|-----------|
| 植物页面（有图片） | SkeletonGridImage |
| 知识库/传承人（卡片） | SkeletonGridCard |
| 问答页面（列表） | SkeletonListQa |
| 资源页面（列表） | SkeletonListResource |

---

**最后更新时间**：2026年4月3日
