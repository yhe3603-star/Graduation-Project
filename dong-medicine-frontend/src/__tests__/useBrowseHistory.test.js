import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockRouter = { push: vi.fn() }
vi.mock('vue-router', () => ({
  useRouter: () => mockRouter
}))

vi.mock('@/utils/request', () => ({
  default: { get: vi.fn() }
}))

vi.mock('@element-plus/icons-vue', () => ({
  Document: { name: 'Document' },
  User: { name: 'User' },
  Star: { name: 'Star' },
  EditPen: { name: 'EditPen' },
  Cherry: { name: 'Cherry' }
}))

import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useBrowseHistory } from '@/composables/useBrowseHistory'

describe('useBrowseHistory', () => {
  let router

  beforeEach(() => {
    vi.clearAllMocks()
    mockRouter.push.mockClear()
    router = useRouter()
  })

  describe('loadBrowseHistory', () => {
    it('should populate browseHistory from res.data', async () => {
      const fakeHistory = [
        { id: 1, targetType: 'plant', targetId: 10 },
        { id: 2, targetType: 'knowledge', targetId: 20 }
      ]
      request.get.mockResolvedValue({ data: fakeHistory })

      const { browseHistory, historyLoading, loadBrowseHistory } = useBrowseHistory()
      await loadBrowseHistory()
      expect(request.get).toHaveBeenCalledWith('/browse-history/my')
      expect(browseHistory.value).toEqual(fakeHistory)
      expect(historyLoading.value).toBe(false)
    })

    it('should fall back to res when res.data is falsy', async () => {
      const fakeHistory = [{ id: 1, targetType: 'plant', targetId: 1 }]
      request.get.mockResolvedValue(fakeHistory)

      const { browseHistory, loadBrowseHistory } = useBrowseHistory()
      await loadBrowseHistory()
      expect(browseHistory.value).toEqual(fakeHistory)
    })

    it('should set historyLoading to true during request', async () => {
      let resolveRequest
      request.get.mockReturnValue(new Promise(r => { resolveRequest = r }))

      const { historyLoading, loadBrowseHistory } = useBrowseHistory()
      const promise = loadBrowseHistory()
      expect(historyLoading.value).toBe(true)

      resolveRequest({ data: [] })
      await promise
      expect(historyLoading.value).toBe(false)
    })

    it('should set empty array on error', async () => {
      request.get.mockRejectedValue(new Error('network'))

      const { browseHistory, loadBrowseHistory } = useBrowseHistory()
      await loadBrowseHistory()
      expect(browseHistory.value).toEqual([])
    })

    it('should set historyLoading to false after error', async () => {
      request.get.mockRejectedValue(new Error('fail'))

      const { historyLoading, loadBrowseHistory } = useBrowseHistory()
      await loadBrowseHistory()
      expect(historyLoading.value).toBe(false)
    })
  })

  describe('pagination', () => {
    it('should paginate browseHistory by page and pageSize', async () => {
      const items = Array.from({ length: 25 }, (_, i) => ({
        id: i + 1, targetType: 'plant', targetId: i + 1
      }))
      request.get.mockResolvedValue({ data: items })

      const { browseHistory, paginatedHistory, historyPage, historyPageSize, loadBrowseHistory } = useBrowseHistory()
      await loadBrowseHistory()

      expect(historyPageSize.value).toBe(10)
      expect(paginatedHistory.value).toHaveLength(10)
      expect(paginatedHistory.value[0].id).toBe(1)

      historyPage.value = 2
      expect(paginatedHistory.value).toHaveLength(10)
      expect(paginatedHistory.value[0].id).toBe(11)

      historyPage.value = 3
      expect(paginatedHistory.value).toHaveLength(5)
      expect(paginatedHistory.value[0].id).toBe(21)
    })

    it('should return empty array when page exceeds data', async () => {
      request.get.mockResolvedValue({ data: [{ id: 1, targetType: 'plant', targetId: 1 }] })

      const { paginatedHistory, historyPage, loadBrowseHistory } = useBrowseHistory()
      await loadBrowseHistory()

      historyPage.value = 5
      expect(paginatedHistory.value).toHaveLength(0)
    })

    it('should use default pageSize of 10', () => {
      const { historyPageSize } = useBrowseHistory()
      expect(historyPageSize.value).toBe(10)
    })
  })

  describe('getHistoryIcon', () => {
    it('should return correct icon for each known type', () => {
      const { getHistoryIcon } = useBrowseHistory()
      const plantIcon = getHistoryIcon('plant')
      const knowledgeIcon = getHistoryIcon('knowledge')
      const inheritorIcon = getHistoryIcon('inheritor')
      const resourceIcon = getHistoryIcon('resource')
      const qaIcon = getHistoryIcon('qa')

      // Icons are truthy objects from our mock
      expect(plantIcon).toBeTruthy()
      expect(knowledgeIcon).toBeTruthy()
      expect(inheritorIcon).toBeTruthy()
      expect(resourceIcon).toBeTruthy()
      expect(qaIcon).toBeTruthy()

      // Each type returns a distinct icon
      expect(plantIcon).not.toBe(knowledgeIcon)
    })

    it('should return knowledge icon as default for unknown type', () => {
      const { getHistoryIcon } = useBrowseHistory()
      const unknownIcon = getHistoryIcon('unknown')
      const knowledgeIcon = getHistoryIcon('knowledge')
      expect(unknownIcon).toBe(knowledgeIcon)
    })
  })

  describe('goToHistoryItem', () => {
    it('should navigate to plant detail route', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'plant', targetId: 42 })
      expect(router.push).toHaveBeenCalledWith('/plants?id=42')
    })

    it('should navigate to knowledge detail route', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'knowledge', targetId: 7 })
      expect(router.push).toHaveBeenCalledWith('/knowledge?id=7')
    })

    it('should navigate to inheritor detail route', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'inheritor', targetId: 3 })
      expect(router.push).toHaveBeenCalledWith('/inheritors?id=3')
    })

    it('should navigate to resource detail route', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'resource', targetId: 5 })
      expect(router.push).toHaveBeenCalledWith('/resources?id=5')
    })

    it('should navigate to qa detail route', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'qa', targetId: 8 })
      expect(router.push).toHaveBeenCalledWith('/qa?id=8')
    })

    it('should not navigate for unknown targetType', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'unknown', targetId: 1 })
      expect(router.push).not.toHaveBeenCalled()
    })

    it('should fall back to item.id when targetId is missing', () => {
      const { goToHistoryItem } = useBrowseHistory()
      goToHistoryItem({ targetType: 'plant', id: 99 })
      expect(router.push).toHaveBeenCalledWith('/plants?id=99')
    })
  })
})
