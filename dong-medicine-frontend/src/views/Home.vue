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
        <p class="hero-subtitle">保护 · 传承 · 活态传播</p>
        <p class="hero-desc">三江侗族自治县传统医药文化数字化保护与传播平台</p>
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
          <p class="section-desc">为您精选本周最值得关注的侗医药知识</p>
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
                <el-icon :size="48"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="featured-img-placeholder">
            <el-icon :size="48"><Picture /></el-icon>
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
          <p class="section-desc">点击感兴趣的关键词，了解侗医药知识</p>
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
          <p class="section-desc">最近更新的侗医药内容</p>
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
            <p class="latest-desc">{{ truncateText(item.description || item.efficacy || item.bio || '', 50) }}</p>
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
          <h2 class="section-title">核心功能</h2>
          <p class="section-desc">全方位展示侗乡医药文化精髓</p>
        </div>
      </div>
      <div class="features-grid">
        <div v-for="(mod, i) in coreModules" :key="i" class="feature-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(mod.path)">
          <div class="feature-header">
            <div class="feature-icon" :style="{ background: mod.bgColor }">
              <el-icon :size="36"><component :is="mod.icon" /></el-icon>
            </div>
            <div class="feature-badge">{{ mod.count }}</div>
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
          <h2 class="section-title">拓展功能</h2>
          <p class="section-desc">更多精彩内容等你探索</p>
        </div>
      </div>
      <div class="extend-grid">
        <div v-for="(mod, i) in extendModules" :key="i" class="extend-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(mod.path)">
          <div class="extend-icon">
            <el-icon :size="40"><component :is="mod.icon" /></el-icon>
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
import { extractPageData, logFetchError, getFirstImage, formatTime } from "@/utils";

const router = useRouter()

const coreModules = [
  { title: '知识库', desc: '系统整理侗医药理论知识，包含病因病机、诊断方法、治疗原则等内容', path: '/knowledge', count: '50+ 条目', bgColor: 'linear-gradient(135deg, #1A5276, var(--dong-indigo-dark))', icon: Document },
  { title: '传承人', desc: '记录各级非遗传承人档案，展示传承谱系与技艺特色', path: '/inheritors', count: '18+ 位', bgColor: 'linear-gradient(135deg, #28B463, var(--dong-jade-dark))', icon: User },
  { title: '药用图鉴', desc: '收录黔东南道地药材，配以精美图片与详细功效说明', path: '/plants', count: '50+ 种', bgColor: 'linear-gradient(135deg, var(--dong-gold-light), #e8930c)', icon: Picture },
  { title: '问答社区', desc: '互动问答平台，解答侗医药相关问题', path: '/qa', count: '60+ 问题', bgColor: 'linear-gradient(135deg, #667eea, #764ba2)', icon: ChatDotRound }
]

const extendModules = [
  { title: '文化互动', desc: '趣味答题、植物识别游戏，寓教于乐', path: '/interact', icon: Aim },
  { title: '学习资源', desc: '视频教程、文档资料，深入学习', path: '/resources', icon: Folder },
  { title: '数据可视化', desc: '统计分析，直观展示平台数据', path: '/visual', icon: DataLine }
]

const createHeroStats = (stats) => [
  { icon: Picture, value: stats.plants, label: '种药用植物' },
  { icon: Document, value: stats.formulas, label: '项经典药方' },
  { icon: User, value: stats.inheritors, label: '名传承人' },
  { icon: Medal, value: stats.therapies, label: '类核心疗法' }
]

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

const loadHotSearches = async () => {
  try {
    const res = await request.get('/search/hot', { params: { limit: 20 }, skipAuthRefresh: true })
    const data = (res.data || res || []).filter(item => item.name)
    if (data.length === 0) return buildCloudItemsFromLists([], [])
    const maxVal = data[0]?.value || 1
    const minVal = data[data.length - 1]?.value || 1
    return data.map((item, index) => {
      const ratio = maxVal === minVal ? 0.5 : (item.value - minVal) / (maxVal - minVal)
      return {
        name: item.name,
        size: Math.round(12 + ratio * 18),
        opacity: Math.round((0.5 + ratio * 0.5) * 10) / 10,
        color: CLOUD_COLORS[index % CLOUD_COLORS.length]
      }
    }).sort(() => Math.random() - 0.5)
  } catch {
    return []
  }
}

const buildCloudItemsFromLists = (plants, knowledge) => {
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

    hotCloudItems.value = await loadHotSearches()

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
@import '@/styles/home.css';

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
