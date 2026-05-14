/**
 * @file 药材相关API
 */
import request from '@/utils/request'

/** 获取药材列表 */
export function getPlants(params) {
  return request.get('/plants/list', { params })
}

/** 获取单个药材详情 */
export function getPlantById(id) {
  return request.get(`/plants/${id}`)
}

/** 搜索药材 */
export function searchPlants(keyword, params) {
  return request.get(`/plants/search?keyword=${encodeURIComponent(keyword)}`, { params })
}

/** 记录药材浏览 */
export function viewPlant(id) {
  return request.post(`/plants/${id}/view`)
}

/** 批量获取药材（用于知识关联等场景） */
export function batchGetPlants(ids) {
  return request.post('/plants/batch', ids)
}

/** 获取随机药材列表 */
export function getRandomPlants(limit) {
  return request.get('/plants/random', { params: { limit } })
}
