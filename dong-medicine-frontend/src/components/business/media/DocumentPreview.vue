<template>
  <el-dialog
    v-if="!inline"
    v-model="visible"
    :title="document?.originalFileName || document?.fileName || '文档预览'"
    width="min(900px, 95vw)"
    append-to-body
    destroy-on-close
    class="document-preview-dialog"
    @close="handleClose"
  >
    <div class="preview-container">
      <div
        v-if="document"
        class="preview-header"
      >
        <el-tag
          :type="getFileTypeTag(document.type)"
          effect="light"
        >
          {{ document.type?.toUpperCase() || '文档' }}
        </el-tag>
        <span
          v-if="document.size"
          class="file-size"
        >{{ formatFileSize(document.size) }}</span>
      </div>

      <div class="preview-body">
        <div
          v-if="previewLoading && canPreviewWithKkFileView"
          class="preview-loading"
        >
          <el-icon
            class="is-loading"
            :size="48"
          >
            <Loading />
          </el-icon>
          <p>正在加载预览...</p>
        </div>
        <iframe
          v-if="canPreviewWithKkFileView"
          v-show="!previewLoading"
          :src="kkFileViewUrl"
          class="pdf-viewer"
          @load="onPreviewLoad"
          @error="onPreviewError"
        />
        <div
          v-else-if="isTxt"
          class="txt-viewer"
        >
          <pre v-if="txtContent">{{ txtContent }}</pre>
          <el-empty
            v-else-if="txtLoading"
            description="正在加载..."
          />
          <el-empty
            v-else
            description="无法加载文本内容"
          />
        </div>
        <div
          v-else
          class="preview-unavailable"
        >
          <el-icon :size="64">
            <Document />
          </el-icon>
          <p>{{ document?.type?.toUpperCase() || '该' }}文件暂不支持在线预览</p>
          <p class="preview-tip">
            {{ isLoggedIn ? '您可以下载后在本地查看' : '登录后即可下载查看' }}
          </p>
        </div>
      </div>

      <div
        v-if="document?.description"
        class="preview-description"
      >
        <h4>文件描述</h4>
        <p>{{ document.description }}</p>
      </div>
    </div>

    <template #footer>
      <div class="preview-footer">
        <el-button
          v-if="isLoggedIn"
          type="primary"
          @click="handleDownload"
        >
          <el-icon><Download /></el-icon>下载文件
        </el-button>
        <el-button
          v-else
          type="primary"
          @click="handleLoginPrompt"
        >
          <el-icon><Download /></el-icon>登录下载
        </el-button>
        <el-button @click="handleClose">
          关闭
        </el-button>
      </div>
    </template>
  </el-dialog>

  <div
    v-else
    class="document-preview-inline"
  >
    <div class="preview-body">
      <div
        v-if="previewLoading && canPreviewWithKkFileView"
        class="preview-loading"
      >
        <el-icon
          class="is-loading"
          :size="48"
        >
          <Loading />
        </el-icon>
        <p>正在加载预览...</p>
      </div>
      <iframe
        v-if="canPreviewWithKkFileView"
        v-show="!previewLoading"
        :src="kkFileViewUrl"
        class="pdf-viewer"
        @load="onPreviewLoad"
        @error="onPreviewError"
      />
      <div
        v-else-if="isTxt"
        class="txt-viewer"
      >
        <pre v-if="txtContent">{{ txtContent }}</pre>
        <el-empty
          v-else-if="txtLoading"
          description="正在加载..."
        />
        <el-empty
          v-else
          description="无法加载文本内容"
        />
      </div>
      <div
        v-else
        class="preview-unavailable"
      >
        <el-icon :size="64">
          <Document />
        </el-icon>
        <p>{{ document?.type?.toUpperCase() || '该' }}文件暂不支持在线预览</p>
        <p class="preview-tip">
          请下载后查看
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, ref, watch } from 'vue'
import { Document, Download, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatFileSize } from '@/utils/adminUtils'
import { normalizeUrl } from '@/utils/media'

const KKFILEVIEW_SERVER = import.meta.env.VITE_KKFILEVIEW_URL || '/kkfileview'
const KKFILEVIEW_FILE_HOST = import.meta.env.VITE_KKFILEVIEW_FILE_HOST || ''

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  document: { type: Object, default: null },
  inline: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'download', 'close'])

const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) })
const documentUrl = computed(() => normalizeUrl(props.document?.url || props.document?.path || ''))
const isTxt = computed(() => props.document?.type?.toLowerCase() === 'txt')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const showLoginDialog = inject('showLoginDialog')

const getFileExtension = (url) => {
  if (!url) return ''
  const path = url.split('?')[0]
  const lastDot = path.lastIndexOf('.')
  if (lastDot === -1) return ''
  return path.substring(lastDot + 1).toLowerCase()
}

const KKFILEVIEW_EXTENSIONS = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
const canPreviewWithKkFileView = computed(() => {
    const ext = getFileExtension(documentUrl.value)
    return KKFILEVIEW_EXTENSIONS.includes(ext) && documentUrl.value
})

const getFullFileUrl = (relativeUrl) => {
  if (!relativeUrl) return ''
  if (relativeUrl.startsWith('http://') || relativeUrl.startsWith('https://')) {
    return relativeUrl
  }
  if (KKFILEVIEW_FILE_HOST) {
    return KKFILEVIEW_FILE_HOST + (relativeUrl.startsWith('/') ? relativeUrl : '/' + relativeUrl)
  }
  const origin = window.location.origin
  return origin + (relativeUrl.startsWith('/') ? relativeUrl : '/' + relativeUrl)
}

const kkFileViewUrl = computed(() => {
  if (!documentUrl.value) return ''
  const fullUrl = getFullFileUrl(documentUrl.value)
  const base64Url = btoa(unescape(encodeURIComponent(fullUrl)))
  return `${KKFILEVIEW_SERVER}/onlinePreview?url=${base64Url}`
})

const previewLoading = ref(true)
const txtContent = ref('')
const txtLoading = ref(false)

const onPreviewLoad = () => {
  previewLoading.value = false
}

const loadTxtContent = async () => {
  if (!isTxt.value || !documentUrl.value) return
  txtLoading.value = true
  txtContent.value = ''
  try {
    const response = await fetch(documentUrl.value)
    if (response.ok) {
      txtContent.value = await response.text()
    }
  } catch (e) {
    console.error('加载文本内容失败:', e)
  } finally {
    txtLoading.value = false
  }
}

watch([visible, () => props.inline, () => props.document], ([vis, inline]) => {
  if (inline || vis) {
    previewLoading.value = true
    if (isTxt.value) {
      loadTxtContent()
    }
  }
}, { immediate: true })

const FILE_TYPE_TAGS = { pdf: 'danger', doc: 'primary', docx: 'primary', xls: 'success', xlsx: 'success', ppt: 'warning', pptx: 'warning', txt: 'info' }

const getFileTypeTag = (fileType) => FILE_TYPE_TAGS[fileType?.toLowerCase()] || 'info'

const onPreviewError = () => {
  previewLoading.value = false
  ElMessage.warning('文档预览加载失败，请尝试下载后查看')
}

const handleLoginPrompt = async () => {
  try {
    await ElMessageBox.confirm('下载文档需要登录，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    })
    visible.value = false
    showLoginDialog()
  } catch (e) {
    console.debug('用户取消登录提示:', e)
  }
}

const handleDownload = async () => {
  if (!isLoggedIn.value) {
    handleLoginPrompt()
    return
  }
  emit('download', props.document)
  const doc = props.document
  if (!doc?.id) {
    if (doc?.url || doc?.path) {
      const link = document.createElement('a')
      link.href = normalizeUrl(doc.url || doc.path)
      link.download = doc.originalFileName || doc.name || 'document'
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
    return
  }
  
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/resources/download/${doc.id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    if (!response.ok) {
      if (response.status === 401) {
        ElMessage.warning('登录已过期，请重新登录')
        localStorage.removeItem('token')
        window.location.href = '/'
      } else {
        throw new Error('下载失败')
      }
      return
    }
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = doc.originalFileName || doc.name || 'document'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    console.error('下载失败:', e)
    ElMessage.error('下载失败，请重试')
  }
}

const handleClose = () => { visible.value = false; emit('close') }
</script>

<style scoped>
.document-preview-dialog :deep(.el-dialog__body) { padding: 0; }
.preview-container { display: flex; flex-direction: column; }
.preview-header { display: flex; align-items: center; gap: 16px; padding: 16px 20px; background: #f8f9fa; border-bottom: 1px solid var(--border-light); }
.file-size { font-size: 13px; color: #666; }
.preview-body { min-height: 500px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; }
.pdf-viewer { width: 100%; height: 70vh; border: none; background: var(--text-inverse); }
.txt-viewer { width: 100%; height: 70vh; overflow: auto; background: var(--text-inverse); padding: 20px; }
.txt-viewer pre { margin: 0; white-space: pre-wrap; word-wrap: break-word; font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; line-height: 1.6; color: #333; }
.preview-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 400px; gap: 16px; color: #999; }
.preview-loading p { margin: 0; font-size: 14px; }
.preview-unavailable { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 400px; gap: 16px; color: #999; }
.preview-unavailable p { margin: 0; font-size: 14px; }
.preview-tip { font-size: 13px; color: var(--text-light); }
.preview-description { padding: 16px 20px; border-top: 1px solid var(--border-light); }
.preview-description h4 { margin: 0 0 8px 0; font-size: 14px; color: #333; }
.preview-description p { margin: 0; font-size: 13px; color: #666; line-height: 1.6; }
.preview-footer { display: flex; justify-content: flex-end; gap: 12px; }

.document-preview-inline { width: 100%; height: 100%; }
.document-preview-inline .preview-body { min-height: 450px; border-radius: 8px; overflow: hidden; }
.document-preview-inline .pdf-viewer { height: 450px; }
.document-preview-inline .txt-viewer { height: 450px; }
.document-preview-inline .preview-loading { min-height: 450px; }
.document-preview-inline .preview-unavailable { min-height: 450px; }

@media (max-width: 768px) {
  .document-preview-dialog :deep(.el-dialog) {
    margin: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    min-height: 100vh;
    border-radius: 0;
  }
  
  .document-preview-dialog :deep(.el-dialog__header) {
    padding: 12px 16px;
    position: sticky;
    top: 0;
    background: var(--text-inverse);
    z-index: 10;
  }
  
  .document-preview-dialog :deep(.el-dialog__body) {
    padding: 0;
  }
  
  .document-preview-dialog :deep(.el-dialog__footer) {
    padding: 12px 16px;
    position: sticky;
    bottom: 0;
    background: var(--text-inverse);
  }
  
  .preview-header {
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 16px;
  }
  
  .file-size {
    font-size: 12px;
  }
  
  .preview-body {
    min-height: 50vh;
  }
  
  .pdf-viewer {
    height: 50vh;
  }
  
  .txt-viewer {
    height: 50vh;
    padding: 12px;
  }
  
  .txt-viewer pre {
    font-size: 13px;
  }
  
  .preview-loading {
    min-height: 300px;
  }
  
  .preview-unavailable {
    min-height: 300px;
    padding: 20px;
  }
  
  .preview-unavailable p {
    font-size: 13px;
  }
  
  .preview-tip {
    font-size: 12px;
  }
  
  .preview-description {
    padding: 12px 16px;
  }
  
  .preview-description h4 {
    font-size: 13px;
  }
  
  .preview-description p {
    font-size: 12px;
  }
  
  .preview-footer {
    flex-direction: column;
    gap: 8px;
  }
  
  .preview-footer .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .document-preview-inline .preview-body { min-height: 350px; }
  .document-preview-inline .pdf-viewer { height: 350px; }
  .document-preview-inline .txt-viewer { height: 350px; }
  .document-preview-inline .preview-loading { min-height: 350px; }
  .document-preview-inline .preview-unavailable { min-height: 350px; }
}

@media (max-width: 480px) {
  .preview-header {
    padding: 10px 12px;
  }
  
  .preview-body {
    min-height: 40vh;
  }
  
  .pdf-viewer {
    height: 40vh;
  }
  
  .txt-viewer {
    height: 40vh;
  }
  
  .preview-loading {
    min-height: 250px;
  }
  
  .preview-unavailable {
    min-height: 250px;
  }
  
  .preview-unavailable .el-icon {
    font-size: 48px;
  }
  
  .document-preview-inline .preview-body { min-height: 280px; }
  .document-preview-inline .pdf-viewer { height: 280px; }
  .document-preview-inline .txt-viewer { height: 280px; }
  .document-preview-inline .preview-loading { min-height: 280px; }
  .document-preview-inline .preview-unavailable { min-height: 280px; }
}
</style>
