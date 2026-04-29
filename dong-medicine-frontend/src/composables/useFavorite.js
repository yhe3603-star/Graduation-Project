import { ref, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { extractData, logFetchError, logOperationWarn } from '@/utils'
import { useUserStore } from '@/stores/user'

/**
 * @typedef {Object} UseFavoriteReturn
 * @property {import('vue').Ref<Array>} favorites - 收藏列表
 * @property {import('vue').Ref<Array>} items - 关联的数据列表
 * @property {import('vue').ComputedRef<boolean>} isLoggedIn - 是否已登录
 * @property {function(number): boolean} isFavorited - 判断是否已收藏
 * @property {import('vue').ComputedRef<function>} isFavoritedObject - 判断对象是否已收藏
 * @property {function(): Promise<void>} loadFavorites - 加载收藏列表
 * @property {function(number, boolean): Promise<boolean>} toggleFavorite - 切换收藏状态
 * @property {function(number, number): void} updateItemCount - 更新收藏计数
 * @property {function(number): Promise<void>} incrementViewCount - 增加浏览量
 */

/**
 * 收藏功能组合式函数
 * @param {string} type - 收藏目标类型 (plant|knowledge|inheritor|resource|qa)
 * @returns {UseFavoriteReturn}
 */
export const useFavorite = (type) => {
  const userStore = useUserStore()
  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const favorites = ref([])
  const items = ref([])

  const isFavorited = (id) => favorites.value.some(f => f.targetId === id && f.type === type)

  const isFavoritedObject = (item) => item && isFavorited(item.id)

  const loadFavorites = async () => {
    if (!isLoggedIn.value) return
    try {
      favorites.value = extractData(await request.get('/favorites/my'))
    } catch (e) {
      logFetchError('收藏列表', e)
    }
  }

  const toggleFavorite = async (id, isFav) => {
    if (!isLoggedIn.value) {
      ElMessage.warning('请先登录')
      return false
    }
    try {
      if (isFav) {
        await request.delete(`/favorites/${type}/${id}`)
        favorites.value = favorites.value.filter(f => !(f.targetId === id && f.type === type))
        updateItemCount(id, -1)
        ElMessage.success('已取消收藏')
      } else {
        await request.post(`/favorites/${type}/${id}`)
        favorites.value.push({ targetId: id, type })
        updateItemCount(id, 1)
        ElMessage.success('收藏成功')
      }
      return true
    } catch (e) {
      logOperationWarn('收藏操作')
      ElMessage.error('操作失败')
      return false
    }
  }

  const updateItemCount = (id, delta) => {
    const idx = items.value.findIndex(item => item.id === id)
    if (idx > -1) {
      items.value[idx].favoriteCount = Math.max(0, (items.value[idx].favoriteCount || 0) + delta)
    }
  }

  const incrementViewCount = async (id) => {
    try {
      await request.post(`/${type}/${id}/view`)
      const idx = items.value.findIndex(item => item.id === id)
      if (idx > -1) {
        items.value[idx].viewCount = (items.value[idx].viewCount || 0) + 1
      }
    } catch (e) {
      console.debug('浏览量更新失败:', e)
    }
  }

  return {
    favorites,
    items,
    isLoggedIn,
    isFavorited,
    isFavoritedObject,
    loadFavorites,
    toggleFavorite,
    updateItemCount,
    incrementViewCount
  }
}
