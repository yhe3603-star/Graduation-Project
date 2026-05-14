/**
 * @file AI聊天历史相关API
 */
import request from '@/utils/request'

/** 获取聊天会话列表 */
export function getChatSessions() {
  return request.get('/chat-history/sessions')
}

/** 获取单个会话的消息列表 */
export function getChatMessages(sessionId) {
  return request.get(`/chat-history/sessions/${sessionId}`)
}

/** 删除聊天会话 */
export function deleteChatSession(sessionId) {
  return request.delete(`/chat-history/sessions/${sessionId}`)
}
