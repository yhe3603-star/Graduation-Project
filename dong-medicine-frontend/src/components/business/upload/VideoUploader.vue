<template>
  <div class="video-uploader">
    <div class="video-list">
      <div
        v-for="(video, index) in videoList"
        :key="index"
        class="video-item"
      >
        <div class="video-preview">
          <video
            :src="video.url"
            class="video-player"
            @click="handlePreview(video)"
          />
          <div class="video-overlay">
            <el-icon
              class="play-icon"
              @click="handlePreview(video)"
            >
              <VideoPlay />
            </el-icon>
          </div>
        </div>
        <div class="video-info">
          <span
            class="video-name"
            :title="video.name"
          >{{ video.name }}</span>
          <el-button
            type="danger"
            size="small"
            text
            @click="handleRemove(index)"
          >
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </div>
      </div>

      <el-upload
        v-show="!limitReached"
        ref="uploadRef"
        :action="uploadUrl"
        :headers="headers"
        :data="{ category }"
        accept=".mp4,.avi,.mov,.wmv,.flv,.mkv"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
:on-progress="handleProgress" :disabled="disabled || uploading" class="video-upload-trigger"
      >
        <div
          class="upload-placeholder"
          :class="{ 'is-uploading': uploading }"
        >
          <template v-if="uploading">
            <el-progress
              type="circle"
              :percentage="uploadProgress"
              :width="60"
            />
            <span class="upload-status">上传中...</span>
          </template>
          <template v-else>
            <el-icon class="upload-icon">
              <VideoCamera />
            </el-icon>
            <span class="upload-text">上传视频</span>
          </template>
        </div>
      </el-upload>
    </div>
    <div
      v-if="showTip"
      class="upload-tip"
    >
      {{ tipText }}
    </div>

    <el-dialog
      v-model="previewVisible"
      title="视频预览"
      width="800px"
      destroy-on-close
    >
      <video
        v-if="previewVisible"
        :src="previewUrl"
        controls
        autoplay
        style="width: 100%"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { VideoPlay, VideoCamera, Delete } from '@element-plus/icons-vue'
import { useFileUpload } from '@/composables/useFileUpload'

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  category: { type: String, default: 'plants' },
  limit: { type: Number, default: 5 },
  multiple: { type: Boolean, default: true },
  maxSize: { type: Number, default: 100 },
  disabled: { type: Boolean, default: false },
  showTip: { type: Boolean, default: true },
  replaceConfirm: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change', 'success', 'error', 'remove'])

const uploadRef = ref(null)
const previewVisible = ref(false)
const previewUrl = ref('')

const {
  fileList: videoList,
  uploading,
  uploadProgress,
  uploadUrl,
  headers,
  limitReached,
  tipText,
  handleBeforeUpload,
  handleProgress,
  handleSuccess,
  handleError,
  handleRemove,
  clearFiles: clearVideos,
} = useFileUpload({
  type: 'video',
  extensions: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'],
  extensionLabel: 'mp4/avi/mov/wmv/flv/mkv',
  uploadPath: '/upload/video',
  simulateProgress: false,
  props,
  emit,
})

// 视频预览（组件特有逻辑）
const handlePreview = (video) => {
  previewUrl.value = video.url
  previewVisible.value = true
}

defineExpose({ clearVideos, getVideos: () => videoList.value })
</script>

<style scoped>
.video-uploader { width: 100%; }
.video-list { display: flex; flex-wrap: wrap; gap: 16px; }
.video-item { width: 240px; border-radius: 8px; overflow: hidden; border: 1px solid #dcdfe6; background: var(--text-inverse); }
.video-preview { position: relative; width: 100%; height: 135px; background: #000; cursor: pointer; }
.video-player { width: 100%; height: 100%; object-fit: cover; }
.video-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: center; background: rgba(0, 0, 0, 0.3); opacity: 0; transition: opacity 0.2s; }
.video-preview:hover .video-overlay { opacity: 1; }
.play-icon { font-size: 48px; color: var(--text-inverse); cursor: pointer; }
.video-info { display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; }
.video-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: #606266; }
.video-upload-trigger { width: 240px; height: 180px; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 240px; height: 180px; border: 1px dashed #dcdfe6; border-radius: 8px; background: #fafafa; cursor: pointer; transition: all 0.2s; }
.upload-placeholder:hover { border-color: #409eff; background: #ecf5ff; }
.upload-placeholder.is-uploading { cursor: not-allowed; }
.upload-icon { font-size: 40px; color: #909399; margin-bottom: 12px; }
.upload-text { font-size: 14px; color: #909399; }
.upload-status { margin-top: 12px; font-size: 14px; color: #409eff; }
.upload-tip { margin-top: 8px; color: #909399; font-size: 12px; }
</style>
