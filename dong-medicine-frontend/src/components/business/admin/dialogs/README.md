# 管理后台对话框目录 (admin/dialogs)

本目录存放管理后台的详情对话框组件，用于查看数据详情。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| CommentDetailDialog.vue | 评论详情对话框 |
| FeedbackDetailDialog.vue | 反馈详情对话框 |
| InheritorDetailDialog.vue | 传承人详情对话框 |
| KnowledgeDetailDialog.vue | 知识详情对话框 |
| LogDetailDialog.vue | 操作日志详情对话框 |
| PlantDetailDialog.vue | 植物详情对话框 |
| QaDetailDialog.vue | 问答详情对话框 |
| QuizDetailDialog.vue | 测验题目详情对话框 |
| ResourceDetailDialog.vue | 资源详情对话框 |
| UserDetailDialog.vue | 用户详情对话框 |

---

## 组件结构

所有详情对话框遵循统一的结构：

```vue
<template>
  <el-dialog v-model="visible" :title="title" width="600px">
    <!-- 详情内容 -->
    <el-descriptions :column="1" border>
      <el-descriptions-item label="字段1">{{ data.field1 }}</el-descriptions-item>
      <el-descriptions-item label="字段2">{{ data.field2 }}</el-descriptions-item>
    </el-descriptions>
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

const handleClose = () => {
  emit('update:visible', false)
}
</script>
```

---

## 使用示例

```vue
<template>
  <!-- 列表操作按钮 -->
  <el-button @click="showDetail(row)">查看详情</el-button>
  
  <!-- 详情对话框 -->
  <UserDetailDialog v-model:visible="dialogVisible" :data="currentUser" />
</template>

<script setup>
import UserDetailDialog from './UserDetailDialog.vue'

const dialogVisible = ref(false)
const currentUser = ref({})

const showDetail = (row) => {
  currentUser.value = row
  dialogVisible.value = true
}
</script>
```

---

**最后更新时间**：2026年4月3日
