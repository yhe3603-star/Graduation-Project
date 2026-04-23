# 通用组件（Common Components）

## 什么是通用组件？

类比：**通用家具**——桌子、椅子、书架，每个房间都能用，不挑地方。

通用组件是可以在任何页面、任何业务场景中复用的 UI 组件。它们：
- **不包含业务逻辑**（不知道什么是"药用植物"或"知识问答"）
- **只负责通用的 UI 展示**（加载状态、占位效果等）
- **通过 props 接收数据，通过 slots 自定义内容**

```
通用组件就像插座——不管你插台灯还是插手机充电器，插座都是一样的
```

---

## 骨架屏组件（Skeleton）

### 什么是骨架屏？

类比：**建筑框架**——盖楼时先搭好钢筋框架，你一眼就能看出这栋楼的大致形状，等装修完再看到真实的样子。

当页面数据还在加载时，先用灰色的占位块勾勒出内容的大致轮廓，让用户知道"内容马上就来"，而不是盯着空白页面发呆。

### 没有骨架屏 vs 有骨架屏

```
没有骨架屏：
┌──────────────────┐
│                  │  ← 白屏等待，用户不知道在加载什么
│     （空白）      │     以为页面坏了，直接关闭 😱
│                  │
└──────────────────┘

有骨架屏：
┌──────────────────┐
│ ████████████     │  ← 灰色占位块，用户知道内容即将出现
│ ██████           │     感觉页面很快，愿意等待 😊
│ ████████████████ │
└──────────────────┘
```

### 为什么骨架屏能提升用户体验？

1. **减少感知等待时间**——用户看到"正在加载"的视觉反馈，心理上觉得更快
2. **避免布局抖动**——内容加载完后页面不会突然跳动
3. **降低跳出率**——空白页面让用户以为出错了，骨架屏告诉用户"等一下就好"

### 骨架屏组件列表

| 组件名 | 用途 | 示例场景 |
|--------|------|----------|
| `SkeletonCard` | 卡片类内容的骨架 | 药用植物卡片加载中 |
| `SkeletonTable` | 表格类内容的骨架 | 后台数据表格加载中 |
| `SkeletonText` | 文本类内容的骨架 | 文章详情加载中 |

### 使用示例

```vue
<script setup>
import { ref } from 'vue'
import SkeletonCard from '@/components/common/SkeletonCard.vue'
import PlantCard from '@/components/business/display/CardGrid.vue'

const isLoading = ref(true)
const plantData = ref(null)

// 模拟数据加载
setTimeout(() => {
  plantData.value = { name: '钩藤', dongName: 'gons jinl' }
  isLoading.value = false
}, 1500)
</script>

<template>
  <!-- 加载中显示骨架屏，加载完显示真实内容 -->
  <SkeletonCard v-if="isLoading" />
  <PlantCard v-else :data="plantData" />
</template>
```

---

## PageLoading.vue —— 页面加载动画

### 什么是页面加载动画？

类比：**餐厅门口的"请稍候"指示牌**——服务员正在准备你的菜，先给你个提示让你安心等待。

当整个页面都在加载时（比如路由切换、首次进入），显示一个全屏的加载动画，告诉用户"页面正在准备中"。

### 和骨架屏的区别

```
骨架屏（Skeleton）          页面加载动画（PageLoading）
┌──────────────────┐      ┌──────────────────┐
│ ████████████     │      │                  │
│ ██████           │      │     🔄 转圈圈     │
│ ████████████████ │      │   页面加载中...    │
└──────────────────┘      └──────────────────┘
局部加载，保留页面结构      全屏加载，遮住整个页面
用于：卡片、列表等局部区域   用于：整页切换、首次加载
```

### 使用示例

```vue
<script setup>
import { ref } from 'vue'
import PageLoading from '@/components/common/PageLoading.vue'

const pageLoading = ref(true)

// 页面数据全部加载完成后关闭
setTimeout(() => {
  pageLoading.value = false
}, 2000)
</script>

<template>
  <!-- 全屏加载动画 -->
  <PageLoading v-if="pageLoading" />

  <!-- 页面真实内容 -->
  <div v-else class="page-content">
    <h1>侗乡医药数字展示平台</h1>
    <!-- ... -->
  </div>
</template>
```

---

## 常见错误

### 错误1：骨架屏和真实内容的尺寸不一致

```vue
<template>
  <!-- ❌ 骨架屏高度100px，真实内容高度200px，加载完后页面会跳动 -->
  <SkeletonCard v-if="isLoading" style="height: 100px" />
  <PlantCard v-else :data="plantData" />  <!-- 实际高度200px -->

  <!-- ✅ 骨架屏和真实内容保持相同尺寸 -->
  <SkeletonCard v-if="isLoading" style="height: 200px" />
  <PlantCard v-else :data="plantData" />
</template>
```

### 错误2：PageLoading 忘记关闭

```vue
<script setup>
import { ref } from 'vue'
import PageLoading from '@/components/common/PageLoading.vue'

const pageLoading = ref(true)

// ❌ 忘记在数据加载完后设置 pageLoading = false
// 结果：页面永远显示加载动画，用户永远看不到内容！

// ✅ 一定要在数据加载完后关闭 loading
const fetchData = async () => {
  try {
    const res = await api.getPlants()
    // 数据加载完，关闭 loading
    pageLoading.value = false
  } catch (err) {
    // 即使出错了也要关闭 loading，否则用户永远卡在加载页
    pageLoading.value = false
  }
}
</script>
```

### 错误3：在骨架屏中写死文字

```vue
<template>
  <!-- ❌ 骨架屏不应该出现真实文字，它只是占位 -->
  <div class="skeleton">
    <p>钩藤</p>  <!-- 不应该出现具体内容 -->
  </div>

  <!-- ✅ 骨架屏用灰色块占位 -->
  <div class="skeleton">
    <div class="skeleton-line" />  <!-- 灰色横线占位 -->
  </div>
</template>
```
