<template>
  <el-dialog
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
        <iframe
          v-if="isPdf"
          :src="documentUrl"
          class="pdf-viewer"
          @error="onPreviewError"
        />
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
</template>

<script setup>
import { computed } from 'vue'
import { Document, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatFileSize } from '@/utils/adminUtils'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  document: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'download', 'close'])

const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) })
const documentUrl = computed(() => props.document?.url || props.document?.path || '')
const isPdf = computed(() => props.document?.type?.toLowerCase() === 'pdf')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const FILE_TYPE_TAGS = { pdf: 'danger', doc: 'primary', docx: 'primary', xls: 'success', xlsx: 'success', ppt: 'warning', pptx: 'warning', txt: 'info' }

const getFileTypeTag = (fileType) => FILE_TYPE_TAGS[fileType?.toLowerCase()] || 'info'

const onPreviewError = () => ElMessage.warning('文档预览加载失败，请尝试下载后查看')

const handleLoginPrompt = async () => {
  try {
    await ElMessageBox.confirm('下载文档需要登录，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    })
    window.location.href = '/'
  } catch {}
}

const handleDownload = () => {
  if (!isLoggedIn.value) {
    handleLoginPrompt()
    return
  }
  emit('download', props.document)
  if (props.document?.url || props.document?.path) {
    const link = document.createElement('a')
    link.href = props.document?.url || props.document?.path
    link.download = props.document?.originalFileName || props.document?.name || 'document'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
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
.preview-unavailable { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 400px; gap: 16px; color: #999; }
.preview-unavailable p { margin: 0; font-size: 14px; }
.preview-tip { font-size: 13px; color: var(--text-light); }
.preview-description { padding: 16px 20px; border-top: 1px solid var(--border-light); }
.preview-description h4 { margin: 0 0 8px 0; font-size: 14px; color: #333; }
.preview-description p { margin: 0; font-size: 13px; color: #666; line-height: 1.6; }
.preview-footer { display: flex; justify-content: flex-end; gap: 12px; }

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
  
  .preview-unavailable {
    min-height: 250px;
  }
  
  .preview-unavailable .el-icon {
    font-size: 48px;
  }
}
</style>
