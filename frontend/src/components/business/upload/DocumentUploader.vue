<template>
  <div class="document-uploader">
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
            type="default"
            size="small"
            @click="handlePreview(doc)"
          >
            <el-icon><View /></el-icon>预览
          </el-button>
          <el-button
            type="danger"
            size="small"
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
        accept=".pdf,.docx,.doc,.xlsx,.xls,.pptx,.ppt,.txt"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-progress="handleProgress"
        :disabled="disabled || uploading"
        :drag="drag"
        class="document-upload-trigger"
        :class="{ 'is-drag': drag }"
      >
        <template v-if="drag">
          <div
            class="upload-dragger"
            :class="{ 'is-uploading': uploading }"
          >
            <el-icon
              class="upload-icon"
              :size="48"
            >
              <UploadFilled />
            </el-icon>
            <div class="upload-text">
              <template v-if="uploading">
                <el-progress
                  :percentage="uploadProgress"
                  :stroke-width="4"
                />
                <span>上传中...</span>
              </template>
              <template v-else>
                <span>拖拽文件到此处或 <em>点击上传</em></span>
              </template>
            </div>
            <div
              v-if="showTip"
              class="upload-tip-drag"
            >
              {{ tipText }}
            </div>
          </div>
        </template>
        <template v-else>
          <div
            class="upload-button"
            :class="{ 'is-uploading': uploading }"
          >
            <template v-if="uploading">
              <el-progress
                type="circle"
                :percentage="uploadProgress"
                :width="40"
              />
              <span class="upload-status">上传中...</span>
            </template>
            <template v-else>
              <el-icon><Upload /></el-icon>
              <span>{{ buttonText }}</span>
            </template>
          </div>
        </template>
      </el-upload>
    </div>
    <div
      v-if="showTip && !drag"
      class="upload-tip"
    >
      {{ tipText }}
    </div>

    <el-dialog
      v-model="previewVisible"
      :title="previewTitle"
      width="800px"
      destroy-on-close
    >
      <iframe
        v-if="previewType === 'pdf'"
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
          @click="downloadFile(previewUrl, previewFileName)"
        >
          <el-icon><Download /></el-icon>下载文件
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Upload, UploadFilled, Delete, View, Download } from '@element-plus/icons-vue'
import { getResourceUrl, getFileType, parseDocumentList, getFileIcon, getFileColor, logDeleteWarn } from '@/utils'
import { formatFileSize } from '@/utils/adminUtils'

const request = inject('request')

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  category: { type: String, default: 'common' },
  limit: { type: Number, default: 5 },
  multiple: { type: Boolean, default: true },
  maxSize: { type: Number, default: 50 },
  disabled: { type: Boolean, default: false },
  showTip: { type: Boolean, default: true },
  drag: { type: Boolean, default: false },
  buttonText: { type: String, default: '上传文档' },
  replaceConfirm: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change', 'success', 'error', 'remove'])

const uploadRef = ref(null)
const documentList = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('文档预览')
const previewFileName = ref('')
const previewType = ref('')

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/upload/document`)
const headers = computed(() => ({ Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' }))
const limitReached = computed(() => documentList.value.length >= props.limit)
const tipText = computed(() => `支持 pdf/docx/doc/xlsx/xls/pptx/ppt/txt 格式，单个文档不超过 ${props.maxSize}MB，最多 ${props.limit} 个`)

const getIcon = (type) => getFileIcon(type)
const getIconColor = (type) => getFileColor(type)
const formatSize = (bytes) => formatFileSize(bytes)

watch(() => props.modelValue, (newVal) => {
  if (!newVal) { documentList.value = []; return }
  const items = parseDocumentList(newVal)
  documentList.value = items.map(item => ({
    url: item.url,
    path: item.path,
    name: item.name,
    type: item.type,
    size: item.size
  }))
}, { immediate: true })

const handleBeforeUpload = async (file) => {
  const extension = file.name.split('.').pop().toLowerCase()
  if (!['pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt'].includes(extension)) {
    ElMessage.error(`不支持的文档格式: ${extension}`)
    return false
  }
  if (file.size > props.maxSize * 1024 * 1024) {
    ElMessage.error(`文档大小不能超过 ${props.maxSize}MB`)
    return false
  }
  
  if (props.replaceConfirm && documentList.value.length > 0) {
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
      for (const doc of documentList.value) {
        if (doc?.path && request) {
          try {
            await request.delete('/upload', { params: { filePath: doc.path } })
          } catch {
            logDeleteWarn('文档', doc.path)
          }
        }
      }
      documentList.value = []
    } catch {
      return false
    }
  }
  
  if (documentList.value.length >= props.limit) {
    ElMessage.warning(`最多只能上传 ${props.limit} 个文档`)
    return false
  }
  uploading.value = true
  uploadProgress.value = 0
  return true
}

const handleProgress = (event) => {
  uploadProgress.value = Math.round(event.percent)
}

const handleSuccess = (response, file) => {
  uploading.value = false
  uploadProgress.value = 0
  if (response.code === 200 && response.data) {
    const filePath = response.data.filePath || response.data.fileUrl
    documentList.value.push({
      url: getResourceUrl(response.data.fileUrl || filePath),
      path: filePath,
      name: response.data.originalFileName || file.name,
      type: getFileType(filePath),
      size: response.data.fileSize || file.size
    })
    updateModelValue()
    emit('success', response.data)
    ElMessage.success('文档上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    emit('error', response.msg)
  }
}

const handleError = (error) => {
  uploading.value = false
  uploadProgress.value = 0
  console.error('文档上传失败:', error)
  ElMessage.error('文档上传失败，请重试')
  emit('error', error)
}

const handleRemove = async (index) => {
  const removed = documentList.value[index]
  if (removed?.path && request) {
    const isExternalUrl = removed.path.startsWith('http://') || removed.path.startsWith('https://')
    if (!isExternalUrl) {
      try {
        await request.delete('/upload', { params: { filePath: removed.path } })
      } catch {
        logDeleteWarn('文档', removed.path)
      }
    }
  }
  documentList.value.splice(index, 1)
  updateModelValue()
  emit('remove', removed)
}

const handlePreview = (doc) => {
  previewUrl.value = doc.url || doc.path
  previewFileName.value = doc.name
  previewTitle.value = '文档预览 - ' + doc.name
  previewType.value = doc.type
  previewVisible.value = true
}

const downloadFile = (url, filename) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const updateModelValue = () => {
  const docs = documentList.value.map(d => ({
    path: d.path,
    name: d.name,
    size: d.size,
    type: d.type
  }))
  emit('update:modelValue', props.multiple ? docs : docs[0] || null)
  emit('change', docs)
}

const clearDocuments = () => {
  documentList.value = []
  emit('update:modelValue', props.multiple ? [] : '')
  emit('change', [])
}

defineExpose({ clearDocuments, getDocuments: () => documentList.value })
</script>

<style scoped>
.document-uploader { width: 100%; }
.document-list { display: flex; flex-direction: column; gap: 12px; }
.document-item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border: 1px solid #dcdfe6; border-radius: 8px; background: var(--text-inverse); transition: all 0.2s; }
.document-item:hover { border-color: #409eff; background: #ecf5ff; }
.document-icon { flex-shrink: 0; width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; border-radius: 8px; }
.document-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.document-name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.document-size { font-size: 12px; color: #909399; }
.document-actions { flex-shrink: 0; display: flex; gap: 8px; }
.document-upload-trigger { width: 100%; }
.upload-dragger { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px; border: 1px dashed #d9d9d9; border-radius: 8px; background: #fafafa; cursor: pointer; transition: all 0.2s; }
.upload-dragger:hover { border-color: #409eff; }
.upload-dragger.is-uploading { cursor: not-allowed; }
.upload-button { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; border: 1px solid #dcdfe6; border-radius: 8px; background: var(--text-inverse); cursor: pointer; transition: all 0.2s; font-size: 14px; color: #606266; }
.upload-button:hover { border-color: #409eff; color: #409eff; }
.upload-button.is-uploading { cursor: not-allowed; border-color: #409eff; color: #409eff; }
.upload-icon { color: #909399; }
.upload-dragger:hover .upload-icon { color: #409eff; }
.upload-text { margin-top: 12px; color: #606266; font-size: 14px; text-align: center; }
.upload-text em { color: #409eff; font-style: normal; }
.upload-status { margin-top: 8px; font-size: 14px; color: #409eff; }
.upload-tip { margin-top: 8px; color: #909399; font-size: 12px; }
.upload-tip-drag { margin-top: 8px; color: #909399; font-size: 12px; text-align: center; }
.preview-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px; color: #909399; }
.preview-placeholder p { margin: 20px 0; font-size: 16px; }
</style>
