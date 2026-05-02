<template>
<section class="hot-search-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <el-icon><TrendCharts /></el-icon>热门搜索
          </h2>
          <p class="section-desc">点击感兴趣的关键词，了解侗医药知识</p>
        </div>
      </div>
      <div v-if="items.length > 0" class="word-cloud">
        <span
          v-for="item in items"
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
</template>

<script setup>
import { TrendCharts } from '@element-plus/icons-vue'

defineProps({ items: { type: Array, default: () => [] } })

defineEmits(['select'])
</script>
