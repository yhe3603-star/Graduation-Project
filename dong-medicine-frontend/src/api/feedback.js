/**
 * @file 反馈相关API
 */
import request from '@/utils/request'

/** 提交反馈 */
export function submitFeedback(data) {
  return request.post('/feedback', data)
}

/** 获取我的反馈 */
export function getMyFeedback() {
  return request.get('/feedback/my')
}

/** 获取反馈统计 */
export function getFeedbackStats() {
  return request.get('/feedback/stats')
}
