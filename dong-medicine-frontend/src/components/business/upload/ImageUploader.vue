<template>
  <div class="image-uploader">
    <div class="image-list">
      <draggable
        v-model="imageList"
        item-key="url"
        class="image-grid"
        :class="{ 'single': !multiple }"
        handle=".drag-handle"
        animation="200"
        @end="updateModelValue"
      >
        <template #item="{ element, index }">
          <div class="image-item">
            <el-image
              :src="element.url"
              fit="cover"
              class="image-preview"
              :preview-src-list="[element.url]"
              :initial-index="0"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="image-actions">
              <el-icon
                class="drag-handle"
                title="拖拽排序"
              >
                <Rank />
              </el-icon>
              <el-icon
                class="delete-btn"
                title="删除"
                @click="handleRemove(index)"
              >
                <Delete />
              </el-icon>
            </div>
            <div
              v-if="showName"
              class="image-name"
            >
              {{ element.name }}
            </div>
          </div>
        </template>
      </draggable>

      <el-upload
        v-show="!limitReached"
        ref="uploadRef"
        :action="uploadUrl"
        :headers="headers"
        :data="{ category }"
        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
:disabled="disabled || uploading" class="image-upload-trigger"
      >
        <div
          class="upload-placeholder"
          :class="{ 'is-uploading': uploading }"
        >
          <el-progress
            v-if="uploading"
            type="circle"
            :percentage="uploadProgress"
            :width="50"
          />
          <template v-else>
            <el-icon class="upload-icon">
              <Plus />
            </el-icon>
            <span class="upload-text">上传图片</span>
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
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, Delete, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { getResourceUrl, getFileName, parseMediaList, logUploadError, logDeleteWarn } from '@/utils'

const request = inject('request')

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  category: { type: String, default: 'plants' },
  limit: { type: Number, default: 9 },
  multiple: { type: Boolean, default: true },
  maxSize: { type: Number, default: 10 },
  disabled: { type: Boolean, default: false },
  showName: { type: Boolean, default: false },
  showTip: { type: Boolean, default: true },
  replaceConfirm: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change', 'success', 'error', 'remove'])

const uploadRef = ref(null)
const imageList = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const pendingFile = ref(null)

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/upload/image`)
const headers = computed(() => ({ Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' }))
const limitReached = computed(() => imageList.value.length >= props.limit)
const tipText = computed(() => `支持 jpg/jpeg/png/gif/bmp/webp 格式，单张不超过 ${props.maxSize}MB，最多 ${props.limit} 张`)

watch(() => props.modelValue, (newVal) => {
  if (!newVal) { imageList.value = []; return }
  const items = parseMediaList(newVal)
  imageList.value = items.map(item => ({
    url: item.url,
    path: item.path,
    name: item.name || item.originalFileName
  }))
}, { immediate: true })

const handleBeforeUpload = async (file) => {
  const extension = file.name.split('.').pop().toLowerCase()
  if (!['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)) { ElMessage.error(`不支持的图片格式: ${extension}`); return false }
  if (file.size > props.maxSize * 1024 * 1024) { ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`); return false }
  
  if (props.replaceConfirm && imageList.value.length > 0) {
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
      for (const img of imageList.value) {
        if (img?.path && request) {
          try {
            await request.delete('/upload', { params: { filePath: img.path } })
          } catch {
            logDeleteWarn('图片', img.path)
          }
        }
      }
      imageList.value = []
    } catch {
      return false
    }
  }
  
  if (imageList.value.length >= props.limit) { ElMessage.warning(`最多只能上传 ${props.limit} 张图片`); return false }
  uploading.value = true
  uploadProgress.value = 0
  file._progressInterval = setInterval(() => { if (uploadProgress.value < 90) uploadProgress.value += 10 }, 100)
  return true
}

const handleSuccess = (response, file) => {
  clearInterval(file._progressInterval)
  uploadProgress.value = 100
  setTimeout(() => { uploading.value = false; uploadProgress.value = 0 }, 500)
  if (response.code === 200 && response.data) {
    imageList.value.push({ 
      url: getResourceUrl(response.data.fileUrl || response.data.filePath), 
      path: response.data.filePath || response.data.fileUrl, 
      name: response.data.originalFileName || file.name,
      size: response.data.fileSize || file.size || 0
    })
    updateModelValue()
    emit('success', response.data)
    ElMessage.success('图片上传成功')
  } else { ElMessage.error(response.msg || '上传失败'); emit('error', response.msg) }
}

const handleError = (error, file) => {
  clearInterval(file._progressInterval)
  uploading.value = false
  uploadProgress.value = 0
  console.error('图片上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
  emit('error', error)
}

const handleRemove = async (index) => {
  const removed = imageList.value[index]
  if (removed?.path && request) {
    const isExternalUrl = removed.path.startsWith('http://') || removed.path.startsWith('https://')
    if (!isExternalUrl) {
      try {
        await request.delete('/upload', { params: { filePath: removed.path } })
      } catch {
        logDeleteWarn('图片', removed.path)
      }
    }
  }
  imageList.value.splice(index, 1)
  updateModelValue()
  emit('remove', removed)
}

const updateModelValue = () => {
  const images = imageList.value.map(img => ({ path: img.path, name: img.name, size: img.size }))
  emit('update:modelValue', props.multiple ? images : images[0] || null)
  emit('change', images)
}

const clearImages = () => {
  imageList.value = []
  emit('update:modelValue', props.multiple ? [] : '')
  emit('change', [])
}

defineExpose({ clearImages, getImages: () => imageList.value })
</script>

<style scoped>
.image-uploader { width: 100%; }
.image-list { display: flex; flex-wrap: wrap; gap: 12px; }
.image-grid { display: contents; }
.image-grid.single { display: flex; }
.image-item { position: relative; width: 120px; height: 120px; border-radius: 8px; overflow: hidden; border: 1px solid #dcdfe6; background: #f5f7fa; }
.image-preview { width: 100%; height: 100%; }
.image-error { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #909399; font-size: 30px; }
.image-actions { position: absolute; top: 0; right: 0; display: flex; gap: 4px; padding: 4px; background: rgba(0, 0, 0, 0.5); border-radius: 0 0 0 8px; opacity: 0; transition: opacity 0.2s; }
.image-item:hover .image-actions { opacity: 1; }
.image-actions .el-icon { color: var(--text-inverse); cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s; }
.image-actions .el-icon:hover { background: rgba(255, 255, 255, 0.2); }
.drag-handle { cursor: move; }
.image-name { position: absolute; bottom: 0; left: 0; right: 0; padding: 4px 8px; background: rgba(0, 0, 0, 0.5); color: var(--text-inverse); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.image-upload-trigger { width: 120px; height: 120px; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 120px; height: 120px; border: 1px dashed #dcdfe6; border-radius: 8px; background: #fafafa; cursor: pointer; transition: all 0.2s; }
.upload-placeholder:hover { border-color: #409eff; background: #ecf5ff; }
.upload-placeholder.is-uploading { cursor: not-allowed; }
.upload-icon { font-size: 28px; color: #909399; margin-bottom: 8px; }
.upload-text { font-size: 12px; color: #909399; }
.upload-tip { margin-top: 8px; color: #909399; font-size: 12px; }
</style>
