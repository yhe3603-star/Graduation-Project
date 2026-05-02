import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { logFetchError } from '@/utils/logger'

export function useCountdown(durationMinutes = 3) {
  const totalSeconds = ref(durationMinutes * 60)
  const isRunning = ref(false)
  const isExpired = ref(false)
  let timer = null

  const remainingSeconds = computed(() => totalSeconds.value)
  
  const formattedTime = computed(() => {
    const minutes = Math.floor(totalSeconds.value / 60)
    const seconds = totalSeconds.value % 60
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  })

  const isLowTime = computed(() => totalSeconds.value <= 10 && totalSeconds.value > 0)

  const start = () => {
    if (isRunning.value) return
    isRunning.value = true
    isExpired.value = false
    timer = setInterval(() => {
      if (totalSeconds.value > 0) {
        totalSeconds.value--
      } else {
        stop()
        isExpired.value = true
      }
    }, 1000)
  }

  const stop = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    isRunning.value = false
  }

  const reset = (newDurationMinutes = durationMinutes) => {
    stop()
    totalSeconds.value = newDurationMinutes * 60
    isExpired.value = false
  }

  const forceExpire = () => {
    stop()
    isExpired.value = true
    totalSeconds.value = 0
  }

  onUnmounted(() => stop())

  return { remainingSeconds, formattedTime, isRunning, isExpired, isLowTime, start, stop, reset, forceExpire }
}

export function useComments(request, isLoggedIn) {
  const comments = ref([])
  const commentLoading = ref(false)
  const currentPage = ref(1)
  const pageSize = ref(12)
  const totalItems = ref(0)

  const loadComments = async () => {
    commentLoading.value = true
    try {
      const res = await request.get('/comments/list/all', {
        params: {
          page: currentPage.value,
          size: pageSize.value
        }
      })
      const data = res?.data || {}
      const raw = data.records || data || []
      comments.value = Array.isArray(raw) ? raw : []
      totalItems.value = data.total || comments.value.length
    } catch (err) {
      logFetchError('评论列表', err)
      comments.value = []
      totalItems.value = 0
    } finally {
      commentLoading.value = false
    }
  }

  const handlePageChange = (page) => {
    currentPage.value = page
    loadComments()
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    currentPage.value = 1
    loadComments()
  }

  const handleCommentPost = async (content, replyData, onSuccess, onError) => {
    if (!isLoggedIn?.value) {
      ElMessage.warning('请先登录后再发表评论')
      onError?.()
      return
    }
    if (!content?.trim()) return
    try {
      const payload = { targetType: 'general', targetId: 0, content }
      if (replyData?.replyToId) {
        payload.replyToId = replyData.replyToId
        payload.replyToUserId = replyData.replyToUserId
        payload.replyToUsername = replyData.replyToName
      }
      await request.post('/comments', payload)
      await loadComments()
      ElMessage.success(replyData ? '回复成功，等待审核' : '评论提交成功，等待审核')
      onSuccess?.()
    } catch (err) {
      logFetchError('评论', err)
      ElMessage.error('评论发送失败，请检查是否已登录')
      onError?.()
    }
  }

  return { comments, commentLoading, loadComments, handleCommentPost, currentPage, pageSize, totalItems, handlePageChange, handleSizeChange }
}

export const usePagination = (defaultSize = 12) => {
  const currentPage = ref(1)
  const pageSize = ref(defaultSize)

  const paginatedList = (list) => {
    const start = (currentPage.value - 1) * pageSize.value
    return list.slice(start, start + pageSize.value)
  }

  const resetPage = () => { currentPage.value = 1 }

  return { currentPage, pageSize, paginatedList, resetPage }
}

export const useFilter = (items, filterFields = []) => {
  const keyword = ref('')
  const filters = ref({})

  const filteredList = computed(() => {
    let result = items.value
    
    if (keyword.value) {
      result = result.filter(item => 
        filterFields.some(field => item[field]?.includes(keyword.value))
      )
    }
    
    Object.entries(filters.value).forEach(([key, value]) => {
      if (value) {
        result = result.filter(item => item[key] === value)
      }
    })
    
    return result
  })

  const setFilter = (key, value) => { filters.value[key] = value }
  const clearFilters = () => { keyword.value = ''; filters.value = {} }

  return { keyword, filters, filteredList, setFilter, clearFilters }
}

export const useStats = (items, config = []) => {
  return computed(() => {
    const list = Array.isArray(items.value) ? items.value : []
    return config.map(({ value, label, compute }) => {
      if (compute) return { value: compute(items.value), label }
      if (value === 'count') return { value: list.length, label }
      if (value === 'views') return { value: list.reduce((sum, item) => sum + (item.viewCount || 0), 0), label }
      if (value === 'favorites') return { value: list.reduce((sum, item) => sum + (item.favoriteCount || 0), 0), label }
      return { value: 0, label }
    })
  })
}
