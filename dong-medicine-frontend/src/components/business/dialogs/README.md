# 前台详情弹窗组件（Frontend Detail Dialog Components）

## 什么是前台详情弹窗？

类比：**博物馆展品放大镜**——在展厅走廊上你只能看到展品的名称和缩略图，拿起放大镜后就能看到展品的全部细节、多媒体资料和相关故事。

用户在卡片列表中浏览时，每张卡片只显示核心信息（名称、图片、一句话简介）。点击卡片后，弹出一个对话框（Dialog），展示该条目的所有详细信息，包括多媒体资源（视频、图片、文档）、收藏功能、浏览历史记录等。

```
列表页（简略信息）              前台详情弹窗（完整信息 + 多媒体 + 交互）
┌──────────────┐          ┌────────────────────────────────────┐
│ 钩藤         │  点击    │  钩藤                    [分类] [产地] │
│ 息风止痉     │ ────→   │  浏览:128  收藏:23                     │
│              │          │  ┌────────────────────────────────┐  │
│              │          │  │ [视频] [图片] [文档]            │  │
│              │          │  │   ▶ 视频播放器 / 图片轮播        │  │
│              │          │  └────────────────────────────────┘  │
│              │          │  中文名：钩藤                         │
│              │          │  侗语名：gons jinl 🔊                 │
│              │          │  学名：Uncaria rhynchophylla         │
│              │          │  功效：息风止痉、清热平肝              │
│              │          │                                      │
│              │          │  [⭐ 收藏]              [关闭]       │
└──────────────┘          └────────────────────────────────────┘
```

---

## 弹窗组件列表

| 组件名 | 用途 | 数据 prop | 展示内容 |
|--------|------|-----------|----------|
| `PlantDetailDialog` | 药用植物详情 | `plant` | 中文名、侗语名(含语音)、学名、分类、用法、生境、功效、媒体资源(视频/图片/文档) |
| `KnowledgeDetailDialog` | 知识条目详情 | `knowledge` | 疗法/疾病/药材分类、内容全文、配方组成、关联药材(含知识图谱)、操作步骤流程图、用法说明、媒体资源 |
| `InheritorDetailDialog` | 传承人详情 | `inheritor` | 姓名、级别徽章、从业年限、技艺特色、简介、代表案例、荣誉资质、传承生涯时间线、媒体资源 |
| `ResourceDetailDialog` | 学习资源详情 | `resource` | 文件类型标签、文件大小、资源预览(视频/图片/文档内嵌)、资源描述、相关文档、浏览/下载统计 |
| `QuizDetailDialog` | 问答详情 | `qa` | 问题(高亮)、解答、分类标签 |

---

## 统一的弹窗模式

所有前台详情弹窗遵循相同的设计模式：

```
1. 通过 props 接收 visible(显示状态)、具体数据对象、isFavorited(收藏状态)
2. 通过 v-model:visible (即 @update:visible) 控制显示/隐藏
3. 通过 emit 通知父组件事件：toggle-favorite(收藏切换)
4. 弹窗打开时自动记录浏览历史(仅登录用户)
5. 弹窗关闭时自动暂停视频播放
```

### 通用 Props 接口

```typescript
interface DetailDialogProps {
  visible: boolean       // 弹窗是否显示
  xxx: Object | null     // 具体数据对象(plant/knowledge/inheritor/resource/qa)
  isFavorited: boolean   // 当前用户是否已收藏
}

interface DetailDialogEmits {
  'update:visible': (val: boolean) => void   // 关闭弹窗
  'toggle-favorite': () => void               // 切换收藏状态
}
```

### 父组件中使用

```vue
<!-- PlantPage.vue -->
<script setup>
import { ref } from 'vue'
import PlantDetailDialog from '@/components/business/dialogs/PlantDetailDialog.vue'

const dialogVisible = ref(false)
const currentPlant = ref(null)
const isFavorited = ref(false)

// 点击卡片时打开弹窗
const handleCardClick = (plant) => {
  currentPlant.value = plant
  isFavorited.value = plant.isFavorited || false
  dialogVisible.value = true
}

// 切换收藏
const handleToggleFavorite = async () => {
  await request.post('/favorites/toggle', { targetType: 'plant', targetId: currentPlant.value.id })
  isFavorited.value = !isFavorited.value
}
</script>

<template>
  <PlantDetailDialog
    v-model:visible="dialogVisible"
    :plant="currentPlant"
    :is-favorited="isFavorited"
    @toggle-favorite="handleToggleFavorite"
  />
</template>
```

---

## 各组件详细说明

### PlantDetailDialog —— 药用植物详情

展示药用植物的全部信息，包含多媒体标签页和侗语语音播放。

```
┌────────────────────────────────────────────┐
│  钩藤                    [木本] [贵州/湖南]  │
│  浏览:128  收藏:23                          │
│  ─────────────────────────────────────────  │
│  [▶ 视频] [🖼 图片] [📄 文档]              │
│  ┌──────────────────────────────────────┐  │
│  │      视频播放器 / 图片轮播 / 文档列表    │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │ 中文名：钩藤    侗语名：gons jinl 🔊  │  │
│  │ 学名：Uncaria   分类：[木本]          │  │
│  │ 用法：内服      产地：贵州/湖南        │  │
│  │ 功效：息风止痉、清热平肝               │  │
│  └──────────────────────────────────────┘  │
│  [⭐ 收藏]                      [关闭]     │
└────────────────────────────────────────────┘
```

**核心逻辑：**
- 媒体标签页切换：有视频时默认显示视频标签，否则显示图片标签
- 切换标签时自动暂停视频播放（`handleTabChange`）
- 关闭弹窗时自动暂停视频播放（`handleDialogClose`）
- 侗语名旁集成 `HerbAudio` 语音播放组件
- 打开弹窗时自动记录浏览历史（仅登录用户，调用 `/browse-history/record`）
- 植物ID变化时重新加载文档和重置标签页

**依赖组件：** `VideoPlayer`、`ImageCarousel`、`DocumentList`、`DocumentPreview`、`HerbAudio`

---

### KnowledgeDetailDialog —— 知识条目详情

展示侗医药知识条目的完整信息，包含配方、步骤流程图、关联药材和知识图谱。

```
┌──────────────────────────────────────────────┐
│  侗族药浴疗法      [药浴疗法] [风湿骨痛] [根茎类] │
│  浏览:256  收藏:45                              │
│  ─────────────────────────────────────────────  │
│  疗法分类：药浴疗法  疾病分类：风湿骨痛  药材分类：根茎类 │
│                                                │
│  📄 内容介绍                                   │
│  侗族药浴是侗族人民世代相传的...                 │
│                                                │
│  📝 配方组成                                   │
│  ┃ 钩藤 15g、透骨草 20g、九节茶 10g...         ┃
│                                                │
│  🌿 关联药材                                   │
│  ┌──────┐ ┌──────┐ ┌──────┐                   │
│  │ 钩藤  │ │透骨草 │ │九节茶 │  → 点击跳转植物页 │
│  └──────┘ └──────┘ └──────┘                   │
│                                                │
│  🔗 知识图谱                                   │
│  ┌────────────────────────────┐               │
│  │   (知识条目) ── (关联药材)   │               │
│  └────────────────────────────┘               │
│                                                │
│  📋 操作步骤                                   │
│  ① ── 准备药材                                │
│  ② ── 加水煎煮                                │
│  ③ ── 熏蒸患处                                │
│                                                │
│  🎬 相关资料  [▶ 视频] [📄 文档]               │
│  [⭐ 收藏]                          [关闭]     │
└──────────────────────────────────────────────┘
```

**核心逻辑：**
- **关联药材加载**：打开弹窗时根据 `relatedPlants` 字段（JSON数组或逗号分隔的ID列表），调用 `/plants/batch` 接口批量获取关联植物信息
- **知识图谱可视化**：使用 `KnowledgeGraph` 组件展示知识条目与关联药材的关系网络
- **步骤流程图**：解析 `steps` 字段（JSON数组或纯文本），渲染为带编号节点的垂直流程图
- **配方展示**：`formula` 字段以等宽字体、渐变背景的代码块样式展示
- **关联植物跳转**：点击关联植物卡片后关闭弹窗并跳转到 `/plants?id=xxx`
- **媒体标签页**：有视频时默认视频标签，否则默认文档标签

**依赖组件：** `VideoPlayer`、`DocumentList`、`DocumentPreview`、`ImageCarousel`、`KnowledgeGraph`

---

### InheritorDetailDialog —— 传承人详情

展示传承人的完整信息，包含头像、级别徽章、传承生涯时间线和多媒体资料。

```
┌──────────────────────────────────────────┐
│  ┌────┐                                  │
│  │ 吴 │  吴老医师                         │
│  │老 │  擅长：侗族药浴疗法、侗医推拿       │
│  │[省级]│  从业 45 年                      │
│  └────┘                                  │
│  ───────────────────────────────────────  │
│  个人简介：吴老医师自幼跟随...              │
│  代表案例：治愈风湿骨痛患者数百例...         │
│  荣誉资质：省级非遗传承人...                │
│                                          │
│  🕐 传承生涯                              │
│  ● 学习经历  1976                         │
│  │  早期跟随师长学习侗医药知识...          │
│  ● 开始行医  1981                         │
│  │  于1981年开始从事侗医药实践...          │
│  ● 技艺特色                               │
│  │  擅长：侗族药浴疗法、侗医推拿           │
│  ● 荣誉奖项                               │
│     省级非遗传承人                         │
│                                          │
│  🎬 媒体资料  [▶ 视频] [🖼 图片] [📄 文档] │
│  [⭐ 收藏]                      [关闭]    │
└──────────────────────────────────────────┘
```

**核心逻辑：**
- **时间线智能生成**：双策略时间线生成算法
  - 策略1：`parseTimelineFromBio` —— 从简介(bio)字段中按正则匹配"学习经历/行医经历/荣誉奖项/传承贡献"等关键词段落，提取结构化时间线条目
  - 策略2：`generateGenericTimeline` —— 当简介无法解析时，根据经验年限、擅长领域、荣誉、简介等字段自动生成通用时间线
- **级别徽章映射**：`LEVEL_TYPES = { '省级': 'warning', '自治区级': 'success', '州级': 'primary', '市级': 'primary', '县级': 'info' }`
- **头像显示**：取姓名首字作为圆形头像
- **年份提取**：从文本中匹配 `(\d{4})\s*年` 模式提取年份

**依赖组件：** `VideoPlayer`、`ImageCarousel`、`DocumentList`、`DocumentPreview`

---

### ResourceDetailDialog —— 学习资源详情

展示学习资源的完整信息，包含文件类型检测、在线预览和下载功能。

```
┌──────────────────────────────────────────────┐
│  侗族药浴入门教程     [视频] MP4  156.3 MB     │
│  ───────────────────────────────────────────  │
│  ┌────────────────────────────────────────┐  │
│  │          视频播放器 / 图片轮播            │  │
│  │          / 文档内嵌预览                  │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  资源描述                                     │
│  本教程详细介绍了侗族药浴的基本方法...         │
│                                              │
│  相关文档                                     │
│  📄 药浴配方表.pdf                            │
│                                              │
│  浏览:89次  下载:34次                         │
│  [⭐ 收藏] [⬇ 下载资源]            [关闭]     │
└──────────────────────────────────────────────┘
```

**核心逻辑：**
- **文件类型检测**：通过文件扩展名和 `files` 字段中的 `type` 属性双重判断文件类型
  - 视频：mp4, avi, mov, wmv, flv, mkv
  - 图片：jpg, jpeg, png, gif, bmp, webp, svg
  - 文档：docx, doc, pdf, pptx, ppt, xlsx, xls, txt
- **智能预览**：根据文件类型自动选择对应的预览组件（VideoPlayer / ImageCarousel / DocumentPreview）
- **文件大小格式化**：`formatSize` 函数将字节数转为 B/KB/MB/GB 可读格式
- **类型标签映射**：`TAG_TYPES = { video: 'danger', document: 'primary', image: 'success' }`
- **相关文档**：解析 `documents` 字段，支持 JSON 数组和字符串格式
- **下载功能**：底部提供下载按钮，emit `download` 事件

**依赖组件：** `VideoPlayer`、`ImageCarousel`、`DocumentList`、`DocumentPreview`

---

### QuizDetailDialog —— 问答详情

展示问答条目的问题与答案，采用问答卡片式布局。

```
┌──────────────────────────────────────┐
│  侗族药浴有哪些功效？    [侗药常识]    │
│  浏览:56  收藏:12                     │
│  ──────────────────────────────────  │
│  ❓ 问题                             │
│  ┃ 侗族药浴有哪些功效？              ┃
│                                      │
│  ✅ 解答                             │
│  侗族药浴具有祛风除湿、活血化瘀...    │
│                                      │
│  [⭐ 收藏]                  [关闭]    │
└──────────────────────────────────────┘
```

**核心逻辑：**
- 问题区域使用渐变背景 + 左侧蓝色边框高亮
- 解答区域使用普通内容框样式
- 分类标签默认显示"侗药常识"
- 打开弹窗时自动记录浏览历史

**依赖组件：** 无额外业务组件，仅使用 Element Plus 基础组件

---

## 共享样式与工具

### 公共样式导入

所有弹窗均导入公共弹窗样式：

```css
@import '@/styles/dialog-common.css';
```

### 公共工具函数

```javascript
import { parseMediaList, parseDocumentList, downloadDocument } from '@/utils'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
```

| 工具 | 用途 |
|------|------|
| `parseMediaList` | 解析媒体字段（JSON字符串/数组）为统一的URL列表 |
| `parseDocumentList` | 解析文档字段为 DocumentList 组件所需格式 |
| `downloadDocument` | 触发文档下载（创建临时 `<a>` 标签） |
| `request` | HTTP 请求工具，用于记录浏览历史 |
| `useUserStore` | 用户状态管理，判断是否登录以决定是否记录浏览历史 |

### 浏览历史记录模式

所有弹窗在打开时（`watch(visible)`），若用户已登录，自动调用浏览历史接口：

```javascript
watch(() => props.visible, (newVal) => {
  if (newVal && props.xxx?.id && userStore.isLoggedIn) {
    request.post('/browse-history/record', null, {
      params: { targetType: 'plant', targetId: props.xxx.id }
    }).catch(() => {});
  }
});
```

| 组件 | targetType 值 |
|------|---------------|
| PlantDetailDialog | `'plant'` |
| KnowledgeDetailDialog | `'knowledge'` |
| InheritorDetailDialog | `'inheritor'` |
| ResourceDetailDialog | `'resource'` |
| QuizDetailDialog | `'qa'` |

---

## 响应式设计

所有弹窗宽度使用 `min(Xpx, 90vw)` 或 `min(Xpx, 95vw)` 确保在小屏幕上自适应：

| 组件 | 宽度 |
|------|------|
| PlantDetailDialog | `min(800px, 90vw)` |
| KnowledgeDetailDialog | `min(800px, 90vw)` |
| InheritorDetailDialog | `min(800px, 90vw)` |
| ResourceDetailDialog | `min(900px, 95vw)` |
| QuizDetailDialog | `min(700px, 90vw)` |

所有组件均包含 `@media (max-width: 768px)` 和 `@media (max-width: 480px)` 两级响应式断点，适配平板和手机屏幕。

---

## 常见错误

### 错误1：用 v-if 代替 v-model:visible 控制弹窗

```vue
<template>
  <!-- ❌ 用 v-if 每次都销毁和重建弹窗，动画效果差，性能也差 -->
  <PlantDetailDialog v-if="dialogVisible" :plant="currentPlant" />

  <!-- ✅ 用 v-model:visible 控制显示隐藏，弹窗只创建一次 -->
  <PlantDetailDialog v-model:visible="dialogVisible" :plant="currentPlant" />
</template>
```

### 错误2：关闭弹窗时没有暂停视频

```vue
<script setup>
// ❌ 关闭弹窗后视频还在后台播放，浪费资源
const handleClose = () => {
  dialogVisible.value = false
}

// ✅ 关闭弹窗时暂停视频播放
const handleClose = (newVisible) => {
  if (!newVisible && videoPlayerRef.value) videoPlayerRef.value.pause()
  emit('update:visible', newVisible)
}
</script>
```

### 错误3：忘记传递 isFavorited 属性

```vue
<template>
  <!-- ❌ 收藏按钮始终显示"收藏"，无法反映真实状态 -->
  <PlantDetailDialog v-model:visible="dialogVisible" :plant="currentPlant" />

  <!-- ✅ 传入 isFavorited 让按钮正确显示"收藏"或"取消收藏" -->
  <PlantDetailDialog
    v-model:visible="dialogVisible"
    :plant="currentPlant"
    :is-favorited="isFavorited"
    @toggle-favorite="handleToggleFavorite"
  />
</template>
```

---

## 代码审查与改进建议

- [结构] 前台 dialogs 与 admin/dialogs 存在同名组件（PlantDetailDialog、KnowledgeDetailDialog、InheritorDetailDialog、ResourceDetailDialog、QuizDetailDialog），功能有重叠但实现不同，可考虑抽取共享逻辑
- [性能] ResourceDetailDialog 设置了 `destroy-on-close`，而其他弹窗没有，行为不一致
- [体验] InheritorDetailDialog 和 KnowledgeDetailDialog 的媒体标签页默认逻辑不同（前者默认 video，后者有视频时默认 video 否则 document），可统一
- [健壮性] 浏览历史记录的 `.catch(() => {})` 静默吞掉错误，建议至少在开发环境打印日志
