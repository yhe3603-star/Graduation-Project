<template>
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
</template>

<script setup>
import { Medal, Compass, InfoFilled } from '@element-plus/icons-vue'

defineProps({ heroStats: { type: Array, required: true } })
</script>
