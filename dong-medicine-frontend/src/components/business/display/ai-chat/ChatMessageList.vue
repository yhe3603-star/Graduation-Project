<template>
  <div ref="listRef" class="chat-message-list">
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
<!-- eslint-disable-next-line vue/no-v-html -->
          <span v-html="renderMarkdown(msg.content)" />
          <span v-if="msg.streaming" class="cursor-blink">▌</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const DOMPURIFY_CONFIG = {
  ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'b', 'i', 'u', 's', 'code', 'pre', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'blockquote', 'a', 'table', 'thead', 'tbody', 'tr', 'th', 'td', 'hr', 'span', 'sub', 'sup', 'del', 'mark'],
  ALLOWED_ATTR: ['href', 'target', 'rel', 'class'],
  ALLOW_DATA_ATTR: false,
  FORBID_TAGS: ['style', 'script', 'iframe', 'form', 'input', 'button', 'object', 'embed', 'link'],
  FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover', 'onfocus', 'onblur']
}

const props = defineProps({
  messages: { type: Array, default: () => [] },
  streaming: { type: Boolean, default: false }
})

const listRef = ref(null)

function renderMarkdown(text) {
  if (!text) return ''
  const html = marked.parse(text)
  return DOMPurify.sanitize(html, DOMPURIFY_CONFIG)
}

function scrollToBottom() {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

watch(() => props.messages.length, scrollToBottom)
watch(() => props.messages, scrollToBottom, { deep: true })

defineExpose({ scrollToBottom })
</script>

<style scoped>
.chat-message-list { min-height: 0; overflow-y: auto; }
.message { display: flex; gap: 12px; margin-bottom: 16px; }
.message.user { flex-direction: row-reverse; }
.message-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.message.user .message-avatar { background: var(--dong-indigo); }
.message.assistant .message-avatar { background: #e8f4f8; }
.message-content { max-width: 80%; }
.message-text { padding: 12px 16px; border-radius: 12px; line-height: 1.6; font-size: 14px; white-space: pre-wrap; word-break: break-word; }
.message.user .message-text { background: var(--dong-indigo); color: white; border-bottom-right-radius: 4px; }
.message.assistant .message-text { background: var(--text-inverse); color: var(--text-primary); border-bottom-left-radius: 4px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); }
.message.assistant .message-text :deep(code) { background: #f0f0f0; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.message.assistant .message-text :deep(strong) { color: var(--dong-indigo); }
.cursor-blink { animation: blink 0.8s infinite; color: var(--dong-indigo); font-weight: bold; }
@keyframes blink { 0%, 50% { opacity: 1; } 51%, 100% { opacity: 0; } }

@media (max-width: 768px) { .message-content { max-width: 90%; } }
@media (max-width: 480px) { .message-text { padding: 10px 12px; font-size: 13px; } }
</style>
