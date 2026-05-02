<template>
<section class="latest-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <el-icon><Clock /></el-icon>最新更新
          </h2>
          <p class="section-desc">最近更新的侗医药内容</p>
        </div>
      </div>
      <div v-if="items.length > 0" class="latest-grid">
        <div
          v-for="item in items"
          :key="item.id + (item.type || '')"
          class="latest-card"
          @click="$emit('select', item)"
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
            <p class="latest-desc">{{ truncate(item.description || item.efficacy || item.bio || '', 50) }}</p>
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
</template>

<script setup>
import { ArrowRight, Clock } from '@element-plus/icons-vue'

defineProps({ items: { type: Array, default: () => [] } })

defineEmits(['select'])

function truncate(text, maxLen = 50) { if (!text) return ""; return text.length > maxLen ? text.substring(0, maxLen) + "..." : text }
</script>
