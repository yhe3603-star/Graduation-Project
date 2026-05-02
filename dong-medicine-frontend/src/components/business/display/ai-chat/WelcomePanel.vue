<template>
  <div class="welcome-message">
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
        @click="$emit('send-question', q)"
      >
{{ q }}
</el-tag>
    </div>
  </div>
</template>

<script setup>
defineProps({
  quickQuestions: { type: Array, default: () => [] }
})

defineEmits(['send-question'])
</script>

<style scoped>
.welcome-message { text-align: center; padding: 40px 20px; }
.welcome-icon { font-size: 48px; margin-bottom: 16px; }
.welcome-message p { margin: 8px 0; color: var(--text-primary); }
.welcome-tip { font-size: 13px; color: var(--text-muted); }
.quick-questions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; margin-top: 16px; }
.quick-tag { cursor: pointer; transition: all 0.3s; }
.quick-tag:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26, 82, 118, 0.2); }

@media (max-width: 480px) {
  .quick-questions { flex-direction: column; align-items: center; }
}
</style>
