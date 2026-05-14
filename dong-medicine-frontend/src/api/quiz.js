/**
 * @file 答题相关API
 */
import request from '@/utils/request'

/** 获取答题题目 */
export function getQuizQuestions(params) {
  return request.get('/quiz/questions', { params })
}

/** 提交答题 */
export function submitQuiz(data, params) {
  return request.post('/quiz/submit', data, { params })
}

/** 获取答题记录 */
export function getQuizRecords() {
  return request.get('/quiz/records')
}

/** 获取植物游戏记录 */
export function getPlantGameRecords() {
  return request.get('/plant-game/records')
}

/** 提交植物游戏成绩 */
export function submitPlantGame(data) {
  return request.post('/plant-game/submit', data)
}

/** 获取随机植物（用于植物识别游戏） */
export function getRandomPlantsForGame(limit) {
  return request.get('/plants/random', { params: { limit } })
}
