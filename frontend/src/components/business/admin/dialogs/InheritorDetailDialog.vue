<template>
  <el-dialog
    :model-value="visible"
    title="传承人详情"
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ inheritor?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="姓名">
        {{ inheritor?.name }}
      </el-descriptions-item>
      <el-descriptions-item label="级别">
        <el-tag :type="getLevelTagType(inheritor?.level)">
          {{ inheritor?.level }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="经验年限">
        {{ inheritor?.experienceYears }}年
      </el-descriptions-item>
      <el-descriptions-item
        label="技艺特色"
        :span="2"
      >
        {{ inheritor?.specialties || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="个人简介"
        :span="2"
      >
        {{ inheritor?.bio || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="代表案例"
        :span="2"
      >
        {{ inheritor?.representativeCases || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="荣誉资质"
        :span="2"
      >
        {{ inheritor?.honors || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(inheritor?.createdAt) }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ formatTime(inheritor?.updatedAt) }}
      </el-descriptions-item>
    </el-descriptions>

    <MediaDisplay
      v-if="inheritor?.images"
      :files="inheritor.images"
      :show-documents="false"
      image-title="传承人照片"
    />

    <MediaDisplay
      v-if="inheritor?.videos"
      :files="inheritor.videos"
      :show-images="false"
      :show-documents="false"
      video-title="相关视频"
    />

    <MediaDisplay
      v-if="inheritor?.documents"
      :files="inheritor.documents"
      :show-images="false"
      :show-videos="false"
      document-title="资质文档"
    />

    <template #footer>
      <el-button
        type="primary"
        @click="$emit('update:visible', false)"
      >
        关闭
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { formatTime, getLevelTagType } from '@/utils/adminUtils'
import MediaDisplay from '@/components/business/media/MediaDisplay.vue'

defineProps({
  visible: { type: Boolean, default: false },
  inheritor: { type: Object, default: null }
})

defineEmits(['update:visible'])
</script>

<style scoped>
@import '@/styles/media-common.css';
</style>
