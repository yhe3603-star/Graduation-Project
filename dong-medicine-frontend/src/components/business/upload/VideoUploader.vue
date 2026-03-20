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
import { ref, computed, watch, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, VideoCamera, Delete } from '@element-plus/icons-vue'
import { getResourceUrl, getFileName, parseMediaList, logUploadError, logDeleteWarn } from '@/utils'

const request = inject('request')

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
const videoList = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const previewVisible = ref(false)
const previewUrl = ref('')

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/upload/video`)
const headers = computed(() => ({ Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' }))
const limitReached = computed(() => videoList.value.length >= props.limit)
const tipText = computed(() => `支持 mp4/avi/mov/wmv/flv/mkv 格式，单个视频不超过 ${props.maxSize}MB，最多 ${props.limit} 个`)

watch(() => props.modelValue, (newVal) => {
  if (!newVal) { videoList.value = []; return }
  const items = parseMediaList(newVal)
  videoList.value = items.map(item => ({
    url: item.url,
    path: item.path,
    name: item.name || item.originalFileName
  }))
}, { immediate: true })

const handleBeforeUpload = async (file) => {
  const extension = file.name.split('.').pop().toLowerCase()
  if (!['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'].includes(extension)) { ElMessage.error(`不支持的视频格式: ${extension}`); return false }
  if (file.size > props.maxSize * 1024 * 1024) { ElMessage.error(`视频大小不能超过 ${props.maxSize}MB`); return false }
  
  if (props.replaceConfirm && videoList.value.length > 0) {
    try {
      await ElMessageBox.confirm(
        '已存在上传的文件，是否替换为新文件？',
        '替换确认',
        {
          confirmButtonText: '替换',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      for (const video of videoList.value) {
        if (video?.path && request) {
          try {
            await request.delete('/upload', { params: { filePath: video.path } })
          } catch {
            logDeleteWarn('视频', video.path)
          }
        }
      }
      videoList.value = []
    } catch {
      return false
    }
  }
  
  if (videoList.value.length >= props.limit) { ElMessage.warning(`最多只能上传 ${props.limit} 个视频`); return false }
  uploading.value = true
  uploadProgress.value = 0
  return true
}

const handleProgress = (event) => { uploadProgress.value = Math.round(event.percent) }

const handleSuccess = (response, file) => {
  uploading.value = false
  uploadProgress.value = 0
  if (response.code === 200 && response.data) {
    videoList.value.push({ 
      url: getResourceUrl(response.data.fileUrl || response.data.filePath), 
      path: response.data.filePath || response.data.fileUrl, 
      name: response.data.originalFileName || file.name,
      size: response.data.fileSize || file.size || 0
    })
    updateModelValue()
    emit('success', response.data)
    ElMessage.success('视频上传成功')
  } else { ElMessage.error(response.msg || '上传失败'); emit('error', response.msg) }
}

const handleError = (error) => {
  uploading.value = false
  uploadProgress.value = 0
  console.error('视频上传失败:', error)
  ElMessage.error('视频上传失败，请重试')
  emit('error', error)
}

const handleRemove = async (index) => {
  const removed = videoList.value[index]
  if (removed?.path && request) {
    const isExternalUrl = removed.path.startsWith('http://') || removed.path.startsWith('https://')
    if (!isExternalUrl) {
      try {
        await request.delete('/upload', { params: { filePath: removed.path } })
      } catch {
        logDeleteWarn('视频', removed.path)
      }
    }
  }
  videoList.value.splice(index, 1)
  updateModelValue()
  emit('remove', removed)
}

const handlePreview = (video) => {
  previewUrl.value = video.url
  previewVisible.value = true
}

const updateModelValue = () => {
  const videos = videoList.value.map(v => ({ path: v.path, name: v.name, size: v.size }))
  emit('update:modelValue', props.multiple ? videos : videos[0] || null)
  emit('change', videos)
}

const clearVideos = () => {
  videoList.value = []
  emit('update:modelValue', props.multiple ? [] : '')
  emit('change', [])
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
