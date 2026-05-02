<template>
  <BaseDetailDialog
    :model-value="visible"
    title="植物详情"
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ plant?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="中文名">
        {{ plant?.nameCn }}
      </el-descriptions-item>
      <el-descriptions-item label="侗语名">
        <div class="dong-name-row">
          <span>{{ plant?.nameDong || '暂无' }}</span>
          <HerbAudio
            v-if="plant?.nameDong"
            :plant-name="plant?.nameDong"
            :plant-id="plant?.id"
            :show-label="false"
            :icon-size="16"
          />
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="学名">
        {{ plant?.scientificName }}
      </el-descriptions-item>
      <el-descriptions-item label="分类">
        {{ plant?.category }}
      </el-descriptions-item>
      <el-descriptions-item label="用法">
        {{ plant?.usageWay }}
      </el-descriptions-item>
      <el-descriptions-item label="生长环境">
        {{ plant?.habitat }}
      </el-descriptions-item>
      <el-descriptions-item
        label="功效"
        :span="2"
      >
        {{ plant?.efficacy }}
      </el-descriptions-item>
      <el-descriptions-item
        label="药用故事"
        :span="2"
      >
        {{ plant?.story }}
      </el-descriptions-item>
      <el-descriptions-item
        label="地域分布"
        :span="2"
      >
        {{ plant?.distribution || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(plant?.createdAt) }}
      </el-descriptions-item>
    </el-descriptions>

    <MediaDisplay
      v-if="plant?.images"
      :files="plant.images"
      :show-documents="false"
      image-title="植物图片"
      video-title="相关视频"
    />

    <MediaDisplay
      v-if="plant?.videos"
      :files="plant.videos"
      :show-images="false"
      :show-documents="false"
      video-title="相关视频"
    />

    <MediaDisplay
      v-if="plant?.documents"
      :files="plant.documents"
      :show-images="false"
      :show-videos="false"
      document-title="相关文档"
    />

    <template #footer>
      <el-button
        type="primary"
        @click="$emit('update:visible', false)"
      >
        关闭
      </el-button>
    </template>
  </BaseDetailDialog>
</template>

<script setup>
import BaseDetailDialog from '@/components/base/BaseDetailDialog.vue'
import { formatTime } from '@/utils/adminUtils'
import MediaDisplay from '@/components/business/media/MediaDisplay.vue'
import HerbAudio from '@/components/business/media/HerbAudio.vue'

defineProps({
  visible: { type: Boolean, default: false },
  plant: { type: Object, default: null }
})

defineEmits(['update:visible'])
</script>

<style scoped>
@import '@/styles/media-common.css';

.dong-name-row {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
</style>
