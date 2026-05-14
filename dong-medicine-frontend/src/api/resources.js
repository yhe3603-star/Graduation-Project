/**
 * @file 资源相关API
 */
import request from '@/utils/request'

/** 获取资源列表 */
export function getResources(params) {
  return request.get('/resources/list', { params })
}

/** 获取单个资源详情 */
export function getResourceById(id) {
  return request.get(`/resources/${id}`)
}

/** 搜索资源 */
export function searchResources(keyword, params) {
  return request.get(`/resources/search?keyword=${encodeURIComponent(keyword)}`, { params })
}

/** 记录资源浏览 */
export function viewResource(id) {
  return request.post(`/resources/${id}/view`)
}

/** 下载单个资源 */
export function downloadResource(id) {
  return request.get(`/resources/download/${id}`, { responseType: 'blob' })
}

/** 批量下载资源 */
export function batchDownloadResources(ids) {
  return request.post('/resources/batch-download', ids, { responseType: 'blob' })
}
