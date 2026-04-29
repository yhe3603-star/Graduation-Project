# 组件（Components）

## 什么是组件？

想象一下**乐高积木**——每一块积木都有固定的形状和功能，你可以把它们拼在一起，搭出城堡、汽车、飞船等各种造型。

Vue 组件就是前端世界的"乐高积木"。每个组件封装了一小块界面（HTML）、样式（CSS）和逻辑（JS），然后像搭积木一样组合出完整的页面。

```
一个页面 = 多个组件拼在一起

┌──────────────────────────────────────┐
│              页面（Page）              │
│  ┌──────────┐  ┌──────────┐  ┌────┐ │
│  │ 组件 A   │  │ 组件 B   │  │组件│ │
│  │ (头部)   │  │ (内容区) │  │ C  │ │
│  └──────────┘  └──────────┘  └────┘ │
└──────────────────────────────────────┘
```

---

## 组件的三层架构

我们项目中的组件分为三层，从底层到高层依次是：

```
┌─────────────────────────────────────────────┐
│           business（业务组件层）              │
│   侗医药专属组件：药用植物卡片、知识问答等      │
│  ┌───────────────────────────────────────┐  │
│  │         common（通用组件层）            │  │
│  │   通用UI组件：骨架屏、加载动画等         │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │      base（基础组件层）          │  │  │
│  │  │   最底层工具：错误边界、虚拟列表   │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

| 层级 | 作用 | 类比 | 例子 |
|------|------|------|------|
| **base** | 最底层的功能组件，不涉及任何业务 | 砖块和水泥，最基础的材料 | ErrorBoundary、VirtualList |
| **common** | 通用UI组件，任何页面都能复用 | 通用家具，每个房间都能用的桌椅 | Skeleton、PageLoading |
| **business** | 业务组件，和侗医药功能绑定 | 专用设备，比如咖啡机只能在咖啡区用 | PlantCard、QuizSection |

**依赖规则：高层可以引用低层，低层不能引用高层。**

```
business ──→ common ──→ base    ✅ 正确
base ──→ business               ❌ 禁止
```

---

## 如何导入和使用组件

### 第一步：导入组件

```vue
<script setup>
// 用 import 导入组件，路径要写对
import AppHeader from '@/components/business/layout/AppHeader.vue'
import SkeletonCard from '@/components/common/SkeletonCard.vue'
</script>
```

### 第二步：在模板中使用

```vue
<template>
  <!-- 导入后直接当标签用，像搭积木一样 -->
  <AppHeader title="侗乡医药" />
  <SkeletonCard :loading="isLoading" />
</template>
```

### 完整示例

```vue
<!-- HomeView.vue -->
<script setup>
import { ref } from 'vue'
// 导入需要的组件
import AppHeader from '@/components/business/layout/AppHeader.vue'
import PlantCard from '@/components/business/display/CardGrid.vue'
import PageLoading from '@/components/common/PageLoading.vue'

// 模拟数据加载状态
const isLoading = ref(true)
const plants = ref([
  { id: 1, name: '钩藤', dongName: 'gons jinl' },
  { id: 2, name: '透骨草', dongName: 'touc gus caoc' }
])

// 2秒后加载完成
setTimeout(() => {
  isLoading.value = false
}, 2000)
</script>

<template>
  <div class="home">
    <AppHeader />

    <!-- 加载中显示loading，加载完显示内容 -->
    <PageLoading v-if="isLoading" />

    <div v-else class="plant-list">
      <PlantCard
        v-for="plant in plants"
        :key="plant.id"
        :data="plant"
      />
    </div>
  </div>
</template>
```

---

## 组件命名规范

### 1. 文件名使用 PascalCase（大驼峰）

```
✅ 正确：PlantCard.vue、SearchFilter.vue、AppHeader.vue
❌ 错误：plantCard.vue、search_filter.vue、appheader.vue
```

### 2. 组件名必须是多单词

Vue 官方要求组件名至少两个单词，避免和 HTML 原生标签冲突：

```
✅ 正确：PlantCard、UserAvatar、SearchBar
❌ 错误：Card、Avatar、Bar（单单词会和 <card>、<bar> 等原生标签冲突）
```

### 3. 目录名使用 kebab-case（短横线）

```
✅ 正确：display/、admin-forms/、media/
❌ 错误：Display/、AdminForms/、MEDIA/
```

---

## 常见错误

### 错误1：导入路径写错

```vue
<script setup>
// ❌ 路径大小写错误，Windows可能不报错，Linux会404
import PlantCard from '@/components/business/display/plantcard.vue'

// ✅ 路径必须和文件名完全一致
import PlantCard from '@/components/business/display/PlantCard.vue'
</script>
```

### 错误2：忘记注册组件

```vue
<script setup>
// 使用 <script setup> 时，import 的组件自动注册，不需要手动注册
// 但如果你用的是 Options API（不推荐），就需要手动注册：

// ❌ Options API 忘记注册
export default {
  // 缺少 components: { AppHeader }
}

// ✅ Options API 正确写法
import AppHeader from '@/components/business/layout/AppHeader.vue'
export default {
  components: { AppHeader }  // 必须在这里注册
}
</script>
```

### 错误3：组件名和原生标签冲突

```vue
<template>
  <!-- ❌ <header> 是HTML原生标签，Vue不知道你用的是组件还是原生标签 -->
  <header />

  <!-- ✅ 用多单词命名避免冲突 -->
  <AppHeader />
</template>
```

---

## 代码审查与改进建议

- [代码重复] 上传组件重复逻辑应抽取为composable
- [代码重复] PlantGame和QuizSection重复UI结构应抽取公共组件
- [可访问性] 多处使用div+@click缺少键盘交互支持
