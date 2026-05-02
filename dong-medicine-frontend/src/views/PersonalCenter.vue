<template>
  <div class="personal-center module-page">
    <div class="module-header">
      <h1>个人中心</h1>
      <p class="subtitle">
我的信息 · 收藏管理 · 学习记录
</p>
    </div>

    <div v-if="!isLoggedIn" class="not-logged-in">
      <div class="empty-state">
        <el-icon class="empty-icon">
<User />
</el-icon>
        <h2>您还未登录</h2>
        <p>登录后可查看个人收藏、答题记录、评论历史等内容</p>
        <el-button type="primary" size="large" @click="$router.push('/')">
立即登录
</el-button>
      </div>
    </div>

    <div v-else class="personal-container">
      <ProfileSection
        :user-name="userName"
        :is-admin="isAdmin"
        :study-stats="studyStats"
        :actions="actions"
        @tab-change="activeTab = $event"
      />

      <!-- Content Section -->
      <div class="content-section">
        <el-tabs v-model="activeTab" class="content-tabs">
          <!-- Study Stats Tab -->
          <el-tab-pane label="学习统计" name="stats">
            <StatsDashboard
              :quiz-records="quizRecords"
              :game-records="gameRecords"
              :favorites="favorites"
              :browse-history="browseHistory"
            />
          </el-tab-pane>

          <!-- Favorites Tab -->
          <el-tab-pane label="我的收藏" name="favorites">
            <FavoritesPanel :favorites="favorites" @go-detail="goToDetail" />
          </el-tab-pane>

          <!-- Quiz Records Tab -->
          <el-tab-pane label="答题记录" name="quiz">
            <QuizHistoryPanel :quiz-records="quizRecords" :game-records="gameRecords" />
          </el-tab-pane>

          <!-- Browse History Tab -->
          <el-tab-pane label="浏览历史" name="history">
            <BrowseHistoryPanel />
          </el-tab-pane>

          <!-- Password Change Tab -->
          <el-tab-pane label="修改密码" name="settings">
            <SettingsPanel
              ref="settingsRef"
              :user-name="userName"
              :is-admin="isAdmin"
              :password-form="passwordForm"
              :password-rules="passwordRules"
              :password-loading="passwordLoading"
              :logout-loading="logoutLoading"
              @change-password="handleChangePassword"
              @reset-password="resetPasswordForm"
              @logout="handleLogout"
              @update:captcha-key="passwordForm.captchaKey = $event"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, watch, onMounted, onUnmounted, nextTick, ref } from 'vue'
import request from '@/utils/request'
import { extractData } from '@/utils'
import { usePersonalCenter, actions } from '@/composables/usePersonalCenter'
import { useStudyStats } from '@/composables/useStudyStats'
import { useBrowseHistory } from '@/composables/useBrowseHistory'
import ProfileSection from './personal-center/ProfileSection.vue'
import StatsDashboard from './personal-center/StatsDashboard.vue'
import FavoritesPanel from './personal-center/FavoritesPanel.vue'
import QuizHistoryPanel from './personal-center/QuizHistoryPanel.vue'
import BrowseHistoryPanel from './personal-center/BrowseHistoryPanel.vue'
import SettingsPanel from './personal-center/SettingsPanel.vue'

const updateUserState = inject('updateUserState')

const {
  isLoggedIn, userName, isAdmin, activeTab,
  favorites, quizRecords, gameRecords,
  passwordFormRef, passwordCaptchaRef, passwordLoading, logoutLoading, passwordForm, passwordRules,
  goToDetail, handleChangePassword, resetPasswordForm, handleLogout
} = usePersonalCenter(request, updateUserState)

// ========== Browse History (composable) ==========
const { browseHistory } = useBrowseHistory()

// ========== Study Stats (composable) ==========
const { studyStats, computeStudyStats, initScoreChart, disposeChart } = useStudyStats()

// Watch for quiz data changes to update stats
watch(
  () => [quizRecords.value, gameRecords.value, browseHistory.value, favorites.value],
  () => {
    computeStudyStats(quizRecords.value, gameRecords.value, favorites.value, browseHistory.value)
    nextTick(() => {
      initScoreChart(quizRecords.value, gameRecords.value)
    })
  },
  { deep: true }
)

// Watch tab changes to load data
watch(activeTab, async (tab) => {
  if (tab === 'stats') {
    try {
      if (isLoggedIn.value) {
        const [quizRes, gameRes] = await Promise.all([
          request.get('/quiz/records').catch(() => ({})),
          request.get('/plant-game/records').catch(() => ({}))
        ])
        quizRecords.value = extractData(quizRes)
        gameRecords.value = extractData(gameRes)
      }
      computeStudyStats(quizRecords.value, gameRecords.value, favorites.value, browseHistory.value)
      await nextTick()
      initScoreChart(quizRecords.value, gameRecords.value)
    } catch {
      // data fetch failed silently
    }
  }
})

const settingsRef = ref(null)

// Wire SettingsPanel's exposed form refs to the composable
onMounted(() => {
  nextTick(() => {
    if (settingsRef.value) {
      passwordFormRef.value = settingsRef.value.formRef
      passwordCaptchaRef.value = settingsRef.value.captchaRef
    }
  })
})

onUnmounted(() => {
  disposeChart()
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

.content-section {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 1024px) {
  .personal-container { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .personal-container { grid-template-columns: 1fr; gap: var(--space-md); }
  .content-section { padding: var(--space-md); }
}

@media (max-width: 480px) {
  .personal-container { gap: var(--space-sm); }
  .content-section { padding: var(--space-sm); }
}
</style>
