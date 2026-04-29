<template>
  <el-card
    class="ai-chat-card"
    shadow="hover"
  >
    <template #header>
      <div class="chat-header">
        <span class="header-title">
          <el-icon><ChatDotRound /></el-icon>
          侗医智能助手
        </span>
        <el-tag
          :type="wsConnected ? 'success' : 'danger'"
          size="small"
        >
          {{ wsConnected ? 'AI在线' : 'AI离线' }}
        </el-tag>
      </div>
    </template>
    
    <div
      ref="chatContainer"
      class="chat-container"
    >
      <div
        v-if="messages.length === 0"
        class="welcome-message"
      >
        <div class="welcome-icon">
          🤖
        </div>
        <p>您好！我是侗族医药智能助手</p>
        <p class="welcome-tip">
          您可以问我关于侗族医药的问题，例如：
        </p>
        <div class="quick-questions">
          <el-tag 
            v-for="(q, i) in quickQuestions" 
            :key="i" 
            class="quick-tag"
            @click="sendQuickQuestion(q)"
          >
            {{ q }}
          </el-tag>
        </div>
      </div>
      
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        :class="['message', msg.role]"
      >
        <div class="message-avatar">
          {{ msg.role === 'user' ? '👤' : '🤖' }}
        </div>
        <div class="message-content">
          <div class="message-text">
            <span v-html="renderMarkdown(msg.content)"></span>
            <span v-if="msg.streaming" class="cursor-blink">▌</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        placeholder="请输入您的问题..."
        :disabled="streaming"
        @keyup.enter="sendMessage"
      >
        <template #append>
          <el-button 
            v-if="!streaming"
            type="primary" 
            :disabled="!inputMessage.trim() || !wsConnected"
            @click="sendMessage"
          >
            发送
          </el-button>
          <el-button 
            v-else
            type="danger" 
            @click="stopGeneration"
          >
            停止
          </el-button>
        </template>
      </el-input>
    </div>
  </el-card>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, onActivated, onDeactivated } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

marked.setOptions({ breaks: true, gfm: true })

const messages = ref([])
const inputMessage = ref('')
const streaming = ref(false)
const wsConnected = ref(false)
const chatContainer = ref(null)

let ws = null
let currentAssistantIndex = -1

const quickQuestions = [
  '侗族医药有什么特点？',
  '什么是侗族药浴疗法？',
  '侗族常用草药有哪些？'
]

const getWsUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const token = sessionStorage.getItem('token')
  const params = token ? `?token=${encodeURIComponent(token)}` : ''
  return `${protocol}//${host}/ws/chat${params}`
}

const MAX_WS_RETRIES = 5
let wsRetryCount = 0

const connectWebSocket = () => {
  const url = getWsUrl()
  ws = new WebSocket(url)

  ws.onopen = () => {
    wsConnected.value = true
    wsRetryCount = 0
    console.log('WebSocket连接成功')
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
    console.log('WebSocket连接关闭')
    if (wsRetryCount < MAX_WS_RETRIES) {
      const delay = Math.min(3000 * Math.pow(2, wsRetryCount), 30000)
      wsRetryCount++
      setTimeout(() => {
        if (!ws || ws.readyState === WebSocket.CLOSED) {
          connectWebSocket()
        }
      }, delay)
    } else {
      console.warn('WebSocket重连已达最大次数')
    }
  }

  ws.onerror = (error) => {
    wsConnected.value = false
    console.error('WebSocket连接错误', error)
  }
}

const handleWsMessage = (data) => {
  switch (data.type) {
    case 'start':
      currentAssistantIndex = messages.value.length
      messages.value.push({ role: 'assistant', content: '', streaming: true })
      streaming.value = true
      scrollToBottom()
      break

    case 'token':
      if (currentAssistantIndex >= 0 && currentAssistantIndex < messages.value.length) {
        messages.value[currentAssistantIndex].content += data.content
        scrollToBottom()
      }
      break

    case 'done':
      if (currentAssistantIndex >= 0 && currentAssistantIndex < messages.value.length) {
        messages.value[currentAssistantIndex].streaming = false
        messages.value[currentAssistantIndex].content = data.content || messages.value[currentAssistantIndex].content
      }
      streaming.value = false
      currentAssistantIndex = -1
      scrollToBottom()
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

const sendMessage = () => {
  if (!inputMessage.value.trim() || streaming.value) return
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('WebSocket未连接，请稍后重试')
    return
  }

  const userMessage = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  inputMessage.value = ''
  scrollToBottom()

  const history = messages.value.slice(-10, -1).map(m => ({
    role: m.role,
    content: m.content
  }))

  ws.send(JSON.stringify({
    type: 'chat',
    message: userMessage,
    history: history
  }))
}

const stopGeneration = () => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'stop' }))
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const renderMarkdown = (text) => {
  if (!text) return ''
  const html = marked.parse(text)
  return DOMPurify.sanitize(html)
}

onMounted(() => {
  connectWebSocket()
})

onActivated(() => {
  if (!ws || ws.readyState === WebSocket.CLOSED) {
    connectWebSocket()
  }
})

onDeactivated(() => {
  if (ws) {
    ws.close()
    ws = null
    wsConnected.value = false
  }
})

onUnmounted(() => {
  if (ws) {
    ws.close()
    ws = null
  }
})
</script>

<style scoped>
.ai-chat-card {
  margin-top: 16px;
  border-radius: 12px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1a5276;
}

.chat-container {
  height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message p {
  margin: 8px 0;
  color: #333;
}

.welcome-tip {
  font-size: 13px;
  color: var(--text-muted);
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 16px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 82, 118, 0.2);
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #1a5276;
}

.message.assistant .message-avatar {
  background: #e8f4f8;
}

.message-content {
  max-width: 80%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .message-text {
  background: #1a5276;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-text {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message.assistant .message-text :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.message.assistant .message-text :deep(strong) {
  color: #1a5276;
}

.cursor-blink {
  animation: blink 0.8s infinite;
  color: #1a5276;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.chat-input {
  display: flex;
  gap: 12px;
}

.chat-input .el-input {
  flex: 1;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--el-border-color) inset;
}

.chat-input :deep(.el-input__wrapper:focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.chat-input .el-button--primary {
  color: var(--text-inverse);
}

.chat-input .el-button--primary:disabled {
  color: #a0cfff;
}
</style>
