<template>
  <div class="ai-chat-wrapper">
    <SessionDrawer
      :visible="drawerVisible"
      :sessions="sessions"
      :sessions-loading="sessionsLoading"
      :current-session-id="currentSessionId"
      :format-session-time="formatSessionTime"
      @open="loadSessions(isLoggedIn)"
      @close="drawerVisible = false"
      @select="onSelectSession"
      @delete="onDeleteSession"
      @new-chat="startNewChatFromDrawer"
    />

    <el-card class="ai-chat-card" shadow="hover">
      <template #header>
        <div class="chat-header">
          <span class="header-title">
            <el-icon><ChatDotRound /></el-icon>
            侗医智能助手
          </span>
          <div class="header-actions">
            <SessionBadge
              v-if="currentSessionId && !showWelcome"
              @new-chat="startNewChat"
            />
            <el-button
              v-if="isLoggedIn"
              class="history-toggle-btn"
              size="small"
              @click="drawerVisible = true"
            >
              <el-icon><ChatLineSquare /></el-icon>
              历史会话
            </el-button>
            <el-tag :type="wsConnected ? 'success' : 'danger'" size="small">
              {{ wsConnected ? 'AI在线' : 'AI离线' }}
            </el-tag>
          </div>
        </div>
      </template>

      <div ref="chatContainer" class="chat-container">
        <WelcomePanel
          v-show="showWelcome"
          :quick-questions="quickQuestions"
          @send-question="onQuickQuestion"
        />

        <ChatMessageList
          v-show="!showWelcome"
          ref="messageListRef"
          :messages="messages"
          :streaming="streaming"
        />
      </div>

      <ChatInputArea
        v-model="inputMessage"
        :disabled="!wsConnected"
        :streaming="streaming"
        @send="onSendMessage"
        @stop="stopGeneration"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, onActivated, onDeactivated } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineSquare } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useChatWebSocket } from '@/composables/useChatWebSocket'
import { useChatSessions } from '@/composables/useChatSessions'
import SessionDrawer from './ai-chat/SessionDrawer.vue'
import WelcomePanel from './ai-chat/WelcomePanel.vue'
import ChatMessageList from './ai-chat/ChatMessageList.vue'
import ChatInputArea from './ai-chat/ChatInputArea.vue'
import SessionBadge from './ai-chat/SessionBadge.vue'

const userStore = useUserStore()
const isLoggedIn = ref(false)
const chatContainer = ref(null)
const messageListRef = ref(null)

const {
  messages, inputMessage, streaming, wsConnected, currentSessionId, forceWelcome,
  connectWebSocket, sendMessage, stopGeneration, sendQuickQuestion,
  resetToWelcome, closeWebSocket
} = useChatWebSocket()

const {
  sessions, sessionsLoading, drawerVisible,
  loadSessions, loadSession, deleteSession, formatSessionTime
} = useChatSessions()

const quickQuestions = [
  '侗族医药有什么特点？',
  '什么是侗族药浴疗法？',
  '侗族常用草药有哪些？'
]

const showWelcome = computed(() => messages.value.length === 0 && !streaming.value)

const scrollToBottom = () => {
  messageListRef.value?.scrollToBottom()
}

function onSendMessage() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再使用AI助手')
    return
  }
  sendMessage()
  scrollToBottom()
}

function onQuickQuestion(question) {
  sendQuickQuestion(question)
  scrollToBottom()
}

function onSelectSession(session) {
  loadSession(session, {
    messages, currentSessionId, forceWelcome, streaming,
    scrollToBottom
  })
}

function onDeleteSession(sessionId) {
  deleteSession(sessionId, { messages, currentSessionId, streaming })
}

function startNewChat() {
  if (streaming.value) stopGeneration()
  resetToWelcome()
  chatContainer.value && (chatContainer.value.scrollTop = 0)
}

function startNewChatFromDrawer() {
  startNewChat()
  drawerVisible.value = false
}

watch(() => userStore.isLoggedIn, (loggedIn) => {
  isLoggedIn.value = loggedIn
  if (loggedIn) {
    loadSessions(true)
    connectWebSocket()
  } else {
    closeWebSocket()
    sessions.value = []
    messages.value = []
    currentSessionId.value = null
  }
})

onMounted(() => {
  isLoggedIn.value = userStore.isLoggedIn
  if (isLoggedIn.value) {
    loadSessions(true)
    connectWebSocket()
  }
})

onActivated(() => {
  isLoggedIn.value = userStore.isLoggedIn
  if (isLoggedIn.value) {
    loadSessions(true)
    connectWebSocket()
  }
})

onDeactivated(() => closeWebSocket())

onUnmounted(() => closeWebSocket())
</script>

<style scoped>
.ai-chat-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  margin-top: 16px;
  min-height: 500px;
}

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

.history-toggle-btn {
  font-size: var(--font-size-xs);
}

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

@media (max-width: 768px) {
  .chat-container { height: 350px; }
}

@media (max-width: 480px) {
  .chat-container { height: 300px; padding: 12px; }
}
</style>
