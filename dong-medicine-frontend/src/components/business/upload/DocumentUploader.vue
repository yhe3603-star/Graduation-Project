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

    <DocumentPreview
      v-model="previewVisible"
      :document="previewDocument"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Document, Upload, UploadFilled, Delete, View, Download } from '@element-plus/icons-vue'
import { getFileIcon, getFileColor } from '@/utils'
import { formatFileSize } from '@/utils/adminUtils'
import { useFileUpload } from '@/composables/useFileUpload'
import DocumentPreview from '../media/DocumentPreview.vue'

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
const previewVisible = ref(false)
const previewDocument = ref(null)

const {
  fileList: documentList,
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
  clearFiles: clearDocuments,
} = useFileUpload({
  type: 'document',
  extensions: ['pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt'],
  extensionLabel: 'pdf/docx/doc/xlsx/xls/pptx/ppt/txt',
  uploadPath: '/upload/document',
  simulateProgress: false,
  props,
  emit,
})

// 文档特有：图标和格式化
const getIcon = (type) => getFileIcon(type)
const getIconColor = (type) => getFileColor(type)
const formatSize = (bytes) => formatFileSize(bytes)

// 文档预览（组件特有逻辑）
const handlePreview = (doc) => {
  previewDocument.value = {
    url: doc.url || doc.path,
    path: doc.path || doc.url,
    name: doc.name,
    size: doc.size,
    type: doc.type,
    originalFileName: doc.name
  }
  previewVisible.value = true
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
