# 基础组件目录 (base)

本目录存放最底层的通用组件，不包含业务逻辑，只提供基础功能。

## 目录

- [什么是基础组件？](#什么是基础组件)
- [组件列表](#组件列表)
- [组件详解](#组件详解)

---

## 什么是基础组件？

**基础组件**是最底层的组件，它们：
- 不包含任何业务逻辑
- 可在任何项目中复用
- 提供通用的UI功能

```
┌─────────────────────────────────────────────────────────────────┐
│                     组件层级关系                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  页面组件 (views/)                                              │
│       │                                                         │
│       └──→ 业务组件 (components/business/)                      │
│                  │                                              │
│                  └──→ 基础组件 (components/base/)               │
│                                                                 │
│  基础组件是最底层，被其他组件调用                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| ErrorBoundary.vue | 错误边界组件，捕获子组件错误 |
| VirtualList.vue | 虚拟列表组件，优化大数据渲染 |
| index.js | 组件导出入口 |

---

## 组件详解

### ErrorBoundary.vue - 错误边界

捕获子组件的错误，显示备用UI，防止整个页面崩溃。

```vue
<template>
  <slot v-if="!hasError" />
  <div v-else class="error-boundary">
    <p>出错了，请刷新页面重试</p>
    <button @click="resetError">重试</button>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'

const hasError = ref(false)
const error = ref(null)

// 捕获子组件错误
onErrorCaptured((err) => {
  hasError.value = true
  error.value = err
  console.error('ErrorBoundary捕获错误:', err)
  return false  // 阻止错误继续向上传播
})

const resetError = () => {
  hasError.value = false
  error.value = null
}
</script>
```

**使用示例：**

```vue
<template>
  <ErrorBoundary>
    <!-- 如果这里出错，不会导致整个页面崩溃 -->
    <MyComponent />
  </ErrorBoundary>
</template>
```

### VirtualList.vue - 虚拟列表

只渲染可视区域内的元素，优化大数据列表的性能。

```vue
<template>
  <div class="virtual-list" @scroll="handleScroll" ref="containerRef">
    <div class="virtual-list-phantom" :style="{ height: totalHeight + 'px' }" />
    <div class="virtual-list-content" :style="{ transform: `translateY(${offset}px)` }">
      <div
        v-for="item in visibleItems"
        :key="item.id"
        class="virtual-list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item.data" :index="item.index" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  items: { type: Array, required: true },      // 所有数据
  itemHeight: { type: Number, default: 50 },   // 每项高度
  buffer: { type: Number, default: 5 }         // 缓冲数量
})

const containerRef = ref(null)
const containerHeight = ref(0)
const scrollTop = ref(0)

// 总高度
const totalHeight = computed(() => props.items.length * props.itemHeight)

// 可视区域能显示的数量
const visibleCount = computed(() => 
  Math.ceil(containerHeight.value / props.itemHeight) + props.buffer * 2
)

// 起始索引
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight) - props.buffer
  return Math.max(0, index)
})

// 结束索引
const endIndex = computed(() => 
  Math.min(props.items.length, startIndex.value + visibleCount.value)
)

// 偏移量
const offset = computed(() => startIndex.value * props.itemHeight)

// 可视项
const visibleItems = computed(() => 
  props.items.slice(startIndex.value, endIndex.value).map((data, i) => ({
    data,
    index: startIndex.value + i,
    id: startIndex.value + i
  }))
)

const handleScroll = (e) => {
  scrollTop.value = e.target.scrollTop
}

onMounted(() => {
  containerHeight.value = containerRef.value.clientHeight
})
</script>
```

**使用示例：**

```vue
<template>
  <!-- 渲染10000条数据，只渲染可视区域的几十条 -->
  <VirtualList :items="plants" :item-height="200">
    <template #default="{ item }">
      <PlantCard :plant="item" />
    </template>
  </VirtualList>
</template>

<script setup>
import VirtualList from '@/components/base/VirtualList.vue'

const plants = ref([...]) // 10000条数据
</script>
```

---

## 最佳实践

### 1. 基础组件应该足够通用

```vue
<!-- ✅ 好的做法：通用、可配置 -->
<VirtualList :items="data" :item-height="50">
  <template #default="{ item }">
    <!-- 使用者决定如何渲染 -->
  </template>
</VirtualList>

<!-- ❌ 不好的做法：绑定具体业务 -->
<PlantVirtualList :plants="data" />
```

### 2. 使用插槽提供灵活性

```vue
<!-- 基础组件通过插槽让使用者自定义内容 -->
<template>
  <div class="base-component">
    <slot name="header" />
    <slot />  <!-- 默认插槽 -->
    <slot name="footer" />
  </div>
</template>
```

---

**最后更新时间**：2026年4月3日
