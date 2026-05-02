<template>
  <BaseDetailDialog
    :model-value="visible"
    title="用户详情"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-alert
      v-if="user?.status === 'banned'"
      type="error"
      title="该用户已被封禁"
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
      <el-descriptions-item label="角色">
        <el-tag :type="user?.role === 'admin' ? 'warning' : 'info'">
          {{ user?.role === 'admin' ? '管理员' : '普通用户' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="user?.status === 'banned' ? 'danger' : 'success'">
          {{ user?.status === 'banned' ? '已封禁' : '正常' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(user?.createdAt) }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <div style="display: flex; justify-content: space-between; width: 100%;">
        <div>
          <el-button
            v-if="user?.status !== 'banned' && user?.role !== 'admin'"
            type="danger"
            @click="handleBan"
          >
            封禁用户
          </el-button>
          <el-button
            v-if="user?.status === 'banned'"
            type="success"
            @click="handleUnban"
          >
            解除封禁
          </el-button>
        </div>
        <el-button @click="$emit('update:visible', false)">
          关闭
        </el-button>
      </div>
    </template>
  </BaseDetailDialog>
</template>

<script setup>
import BaseDetailDialog from '@/components/base/BaseDetailDialog.vue'
import { formatTime } from '@/utils/adminUtils';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = defineProps({
  visible: { type: Boolean, default: false },
  user: { type: Object, default: null }
});

const emit = defineEmits(['update:visible', 'ban', 'unban']);

const handleBan = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要封禁用户 "${props.user?.username}" 吗？封禁后该用户将无法登录系统。`,
      '封禁确认',
      { type: 'warning', confirmButtonText: '确定封禁', cancelButtonText: '取消' }
    );
    emit('ban', props.user);
  } catch {
    // 用户取消
  }
};

const handleUnban = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要解除用户 "${props.user?.username}" 的封禁吗？`,
      '解封确认',
      { type: 'info', confirmButtonText: '确定解封', cancelButtonText: '取消' }
    );
    emit('unban', props.user);
  } catch {
    // 用户取消
  }
};
</script>
