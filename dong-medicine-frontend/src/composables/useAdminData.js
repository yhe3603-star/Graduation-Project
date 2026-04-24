import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logFetchError, logOperationWarn } from '@/utils'

function parsePageResponse(res) {
  const raw = res?.data ?? res
  const data = raw?.data ?? raw
  if (data && typeof data === 'object' && !Array.isArray(data) && Array.isArray(data.records)) {
    return {
      records: data.records,
      total: Number(data.total) || 0,
      page: data.page ?? 1,
      size: data.size ?? 20
    }
  }
  const arr = Array.isArray(data) ? data : []
  return { records: arr, total: arr.length, page: 1, size: arr.length }
}

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
  const adminStats = ref(null)
  const activeSection = ref('dashboard')
  const loadedSections = new Set()

  const pagination = ref({
    users: { page: 1, size: 20, total: 0 },
    knowledge: { page: 1, size: 20, total: 0 },
    inheritors: { page: 1, size: 20, total: 0 },
    plants: { page: 1, size: 20, total: 0 },
    qa: { page: 1, size: 20, total: 0 },
    resources: { page: 1, size: 20, total: 0 },
    feedback: { page: 1, size: 20, total: 0 },
    comments: { page: 1, size: 20, total: 0 },
    quiz: { page: 1, size: 20, total: 0 },
    logs: { page: 1, size: 500, total: 0 }
  })

  const SECTIONS = {
    users: { ref: users, path: '/admin/users' },
    knowledge: { ref: knowledgeList, path: '/admin/knowledge' },
    inheritors: { ref: inheritorsList, path: '/admin/inheritors' },
    plants: { ref: plantsList, path: '/admin/plants' },
    qa: { ref: qaList, path: '/admin/qa' },
    resources: { ref: resourcesList, path: '/admin/resources' },
    feedback: { ref: feedbackList, path: '/admin/feedback', extra: 'status=all' },
    comments: { ref: commentsList, path: '/admin/comments', extra: 'status=all' },
    quiz: { ref: quizList, path: '/quiz/list' },
    logs: { ref: logList, path: '/admin/logs/list', extra: 'limit=500', raw: true }
  }

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

  async function fetchStats() {
    const res = await request.get('/admin/stats').catch(() => ({}))
    const raw = res?.data ?? res
    adminStats.value = raw?.data ?? raw ?? {}
  }

  async function loadSection(key, force = false) {
    if (!force && loadedSections.has(key)) return
    const cfg = SECTIONS[key]
    if (!cfg) return
    loadedSections.add(key)

    if (cfg.raw) {
      const qs = cfg.extra || ''
      const res = await request.get(`${cfg.path}?${qs}`).catch(() => ({}))
      const raw = res?.data?.data ?? res?.data ?? res
      cfg.ref.value = Array.isArray(raw) ? raw : []
      return
    }

    const p = pagination.value[key]
    let qs = `page=${p.page}&size=${p.size}`
    if (cfg.extra) qs += `&${cfg.extra}`
    const res = await request.get(`${cfg.path}?${qs}`).catch(() => ({}))
    const parsed = parsePageResponse(res)
    pagination.value[key] = {
      ...pagination.value[key],
      total: parsed.total,
      page: parsed.page,
      size: parsed.size
    }
    cfg.ref.value = parsed.records
  }

  async function handleAdminPage(key, page) {
    pagination.value[key] = { ...pagination.value[key], page }
    await loadSection(key, true)
  }

  async function handleAdminSize(key, size) {
    pagination.value[key] = { ...pagination.value[key], page: 1, size }
    await loadSection(key, true)
  }

  const fetchData = async () => {
    try {
      await fetchStats()
      if (activeSection.value !== 'dashboard') {
        await loadSection(activeSection.value, true)
      }
    } catch (e) {
      logFetchError('管理后台数据', e)
      ElMessage.error('数据加载失败')
    }
  }

  const switchSection = async (sectionKey) => {
    activeSection.value = sectionKey
    await loadSection(sectionKey)
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
    adminStats,
    activeSection,
    pagination,
    sortedComments,
    sortedFeedback,
    sortedUsers,
    fetchData,
    switchSection,
    handleAdminPage,
    handleAdminSize
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
    } catch (e) {
      if (e !== 'cancel') {
        console.debug('删除操作取消或失败:', e)
      }
    }
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
    } catch (e) {
      logOperationWarn(`保存${type}`)
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
    } catch (e) {
      logOperationWarn('审核评论')
      ElMessage.error('操作失败')
    }
  }

  const rejectComment = async (row) => {
    try {
      await request.put(`/admin/comments/${row.id}/reject`)
      ElMessage.success('已拒绝该评论')
      fetchData()
    } catch (e) {
      logOperationWarn('拒绝评论')
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
    } catch (e) {
      if (e !== 'cancel') {
        console.debug('批量删除日志失败:', e)
      }
    }
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
    } catch (e) {
      if (e !== 'cancel') {
        console.debug('清空日志失败:', e)
      }
    }
  }

  const replyFeedback = async ({ feedback, reply }) => {
    try {
      await request.put(`/admin/feedback/${feedback.id}/reply`, { reply })
      ElMessage.success('回复成功')
      return true
    } catch (e) {
      logOperationWarn('回复反馈')
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
