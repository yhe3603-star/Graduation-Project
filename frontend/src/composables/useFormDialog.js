import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUpdateLog } from './useUpdateLog'

export const useFormDialog = (getDefaultForm, options = {}) => {
  const {
    validate = (form) => true,
    autoLogMessages = { create: '新增数据', update: '更新数据' },
    arrayFields = [],
    singleArrayFields = []
  } = options

  const { 
    parseUpdateLog, stringifyUpdateLog, addLog, updateLog, deleteLog, 
    logDialogVisible, editingLog, openLogDialog, closeLogDialog 
  } = useUpdateLog()

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
        try { 
          const parsed = JSON.parse(trimmed)
          return Array.isArray(parsed) ? parsed.filter(Boolean) : [] 
        } catch { return [] }
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

  const initForm = (data) => {
    if (!data) {
      form.value = getDefaultForm()
      return
    }
    
    const formData = { ...data, updateLog: data.updateLog || '' }
    
    arrayFields.forEach(field => {
      formData[field] = parseToArray(data[field])
    })
    
    form.value = formData
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

  const getFormData = () => {
    const autoLog = isEdit.value ? autoLogMessages.update : autoLogMessages.create
    const currentLogs = parseUpdateLog(form.value.updateLog)
    const today = new Date().toISOString().split('T')[0]
    const hasRecentLog = currentLogs.length > 0 && currentLogs[0].time === today

    const finalUpdateLog = hasRecentLog 
      ? form.value.updateLog 
      : stringifyUpdateLog(addLog(form.value.updateLog, autoLog, '管理员'))

    const result = { ...form.value, updateLog: finalUpdateLog }
    
    arrayFields.forEach(field => {
      result[field] = parseToString(form.value[field])
    })

    singleArrayFields.forEach(field => {
      const val = form.value[field]
      result[field] = typeof val === 'string' ? val : (Array.isArray(val) ? (val[0] || '') : '')
    })

    return result
  }

  const handleSave = () => {
    const validation = validate(form.value)
    if (validation !== true) {
      if (validation) ElMessage.warning(validation)
      return false
    }
    return true
  }

  const setSaving = (val) => { saving.value = val }

  return {
    form,
    saving,
    isEdit,
    updateLogs,
    logDialogVisible,
    editingLog,
    initForm,
    handleAddLog,
    handleEditLog,
    handleDeleteLog,
    handleSaveLog,
    getFormData,
    handleSave,
    setSaving
  }
}
