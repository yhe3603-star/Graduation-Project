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
          type="success"
          size="small"
        >
          AI在线
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
            {{ msg.content }}
          </div>
        </div>
      </div>
      
      <div
        v-if="loading"
        class="message assistant loading"
      >
        <div class="message-avatar">
          🤖
        </div>
        <div class="message-content">
          <div class="typing-indicator">
            <span /><span /><span />
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        placeholder="请输入您的问题..."
        :disabled="loading"
        @keyup.enter="sendMessage"
      >
        <template #append>
          <el-button 
            type="primary" 
            :loading="loading" 
            :disabled="!inputMessage.trim()"
            @click="sendMessage"
          >
            发送
          </el-button>
        </template>
      </el-input>
    </div>
  </el-card>
</template>

<script setup>
import { ref, nextTick, inject } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'

const request = inject('request')

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const chatContainer = ref(null)

const quickQuestions = [
  '侗族医药有什么特点？',
  '什么是侗族药浴疗法？',
  '侗族常用草药有哪些？'
]

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMessage = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  inputMessage.value = ''
  scrollToBottom()
  
  loading.value = true
  try {
    const history = messages.value.slice(-10).map(m => ({
      role: m.role,
      content: m.content
    }))
    
    const res = await request.post('/chat', { 
      message: userMessage,
      history: history.slice(0, -1)
    })
    
    if (res.code === 200) {
      messages.value.push({ role: 'assistant', content: res.data })
    } else {
      messages.value.push({ 
        role: 'assistant', 
        content: res.msg || '抱歉，服务暂时不可用，请稍后重试。' 
      })
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
    messages.value.push({ 
      role: 'assistant', 
      content: '抱歉，网络连接出现问题，请稍后重试。' 
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}
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

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #1a5276;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.chat-input {
  display: flex;
  gap: 12px;
}

.chat-input .el-input {
  flex: 1;
}

.chat-input .el-button--primary {
  color: var(--text-inverse);
}

.chat-input .el-button--primary:disabled {
  color: #a0cfff;
}
</style>
