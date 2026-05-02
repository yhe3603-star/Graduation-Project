# PersonalCenter.vue 组件拆分实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 1131 行的 PersonalCenter.vue 拆分为 1 容器 + 6 子组件 + 2 composables

**Architecture:** 从 PersonalCenter.vue 中提取代码到独立文件，不重写逻辑，只做代码迁移。子组件通过 props 接收数据，通过 emits 回传事件。容器组件只做布局编排。

**Tech Stack:** Vue 3 SFC + Element Plus + ECharts + 已有 usePersonalCenter composable

**Source file:** `dong-medicine-frontend/src/views/PersonalCenter.vue` (1131 lines)
**Target dir:** `dong-medicine-frontend/src/views/personal-center/`

---

### Task 1: Create `useStudyStats.js` composable

**Files:**
- Create: `dong-medicine-frontend/src/composables/useStudyStats.js`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract study stats computation + ECharts chart logic (lines 327-535 of PersonalCenter.vue).

- [ ] **Step 1: Write useStudyStats.js**

```js
import { ref, computed, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'

export function useStudyStats() {
  const studyStatsRaw = ref({
    totalQuizAttempts: 0,
    averageScore: 0,
    totalGameAttempts: 0,
    totalFavorites: 0,
    totalBrowseCount: 0
  })

  const studyStats = computed(() => [
    { value: studyStatsRaw.value.totalQuizAttempts, label: '总答题次数' },
    { value: Math.round(studyStatsRaw.value.averageScore), label: '平均得分' },
    { value: studyStatsRaw.value.totalGameAttempts, label: '植物游戏次' },
    { value: studyStatsRaw.value.totalFavorites, label: '收藏总数' },
    { value: studyStatsRaw.value.totalBrowseCount, label: '浏览总数' }
  ])

  function computeStudyStats(quizRecords, gameRecords, favorites, browseHistory) {
    const quizTotal = quizRecords.length
    const gameTotal = gameRecords.length
    const totalAttempts = quizTotal + gameTotal

    let totalScore = 0
    let scoreCount = 0
    quizRecords.forEach(r => {
      if (r.score !== undefined && r.score !== null) {
        totalScore += Number(r.score)
        scoreCount++
      }
    })
    gameRecords.forEach(r => {
      if (r.score !== undefined && r.score !== null) {
        totalScore += Number(r.score)
        scoreCount++
      }
    })
    const avgScore = scoreCount > 0 ? totalScore / scoreCount : 0

    studyStatsRaw.value = {
      totalQuizAttempts: totalAttempts,
      averageScore: avgScore,
      totalGameAttempts: gameTotal,
      totalFavorites: favorites.length,
      totalBrowseCount: browseHistory.length
    }
  }

  const scoreChartRef = ref(null)
  let scoreChartInstance = null
  let scoreResizeObserver = null
  const hasScoreData = ref(false)

  function buildScoreTrendData(quizRecords, gameRecords) {
    const quizPoints = quizRecords.map(r => ({
      date: (r.createdAt || r.createTime) ? new Date(r.createdAt || r.createTime).toISOString().slice(0, 10) : '',
      score: Number(r.score || 0),
      type: 'quiz'
    }))
    const gamePoints = gameRecords.map(r => ({
      date: (r.createdAt || r.createTime) ? new Date(r.createdAt || r.createTime).toISOString().slice(0, 10) : '',
      score: Number(r.score || 0),
      type: 'game'
    }))
    const allPoints = [...quizPoints, ...gamePoints].filter(p => p.date).sort((a, b) => a.date.localeCompare(b.date))

    if (allPoints.length === 0) {
      hasScoreData.value = false
      return { dates: [], quizScores: [], gameScores: [] }
    }

    hasScoreData.value = true
    const dateMap = new Map()
    allPoints.forEach(p => {
      if (!dateMap.has(p.date)) dateMap.set(p.date, { quizScores: [], gameScores: [] })
      const entry = dateMap.get(p.date)
      if (p.type === 'quiz') entry.quizScores.push(p.score)
      else entry.gameScores.push(p.score)
    })

    const dates = [...dateMap.keys()].sort()
    const quizScores = dates.map(d => {
      const arr = dateMap.get(d).quizScores
      return arr.length > 0 ? Math.round(arr.reduce((a, b) => a + b, 0) / arr.length) : null
    })
    const gameScores = dates.map(d => {
      const arr = dateMap.get(d).gameScores
      return arr.length > 0 ? Math.round(arr.reduce((a, b) => a + b, 0) / arr.length) : null
    })
    return { dates, quizScores, gameScores }
  }

  function initScoreChart(quizRecords, gameRecords) {
    if (!scoreChartRef.value) return
    if (scoreChartInstance) scoreChartInstance.dispose()

    scoreChartInstance = echarts.init(scoreChartRef.value)
    const { dates, quizScores, gameScores } = buildScoreTrendData(quizRecords, gameRecords)

    if (!dates.length) {
      scoreChartInstance = null
      return
    }

    scoreChartInstance.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          let result = `<b>${params[0].axisValue}</b><br/>`
          params.forEach(p => {
            if (p.value !== null) result += `${p.marker} ${p.seriesName}: ${p.value}分<br/>`
          })
          return result
        }
      },
      legend: {
        data: ['趣味答题', '植物识别'],
        bottom: 0,
        textStyle: { color: 'var(--text-secondary, #555)', fontSize: 12 }
      },
      grid: { left: '3%', right: '4%', top: '10%', bottom: '15%', containLabel: true },
      xAxis: {
        type: 'category', data: dates, boundaryGap: false,
        axisLabel: { color: 'var(--text-muted, #888)', fontSize: 10, rotate: 30 },
        axisLine: { lineStyle: { color: 'var(--border-light, #eee)' } }
      },
      yAxis: {
        type: 'value', name: '分数', min: 0, max: 100,
        axisLabel: { color: 'var(--text-muted, #888)' },
        splitLine: { lineStyle: { color: 'var(--border-light, #eee)', type: 'dashed' } }
      },
      series: [
        {
          name: '趣味答题', type: 'line', data: quizScores, smooth: true, connectNulls: true,
          symbol: 'circle', symbolSize: 6,
          lineStyle: { color: '#3498db', width: 2 }, itemStyle: { color: '#3498db' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(52, 152, 219, 0.3)' },
              { offset: 1, color: 'rgba(52, 152, 219, 0.02)' }
            ])
          }
        },
        {
          name: '植物识别', type: 'line', data: gameScores, smooth: true, connectNulls: true,
          symbol: 'diamond', symbolSize: 6,
          lineStyle: { color: '#28B463', width: 2 }, itemStyle: { color: '#28B463' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(40, 180, 99, 0.3)' },
              { offset: 1, color: 'rgba(40, 180, 99, 0.02)' }
            ])
          }
        }
      ]
    })

    if (scoreResizeObserver) scoreResizeObserver.disconnect()
    scoreResizeObserver = new ResizeObserver(() => {
      if (scoreChartInstance && !scoreChartInstance.isDisposed()) scoreChartInstance.resize()
    })
    scoreResizeObserver.observe(scoreChartRef.value)
  }

  function disposeChart() {
    if (scoreResizeObserver) { scoreResizeObserver.disconnect(); scoreResizeObserver = null }
    if (scoreChartInstance && !scoreChartInstance.isDisposed()) { scoreChartInstance.dispose(); scoreChartInstance = null }
  }

  return { studyStatsRaw, studyStats, hasScoreData, scoreChartRef, computeStudyStats, initScoreChart, disposeChart }
}
```

- [ ] **Step 2: Remove extracted code from PersonalCenter.vue**

Delete lines 327-535 (study stats raw, studyStats computed, computeStudyStats, buildScoreTrendData, initScoreChart, scoreChartRef, hasScoreData). Also remove ECharts and ResizeObserver cleanup from onUnmounted at lines 578-587.

- [ ] **Step 3: Import useStudyStats in PersonalCenter.vue script**

Add import at top of script:
```js
import { useStudyStats } from '@/composables/useStudyStats'
```

Replace the study stats related code with:
```js
const { studyStats, hasScoreData, scoreChartRef, computeStudyStats, initScoreChart, disposeChart } = useStudyStats()
```

And update onUnmounted:
```js
onUnmounted(() => {
  disposeChart()
})
```

And update the watch and watch(activeTab) to pass quizRecords/gameRecords as params:
```js
watch(
  () => [quizRecords.value, gameRecords.value, browseHistory.value, favorites.value],
  () => {
    computeStudyStats(quizRecords.value, gameRecords.value, favorites.value, browseHistory.value)
    nextTick(() => initScoreChart(quizRecords.value, gameRecords.value))
  },
  { deep: true }
)
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/composables/useStudyStats.js dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract useStudyStats composable from PersonalCenter"
```

---

### Task 2: Create `useBrowseHistory.js` composable

**Files:**
- Create: `dong-medicine-frontend/src/composables/useBrowseHistory.js`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract browse history logic (lines 279-324 of PersonalCenter.vue).

- [ ] **Step 1: Write useBrowseHistory.js**

```js
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Star, EditPen, Cherry } from '@element-plus/icons-vue'
import request from '@/utils/request'

const historyTypeIcons = {
  plant: Cherry,
  knowledge: Document,
  inheritor: User,
  resource: Star,
  qa: EditPen
}

const typeIconMap = {
  plant: Cherry,
  knowledge: Document,
  inheritor: User,
  resource: Star,
  qa: EditPen
}

export function useBrowseHistory() {
  const router = useRouter()
  const browseHistory = ref([])
  const historyLoading = ref(false)
  const historyPage = ref(1)
  const historyPageSize = ref(10)

  const paginatedHistory = computed(() => {
    const start = (historyPage.value - 1) * historyPageSize.value
    return browseHistory.value.slice(start, start + historyPageSize.value)
  })

  function getHistoryIcon(type) {
    return historyTypeIcons[type] || typeIconMap.knowledge
  }

  function goToHistoryItem(item) {
    const pathMap = {
      plant: '/plants',
      knowledge: '/knowledge',
      inheritor: '/inheritors',
      resource: '/resources',
      qa: '/qa'
    }
    const basePath = pathMap[item.targetType]
    if (basePath) {
      router.push(`${basePath}?id=${item.targetId || item.id}`)
    }
  }

  async function loadBrowseHistory() {
    historyLoading.value = true
    try {
      const res = await request.get('/browse-history/my')
      browseHistory.value = res.data || res || []
    } catch (e) {
      console.error('加载浏览历史失败:', e)
      browseHistory.value = []
    } finally {
      historyLoading.value = false
    }
  }

  return { browseHistory, historyLoading, historyPage, historyPageSize, paginatedHistory, getHistoryIcon, goToHistoryItem, loadBrowseHistory }
}
```

- [ ] **Step 2: Remove extracted code from PersonalCenter.vue**

Delete lines 279-324 (browseHistory through loadBrowseHistory). Keep the `watch(activeTab)` handler that calls `loadBrowseHistory()` when tab='history'.

- [ ] **Step 3: Import useBrowseHistory in PersonalCenter.vue script**

```js
import { useBrowseHistory } from '@/composables/useBrowseHistory'
```

Add inside setup:
```js
const { browseHistory, historyLoading, historyPage, historyPageSize, paginatedHistory, getHistoryIcon, goToHistoryItem, loadBrowseHistory } = useBrowseHistory()
```

Also remove the icon imports that are now only needed in the composable: `Clock, ArrowRight` from the import line (keep if used elsewhere, check). Remove Document import from Element Plus icons if only used by history.

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/composables/useBrowseHistory.js dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract useBrowseHistory composable from PersonalCenter"
```

---

### Task 3: Create `ProfileSection.vue`

**Files:**
- Create: `dong-medicine-frontend/src/views/personal-center/ProfileSection.vue`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract profile card + quick actions section (template lines 18-44 of original).

- [ ] **Step 1: Write ProfileSection.vue**

```vue
<template>
  <div class="profile-section">
    <div class="profile-card">
      <div class="avatar-section">
        <el-avatar :size="100" class="user-avatar">{{ userName?.charAt(0) || '用' }}</el-avatar>
        <div class="user-info">
          <h2 class="user-name">{{ userName || '侗乡医药用户' }}</h2>
          <el-tag :type="isAdmin ? 'warning' : 'success'" effect="dark">{{ isAdmin ? '管理员' : '普通用户' }}</el-tag>
        </div>
      </div>
      <el-divider />
      <div class="stats-row">
        <div class="stat-block" v-for="s in studyStats" :key="s.label">
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
```

- [ ] **Step 2: Replace template section in PersonalCenter.vue**

Replace the profile section in template (lines 18-44 in original) with:
```vue
<ProfileSection
  :user-name="userName"
  :is-admin="isAdmin"
  :study-stats="studyStats"
  :actions="actions"
  @tab-change="activeTab = $event"
/>
```

Add import:
```js
import ProfileSection from './personal-center/ProfileSection.vue'
```

- [ ] **Step 3: Run build to verify**

```bash
cd dong-medicine-frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/views/personal-center/ProfileSection.vue dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract ProfileSection from PersonalCenter"
```

---

### Task 4: Create `StatsDashboard.vue`

**Files:**
- Create: `dong-medicine-frontend/src/views/personal-center/StatsDashboard.vue`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract study stats tab (template lines 50-109 of original).

- [ ] **Step 1: Write StatsDashboard.vue**

```vue
<template>
  <div class="stats-dashboard" v-loading="loading">
    <div class="stats-cards">
      <div class="stats-card-item" v-for="(s, i) in statItems" :key="i">
        <div class="stats-card-icon" :style="{ background: s.bg }">
          <el-icon><component :is="s.icon" /></el-icon>
        </div>
        <div class="stats-card-body">
          <div class="stats-card-value">{{ s.value }}</div>
          <div class="stats-card-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <h3 class="chart-title">
        <el-icon><TrendCharts /></el-icon>成绩趋势
      </h3>
      <div ref="chartRef" class="score-chart"></div>
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
```

- [ ] **Step 2: Replace stats tab in PersonalCenter.vue template**

Replace `<el-tab-pane label="学习统计" name="stats">...</el-tab-pane>` with:
```vue
<el-tab-pane label="学习统计" name="stats">
  <StatsDashboard
    :quiz-records="quizRecords"
    :game-records="gameRecords"
    :favorites="favorites"
    :browse-history="browseHistory"
    :loading="statsLoading"
  />
</el-tab-pane>
```

Add import:
```js
import StatsDashboard from './personal-center/StatsDashboard.vue'
```

Also remove the now-unused `statsLoading` ref from PersonalCenter.vue (the StatsDashboard manages its own loading from props) — OR keep it if still used for data fetching in the watch(activeTab).

- [ ] **Step 3: Run build to verify**

```bash
cd dong-medicine-frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/views/personal-center/StatsDashboard.vue dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract StatsDashboard from PersonalCenter"
```

---

### Task 5: Create `FavoritesPanel.vue`

**Files:**
- Create: `dong-medicine-frontend/src/views/personal-center/FavoritesPanel.vue`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract favorites tab (template lines 112-137 of original).

- [ ] **Step 1: Write FavoritesPanel.vue**

```vue
<template>
  <div>
    <div class="tab-header">
      <el-radio-group v-model="typeFilter" size="small">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="plant">药材</el-radio-button>
        <el-radio-button value="knowledge">知识</el-radio-button>
        <el-radio-button value="inheritor">传承人</el-radio-button>
        <el-radio-button value="resource">学习资源</el-radio-button>
        <el-radio-button value="qa">问答</el-radio-button>
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
        <el-tag size="small" :type="getTypeTag(item.type)">{{ getTypeName(item.type) }}</el-tag>
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

@media (max-width: 768px) {
  .fav-icon { width: 36px; height: 36px; }
}
@media (max-width: 480px) {
  .favorite-card { flex-wrap: wrap; }
}
</style>
```

- [ ] **Step 2: Replace favorites tab in PersonalCenter.vue template**

Replace `<el-tab-pane label="我的收藏" name="favorites">...</el-tab-pane>` with:
```vue
<el-tab-pane label="我的收藏" name="favorites">
  <FavoritesPanel :favorites="favorites" @go-detail="goToDetail" />
</el-tab-pane>
```

Add import:
```js
import FavoritesPanel from './personal-center/FavoritesPanel.vue'
```

Remove the inline `getTypeIcon`, `getTypeTag`, `getTypeName` helper wrappers from PersonalCenter.vue (they're now in FavoritesPanel.vue and usePersonalCenter composable already exports them).

- [ ] **Step 3: Run build to verify**

```bash
cd dong-medicine-frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/views/personal-center/FavoritesPanel.vue dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract FavoritesPanel from PersonalCenter"
```

---

### Task 6: Create `QuizHistoryPanel.vue`

**Files:**
- Create: `dong-medicine-frontend/src/views/personal-center/QuizHistoryPanel.vue`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract quiz records tab (template lines 139-153 of original).

- [ ] **Step 1: Write QuizHistoryPanel.vue**

```vue
<template>
  <div>
    <div class="record-list">
      <div v-for="record in paginated" :key="record.id + record.type" class="record-item">
        <div class="record-score" :class="getScoreClass(record.score)">{{ record.score }}分</div>
        <div class="record-info">
          <p class="record-title">{{ record.type === 'quiz' ? '趣味答题' : '植物识别 · ' + getDifficultyName(record.difficulty) }}</p>
          <p class="record-time">{{ formatTime(record.createTime) }}</p>
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
```

- [ ] **Step 2: Replace quiz tab in PersonalCenter.vue template**

Replace `<el-tab-pane label="答题记录" name="quiz">...</el-tab-pane>` with:
```vue
<el-tab-pane label="答题记录" name="quiz">
  <QuizHistoryPanel :quiz-records="quizRecords" :game-records="gameRecords" />
</el-tab-pane>
```

Add import:
```js
import QuizHistoryPanel from './personal-center/QuizHistoryPanel.vue'
```

- [ ] **Step 3: Run build to verify**

```bash
cd dong-medicine-frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/views/personal-center/QuizHistoryPanel.vue dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract QuizHistoryPanel from PersonalCenter"
```

---

### Task 7: Create `BrowseHistoryPanel.vue`

**Files:**
- Create: `dong-medicine-frontend/src/views/personal-center/BrowseHistoryPanel.vue`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract browse history tab (template lines 156-175 of original).

- [ ] **Step 1: Write BrowseHistoryPanel.vue**

```vue
<template>
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
      <el-tag size="small" :type="getTypeTag(item.targetType)">{{ getTypeName(item.targetType) }}</el-tag>
      <el-icon class="history-arrow"><ArrowRight /></el-icon>
    </div>
  </div>
  <el-empty v-if="!historyLoading && !browseHistory.length" description="暂无浏览记录" />
  <Pagination v-if="browseHistory.length > historyPageSize" :page="historyPage" :size="historyPageSize" :total="browseHistory.length" @update:page="historyPage = $event" @update:size="historyPageSize = $event" />
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
```

- [ ] **Step 2: Replace history tab in PersonalCenter.vue template**

Replace `<el-tab-pane label="浏览历史" name="history">...</el-tab-pane>` with:
```vue
<el-tab-pane label="浏览历史" name="history">
  <BrowseHistoryPanel />
</el-tab-pane>
```

Add import:
```js
import BrowseHistoryPanel from './personal-center/BrowseHistoryPanel.vue'
```

Remove the inline browseHistory-related refs and functions from PersonalCenter.vue (they're now fully in useBrowseHistory + BrowseHistoryPanel). Also remove the `loadBrowseHistory()` call from onMounted and the watch(activeTab) handler for history.

- [ ] **Step 3: Run build to verify**

```bash
cd dong-medicine-frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/views/personal-center/BrowseHistoryPanel.vue dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract BrowseHistoryPanel from PersonalCenter"
```

---

### Task 8: Create `SettingsPanel.vue`

**Files:**
- Create: `dong-medicine-frontend/src/views/personal-center/SettingsPanel.vue`
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Extract settings tab (template lines 178-240 of original).

- [ ] **Step 1: Write SettingsPanel.vue**

```vue
<template>
  <div class="settings-container">
    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon><span>基本信息</span>
        </div>
      </template>
      <el-form label-width="80px" class="settings-form">
        <el-form-item label="用户名">
          <el-input :value="userName" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :value="isAdmin ? '管理员' : '普通用户'" disabled />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon><span>修改密码</span>
        </div>
      </template>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="settings-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item label="验证码" prop="captchaCode">
          <CaptchaInput
            ref="passwordCaptchaRef"
            v-model="passwordForm.captchaCode"
            @update:captcha-key="passwordForm.captchaKey = $event"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="$emit('change-password')">确认修改</el-button>
          <el-button @click="$emit('reset-password')">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><SwitchButton /></el-icon><span>退出登录</span>
        </div>
      </template>
      <div class="logout-section">
        <p class="logout-tip">点击下方按钮将退出当前账号，退出后需要重新登录才能访问个人功能。</p>
        <el-button type="danger" :loading="logoutLoading" @click="$emit('logout')">
          <el-icon><SwitchButton /></el-icon>退出登录
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { User, Lock, SwitchButton } from '@element-plus/icons-vue'
import CaptchaInput from '@/components/business/interact/CaptchaInput.vue'

defineProps({
  userName: { type: String, default: '' },
  isAdmin: { type: Boolean, default: false },
  passwordForm: { type: Object, required: true },
  passwordRules: { type: Object, required: true },
  passwordLoading: { type: Boolean, default: false },
  logoutLoading: { type: Boolean, default: false },
  passwordFormRef: { type: Object, default: null },
  passwordCaptchaRef: { type: Object, default: null }
})

defineEmits(['change-password', 'reset-password', 'logout'])
</script>

<style scoped>
.settings-container { display: flex; flex-direction: column; gap: var(--space-xl); }
.settings-card { border-radius: var(--radius-md); }
.card-header { display: flex; align-items: center; gap: var(--space-sm); font-weight: var(--font-weight-semibold); color: var(--dong-indigo); }
.settings-form { max-width: 400px; }
.logout-section { text-align: center; }
.logout-tip { color: var(--text-muted); font-size: var(--font-size-sm); margin-bottom: var(--space-lg); }

@media (max-width: 768px) { .settings-form { max-width: 100%; } }
@media (max-width: 480px) { .settings-form :deep(.el-form-item__label) { font-size: var(--font-size-sm); } }
</style>
```

- [ ] **Step 2: Replace settings tab in PersonalCenter.vue template**

Replace `<el-tab-pane label="修改密码" name="settings">...</el-tab-pane>` with:
```vue
<el-tab-pane label="修改密码" name="settings">
  <SettingsPanel
    :user-name="userName"
    :is-admin="isAdmin"
    :password-form="passwordForm"
    :password-rules="passwordRules"
    :password-loading="passwordLoading"
    :logout-loading="logoutLoading"
    :password-form-ref="passwordFormRef"
    :password-captcha-ref="passwordCaptchaRef"
    @change-password="handleChangePassword"
    @reset-password="resetPasswordForm"
    @logout="handleLogout"
  />
</el-tab-pane>
```

Add import:
```js
import SettingsPanel from './personal-center/SettingsPanel.vue'
```

Remove `Lock, User, SwitchButton` icon imports from PersonalCenter.vue (they're now in SettingsPanel). Also remove `CaptchaInput` import if it's no longer used directly in PersonalCenter.vue.

- [ ] **Step 3: Run build to verify**

```bash
cd dong-medicine-frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add dong-medicine-frontend/src/views/personal-center/SettingsPanel.vue dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: extract SettingsPanel from PersonalCenter"
```

---

### Task 9: Clean up PersonalCenter.vue container

**Files:**
- Modify: `dong-medicine-frontend/src/views/PersonalCenter.vue`

Final cleanup — remove dead imports, verify line count target met.

- [ ] **Step 1: Remove unused imports and refs**

Remove from script imports any icons/functions now only used in sub-components:
- `EditPen, Star, View, Clock, ArrowRight, TrendCharts, Trophy, Cherry` (now in StatsDashboard/QuizHistoryPanel/BrowseHistoryPanel)
- `echarts` (now in useStudyStats)
- `extractData` (if no longer used directly — check if quiz/game data fetching still uses it in watch(activeTab))

Remove refs/functions that have been fully extracted:
- All browseHistory-related refs (if BrowseHistoryPanel self-manages)
- getTypeIcon, getTypeTag, getTypeName wrappers (now in FavoritesPanel)

Remove the `onMounted(() => { loadBrowseHistory() })` call (BrowseHistoryPanel does it itself).

- [ ] **Step 2: Remove unused style blocks**

Delete from `<style scoped>` all styles that belong to extracted components (keep only `.personal-container`, `.not-logged-in`, `.empty-state`, `.empty-icon`, `.content-section`, responsive for `.personal-container`).

Expected remaining style: ~40 lines.

- [ ] **Step 3: Verify line counts**

```bash
wc -l dong-medicine-frontend/src/views/PersonalCenter.vue
wc -l dong-medicine-frontend/src/views/personal-center/*.vue
wc -l dong-medicine-frontend/src/composables/useStudyStats.js
wc -l dong-medicine-frontend/src/composables/useBrowseHistory.js
```

Target: PersonalCenter.vue <150 lines, each sub-component <200 lines.

- [ ] **Step 4: Run full build + tests**

```bash
cd dong-medicine-frontend && npm run build
cd dong-medicine-frontend && npm run test:run
cd dong-medicine-frontend && npm run lint
```

- [ ] **Step 5: Commit**

```bash
git add dong-medicine-frontend/src/views/PersonalCenter.vue
git commit -m "refactor: final cleanup of PersonalCenter container after extraction"
```

---

### Task 10: Final verification

- [ ] **Step 1: Run full test suite**

```bash
cd dong-medicine-frontend && npm run build && npm run test:run && npm run lint
```

All must pass.

- [ ] **Step 2: Report final file sizes**

```bash
echo "=== File sizes after refactoring ===" && wc -l dong-medicine-frontend/src/views/PersonalCenter.vue dong-medicine-frontend/src/views/personal-center/*.vue dong-medicine-frontend/src/composables/useStudyStats.js dong-medicine-frontend/src/composables/useBrowseHistory.js
```
