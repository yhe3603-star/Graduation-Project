import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref, reactive } from 'vue'

vi.mock('@/utils/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), delete: vi.fn() }
}))

vi.mock('@/utils', () => ({
  extractData: vi.fn((res) => res?.data || []),
  logFetchError: vi.fn(),
  logOperationWarn: vi.fn()
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), warning: vi.fn(), error: vi.fn() }
}))

const mockIsLoggedIn = ref(false)
vi.mock('@/stores/user', () => ({
  useUserStore: () => reactive({ isLoggedIn: mockIsLoggedIn })
}))

import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { extractData, logFetchError, logOperationWarn } from '@/utils'
import { useFavorite } from '@/composables/useFavorite'

describe('useFavorite', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockIsLoggedIn.value = false
  })

  describe('loadFavorites', () => {
    it('should not fetch when not logged in', async () => {
      mockIsLoggedIn.value = false
      const { loadFavorites } = useFavorite('plant')
      await loadFavorites()
      expect(request.get).not.toHaveBeenCalled()
    })

    it('should fetch and populate favorites when logged in', async () => {
      mockIsLoggedIn.value = true
      const fakeData = [{ id: 1, targetId: 10, type: 'plant' }]
      extractData.mockReturnValue(fakeData)
      request.get.mockResolvedValue({ data: fakeData })

      const { loadFavorites, favorites } = useFavorite('plant')
      await loadFavorites()
      expect(request.get).toHaveBeenCalledWith('/favorites/my')
      expect(favorites.value).toEqual(fakeData)
    })

    it('should log fetch error on failure', async () => {
      mockIsLoggedIn.value = true
      const error = new Error('network')
      request.get.mockRejectedValue(error)

      const { loadFavorites } = useFavorite('plant')
      await loadFavorites()
      expect(logFetchError).toHaveBeenCalledWith('收藏列表', error)
    })
  })

  describe('isFavorited', () => {
    it('should return false when favorites list is empty', () => {
      const { isFavorited } = useFavorite('plant')
      expect(isFavorited(1)).toBe(false)
    })

    it('should return true when item is favorited with matching type', async () => {
      mockIsLoggedIn.value = true
      extractData.mockReturnValue([{ targetId: 10, type: 'plant' }])
      request.get.mockResolvedValue({ data: [] })

      const { isFavorited, loadFavorites } = useFavorite('plant')
      await loadFavorites()
      expect(isFavorited(10)).toBe(true)
    })

    it('should return false when type does not match', async () => {
      mockIsLoggedIn.value = true
      extractData.mockReturnValue([{ targetId: 10, type: 'knowledge' }])
      request.get.mockResolvedValue({ data: [] })

      const { isFavorited, loadFavorites } = useFavorite('plant')
      await loadFavorites()
      expect(isFavorited(10)).toBe(false)
    })
  })

  describe('toggleFavorite', () => {
    it('should warn when not logged in', async () => {
      mockIsLoggedIn.value = false
      const { toggleFavorite } = useFavorite('plant')
      const result = await toggleFavorite(1, false)
      expect(ElMessage.warning).toHaveBeenCalledWith('请先登录')
      expect(result).toBe(false)
    })

    it('should add favorite (POST) when not already favorited', async () => {
      mockIsLoggedIn.value = true
      request.post.mockResolvedValue({})

      const { toggleFavorite, favorites } = useFavorite('plant')
      const result = await toggleFavorite(5, false)
      expect(request.post).toHaveBeenCalledWith('/favorites/plant/5')
      expect(favorites.value).toContainEqual({ targetId: 5, type: 'plant' })
      expect(ElMessage.success).toHaveBeenCalledWith('收藏成功')
      expect(result).toBe(true)
    })

    it('should remove favorite (DELETE) when already favorited', async () => {
      mockIsLoggedIn.value = true
      request.delete.mockResolvedValue({})

      const { toggleFavorite, favorites, loadFavorites } = useFavorite('plant')
      // First add to favorites via load
      extractData.mockReturnValue([{ targetId: 5, type: 'plant' }])
      request.get.mockResolvedValue({ data: [] })
      await loadFavorites()
      expect(favorites.value).toHaveLength(1)

      const result = await toggleFavorite(5, true)
      expect(request.delete).toHaveBeenCalledWith('/favorites/plant/5')
      expect(favorites.value).toHaveLength(0)
      expect(ElMessage.success).toHaveBeenCalledWith('已取消收藏')
      expect(result).toBe(true)
    })

    it('should handle error during toggle', async () => {
      mockIsLoggedIn.value = true
      request.post.mockRejectedValue(new Error('fail'))

      const { toggleFavorite } = useFavorite('plant')
      const result = await toggleFavorite(1, false)
      expect(logOperationWarn).toHaveBeenCalledWith('收藏操作')
      expect(ElMessage.error).toHaveBeenCalledWith('操作失败')
      expect(result).toBe(false)
    })
  })

  describe('updateItemCount', () => {
    it('should update favoriteCount for matching item', () => {
      const { items, updateItemCount } = useFavorite('plant')
      items.value = [{ id: 1, favoriteCount: 3 }, { id: 2, favoriteCount: 0 }]
      updateItemCount(1, 1)
      expect(items.value[0].favoriteCount).toBe(4)
    })

    it('should decrement favoriteCount but not go below 0', () => {
      const { items, updateItemCount } = useFavorite('plant')
      items.value = [{ id: 1, favoriteCount: 0 }]
      updateItemCount(1, -1)
      expect(items.value[0].favoriteCount).toBe(0)
    })

    it('should not modify items array if id not found', () => {
      const { items, updateItemCount } = useFavorite('plant')
      items.value = [{ id: 1, favoriteCount: 5 }]
      updateItemCount(999, 1)
      expect(items.value[0].favoriteCount).toBe(5)
    })

    it('should default favoriteCount to 0 if undefined', () => {
      const { items, updateItemCount } = useFavorite('plant')
      items.value = [{ id: 1 }]
      updateItemCount(1, 1)
      expect(items.value[0].favoriteCount).toBe(1)
    })
  })

  describe('incrementViewCount', () => {
    it('should call POST and increment viewCount in items', async () => {
      request.post.mockResolvedValue({})
      const { items, incrementViewCount } = useFavorite('plant')
      items.value = [{ id: 10, viewCount: 5 }]

      await incrementViewCount(10)
      expect(request.post).toHaveBeenCalledWith('/plant/10/view')
      expect(items.value[0].viewCount).toBe(6)
    })

    it('should default viewCount to 0 if undefined', async () => {
      request.post.mockResolvedValue({})
      const { items, incrementViewCount } = useFavorite('knowledge')
      items.value = [{ id: 10 }]

      await incrementViewCount(10)
      expect(items.value[0].viewCount).toBe(1)
    })

    it('should handle missing item in items array gracefully', async () => {
      request.post.mockResolvedValue({})
      const { items, incrementViewCount } = useFavorite('plant')
      items.value = []

      await incrementViewCount(999)
      expect(request.post).toHaveBeenCalledWith('/plant/999/view')
    })

    it('should silently catch errors', async () => {
      request.post.mockRejectedValue(new Error('network'))
      const { incrementViewCount } = useFavorite('plant')
      // Should not throw
      await expect(incrementViewCount(1)).resolves.toBeUndefined()
    })
  })

  describe('isFavoritedObject', () => {
    it('should return falsy for null/undefined item', () => {
      const { isFavoritedObject } = useFavorite('plant')
      expect(isFavoritedObject(null)).toBeFalsy()
      expect(isFavoritedObject(undefined)).toBeFalsy()
    })

    it('should delegate to isFavorited with item.id', async () => {
      mockIsLoggedIn.value = true
      extractData.mockReturnValue([{ targetId: 42, type: 'plant' }])
      request.get.mockResolvedValue({ data: [] })

      const { isFavoritedObject, loadFavorites } = useFavorite('plant')
      await loadFavorites()
      expect(isFavoritedObject({ id: 42 })).toBe(true)
      expect(isFavoritedObject({ id: 99 })).toBe(false)
    })
  })
})
