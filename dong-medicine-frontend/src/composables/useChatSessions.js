import { ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

export function useChatSessions() {
  const sessions = ref([])
  const sessionsLoading = ref(false)
  const drawerVisible = ref(false)

  async function loadSessions(isLoggedIn) {
    if (!isLoggedIn) return
    if (sessionsLoading.value) return
    sessionsLoading.value = true
    try {
      const res = await request.get('/chat-history/sessions')
      sessions.value = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : [])
    } catch (e) {
      console.error('加载会话列表失败:', e)
      sessions.value = []
    } finally {
      sessionsLoading.value = false
    }
  }

  async function loadSession(session, { messages, currentSessionId, forceWelcome, scrollToBottom }) {
    if (!session || !session.sessionId) return
    forceWelcome.value = false
    currentSessionId.value = session.sessionId
    drawerVisible.value = false
    try {
      const res = await request.get(`/chat-history/sessions/${session.sessionId}`)
      const list = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : [])
      if (list.length > 0) {
        messages.value = list.map(m => ({
          role: m.role,
          content: m.content,
          streaming: false
        }))
      } else {
        messages.value = []
      }
      await nextTick()
      if (scrollToBottom) scrollToBottom()
    } catch (e) {
      console.error('加载会话消息失败:', e)
      ElMessage.error('加载会话消息失败')
    }
  }

  async function deleteSession(sessionId, { messages, currentSessionId, streaming, currentAssistantIndexRef }) {
    try {
      await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      })
    } catch {
      return
    }
    try {
      await request.delete(`/chat-history/sessions/${sessionId}`)
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
      if (currentSessionId.value === sessionId) {
        currentSessionId.value = null
        messages.value = []
        if (currentAssistantIndexRef) currentAssistantIndexRef.value = -1
        streaming.value = false
      }
      ElMessage.success('会话已删除')
    } catch (e) {
      console.error('删除会话失败:', e)
      ElMessage.error('删除会话失败')
    }
  }

  function formatSessionTime(time) {
    if (!time) return ''
    const d = new Date(time)
    const now = new Date()
    const diff = now - d
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
    if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
    return d.toLocaleDateString('zh-CN')
  }

  return { sessions, sessionsLoading, drawerVisible, loadSessions, loadSession, deleteSession, formatSessionTime }
}
