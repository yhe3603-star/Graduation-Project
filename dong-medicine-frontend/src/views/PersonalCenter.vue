<template>
  <div class="personal-center module-page">
    <div class="module-header">
      <h1>个人中心</h1>
      <p class="subtitle">我的信息 · 收藏管理 · 学习记录</p>
    </div>

    <div v-if="!isLoggedIn" class="not-logged-in">
      <div class="empty-state">
        <el-icon class="empty-icon"><User /></el-icon>
        <h2>您还未登录</h2>
        <p>登录后可查看个人收藏、答题记录、评论历史等内容</p>
        <el-button type="primary" size="large" @click="$router.push('/')">立即登录</el-button>
      </div>
    </div>

    <div v-else class="personal-container">
      <!-- Profile Section -->
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
          <!-- Study Stats Summary -->
          <div class="stats-row">
            <div class="stat-block" v-for="s in studyStats" :key="s.label">
              <span class="stat-num">{{ s.value }}</span>
              <span class="stat-text">{{ s.label }}</span>
            </div>
          </div>
        </div>

        <div class="quick-actions">
          <el-card v-for="action in actions" :key="action.key" class="action-card" shadow="hover" @click="activeTab = action.key">
            <el-icon><component :is="action.icon" /></el-icon>
            <span>{{ action.label }}</span>
          </el-card>
        </div>
      </div>

      <!-- Content Section -->
      <div class="content-section">
        <el-tabs v-model="activeTab" class="content-tabs">
          <!-- Study Stats Tab -->
          <el-tab-pane label="学习统计" name="stats">
            <div class="stats-dashboard" v-loading="statsLoading">
              <div class="stats-cards">
                <div class="stats-card-item">
                  <div class="stats-card-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                    <el-icon><EditPen /></el-icon>
                  </div>
                  <div class="stats-card-body">
                    <div class="stats-card-value">{{ studyStats[0]?.value || 0 }}</div>
                    <div class="stats-card-label">总答题次数</div>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon" style="background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));">
                    <el-icon><Trophy /></el-icon>
                  </div>
                  <div class="stats-card-body">
                    <div class="stats-card-value">{{ studyStats[1]?.value || 0 }}分</div>
                    <div class="stats-card-label">平均得分</div>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon" style="background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));">
                    <el-icon><Cherry /></el-icon>
                  </div>
                  <div class="stats-card-body">
                    <div class="stats-card-value">{{ studyStats[2]?.value || 0 }}</div>
                    <div class="stats-card-label">植物游戏次数</div>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon" style="background: linear-gradient(135deg, var(--dong-gold-light), var(--dong-copper));">
                    <el-icon><Star /></el-icon>
                  </div>
                  <div class="stats-card-body">
                    <div class="stats-card-value">{{ studyStats[3]?.value || 0 }}</div>
                    <div class="stats-card-label">收藏总数</div>
                  </div>
                </div>
                <div class="stats-card-item">
                  <div class="stats-card-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                    <el-icon><View /></el-icon>
                  </div>
                  <div class="stats-card-body">
                    <div class="stats-card-value">{{ studyStats[4]?.value || 0 }}</div>
                    <div class="stats-card-label">浏览总数</div>
                  </div>
                </div>
              </div>

              <!-- Score Trend Chart -->
              <div class="chart-section">
                <h3 class="chart-title">
                  <el-icon><TrendCharts /></el-icon>成绩趋势
                </h3>
                <div ref="scoreChartRef" class="score-chart"></div>
                <el-empty v-if="!hasScoreData && !statsLoading" description="暂无答题数据" :image-size="80" />
              </div>
            </div>
          </el-tab-pane>

          <!-- Favorites Tab -->
          <el-tab-pane label="我的收藏" name="favorites">
            <div class="tab-header">
              <el-radio-group v-model="favoriteType" size="small">
                <el-radio-button value="all">全部</el-radio-button>
                <el-radio-button value="plant">药材</el-radio-button>
                <el-radio-button value="knowledge">知识</el-radio-button>
                <el-radio-button value="inheritor">传承人</el-radio-button>
                <el-radio-button value="resource">学习资源</el-radio-button>
                <el-radio-button value="qa">问答</el-radio-button>
              </el-radio-group>
            </div>
            <div class="favorites-grid">
              <div v-for="item in paginatedFavorites" :key="item.id" class="favorite-card" @click="goToDetail(item)">
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
            <el-empty v-if="!filteredFavorites.length" description="暂无收藏" />
            <Pagination v-else-if="filteredFavorites.length > favPageSize" :page="favPage" :size="favPageSize" :total="filteredFavorites.length" @update:page="favPage = $event" @update:size="favPageSize = $event" />
          </el-tab-pane>

          <!-- Quiz Records Tab -->
          <el-tab-pane label="答题记录" name="quiz">
            <div class="record-list">
              <div v-for="record in paginatedRecords" :key="record.id + record.type" class="record-item">
                <div class="record-score" :class="getScoreClass(record.score)">{{ record.score }}分</div>
                <div class="record-info">
                  <p class="record-title">{{ record.type === 'quiz' ? '趣味答题' : '植物识别 · ' + getDifficultyName(record.difficulty) }}</p>
                  <p class="record-time">{{ formatTime(record.createTime) }}</p>
                </div>
                <span class="record-stats">正确 {{ record.correctCount }}/{{ record.totalCount }}</span>
              </div>
            </div>
            <el-empty v-if="!allRecords.length" description="暂无答题记录" />
            <Pagination v-else-if="allRecords.length > quizPageSize" :page="quizPage" :size="quizPageSize" :total="allRecords.length" @update:page="quizPage = $event" @update:size="quizPageSize = $event" />
          </el-tab-pane>

          <!-- Browse History Tab -->
          <el-tab-pane label="浏览历史" name="history">
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
          </el-tab-pane>

          <!-- Password Change Tab -->
          <el-tab-pane label="修改密码" name="settings">
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
                    <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">确认修改</el-button>
                    <el-button @click="resetPasswordForm">重置</el-button>
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
                  <el-button type="danger" :loading="logoutLoading" @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-button>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, watch, ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Lock, SwitchButton, User, EditPen, Star, View, Clock, ArrowRight, TrendCharts, Trophy, Cherry } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { extractData } from '@/utils'
import Pagination from '@/components/business/display/Pagination.vue'
import CaptchaInput from '@/components/business/interact/CaptchaInput.vue'
import { usePersonalCenter, actions, typeIconMap, typeTagMap, typeNameMap } from '@/composables/usePersonalCenter'

const updateUserState = inject('updateUserState')
const router = useRouter()

const {
  isLoggedIn, userName, isAdmin, pageLoading, activeTab, favoriteType,
  favorites, quizRecords, gameRecords,
  favPage, favPageSize, quizPage, quizPageSize,
  passwordFormRef, passwordCaptchaRef, passwordLoading, logoutLoading, passwordForm, passwordRules,
  filteredFavorites, paginatedFavorites, allRecords, paginatedRecords,
  goToDetail, handleChangePassword, resetPasswordForm, handleLogout,
  formatTime, getDifficultyName, getScoreClass
} = usePersonalCenter(request, updateUserState)

watch(favoriteType, () => { favPage.value = 1 })

const getTypeIcon = (type) => typeIconMap[type] || typeIconMap.knowledge
const getTypeTag = (type) => typeTagMap[type] || 'info'
const getTypeName = (type) => typeNameMap[type] || '其他'

// ========== Browse History ==========
const browseHistory = ref([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historyPageSize = ref(10)

const paginatedHistory = computed(() => {
  const start = (historyPage.value - 1) * historyPageSize.value
  return browseHistory.value.slice(start, start + historyPageSize.value)
})

const historyTypeIcons = {
  plant: Cherry,
  knowledge: Document,
  inheritor: User,
  resource: Star,
  qa: EditPen
}

const getHistoryIcon = (type) => historyTypeIcons[type] || typeIconMap.knowledge

const goToHistoryItem = (item) => {
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

const loadBrowseHistory = async () => {
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

// ========== Study Stats ==========
const statsLoading = ref(false)
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

const computeStudyStats = () => {
  const quizTotal = quizRecords.value.length
  const gameTotal = gameRecords.value.length
  const totalAttempts = quizTotal + gameTotal

  let totalScore = 0
  let scoreCount = 0
  quizRecords.value.forEach(r => {
    if (r.score !== undefined && r.score !== null) {
      totalScore += Number(r.score)
      scoreCount++
    }
  })
  gameRecords.value.forEach(r => {
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
    totalFavorites: favorites.value.length,
    totalBrowseCount: browseHistory.value.length
  }
}

// ========== Score Trend Chart ==========
const scoreChartRef = ref(null)
let scoreChartInstance = null
let scoreResizeObserver = null
const hasScoreData = ref(false)

function buildScoreTrendData() {
  // Collect all quiz scores with timestamps
  const quizPoints = quizRecords.value.map(r => ({
    date: (r.createdAt || r.createTime) ? new Date(r.createdAt || r.createTime).toISOString().slice(0, 10) : '',
    score: Number(r.score || 0),
    type: 'quiz'
  }))

  // Collect all game scores with timestamps
  const gamePoints = gameRecords.value.map(r => ({
    date: (r.createdAt || r.createTime) ? new Date(r.createdAt || r.createTime).toISOString().slice(0, 10) : '',
    score: Number(r.score || 0),
    type: 'game'
  }))

  // Sort by date and aggregate by date
  const allPoints = [...quizPoints, ...gamePoints].filter(p => p.date).sort((a, b) => a.date.localeCompare(b.date))

  if (allPoints.length === 0) {
    hasScoreData.value = false
    return { dates: [], quizScores: [], gameScores: [] }
  }

  hasScoreData.value = true

  // Aggregate: average score per date per type
  const dateMap = new Map()
  allPoints.forEach(p => {
    if (!dateMap.has(p.date)) {
      dateMap.set(p.date, { quizScores: [], gameScores: [] })
    }
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

function initScoreChart() {
  if (!scoreChartRef.value) return

  if (scoreChartInstance) {
    scoreChartInstance.dispose()
  }

  scoreChartInstance = echarts.init(scoreChartRef.value)

  const { dates, quizScores, gameScores } = buildScoreTrendData()

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
          if (p.value !== null) {
            result += `${p.marker} ${p.seriesName}: ${p.value}分<br/>`
          }
        })
        return result
      }
    },
    legend: {
      data: ['趣味答题', '植物识别'],
      bottom: 0,
      textStyle: { color: 'var(--text-secondary, #555)', fontSize: 12 }
    },
    grid: {
      left: '3%',
      right: '4%',
      top: '10%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLabel: {
        color: 'var(--text-muted, #888)',
        fontSize: 10,
        rotate: 30
      },
      axisLine: { lineStyle: { color: 'var(--border-light, #eee)' } }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      min: 0,
      max: 100,
      axisLabel: { color: 'var(--text-muted, #888)' },
      splitLine: { lineStyle: { color: 'var(--border-light, #eee)', type: 'dashed' } }
    },
    series: [
      {
        name: '趣味答题',
        type: 'line',
        data: quizScores,
        smooth: true,
        connectNulls: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#3498db', width: 2 },
        itemStyle: { color: '#3498db' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(52, 152, 219, 0.3)' },
            { offset: 1, color: 'rgba(52, 152, 219, 0.02)' }
          ])
        }
      },
      {
        name: '植物识别',
        type: 'line',
        data: gameScores,
        smooth: true,
        connectNulls: true,
        symbol: 'diamond',
        symbolSize: 6,
        lineStyle: { color: '#28B463', width: 2 },
        itemStyle: { color: '#28B463' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(40, 180, 99, 0.3)' },
            { offset: 1, color: 'rgba(40, 180, 99, 0.02)' }
          ])
        }
      }
    ]
  })

  // Resize handling
  if (scoreResizeObserver) scoreResizeObserver.disconnect()
  scoreResizeObserver = new ResizeObserver(() => {
    if (scoreChartInstance && !scoreChartInstance.isDisposed()) {
      scoreChartInstance.resize()
    }
  })
  scoreResizeObserver.observe(scoreChartRef.value)
}

// Watch for quiz data changes to update chart and stats
watch(
  () => [quizRecords.value, gameRecords.value, browseHistory.value, favorites.value],
  () => {
    computeStudyStats()
    nextTick(() => {
      initScoreChart()
    })
  },
  { deep: true }
)

// Watch tab changes to load data and init chart
watch(activeTab, async (tab) => {
  if (tab === 'stats') {
    statsLoading.value = true
    try {
      if (isLoggedIn.value) {
        const [quizRes, gameRes] = await Promise.all([
          request.get('/quiz/records').catch(() => ({})),
          request.get('/plant-game/records').catch(() => ({}))
        ])
        quizRecords.value = extractData(quizRes)
        gameRecords.value = extractData(gameRes)
      }
      computeStudyStats()
      await nextTick()
      initScoreChart()
    } finally {
      statsLoading.value = false
    }
  }
  if (tab === 'history') {
    loadBrowseHistory()
  }
})

onMounted(() => {
  loadBrowseHistory()
})

onUnmounted(() => {
  if (scoreResizeObserver) {
    scoreResizeObserver.disconnect()
    scoreResizeObserver = null
  }
  if (scoreChartInstance && !scoreChartInstance.isDisposed()) {
    scoreChartInstance.dispose()
    scoreChartInstance = null
  }
})
</script>

<style scoped>
.personal-container {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: var(--space-xl);
}

.not-logged-in {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-state {
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: var(--text-light);
  margin-bottom: var(--space-lg);
}

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

.content-section {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

.tab-header {
  margin-bottom: var(--space-xl);
}

/* Study Stats Dashboard */
.stats-dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: var(--space-md);
}

.stats-card-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  transition: transform var(--transition-fast);
}

.stats-card-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.stats-card-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: 20px;
  flex-shrink: 0;
}

.stats-card-value {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}

.stats-card-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

/* Chart Section */
.chart-section {
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  padding: var(--space-lg);
}

.chart-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin: 0 0 var(--space-lg) 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--dong-indigo);
}

.score-chart {
  width: 100%;
  height: 350px;
}

/* Favorites */
.favorites-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.favorite-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.fav-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.fav-content {
  flex: 1;
}

.fav-content h4 {
  margin: 0 0 var(--space-xs) 0;
  font-size: var(--font-size-sm);
}

.fav-content p {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

/* Records */
.record-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.record-item {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
}

.record-score {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  font-size: 18px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.record-score.score-high {
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
}

.record-score.score-medium {
  background: linear-gradient(135deg, var(--dong-gold-light), var(--dong-gold));
}

.record-score.score-low {
  background: linear-gradient(135deg, var(--color-info), #2980b9);
}

.record-info {
  flex: 1;
}

.record-title {
  margin: 0 0 var(--space-xs) 0;
  font-weight: var(--font-weight-medium);
}

.record-time {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.record-stats {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* Browse History */
.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.history-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.history-item:hover {
  background: var(--bg-rice-dark);
}

.history-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.history-content {
  flex: 1;
}

.history-content h4 {
  margin: 0 0 var(--space-xs) 0;
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.history-time {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.history-arrow {
  color: var(--text-light);
  font-size: var(--font-size-base);
}

/* Settings */
.settings-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.settings-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--dong-indigo);
}

.settings-form {
  max-width: 400px;
}

.logout-section {
  text-align: center;
}

.logout-tip {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-lg);
}

/* Responsive */
@media (max-width: 1024px) {
  .personal-container {
    grid-template-columns: 1fr;
  }

  .stats-cards {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}

@media (max-width: 768px) {
  .personal-container {
    grid-template-columns: 1fr;
  }

  .stats-cards {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-sm);
  }

  .stats-card-item {
    padding: var(--space-md);
  }

  .stats-card-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .stats-card-value {
    font-size: var(--font-size-lg);
  }

  .score-chart {
    height: 280px;
  }

  .settings-form {
    max-width: 100%;
  }

  .record-item {
    flex-wrap: wrap;
    gap: var(--space-md);
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .score-chart {
    height: 240px;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .favorite-card {
    flex-wrap: wrap;
  }

  .history-item {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }
}
</style>
