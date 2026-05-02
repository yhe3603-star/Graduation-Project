<template>
<section class="features-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">核心功能</h2>
          <p class="section-desc">全方位展示侗乡医药文化精髓</p>
        </div>
      </div>
      <div class="features-grid">
        <div v-for="(mod, i) in modules" :key="i" class="feature-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(mod.path)">
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
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({ modules: { type: Array, default: () => [] } })
</script>
