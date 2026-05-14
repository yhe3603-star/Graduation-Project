/**
 * @file 搜索相关API
 */
import request from '@/utils/request'

/** 全局搜索 */
export function globalSearch(params) {
  return request.get('/search', { params })
}

/** 搜索建议 */
export function getSearchSuggestions(keyword) {
  return request.get('/search/suggest', { params: { keyword } })
}

/** 获取搜索热词 */
export function getHotWords(limit) {
  return request.get('/search/hot', { params: { limit } })
}
