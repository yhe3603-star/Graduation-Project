import { ref, toValue } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { logFetchError } from '@/utils/logger'

export function useEntityComments(targetType, targetId) {
  const comments = ref([])
  const commentLoading = ref(false)
  const currentPage = ref(1)
  const pageSize = ref(6)
  const totalItems = ref(0)

  const loadComments = async () => {
    const id = toValue(targetId)
    if (!id) return
    commentLoading.value = true
    try {
      const res = await request.get(`/comments/list/${toValue(targetType)}/${id}`, {
        params: { page: currentPage.value, size: pageSize.value }
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

  const handleCommentPost = async (content, replyData, onSuccess, onError) => {
    if (!content?.trim()) return
    const id = toValue(targetId)
    if (!id) return
    try {
      const payload = { targetType: toValue(targetType), targetId: id, content }
      if (replyData?.replyToId) {
        payload.replyToId = replyData.replyToId
        payload.replyToUserId = replyData.replyToUserId
        payload.replyToUsername = replyData.replyToName
      }
      const res = await request.post('/comments', payload)
      const status = res?.data
      if (status === 'approved') {
        ElMessage.success('评论发布成功')
      } else {
        ElMessage.success('评论提交成功，等待审核')
      }
      await loadComments()
      onSuccess?.()
    } catch (err) {
      logFetchError('评论', err)
      onError?.()
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

  return {
    comments,
    commentLoading,
    totalItems,
    currentPage,
    pageSize,
    loadComments,
    handleCommentPost,
    handlePageChange,
    handleSizeChange
  }
}
