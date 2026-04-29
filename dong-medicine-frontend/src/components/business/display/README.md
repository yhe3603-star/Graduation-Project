# 展示组件（Display Components）

## 什么是展示组件？

类比：**展示柜**——商场里的展示柜把商品漂亮地陈列出来，让顾客一目了然。

展示组件负责把数据"好看地"呈现给用户，是用户看到最多的组件类型。它们不处理复杂的交互逻辑，主要工作是：

- **接收数据**（通过 props）
- **展示数据**（通过模板）
- **通知父组件**（通过 emit，比如"用户点击了这张卡片"）

```
数据 → [展示组件] → 界面
       ↑ 接收      ↓ 显示
     props        模板渲染
```

---

## 组件列表

### CardGrid —— 卡片网格

**用途：** 以网格形式展示多张卡片（药用植物、知识条目等）

```
┌─────────┐ ┌─────────┐ ┌─────────┐
│ 🌿 钩藤  │ │ 🌿 透骨草│ │ 🌿 九节茶│
│  息风止痉 │ │  祛风除湿│ │  清热解毒│
└─────────┘ └─────────┘ └─────────┘
┌─────────┐ ┌─────────┐ ┌─────────┐
│ 🌿 半夏  │ │ 🌿 白及  │ │ 🌿 黄精  │
│  燥湿化痰 │ │  收敛止血│ │  补气养阴│
└─────────┘ └─────────┘ └─────────┘
```

```vue
<!-- 使用示例 -->
<CardGrid
  :items="plantList"
  :columns="3"
  @card-click="handleCardClick"
/>
```

---

### ChartCard —— 图表卡片

**用途：** 在卡片中展示数据图表（柱状图、饼图、折线图等）

```
┌──────────────────────────┐
│  药用植物分类统计          │
│  ┌──┐                    │
│  │  │ ┌──┐               │
│  │  │ │  │ ┌──┐          │
│  │  │ │  │ │  │          │
│  └──┘ └──┘ └──┘          │
│  草本  灌木  藤本          │
└──────────────────────────┘
```

```vue
<!-- 使用示例 -->
<ChartCard
  title="药用植物分类统计"
  :chart-data="categoryData"
  chart-type="bar"
/>
```

---

### Pagination —— 分页器

**用途：** 当数据太多时，分页显示，每页只显示一部分

```
← 上一页  [1]  2  3  4  5  ...  20  下一页 →
            ↑ 当前页
```

```vue
<!-- 使用示例 -->
<Pagination
  :current="currentPage"
  :total="200"
  :page-size="10"
  @change="handlePageChange"
/>
```

---

### SearchFilter —— 搜索过滤器

**用途：** 提供搜索框和筛选条件，帮用户快速找到想要的内容

```
┌────────────────────────────────────────┐
│ 🔍 [搜索药用植物名称...]               │
│                                        │
│ 分类：[全部 ▼]  功效：[全部 ▼]  排序：[默认 ▼] │
└────────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<SearchFilter
  v-model:keyword="searchKeyword"
  :filters="filterOptions"
  @search="handleSearch"
  @filter-change="handleFilterChange"
/>
```

---

### AiChatCard —— AI 对话卡片

**用途：** 展示 AI 侗医药助手的对话界面，用户可以提问

```
┌──────────────────────────┐
│  🤖 侗医药智能助手        │
│                          │
│  用户：钩藤有什么功效？    │
│  AI：钩藤具有息风止痉、    │
│      清热平肝的功效...     │
│                          │
│  [输入你的问题...]  [发送] │
└──────────────────────────┘
```

```vue
<!-- 使用示例 -->
<AiChatCard
  :chat-history="messages"
  @send-message="handleSendMessage"
/>
```

---

### PageSidebar —— 页面侧边栏

**用途：** 在页面侧边展示导航链接或快捷操作

```
┌──────────┬─────────────────────┐
│ 📋 导航   │                     │
│          │                     │
│ · 药用植物 │    主内容区域        │
│ · 传统疗法 │                     │
│ · 传承人   │                     │
│ · 学习资源 │                     │
│          │                     │
│ 🔍 快速搜索│                     │
└──────────┴─────────────────────┘
```

```vue
<!-- 使用示例 -->
<PageSidebar
  :nav-items="sidebarNavItems"
  :active-key="currentSection"
  @nav-click="handleNavClick"
/>
```

---

## 常见错误

### 错误1：在展示组件里发请求

```vue
<script setup>
// ❌ 展示组件不应该自己发请求，数据应该由父组件传入
import { ref } from 'vue'
import { getPlants } from '@/api/plant'
const plants = ref([])
getPlants().then(res => { plants.value = res.data })

// ✅ 展示组件只负责展示，数据通过 props 传入
const props = defineProps({
  items: { type: Array, default: () => [] }
})
</script>
```

### 错误2：分页器忘记处理边界

```vue
<script setup>
// ❌ 没有处理第一页点"上一页"和最后一页点"下一页"的情况
const prevPage = () => { currentPage.value-- }  // 第一页时变成0或负数！

// ✅ 加上边界判断
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}
</script>
```

---

## 代码审查与改进建议

- [安全] AiChatCard.vue使用v-html渲染AI返回内容，虽然使用了DOMPurify.sanitize()但marked配置未禁用HTML标签
- [安全] AiChatCard.vue聊天消息无长度限制，可能触发WebSocket传输问题
- [性能] CardGrid.vue每次渲染重复JSON.parse，应使用computed缓存
- [性能] AdminDashboard.vue全量导入echarts约1MB+，应按需导入
- [性能] AdminDashboard.vue中ECharts实例未在组件卸载时销毁，导致内存泄漏
- [API] AdminDashboard.vue和CaptchaInput.vue直接import request绕过inject依赖注入体系
