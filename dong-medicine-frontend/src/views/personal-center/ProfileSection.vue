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
        <div class="stat-block">
          <span class="stat-num">{{ commentCount }}</span>
          <span class="stat-text">评论总数</span>
        </div>
      </div>
    </div>

    <div v-if="recentBrowseHistory?.length || recentComments?.length" class="recent-cards">
      <div v-if="recentBrowseHistory?.length" class="recent-card" @click="$emit('switch-tab', 'history')">
        <div class="recent-card-header">
          <span class="recent-card-title">最近浏览</span>
          <el-icon class="recent-card-arrow">
<ArrowRight />
</el-icon>
        </div>
        <div v-for="item in recentBrowseHistory" :key="item.id" class="recent-item">
          <span class="recent-item-title">{{ item.title || item.targetTitle || '未知内容' }}</span>
          <span class="recent-item-time">{{ formatRelativeTime(item.createdAt || item.createTime) }}</span>
        </div>
      </div>
      <div v-if="recentComments?.length" class="recent-card" @click="$emit('switch-tab', 'comments')">
        <div class="recent-card-header">
          <span class="recent-card-title">最近评论</span>
          <el-icon class="recent-card-arrow">
<ArrowRight />
</el-icon>
        </div>
        <div v-for="item in recentComments" :key="item.id" class="recent-item">
          <span class="recent-item-title">{{ truncateText(item.content, 20) }}</span>
          <span class="recent-item-time">{{ formatRelativeTime(item.createdAt || item.createTime) }}</span>
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
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({
  userName: { type: String, default: '侗乡医药用户' },
  isAdmin: { type: Boolean, default: false },
  studyStats: { type: Array, required: true },
  commentCount: { type: Number, default: 0 },
  recentBrowseHistory: { type: Array, default: () => [] },
  recentComments: { type: Array, default: () => [] },
  actions: { type: Array, required: true }
})

defineEmits(['tab-change', 'switch-tab'])

function truncateText(text, maxLen) {
  if (!text) return ''
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
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
.recent-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-md);
  margin-top: var(--space-xl);
}
.recent-card {
  background: var(--text-inverse);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  border: 1px solid var(--border-light, #eee);
  cursor: pointer;
  transition: box-shadow var(--transition-fast);
}
.recent-card:hover {
  box-shadow: var(--shadow-sm);
}
.recent-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-sm);
}
.recent-card-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--dong-indigo);
}
.recent-card-arrow {
  font-size: 14px;
  color: var(--text-muted);
}
.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-xs) 0;
  font-size: var(--font-size-xs);
  border-bottom: 1px solid var(--border-light, #f5f5f5);
}
.recent-item:last-child {
  border-bottom: none;
}
.recent-item-title {
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: var(--space-sm);
}
.recent-item-time {
  color: var(--text-muted);
  font-size: 11px;
  flex-shrink: 0;
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
  .recent-cards { grid-template-columns: 1fr; gap: var(--space-sm); margin-top: var(--space-md); }
  .quick-actions { grid-template-columns: 1fr; gap: var(--space-sm); margin-top: var(--space-md); }
  .action-card { padding: var(--space-md); }
}

@media (max-width: 480px) {
  .profile-card { padding: var(--space-sm); }
  .stat-block { flex: 1 1 45%; }
}
</style>
