/**
 * @file 知识相关API
 */
import request from '@/utils/request'

/** 获取知识列表 */
export function getKnowledgeList(params) {
  return request.get('/knowledge/list', { params })
}

/** 获取单条知识详情 */
export function getKnowledgeById(id) {
  return request.get(`/knowledge/${id}`)
}

/** 搜索知识 */
export function searchKnowledge(keyword, params) {
  return request.get(`/knowledge/search?keyword=${encodeURIComponent(keyword)}`, { params })
}

/** 记录知识浏览 */
export function viewKnowledge(id) {
  return request.post(`/knowledge/${id}/view`)
}
