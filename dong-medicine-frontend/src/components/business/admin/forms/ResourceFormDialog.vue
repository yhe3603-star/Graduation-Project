<template>
  <el-dialog 
    :model-value="visible" 
    :title="isEdit ? '编辑学习资源' : '新增学习资源'" 
    width="800px" 
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form
      :model="form"
      label-width="80px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="标题">
            <el-input
              v-model="form.title"
              placeholder="请输入资源标题"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类">
            <el-select
              v-model="form.category"
              style="width: 100%"
              placeholder="请选择分类"
            >
              <el-option
                label="入门"
                value="入门"
              />
              <el-option
                label="进阶"
                value="进阶"
              />
              <el-option
                label="专业"
                value="专业"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="资源文件">
        <div class="upload-section">
          <el-radio-group
            v-model="uploadType"
            class="upload-type-selector"
          >
            <el-radio-button label="image">
              图片
            </el-radio-button>
            <el-radio-button label="video">
              视频
            </el-radio-button>
            <el-radio-button label="document">
              文档
            </el-radio-button>
          </el-radio-group>
          
          <div
            v-if="hasOtherTypeFiles"
            class="other-type-warning"
          >
            <el-alert
              type="warning"
              :closable="false"
              show-icon
            >
              <template #title>
                当前已有{{ otherTypeText }}文件，如需上传{{ currentTypeText }}请先删除现有文件
              </template>
            </el-alert>
          </div>
          
          <div class="upload-area">
            <ImageUploader
              v-show="uploadType === 'image'"
              ref="imageUploaderRef"
              v-model="imageFiles"
              category="resources"
              :limit="9"
              :max-size="10"
              :multiple="true"
              :show-tip="true"
              :disabled="hasOtherTypeFiles"
              @success="handleFileUploadSuccess('image', $event)"
            />
            <VideoUploader
              v-show="uploadType === 'video'"
              ref="videoUploaderRef"
              v-model="videoFiles"
              category="resources"
              :limit="5"
              :max-size="100"
              :multiple="true"
              :disabled="hasOtherTypeFiles"
              @success="handleFileUploadSuccess('video', $event)"
            />
            <DocumentUploader
              v-show="uploadType === 'document'"
              ref="documentUploaderRef"
              v-model="documentFiles"
              category="resources"
              :limit="5"
              :max-size="50"
              :multiple="true"
              drag
              :show-tip="true"
              :disabled="hasOtherTypeFiles"
              @success="handleFileUploadSuccess('document', $event)"
            />
          </div>
        </div>
      </el-form-item>
      
      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="资源描述"
        />
      </el-form-item>

      <el-divider content-position="left">
        更新日志
      </el-divider>
      
      <el-form-item label="">
        <UpdateLogCard 
          :logs="updateLogs" 
          :editable="true" 
          title="操作记录"
          @add="handleAddLog"
          @edit="handleEditLog"
          @delete="handleDeleteLog"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="$emit('update:visible', false)">
        取消
      </el-button>
      <el-button
        type="primary"
        :loading="saving"
        @click="handleSave"
      >
        保存
      </el-button>
    </template>

    <UpdateLogDialog 
      v-model:visible="logDialogVisible"
      :editing-log="editingLog"
      @save="handleSaveLog"
    />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUploader from '@/components/business/upload/ImageUploader.vue'
import VideoUploader from '@/components/business/upload/VideoUploader.vue'
import DocumentUploader from '@/components/business/upload/DocumentUploader.vue'
import UpdateLogCard from '@/components/business/display/UpdateLogCard.vue'
import UpdateLogDialog from '@/components/business/display/UpdateLogDialog.vue'
import { useUpdateLog } from '@/composables/useUpdateLog'
import { parseMediaList, stringifyMediaList, getMediaType } from '@/utils/media'
import { getFileName } from '@/utils'

const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'save'])

const { 
  parseUpdateLog, stringifyUpdateLog, addLog, updateLog, deleteLog, 
  logDialogVisible, editingLog, openLogDialog, closeLogDialog
} = useUpdateLog()

const getDefaultForm = () => ({ 
  id: null, title: "", category: "", fileType: "image", fileUrl: null, 
  fileName: "", fileSize: 0, files: "[]", description: "", updateLog: "" 
})

const form = ref(getDefaultForm())
const saving = ref(false)
const uploadType = ref('image')
const isInitializing = ref(false)

const imageFiles = ref([])
const videoFiles = ref([])
const documentFiles = ref([])

const imageUploaderRef = ref(null)
const videoUploaderRef = ref(null)
const documentUploaderRef = ref(null)

const isEdit = computed(() => !!form.value.id)

const updateLogs = computed(() => parseUpdateLog(form.value.updateLog))

const TYPE_NAMES = {
  image: '图片',
  video: '视频',
  document: '文档'
}

const currentTypeText = computed(() => TYPE_NAMES[uploadType.value] || '')

const hasOtherTypeFiles = computed(() => {
  const currentType = uploadType.value
  if (currentType !== 'image' && imageFiles.value.length > 0) return true
  if (currentType !== 'video' && videoFiles.value.length > 0) return true
  if (currentType !== 'document' && documentFiles.value.length > 0) return true
  return false
})

const otherTypeText = computed(() => {
  const currentType = uploadType.value
  const types = []
  if (currentType !== 'image' && imageFiles.value.length > 0) types.push('图片')
  if (currentType !== 'video' && videoFiles.value.length > 0) types.push('视频')
  if (currentType !== 'document' && documentFiles.value.length > 0) types.push('文档')
  return types.join('、')
})

const VIDEO_EXTS = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv']
const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
const DOC_EXTS = ['pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt']

const detectFileTypeFromUrl = (url) => {
  if (!url) return 'document'
  const ext = url.split('.').pop()?.toLowerCase() || ''
  if (VIDEO_EXTS.includes(ext)) return 'video'
  if (IMAGE_EXTS.includes(ext)) return 'image'
  if (DOC_EXTS.includes(ext)) return 'document'
  return 'document'
}

const parseFilesToTypeArrays = (filesData) => {
  const images = []
  const videos = []
  const documents = []
  
  if (!filesData) return { images, videos, documents }
  
  const files = parseMediaList(filesData)
  
  files.forEach(file => {
    const fileType = file.type || detectFileTypeFromUrl(file.path || file.url || '')
    const fileObj = {
      path: file.path || file.url || '',
      url: file.url || file.path || '',
      name: file.name || file.originalFileName || getFileName(file.path || file.url || ''),
      size: file.size || 0,
      type: fileType
    }
    
    if (fileType === 'image') {
      images.push(fileObj)
    } else if (fileType === 'video') {
      videos.push(fileObj)
    } else {
      documents.push(fileObj)
    }
  })
  
  return { images, videos, documents }
}

watch(() => props.visible, async (val) => {
  if (val) {
    isInitializing.value = true
    if (props.data) {
      form.value = { 
        ...props.data, 
        updateLog: props.data.updateLog || ''
      }
      
      const { images, videos, documents } = parseFilesToTypeArrays(props.data.files)
      imageFiles.value = images
      videoFiles.value = videos
      documentFiles.value = documents
      
      if (images.length > 0) {
        uploadType.value = 'image'
      } else if (videos.length > 0) {
        uploadType.value = 'video'
      } else if (documents.length > 0) {
        uploadType.value = 'document'
      } else {
        uploadType.value = 'image'
      }
    } else {
      form.value = getDefaultForm()
      imageFiles.value = []
      videoFiles.value = []
      documentFiles.value = []
      uploadType.value = 'image'
    }
    await nextTick()
    setTimeout(() => { isInitializing.value = false }, 100)
  }
})

const handleFileUploadSuccess = (type, fileData) => {
  form.value.fileType = type
}

const handleAddLog = () => openLogDialog()

const handleEditLog = (log) => openLogDialog(log)

const handleDeleteLog = (log) => {
  form.value.updateLog = stringifyUpdateLog(deleteLog(form.value.updateLog, log.id))
}

const handleSaveLog = (logData) => {
  if (editingLog.value) {
    form.value.updateLog = stringifyUpdateLog(updateLog(form.value.updateLog, editingLog.value.id, logData.content))
  } else {
    form.value.updateLog = stringifyUpdateLog(addLog(form.value.updateLog, logData.content, logData.operator))
  }
  closeLogDialog()
}

const handleSave = () => {
  if (!form.value.title) { ElMessage.warning('请输入资源标题'); return }
  if (!form.value.category) { ElMessage.warning('请选择分类'); return }
  
  const allFiles = [
    ...imageFiles.value.map(f => ({ ...f, type: 'image' })),
    ...videoFiles.value.map(f => ({ ...f, type: 'video' })),
    ...documentFiles.value.map(f => ({ ...f, type: 'document' }))
  ]
  
  if (allFiles.length === 0) { ElMessage.warning('请上传资源文件'); return }
  
  const files = stringifyMediaList(allFiles)
  const firstFile = allFiles[0]
  const fileType = firstFile.type || 'document'
  const totalSize = allFiles.reduce((sum, f) => sum + (f.size || 0), 0)
  
  const autoLog = isEdit.value ? '更新学习资源' : '新增学习资源'
  const currentLogs = parseUpdateLog(form.value.updateLog)
  const hasRecentLog = currentLogs.length > 0 && currentLogs[0].time === new Date().toISOString().split('T')[0]
  
  const finalUpdateLog = hasRecentLog 
    ? form.value.updateLog 
    : stringifyUpdateLog(addLog(form.value.updateLog, autoLog, '管理员'))
  
  emit('save', { 
    ...form.value, 
    fileUrl: firstFile.path || firstFile.url || '', 
    fileName: firstFile.name || form.value.title,
    fileSize: totalSize,
    fileType: fileType,
    files: files,
    updateLog: finalUpdateLog 
  })
}

defineExpose({ setSaving: (val) => { saving.value = val } })
</script>

<style scoped>
.upload-section { width: 100%; }
.upload-type-selector { margin-bottom: 16px; }
.upload-area { width: 100%; }
.other-type-warning { margin-bottom: 16px; }
.el-form-item { margin-bottom: 18px; }
</style>
