<template>
  <el-dialog
    :model-value="visible"
    title="用户详情"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-alert
      type="warning"
      title="安全提示"
      description="密码信息敏感，请勿泄露给他人"
      show-icon
      :closable="false"
      style="margin-bottom: 16px;"
    />
    <el-descriptions
      :column="1"
      border
    >
      <el-descriptions-item label="ID">
        {{ user?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="用户名">
        {{ user?.username }}
      </el-descriptions-item>
      <el-descriptions-item label="密码">
        <span style="color: #e74c3c; font-family: monospace;">{{ user?.passwordHash || user?.password || '******' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="角色">
        <el-tag :type="user?.role === 'admin' ? 'warning' : 'info'">
          {{ user?.role === 'admin' ? '管理员' : '普通用户' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(user?.createdAt) }}
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
  user: { type: Object, default: null }
});

defineEmits(['update:visible']);
</script>
