# 详情对话框目录 (dialogs)

本目录存放各种数据的详情弹窗组件。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| PlantDetailDialog.vue | 药材详情弹窗 |
| KnowledgeDetailDialog.vue | 知识详情弹窗 |
| InheritorDetailDialog.vue | 传承人详情弹窗 |
| ResourceDetailDialog.vue | 资源详情弹窗 |
| QuizDetailDialog.vue | 答题详情弹窗 |

---

## 基本结构

所有详情对话框都遵循类似的结构：

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="800px"
    @close="handleClose"
  >
    <!-- 头部信息 -->
    <div class="detail-header">
      <h2>{{ data.name }}</h2>
      <div class="detail-tags">
        <span v-for="tag in data.tags" :key="tag">{{ tag }}</span>
      </div>
    </div>
    
    <!-- 详细内容 -->
    <div class="detail-content">
      <div class="detail-section">
        <h3>基本信息</h3>
        <p>{{ data.description }}</p>
      </div>
      
      <div class="detail-section">
        <h3>详细信息</h3>
        <!-- 具体内容... -->
      </div>
    </div>
    
    <!-- 底部操作 -->
    <template #footer>
      <button @click="handleClose">关闭</button>
      <button @click="handleFavorite">收藏</button>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible', 'favorite'])

const handleClose = () => {
  emit('update:visible', false)
}

const handleFavorite = () => {
  emit('favorite', props.data)
}
</script>
```

---

## PlantDetailDialog.vue - 药材详情

展示药材的详细信息，包括：
- 基本信息（名称、分类、功效）
- 图片轮播
- 相关故事
- 收藏功能

---

## KnowledgeDetailDialog.vue - 知识详情

展示知识条目的详细信息，包括：
- 标题和分类
- 内容详情
- 配方和用法
- 相关植物

---

## InheritorDetailDialog.vue - 传承人详情

展示传承人的详细信息，包括：
- 基本信息（姓名、级别）
- 专长领域
- 荣誉成就
- 代表作品

---

## ResourceDetailDialog.vue - 资源详情

展示学习资源的详细信息，包括：
- 资源标题
- 文件预览
- 下载链接

---

**最后更新时间**：2026年4月3日
