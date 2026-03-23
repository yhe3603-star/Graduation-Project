<template>
  <el-dialog 
    :model-value="visible" 
    :title="isEdit ? '编辑传承人' : '新增传承人'" 
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
          <el-form-item label="姓名">
            <el-input
              v-model="form.name"
              placeholder="请输入传承人姓名"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="级别">
            <el-select
              v-model="form.level"
              style="width: 100%"
              placeholder="请选择级别"
            >
              <el-option
                label="国家级"
                value="国家级"
              />
              <el-option
                label="自治区级"
                value="自治区级"
              />
              <el-option
                label="市级"
                value="市级"
              />
              <el-option
                label="县级"
                value="县级"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="经验年限">
            <el-input-number
              v-model="form.experienceYears"
              :min="0"
              :max="100"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="技艺特色">
            <el-input
              v-model="form.specialties"
              placeholder="如：侗族药浴疗法、侗医推拿"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="简介">
        <el-input
          v-model="form.bio"
          type="textarea"
          :rows="4"
          placeholder="请输入传承人简介"
        />
      </el-form-item>
      
      <el-form-item label="传承人照片">
        <ImageUploader
          v-model="form.images"
          category="inheritors"
          :limit="9"
          :max-size="10"
        />
      </el-form-item>
      
      <el-form-item label="相关视频">
        <VideoUploader
          v-model="form.videos"
          category="inheritors"
          :limit="3"
          :max-size="100"
        />
      </el-form-item>
      
      <el-form-item label="代表案例">
        <el-input
          v-model="form.representativeCases"
          type="textarea"
          :rows="2"
          placeholder="每个案例一行"
        />
      </el-form-item>
      
      <el-form-item label="荣誉资质">
        <el-input
          v-model="form.honors"
          placeholder="多个荣誉用逗号分隔"
        />
      </el-form-item>
      
      <el-form-item label="资质文档">
        <DocumentUploader
          v-model="form.documents"
          category="inheritors"
          :limit="5"
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
  id: null, name: "", level: "", bio: "", specialties: "", 
  experienceYears: 0, videos: [], images: [], representativeCases: "", 
  honors: "", documents: [], updateLog: "" 
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
      experienceYears: props.data.experienceYears || 0,
      videos: parseToArray(props.data.videos),
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
  if (!form.value.name) { ElMessage.warning('请输入传承人姓名'); return }
  if (!form.value.level) { ElMessage.warning('请选择传承人级别'); return }
  
  const autoLog = isEdit.value ? '更新传承人信息' : '新增传承人'
  const currentLogs = parseUpdateLog(form.value.updateLog)
  const hasRecentLog = currentLogs.length > 0 && currentLogs[0].time === new Date().toISOString().split('T')[0]
  
  const finalUpdateLog = hasRecentLog 
    ? form.value.updateLog 
    : stringifyUpdateLog(addLog(form.value.updateLog, autoLog, '管理员'))
  
  emit('save', {
    ...form.value,
    videos: parseToString(form.value.videos),
    images: parseToString(form.value.images),
    documents: parseToString(form.value.documents),
    updateLog: finalUpdateLog
  })
}

defineExpose({ setSaving: (val) => { saving.value = val } })
</script>

<style scoped>
.el-form-item { margin-bottom: 18px; }
</style>
