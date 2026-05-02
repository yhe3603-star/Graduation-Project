<template>
  <div v-loading="loading" class="stats-dashboard">
    <div class="stats-cards">
      <div v-for="(s, i) in statItems" :key="i" class="stats-card-item">
        <div class="stats-card-icon" :style="{ background: s.bg }">
          <el-icon><component :is="s.icon" /></el-icon>
        </div>
        <div class="stats-card-body">
          <div class="stats-card-value">
{{ s.value }}
</div>
          <div class="stats-card-label">
{{ s.label }}
</div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <h3 class="chart-title">
        <el-icon><TrendCharts /></el-icon>成绩趋势
      </h3>
      <div ref="chartRef" class="score-chart" />
      <el-empty v-if="!hasScoreData && !loading" description="暂无答题数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { EditPen, Trophy, Cherry, Star, View, TrendCharts } from '@element-plus/icons-vue'
import { useStudyStats } from '@/composables/useStudyStats'

const props = defineProps({
  quizRecords: { type: Array, default: () => [] },
  gameRecords: { type: Array, default: () => [] },
  favorites: { type: Array, default: () => [] },
  browseHistory: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

const { studyStats, hasScoreData, scoreChartRef: chartRef, computeStudyStats, initScoreChart, disposeChart } = useStudyStats()

const statItems = ref([])

function updateStats() {
  computeStudyStats(props.quizRecords, props.gameRecords, props.favorites, props.browseHistory)
  statItems.value = [
    { value: studyStats.value[0]?.value || 0, label: '总答题次数', icon: EditPen, bg: 'linear-gradient(135deg, #e74c3c, #c0392b)' },
    { value: (studyStats.value[1]?.value || 0) + '分', label: '平均得分', icon: Trophy, bg: 'linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark))' },
    { value: studyStats.value[2]?.value || 0, label: '植物游戏次数', icon: Cherry, bg: 'linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark))' },
    { value: studyStats.value[3]?.value || 0, label: '收藏总数', icon: Star, bg: 'linear-gradient(135deg, var(--dong-gold-light), var(--dong-copper))' },
    { value: studyStats.value[4]?.value || 0, label: '浏览总数', icon: View, bg: 'linear-gradient(135deg, #9b59b6, #8e44ad)' }
  ]
  nextTick(() => initScoreChart(props.quizRecords, props.gameRecords))
}

watch(() => [props.quizRecords, props.gameRecords, props.favorites, props.browseHistory], updateStats, { deep: true })

onMounted(updateStats)
onUnmounted(disposeChart)
</script>

<style scoped>
.stats-dashboard { display: flex; flex-direction: column; gap: var(--space-xl); }
.stats-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: var(--space-md); }
.stats-card-item { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-lg); background: var(--bg-rice); border-radius: var(--radius-md); transition: transform var(--transition-fast); }
.stats-card-item:hover { transform: translateY(-2px); box-shadow: var(--shadow-sm); }
.stats-card-icon { width: 44px; height: 44px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; color: var(--text-inverse); font-size: 20px; flex-shrink: 0; }
.stats-card-value { font-size: var(--font-size-xl); font-weight: var(--font-weight-bold); color: var(--text-primary); }
.stats-card-label { font-size: var(--font-size-xs); color: var(--text-muted); margin-top: 2px; }
.chart-section { background: var(--bg-rice); border-radius: var(--radius-md); padding: var(--space-lg); }
.chart-title { display: flex; align-items: center; gap: var(--space-sm); margin: 0 0 var(--space-lg) 0; font-size: var(--font-size-md); font-weight: var(--font-weight-semibold); color: var(--dong-indigo); }
.score-chart { width: 100%; height: 350px; }

@media (max-width: 1024px) { .stats-cards { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); } }
@media (max-width: 768px) {
  .stats-cards { grid-template-columns: 1fr 1fr; gap: var(--space-sm); }
  .stats-card-item { padding: var(--space-md); }
  .stats-card-icon { width: 36px; height: 36px; font-size: 16px; }
  .stats-card-value { font-size: var(--font-size-lg); }
  .score-chart { height: 280px; }
}
@media (max-width: 480px) {
  .stats-cards { grid-template-columns: 1fr; }
  .score-chart { height: 220px; }
}
</style>
