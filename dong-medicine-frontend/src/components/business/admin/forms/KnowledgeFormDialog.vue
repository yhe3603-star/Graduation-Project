<template>
  <el-dialog 
    :model-value="visible" 
    :title="isEdit ? '编辑知识条目' : '新增知识条目'" 
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
          <el-form-item label="标题">
            <el-input
              v-model="form.title"
              placeholder="请输入知识标题"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型">
            <el-select
              v-model="form.type"
              style="width: 100%"
              placeholder="请选择类型"
            >
              <el-option
                label="药方"
                value="药方"
              />
              <el-option
                label="疗法"
                value="疗法"
              />
              <el-option
                label="文化"
                value="文化"
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
          <el-form-item label="疗法分类">
            <el-select
              v-model="form.therapyCategory"
              style="width: 100%"
              placeholder="请选择疗法分类"
              clearable
            >
              <el-option
                label="药浴疗法"
                value="药浴疗法"
              />
              <el-option
                label="艾灸疗法"
                value="艾灸疗法"
              />
              <el-option
                label="推拿疗法"
                value="推拿疗法"
              />
              <el-option
                label="针灸疗法"
                value="针灸疗法"
              />
              <el-option
                label="其他疗法"
                value="其他疗法"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="疾病分类">
            <el-select
              v-model="form.diseaseCategory"
              style="width: 100%"
              placeholder="请选择疾病分类"
              clearable
            >
              <el-option
                label="风湿骨痛"
                value="风湿骨痛"
              />
              <el-option
                label="妇科疾病"
                value="妇科疾病"
              />
              <el-option
                label="跌打损伤"
                value="跌打损伤"
              />
              <el-option
                label="消化系统"
                value="消化系统"
              />
              <el-option
                label="呼吸系统"
                value="呼吸系统"
              />
              <el-option
                label="皮肤疾病"
                value="皮肤疾病"
              />
              <el-option
                label="其他"
                value="其他"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="内容">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入知识内容"
        />
      </el-form-item>
      
      <el-form-item label="步骤">
        <el-input
          v-model="form.steps"
          type="textarea"
          :rows="3"
          placeholder="每行一个步骤，如：1. 准备药材 2. 加水煎煮"
        />
      </el-form-item>
      
      <el-form-item label="相关图片">
        <ImageUploader
          v-model="form.images"
          category="knowledge"
          :limit="9"
          :max-size="10"
          list-type="picture-card"
        />
      </el-form-item>
      
      <el-form-item label="演示视频">
        <VideoUploader
          v-model="form.videoUrl"
          category="knowledge"
          :limit="1"
          :max-size="100"
          :multiple="false"
        />
      </el-form-item>
      
      <el-form-item label="相关文档">
        <DocumentUploader
          v-model="form.documents"
          category="knowledge"
          :limit="3"
          :max-size="50"
          :drag="true"
          :show-tip="true"
        />
      </el-form-item>
      
      <el-form-item label="关联植物">
        <el-input
          v-model="form.relatedPlants"
          placeholder="多个植物ID用逗号分隔，如：1,2,3"
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
  id: null, title: "", type: "", therapyCategory: "", diseaseCategory: "", 
  content: "", steps: "", images: [], videoUrl: "", documents: [], 
  relatedPlants: "", updateLog: "" 
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
  if (Array.isArray(value)) return JSON.stringify(value.filter(Boolean))
  return ''
}

watch(() => props.visible, (val) => {
  if (val) {
    form.value = props.data ? { 
      ...props.data,
      images: parseToArray(props.data.images),
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
  if (!form.value.title) { ElMessage.warning('请输入知识标题'); return }
  if (!form.value.type) { ElMessage.warning('请选择类型'); return }
  
  const autoLog = isEdit.value ? '更新知识条目内容' : '新增知识条目'
  const currentLogs = parseUpdateLog(form.value.updateLog)
  const hasRecentLog = currentLogs.length > 0 && currentLogs[0].time === new Date().toISOString().split('T')[0]
  
  const finalUpdateLog = hasRecentLog 
    ? form.value.updateLog 
    : stringifyUpdateLog(addLog(form.value.updateLog, autoLog, '管理员'))
  
  emit('save', {
    ...form.value,
    images: parseToString(form.value.images),
    videoUrl: typeof form.value.videoUrl === 'string' ? form.value.videoUrl : (form.value.videoUrl[0] || ''),
    documents: parseToString(form.value.documents),
    updateLog: finalUpdateLog
  })
}

defineExpose({ setSaving: (val) => { saving.value = val } })
</script>

<style scoped>
.el-form-item { margin-bottom: 18px; }
</style>
