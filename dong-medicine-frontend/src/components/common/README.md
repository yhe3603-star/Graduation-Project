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

## 目录结构

```
common/
├── PageLoading.vue           全页面加载动画
├── SkeletonGridCard.vue      卡片列表骨架屏
├── SkeletonGridImage.vue     图片卡片骨架屏
├── SkeletonListQa.vue        问答列表骨架屏
├── SkeletonListResource.vue  资源列表骨架屏
└── README.md                 本文件
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
│     （空白）      │     以为页面坏了，直接关闭
│                  │
└──────────────────┘

有骨架屏：
┌──────────────────┐
│ ████████████     │  ← 灰色占位块，用户知道内容即将出现
│ ██████           │     感觉页面很快，愿意等待
│ ████████████████ │
└──────────────────┘
```

### 为什么骨架屏能提升用户体验？

1. **减少感知等待时间**——用户看到"正在加载"的视觉反馈，心理上觉得更快
2. **避免布局抖动**——内容加载完后页面不会突然跳动
3. **降低跳出率**——空白页面让用户以为出错了，骨架屏告诉用户"等一下就好"

### 骨架屏组件列表

| 组件名 | 用途 | 默认数量 | 示例场景 |
|--------|------|----------|----------|
| `SkeletonGridCard` | 卡片类内容的骨架（圆形图标+标题+描述+标签） | 12 | 药用植物卡片加载中 |
| `SkeletonGridImage` | 图片卡片类内容的骨架（图片区+标题+描述+标签） | 12 | 植物图鉴卡片加载中 |
| `SkeletonListQa` | 问答列表类内容的骨架（问题图标+问题+答案+统计/按钮） | 6 | 知识问答列表加载中 |
| `SkeletonListResource` | 资源列表类内容的骨架（图标+信息+操作按钮） | 6 | 学习资源列表加载中 |

### 骨架屏布局对比

```
SkeletonGridCard（网格卡片）        SkeletonGridImage（图片卡片）
┌─────────────────────┐           ┌─────────────────────┐
│ ○ ██████            │           │ ┌─────────────────┐ │
│ ████████████████    │           │ │   图片占位区     │ │
│ ██████████          │           │ └─────────────────┘ │
│ ████████            │           │ ████████████        │
└─────────────────────┘           │ ████████            │
                                  │ ██████    ██████    │
CSS Grid: auto-fill               └─────────────────────┘
minmax(280px, 1fr)
                                  CSS Grid: auto-fill
                                  minmax(280px, 1fr)

SkeletonListQa（问答列表）         SkeletonListResource（资源列表）
┌─────────────────────────────┐   ┌─────────────────────────────┐
│ ○ ████████████████          │   │ ○ ██████████  ████ ████ ████ │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─   │   │ ████████      ████ ████ ████ │
│ ██████████████████          │   │ ██████                      │
│ ████████                    │   └─────────────────────────────┘
│ ██████  ██████  ████ ████   │
└─────────────────────────────┘   Flex 纵向列表
                                  图标+内容+按钮横排
Flex 纵向列表
问题+答案+底部横排
```

### 使用示例

```vue
<script setup>
import { ref } from 'vue'
import SkeletonGridCard from '@/components/common/SkeletonGridCard.vue'
import SkeletonGridImage from '@/components/common/SkeletonGridImage.vue'
import SkeletonListQa from '@/components/common/SkeletonListQa.vue'
import SkeletonListResource from '@/components/common/SkeletonListResource.vue'
import CardGrid from '@/components/business/display/CardGrid.vue'

const isLoading = ref(true)
const plantData = ref([])

// 模拟数据加载
setTimeout(() => {
  plantData.value = [{ name: '钩藤', nameDong: 'gons jinl' }]
  isLoading.value = false
}, 1500)
</script>

<template>
  <!-- 卡片网格骨架屏 -->
  <SkeletonGridCard v-if="isLoading" :count="12" />
  <CardGrid v-else :items="plantData" />

  <!-- 图片卡片骨架屏 -->
  <SkeletonGridImage v-if="isLoading" :count="9" />

  <!-- 问答列表骨架屏 -->
  <SkeletonListQa v-if="isLoading" :count="6" />

  <!-- 资源列表骨架屏 -->
  <SkeletonListResource v-if="isLoading" :count="6" />
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

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `visible` | `Boolean` | `false` | 控制加载动画的显示/隐藏 |

### 视觉设计

```
┌──────────────────────────────────────────┐
│                                          │
│         半透明白色遮罩 + 毛玻璃模糊        │
│                                          │
│              ╭──╮                         │
│              │⟳│  ← 外环：靛蓝→绿色 0.8s  │
│              │⟲│  ← 内环：绿色→靛蓝 0.6s  │
│              ╰──╯    (反向旋转)            │
│                                          │
│           加载中 ● ● ●                    │
│           ↑ 文字  ↑ 三点弹跳动画           │
│                                          │
└──────────────────────────────────────────┘
```

- **双环旋转动画**：外环 `border-top: #1A5276, border-right: #28B463`，顺时针 0.8s；内环反向旋转 0.6s
- **三点弹跳动画**：三个绿色圆点（`#28B463`），间隔 0.15s 错开弹跳
- **品牌色系**：使用项目主色靛蓝 `#1A5276` 和侗绿 `#28B463`
- **过渡动画**：`<Transition name="page-loading">` 包裹，0.2s 淡入淡出

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
  <PageLoading :visible="pageLoading" />

  <!-- 页面真实内容 -->
  <div v-if="!pageLoading" class="page-content">
    <h1>侗乡医药数字展示平台</h1>
    <!-- ... -->
  </div>
</template>
```

---

## 各骨架屏组件详细说明

### SkeletonGridCard.vue

模拟**文字卡片**加载状态，布局为：圆形图标 + 元信息行 + 标题 + 两行描述 + 底部标签。

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | `Number` | `12` | 骨架卡片数量 |

**布局特点：**
- CSS Grid 响应式布局：`grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))`
- 768px 以下切换为单列布局
- 使用 `el-skeleton` 组件的 `animated` 属性实现脉冲动画

**适用场景：** 知识条目卡片、传承人卡片等不含图片的卡片列表

---

### SkeletonGridImage.vue

模拟**图片卡片**加载状态，布局为：图片占位区 + 标题 + 两行描述 + 底部标签行。

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | `Number` | `12` | 骨架卡片数量 |

**布局特点：**
- 与 SkeletonGridCard 相同的 Grid 布局策略
- 图片区高度 180px，使用 `el-skeleton-item variant="image"`
- 768px 以下：`minmax(160px, 1fr)` + 图片高度 120px

**适用场景：** 药用植物卡片、学习资源卡片等含图片的卡片列表

---

### SkeletonListQa.vue

模拟**问答卡片**加载状态，布局为：圆形问题图标 + 问题标题 + 分隔线 + 答案预览 + 底部统计/操作按钮。

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | `Number` | `6` | 骨架项数量 |

**布局特点：**
- 纵向 Flex 列表，每项为独立卡片
- 问答区域间使用虚线分隔（`border-top: 1px dashed`）
- 底部三区域：标签 + 统计 + 操作按钮
- 768px 以下底部区域自动换行

**适用场景：** 知识问答列表

---

### SkeletonListResource.vue

模拟**资源行**加载状态，布局为：圆形图标 + 信息区（标题+描述+元数据） + 操作按钮组。

**Props：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | `Number` | `6` | 骨架项数量 |

**布局特点：**
- 纵向 Flex 列表，每项为横向排列（图标 + 内容 + 按钮）
- 图标区 56x56px 圆形占位
- 内容区包含标题行 + 描述行 + 三个元数据标签
- 操作区三个按钮占位
- 768px 以下操作按钮换行至底部

**适用场景：** 学习资源列表、文档列表

---

## 常见错误

### 错误1：骨架屏和真实内容的尺寸不一致

```vue
<template>
  <!-- ❌ 骨架屏高度100px，真实内容高度200px，加载完后页面会跳动 -->
  <SkeletonGridCard v-if="isLoading" style="height: 100px" />
  <PlantCard v-else :data="plantData" />  <!-- 实际高度200px -->

  <!-- ✅ 骨架屏和真实内容保持相同尺寸，或让骨架屏自适应 -->
  <SkeletonGridCard v-if="isLoading" />
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

### 错误4：选错骨架屏类型

```vue
<template>
  <!-- ❌ 图片卡片列表用了文字卡片骨架屏，布局不匹配 -->
  <SkeletonGridCard v-if="isLoading" />
  <ImageCardGrid v-else :items="plantData" />

  <!-- ✅ 图片卡片列表用图片骨架屏 -->
  <SkeletonGridImage v-if="isLoading" />
  <ImageCardGrid v-else :items="plantData" />
</template>
```

---

## 代码审查与改进建议

- [可访问性] 骨架屏组件应添加 `aria-busy="true"` 和 `role="status"` 属性，让屏幕阅读器能识别加载状态
- [可访问性] PageLoading.vue 应添加 `aria-label="页面加载中"` 属性
- [性能] 骨架屏组件的 `count` prop 无上限校验，传入过大值可能导致渲染大量 DOM 节点
- [一致性] 四个骨架屏组件的 CSS 变量使用不完全一致（如 `--space-xl` vs `--space-lg`），建议统一间距规范
