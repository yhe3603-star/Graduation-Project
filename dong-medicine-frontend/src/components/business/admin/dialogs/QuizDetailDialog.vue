<template>
  <el-dialog
    :model-value="visible"
    title="题目详情"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ quiz?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="分类">
        {{ quiz?.category }}
      </el-descriptions-item>
      <el-descriptions-item label="正确答案">
        {{ quiz?.correctAnswer || quiz?.answer }}
      </el-descriptions-item>
      <el-descriptions-item
        label="题目"
        :span="2"
      >
        {{ quiz?.question }}
      </el-descriptions-item>
      <el-descriptions-item label="选项A">
        {{ (quiz?.options || quiz?.optionList || [])[0] || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="选项B">
        {{ (quiz?.options || quiz?.optionList || [])[1] || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="选项C">
        {{ (quiz?.options || quiz?.optionList || [])[2] || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="选项D">
        {{ (quiz?.options || quiz?.optionList || [])[3] || '-' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="解析"
        :span="2"
      >
        {{ quiz?.explanation || '无' }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">
        关闭
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  quiz: { type: Object, default: null }
});

defineEmits(['update:visible']);
</script>
