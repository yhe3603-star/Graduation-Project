<template>
  <div class="home">
    <HeroSection :hero-stats="heroStats" />

    <WeeklyFeatured :item="weeklyFeatured" />

    <HotSearch
      :items="hotCloudItems"
      @select="searchKeyword"
    />

    <LatestUpdates
      :items="latestItems"
      @select="goToDetail"
    />

    <FeatureCards :modules="coreModules" />

    <ExtendModules :modules="extendModules" />

    <CtaSection />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { coreModules, extendModules, createHeroStats } from '@/config/homeConfig'
import { extractPageData, logFetchError, getFirstImage } from "@/utils"
import HeroSection from './home/HeroSection.vue'
import WeeklyFeatured from './home/WeeklyFeatured.vue'
import HotSearch from './home/HotSearch.vue'
import LatestUpdates from './home/LatestUpdates.vue'
import FeatureCards from './home/FeatureCards.vue'
import ExtendModules from './home/ExtendModules.vue'
import CtaSection from './home/CtaSection.vue'

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
  plant: '/plants', knowledge: '/knowledge', inheritor: '/inheritors',
  qa: '/qa', resource: '/resources'
}

const goToDetail = (item) => {
  if (!item) return
  router.push(DETAIL_ROUTES[item.type] || '/')
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
  return allItems.sort((a, b) => new Date(b.updatedAt || b.createdAt || 0) - new Date(a.updatedAt || a.createdAt || 0)).slice(0, 6)
}

const buildCloudItems = (plants, knowledge) => {
  const cloudMap = new Map()
  const addToCloud = (name, viewCount = 0) => {
    if (!name) return
    cloudMap.set(name, (cloudMap.get(name) || 0) + (viewCount || 1))
  }
  plants.forEach(p => { if (p.nameCn) addToCloud(p.nameCn, p.viewCount || 0); if (p.category) addToCloud(p.category, Math.max((p.viewCount || 0) - 5, 1)) })
  knowledge.forEach(k => { if (k.title) addToCloud(k.title, k.viewCount || 0) })
  const entries = [...cloudMap.entries()].sort((a, b) => b[1] - a[1]).slice(0, 20)
  const maxCount = entries.length > 0 ? entries[0][1] : 1
  const minCount = entries.length > 0 ? entries[entries.length - 1][1] : 1
  return entries.map(([name, count], index) => {
    const ratio = maxCount === minCount ? 0.5 : (count - minCount) / (maxCount - minCount)
    const size = 0.85 + ratio * 0.9
    const color = CLOUD_COLORS[index % CLOUD_COLORS.length]
    return { name, count, size, color }
  })
}

const fetchData = async () => {
  pageLoading.value = true
  try {
    const [plantRes, knowledgeRes, inheritorRes, qaRes, featuredRes, statsRes] = await Promise.all([
      request.get('/plants/list', { params: { page: 1, size: 10, sortBy: 'newest' } }).catch(() => ({})),
      request.get('/knowledge/list', { params: { page: 1, size: 10, sortBy: 'newest' } }).catch(() => ({})),
      request.get('/inheritors/list', { params: { page: 1, size: 10, sortBy: 'newest' } }).catch(() => ({})),
      request.get('/qa/list', { params: { page: 1, size: 10 } }).catch(() => ({})),
      request.get('/metadata/featured').catch(() => ({})),
      request.get('/stats/overview').catch(() => ({})),
    ])

    const plants = extractPageData(plantRes)?.records || extractPageData(plantRes) || []
    const knowledge = extractPageData(knowledgeRes)?.records || extractPageData(knowledgeRes) || []
    const inheritors = extractPageData(inheritorRes)?.records || extractPageData(inheritorRes) || []
    const qas = extractPageData(qaRes)?.records || extractPageData(qaRes) || []

    latestItems.value = buildLatestItems(plants, knowledge, inheritors, qas)
    hotCloudItems.value = buildCloudItems(plants, knowledge)
    weeklyFeatured.value = featuredRes?.data || featuredRes || null
    if (statsRes?.data) stats.value = { ...stats.value, ...statsRes.data }
  } catch (e) {
    logFetchError('首页数据', e)
  } finally {
    pageLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
@import '@/styles/home.css';
</style>
