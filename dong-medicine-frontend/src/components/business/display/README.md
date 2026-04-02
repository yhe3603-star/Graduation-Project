# 展示组件目录 (display)

本目录存放数据展示相关的组件，如卡片、图表、分页等。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| AiChatCard.vue | AI对话卡片，集成智能问答 |
| CardGrid.vue | 卡片网格布局，展示列表数据 |
| ChartCard.vue | 图表卡片，封装ECharts图表 |
| PageSidebar.vue | 页面侧边栏，展示统计和热门内容 |
| Pagination.vue | 分页组件 |
| SearchFilter.vue | 搜索过滤组件 |

---

## CardGrid.vue - 卡片网格

用于展示卡片列表，支持响应式布局。

```vue
<template>
  <div class="card-grid">
    <div
      v-for="item in items"
      :key="item.id"
      class="card-item"
      @click="$emit('item-click', item)"
    >
      <slot :item="item" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})

defineEmits(['item-click'])
</script>
```

**使用示例：**

```vue
<CardGrid :items="plants" @item-click="handleItemClick">
  <template #default="{ item }">
    <PlantCard :plant="item" />
  </template>
</CardGrid>
```

---

## Pagination.vue - 分页组件

```vue
<template>
  <div class="pagination">
    <button :disabled="current <= 1" @click="changePage(current - 1)">上一页</button>
    <span>{{ current }} / {{ totalPages }}</span>
    <button :disabled="current >= totalPages" @click="changePage(current + 1)">下一页</button>
  </div>
</template>

<script setup>
const props = defineProps({
  current: { type: Number, default: 1 },
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 10 }
})

const emit = defineEmits(['update:current', 'change'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize))

const changePage = (page) => {
  emit('update:current', page)
  emit('change', page)
}
</script>
```

---

## SearchFilter.vue - 搜索过滤

```vue
<template>
  <div class="search-filter">
    <input
      v-model="keyword"
      placeholder="搜索..."
      @input="handleSearch"
    >
    <select v-model="category" @change="handleFilter">
      <option value="">全部分类</option>
      <option v-for="cat in categories" :key="cat" :value="cat">
        {{ cat }}
      </option>
    </select>
  </div>
</template>
```

---

**最后更新时间**：2026年4月3日
