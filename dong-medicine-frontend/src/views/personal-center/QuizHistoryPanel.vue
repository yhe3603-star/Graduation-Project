<template>
  <div>
    <div class="record-list">
      <div v-for="record in paginated" :key="record.id + record.type" class="record-item">
        <div class="record-score" :class="getScoreClass(record.score)">
{{ record.score }}分
</div>
        <div class="record-info">
          <p class="record-title">
{{ record.type === 'quiz' ? '趣味答题' : '植物识别 · ' + getDifficultyName(record.difficulty) }}
</p>
          <p class="record-time">
{{ formatTime(record.createTime) }}
</p>
        </div>
        <span class="record-stats">正确 {{ record.correctCount }}/{{ record.totalCount }}</span>
      </div>
    </div>
    <el-empty v-if="!allRecords.length" description="暂无答题记录" />
    <Pagination v-else-if="allRecords.length > pageSize" :page="page" :size="pageSize" :total="allRecords.length" @update:page="page = $event" @update:size="pageSize = $event" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Pagination from '@/components/business/display/Pagination.vue'

const props = defineProps({
  quizRecords: { type: Array, default: () => [] },
  gameRecords: { type: Array, default: () => [] }
})

const page = ref(1)
const pageSize = ref(6)

const allRecords = computed(() => {
  const quiz = props.quizRecords.map(r => ({ ...r, type: 'quiz' }))
  const game = props.gameRecords.map(r => ({ ...r, type: 'game' }))
  return [...quiz, ...game].sort((a, b) => new Date(b.createdAt || b.createTime) - new Date(a.createdAt || a.createTime))
})

const paginated = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return allRecords.value.slice(start, start + pageSize.value)
})

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

function getDifficultyName(d) {
  return { easy: '简单', medium: '中等', hard: '困难' }[d] || d
}

function getScoreClass(score) {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}
</script>

<style scoped>
.record-list { display: flex; flex-direction: column; gap: var(--space-md); }
.record-item { display: flex; align-items: center; gap: var(--space-xl); padding: var(--space-md); background: var(--bg-rice); border-radius: var(--radius-md); }
.record-score { width: 60px; height: 60px; border-radius: var(--radius-full); display: flex; align-items: center; justify-content: center; font-weight: var(--font-weight-bold); color: #ffffff; font-size: 18px; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3); }
.record-score.score-high { background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark)); }
.record-score.score-medium { background: linear-gradient(135deg, var(--dong-gold-light), var(--dong-gold)); }
.record-score.score-low { background: linear-gradient(135deg, var(--color-info), #2980b9); }
.record-info { flex: 1; }
.record-title { margin: 0 0 var(--space-xs) 0; font-weight: var(--font-weight-medium); }
.record-time { margin: 0; font-size: var(--font-size-sm); color: var(--text-muted); }
.record-stats { font-size: var(--font-size-sm); color: var(--text-secondary); }

@media (max-width: 768px) {
  .record-item { flex-wrap: wrap; gap: var(--space-md); }
  .record-score { width: 50px; height: 50px; font-size: 15px; }
}
@media (max-width: 480px) {
  .record-item { flex-direction: column; align-items: flex-start; }
  .record-score { width: 44px; height: 44px; font-size: 14px; }
}
</style>
