<template>
<section class="extend-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">拓展功能</h2>
          <p class="section-desc">更多精彩内容等你探索</p>
        </div>
      </div>
      <div class="extend-grid">
        <div v-for="(mod, i) in modules" :key="i" class="extend-card" :style="{ '--delay': i * 0.1 + 's' }" @click="$router.push(mod.path)">
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
</template>

<script setup>

defineProps({ modules: { type: Array, default: () => [] } })
</script>
