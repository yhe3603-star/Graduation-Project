/**
 * @file 管理后台相关API
 */
import request from '@/utils/request'

/** 获取管理后台列表数据 */
export function getAdminItems(entity, params) {
  const qs = new URLSearchParams(params).toString()
  return request.get(`/admin/${entity}?${qs}`)
}

/** 获取管理后台统计 */
export function getAdminStats() {
  return request.get('/admin/stats')
}

/** 获取日志统计 */
export function getLogStats() {
  return request.get('/admin/logs/stats')
}

/** 获取用户增长统计 */
export function getUserGrowthStats() {
  return request.get('/admin/stats/user-growth')
}

/** 获取内容浏览统计 */
export function getContentViewStats() {
  return request.get('/admin/stats/content-views')
}

/** 获取搜索关键词统计 */
export function getSearchKeywordsStats() {
  return request.get('/admin/stats/search-keywords')
}

// --- 药材管理 ---
export function createPlant(data) {
  return request.post('/admin/plants', data)
}

export function updatePlant(id, data) {
  return request.put(`/admin/plants/${id}`, data)
}

export function deletePlant(id) {
  return request.delete(`/admin/plants/${id}`)
}

// --- 知识管理 ---
export function createKnowledge(data) {
  return request.post('/admin/knowledge', data)
}

export function updateKnowledge(id, data) {
  return request.put(`/admin/knowledge/${id}`, data)
}

export function deleteKnowledge(id) {
  return request.delete(`/admin/knowledge/${id}`)
}

// --- 传承人管理 ---
export function createInheritor(data) {
  return request.post('/admin/inheritors', data)
}

export function updateInheritor(id, data) {
  return request.put(`/admin/inheritors/${id}`, data)
}

export function deleteInheritor(id) {
  return request.delete(`/admin/inheritors/${id}`)
}

// --- 资源管理 ---
export function createResource(data) {
  return request.post('/admin/resources', data)
}

export function updateResource(id, data) {
  return request.put(`/admin/resources/${id}`, data)
}

export function deleteResource(id) {
  return request.delete(`/admin/resources/${id}`)
}

// --- 问答管理 ---
export function createQa(data) {
  return request.post('/admin/qa', data)
}

export function updateQa(id, data) {
  return request.put(`/admin/qa/${id}`, data)
}

export function deleteQa(id) {
  return request.delete(`/admin/qa/${id}`)
}

// --- 用户管理 ---
export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

export function banUser(id) {
  return request.put(`/admin/users/${id}/ban`)
}

export function unbanUser(id) {
  return request.put(`/admin/users/${id}/unban`)
}

// --- 答题管理 ---
export function addQuizQuestion(data) {
  return request.post('/quiz/add', data)
}

export function updateQuizQuestion(data) {
  return request.put('/quiz/update', data)
}

// --- 评论管理 ---
export function approveComment(id) {
  return request.put(`/admin/comments/${id}/approve`)
}

export function rejectComment(id) {
  return request.put(`/admin/comments/${id}/reject`)
}

export function deleteComment(id) {
  return request.delete(`/admin/comments/${id}`)
}

// --- 反馈管理 ---
export function replyFeedback(id, data) {
  return request.put(`/admin/feedback/${id}/reply`, data)
}

export function deleteFeedback(id) {
  return request.delete(`/admin/feedback/${id}`)
}

// --- 日志管理 ---
export function deleteLog(id) {
  return request.delete(`/admin/logs/${id}`)
}

export function batchDeleteLogs(ids) {
  return request.delete('/admin/logs/batch', { data: ids })
}

export function clearLogs() {
  return request.delete('/admin/logs/clear')
}

// --- 通知管理 ---
export function getNotifications() {
  return request.get('/notifications')
}

export function getUnreadNotificationCount() {
  return request.get('/notifications/unread-count')
}

export function markNotificationsRead() {
  return request.post('/notifications/read')
}

// --- 导出 ---
export function exportEntity(entity, params) {
  return request.get(`/admin/export/${entity}`, {
    params,
    responseType: 'blob'
  })
}

// --- 通用元数据 ---
export function getMetadataFilters() {
  return request.get('/metadata/filters')
}
