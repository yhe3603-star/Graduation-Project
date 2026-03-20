<template>
  <div v-loading="pageLoading" class="personal-center module-page">
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
            <div class="stat-block">
              <span class="stat-num">{{ favorites.length }}</span><span class="stat-text">收藏</span>
            </div>
            <div class="stat-block">
              <span class="stat-num">{{ quizRecords.length + gameRecords.length }}</span><span class="stat-text">答题</span>
            </div>
            <div class="stat-block">
              <span class="stat-num">{{ comments.length }}</span><span class="stat-text">评论</span>
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

      <div class="content-section">
        <el-tabs v-model="activeTab" class="content-tabs">
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
            <Pagination v-else :page="favPage" :size="favPageSize" :total="filteredFavorites.length" @update:page="favPage = $event" @update:size="favPageSize = $event" />
          </el-tab-pane>

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
            <Pagination v-else :page="quizPage" :size="quizPageSize" :total="allRecords.length" @update:page="quizPage = $event" @update:size="quizPageSize = $event" />
          </el-tab-pane>

          <el-tab-pane label="我的评论" name="comments">
            <div class="comment-list">
              <div v-for="c in paginatedComments" :key="c.id" class="comment-item">
                <p class="comment-content">{{ c.content }}</p>
                <div class="comment-meta">
                  <span class="comment-target">{{ c.targetType }}</span>
                  <span class="comment-time">{{ formatTime(c.createTime) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-if="!comments.length" description="暂无评论" />
            <Pagination v-else :page="commentPage" :size="commentPageSize" :total="comments.length" @update:page="commentPage = $event" @update:size="commentPageSize = $event" />
          </el-tab-pane>

          <el-tab-pane label="账号设置" name="settings">
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
import { inject, watch } from 'vue'
import { Lock, SwitchButton, User } from '@element-plus/icons-vue'
import Pagination from '@/components/business/display/Pagination.vue'
import { usePersonalCenter, actions, typeIconMap, typeTagMap, typeNameMap } from '@/composables/usePersonalCenter'

const request = inject('request')
const updateUserState = inject('updateUserState')

const {
  isLoggedIn, userName, isAdmin, pageLoading, activeTab, favoriteType,
  favorites, quizRecords, gameRecords, comments,
  favPage, favPageSize, quizPage, quizPageSize, commentPage, commentPageSize,
  passwordFormRef, passwordLoading, logoutLoading, passwordForm, passwordRules,
  filteredFavorites, paginatedFavorites, allRecords, paginatedRecords, paginatedComments,
  goToDetail, handleChangePassword, resetPasswordForm, handleLogout,
  formatTime, getDifficultyName, getScoreClass
} = usePersonalCenter(request, updateUserState)

watch(favoriteType, () => { favPage.value = 1 })

const getTypeIcon = (type) => typeIconMap[type] || typeIconMap.knowledge
const getTypeTag = (type) => typeTagMap[type] || 'info'
const getTypeName = (type) => typeNameMap[type] || '其他'
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
  color: var(--text-inverse);
}

.record-score.excellent {
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
}

.record-score.good {
  background: linear-gradient(135deg, var(--dong-gold-light), var(--dong-gold));
}

.record-score.pass {
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

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.comment-item {
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
}

.comment-content {
  margin: 0 0 var(--space-sm) 0;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

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

@media (max-width: 768px) {
  .personal-container {
    grid-template-columns: 1fr;
  }
}
</style>
