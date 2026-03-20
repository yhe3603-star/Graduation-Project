<template>
  <el-dialog 
    :model-value="visible" 
    :title="editingLog ? '编辑日志' : '添加日志'" 
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form
      :model="form"
      label-width="80px"
    >
      <el-form-item label="日期">
        <el-date-picker 
          v-model="form.time" 
          type="date" 
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="操作人">
        <el-input
          v-model="form.operator"
          placeholder="请输入操作人"
        />
      </el-form-item>
      <el-form-item label="内容">
        <el-input 
          v-model="form.content" 
          type="textarea" 
          :rows="4" 
          placeholder="请输入日志内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="$emit('update:visible', false)">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="handleSave"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  editingLog: { type: Object, default: null },
  defaultOperator: { type: String, default: '管理员' }
})

const emit = defineEmits(['update:visible', 'save'])

const form = ref({
  time: new Date().toISOString().split('T')[0],
  operator: props.defaultOperator,
  content: ''
})

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editingLog) {
      form.value = {
        time: props.editingLog.time || new Date().toISOString().split('T')[0],
        operator: props.editingLog.operator || props.defaultOperator,
        content: props.editingLog.content || ''
      }
    } else {
      form.value = {
        time: new Date().toISOString().split('T')[0],
        operator: props.defaultOperator,
        content: ''
      }
    }
  }
})

const handleClose = () => {
  form.value = {
    time: new Date().toISOString().split('T')[0],
    operator: props.defaultOperator,
    content: ''
  }
}

const handleSave = () => {
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入日志内容')
    return
  }
  emit('save', {
    ...form.value,
    id: props.editingLog?.id || Date.now()
  })
}
</script>
