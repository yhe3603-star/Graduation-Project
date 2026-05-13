<template>
  <div>
    <div class="tab-header">
      <el-radio-group v-model="statusFilter" size="small">
        <el-radio-button value="all">
全部
</el-radio-button>
        <el-radio-button value="approved">
已通过
</el-radio-button>
        <el-radio-button value="pending">
待审核
</el-radio-button>
        <el-radio-button value="rejected">
已拒绝
</el-radio-button>
      </el-radio-group>
    </div>
    <div v-loading="commentLoading" class="comment-list">
      <div v-for="item in paginatedComments" :key="item.id" class="comment-item">
        <div class="comment-status">
          <el-tag size="small" :type="statusTagMap[item.status] || 'info'">
{{ statusNameMap[item.status] || item.status }}
</el-tag>
        </div>
        <div class="comment-content">
          <p class="comment-text">{{ item.content }}</p>
          <div class="comment-meta">
            <el-tag size="small" :type="getTypeTag(item.targetType)" class="type-tag">
{{ getTypeName(item.targetType) }}
</el-tag>
            <span class="comment-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(item.createdAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-if="!commentLoading && !filteredComments.length" description="暂无评论记录" />
    <Pagination v-else-if="filteredComments.length > pageSize" :page="page" :size="pageSize" :total="filteredComments.length" @update:page="page = $event" @update:size="pageSize = $event" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import Pagination from '@/components/business/display/Pagination.vue'
import request from '@/utils/request'
import { typeTagMap, typeNameMap } from '@/composables/usePersonalCenter'

const commentLoading = ref(false)
const comments = ref([])
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(6)

const statusTagMap = {
  approved: 'success',
  pending: 'warning',
  rejected: 'danger'
}

const statusNameMap = {
  approved: '已通过',
  pending: '待审核',
  rejected: '已拒绝'
}

const filteredComments = computed(() => {
  if (statusFilter.value === 'all') return comments.value
  return comments.value.filter(c => c.status === statusFilter.value)
})

const paginatedComments = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredComments.value.slice(start, start + pageSize.value)
})

function getTypeTag(type) { return typeTagMap[type] || 'info' }
function getTypeName(type) { return typeNameMap[type] || '其他' }

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

async function loadComments() {
  commentLoading.value = true
  try {
    const res = await request.get('/comments/my', { params: { page: 1, size: 100 } })
    comments.value = res.data?.records || res.data?.list || res.data || []
  } catch {
    comments.value = []
  } finally {
    commentLoading.value = false
  }
}

onMounted(loadComments)
</script>

<style scoped>
.tab-header { margin-bottom: var(--space-xl); }
.tab-header :deep(.el-radio-group) { flex-wrap: wrap; gap: 4px; }
.tab-header :deep(.el-radio-button__inner) { white-space: nowrap; }
.comment-list { display: flex; flex-direction: column; gap: var(--space-md); }
.comment-item { display: flex; gap: var(--space-md); padding: var(--space-md); background: var(--bg-rice); border-radius: var(--radius-md); }
.comment-status { flex-shrink: 0; padding-top: 2px; }
.comment-content { flex: 1; }
.comment-text { margin: 0 0 var(--space-sm) 0; font-size: var(--font-size-sm); color: var(--text-primary); line-height: 1.6; word-break: break-all; }
.comment-meta { display: flex; align-items: center; gap: var(--space-md); }
.type-tag { flex-shrink: 0; }
.comment-time { margin: 0; font-size: var(--font-size-xs); color: var(--text-light); display: flex; align-items: center; gap: var(--space-xs); }

@media (max-width: 768px) {
  .comment-item { gap: var(--space-sm); }
}
@media (max-width: 480px) {
  .comment-item { flex-wrap: wrap; gap: var(--space-sm); }
}
</style>
