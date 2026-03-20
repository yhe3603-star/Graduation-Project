import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export function useUpdateLog() {
  const newLogContent = ref('')
  const logDialogVisible = ref(false)
  const editingLog = ref(null)

  const parseUpdateLog = (logData) => {
    if (!logData) return []
    if (Array.isArray(logData)) return logData
    if (typeof logData === 'string') {
      const trimmed = logData.trim()
      if (trimmed.startsWith('[')) {
        try {
          const parsed = JSON.parse(trimmed)
          return Array.isArray(parsed) ? parsed : []
        } catch {
          return []
        }
      }
      if (trimmed) {
        return [{ time: new Date().toISOString().split('T')[0], content: trimmed, operator: '系统' }]
      }
    }
    return []
  }

  const stringifyUpdateLog = (logs) => {
    if (!logs || !Array.isArray(logs)) return '[]'
    return JSON.stringify(logs.filter(Boolean))
  }

  const addLog = (existingLogs, content, operator = '管理员') => {
    const logs = parseUpdateLog(existingLogs)
    const newLog = {
      id: Date.now(),
      time: new Date().toISOString().split('T')[0],
      content,
      operator
    }
    return [newLog, ...logs]
  }

  const updateLog = (existingLogs, logId, newContent) => {
    const logs = parseUpdateLog(existingLogs)
    const index = logs.findIndex(l => l.id === logId || l.time === logId)
    if (index > -1) {
      logs[index] = { ...logs[index], content: newContent }
    }
    return logs
  }

  const deleteLog = (existingLogs, logId) => {
    const logs = parseUpdateLog(existingLogs)
    return logs.filter(l => l.id !== logId && l.time !== logId)
  }

  const generateAutoLog = (action, itemName, details = '') => {
    const actionMap = {
      create: `新增${itemName}`,
      update: `更新${itemName}`,
      delete: `删除${itemName}`
    }
    const baseLog = actionMap[action] || `操作${itemName}`
    return details ? `${baseLog}：${details}` : baseLog
  }

  const openLogDialog = (log = null) => {
    editingLog.value = log
    newLogContent.value = log?.content || ''
    logDialogVisible.value = true
  }

  const closeLogDialog = () => {
    editingLog.value = null
    newLogContent.value = ''
    logDialogVisible.value = false
  }

  const saveLog = (existingLogs, operator = '管理员') => {
    if (!newLogContent.value.trim()) {
      ElMessage.warning('请输入日志内容')
      return null
    }
    const logs = parseUpdateLog(existingLogs)
    if (editingLog.value) {
      const index = logs.findIndex(l => l.id === editingLog.value.id)
      if (index > -1) {
        logs[index] = { ...logs[index], content: newLogContent.value.trim() }
      }
    } else {
      const newLog = {
        id: Date.now(),
        time: new Date().toISOString().split('T')[0],
        content: newLogContent.value.trim(),
        operator
      }
      logs.unshift(newLog)
    }
    closeLogDialog()
    return logs
  }

  const formatLogTime = (time) => {
    if (!time) return ''
    if (time.includes('T')) {
      return time.split('T')[0]
    }
    return time
  }

  return {
    newLogContent,
    logDialogVisible,
    editingLog,
    parseUpdateLog,
    stringifyUpdateLog,
    addLog,
    updateLog,
    deleteLog,
    generateAutoLog,
    openLogDialog,
    closeLogDialog,
    saveLog,
    formatLogTime
  }
}

export function useUpdateLogDisplay(logData) {
  const { parseUpdateLog, formatLogTime } = useUpdateLog()
  
  const logs = computed(() => parseUpdateLog(logData?.value || logData))
  
  const recentLogs = computed(() => logs.value.slice(0, 5))
  
  const hasLogs = computed(() => logs.value.length > 0)

  return {
    logs,
    recentLogs,
    hasLogs,
    formatLogTime
  }
}
