<template>
  <el-dialog
    :model-value="visible"
    title="问答详情"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ qa?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="分类">
        {{ qa?.category }}
      </el-descriptions-item>
      <el-descriptions-item
        label="问题"
        :span="2"
      >
        {{ qa?.question }}
      </el-descriptions-item>
      <el-descriptions-item
        label="答案"
        :span="2"
      >
        {{ qa?.answer }}
      </el-descriptions-item>
      <el-descriptions-item label="热度">
        {{ qa?.popularity }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(qa?.createdAt) }}
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
import { formatTime } from '@/utils/adminUtils';

defineProps({
  visible: { type: Boolean, default: false },
  qa: { type: Object, default: null }
});

defineEmits(['update:visible']);
</script>
