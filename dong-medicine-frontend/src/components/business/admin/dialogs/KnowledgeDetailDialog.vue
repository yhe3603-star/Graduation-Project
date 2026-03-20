<template>
  <el-dialog
    :model-value="visible"
    title="知识详情"
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ knowledge?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="标题">
        {{ knowledge?.title }}
      </el-descriptions-item>
      <el-descriptions-item label="类型">
        {{ knowledge?.type }}
      </el-descriptions-item>
      <el-descriptions-item label="疗法分类">
        {{ knowledge?.therapyCategory || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="疾病分类">
        {{ knowledge?.diseaseCategory || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="热度">
        {{ knowledge?.popularity }}
      </el-descriptions-item>
      <el-descriptions-item
        label="内容"
        :span="2"
      >
        {{ knowledge?.content }}
      </el-descriptions-item>
      <el-descriptions-item
        label="步骤"
        :span="2"
      >
        {{ knowledge?.steps }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(knowledge?.createdAt) }}
      </el-descriptions-item>
    </el-descriptions>

    <MediaDisplay
      v-if="knowledge?.images"
      :files="knowledge.images"
      :show-documents="false"
      image-title="相关图片"
    />

    <MediaDisplay
      v-if="knowledge?.videoUrl"
      :files="knowledge.videoUrl"
      :show-images="false"
      :show-documents="false"
      video-title="演示视频"
    />

    <MediaDisplay
      v-if="knowledge?.documents"
      :files="knowledge.documents"
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
  </el-dialog>
</template>

<script setup>
import { formatTime } from '@/utils/adminUtils'
import MediaDisplay from '@/components/business/media/MediaDisplay.vue'

defineProps({
  visible: { type: Boolean, default: false },
  knowledge: { type: Object, default: null }
})

defineEmits(['update:visible'])
</script>

<style scoped>
@import '@/styles/media-common.css';
</style>
