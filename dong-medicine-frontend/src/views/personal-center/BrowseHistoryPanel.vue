<template>
  <div>
    <div v-loading="historyLoading" class="history-list">
      <div v-for="item in paginatedHistory" :key="item.id" class="history-item" @click="goToHistoryItem(item)">
        <div class="history-icon">
          <el-icon><component :is="getHistoryIcon(item.targetType)" /></el-icon>
        </div>
        <div class="history-content">
          <h4>{{ item.title || item.targetTitle || item.targetName || item.targetType }}</h4>
          <p class="history-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(item.createTime || item.viewTime) }}
          </p>
        </div>
        <el-tag size="small" :type="getTypeTag(item.targetType)">
{{ getTypeName(item.targetType) }}
</el-tag>
        <el-icon class="history-arrow">
<ArrowRight />
</el-icon>
      </div>
    </div>
    <el-empty v-if="!historyLoading && !browseHistory.length" description="暂无浏览记录" />
    <Pagination v-if="browseHistory.length > historyPageSize" :page="historyPage" :size="historyPageSize" :total="browseHistory.length" @update:page="historyPage = $event" @update:size="historyPageSize = $event" />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Clock, ArrowRight } from '@element-plus/icons-vue'
import Pagination from '@/components/business/display/Pagination.vue'
import { useBrowseHistory } from '@/composables/useBrowseHistory'
import { typeTagMap, typeNameMap } from '@/composables/usePersonalCenter'

const { browseHistory, historyLoading, historyPage, historyPageSize, paginatedHistory, getHistoryIcon, goToHistoryItem, loadBrowseHistory } = useBrowseHistory()

function getTypeTag(type) { return typeTagMap[type] || 'info' }
function getTypeName(type) { return typeNameMap[type] || '其他' }

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(loadBrowseHistory)
</script>

<style scoped>
.history-list { display: flex; flex-direction: column; gap: var(--space-md); }
.history-item { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-md); background: var(--bg-rice); border-radius: var(--radius-md); cursor: pointer; transition: background var(--transition-fast); }
.history-item:hover { background: var(--bg-rice-dark); }
.history-icon { width: 40px; height: 40px; background: linear-gradient(135deg, #9b59b6, #8e44ad); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: var(--text-inverse); flex-shrink: 0; }
.history-content { flex: 1; }
.history-content h4 { margin: 0 0 var(--space-xs) 0; font-size: var(--font-size-sm); color: var(--text-primary); }
.history-time { margin: 0; font-size: var(--font-size-xs); color: var(--text-muted); display: flex; align-items: center; gap: var(--space-xs); }
.history-arrow { color: var(--text-light); font-size: var(--font-size-base); }

@media (max-width: 768px) {
  .history-item { gap: var(--space-sm); }
  .history-icon { width: 36px; height: 36px; }
}
@media (max-width: 480px) {
  .history-item { flex-wrap: wrap; gap: var(--space-sm); }
}
</style>
