<template>
  <div class="page-sidebar">
    <el-card
      class="stats-card"
      shadow="hover"
    >
      <template #header>
        <div class="card-header-title">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ title }}</span>
        </div>
      </template>
      <div class="stats-grid">
        <div
          v-for="(stat, i) in stats"
          :key="i"
          class="stat-item"
        >
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </el-card>

    <el-card
      v-if="hotItems?.length"
      class="hot-card"
      shadow="hover"
    >
      <template #header>
        <div class="card-header-title">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ hotTitle || '热门推荐' }}</span>
        </div>
      </template>
      <div class="hot-list">
        <div
          v-for="(item, i) in hotItems"
          :key="i"
          class="hot-item"
          @click="$emit('hotClick', item)"
        >
          <span
            class="hot-rank"
            :class="getRankClass(i)"
          >{{ i + 1 }}</span>
          <span class="hot-name">{{ item.name || item.title || item.nameCn || item.question || '未命名' }}</span>
        </div>
      </div>
    </el-card>

    <slot />
  </div>
</template>

<script setup>
import { DataAnalysis, TrendCharts } from "@element-plus/icons-vue"

defineProps({
  title: { type: String, default: "数据统计" },
  stats: { type: Array, default: () => [] },
  hotTitle: String,
  hotItems: { type: Array, default: () => [] }
})

defineEmits(["hotClick"])

const getRankClass = (i) => (i === 0 ? "gold" : i === 1 ? "silver" : i === 2 ? "bronze" : "")
</script>

<style scoped>
.page-sidebar { display: flex; flex-direction: column; gap: 20px; }
.stats-card, .hot-card { border-radius: 16px; border: none; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); }
.card-header-title { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--dong-blue); }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.stat-item { text-align: center; padding: 10px 8px; background: #f8fafb; border-radius: 10px; }
.stat-value { display: block; font-size: 20px; font-weight: 700; color: var(--dong-blue); }
.stat-label { font-size: 12px; color: var(--text-muted); }
.hot-list { display: flex; flex-direction: column; gap: 8px; }
.hot-item { display: flex; align-items: center; gap: 10px; padding: 8px; background: #f8fafb; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.hot-item:hover { background: #e8f4f8; }
.hot-rank { width: 22px; height: 22px; line-height: 22px; text-align: center; background: #e8f4f8; color: var(--dong-blue); border-radius: 6px; font-size: 12px; font-weight: 600; }
.hot-rank.gold { background: var(--dong-gold-light); color: var(--text-inverse); }
.hot-rank.silver { background: #bdc3c7; color: var(--text-inverse); }
.hot-rank.bronze { background: #cd7f32; color: var(--text-inverse); }
.hot-name { flex: 1; font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
