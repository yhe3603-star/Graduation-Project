# 详情弹窗组件（Detail Dialog Components）

## 什么是详情弹窗？

类比：**放大镜**——在列表页看到的是简略信息，点击后弹出一个窗口，展示完整的详细信息。

用户在卡片列表中浏览时，每张卡片只显示核心信息（名称、图片、一句话简介）。点击卡片后，弹出一个对话框（Dialog），展示该条目的所有详细信息。

```
列表页（简略信息）          详情弹窗（完整信息）
┌──────────────┐          ┌────────────────────────────┐
│ 🌿 钩藤      │  点击    │  🌿 钩藤                    │
│ 息风止痉     │ ────→   │  侗语名：gons jinl           │
│              │          │  学名：Uncaria rhynchophylla │
└──────────────┘          │  功效：息风止痉、清热平肝     │
                          │  主治：头痛眩晕、感冒夹惊     │
                          │  产地：贵州、湖南、广西        │
                          │  采收：秋冬季采收带钩茎枝      │
                          │  故事：侗族老人说...          │
                          │                              │
                          │              [关闭]           │
                          └────────────────────────────┘
```

---

## 弹窗组件列表

| 组件名 | 用途 | 展示内容 |
|--------|------|----------|
| `PlantDetailDialog` | 药用植物详情 | 侗语名、学名、功效、主治、产地、采收时节、民间故事 |
| `KnowledgeDetailDialog` | 知识条目详情 | 分类、内容全文、来源、相关植物 |
| `InheritorDetailDialog` | 传承人详情 | 姓名、从业经历、擅长领域、代表案例、荣誉资质 |
| `ResourceDetailDialog` | 学习资源详情 | 资源名称、类型、难度级别、简介、下载链接 |
| `QuizDetailDialog` | 测验题目详情 | 题目内容、选项、正确答案、解析、难度 |

---

## 统一的弹窗模式

所有详情弹窗遵循相同的设计模式，学会一个就等于学会了全部：

```
1. 通过 props 接收数据
2. 通过 v-model 控制显示/隐藏
3. 通过 emit 通知父组件事件
```

### 通用代码模式

```vue
<!-- 以 PlantDetailDialog 为例，其他弹窗结构完全一样 -->
<script setup>
/**
 * 药用植物详情弹窗
 * 遵循统一的弹窗模式：v-model控制显示 + props传入数据 + emit通知事件
 */

// v-model:visible 控制弹窗的显示和隐藏
const props = defineProps({
  visible: { type: Boolean, default: false },  // 弹窗是否显示
  data: { type: Object, default: () => null }  // 植物详细数据
})

// 定义事件
const emit = defineEmits([
  'update:visible',  // 关闭弹窗时通知父组件
  'edit',            // 点击编辑按钮
  'delete'           // 点击删除按钮
])

// 关闭弹窗
const handleClose = () => {
  // 通知父组件把 visible 改为 false
  emit('update:visible', false)
}
</script>

<template>
  <!-- 使用 Element Plus 的 Dialog 组件 -->
  <el-dialog
    :model-value="visible"
    title="药用植物详情"
    width="700px"
    @close="handleClose"
  >
    <!-- 数据还没加载时显示提示 -->
    <div v-if="!data" class="empty-tip">
      暂无数据
    </div>

    <!-- 数据加载后显示详情 -->
    <div v-else class="detail-content">
      <!-- 基本信息 -->
      <div class="info-section">
        <h3>{{ data.name }}</h3>
        <p>侗语名：{{ data.dongName }}</p>
        <p>学名：{{ data.scientificName }}</p>
      </div>

      <!-- 功效主治 -->
      <div class="info-section">
        <h4>功效主治</h4>
        <p>功效：{{ data.efficacy }}</p>
        <p>主治：{{ data.indications }}</p>
      </div>

      <!-- 产地与采收 -->
      <div class="info-section">
        <h4>产地与采收</h4>
        <p>产地：{{ data.origin }}</p>
        <p>采收时节：{{ data.harvestSeason }}</p>
      </div>

      <!-- 民间故事 -->
      <div class="info-section" v-if="data.story">
        <h4>民间故事</h4>
        <p>{{ data.story }}</p>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="emit('edit', data)">编辑</el-button>
    </template>
  </el-dialog>
</template>
```

### 父组件中使用

```vue
<!-- PlantPage.vue -->
<script setup>
import { ref } from 'vue'
import PlantDetailDialog from '@/components/business/dialogs/PlantDetailDialog.vue'

// 控制弹窗显示
const dialogVisible = ref(false)
// 当前查看的植物数据
const currentPlant = ref(null)

// 点击卡片时打开弹窗
const handleCardClick = (plant) => {
  currentPlant.value = plant
  dialogVisible.value = true
}

// 弹窗关闭时的回调
const handleDialogClose = () => {
  dialogVisible.value = false
}
</script>

<template>
  <!-- 卡片列表 -->
  <CardGrid
    :items="plantList"
    @card-click="handleCardClick"
  />

  <!-- 详情弹窗 -->
  <PlantDetailDialog
    v-model:visible="dialogVisible"
    :data="currentPlant"
    @edit="handleEdit"
  />
</template>
```

---

## 常见错误

### 错误1：用 v-if 代替 v-model:visible 控制弹窗

```vue
<template>
  <!-- ❌ 用 v-if 每次都销毁和重建弹窗，动画效果差，性能也差 -->
  <PlantDetailDialog v-if="dialogVisible" :data="currentPlant" />

  <!-- ✅ 用 v-model:visible 控制显示隐藏，弹窗只创建一次 -->
  <PlantDetailDialog v-model:visible="dialogVisible" :data="currentPlant" />
</template>
```

### 错误2：关闭弹窗时没有清空数据

```vue
<script setup>
// ❌ 关闭弹窗后数据还在，下次打开会先闪一下旧数据
const handleClose = () => {
  dialogVisible.value = false
  // 缺少清空数据的代码！
}

// ✅ 关闭弹窗时清空数据
const handleClose = () => {
  dialogVisible.value = false
  currentPlant.value = null  // 清空数据，下次打开不会闪旧数据
}
</script>
```

### 错误3：弹窗内容太长没有滚动条

```vue
<template>
  <!-- ❌ 内容太长时弹窗会超出屏幕，底部按钮看不到 -->
  <el-dialog :model-value="visible" title="详情">
    <div class="very-long-content">
      <!-- 很多内容... -->
    </div>
  </el-dialog>

  <!-- ✅ 设置弹窗内容区域可滚动 -->
  <el-dialog :model-value="visible" title="详情" top="5vh">
    <div class="scrollable-content" style="max-height: 60vh; overflow-y: auto;">
      <!-- 很多内容... -->
    </div>
  </el-dialog>
</template>
```
