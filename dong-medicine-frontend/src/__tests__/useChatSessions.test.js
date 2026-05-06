import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') }
}))

vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn().mockResolvedValue([]),
    post: vi.fn().mockResolvedValue({}),
    put: vi.fn().mockResolvedValue({}),
    delete: vi.fn().mockResolvedValue({})
  }
}))

import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useChatSessions } from '@/composables/useChatSessions'

describe('useChatSessions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('formatSessionTime', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2026-05-07T12:00:00'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should return empty string for falsy time', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime(null)).toBe('')
      expect(formatSessionTime(undefined)).toBe('')
      expect(formatSessionTime('')).toBe('')
    })

    it('should return "刚刚" for times less than 1 minute ago', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-07T11:59:30')).toBe('刚刚')
    })

    it('should return minutes ago for times less than 1 hour ago', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-07T11:55:00')).toBe('5分钟前')
    })

    it('should return hours ago for times less than 24 hours ago', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-07T09:00:00')).toBe('3小时前')
    })

    it('should return days ago for times less than 7 days ago', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-04T12:00:00')).toBe('3天前')
    })

    it('should return formatted date for times older than 7 days', () => {
      const { formatSessionTime } = useChatSessions()
      const result = formatSessionTime('2026-04-01T12:00:00')
      // toLocaleDateString('zh-CN') returns something like "2026/4/1"
      expect(result).toMatch(/2026/)
      expect(result).not.toBe('刚刚')
      expect(result).not.toMatch(/分钟前|小时前|天前/)
    })

    it('should handle 59 minutes ago correctly', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-07T11:01:00')).toBe('59分钟前')
    })

    it('should handle 23 hours ago correctly', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-06T13:00:00')).toBe('23小时前')
    })

    it('should handle 6 days ago correctly', () => {
      const { formatSessionTime } = useChatSessions()
      expect(formatSessionTime('2026-05-01T12:00:00')).toBe('6天前')
    })
  })

  describe('loadSessions', () => {
    it('should load sessions when user is logged in', async () => {
      const sessionsData = [
        { sessionId: 's1', title: '会话1' },
        { sessionId: 's2', title: '会话2' }
      ]
      request.get.mockResolvedValue(sessionsData)

      const { sessions, loadSessions } = useChatSessions()
      await loadSessions(true)

      expect(request.get).toHaveBeenCalledWith('/chat-history/sessions')
      expect(sessions.value).toEqual(sessionsData)
    })

    it('should handle response wrapped in data property', async () => {
      const sessionsData = [{ sessionId: 's1' }]
      request.get.mockResolvedValue({ data: sessionsData })

      const { sessions, loadSessions } = useChatSessions()
      await loadSessions(true)

      expect(sessions.value).toEqual(sessionsData)
    })

    it('should not fetch when user is not logged in', async () => {
      const { sessions, loadSessions } = useChatSessions()
      await loadSessions(false)
      expect(request.get).not.toHaveBeenCalled()
      expect(sessions.value).toEqual([])
    })

    it('should not fetch when already loading', async () => {
      request.get.mockImplementation(() => new Promise(() => {}))
      const { sessionsLoading, loadSessions } = useChatSessions()
      loadSessions(true)
      expect(sessionsLoading.value).toBe(true)
      await loadSessions(true)
      expect(request.get).toHaveBeenCalledTimes(1)
    })

    it('should set sessions to empty array on error', async () => {
      request.get.mockRejectedValue(new Error('network'))
      const { sessions, loadSessions } = useChatSessions()
      await loadSessions(true)
      expect(sessions.value).toEqual([])
    })

    it('should set loading to false after completion', async () => {
      request.get.mockResolvedValue([])
      const { sessionsLoading, loadSessions } = useChatSessions()
      await loadSessions(true)
      expect(sessionsLoading.value).toBe(false)
    })

    it('should set loading to false on error', async () => {
      request.get.mockRejectedValue(new Error('fail'))
      const { sessionsLoading, loadSessions } = useChatSessions()
      await loadSessions(true)
      expect(sessionsLoading.value).toBe(false)
    })
  })

  describe('loadSession', () => {
    let messages, currentSessionId, forceWelcome, scrollToBottom

    beforeEach(() => {
      messages = { value: [{ role: 'user', content: 'old' }] }
      currentSessionId = { value: null }
      forceWelcome = { value: true }
      scrollToBottom = vi.fn()
    })

    it('should do nothing when session is null', async () => {
      const { loadSession } = useChatSessions()
      await loadSession(null, { messages, currentSessionId, forceWelcome, scrollToBottom })
      expect(request.get).not.toHaveBeenCalled()
    })

    it('should do nothing when session has no sessionId', async () => {
      const { loadSession } = useChatSessions()
      await loadSession({}, { messages, currentSessionId, forceWelcome, scrollToBottom })
      expect(request.get).not.toHaveBeenCalled()
    })

    it('should load messages and update state', async () => {
      const msgData = [
        { role: 'user', content: '你好' },
        { role: 'assistant', content: '你好！' }
      ]
      request.get.mockResolvedValue(msgData)

      const { drawerVisible, loadSession } = useChatSessions()
      drawerVisible.value = true
      await loadSession({ sessionId: 's1' }, { messages, currentSessionId, forceWelcome, scrollToBottom })

      expect(forceWelcome.value).toBe(false)
      expect(currentSessionId.value).toBe('s1')
      expect(drawerVisible.value).toBe(false)
      expect(request.get).toHaveBeenCalledWith('/chat-history/sessions/s1')
      expect(messages.value).toEqual([
        { role: 'user', content: '你好', streaming: false },
        { role: 'assistant', content: '你好！', streaming: false }
      ])
      expect(scrollToBottom).toHaveBeenCalled()
    })

    it('should handle response wrapped in data property', async () => {
      const msgData = [{ role: 'user', content: 'test' }]
      request.get.mockResolvedValue({ data: msgData })

      const { loadSession } = useChatSessions()
      await loadSession({ sessionId: 's1' }, { messages, currentSessionId, forceWelcome, scrollToBottom })

      expect(messages.value).toEqual([{ role: 'user', content: 'test', streaming: false }])
    })

    it('should clear messages when session has no messages', async () => {
      request.get.mockResolvedValue([])
      messages.value = [{ role: 'user', content: 'old' }]

      const { loadSession } = useChatSessions()
      await loadSession({ sessionId: 's1' }, { messages, currentSessionId, forceWelcome, scrollToBottom })

      expect(messages.value).toEqual([])
      expect(scrollToBottom).toHaveBeenCalled()
    })

    it('should show error message on failure', async () => {
      request.get.mockRejectedValue(new Error('fail'))

      const { loadSession } = useChatSessions()
      await loadSession({ sessionId: 's1' }, { messages, currentSessionId, forceWelcome, scrollToBottom })

      expect(ElMessage.error).toHaveBeenCalledWith('加载会话消息失败')
    })
  })

  describe('deleteSession', () => {
    let messages, currentSessionId, streaming, currentAssistantIndexRef

    beforeEach(() => {
      messages = { value: [{ role: 'user', content: 'test' }] }
      currentSessionId = { value: 's1' }
      streaming = { value: true }
      currentAssistantIndexRef = { value: 0 }
    })

    it('should confirm, delete session, and update state', async () => {
      const { sessions, deleteSession } = useChatSessions()
      sessions.value = [
        { sessionId: 's1', title: '会话1' },
        { sessionId: 's2', title: '会话2' }
      ]

      await deleteSession('s1', { messages, currentSessionId, streaming, currentAssistantIndexRef })

      expect(ElMessageBox.confirm).toHaveBeenCalled()
      expect(request.delete).toHaveBeenCalledWith('/chat-history/sessions/s1')
      expect(sessions.value).toEqual([{ sessionId: 's2', title: '会话2' }])
      expect(currentSessionId.value).toBeNull()
      expect(messages.value).toEqual([])
      expect(currentAssistantIndexRef.value).toBe(-1)
      expect(streaming.value).toBe(false)
      expect(ElMessage.success).toHaveBeenCalledWith('会话已删除')
    })

    it('should not modify current session state when deleting non-current session', async () => {
      const { sessions, deleteSession } = useChatSessions()
      sessions.value = [
        { sessionId: 's1', title: '会话1' },
        { sessionId: 's2', title: '会话2' }
      ]
      currentSessionId.value = 's2'

      await deleteSession('s1', { messages, currentSessionId, streaming, currentAssistantIndexRef })

      expect(sessions.value).toEqual([{ sessionId: 's2', title: '会话2' }])
      expect(currentSessionId.value).toBe('s2')
    })

    it('should not delete when user cancels confirmation', async () => {
      ElMessageBox.confirm.mockRejectedValueOnce('cancel')
      const { sessions, deleteSession } = useChatSessions()
      sessions.value = [{ sessionId: 's1' }]

      await deleteSession('s1', { messages, currentSessionId, streaming, currentAssistantIndexRef })

      expect(request.delete).not.toHaveBeenCalled()
      expect(sessions.value).toEqual([{ sessionId: 's1' }])
    })

    it('should show error message when delete request fails', async () => {
      request.delete.mockRejectedValueOnce(new Error('network'))
      const { sessions, deleteSession } = useChatSessions()
      sessions.value = [{ sessionId: 's1' }]

      await deleteSession('s1', { messages, currentSessionId, streaming, currentAssistantIndexRef })

      expect(ElMessage.error).toHaveBeenCalledWith('删除会话失败')
      expect(sessions.value).toEqual([{ sessionId: 's1' }])
    })

    it('should handle missing currentAssistantIndexRef', async () => {
      const { sessions, deleteSession } = useChatSessions()
      sessions.value = [{ sessionId: 's1' }]

      await deleteSession('s1', { messages, currentSessionId, streaming, currentAssistantIndexRef: null })

      expect(request.delete).toHaveBeenCalled()
      expect(sessions.value).toEqual([])
    })
  })

  describe('returned refs and defaults', () => {
    it('should expose expected properties', () => {
      const result = useChatSessions()
      expect(result.sessions.value).toEqual([])
      expect(result.sessionsLoading.value).toBe(false)
      expect(result.drawerVisible.value).toBe(false)
      expect(typeof result.loadSessions).toBe('function')
      expect(typeof result.loadSession).toBe('function')
      expect(typeof result.deleteSession).toBe('function')
      expect(typeof result.formatSessionTime).toBe('function')
    })
  })
})
