<template>
  <div class="media-display">
    <div
      v-if="showImages && imageList.length"
      class="media-section"
    >
      <el-divider
        v-if="showDivider"
        content-position="left"
      >
        {{ imageTitle }}
      </el-divider>
      <div class="image-grid">
        <div
          v-for="(img, index) in imageList"
          :key="index"
          class="image-item"
        >
          <el-image
            :src="img.url"
            fit="cover"
            class="preview-image"
            :preview-src-list="previewList"
            :initial-index="index"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
      </div>
    </div>

    <div
      v-if="showVideos && videoList.length"
      class="media-section"
    >
      <el-divider
        v-if="showDivider"
        content-position="left"
      >
        {{ videoTitle }}
      </el-divider>
      <div class="video-grid">
        <div
          v-for="(video, index) in videoList"
          :key="index"
          class="video-item"
        >
          <video
            :src="video.url"
            controls
            class="preview-video"
            preload="metadata"
          >您的浏览器不支持视频播放</video>
        </div>
      </div>
    </div>

    <div
      v-if="showDocuments && documentList.length"
      class="media-section"
    >
      <el-divider
        v-if="showDivider"
        content-position="left"
      >
        {{ documentTitle }}
      </el-divider>
      <div class="document-list">
        <div
          v-for="(doc, index) in documentList"
          :key="index"
          class="document-item"
        >
          <div class="document-icon">
            <el-icon
              :size="32"
              :color="getIconColor(doc.type)"
            >
              <component :is="getIcon(doc.type)" />
            </el-icon>
          </div>
          <div class="document-info">
            <span
              class="document-name"
              :title="doc.name"
            >{{ doc.name }}</span>
            <span class="document-size">{{ formatSize(doc.size) }}</span>
          </div>
          <div class="document-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleDownload(doc)"
            >
              <el-icon><Download /></el-icon>下载
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Picture, Download } from '@element-plus/icons-vue'
import { parseMediaList, separateMediaByType, downloadMedia, getFileIcon, getFileColor, formatFileSize } from '@/utils'

const props = defineProps({
  files: { type: [String, Array], default: '' },
  showImages: { type: Boolean, default: true },
  showVideos: { type: Boolean, default: true },
  showDocuments: { type: Boolean, default: true },
  showDivider: { type: Boolean, default: true },
  imageTitle: { type: String, default: '相关图片' },
  videoTitle: { type: String, default: '相关视频' },
  documentTitle: { type: String, default: '相关文档' }
})

const allFiles = computed(() => parseMediaList(props.files))

const { images, videos, documents } = computed(() => 
  separateMediaByType(allFiles.value)
).value

const imageList = computed(() => images)
const videoList = computed(() => videos)
const documentList = computed(() => documents)

const previewList = computed(() => imageList.value.map(img => img.url))

const getIcon = (type) => getFileIcon(type)
const getIconColor = (type) => getFileColor(type)
const formatSize = (bytes) => formatFileSize(bytes)

const handleDownload = (doc) => downloadMedia(doc)
</script>

<style scoped>
@import '@/styles/media-common.css';

.media-display { width: 100%; }
.media-section { margin-top: 20px; }
</style>
