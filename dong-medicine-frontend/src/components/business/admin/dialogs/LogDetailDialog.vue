<template>
  <BaseDetailDialog
    :model-value="visible"
    title="日志详情"
    width="650px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      v-if="log"
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ log.id }}
      </el-descriptions-item>
      <el-descriptions-item label="用户ID">
        {{ log.userId || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="用户名">
        {{ log.username }}
      </el-descriptions-item>
      <el-descriptions-item label="模块">
        <el-tag
          :type="getModuleTagType(log.module)"
          size="small"
        >
          {{ log.module }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="操作类型">
        <el-tag
          :type="getTypeTagType(log.type)"
          size="small"
        >
          {{ log.type }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag
          :type="log.success ? 'success' : 'danger'"
          size="small"
        >
          {{ log.success ? '成功' : '失败' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item
        label="操作描述"
        :span="2"
      >
        {{ log.operation }}
      </el-descriptions-item>
      <el-descriptions-item
        label="请求方法"
        :span="2"
      >
        {{ log.method }}
      </el-descriptions-item>
      <el-descriptions-item
        label="请求参数"
        :span="2"
      >
        <div class="params-box">
          {{ formatParams(log.params) }}
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="IP地址">
        {{ log.ip }}
      </el-descriptions-item>
      <el-descriptions-item label="执行耗时">
        {{ log.duration }}ms
      </el-descriptions-item>
      <el-descriptions-item
        label="操作时间"
        :span="2"
      >
        {{ formatTime(log.createdAt) }}
      </el-descriptions-item>
      <el-descriptions-item
        v-if="log.errorMsg"
        label="错误信息"
        :span="2"
      >
        <div class="error-box">
          {{ log.errorMsg }}
        </div>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button
        type="primary"
        @click="$emit('update:visible', false)"
      >
        关闭
      </el-button>
    </template>
  </BaseDetailDialog>
</template>

<script setup>
import BaseDetailDialog from '@/components/base/BaseDetailDialog.vue'
defineProps({
  visible: { type: Boolean, default: false },
  log: { type: Object, default: null }
});

defineEmits(['update:visible']);

const MODULE_TYPES = { USER: 'primary', PLANT: 'success', KNOWLEDGE: 'warning', INHERITOR: 'info', RESOURCE: 'danger', QA: '', FEEDBACK: 'warning', COMMENT: 'info', FAVORITE: 'success', SYSTEM: 'danger' };
const TYPE_TYPES = { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', QUERY: '' };

const getModuleTagType = (module) => MODULE_TYPES[module] || '';
const getTypeTagType = (type) => TYPE_TYPES[type] || '';
const formatTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : '无';
const formatParams = (params) => {
  if (!params) return '无';
  try { return JSON.stringify(JSON.parse(params), null, 2); } catch { return params; }
};
</script>

<style scoped>
.params-box { padding: 10px; background: #f5f7fa; border-radius: 4px; font-family: monospace; font-size: 12px; white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow: auto; }
.error-box { padding: 10px; background: #fef0f0; border-radius: 4px; color: #f56c6c; font-family: monospace; white-space: pre-wrap; word-break: break-all; }
</style>
