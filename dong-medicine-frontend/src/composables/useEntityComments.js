import { ref, toValue } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { logFetchError } from '@/utils/logger'

export function useEntityComments(targetType, targetId) {
  const comments = ref([])
  const commentLoading = ref(false)
  const hasMore = ref(true)
  const currentPage = ref(1)
  const pageSize = 20

  const loadComments = async (reset = true) => {
    const id = toValue(targetId)
    if (!id) return
    if (reset) {
      currentPage.value = 1
      hasMore.value = true
    }
    commentLoading.value = true
    try {
      const res = await request.get(`/comments/list/${toValue(targetType)}/${id}`, {
        params: { page: currentPage.value, size: pageSize }
      })
      const data = res?.data || {}
      const raw = data.records || data || []
      const list = Array.isArray(raw) ? raw : []
      if (reset) {
        comments.value = list
      } else {
        comments.value = [...comments.value, ...list]
      }
      hasMore.value = list.length >= pageSize
    } catch (err) {
      logFetchError('评论列表', err)
      if (reset) comments.value = []
    } finally {
      commentLoading.value = false
    }
  }

  const loadMore = async () => {
    if (commentLoading.value || !hasMore.value) return
    currentPage.value++
    await loadComments(false)
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
      const msg = err?.msg || err?.response?.data?.msg
      if (msg) {
        ElMessage.error(msg)
      }
      logFetchError('评论', err)
      onError?.()
    }
  }

  const handleLike = async (comment) => {
    try {
      if (comment.isLiked) {
        await request.delete(`/comments/${comment.id}/like`)
        comment.isLiked = false
        comment.likes = Math.max((comment.likes || 1) - 1, 0)
      } else {
        await request.post(`/comments/${comment.id}/like`)
        comment.isLiked = true
        comment.likes = (comment.likes || 0) + 1
      }
    } catch (err) {
      const msg = err?.msg || err?.response?.data?.msg
      if (msg) ElMessage.error(msg)
    }
  }

  return {
    comments,
    commentLoading,
    hasMore,
    loadComments,
    loadMore,
    handleCommentPost,
    handleLike
  }
}
