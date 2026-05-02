<template>
  <div>
    <div class="tab-header">
      <el-radio-group v-model="typeFilter" size="small">
        <el-radio-button value="all">
全部
</el-radio-button>
        <el-radio-button value="plant">
药材
</el-radio-button>
        <el-radio-button value="knowledge">
知识
</el-radio-button>
        <el-radio-button value="inheritor">
传承人
</el-radio-button>
        <el-radio-button value="resource">
学习资源
</el-radio-button>
        <el-radio-button value="qa">
问答
</el-radio-button>
      </el-radio-group>
    </div>
    <div class="favorites-grid">
      <div v-for="item in paginated" :key="item.id" class="favorite-card" @click="$emit('go-detail', item)">
        <div class="fav-icon">
          <el-icon><component :is="getTypeIcon(item.type)" /></el-icon>
        </div>
        <div class="fav-content">
          <h4>{{ item.title || item.nameCn || item.name || '未命名' }}</h4>
          <p>{{ (item.description || item.bio || item.efficacy || '').substring(0, 40) }}...</p>
        </div>
        <el-tag size="small" :type="getTypeTag(item.type)">
{{ getTypeName(item.type) }}
</el-tag>
      </div>
    </div>
    <el-empty v-if="!filtered.length" description="暂无收藏" />
    <Pagination v-else-if="filtered.length > pageSize" :page="page" :size="pageSize" :total="filtered.length" @update:page="page = $event" @update:size="pageSize = $event" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Picture, Document, User, Folder, ChatDotRound } from '@element-plus/icons-vue'
import Pagination from '@/components/business/display/Pagination.vue'

const typeIconMap = { plant: Picture, knowledge: Document, inheritor: User, resource: Folder, qa: ChatDotRound }
const typeTagMap = { plant: 'success', knowledge: 'primary', inheritor: 'warning', resource: 'info', qa: '' }
const typeNameMap = { plant: '药材', knowledge: '知识', inheritor: '传承人', resource: '资源', qa: '问答' }

const props = defineProps({
  favorites: { type: Array, default: () => [] }
})

defineEmits(['go-detail'])

const typeFilter = ref('all')
const page = ref(1)
const pageSize = ref(6)

const filtered = computed(() => {
  if (typeFilter.value === 'all') return props.favorites
  return props.favorites.filter(f => f.type === typeFilter.value)
})

const paginated = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

function getTypeIcon(type) { return typeIconMap[type] || typeIconMap.knowledge }
function getTypeTag(type) { return typeTagMap[type] || 'info' }
function getTypeName(type) { return typeNameMap[type] || '其他' }
</script>

<style scoped>
.tab-header { margin-bottom: var(--space-xl); }
.tab-header :deep(.el-radio-group) { flex-wrap: wrap; gap: 4px; }
.tab-header :deep(.el-radio-button__inner) { white-space: nowrap; }
.favorites-grid { display: flex; flex-direction: column; gap: var(--space-md); }
.favorite-card { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-md); background: var(--bg-rice); border-radius: var(--radius-md); cursor: pointer; }
.fav-icon { width: 40px; height: 40px; background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark)); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: var(--text-inverse); }
.fav-content { flex: 1; }
.fav-content h4 { margin: 0 0 var(--space-xs) 0; font-size: var(--font-size-sm); }
.fav-content p { margin: 0; font-size: var(--font-size-xs); color: var(--text-muted); }

@media (max-width: 768px) { .fav-icon { width: 36px; height: 36px; } }
@media (max-width: 480px) { .favorite-card { flex-wrap: wrap; } }
</style>
