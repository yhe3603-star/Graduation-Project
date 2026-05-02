<template>
  <div class="ai-chat-wrapper">
    <!-- Drawer for session history (all screen sizes) -->
    <el-drawer
      v-model="drawerVisible"
      title="历史会话"
      direction="ltr"
      size="300px"
      @open="loadSessions"
    >
      <div class="drawer-content">
        <div class="drawer-new-chat">
          <el-button type="primary" @click="startNewChatFromDrawer">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
        <div class="session-list" v-loading="sessionsLoading">
          <div
            v-for="session in sessions"
            :key="session.sessionId"
            :class="['session-item', { active: session.sessionId === currentSessionId }]"
            @click="selectSessionFromDrawer(session)"
          >
            <div class="session-info">
              <div class="session-title">{{ session.preview || '新对话' }}</div>
              <div class="session-time">{{ formatSessionTime(session.lastMessageAt) }}</div>
            </div>
            <el-button
              class="session-delete-btn"
              text
              size="small"
              @click.stop="deleteSession(session.sessionId)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-empty v-if="!sessionsLoading && sessions.length === 0" description="暂无历史会话" :image-size="60" />
        </div>
      </div>
    </el-drawer>

    <!-- Chat area -->
    <el-card class="ai-chat-card" shadow="hover">
      <template #header>
        <div class="chat-header">
          <span class="header-title">
            <el-icon><ChatDotRound /></el-icon>
            侗医智能助手
          </span>
          <div class="header-actions">
            <!-- Show session info bar when continuing a session -->
            <div v-if="currentSessionId && !showWelcome" class="session-badge">
              <el-icon><ChatLineSquare /></el-icon>
              <span class="session-badge-text">继续对话</span>
              <el-button
                class="session-badge-new"
                type="primary"
                size="small"
                plain
                @click="startNewChat"
              >
                <el-icon><Plus /></el-icon>
                新对话
              </el-button>
            </div>
            <!-- Show history button when there are sessions -->
            <el-button
              v-if="isLoggedIn && sessions.length > 0"
              class="history-toggle-btn"
              size="small"
              @click="drawerVisible = true"
            >
              <el-icon><ChatLineSquare /></el-icon>
              历史会话
            </el-button>
            <el-tag
              :type="wsConnected ? 'success' : 'danger'"
              size="small"
            >
              {{ wsConnected ? 'AI在线' : 'AI离线' }}
            </el-tag>
          </div>
        </div>
      </template>

      <div
        ref="chatContainer"
        class="chat-container"
      >
        <div v-show="showWelcome" class="welcome-message">
          <div class="welcome-icon">🤖</div>
          <p>您好！我是侗族医药智能助手</p>
          <p class="welcome-tip">您可以问我关于侗族医药的问题，例如：</p>
          <div class="quick-questions">
            <el-tag
              v-for="(q, i) in quickQuestions"
              :key="i"
              class="quick-tag"
              @click="sendQuickQuestion(q)"
            >{{ q }}</el-tag>
          </div>
        </div>

        <div v-show="!showWelcome">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="message-content">
              <div class="message-text">
                <span v-html="renderMarkdown(msg.content)"></span>
                <span v-if="msg.streaming" class="cursor-blink">▌</span>
              </div>
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
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted, onUnmounted, onActivated, onDeactivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, ChatLineSquare, Delete, Plus } from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

marked.setOptions({ breaks: true, gfm: true })

const DOMPURIFY_CONFIG = {
  ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'b', 'i', 'u', 's', 'code', 'pre', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'blockquote', 'a', 'table', 'thead', 'tbody', 'tr', 'th', 'td', 'hr', 'span', 'sub', 'sup', 'del', 'mark'],
  ALLOWED_ATTR: ['href', 'target', 'rel', 'class'],
  ALLOW_DATA_ATTR: false,
  FORBID_TAGS: ['style', 'script', 'iframe', 'form', 'input', 'button', 'object', 'embed', 'link'],
  FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover', 'onfocus', 'onblur']
}

const userStore = useUserStore()
const isLoggedIn = ref(false)
const messages = ref([])
const inputMessage = ref('')
const streaming = ref(false)
const wsConnected = ref(false)
const chatContainer = ref(null)
const drawerVisible = ref(false)

// Chat history state
const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)

let ws = null
let currentAssistantIndex = -1

const forceWelcome = ref(false)
const showWelcome = computed(() => messages.value.length === 0 && !streaming.value)

const quickQuestions = [
  '侗族医药有什么特点？',
  '什么是侗族药浴疗法？',
  '侗族常用草药有哪些？'
]

const getWsUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const token = localStorage.getItem('token')
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
      // Backend returns sessionId in start message
      if (data.sessionId) {
        currentSessionId.value = data.sessionId
      }
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
      if (data.sessionId) {
        currentSessionId.value = data.sessionId
        // Do NOT update sessions list mid-conversation — sidebar stays hidden
        // Sessions will appear on next page load after Redis→MySQL flush
      }
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
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再使用AI助手')
    return
  }
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，请刷新页面后重试')
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

  const payload = {
    type: 'chat',
    message: userMessage,
    history: history
  }
  // Send sessionId to backend so it can associate messages
  if (currentSessionId.value) {
    payload.sessionId = currentSessionId.value
  }

  ws.send(JSON.stringify(payload))
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
  return DOMPurify.sanitize(html, DOMPURIFY_CONFIG)
}

// Chat history methods
const loadSessions = async () => {
  if (!isLoggedIn.value) return
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

const loadSession = async (session) => {
  console.log('[AiChat] loadSession clicked:', session.sessionId)
  if (!session || !session.sessionId) {
    console.error('[AiChat] Invalid session:', session)
    return
  }
  forceWelcome.value = false
  currentSessionId.value = session.sessionId
  // Close drawer on any screen size (mobile & desktop)
  drawerVisible.value = false
  try {
    const res = await request.get(`/chat-history/sessions/${session.sessionId}`)
    console.log('[AiChat] loadSession response:', res)
    const list = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : [])
    if (list.length > 0) {
      messages.value = list.map(m => ({
        role: m.role,
        content: m.content,
        streaming: false
      }))
      console.log('[AiChat] Session loaded:', list.length, 'messages')
    } else {
      messages.value = []
      console.log('[AiChat] Session has no messages')
    }
    await nextTick()
    scrollToBottom()
    // Refresh session list to update previews
    loadSessions()
  } catch (e) {
    console.error('[AiChat] 加载会话消息失败:', e)
    ElMessage.error('加载会话消息失败')
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
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
      currentAssistantIndex = -1
      streaming.value = false
    }
    ElMessage.success('会话已删除')
  } catch (e) {
    console.error('删除会话失败:', e)
    ElMessage.error('删除会话失败')
  }
}

const resetToWelcome = () => {
  currentSessionId.value = null
  messages.value = []
  currentAssistantIndex = -1
  streaming.value = false
  drawerVisible.value = false
  forceWelcome.value = true
}

const startNewChat = () => {
  console.log('[AiChat] startNewChat clicked, messages:', messages.value.length)
  if (streaming.value && ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'stop' }))
  }
  resetToWelcome()
  // Force DOM update then scroll to top
  nextTick(() => {
    nextTick(() => {
      if (chatContainer.value) {
        chatContainer.value.scrollTop = 0
        console.log('[AiChat] scrolled to top, welcome visible:', messages.value.length === 0)
      }
    })
  })
}

const updateLocalSession = (sid) => {
  const existing = sessions.value.find(s => s.sessionId === sid)
  const lastMsg = messages.value.length > 0 ? messages.value[0] : null
  const preview = lastMsg ? (lastMsg.content.length > 50 ? lastMsg.content.substring(0, 50) + '...' : lastMsg.content) : '新对话'
  const now = new Date().toISOString()
  if (existing) {
    existing.preview = preview
    existing.messageCount = messages.value.length
    existing.lastMessageAt = now
  } else {
    sessions.value.unshift({
      sessionId: sid,
      preview: preview,
      messageCount: messages.value.length,
      lastMessageAt: now
    })
  }
}

const startNewChatFromDrawer = () => {
  startNewChat()
  drawerVisible.value = false
}

const selectSessionFromDrawer = (session) => {
  loadSession(session)
}

const formatSessionTime = (time) => {
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

// Watch login state changes to connect/disconnect WebSocket
watch(() => userStore.isLoggedIn, (loggedIn) => {
  isLoggedIn.value = loggedIn
  if (loggedIn) {
    loadSessions()
    if (!ws || ws.readyState === WebSocket.CLOSED) {
      connectWebSocket()
    }
  } else {
    if (ws) {
      ws.close()
      ws = null
      wsConnected.value = false
    }
    sessions.value = []
    messages.value = []
    currentSessionId.value = null
  }
})

onMounted(() => {
  forceWelcome.value = false
  isLoggedIn.value = userStore.isLoggedIn
  if (isLoggedIn.value) {
    loadSessions()
    connectWebSocket()
  }
})

onActivated(() => {
  forceWelcome.value = false
  isLoggedIn.value = userStore.isLoggedIn
  if (isLoggedIn.value) {
    loadSessions()
    if (!ws || ws.readyState === WebSocket.CLOSED) {
      connectWebSocket()
    }
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
.ai-chat-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  margin-top: 16px;
  min-height: 500px;
}

/* Chat card */
.ai-chat-card {
  flex: 1;
  border-radius: var(--radius-lg);
  min-width: 0;
}

.ai-chat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  min-height: 400px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--dong-indigo);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

/* Session badge — shown when continuing a previous session */
.session-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: linear-gradient(135deg, rgba(40, 180, 99, 0.08), rgba(26, 82, 118, 0.06));
  border: 1px solid rgba(40, 180, 99, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  color: var(--dong-jade-dark);
}

.session-badge .el-icon {
  font-size: 14px;
}

.session-badge-text {
  font-weight: var(--font-weight-medium);
}

.session-badge-new {
  margin-left: 4px;
  font-size: var(--font-size-xs) !important;
  padding: 2px 10px !important;
}

/* History toggle button */
.history-toggle-btn {
  font-size: var(--font-size-xs);
}

/* Drawer */
:deep(.el-drawer__body) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

.drawer-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.drawer-new-chat {
  padding: var(--space-md) var(--space-sm);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.drawer-new-chat .el-button {
  width: 100%;
}

/* Session list (in drawer) */
.session-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: var(--space-sm);
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-bottom: var(--space-xs);
}

.session-item:hover {
  background: var(--bg-rice);
}

.session-item.active {
  background: linear-gradient(135deg, rgba(26, 82, 118, 0.08), rgba(26, 82, 118, 0.12));
}

.session-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.session-title {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

.session-delete-btn {
  opacity: 0;
  transition: opacity var(--transition-fast);
  color: var(--text-light);
}

.session-item:hover .session-delete-btn {
  opacity: 1;
}

.session-delete-btn:hover {
  color: var(--color-danger);
}

/* Chat container */
.chat-container {
  flex: 1;
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
  background: var(--bg-rice);
  border-radius: var(--radius-sm);
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
  color: var(--text-primary);
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
  background: var(--dong-indigo);
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
  background: var(--dong-indigo);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-text {
  background: var(--text-inverse);
  color: var(--text-primary);
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
  color: var(--dong-indigo);
}

.cursor-blink {
  animation: blink 0.8s infinite;
  color: var(--dong-indigo);
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

/* Responsive */
@media (max-width: 768px) {
  .chat-container {
    height: 350px;
  }
  .message-content {
    max-width: 90%;
  }
}

@media (max-width: 480px) {
  .quick-questions {
    flex-direction: column;
    align-items: center;
  }
  .chat-container {
    height: 300px;
    padding: 12px;
  }
  .message-text {
    padding: 10px 12px;
    font-size: 13px;
  }
  .session-badge {
    font-size: 11px;
    padding: 3px 8px;
  }
}
</style>
