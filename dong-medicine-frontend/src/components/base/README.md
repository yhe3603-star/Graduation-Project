# 基础组件（Base Components）

## 什么是基础组件？

类比：**砖块和水泥**——它们是最基础的材料，没有任何装饰，但所有建筑都离不开它们。

基础组件是项目中最底层的功能组件，它们：
- **不涉及任何业务逻辑**（不知道什么是"侗医药"）
- **只提供通用技术能力**（错误捕获、性能优化等）
- **被其他所有层级的组件依赖**

```
基础组件就像水电管道——你看不到它们，但没有它们房子就没法住
```

---

## ErrorBoundary.vue —— 错误边界

### 什么是错误边界？

类比：**防火墙**——当某个房间着火时，防火墙能阻止火势蔓延到其他房间。

在 Vue 中，如果一个组件报错了，默认情况下整个页面都会崩溃白屏。错误边界就像"防火墙"，把错误限制在出问题的组件内部，不让它影响其他组件。

### 没有错误边界 vs 有错误边界

```
没有错误边界：
┌────────────────────────────────────┐
│  头部 ✅                            │
│  ┌────────────┐  ┌────────────┐   │
│  │ 植物卡片 💥 │  │ 知识卡片 💥 │   │  ← 一个报错，全部崩溃
│  └────────────┘  └────────────┘   │
│  底部 💥                           │
└────────────────────────────────────┘

有错误边界：
┌────────────────────────────────────┐
│  头部 ✅                            │
│  ┌────────────┐  ┌────────────┐   │
│  │ 植物卡片 💥 │  │ 知识卡片 ✅ │   │  ← 只有出错的组件显示错误提示
│  │ 显示：出错了 │  │ 正常显示    │   │
│  └────────────┘  └────────────┘   │
│  底部 ✅                           │
└────────────────────────────────────┘
```

### 工作原理

```vue
<!-- ErrorBoundary.vue 的核心逻辑 -->
<script setup>
import { ref, onErrorCaptured } from 'vue'

// 是否捕获到错误
const hasError = ref(false)
// 错误信息
const errorMsg = ref('')

// onErrorCaptured 是 Vue 提供的生命周期钩子
// 当子组件抛出错误时，这个函数会被调用
onErrorCaptured((err, instance, info) => {
  hasError.value = true
  errorMsg.value = err.message
  // 返回 false 阻止错误继续向上传播（阻止"火势蔓延"）
  return false
})
</script>

<template>
  <!-- 如果子组件报错了，显示错误提示 -->
  <div v-if="hasError" class="error-fallback">
    <p>这个区域出了点问题：{{ errorMsg }}</p>
    <button @click="hasError = false">重试</button>
  </div>

  <!-- 没报错就正常显示子组件 -->
  <slot v-else />
</template>
```

### 使用方式

```vue
<template>
  <!-- 用 ErrorBoundary 包裹可能出错的组件 -->
  <ErrorBoundary>
    <PlantCard :data="plantData" />
  </ErrorBoundary>

  <!-- 即使 PlantCard 报错了，下面的内容也不受影响 -->
  <KnowledgeCard :data="knowledgeData" />
</template>
```

---

## VirtualList.vue —— 虚拟列表

### 什么是虚拟列表？

类比：**只铺你看得到的地砖**——你有一个超大的房间，但每次只能看到眼前几平方米，那就只铺这几平方米的地砖，其他的等你走过去再铺。

当列表有上千条数据时，如果全部渲染成 DOM 节点，浏览器会非常卡顿。虚拟列表只渲染**当前可见区域**的元素，滚动时动态替换，就像只铺看得见的地砖。

### 性能对比

```
普通列表（渲染全部 10000 条）：
┌──────────────────┐
│  第 1 条    ✅可见 │
│  第 2 条    ✅可见 │
│  第 3 条    ✅可见 │
│  第 4 条    ✅可见 │
│  第 5 条    ✅可见 │
│  ...              │
│  第 9999 条 ❌看不见也要渲染 │
│  第 10000 条 ❌看不见也要渲染 │
└──────────────────┘
DOM 节点数：10000 个 😱 页面卡顿！

虚拟列表（只渲染可见的 5 条）：
┌──────────────────┐
│  第 1 条    ✅渲染 │
│  第 2 条    ✅渲染 │
│  第 3 条    ✅渲染 │
│  第 4 条    ✅渲染 │
│  第 5 条    ✅渲染 │
│  （空白占位）      │  ← 用 CSS 撑高度，不渲染真实 DOM
│  （空白占位）      │
└──────────────────┘
DOM 节点数：5 个 😊 丝滑流畅！
```

### 工作原理

```vue
<!-- VirtualList.vue 简化版核心逻辑 -->
<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  items: { type: Array, required: true },   // 全部数据
  itemHeight: { type: Number, default: 50 }, // 每项高度（像素）
  visibleCount: { type: Number, default: 10 } // 可见数量
})

// 当前滚动位置
const scrollTop = ref(0)

// 计算应该从第几条开始渲染
const startIndex = computed(() => {
  return Math.floor(scrollTop.value / props.itemHeight)
})

// 计算渲染到第几条结束
const endIndex = computed(() => {
  return startIndex.value + props.visibleCount
})

// 实际渲染的数据（只是全部数据的一小片）
const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value)
})

// 滚动事件处理
const onScroll = (e) => {
  scrollTop.value = e.target.scrollTop
}
</script>

<template>
  <div class="virtual-list" @scroll="onScroll">
    <!-- 撑开总高度，让滚动条出现 -->
    <div
      class="virtual-list-spacer"
      :style="{ height: items.length * itemHeight + 'px' }"
    />
    <!-- 只渲染可见的项 -->
    <div
      class="virtual-list-content"
      :style="{ transform: `translateY(${startIndex * itemHeight}px)` }"
    >
      <div
        v-for="item in visibleItems"
        :key="item.id"
        class="virtual-list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item" />
      </div>
    </div>
  </div>
</template>
```

### 使用方式

```vue
<template>
  <!-- 药用植物列表有 5000 条，用虚拟列表保证流畅 -->
  <VirtualList :items="allPlants" :item-height="80" :visible-count="8">
    <!-- 用作用域插槽自定义每一项的显示 -->
    <template #default="{ item }">
      <div class="plant-item">
        <img :src="item.image" :alt="item.name" />
        <span>{{ item.name }}（{{ item.dongName }}）</span>
      </div>
    </template>
  </VirtualList>
</template>
```

---

## 常见错误

### 错误1：onErrorCaptured 写错位置

```vue
<script setup>
// ❌ onErrorCaptured 不是在普通函数里调用的，必须在 setup 顶层调用
function handleClick() {
  onErrorCaptured(() => {})  // 错误！这是生命周期钩子，不能在事件处理函数里调用
}

// ✅ 必须在 setup 顶层直接调用
onErrorCaptured((err) => {
  console.error('捕获到错误：', err)
  return false
})
</script>
```

### 错误2：虚拟列表没有设置固定高度

```vue
<template>
  <!-- ❌ 虚拟列表容器必须有固定高度，否则无法计算可见区域 -->
  <VirtualList :items="data" />

  <!-- ✅ 给容器设置固定高度 -->
  <VirtualList :items="data" style="height: 500px; overflow-y: auto;" />
</template>
```

---

## 代码审查与改进建议

- [逻辑] VirtualList.vue中展开item添加_index可能覆盖原始数据中的同名属性，应使用Symbol或不可枚举属性
