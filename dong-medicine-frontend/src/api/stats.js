/**
 * @file 统计相关API
 */
import request from '@/utils/request'

/** 获取统计概览 */
export function getStatsOverview() {
  return request.get('/stats/overview')
}

/** 获取图表数据 */
export function getChartData() {
  return request.get('/stats/chart')
}

/** 获取趋势数据 */
export function getTrendData() {
  return request.get('/stats/trend')
}

/** 获取药材统计 */
export function getPlantsStats() {
  return request.get('/stats/plants')
}

/** 获取知识统计 */
export function getKnowledgeStats() {
  return request.get('/stats/knowledge')
}

/** 获取传承人统计 */
export function getInheritorsStats() {
  return request.get('/stats/inheritors')
}

/** 获取资源统计 */
export function getResourcesStats() {
  return request.get('/stats/resources')
}

/** 获取问答统计 */
export function getQaStats() {
  return request.get('/stats/qa')
}
