/**
 * @file 收藏相关API
 */
import request from '@/utils/request'

/** 获取当前用户收藏列表 */
export function getMyFavorites() {
  return request.get('/favorites/my')
}

/** 收藏目标 */
export function addFavorite(targetType, targetId) {
  return request.post(`/favorites/${targetType}/${targetId}`)
}

/** 取消收藏 */
export function removeFavorite(targetType, targetId) {
  return request.delete(`/favorites/${targetType}/${targetId}`)
}

/** 记录浏览量（通用，根据类型） */
export function recordView(targetType, targetId) {
  return request.post(`/${targetType}/${targetId}/view`)
}
