import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useAdminData(request) {
  const users = ref([])
  const knowledgeList = ref([])
  const inheritorsList = ref([])
  const plantsList = ref([])
  const qaList = ref([])
  const resourcesList = ref([])
  const feedbackList = ref([])
  const quizList = ref([])
  const commentsList = ref([])
  const logList = ref([])

  const sortedComments = computed(() => {
    const list = commentsList.value || []
    return [
      ...list.filter(c => c.status === 'pending').sort((a, b) => a.id - b.id),
      ...list.filter(c => c.status !== 'pending').sort((a, b) => a.id - b.id)
    ]
  })

  const sortedFeedback = computed(() => {
    const list = feedbackList.value || []
    return [
      ...list.filter(f => f.status === 'pending' || f.status === 'processing').sort((a, b) => a.id - b.id),
      ...list.filter(f => f.status === 'resolved').sort((a, b) => a.id - b.id)
    ]
  })

  const sortedUsers = computed(() => [...(users.value || [])].sort((a, b) => a.id - b.id))

  const fetchData = async () => {
    try {
      const [uRes, kRes, iRes, pRes, qRes, rRes, fRes, quizRes, cRes, logRes] = await Promise.all([
        request.get('/admin/users').catch(() => ({})),
        request.get('/admin/knowledge').catch(() => ({})),
        request.get('/admin/inheritors').catch(() => ({})),
        request.get('/admin/plants').catch(() => ({})),
        request.get('/admin/qa').catch(() => ({})),
        request.get('/admin/resources').catch(() => ({})),
        request.get('/admin/feedback').catch(() => ({})),
        request.get('/quiz/list').catch(() => ({})),
        request.get('/admin/comments').catch(() => ({})),
        request.get('/admin/logs/list').catch(() => ({}))
      ])
      
      const extractData = (res) => {
    const data = res?.data?.data || res?.data || []
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      return data.records || []
    }
    return Array.isArray(data) ? data : []
  }
      
      users.value = extractData(uRes)
      knowledgeList.value = extractData(kRes)
      inheritorsList.value = extractData(iRes)
      plantsList.value = extractData(pRes)
      qaList.value = extractData(qRes)
      resourcesList.value = extractData(rRes)
      feedbackList.value = extractData(fRes)
      quizList.value = extractData(quizRes)
      commentsList.value = extractData(cRes)
      logList.value = extractData(logRes)
    } catch {
      ElMessage.error('数据加载失败')
    }
  }

  return {
    users,
    knowledgeList,
    inheritorsList,
    plantsList,
    qaList,
    resourcesList,
    feedbackList,
    quizList,
    commentsList,
    logList,
    sortedComments,
    sortedFeedback,
    sortedUsers,
    fetchData
  }
}

export function useAdminDialogs() {
  const dialogVisible = ref({
    knowledge: false,
    inheritor: false,
    plant: false,
    qa: false,
    resource: false,
    quiz: false
  })

  const detailVisible = ref({
    user: false,
    knowledge: false,
    inheritor: false,
    plant: false,
    qa: false,
    resource: false,
    quiz: false
  })

  const currentDetail = ref(null)
  const formData = ref({
    knowledge: null,
    inheritor: null,
    plant: null,
    qa: null,
    resource: null,
    quiz: null
  })

  const commentDetailVisible = ref(false)
  const currentComment = ref(null)
  const feedbackDetailVisible = ref(false)
  const currentFeedback = ref(null)
  const logDetailVisible = ref(false)
  const currentLog = ref(null)

  const openDialog = (type) => {
    formData.value[type] = null
    dialogVisible.value[type] = true
  }

  const viewDetail = (type, row) => {
    currentDetail.value = row
    detailVisible.value[type] = true
  }

  const editItem = (type, row, extraData = {}) => {
    formData.value[type] = { ...row, ...extraData }
    dialogVisible.value[type] = true
  }

  return {
    dialogVisible,
    detailVisible,
    currentDetail,
    formData,
    commentDetailVisible,
    currentComment,
    feedbackDetailVisible,
    currentFeedback,
    logDetailVisible,
    currentLog,
    openDialog,
    viewDetail,
    editItem
  }
}

export function useAdminActions(request, fetchData) {
  const selectedLogs = ref([])

  const confirmDelete = async (msg, fn) => {
    try {
      await ElMessageBox.confirm(msg, '提示', { type: 'warning' })
      await fn()
      ElMessage.success('删除成功')
      fetchData()
    } catch {}
  }

  const saveItem = async (type, data, endpoints) => {
    try {
      if (data.id) {
        await request.put(`${endpoints.update}/${data.id}`, data)
      } else {
        await request.post(endpoints.create, data)
      }
      ElMessage.success('保存成功')
      fetchData()
      return true
    } catch {
      ElMessage.error('保存失败')
      return false
    }
  }

  const deleteItem = (type, id, endpoint) => {
    return confirmDelete('确定删除？', () => request.delete(`${endpoint}/${id}`))
  }

  const approveComment = async (row) => {
    try {
      await request.put(`/admin/comments/${row.id}/approve`)
      ElMessage.success('审核通过')
      fetchData()
    } catch {
      ElMessage.error('操作失败')
    }
  }

  const rejectComment = async (row) => {
    try {
      await request.put(`/admin/comments/${row.id}/reject`)
      ElMessage.success('已拒绝该评论')
      fetchData()
    } catch {
      ElMessage.error('操作失败')
    }
  }

  const handleLogSelectionChange = (selection) => {
    selectedLogs.value = selection
  }

  const batchDeleteLogs = async () => {
    if (selectedLogs.value.length === 0) return
    try {
      await ElMessageBox.confirm(`确定删除选中的 ${selectedLogs.value.length} 条日志？`, '提示', { type: 'warning' })
      await request.delete('/admin/logs/batch', { data: selectedLogs.value.map(log => log.id) })
      ElMessage.success('批量删除成功')
      selectedLogs.value = []
      fetchData()
    } catch {}
  }

  const clearAllLogs = async () => {
    try {
      await ElMessageBox.confirm('确定清空所有日志？此操作不可恢复！', '警告', {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消'
      })
      await request.delete('/admin/logs/clear')
      ElMessage.success('清空成功')
      selectedLogs.value = []
      fetchData()
    } catch {}
  }

  const replyFeedback = async ({ feedback, reply }) => {
    try {
      await request.put(`/admin/feedback/${feedback.id}/reply`, { reply })
      ElMessage.success('回复成功')
      return true
    } catch {
      ElMessage.error('回复失败')
      return false
    }
  }

  return {
    selectedLogs,
    confirmDelete,
    saveItem,
    deleteItem,
    approveComment,
    rejectComment,
    handleLogSelectionChange,
    batchDeleteLogs,
    clearAllLogs,
    replyFeedback
  }
}
