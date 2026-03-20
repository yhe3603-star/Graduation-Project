<template>
  <el-dialog 
    :model-value="visible" 
    :title="isEdit ? '编辑药用植物' : '新增药用植物'" 
    width="900px" 
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form
      :model="form"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="中文名">
            <el-input
              v-model="form.nameCn"
              placeholder="请输入中文名称"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="侗语名">
            <el-input
              v-model="form.nameDong"
              placeholder="请输入侗语名称"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学名">
            <el-input
              v-model="form.scientificName"
              placeholder="请输入拉丁学名"
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
                label="藤本类"
                value="藤本类"
              />
              <el-option
                label="全草类"
                value="全草类"
              />
              <el-option
                label="根茎类"
                value="根茎类"
              />
              <el-option
                label="叶类"
                value="叶类"
              />
              <el-option
                label="花类"
                value="花类"
              />
              <el-option
                label="果实类"
                value="果实类"
              />
              <el-option
                label="其他"
                value="其他"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用法">
            <el-select
              v-model="form.usageWay"
              style="width: 100%"
              placeholder="请选择用法"
            >
              <el-option
                label="内服"
                value="内服"
              />
              <el-option
                label="外用"
                value="外用"
              />
              <el-option
                label="内外兼用"
                value="内外兼用"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="难度">
            <el-select
              v-model="form.difficulty"
              style="width: 100%"
              placeholder="请选择难度"
            >
              <el-option
                label="初级"
                value="beginner"
              />
              <el-option
                label="中级"
                value="intermediate"
              />
              <el-option
                label="高级"
                value="advanced"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="生境">
        <el-input
          v-model="form.habitat"
          placeholder="请输入生长环境"
        />
      </el-form-item>
      
      <el-form-item label="功效">
        <el-input
          v-model="form.efficacy"
          type="textarea"
          :rows="3"
          placeholder="请输入主要功效"
        />
      </el-form-item>
      
      <el-form-item label="故事">
        <el-input
          v-model="form.story"
          type="textarea"
          :rows="2"
          placeholder="请输入相关故事或传说"
        />
      </el-form-item>
      
      <el-form-item label="分布地区">
        <el-input
          v-model="form.distribution"
          placeholder="请输入分布地区"
        />
      </el-form-item>
      
      <el-form-item label="植物图片">
        <ImageUploader
          v-model="form.images"
          category="plants"
          :limit="9"
          :max-size="10"
        />
      </el-form-item>
      
      <el-form-item label="相关视频">
        <VideoUploader
          v-model="form.videos"
          category="plants"
          :limit="3"
          :max-size="100"
        />
      </el-form-item>
      
      <el-form-item label="相关文档">
        <DocumentUploader
          v-model="form.documents"
          category="plants"
          :limit="3"
          :max-size="50"
          drag
          :show-tip="true"
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
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUploader from '@/components/business/upload/ImageUploader.vue'
import VideoUploader from '@/components/business/upload/VideoUploader.vue'
import DocumentUploader from '@/components/business/upload/DocumentUploader.vue'
import UpdateLogCard from '@/components/business/display/UpdateLogCard.vue'
import UpdateLogDialog from '@/components/business/display/UpdateLogDialog.vue'
import { useUpdateLog } from '@/composables/useUpdateLog'

const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'save'])

const { 
  parseUpdateLog, stringifyUpdateLog, addLog, updateLog, deleteLog, 
  logDialogVisible, editingLog, openLogDialog, closeLogDialog, saveLog 
} = useUpdateLog()

const getDefaultForm = () => ({ 
  id: null, nameCn: "", nameDong: "", scientificName: "", category: "", usageWay: "内服", 
  difficulty: "beginner", habitat: "", efficacy: "", story: "", images: [], 
  videos: [], documents: [], distribution: "", updateLog: "" 
})

const form = ref(getDefaultForm())
const saving = ref(false)

const isEdit = computed(() => !!form.value.id)

const updateLogs = computed(() => parseUpdateLog(form.value.updateLog))

const parseToArray = (value) => {
  if (!value) return []
  if (Array.isArray(value)) return value
  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed.startsWith('[')) {
      try { const parsed = JSON.parse(trimmed); return Array.isArray(parsed) ? parsed.filter(Boolean) : [] } catch { return [] }
    }
    return trimmed.split(',').filter(Boolean)
  }
  return []
}

const parseToString = (value) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (Array.isArray(value)) {
    const filtered = value.filter(Boolean)
    return JSON.stringify(filtered)
  }
  return ''
}

watch(() => props.visible, (val) => {
  if (val) {
    form.value = props.data ? { 
      ...props.data,
      images: parseToArray(props.data.images),
      videos: parseToArray(props.data.videos),
      documents: parseToArray(props.data.documents),
      updateLog: props.data.updateLog || ''
    } : getDefaultForm()
  }
})

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
  if (!form.value.nameCn) { ElMessage.warning('请输入中文名称'); return }
  
  const autoLog = isEdit.value ? '更新药用植物信息' : '新增药用植物'
  const currentLogs = parseUpdateLog(form.value.updateLog)
  const hasRecentLog = currentLogs.length > 0 && currentLogs[0].time === new Date().toISOString().split('T')[0]
  
  const finalUpdateLog = hasRecentLog 
    ? form.value.updateLog 
    : stringifyUpdateLog(addLog(form.value.updateLog, autoLog, '管理员'))
  
  emit('save', {
    ...form.value,
    images: parseToString(form.value.images),
    videos: parseToString(form.value.videos),
    documents: parseToString(form.value.documents),
    updateLog: finalUpdateLog
  })
}

defineExpose({ setSaving: (val) => { saving.value = val } })
</script>

<style scoped>
.el-form-item { margin-bottom: 18px; }
</style>
