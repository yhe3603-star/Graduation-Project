<template>
  <BaseDetailDialog
    :model-value="visible"
    title="评论详情"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="1"
      border
    >
      <el-descriptions-item label="ID">
        {{ comment?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="用户">
        {{ comment?.username }}
      </el-descriptions-item>
      <el-descriptions-item label="目标类型">
        {{ comment?.targetType }}
      </el-descriptions-item>
      <el-descriptions-item label="目标ID">
        {{ comment?.targetId }}
      </el-descriptions-item>
      <el-descriptions-item label="评论内容">
        {{ comment?.content }}
      </el-descriptions-item>
      <el-descriptions-item label="时间">
        {{ formatTime(comment?.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="comment?.status === 'approved' ? 'success' : comment?.status === 'pending' ? 'warning' : 'danger'">
          {{ comment?.status === 'approved' ? '已审核' : comment?.status === 'pending' ? '待审核' : '已拒绝' }}
        </el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button
        v-if="comment?.status !== 'approved'"
        type="success"
        @click="handleApprove"
      >
        审核通过
      </el-button>
      <el-button
        type="danger"
        @click="handleDelete"
      >
        删除
      </el-button>
      <el-button @click="$emit('update:visible', false)">
        关闭
      </el-button>
    </template>
  </BaseDetailDialog>
</template>

<script setup>
import BaseDetailDialog from '@/components/base/BaseDetailDialog.vue'
import { formatTime } from '@/utils/adminUtils';

const props = defineProps({
  visible: { type: Boolean, default: false },
  comment: { type: Object, default: null }
});

const emit = defineEmits(['update:visible', 'approve', 'delete']);

const handleApprove = () => {
  emit('approve', props.comment);
  emit('update:visible', false);
};

const handleDelete = () => {
  emit('delete', props.comment?.id);
  emit('update:visible', false);
};
</script>
