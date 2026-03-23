<template>
  <div class="file-uploader">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="headers"
      :accept="acceptTypes"
      :limit="limit"
      :multiple="multiple"
      :file-list="fileList"
      :before-upload="handleBeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :on-preview="handlePreview"
      :disabled="disabled"
      :list-type="listType"
      :auto-upload="autoUpload"
      :show-file-list="showFileList"
      :drag="drag"
      class="uploader"
    >
      <template v-if="drag">
        <el-icon class="el-icon--upload">
          <upload-filled />
        </el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
      </template>
      <template v-else-if="listType === 'picture-card'">
        <el-icon><Plus /></el-icon>
      </template>
      <template v-else>
        <el-button type="primary">
          <el-icon class="el-icon--left">
            <Upload />
          </el-icon>
          {{ buttonText }}
        </el-button>
      </template>
      <template #tip>
        <div
          v-if="showTip"
          class="el-upload__tip"
        >
          <slot name="tip">
            {{ tipText }}
          </slot>
        </div>
      </template>
    </el-upload>

    <el-dialog
      v-model="previewVisible"
      title="预览"
      width="800px"
    >
      <img
        v-if="previewType === 'image'"
        :src="previewUrl"
        alt="预览"
        style="width: 100%"
      >
      <video
        v-else-if="previewType === 'video'"
        :src="previewUrl"
        controls
        style="width: 100%"
      />
      <iframe
        v-else-if="previewType === 'pdf'"
        :src="previewUrl"
        style="width: 100%; height: 600px; border: none;"
      />
      <div
        v-else
        class="preview-placeholder"
      >
        <el-icon :size="60">
          <Document />
        </el-icon>
        <p>{{ previewFileName }}</p>
        <el-button
          type="primary"
          @click="downloadFile(previewUrl)"
        >
          下载文件
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload, UploadFilled, Document } from '@element-plus/icons-vue'
import { getResourceUrl, getFileName, logUploadError } from '@/utils'

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  fileType: { type: String, default: 'image', validator: (val) => ['image', 'video', 'document', 'all'].includes(val) },
  category: { type: String, default: 'common' },
  limit: { type: Number, default: 5 },
  multiple: { type: Boolean, default: true },
  maxSize: { type: Number, default: 10 },
  disabled: { type: Boolean, default: false },
  listType: { type: String, default: 'text', validator: (val) => ['text', 'picture', 'picture-card'].includes(val) },
  autoUpload: { type: Boolean, default: true },
  showFileList: { type: Boolean, default: true },
  drag: { type: Boolean, default: false },
  buttonText: { type: String, default: '选择文件' }
})

const emit = defineEmits(['update:modelValue', 'success', 'error', 'remove'])

const uploadRef = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('')
const previewFileName = ref('')

const CONFIG = {
  accept: {
    image: '.jpg,.jpeg,.png,.gif,.bmp,.webp',
    video: '.mp4,.avi,.mov,.wmv,.flv,.mkv',
    document: '.pdf,.docx,.doc,.xlsx,.xls,.pptx,.ppt,.txt',
    all: '.jpg,.jpeg,.png,.gif,.bmp,.webp,.mp4,.avi,.mov,.wmv,.flv,.mkv,.pdf,.docx,.doc,.xlsx,.xls,.pptx,.ppt,.txt'
  },
  extensions: {
    image: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
    video: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'],
    document: ['pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt'],
    all: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt']
  },
  typeNames: { image: '图片', video: '视频', document: '文档', all: '文件' },
  defaultSizes: { image: 10, video: 100, document: 50, all: 100 }
}

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/upload/${props.fileType === 'all' ? 'file' : props.fileType}`)

const headers = computed(() => ({ Authorization: sessionStorage.getItem('token') ? `Bearer ${sessionStorage.getItem('token')}` : '' }))

const acceptTypes = computed(() => CONFIG.accept[props.fileType] || CONFIG.accept.all)

const tipText = computed(() => {
  const ext = { image: 'jpg/jpeg/png/gif/bmp/webp', video: 'mp4/avi/mov/wmv/flv/mkv', document: 'pdf/docx/doc/xlsx/xls/pptx/ppt/txt', all: '图片、视频、文档' }
  return `支持${ext[props.fileType]}格式，单个文件不超过${props.maxSize}MB，最多上传${props.limit}个${CONFIG.typeNames[props.fileType]}`
})

const showTip = computed(() => !props.disabled)

const actualMaxSize = computed(() => props.maxSize || CONFIG.defaultSizes[props.fileType] || 10)

watch(() => props.modelValue, (newVal) => {
  if (!newVal) { fileList.value = []; return }
  const urls = Array.isArray(newVal) ? newVal : newVal.split(',').filter(Boolean)
  fileList.value = urls.map(url => ({
    name: getFileName(url), url: getResourceUrl(url), response: { data: { fileUrl: url, filePath: url } }
  }))
}, { immediate: true })

const handleBeforeUpload = (file) => {
  const extension = file.name.split('.').pop().toLowerCase()
  const allowed = CONFIG.extensions[props.fileType] || CONFIG.extensions.all
  if (!allowed.includes(extension)) { ElMessage.error(`不支持的文件格式: ${extension}`); return false }
  if (file.size > actualMaxSize.value * 1024 * 1024) { ElMessage.error(`文件大小不能超过 ${actualMaxSize.value}MB`); return false }
  return true
}

const handleSuccess = (response, file, uploadFileList) => {
  if (response.code === 200 && response.data) {
    file.response = { data: response.data }
    updateModelValue(uploadFileList)
    emit('success', response.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    emit('error', response.msg)
  }
}

const handleError = (error) => {
  logUploadError('文件', error)
  ElMessage.error('上传失败，请重试')
  emit('error', error)
}

const handleRemove = (file, uploadFileList) => {
  updateModelValue(uploadFileList)
  emit('remove', file)
}

const handleExceed = () => ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)

const handlePreview = (file) => {
  const url = file.url || file.response?.data?.fileUrl
  if (!url) return
  previewUrl.value = getResourceUrl(url)
  previewFileName.value = file.name
  const extension = file.name.split('.').pop().toLowerCase()
  previewType.value = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension) ? 'image'
    : ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'].includes(extension) ? 'video'
    : extension === 'pdf' ? 'pdf' : 'other'
  previewVisible.value = true
}

const updateModelValue = (uploadFileList) => {
  const urls = uploadFileList.filter(f => f.response?.data?.fileUrl || f.url).map(f => f.response?.data?.filePath || f.response?.data?.fileUrl || f.url)
  emit('update:modelValue', props.multiple ? urls : urls[0] || '')
}

const downloadFile = (url) => {
  const link = document.createElement('a')
  link.href = url
  link.download = previewFileName.value
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const clearFiles = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
  emit('update:modelValue', props.multiple ? [] : '')
}

defineExpose({ clearFiles, submit: () => uploadRef.value?.submit() })
</script>

<style scoped>
.file-uploader { width: 100%; }
.uploader :deep(.el-upload-dragger) { padding: 30px; }
.preview-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px; color: #909399; }
.preview-placeholder p { margin: 16px 0; font-size: 16px; }
.el-upload__tip { color: #909399; font-size: 12px; margin-top: 8px; }
</style>
