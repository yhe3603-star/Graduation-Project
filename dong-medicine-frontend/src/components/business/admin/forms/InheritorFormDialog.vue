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
        @click="onSave"
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
import { watch } from 'vue'
import ImageUploader from '@/components/business/upload/ImageUploader.vue'
import VideoUploader from '@/components/business/upload/VideoUploader.vue'
import DocumentUploader from '@/components/business/upload/DocumentUploader.vue'
import UpdateLogCard from '@/components/business/display/UpdateLogCard.vue'
import UpdateLogDialog from '@/components/business/display/UpdateLogDialog.vue'
import { useFormDialog } from '@/composables/useFormDialog'

const props = defineProps({
  visible: { type: Boolean, default: false },
  data: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'save'])

const getDefaultForm = () => ({
  id: null, name: "", level: "", bio: "", specialties: "",
  experienceYears: 0, videos: [], images: [], representativeCases: "",
  honors: "", documents: [], updateLog: ""
})

const {
  form, saving, isEdit, updateLogs,
  logDialogVisible, editingLog,
  initForm, handleAddLog, handleEditLog, handleDeleteLog, handleSaveLog,
  getFormData, handleSave, setSaving
} = useFormDialog(getDefaultForm, {
  validate: (form) => {
    if (!form.name) return '请输入传承人姓名'
    if (!form.level) return '请选择传承人级别'
    return true
  },
  autoLogMessages: { create: '新增传承人', update: '更新传承人信息' },
  arrayFields: ['videos', 'images', 'documents']
})

watch(() => props.visible, (val) => {
  if (val) {
    initForm(props.data)
    if (props.data) {
      form.value.experienceYears = props.data.experienceYears || 0
    }
  }
})

const onSave = () => {
  if (handleSave()) {
    emit('save', getFormData())
  }
}

defineExpose({ setSaving })
</script>

<style scoped>
.el-form-item { margin-bottom: 18px; }
</style>
