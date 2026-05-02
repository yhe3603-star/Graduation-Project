<template>
  <el-drawer
    :model-value="visible"
    title="历史会话"
    direction="ltr"
    size="300px"
    @open="$emit('open')"
    @close="$emit('close')"
  >
    <div class="drawer-content">
      <div class="drawer-new-chat">
        <el-button type="primary" @click="$emit('new-chat')">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>
      <div v-loading="sessionsLoading" class="session-list">
        <div
          v-for="session in sessions"
          :key="session.sessionId"
          :class="['session-item', { active: session.sessionId === currentSessionId }]"
          @click="$emit('select', session)"
        >
          <div class="session-info">
            <div class="session-title">
{{ session.preview || '新对话' }}
</div>
            <div class="session-time">
{{ formatSessionTime(session.lastMessageAt) }}
</div>
          </div>
          <el-button
            class="session-delete-btn"
            text
            size="small"
            @click.stop="$emit('delete', session.sessionId)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <el-empty v-if="!sessionsLoading && sessions.length === 0" description="暂无历史会话" :image-size="60" />
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { Plus, Delete } from '@element-plus/icons-vue'

defineProps({
  visible: { type: Boolean, default: false },
  sessions: { type: Array, default: () => [] },
  sessionsLoading: { type: Boolean, default: false },
  currentSessionId: { type: String, default: null },
  formatSessionTime: { type: Function, required: true }
})

defineEmits(['open', 'close', 'select', 'delete', 'new-chat'])
</script>

<style scoped>
:deep(.el-drawer__body) { display: flex; flex-direction: column; overflow: hidden; padding: 0; }
.drawer-content { display: flex; flex-direction: column; flex: 1; min-height: 0; overflow: hidden; }
.drawer-new-chat { padding: var(--space-md) var(--space-sm); border-bottom: 1px solid var(--border-light); flex-shrink: 0; }
.drawer-new-chat .el-button { width: 100%; }
.session-list { flex: 1; min-height: 0; overflow-y: auto; padding: var(--space-sm); }
.session-item { display: flex; align-items: center; justify-content: space-between; padding: var(--space-md); border-radius: var(--radius-sm); cursor: pointer; transition: all var(--transition-fast); margin-bottom: var(--space-xs); }
.session-item:hover { background: var(--bg-rice); }
.session-item.active { background: linear-gradient(135deg, rgba(26, 82, 118, 0.08), rgba(26, 82, 118, 0.12)); }
.session-info { flex: 1; min-width: 0; overflow: hidden; }
.session-title { font-size: var(--font-size-sm); color: var(--text-primary); font-weight: var(--font-weight-medium); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.session-time { font-size: var(--font-size-xs); color: var(--text-muted); margin-top: 2px; }
.session-delete-btn { opacity: 0; transition: opacity var(--transition-fast); color: var(--text-light); }
.session-item:hover .session-delete-btn { opacity: 1; }
.session-delete-btn:hover { color: var(--color-danger); }
</style>
