<template>
  <div class="chat-input">
    <el-input
      :model-value="modelValue"
      placeholder="请输入您的问题..."
      :disabled="disabled || streaming"
      @update:model-value="$emit('update:modelValue', $event)"
      @keyup.enter="$emit('send')"
    >
      <template #append>
        <el-button
          v-if="!streaming"
          type="primary"
          :disabled="!modelValue?.trim() || disabled"
          @click="$emit('send')"
        >
          发送
        </el-button>
        <el-button
          v-else
          type="danger"
          @click="$emit('stop')"
        >
          停止
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  streaming: { type: Boolean, default: false }
})

defineEmits(['update:modelValue', 'send', 'stop'])
</script>

<style scoped>
.chat-input { display: flex; gap: 12px; }
.chat-input .el-input { flex: 1; }
.chat-input :deep(.el-input__wrapper) { border-radius: 8px; box-shadow: 0 0 0 1px var(--el-border-color) inset; }
.chat-input :deep(.el-input__wrapper:focus) { box-shadow: 0 0 0 1px var(--el-color-primary) inset !important; }
.chat-input .el-button--primary { color: var(--text-inverse); }
.chat-input .el-button--primary:disabled { color: #a0cfff; }
</style>
