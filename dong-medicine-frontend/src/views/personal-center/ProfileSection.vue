<template>
  <div class="profile-section">
    <div class="profile-card">
      <div class="avatar-section">
        <el-avatar :size="100" class="user-avatar">
{{ userName?.charAt(0) || '用' }}
</el-avatar>
        <div class="user-info">
          <h2 class="user-name">
{{ userName || '侗乡医药用户' }}
</h2>
          <el-tag :type="isAdmin ? 'warning' : 'success'" effect="dark">
{{ isAdmin ? '管理员' : '普通用户' }}
</el-tag>
        </div>
      </div>
      <el-divider />
      <div class="stats-row">
        <div v-for="s in studyStats" :key="s.label" class="stat-block">
          <span class="stat-num">{{ s.value }}</span>
          <span class="stat-text">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <el-card v-for="action in actions" :key="action.key" class="action-card" shadow="hover" @click="$emit('tab-change', action.key)">
        <el-icon><component :is="action.icon" /></el-icon>
        <span>{{ action.label }}</span>
      </el-card>
    </div>
  </div>
</template>

<script setup>
defineProps({
  userName: { type: String, default: '侗乡医药用户' },
  isAdmin: { type: Boolean, default: false },
  studyStats: { type: Array, required: true },
  actions: { type: Array, required: true }
})

defineEmits(['tab-change'])
</script>

<style scoped>
.profile-card {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}
.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}
.user-avatar {
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  color: var(--text-inverse);
}
.user-name {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-xl);
}
.stats-row {
  display: flex;
  justify-content: space-around;
}
.stat-block {
  text-align: center;
}
.stat-num {
  display: block;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--dong-indigo);
}
.stat-text {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-md);
  margin-top: var(--space-xl);
}
.action-card {
  cursor: pointer;
  text-align: center;
  padding: var(--space-lg);
}
.action-card .el-icon {
  font-size: var(--font-size-xl);
  color: var(--dong-indigo);
  margin-bottom: var(--space-sm);
}

@media (max-width: 768px) {
  .profile-card { padding: var(--space-md); }
  .avatar-section { flex-direction: column; text-align: center; gap: var(--space-md); }
  .user-avatar { --el-avatar-size: 72px !important; }
  .user-name { font-size: var(--font-size-lg); }
  .stats-row { flex-wrap: wrap; gap: var(--space-sm); }
  .stat-block { flex: 1 1 30%; min-width: 0; }
  .stat-num { font-size: var(--font-size-lg); }
  .quick-actions { grid-template-columns: 1fr; gap: var(--space-sm); margin-top: var(--space-md); }
  .action-card { padding: var(--space-md); }
}

@media (max-width: 480px) {
  .profile-card { padding: var(--space-sm); }
  .stat-block { flex: 1 1 45%; }
}
</style>
