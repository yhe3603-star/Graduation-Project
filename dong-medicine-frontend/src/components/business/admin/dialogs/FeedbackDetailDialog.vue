<template>
  <el-dialog
    :model-value="visible"
    title="反馈详情"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="1"
      border
    >
      <el-descriptions-item label="ID">
        {{ feedback?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="用户">
        {{ feedback?.userName || '匿名用户' }}
      </el-descriptions-item>
      <el-descriptions-item label="类型">
        {{ feedback?.type }}
      </el-descriptions-item>
      <el-descriptions-item label="标题">
        {{ feedback?.title }}
      </el-descriptions-item>
      <el-descriptions-item label="内容">
        {{ feedback?.content }}
      </el-descriptions-item>
      <el-descriptions-item label="联系方式">
        {{ feedback?.contact || '未填写' }}
      </el-descriptions-item>
      <el-descriptions-item label="提交时间">
        {{ formatTime(feedback?.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="getFeedbackStatusTag(feedback?.status)">
          {{ getFeedbackStatusText(feedback?.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item
        v-if="feedback?.reply"
        label="回复"
      >
        {{ feedback?.reply }}
      </el-descriptions-item>
    </el-descriptions>
    <div
      v-if="feedback?.status !== 'resolved'"
      style="margin-top: 20px"
    >
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="3"
        placeholder="输入回复内容..."
      />
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">
        关闭
      </el-button>
      <el-button
        v-if="feedback?.status !== 'resolved'"
        type="primary"
        :disabled="!replyContent.trim()"
        @click="handleReply"
      >
        回复并处理
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import { formatTime, getFeedbackStatusTag, getFeedbackStatusText } from '@/utils/adminUtils';

const props = defineProps({
  visible: { type: Boolean, default: false },
  feedback: { type: Object, default: null }
});

const emit = defineEmits(['update:visible', 'reply']);

const replyContent = ref('');

watch(() => props.visible, (val) => {
  if (val) replyContent.value = '';
});

const handleReply = () => {
  if (!replyContent.value.trim()) return;
  emit('reply', { feedback: props.feedback, reply: replyContent.value });
  emit('update:visible', false);
};
</script>
