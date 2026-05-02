<template>
  <div class="home">
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-pattern" />
        <div class="hero-glow" />
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <el-icon><Medal /></el-icon>
          <span>广西壮族自治区级非物质文化遗产</span>
        </div>
        <h1 class="hero-title">
          <span class="title-main">非遗视角下</span>
          <span class="title-highlight">侗乡医药</span>
          <span class="title-main">数字展示平台</span>
        </h1>
        <p class="hero-subtitle">
保护 · 传承 · 活态传播
</p>
        <p class="hero-desc">
三江侗族自治县传统医药文化数字化保护与传播平台
</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="action-btn primary" @click="$router.push('/knowledge')">
            <el-icon><Compass /></el-icon>
            <span>立即探索</span>
          </el-button>
          <el-button size="large" class="action-btn secondary" @click="$router.push('/about')">
            <el-icon><InfoFilled /></el-icon>
            <span>了解非遗</span>
          </el-button>
        </div>
      </div>
      <div class="hero-stats">
        <div v-for="(stat, i) in heroStats" :key="i" class="stat-card">
          <div class="stat-icon">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="weekly-featured-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <el-icon><Medal /></el-icon>每周精选
          </h2>
          <p class="section-desc">
为您精选本周最值得关注的侗医药知识
</p>
        </div>
      </div>
      <div v-if="weeklyFeatured" class="featured-card" @click="goToDetail(weeklyFeatured)">
        <div class="featured-card-img">
          <el-image
            v-if="weeklyFeatured.image"
            :src="weeklyFeatured.image"
            fit="cover"
            class="featured-img"
          >
            <template #error>
              <div class="featured-img-placeholder">
                <el-icon :size="48">
<Picture />
</el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="featured-img-placeholder">
            <el-icon :size="48">
<Picture />
</el-icon>
          </div>
        </div>
        <div class="featured-card-content">
          <el-tag
            :type="weeklyFeatured.tagType || 'primary'"
            size="small"
            effect="dark"
            class="featured-tag"
          >
            {{ weeklyFeatured.typeLabel || '精选' }}
          </el-tag>
          <h3>{{ weeklyFeatured.title || weeklyFeatured.nameCn || weeklyFeatured.name }}</h3>
          <p>{{ weeklyFeatured.description || weeklyFeatured.efficacy || weeklyFeatured.bio || '' }}</p>
          <div class="featured-stats">
            <span v-if="weeklyFeatured.viewCount !== undefined">
              <el-icon><View /></el-icon>{{ weeklyFeatured.viewCount }} 次浏览
            </span>
            <span v-if="weeklyFeatured.favoriteCount !== undefined">
              <el-icon><Star /></el-icon>{{ weeklyFeatured.favoriteCount }} 人收藏
            </span>
          </div>
        </div>
      </div>
      <div v-else class="section-skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="featured-skeleton">
              <el-skeleton-item variant="image" style="width: 200px; height: 160px;" />
              <div style="flex: 1; padding: 16px;">
                <el-skeleton-item variant="text" style="width: 30%;" />
                <el-skeleton-item variant="text" />
                <el-skeleton-item variant="text" style="width: 80%;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <section class="hot-search-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <el-icon><TrendCharts /></el-icon>热门搜索
          </h2>
          <p class="section-desc">
点击感兴趣的关键词，了解侗医药知识
</p>
        </div>
      </div>
      <div v-if="hotCloudItems.length > 0" class="word-cloud">
        <span
          v-for="item in hotCloudItems"
          :key="item.name"
          class="cloud-tag"
          :style="{
            fontSize: item.size + 'px',
            opacity: item.opacity,
            color: item.color
          }"
          @click="searchKeyword(item.name)"
        >
          {{ item.name }}
        </span>
      </div>
      <div v-else class="section-skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="cloud-skeleton">
              <el-skeleton-item
                v-for="i in 12"
                :key="i"
                variant="text"
                style="width: 80px; margin-right: 12px; display: inline-block;"
              />
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <section class="latest-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <el-icon><Clock /></el-icon>最新更新
          </h2>
          <p class="section-desc">
最近更新的侗医药内容
</p>
        </div>
      </div>
      <div v-if="latestItems.length > 0" class="latest-grid">
        <div
          v-for="item in latestItems"
          :key="item.id + (item.type || '')"
          class="latest-card"
          @click="goToDetail(item)"
        >
          <div class="latest-card-img">
            <el-image
              v-if="item.image"
              :src="item.image"
              fit="cover"
              class="latest-img"
            >
              <template #error>
                <div class="latest-img-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="latest-img-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </div>
          <div class="latest-card-body">
            <el-tag
              :type="item.tagType || 'info'"
              size="small"
              class="latest-tag"
            >
              {{ item.typeLabel || '其他' }}
            </el-tag>
            <h4>{{ item.title || item.nameCn || item.name }}</h4>
            <p class="latest-desc">
{{ truncateText(item.description || item.efficacy || item.bio || '', 50) }}
</p>
            <span class="latest-time">
              <el-icon><Clock /></el-icon>{{ formatTime(item.updatedAt || item.createdAt) }}
            </span>
          </div>
        </div>
      </div>
      <div v-else class="section-skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="latest-skeleton-grid">
              <div v-for="i in 6" :key="i" style="display: flex; flex-direction: column; gap: 8px;">
                <el-skeleton-item variant="image" style="width: 100%; height: 140px;" />
                <el-skeleton-item variant="text" />
                <el-skeleton-item variant="text" style="width: 60%;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <section class="features-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
核心功能
</h2>
          <p class="section-desc">
全方位展示侗乡医药文化精髓
</p>
        </div>
      </div>
      <div class="features-grid">
        <div v-for="(mod, i) in coreModules" :key="i" class="feature-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(mod.path)">
          <div class="feature-header">
            <div class="feature-icon" :style="{ background: mod.bgColor }">
              <el-icon :size="36">
<component :is="mod.icon" />
</el-icon>
            </div>
            <div class="feature-badge">
{{ mod.count }}
</div>
          </div>
          <h3>{{ mod.title }}</h3>
          <p>{{ mod.desc }}</p>
          <div class="feature-footer">
            <span class="feature-link">查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <section class="extend-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
拓展功能
</h2>
          <p class="section-desc">
更多精彩内容等你探索
</p>
        </div>
      </div>
      <div class="extend-grid">
        <div v-for="(mod, i) in extendModules" :key="i" class="extend-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(mod.path)">
          <div class="extend-icon">
            <el-icon :size="40">
<component :is="mod.icon" />
</el-icon>
          </div>
          <div class="extend-content">
            <h4>{{ mod.title }}</h4>
            <p>{{ mod.desc }}</p>
          </div>
          <div class="extend-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="cta-content">
        <h2>探索侗乡医药文化</h2>
        <p>了解更多关于侗族传统医药的知识，感受非遗文化的魅力</p>
        <div class="cta-actions">
          <el-button type="primary" size="large" @click="$router.push('/interact')">
            <el-icon><Aim /></el-icon>
            开始互动体验
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { Aim, ArrowRight, ChatDotRound, Clock, Compass, DataLine, Document, Folder, InfoFilled, Medal, Picture, Star, TrendCharts, User, View } from '@element-plus/icons-vue'
import { coreModules, extendModules, createHeroStats } from '@/config/homeConfig'
import { extractPageData, logFetchError, getFirstImage, formatTime } from "@/utils";

const router = useRouter()

const stats = ref({ plants: 21, formulas: 12, inheritors: 10, therapies: 6 })
const latestItems = ref([])
const hotCloudItems = ref([])
const weeklyFeatured = ref(null)
const pageLoading = ref(true)

const heroStats = computed(() => createHeroStats(stats.value))

const CLOUD_COLORS = [
  '#1A5276', '#28B463', '#E67E22', '#8E44AD', '#2C3E50',
  '#16A085', '#D35400', '#2980B9', '#27AE60', '#C0392B',
  '#7D3C98', '#117A65'
]

const TAG_TYPE_MAP = {
  plant: { tagType: 'success', typeLabel: '植物' },
  knowledge: { tagType: 'primary', typeLabel: '知识' },
  inheritor: { tagType: 'warning', typeLabel: '传承人' },
  qa: { tagType: 'info', typeLabel: '问答' },
  resource: { tagType: 'danger', typeLabel: '资源' }
}

const DETAIL_ROUTES = {
  plant: '/plants',
  knowledge: '/knowledge',
  inheritor: '/inheritors',
  qa: '/qa',
  resource: '/resources'
}

const truncateText = (text, maxLen = 50) => {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

const goToDetail = (item) => {
  if (!item) return
  const path = DETAIL_ROUTES[item.type] || '/'
  router.push(path)
}

const searchKeyword = (keyword) => {
  router.push({ path: '/search', query: { q: keyword } })
}

const buildLatestItems = (plants, knowledge, inheritors, qas) => {
  const typeMeta = (type) => TAG_TYPE_MAP[type] || { tagType: 'info', typeLabel: '其他' }

  const allItems = [
    ...plants.map(p => ({ ...p, type: 'plant', image: getFirstImage(p.images), ...typeMeta('plant') })),
    ...knowledge.map(k => ({ ...k, type: 'knowledge', image: getFirstImage(k.images || k.covers), ...typeMeta('knowledge') })),
    ...inheritors.map(i => ({ ...i, type: 'inheritor', image: null, ...typeMeta('inheritor') })),
    ...qas.map(q => ({ ...q, type: 'qa', image: null, ...typeMeta('qa') })),
  ]

  return allItems
    .sort((a, b) => new Date(b.updatedAt || b.createdAt || 0) - new Date(a.updatedAt || a.createdAt || 0))
    .slice(0, 6)
}

const buildCloudItems = (plants, knowledge) => {
  const cloudMap = new Map()

  const addToCloud = (name, viewCount = 0) => {
    if (!name) return
    if (cloudMap.has(name)) {
      cloudMap.set(name, cloudMap.get(name) + (viewCount || 1))
    } else {
      cloudMap.set(name, viewCount || 1)
    }
  }

  plants.forEach(p => {
    if (p.nameCn) addToCloud(p.nameCn, p.viewCount || 0)
    if (p.category) addToCloud(p.category, Math.max((p.viewCount || 0) - 5, 1))
  })

  knowledge.forEach(k => {
    if (k.title) addToCloud(k.title, k.viewCount || 0)
  })

  const entries = [...cloudMap.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 20)

  const maxCount = entries.length > 0 ? entries[0][1] : 1
  const minCount = entries.length > 0 ? entries[entries.length - 1][1] : 1

  return entries.map(([name, count], index) => {
    const ratio = maxCount === minCount ? 0.5 : (count - minCount) / (maxCount - minCount)
    const size = Math.round(12 + ratio * 18)
    const opacity = Math.round((0.5 + ratio * 0.5) * 10) / 10
    const color = CLOUD_COLORS[index % CLOUD_COLORS.length]
    return { name, size, opacity, color }
  }).sort(() => Math.random() - 0.5)
}

onMounted(async () => {
  pageLoading.value = true
  try {
    const [pRes, iRes, kRes, qRes] = await Promise.all([
      request.get('/plants/list', { params: { page: 1, size: 20 }, skipAuthRefresh: true }).catch(() => ({})),
      request.get('/inheritors/list', { params: { page: 1, size: 5 }, skipAuthRefresh: true }).catch(() => ({})),
      request.get('/knowledge/list', { params: { page: 1, size: 10 }, skipAuthRefresh: true }).catch(() => ({})),
      request.get('/qa/list', { params: { page: 1, size: 10 }, skipAuthRefresh: true }).catch(() => ({}))
    ])

    const plantsData = extractPageData(pRes)
    const inheritorsData = extractPageData(iRes)
    const knowledgeData = extractPageData(kRes)
    const qaData = extractPageData(qRes)

    stats.value.plants = plantsData.total || stats.value.plants
    stats.value.inheritors = inheritorsData.total || stats.value.inheritors

    latestItems.value = buildLatestItems(
      plantsData.records.slice(0, 20),
      knowledgeData.records.slice(0, 10),
      inheritorsData.records.slice(0, 5),
      qaData.records.slice(0, 10)
    )

    hotCloudItems.value = buildCloudItems(
      plantsData.records.slice(0, 20),
      knowledgeData.records.slice(0, 10)
    )

    const allForFeatured = [
      ...plantsData.records.map(p => ({ ...p, type: 'plant', image: getFirstImage(p.images), tagType: 'success', typeLabel: '植物' })),
      ...knowledgeData.records.map(k => ({ ...k, type: 'knowledge', image: getFirstImage(k.images || k.covers), tagType: 'primary', typeLabel: '知识' })),
      ...inheritorsData.records.map(i => ({ ...i, type: 'inheritor', image: null, tagType: 'warning', typeLabel: '传承人' }))
    ].filter(item => item.title || item.nameCn || item.name)

    if (allForFeatured.length > 0) {
      const popular = allForFeatured.sort((a, b) => ((b.viewCount || 0) + (b.favoriteCount || 0)) - ((a.viewCount || 0) + (a.favoriteCount || 0)))
      const randomIndex = Math.floor(Math.random() * Math.min(5, popular.length))
      weeklyFeatured.value = popular[randomIndex]
    }
  } catch (e) {
    logFetchError('首页数据', e)
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped>
.home {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--space-xl) var(--space-4xl);
}

.hero-section {
  position: relative;
  padding: 64px 48px;
  border-radius: var(--radius-2xl);
  margin-bottom: var(--space-3xl);
  overflow: hidden;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(40, 180, 99, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(201, 162, 39, 0.1) 0%, transparent 40%),
    radial-gradient(ellipse at 50% 50%, rgba(26, 82, 118, 0.3) 0%, transparent 70%),
    linear-gradient(160deg, #0a2e45 0%, var(--dong-indigo) 30%, #0d3d5c 60%, var(--dong-indigo-dark) 100%);
  color: var(--text-inverse);
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 20px 60px rgba(26, 82, 118, 0.3);
  border: 1px solid rgba(201, 162, 39, 0.15);
}

.hero-bg {
  position: absolute;
  inset: 0;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image:
    url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' stroke='%23ffffff' stroke-opacity='0.04' stroke-width='0.5'%3E%3Cpath d='M30 0L35 15L50 20L35 25L30 40L25 25L10 20L25 15Z'/%3E%3Cpath d='M30 20L32 28L40 30L32 32L30 40L28 32L20 30L28 28Z' fill='%23ffffff' fill-opacity='0.02'/%3E%3C/g%3E%3C/svg%3E"),
    url("data:image/svg+xml,%3Csvg width='120' height='120' viewBox='0 0 120 120' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' stroke='%2340b480' stroke-opacity='0.03' stroke-width='0.5'%3E%3Cpath d='M60 0L65 30L90 40L65 50L60 80L55 50L30 40L55 30Z'/%3E%3C/g%3E%3C/svg%3E");
  background-size: 60px 60px, 120px 120px;
  animation: patternMove 30s linear infinite;
  opacity: 0.8;
}

@keyframes patternMove {
  0% { background-position: 0 0, 0 0; }
  100% { background-position: 60px 60px, 120px 120px; }
}

.hero-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(40, 180, 99, 0.15) 0%, transparent 70%);
  top: -200px;
  right: -200px;
  animation: glowPulse 4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 800px;
  width: 100%;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-lg);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-xl);
  animation: fadeInDown 0.6s ease-out;
}

.hero-badge .el-icon {
  color: var(--dong-gold-light);
}

.hero-title {
  font-family: var(--font-display);
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--space-md) 0;
  line-height: var(--line-height-tight);
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.title-main {
  display: inline-block;
}

.title-highlight {
  display: inline-block;
  color: var(--dong-jade);
  margin: 0 var(--space-sm);
  position: relative;
}

.title-highlight::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 0;
  right: 0;
  height: 8px;
  background: rgba(40, 180, 99, 0.3);
  border-radius: var(--radius-xs);
  z-index: -1;
}

.hero-subtitle {
  font-size: var(--font-size-xl);
  opacity: 0.9;
  margin: 0 0 var(--space-sm) 0;
  animation: fadeInUp 0.6s ease-out 0.2s both;
  text-align: center;
}

.hero-desc {
  font-size: var(--font-size-base);
  opacity: 0.7;
  margin: 0 0 var(--space-2xl) 0;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-lg);
  animation: fadeInUp 0.6s ease-out 0.4s both;
}

.action-btn {
  padding: var(--space-lg) var(--space-2xl);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
  border: none;
}

.action-btn.primary:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-glow);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: var(--text-inverse);
}

.action-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-3px);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
  margin-top: var(--space-3xl);
  padding-top: var(--space-2xl);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  animation: fadeInUp 0.6s ease-out 0.5s both;
  max-width: 900px;
  width: 100%;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  padding: var(--space-xl);
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all var(--transition-normal);
  backdrop-filter: blur(10px);
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.stat-icon {
  width: 52px;
  height: 52px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-2xl);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  display: block;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-inverse);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.9);
  font-weight: var(--font-weight-medium);
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.quick-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
  margin-bottom: var(--space-3xl);
}

.nav-card {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-xl);
  background: var(--text-inverse);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.nav-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, transparent 60%, rgba(201, 162, 39, 0.03) 100%);
  pointer-events: none;
}

.nav-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
  border-color: rgba(201, 162, 39, 0.3);
}

.nav-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.nav-content {
  flex: 1;
}

.nav-content h3 {
  margin: 0 0 var(--space-xs) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.nav-content p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.nav-arrow {
  width: 36px;
  height: 36px;
  background: var(--bg-rice);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  transition: all var(--transition-normal);
}

.nav-card:hover .nav-arrow {
  background: var(--dong-indigo);
  color: var(--text-inverse);
  transform: translateX(4px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--space-2xl);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--dong-indigo);
  margin: 0 0 var(--space-sm) 0;
}

.section-desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

.view-all-btn {
  font-size: var(--font-size-sm);
  color: var(--text-inverse);
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-sm);
}

.view-all-btn:hover {
  background: linear-gradient(135deg, var(--dong-indigo-dark), var(--dong-indigo));
}

.features-section {
  margin-bottom: var(--space-3xl);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
}

.feature-card {
  background: var(--text-inverse);
  border-radius: var(--radius-xl);
  padding: var(--space-2xl);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--feature-color, var(--dong-indigo));
  transform: scaleX(0);
  transition: transform var(--transition-normal);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.feature-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-xl);
}

.feature-icon {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.feature-badge {
  padding: var(--space-xs) var(--space-md);
  background: linear-gradient(135deg, rgba(40, 180, 99, 0.1), rgba(26, 82, 118, 0.1));
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--dong-jade);
}

.feature-card h3 {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-md) 0;
}

.feature-card p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  margin: 0 0 var(--space-xl) 0;
}

.feature-footer {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--dong-indigo);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.feature-card:hover .feature-footer {
  gap: var(--space-md);
}

.inheritors-section {
  margin-bottom: var(--space-3xl);
}

.inheritors-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
}

.inheritor-card {
  background: var(--text-inverse);
  border-radius: var(--radius-xl);
  padding: var(--space-2xl) var(--space-xl);
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
  position: relative;
}

.inheritor-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.inheritor-avatar {
  width: 80px;
  height: 80px;
  margin: 0 auto var(--space-xl);
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  box-shadow: var(--shadow-md);
}

.inheritor-content h4 {
  margin: 0 0 var(--space-md) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.inheritor-level {
  display: inline-block;
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
  margin-bottom: var(--space-md);
}

.level-gold {
  background: linear-gradient(135deg, var(--dong-gold-light), var(--dong-gold));
}

.level-green {
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
}

.level-blue {
  background: linear-gradient(135deg, var(--color-info), #2980b9);
}

.inheritor-specialty {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
  line-height: var(--line-height-normal);
}

.inheritor-badge {
  position: absolute;
  top: var(--space-md);
  right: var(--space-md);
  color: var(--dong-gold-light);
  font-size: var(--font-size-xl);
}

.extend-section {
  margin-bottom: var(--space-3xl);
}

.extend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-xl);
}

.extend-card {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-2xl);
  background: var(--text-inverse);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

.extend-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.extend-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, rgba(26, 82, 118, 0.1), rgba(40, 180, 99, 0.1));
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dong-indigo);
  flex-shrink: 0;
}

.extend-content {
  flex: 1;
}

.extend-content h4 {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.extend-content p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.extend-arrow {
  width: 40px;
  height: 40px;
  background: var(--bg-rice);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  transition: all var(--transition-normal);
}

.extend-card:hover .extend-arrow {
  background: var(--dong-indigo);
  color: var(--text-inverse);
  transform: translateX(4px);
}

.cta-section {
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-2xl);
  padding: var(--space-4xl);
  text-align: center;
  color: var(--text-inverse);
  position: relative;
  overflow: hidden;
}

.cta-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none'%3E%3Cpath d='M30 0L33 15L45 20L33 25L30 40L27 25L15 20L27 15Z' fill='%23ffffff' fill-opacity='0.04'/%3E%3C/g%3E%3C/svg%3E"),
    url("data:image/svg+xml,%3Csvg width='120' height='120' viewBox='0 0 120 120' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' stroke='%2340b480' stroke-opacity='0.03' stroke-width='0.5'%3E%3Cpath d='M60 0L65 30L90 40L65 50L60 80L55 50L30 40L55 30Z'/%3E%3C/g%3E%3C/svg%3E");
  background-size: 60px 60px, 120px 120px;
}

.cta-content {
  position: relative;
  z-index: 1;
}

.cta-content h2 {
  font-family: var(--font-display);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--space-md) 0;
}

.cta-content p {
  font-size: var(--font-size-md);
  opacity: 0.8;
  margin: 0 0 var(--space-2xl) 0;
}

.cta-actions .el-button {
  padding: var(--space-lg) var(--space-2xl);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
  border: none;
}

.cta-actions .el-button:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-glow);
}

@media (max-width: 1200px) {
  .features-grid,
  .inheritors-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .quick-nav {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .extend-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home {
    padding: 0 var(--space-md) var(--space-2xl);
  }
  
  .hero-section {
    padding: var(--space-2xl) var(--space-lg);
  }
  
  .hero-badge {
    font-size: var(--font-size-xs);
    padding: var(--space-xs) var(--space-md);
  }
  
  .hero-title {
    font-size: var(--font-size-2xl);
  }
  
  .title-highlight {
    display: block;
    margin: var(--space-sm) 0;
  }
  
  .hero-subtitle {
    font-size: var(--font-size-md);
    letter-spacing: 4px;
  }
  
  .hero-desc {
    font-size: var(--font-size-sm);
  }
  
  .hero-actions {
    flex-direction: column;
    gap: var(--space-md);
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
  
  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-md);
    margin-top: var(--space-xl);
    padding-top: var(--space-xl);
  }
  
  .stat-card {
    padding: var(--space-md);
    gap: var(--space-md);
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: var(--font-size-lg);
  }
  
  .stat-value {
    font-size: var(--font-size-xl);
  }
  
  .stat-label {
    font-size: var(--font-size-xs);
  }
  
  .quick-nav,
  .features-grid,
  .inheritors-grid {
    grid-template-columns: 1fr;
    gap: var(--space-md);
  }
  
  .nav-card {
    padding: var(--space-lg);
    gap: var(--space-md);
  }
  
  .nav-icon {
    width: 48px;
    height: 48px;
  }
  
  .nav-icon .el-icon {
    font-size: 24px;
  }
  
  .nav-content h3 {
    font-size: var(--font-size-base);
  }
  
  .nav-content p {
    font-size: var(--font-size-xs);
  }
  
  .nav-arrow {
    width: 28px;
    height: 28px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .section-title {
    font-size: var(--font-size-xl);
  }
  
  .feature-card {
    padding: var(--space-lg);
  }
  
  .feature-header {
    margin-bottom: var(--space-md);
  }
  
  .feature-icon {
    width: 56px;
    height: 56px;
  }
  
  .feature-icon .el-icon {
    font-size: 28px;
  }
  
  .feature-card h3 {
    font-size: var(--font-size-md);
  }
  
  .feature-card p {
    font-size: var(--font-size-xs);
    margin-bottom: var(--space-md);
  }
  
  .inheritor-card {
    padding: var(--space-lg);
  }
  
  .inheritor-avatar {
    width: 60px;
    height: 60px;
    font-size: var(--font-size-lg);
    margin-bottom: var(--space-md);
  }
  
  .inheritor-content h4 {
    font-size: var(--font-size-base);
  }
  
  .inheritor-specialty {
    font-size: var(--font-size-xs);
  }
  
  .extend-card {
    padding: var(--space-lg);
    gap: var(--space-md);
  }
  
  .extend-icon {
    width: 56px;
    height: 56px;
  }
  
  .extend-icon .el-icon {
    font-size: 28px;
  }
  
  .extend-content h4 {
    font-size: var(--font-size-base);
  }
  
  .extend-content p {
    font-size: var(--font-size-xs);
  }
  
  .extend-arrow {
    width: 32px;
    height: 32px;
  }
  
  .cta-section {
    padding: var(--space-2xl) var(--space-lg);
  }
  
  .cta-content h2 {
    font-size: var(--font-size-xl);
  }
  
  .cta-content p {
    font-size: var(--font-size-sm);
  }
  
  .cta-actions .el-button {
    padding: var(--space-md) var(--space-xl);
    font-size: var(--font-size-sm);
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: var(--space-xl) var(--space-md);
  }
  
  .hero-title {
    font-size: var(--font-size-xl);
  }
  
  .hero-subtitle {
    font-size: var(--font-size-sm);
    letter-spacing: 2px;
  }
  
  .hero-stats {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    justify-content: center;
  }
}

.weekly-featured-section {
  padding: var(--space-2xl) var(--page-padding);
  max-width: var(--page-max-width);
  margin: 0 auto;
}

.featured-card {
  display: flex;
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid var(--border-light);
}

.featured-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.featured-card-img {
  width: 240px;
  height: 180px;
  flex-shrink: 0;
  overflow: hidden;
}

.featured-img {
  width: 100%;
  height: 100%;
}

.featured-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-rice);
  color: var(--text-muted);
}

.featured-card-content {
  flex: 1;
  padding: var(--space-xl);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  min-width: 0;
}

.featured-card-content h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.featured-card-content p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured-stats {
  display: flex;
  gap: var(--space-lg);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.featured-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.featured-skeleton {
  display: flex;
  gap: var(--space-lg);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
}

.hot-search-section {
  padding: var(--space-2xl) var(--page-padding);
  max-width: var(--page-max-width);
  margin: 0 auto;
}

.word-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  min-height: 120px;
  align-items: center;
}

.cloud-tag {
  display: inline-block;
  cursor: pointer;
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
}

.cloud-tag:hover {
  transform: scale(1.15);
  background: rgba(26, 82, 118, 0.08);
}

.cloud-skeleton {
  padding: var(--space-xl);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  text-align: center;
}

.latest-section {
  padding: var(--space-2xl) var(--page-padding);
  max-width: var(--page-max-width);
  margin: 0 auto;
}

.latest-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-lg);
}

.latest-card {
  background: var(--text-inverse);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid var(--border-light);
}

.latest-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.latest-card-img {
  width: 100%;
  height: 140px;
  overflow: hidden;
}

.latest-img {
  width: 100%;
  height: 100%;
}

.latest-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-rice);
  color: var(--text-muted);
  font-size: 36px;
}

.latest-card-body {
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.latest-card-body h4 {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.latest-tag {
  align-self: flex-start;
}

.latest-desc {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  line-height: 1.4;
}

.latest-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-muted-light);
}

.latest-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-lg);
}

.section-skeleton {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 768px) {
  .featured-card {
    flex-direction: column;
  }

  .featured-card-img {
    width: 100%;
    height: 180px;
  }

  .featured-card-content {
    padding: var(--space-lg);
  }

  .featured-card-content h3 {
    font-size: var(--font-size-md);
  }

  .latest-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: var(--space-md);
  }

  .latest-card-img {
    height: 120px;
  }

  .word-cloud {
    padding: var(--space-lg);
    gap: var(--space-sm);
  }
}

@media (max-width: 480px) {
  .featured-card-img {
    height: 150px;
  }

  .featured-card-content {
    padding: var(--space-md);
  }

  .featured-card-content h3 {
    font-size: var(--font-size-sm);
  }

  .latest-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-sm);
  }

  .latest-card-img {
    height: 100px;
  }

  .latest-card-body h4 {
    font-size: var(--font-size-xs);
  }

  .word-cloud {
    gap: var(--space-xs);
  }

  .cloud-tag {
    padding: 2px 6px;
  }
}
</style>
