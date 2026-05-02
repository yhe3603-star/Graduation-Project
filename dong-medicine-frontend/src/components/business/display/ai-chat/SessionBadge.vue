<template>
  <div class="session-badge">
    <el-icon><ChatLineSquare /></el-icon>
    <span class="session-badge-text">继续对话</span>
    <el-button
      class="session-badge-new"
      type="primary"
      size="small"
      @click="$emit('new-chat')"
    >
      <el-icon><Plus /></el-icon>
      新对话
    </el-button>
  </div>
</template>

<script setup>
import { ChatLineSquare, Plus } from '@element-plus/icons-vue'

defineEmits(['new-chat'])
</script>

<style scoped>
.session-badge { display: flex; align-items: center; gap: 6px; padding: 4px 12px; background: linear-gradient(135deg, rgba(40, 180, 99, 0.08), rgba(26, 82, 118, 0.06)); border: 1px solid rgba(40, 180, 99, 0.2); border-radius: var(--radius-full); font-size: var(--font-size-xs); color: var(--dong-jade-dark); }
.session-badge .el-icon { font-size: 14px; }
.session-badge-text { font-weight: var(--font-weight-medium); }
.session-badge-new { margin-left: 4px; font-size: var(--font-size-xs) !important; padding: 2px 10px !important; color: #fff !important; opacity: 1 !important; }

@media (max-width: 480px) {
  .session-badge { font-size: 11px; padding: 3px 8px; }
}
</style>
