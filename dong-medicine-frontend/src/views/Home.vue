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
const stats = ref({ plants: 21, formulas: 10, inheritors: 10, therapies: 5 })
const featuredInheritors = ref([])

const heroStats = computed(() => createHeroStats(stats.value))

onMounted(async () => {
  try {
    const [pRes, iRes] = await Promise.all([
      request.get('/plants/list', { params: { page: 1, size: 1 } }).catch(() => ({})),
      request.get('/inheritors/list', { params: { page: 1, size: 5 } }).catch(() => ({}))
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
.home {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--space-xl) var(--space-4xl);
}

.hero-section {
  position: relative;
  padding: var(--space-4xl) var(--space-3xl);
  border-radius: var(--radius-2xl);
  margin-bottom: var(--space-2xl);
  overflow: hidden;
  background: linear-gradient(135deg, var(--dong-indigo) 0%, var(--dong-indigo-dark) 50%, var(--dong-indigo) 100%);
  color: var(--text-inverse);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-bg {
  position: absolute;
  inset: 0;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M50 0L55 20L75 25L55 30L50 50L45 30L25 25L45 20Z' fill='%23ffffff' fill-opacity='0.03'/%3E%3C/svg%3E");
  background-size: 100px 100px;
  animation: patternMove 20s linear infinite;
}

@keyframes patternMove {
  0% { background-position: 0 0; }
  100% { background-position: 100px 100px; }
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
  letter-spacing: 8px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
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
}

.nav-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
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
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
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
</style>
