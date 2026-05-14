/**
 * @file 浏览历史相关API
 */
import request from '@/utils/request'

/** 获取当前用户浏览历史 */
export function getMyBrowseHistory() {
  return request.get('/browse-history/my')
}

/** 记录浏览历史 */
export function recordBrowseHistory(targetType, targetId) {
  return request.post('/browse-history/record', null, {
    params: { targetType, targetId }
  })
}
