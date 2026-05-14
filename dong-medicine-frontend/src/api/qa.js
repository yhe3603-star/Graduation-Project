/**
 * @file 问答相关API
 */
import request from '@/utils/request'

/** 获取问答列表 */
export function getQaList(params) {
  return request.get('/qa/list', { params })
}

/** 获取单条问答详情 */
export function getQaById(id) {
  return request.get(`/qa/${id}`)
}

/** 搜索问答 */
export function searchQa(keyword, params) {
  return request.get(`/qa/search?keyword=${encodeURIComponent(keyword)}`, { params })
}

/** 记录问答浏览 */
export function viewQa(id) {
  return request.post(`/qa/${id}/view`)
}
