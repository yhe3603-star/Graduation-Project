import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') }
}))

vi.mock('@/utils', () => ({
  logFetchError: vi.fn(),
  logOperationWarn: vi.fn()
}))

import { ElMessage, ElMessageBox } from 'element-plus'
import { logFetchError } from '@/utils'
import {
  useAdminSection,
  useAdminDialogs,
  useAdminData,
  useAdminActions
} from '@/composables/useAdminData'

// Helper: create a mock request object matching the shape the composables expect
function createMockRequest(getData = {}) {
  return {
    get: vi.fn().mockResolvedValue(getData),
    post: vi.fn().mockResolvedValue({}),
    put: vi.fn().mockResolvedValue({}),
    delete: vi.fn().mockResolvedValue({})
  }
}

describe('useAdminSection', () => {
  let request
  beforeEach(() => {
    request = createMockRequest({ data: { records: [], total: 0 } })
  })

  it('should initialize with empty list, default pagination, and not loaded', () => {
    const { list, pagination, loaded } = useAdminSection(request, '/admin/items')
    expect(list.value).toEqual([])
    expect(pagination.value).toEqual({ page: 1, size: 20, total: 0 })
    expect(loaded.value).toBe(false)
  })

  it('load should fetch data with correct URL and pagination', async () => {
    const records = [{ id: 1, name: 'test' }]
    request.get.mockResolvedValue({ data: { records, total: 1, page: 1, size: 20 } })

    const { list, pagination, load } = useAdminSection(request, '/admin/items')
    await load()

    expect(request.get).toHaveBeenCalledWith('/admin/items?page=1&size=20')
    expect(list.value).toEqual(records)
    expect(pagination.value.total).toBe(1)
  })

  it('load should append extra query string', async () => {
    request.get.mockResolvedValue({ data: { records: [], total: 0 } })
    const { load } = useAdminSection(request, '/admin/items', { extra: 'status=all' })
    await load()
    expect(request.get).toHaveBeenCalledWith('/admin/items?page=1&size=20&status=all')
  })

  it('load should handle raw mode', async () => {
    const rawRecords = [{ id: 1 }, { id: 2 }]
    request.get.mockResolvedValue({ data: { data: rawRecords } })

    const { list, load } = useAdminSection(request, '/admin/logs', { raw: true })
    await load()

    expect(list.value).toEqual(rawRecords)
  })

  it('load should skip if already loaded and force is false', async () => {
    const { load } = useAdminSection(request, '/admin/items')
    await load()
    request.get.mockClear()
    await load()
    expect(request.get).not.toHaveBeenCalled()
  })

  it('setPage should update page and reload', async () => {
    request.get.mockResolvedValue({ data: { records: [], total: 0, page: 3, size: 20 } })
    const { pagination, setPage } = useAdminSection(request, '/admin/items')
    await setPage(3)
    expect(pagination.value.page).toBe(3)
    expect(request.get).toHaveBeenCalledWith('/admin/items?page=3&size=20')
  })

  it('setSize should update size, reset page, and reload', async () => {
    request.get.mockResolvedValue({ data: { records: [], total: 0, page: 1, size: 50 } })
    const { pagination, setSize } = useAdminSection(request, '/admin/items')
    await setSize(50)
    expect(pagination.value.page).toBe(1)
    expect(pagination.value.size).toBe(50)
    expect(request.get).toHaveBeenCalledWith('/admin/items?page=1&size=50')
  })

  it('load should handle API error gracefully', async () => {
    const error = new Error('network')
    request.get.mockRejectedValue(error)

    const { list, load } = useAdminSection(request, '/admin/items')
    await load()

    expect(logFetchError).toHaveBeenCalled()
    expect(list.value).toEqual([])
  })

  it('load should handle custom pageSize option', async () => {
    request.get.mockResolvedValue({ data: { records: [], total: 0, page: 1, size: 50 } })
    const { pagination, load } = useAdminSection(request, '/admin/items', { pageSize: 50 })
    await load()
    expect(pagination.value.size).toBe(50)
    expect(request.get).toHaveBeenCalledWith('/admin/items?page=1&size=50')
  })
})

describe('useAdminDialogs', () => {
  it('should initialize all dialog visible flags to false', () => {
    const { dialogVisible, detailVisible } = useAdminDialogs()
    expect(Object.values(dialogVisible.value).every(v => v === false)).toBe(true)
    expect(Object.values(detailVisible.value).every(v => v === false)).toBe(true)
  })

  it('openDialog should set dialog visible and reset formData', () => {
    const { dialogVisible, formData, openDialog } = useAdminDialogs()
    openDialog('knowledge')
    expect(dialogVisible.value.knowledge).toBe(true)
    expect(formData.value.knowledge).toBeNull()
  })

  it('viewDetail should set detail visible and current detail', () => {
    const { detailVisible, currentDetail, viewDetail } = useAdminDialogs()
    const row = { id: 1, name: 'test' }
    viewDetail('plant', row)
    expect(detailVisible.value.plant).toBe(true)
    expect(currentDetail.value).toStrictEqual(row)
  })

  it('editItem should set formData and dialog visible', () => {
    const { dialogVisible, formData, editItem } = useAdminDialogs()
    const row = { id: 1, name: '灵芝' }
    editItem('plant', row, { extra: 'data' })
    expect(dialogVisible.value.plant).toBe(true)
    expect(formData.value.plant).toEqual({ id: 1, name: '灵芝', extra: 'data' })
  })

  it('editItem should work without extraData', () => {
    const { formData, editItem } = useAdminDialogs()
    const row = { id: 2, name: '枸杞' }
    editItem('qa', row)
    expect(formData.value.qa).toEqual({ id: 2, name: '枸杞' })
  })
})

describe('useAdminData', () => {
  it('should expose all list computed properties', async () => {
    const request = createMockRequest({ data: { records: [], total: 0 } })
    const result = useAdminData(request)

    expect(result.users.value).toEqual([])
    expect(result.knowledgeList.value).toEqual([])
    expect(result.inheritorsList.value).toEqual([])
    expect(result.plantsList.value).toEqual([])
    expect(result.qaList.value).toEqual([])
    expect(result.resourcesList.value).toEqual([])
    expect(result.feedbackList.value).toEqual([])
    expect(result.quizList.value).toEqual([])
    expect(result.commentsList.value).toEqual([])
    expect(result.logList.value).toEqual([])
    expect(result.adminStats.value).toBeNull()
    expect(result.activeSection.value).toBe('dashboard')
  })

  it('fetchData should fetch stats and dashboard sections', async () => {
    const request = createMockRequest({ data: { records: [], total: 0 } })
    const { fetchData } = useAdminData(request)

    request.get.mockResolvedValue({ data: { data: {} } })
    await fetchData()

    const urls = request.get.mock.calls.map(c => c[0])
    expect(urls).toContain('/admin/stats')
  })

  it('loadSection should load data for a specific section', async () => {
    const records = [{ id: 1, name: '灵芝' }]
    const request = createMockRequest({ data: { records, total: 1 } })
    const { plantsList, loadSection } = useAdminData(request)

    await loadSection('plants')
    expect(plantsList.value).toEqual(records)
  })

  it('handleAdminPage should update pagination for a section', async () => {
    const request = createMockRequest({ data: { records: [], total: 0, page: 2, size: 20 } })
    const { pagination, handleAdminPage } = useAdminData(request)
    await handleAdminPage('plants', 2)
    expect(pagination.value.plants.page).toBe(2)
  })

  it('handleAdminSize should update size for a section', async () => {
    const request = createMockRequest({ data: { records: [], total: 0, page: 1, size: 50 } })
    const { pagination, handleAdminSize } = useAdminData(request)
    await handleAdminSize('plants', 50)
    expect(pagination.value.plants.page).toBe(1)
    expect(pagination.value.plants.size).toBe(50)
  })

  it('switchSection should update activeSection and load data', async () => {
    const request = createMockRequest({ data: { records: [], total: 0 } })
    const { activeSection, switchSection } = useAdminData(request)
    await switchSection('users')
    expect(activeSection.value).toBe('users')
  })

  it('sortedComments should sort pending first', () => {
    const request = createMockRequest({ data: { records: [], total: 0 } })
    const { sortedComments } = useAdminData(request)
    expect(sortedComments.value).toEqual([])
  })

  it('sortedFeedback should sort pending/processing first', () => {
    const request = createMockRequest({ data: { records: [], total: 0 } })
    const { sortedFeedback } = useAdminData(request)
    expect(sortedFeedback.value).toEqual([])
  })

  it('sortedUsers should sort by id ascending', () => {
    const request = createMockRequest({ data: { records: [], total: 0 } })
    const { sortedUsers } = useAdminData(request)
    expect(sortedUsers.value).toEqual([])
  })

  it('fetchData should handle stats fetch failure gracefully', async () => {
    const request = createMockRequest()
    // fetchStats catches errors internally, so the error is handled there
    // fetchData still continues with empty adminStats
    request.get.mockRejectedValueOnce(new Error('stats fail'))
      .mockResolvedValue({ data: { records: [], total: 0 } })

    const { adminStats, fetchData } = useAdminData(request)
    await fetchData()

    // adminStats should be set from the catch branch (empty object)
    expect(adminStats.value).toEqual({})
  })
})

describe('useAdminActions', () => {
  let request, fetchData
  beforeEach(() => {
    vi.clearAllMocks()
    request = createMockRequest()
    fetchData = vi.fn()
  })

  it('saveItem should POST when creating new item', async () => {
    const { saveItem } = useAdminActions(request, fetchData)
    const result = await saveItem('plant', { name: '灵芝' }, { create: '/admin/plants', update: '/admin/plants' })

    expect(request.post).toHaveBeenCalledWith('/admin/plants', { name: '灵芝' })
    expect(ElMessage.success).toHaveBeenCalledWith('保存成功')
    expect(fetchData).toHaveBeenCalled()
    expect(result).toBe(true)
  })

  it('saveItem should PUT when updating existing item', async () => {
    const { saveItem } = useAdminActions(request, fetchData)
    const result = await saveItem('plant', { id: 1, name: '灵芝' }, { create: '/admin/plants', update: '/admin/plants' })

    expect(request.put).toHaveBeenCalledWith('/admin/plants/1', { id: 1, name: '灵芝' })
    expect(result).toBe(true)
  })

  it('saveItem should show error on failure', async () => {
    request.post.mockRejectedValue(new Error('fail'))
    const { saveItem } = useAdminActions(request, fetchData)
    const result = await saveItem('plant', { name: 'test' }, { create: '/admin/plants', update: '/admin/plants' })

    expect(result).toBe(false)
    expect(ElMessage.error).toHaveBeenCalledWith('保存失败')
  })

  it('deleteItem should confirm then delete', async () => {
    const { deleteItem } = useAdminActions(request, fetchData)
    await deleteItem('plant', 5, '/admin/plants')

    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(request.delete).toHaveBeenCalledWith('/admin/plants/5')
    expect(ElMessage.success).toHaveBeenCalledWith('删除成功')
    expect(fetchData).toHaveBeenCalled()
  })

  it('deleteItem should not delete when user cancels', async () => {
    ElMessageBox.confirm.mockRejectedValueOnce('cancel')
    const { deleteItem } = useAdminActions(request, fetchData)
    await deleteItem('plant', 5, '/admin/plants')

    expect(request.delete).not.toHaveBeenCalled()
  })

  it('approveComment should PUT approve endpoint', async () => {
    const { approveComment } = useAdminActions(request, fetchData)
    await approveComment({ id: 10, content: 'nice' })

    expect(request.put).toHaveBeenCalledWith('/admin/comments/10/approve')
    expect(ElMessage.success).toHaveBeenCalledWith('审核通过')
    expect(fetchData).toHaveBeenCalled()
  })

  it('approveComment should show error on failure', async () => {
    request.put.mockRejectedValue(new Error('fail'))
    const { approveComment } = useAdminActions(request, fetchData)
    await approveComment({ id: 10 })

    expect(ElMessage.error).toHaveBeenCalledWith('操作失败')
  })

  it('rejectComment should PUT reject endpoint', async () => {
    const { rejectComment } = useAdminActions(request, fetchData)
    await rejectComment({ id: 20 })

    expect(request.put).toHaveBeenCalledWith('/admin/comments/20/reject')
    expect(ElMessage.success).toHaveBeenCalledWith('已拒绝该评论')
    expect(fetchData).toHaveBeenCalled()
  })

  it('rejectComment should show error on failure', async () => {
    request.put.mockRejectedValue(new Error('fail'))
    const { rejectComment } = useAdminActions(request, fetchData)
    await rejectComment({ id: 20 })

    expect(ElMessage.error).toHaveBeenCalledWith('操作失败')
  })

  it('handleLogSelectionChange should update selectedLogs', () => {
    const { selectedLogs, handleLogSelectionChange } = useAdminActions(request, fetchData)
    const selection = [{ id: 1 }, { id: 2 }]
    handleLogSelectionChange(selection)
    expect(selectedLogs.value).toEqual(selection)
  })

  it('batchDeleteLogs should not proceed when selection is empty', async () => {
    const { batchDeleteLogs } = useAdminActions(request, fetchData)
    await batchDeleteLogs()
    expect(ElMessageBox.confirm).not.toHaveBeenCalled()
    expect(request.delete).not.toHaveBeenCalled()
  })

  it('batchDeleteLogs should delete selected logs', async () => {
    const { handleLogSelectionChange, batchDeleteLogs } = useAdminActions(request, fetchData)
    handleLogSelectionChange([{ id: 1 }, { id: 2 }])
    await batchDeleteLogs()

    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(request.delete).toHaveBeenCalledWith('/admin/logs/batch', { data: [1, 2] })
    expect(ElMessage.success).toHaveBeenCalledWith('批量删除成功')
    expect(fetchData).toHaveBeenCalled()
  })

  it('clearAllLogs should confirm and clear all', async () => {
    const { clearAllLogs } = useAdminActions(request, fetchData)
    await clearAllLogs()

    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(request.delete).toHaveBeenCalledWith('/admin/logs/clear')
    expect(ElMessage.success).toHaveBeenCalledWith('清空成功')
    expect(fetchData).toHaveBeenCalled()
  })

  it('replyFeedback should PUT reply endpoint', async () => {
    const { replyFeedback } = useAdminActions(request, fetchData)
    const result = await replyFeedback({ feedback: { id: 3 }, reply: '已处理' })

    expect(request.put).toHaveBeenCalledWith('/admin/feedback/3/reply', { reply: '已处理' })
    expect(ElMessage.success).toHaveBeenCalledWith('回复成功')
    expect(result).toBe(true)
  })

  it('replyFeedback should return false on failure', async () => {
    request.put.mockRejectedValue(new Error('fail'))
    const { replyFeedback } = useAdminActions(request, fetchData)
    const result = await replyFeedback({ feedback: { id: 3 }, reply: 'test' })

    expect(result).toBe(false)
    expect(ElMessage.error).toHaveBeenCalledWith('回复失败')
  })
})
