/**
 * @file 排行榜相关API
 */
import request from '@/utils/request'

/** 获取排行榜数据 */
export function getLeaderboard(params) {
  return request.get('/leaderboard', { params })
}
