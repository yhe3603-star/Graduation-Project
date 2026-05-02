import { ref } from 'vue'
import { ElMessage } from 'element-plus'

export function useChatWebSocket() {
  const messages = ref([])
  const inputMessage = ref('')
  const streaming = ref(false)
  const wsConnected = ref(false)
  const currentSessionId = ref(null)
  const forceWelcome = ref(false)

  let ws = null
  let currentAssistantIndex = -1

  const MAX_WS_RETRIES = 5
  let wsRetryCount = 0

  const getWsUrl = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const token = localStorage.getItem('token')
    const params = token ? `?token=${encodeURIComponent(token)}` : ''
    return `${protocol}//${host}/ws/chat${params}`
  }

  function handleWsMessage(data) {
    switch (data.type) {
      case 'start':
        currentAssistantIndex = messages.value.length
        messages.value.push({ role: 'assistant', content: '', streaming: true })
        streaming.value = true
        if (data.sessionId) {
          currentSessionId.value = data.sessionId
        }
        break

      case 'token':
        if (currentAssistantIndex >= 0 && currentAssistantIndex < messages.value.length) {
          messages.value[currentAssistantIndex].content += data.content
        }
        break

      case 'done':
        if (currentAssistantIndex >= 0 && currentAssistantIndex < messages.value.length) {
          messages.value[currentAssistantIndex].streaming = false
          messages.value[currentAssistantIndex].content = data.content || messages.value[currentAssistantIndex].content
        }
        streaming.value = false
        currentAssistantIndex = -1
        if (data.sessionId) {
          currentSessionId.value = data.sessionId
        }
        break

      case 'stopped':
        if (currentAssistantIndex >= 0 && currentAssistantIndex < messages.value.length) {
          messages.value[currentAssistantIndex].streaming = false
        }
        streaming.value = false
        currentAssistantIndex = -1
        break

      case 'error':
        if (currentAssistantIndex >= 0 && currentAssistantIndex < messages.value.length) {
          messages.value[currentAssistantIndex].streaming = false
          if (!messages.value[currentAssistantIndex].content) {
            messages.value[currentAssistantIndex].content = data.content || '抱歉，服务暂时不可用。'
          }
        } else {
          messages.value.push({
            role: 'assistant',
            content: data.content || '抱歉，服务暂时不可用。',
            streaming: false
          })
        }
        streaming.value = false
        currentAssistantIndex = -1
        break
    }
  }

  function connectWebSocket() {
    const url = getWsUrl()
    ws = new WebSocket(url)

    ws.onopen = () => {
      wsConnected.value = true
      wsRetryCount = 0
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWsMessage(data)
      } catch (e) {
        console.error('WebSocket消息解析失败', e)
      }
    }

    ws.onclose = () => {
      wsConnected.value = false
      if (wsRetryCount < MAX_WS_RETRIES) {
        const delay = Math.min(3000 * Math.pow(2, wsRetryCount), 30000)
        wsRetryCount++
        setTimeout(() => {
          if (!ws || ws.readyState === WebSocket.CLOSED) {
            connectWebSocket()
          }
        }, delay)
      }
    }

    ws.onerror = () => {
      wsConnected.value = false
    }
  }

  function sendMessage() {
    if (!inputMessage.value.trim() || streaming.value) return
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      ElMessage.error('连接已断开，请刷新页面后重试')
      return
    }

    const userMessage = inputMessage.value.trim()
    messages.value.push({ role: 'user', content: userMessage })
    inputMessage.value = ''

    const history = messages.value.slice(-10, -1).map(m => ({
      role: m.role,
      content: m.content
    }))

    const payload = { type: 'chat', message: userMessage, history }
    if (currentSessionId.value) {
      payload.sessionId = currentSessionId.value
    }

    ws.send(JSON.stringify(payload))
  }

  function stopGeneration() {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'stop' }))
    }
  }

  function sendQuickQuestion(question) {
    inputMessage.value = question
    sendMessage()
  }

  function resetToWelcome() {
    currentSessionId.value = null
    messages.value = []
    currentAssistantIndex = -1
    streaming.value = false
    forceWelcome.value = true
  }

  function closeWebSocket() {
    if (ws) {
      ws.close()
      ws = null
      wsConnected.value = false
    }
  }

  function isWsActive() {
    return ws && ws.readyState === WebSocket.OPEN
  }

  return {
    messages, inputMessage, streaming, wsConnected, currentSessionId, forceWelcome,
    connectWebSocket, sendMessage, stopGeneration, sendQuickQuestion,
    resetToWelcome, closeWebSocket, isWsActive
  }
}
