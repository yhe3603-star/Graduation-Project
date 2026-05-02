<template>
<section class="weekly-featured-section">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title">
            <el-icon><Medal /></el-icon>每周精选
          </h2>
          <p class="section-desc">为您精选本周最值得关注的侗医药知识</p>
        </div>
      </div>
      <div v-if="item" class="featured-card" @click="goToDetail(weeklyFeatured)">
        <div class="featured-card-img">
          <el-image
            v-if="item?.image"
            :src="item?.image"
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
            :type="item?.tagType || 'primary'"
            size="small"
            effect="dark"
            class="featured-tag"
          >
            {{ item?.typeLabel || '精选' }}
          </el-tag>
          <h3>{{ item?.title || item?.nameCn || item?.name }}</h3>
          <p>{{ item?.description || item?.efficacy || item?.bio || '' }}</p>
          <div class="featured-stats">
            <span v-if="item?.viewCount !== undefined">
              <el-icon><View /></el-icon>{{ item?.viewCount }} 次浏览
            </span>
            <span v-if="item?.favoriteCount !== undefined">
              <el-icon><Star /></el-icon>{{ item?.favoriteCount }} 人收藏
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
</template>

<script setup>
import { Medal, ArrowRight } from '@element-plus/icons-vue'

defineProps({ item: { type: Object, default: null } })
</script>
