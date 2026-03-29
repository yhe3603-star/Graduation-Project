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

    <section class="quick-nav">
      <div v-for="(item, i) in quickEntries" :key="i" class="nav-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(item.path)">
        <div class="nav-icon" :style="{ background: item.color }">
          <el-icon :size="32">
<component :is="item.icon" />
</el-icon>
        </div>
        <div class="nav-content">
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
        <div class="nav-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
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

    <section v-if="featuredInheritors.length" class="inheritors-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
传承人风采
</h2>
          <p class="section-desc">
守护侗医药文化的使者
</p>
        </div>
        <el-button type="primary" text class="view-all-btn" @click="$router.push('/inheritors')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="inheritors-grid">
        <div v-for="(item, i) in featuredInheritors" :key="item.id" class="inheritor-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push('/inheritors')">
          <div class="inheritor-avatar">
            <span>{{ (item.name || '传').charAt(0) }}</span>
          </div>
          <div class="inheritor-content">
            <h4>{{ item.name }}</h4>
            <div class="inheritor-level" :class="getLevelClass(item.level)">
{{ item.level }}
</div>
            <p class="inheritor-specialty">
{{ item.specialties || '侗医药传承' }}
</p>
          </div>
          <div v-if="item.level === '省级' || item.level === '自治区级'" class="inheritor-badge">
            <el-icon><Medal /></el-icon>
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
import { computed, inject, onMounted, ref } from 'vue'
import { Aim, ArrowRight, ChatDotRound, Compass, DataLine, Document, Folder, InfoFilled, Medal, Picture, User } from '@element-plus/icons-vue'
import { quickEntries, coreModules, extendModules, getLevelClass, createHeroStats } from '@/config/homeConfig'
import { extractPageData, logFetchError } from "@/utils";

const request = inject('request')
const stats = ref({ plants: 21, formulas: 12, inheritors: 10, therapies: 6 })
const featuredInheritors = ref([])

const heroStats = computed(() => createHeroStats(stats.value))

onMounted(async () => {
  try {
    const [pRes, iRes] = await Promise.all([
      request.get('/plants/list', { params: { page: 1, size: 1 }, skipAuthRefresh: true }).catch(() => ({})),
      request.get('/inheritors/list', { params: { page: 1, size: 5 }, skipAuthRefresh: true }).catch(() => ({}))
    ])
    const plantsData = extractPageData(pRes)
    const inheritorsData = extractPageData(iRes)
    stats.value.plants = plantsData.total
    stats.value.inheritors = inheritorsData.total
    featuredInheritors.value = inheritorsData.records.slice(0, 4)
  } catch (e) {
    logFetchError('首页数据', e)
  }
})
</script>

<style scoped>
@import '@/styles/home.css';
</style>
