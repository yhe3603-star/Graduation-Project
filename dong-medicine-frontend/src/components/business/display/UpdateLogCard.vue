<template>
  <el-card
    class="update-log-card"
    shadow="hover"
  >
    <template #header>
      <div class="card-header-title">
        <el-icon><Clock /></el-icon>
        <span>{{ title }}</span>
        <el-button
          v-if="editable"
          type="primary"
          size="small"
          @click="handleAddLog"
        >
          <el-icon><Plus /></el-icon>添加日志
        </el-button>
      </div>
    </template>
    
    <div
      v-if="logs.length > 0"
      class="log-list"
    >
      <el-timeline>
        <el-timeline-item 
          v-for="(log, index) in displayLogs" 
          :key="log.id || index"
          :timestamp="formatTime(log.time)" 
          placement="top"
          :color="getLogColor(index)"
        >
          <div class="log-item">
            <p class="log-content">
              {{ log.content }}
            </p>
            <div class="log-meta">
              <span
                v-if="log.operator"
                class="log-operator"
              >
                <el-icon><User /></el-icon>{{ log.operator }}
              </span>
              <div
                v-if="editable"
                class="log-actions"
              >
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEditLog(log)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteLog(log)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      
      <div
        v-if="logs.length > limit"
        class="show-more"
      >
        <el-button
          type="primary"
          class="view-all-btn"
          @click="showAll = !showAll"
        >
          {{ showAll ? '收起' : `查看全部 (${logs.length})` }}
          <el-icon><component :is="showAll ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
        </el-button>
      </div>
    </div>
    
    <el-empty
      v-else
      :description="emptyText"
      :image-size="60"
    />
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Clock, Plus, Edit, Delete, User, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { useUpdateLog } from '@/composables/useUpdateLog'

const props = defineProps({
  logs: { type: Array, default: () => [] },
  title: { type: String, default: '更新日志' },
  limit: { type: Number, default: 5 },
  editable: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无更新记录' }
})

const emit = defineEmits(['add', 'edit', 'delete'])

const { formatLogTime } = useUpdateLog()
const showAll = ref(false)

const displayLogs = computed(() => {
  if (showAll.value || props.logs.length <= props.limit) {
    return props.logs
  }
  return props.logs.slice(0, props.limit)
})

const formatTime = (time) => formatLogTime(time)

const getLogColor = (index) => {
  const colors = ['#1A5276', '#28B463', '#F39C12', '#E74C3C', '#9B59B6']
  return colors[index % colors.length]
}

const handleAddLog = () => emit('add')

const handleEditLog = (log) => emit('edit', log)

const handleDeleteLog = (log) => {
  ElMessageBox.confirm('确定要删除这条日志记录吗？', '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('delete', log)
    ElMessage.success('删除成功')
  }).catch((e) => {
    console.debug('用户取消删除日志:', e)
  })
}
</script>

<style scoped>
.update-log-card { margin-top: 16px; }
.card-header-title { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.card-header-title .el-button { margin-left: auto; }
.log-list { max-height: 400px; overflow-y: auto; }
.log-item { padding: 4px 0; }
.log-content { margin: 0 0 8px 0; font-size: 14px; color: #333; line-height: 1.6; }
.log-meta { display: flex; align-items: center; justify-content: space-between; }
.log-operator { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--text-muted); }
.log-actions { display: flex; gap: 4px; }
.show-more { text-align: center; padding: 12px 0 0; border-top: 1px dashed #e8e8e8; margin-top: 12px; }
.view-all-btn { font-size: 14px; color: var(--text-inverse); background: linear-gradient(135deg, #1A5276, var(--dong-indigo-dark)); padding: 8px 16px; border-radius: 8px; }
.view-all-btn:hover { background: linear-gradient(135deg, var(--dong-indigo-dark), #1A5276); }
</style>
