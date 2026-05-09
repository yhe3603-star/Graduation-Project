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

## 目录结构

```
base/
├── ErrorBoundary.vue   错误边界组件
├── VirtualList.vue     虚拟滚动列表组件
├── index.js            统一导出（仅导出 ErrorBoundary）
└── README.md           本文件
```

> **注意：** `index.js` 当前仅导出 `ErrorBoundary`，`VirtualList` 需直接引用 `.vue` 文件使用。

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

### Props

无。通过默认 slot 包裹子组件。

### 内部状态

| 状态 | 类型 | 说明 |
|------|------|------|
| `hasError` | `ref(Boolean)` | 是否捕获到错误 |
| `errorMessage` | `ref(String)` | 错误消息文本 |
| `errorStack` | `ref(String)` | 完整错误调用栈 |
| `showDetails` | `ref(Boolean)` | 是否展开错误详情 |
| `isDev` | `Boolean` | 是否开发环境（`import.meta.env.DEV`） |

### 工作原理

```vue
<!-- ErrorBoundary.vue 的核心逻辑 -->
<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isDev = import.meta.env.DEV
const hasError = ref(false)
const errorMessage = ref('')
const errorStack = ref('')
const showDetails = ref(false)

// onErrorCaptured 是 Vue 提供的生命周期钩子
// 当子组件抛出错误时，这个函数会被调用
onErrorCaptured((error, instance, info) => {
  console.error('ErrorBoundary captured:', error)
  console.error('Component:', instance)
  console.error('Info:', info)

  hasError.value = true
  errorMessage.value = error.message || '未知错误'
  errorStack.value = `${error.stack || ''}\n\nComponent: ${info}`

  // 返回 false 阻止错误继续向上传播（阻止"火势蔓延"）
  return false
})

function retry() {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
  showDetails.value = false
  ElMessage.success('页面已重置')
}

function goHome() {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
  showDetails.value = false
  router.push('/')
}
</script>

<template>
  <!-- 如果子组件报错了，显示错误提示 -->
  <div v-if="hasError" class="error-fallback">
    <el-icon :size="64" color="#f56c6c"><WarningFilled /></el-icon>
    <h2>页面出错了</h2>
    <p>{{ errorMessage }}</p>
    <el-button type="primary" @click="retry">重试</el-button>
    <el-button @click="goHome">返回首页</el-button>
    <!-- 开发环境才显示错误详情 -->
    <template v-if="isDev">
      <el-collapse v-if="showDetails">
        <el-collapse-item title="错误详情" name="details">
          <pre>{{ errorStack }}</pre>
        </el-collapse-item>
      </el-collapse>
      <el-button text @click="showDetails = !showDetails">
        {{ showDetails ? '隐藏详情' : '显示详情' }}
      </el-button>
    </template>
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

### 设计要点

1. **开发/生产环境区分**：通过 `import.meta.env.DEV` 判断，仅在开发环境显示可折叠的错误详情面板（包含完整调用栈），生产环境只显示友好提示
2. **双重恢复路径**：提供"重试"按钮（重置错误状态，重新渲染子组件）和"返回首页"按钮（路由跳转到 `/`）
3. **错误信息记录**：`errorStack` 同时包含原始 `error.stack` 和 Vue 组件信息 `info`，便于调试定位
4. **控制台日志**：`onErrorCaptured` 回调中同时输出 `error`、`instance`、`info` 到控制台，方便开发者排查

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
DOM 节点数：10000 个    页面卡顿！

虚拟列表（只渲染可见的 5 条 + 缓冲区）：
┌──────────────────┐
│  第 1 条    ✅渲染 │  ← 可见区域
│  第 2 条    ✅渲染 │
│  第 3 条    ✅渲染 │
│  第 4 条    ✅渲染 │
│  第 5 条    ✅渲染 │
│  （空白占位）      │  ← phantom div 撑高度，不渲染真实 DOM
│  （空白占位）      │
└──────────────────┘
DOM 节点数：约 15 个（5 可见 + 5 上缓冲 + 5 下缓冲）   丝滑流畅！
```

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `items` | `Array` | (必填) | 全部数据列表 |
| `itemSize` | `Number` | `50` | 每项高度（像素），所有项等高 |
| `buffer` | `Number` | `5` | 可视区域上下各多渲染的缓冲项数 |
| `keyField` | `String` | `'id'` | 用作 `v-for :key` 的字段名 |

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `scroll` | `{ scrollTop, startIndex, endIndex }` | 滚动事件 |
| `visible-change` | `{ startIndex, endIndex }` | 可视范围变化 |

### Expose 方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `scrollToIndex` | `(index: Number)` | 滚动到指定索引位置 |
| `scrollToTop` | 无 | 滚动到列表顶部 |
| `updateContainerHeight` | 无 | 重新计算容器高度 |

### 核心算法

```javascript
// 1. 总高度：用 phantom div 撑开滚动区域
const totalHeight = computed(() => props.items.length * props.itemSize)

// 2. 可见项数：容器高度 / 每项高度 + 上下缓冲区
const visibleCount = computed(() => {
  return Math.ceil(containerHeight.value / props.itemSize) + props.buffer * 2
})

// 3. 起始索引：当前滚动位置 / 每项高度 - 缓冲区，最小为 0
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemSize) - props.buffer
  return Math.max(0, index)
})

// 4. 结束索引：起始索引 + 可见项数，最大为数据总长度
const endIndex = computed(() => {
  return Math.min(props.items.length, startIndex.value + visibleCount.value)
})

// 5. 偏移量：起始索引 * 每项高度，通过 translateY 定位
const offset = computed(() => {
  return startIndex.value * props.itemSize
})

// 6. 可见数据：从全部数据中切片，并附加 _index 属性
const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value).map((item, i) => ({
    ...item,
    _index: startIndex.value + i
  }))
})
```

### DOM 结构

```
┌─ .virtual-list (overflow-y: auto, 监听 scroll) ──────────┐
│                                                            │
│  ┌─ .virtual-list-phantom (absolute, z-index: -1) ──────┐ │
│  │  height: totalHeight px                               │ │
│  │  作用：撑开滚动条，让容器可以滚动                       │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌─ .virtual-list-content (absolute, translateY) ───────┐ │
│  │  transform: translateY(${offset}px)                   │ │
│  │                                                       │ │
│  │  ┌─ .virtual-list-item (height: itemSize px) ──────┐ │ │
│  │  │  <slot :item="item" :index="item._index" />      │ │ │
│  │  └─────────────────────────────────────────────────┘ │ │
│  │  ...                                                  │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### 使用方式

```vue
<template>
  <!-- 药用植物列表有 5000 条，用虚拟列表保证流畅 -->
  <VirtualList
    :items="allPlants"
    :item-size="80"
    :buffer="5"
    key-field="id"
    style="height: 500px; overflow-y: auto;"
    @scroll="onScroll"
  >
    <!-- 用作用域插槽自定义每一项的显示 -->
    <template #default="{ item, index }">
      <div class="plant-item">
        <img :src="item.image" :alt="item.name" />
        <span>{{ item.name }}（{{ item.nameDong }}）</span>
      </div>
    </template>
  </VirtualList>
</template>
```

### 生命周期管理

```javascript
onMounted(() => {
  updateContainerHeight()                        // 初始化容器高度
  window.addEventListener('resize', updateContainerHeight)  // 监听窗口变化
})

onUnmounted(() => {
  window.removeEventListener('resize', updateContainerHeight) // 清理监听
})
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

### 错误3：VirtualList 的 itemSize 与实际渲染高度不一致

```vue
<template>
  <!-- ❌ itemSize=50 但实际每项渲染高度为 80px，会导致滚动位置计算错误 -->
  <VirtualList :items="data" :item-size="50">
    <template #default="{ item }">
      <div style="height: 80px;">{{ item.name }}</div>
    </template>
  </VirtualList>

  <!-- ✅ itemSize 必须与实际渲染高度一致 -->
  <VirtualList :items="data" :item-size="80">
    <template #default="{ item }">
      <div style="height: 80px;">{{ item.name }}</div>
    </template>
  </VirtualList>
</template>
```

---

## 代码审查与改进建议

- [逻辑] `VirtualList.vue` 中展开 item 添加 `_index` 属性使用展开运算符，可能覆盖原始数据中的同名属性，应使用 `Symbol` 或 `Object.defineProperty` 定义为不可枚举属性
- [功能] `VirtualList.vue` 仅支持等高列表项（`itemSize` 为固定值），不支持动态高度场景，可考虑增加动态高度支持
- [导出] `index.js` 仅导出 `ErrorBoundary`，未导出 `VirtualList`，建议补充导出或添加注释说明原因
