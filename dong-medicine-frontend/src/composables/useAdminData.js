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

export function useAdminSection(request, path, options = {}) {
  const { pageSize = 20, extra = '', raw = false } = options
  const list = ref([])
  const pagination = ref({ page: 1, size: pageSize, total: 0 })
  const loaded = ref(false)

  const load = async (force = false) => {
    if (!force && loaded.value) return
    loaded.value = true

    if (raw) {
      const qs = extra || ''
      const res = await request.get(`${path}?${qs}`).catch(() => ({}))
      const rawData = res?.data?.data ?? res?.data ?? res
      list.value = Array.isArray(rawData) ? rawData : []
      return
    }

    const p = pagination.value
    let qs = `page=${p.page}&size=${p.size}`
    if (extra) qs += `&${extra}`
    const res = await request.get(`${path}?${qs}`).catch(() => ({}))
    const parsed = parsePageResponse(res)
    pagination.value = { ...pagination.value, total: parsed.total, page: parsed.page, size: parsed.size }
    list.value = parsed.records
  }

  const setPage = async (page) => {
    pagination.value = { ...pagination.value, page }
    await load(true)
  }

  const setSize = async (size) => {
    pagination.value = { ...pagination.value, page: 1, size }
    await load(true)
  }

  return { list, pagination, loaded, load, setPage, setSize }
}

export function useAdminData(request) {
  const usersSection = useAdminSection(request, '/admin/users')
  const knowledgeSection = useAdminSection(request, '/admin/knowledge')
  const inheritorsSection = useAdminSection(request, '/admin/inheritors')
  const plantsSection = useAdminSection(request, '/admin/plants')
  const qaSection = useAdminSection(request, '/admin/qa')
  const resourcesSection = useAdminSection(request, '/admin/resources')
  const feedbackSection = useAdminSection(request, '/admin/feedback', { extra: 'status=all' })
  const commentsSection = useAdminSection(request, '/admin/comments', { extra: 'status=all' })
  const quizSection = useAdminSection(request, '/quiz/list')
  const logsSection = useAdminSection(request, '/admin/logs/list', { pageSize: 500, extra: 'limit=500', raw: true })

  const sections = {
    users: usersSection,
    knowledge: knowledgeSection,
    inheritors: inheritorsSection,
    plants: plantsSection,
    qa: qaSection,
    resources: resourcesSection,
    feedback: feedbackSection,
    comments: commentsSection,
    quiz: quizSection,
    logs: logsSection
  }

  const adminStats = ref(null)
  const activeSection = ref('dashboard')

  const users = computed(() => usersSection.list.value)
  const knowledgeList = computed(() => knowledgeSection.list.value)
  const inheritorsList = computed(() => inheritorsSection.list.value)
  const plantsList = computed(() => plantsSection.list.value)
  const qaList = computed(() => qaSection.list.value)
  const resourcesList = computed(() => resourcesSection.list.value)
  const feedbackList = computed(() => feedbackSection.list.value)
  const quizList = computed(() => quizSection.list.value)
  const commentsList = computed(() => commentsSection.list.value)
  const logList = computed(() => logsSection.list.value)

  const pagination = computed(() => {
    const result = {}
    for (const [key, section] of Object.entries(sections)) {
      result[key] = section.pagination.value
    }
    return result
  })

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
    const section = sections[key]
    if (section) await section.load(force)
  }

  async function handleAdminPage(key, page) {
    const section = sections[key]
    if (section) await section.setPage(page)
  }

  async function handleAdminSize(key, size) {
    const section = sections[key]
    if (section) await section.setSize(size)
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
    loadSection,
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
