/**
 * @file 评论相关API
 */
import request from '@/utils/request'

/** 获取某实体的评论列表 */
export function getCommentsByTarget(targetType, targetId, params) {
  return request.get(`/comments/list/${targetType}/${targetId}`, { params })
}

/** 获取通用评论列表 */
export function getGeneralComments(params) {
  return request.get('/comments/list/general', { params })
}

/** 获取当前用户的评论 */
export function getMyComments(params) {
  return request.get('/comments/my', { params })
}

/** 发表评论 */
export function addComment(data) {
  return request.post('/comments', data)
}

/** 点赞评论 */
export function likeComment(commentId) {
  return request.post(`/comments/${commentId}/like`)
}

/** 取消点赞评论 */
export function unlikeComment(commentId) {
  return request.delete(`/comments/${commentId}/like`)
}
