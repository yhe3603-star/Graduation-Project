/**
 * @file 传承人相关API
 */
import request from '@/utils/request'

/** 获取传承人列表 */
export function getInheritors(params) {
  return request.get('/inheritors/list', { params })
}

/** 获取单个传承人详情 */
export function getInheritorById(id) {
  return request.get(`/inheritors/${id}`)
}

/** 搜索传承人 */
export function searchInheritors(keyword, params) {
  return request.get(`/inheritors/search?keyword=${encodeURIComponent(keyword)}`, { params })
}

/** 记录传承人浏览 */
export function viewInheritor(id) {
  return request.post(`/inheritors/${id}/view`)
}
