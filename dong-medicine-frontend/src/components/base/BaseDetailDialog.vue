<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :class="dialogClass"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <slot />
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: 'min(800px, 90vw)' },
  dialogClass: { type: String, default: 'detail-dialog' }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.detail-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}
.detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
  margin-right: 0;
}
.detail-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}
.detail-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--border-light);
}
.detail-dialog :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #666;
}
.detail-dialog :deep(.el-descriptions__content) {
  color: var(--text-primary);
}
.detail-dialog :deep(.el-tag) {
  border-radius: 4px;
}
.detail-dialog :deep(.el-image) {
  border-radius: 8px;
}
.detail-dialog :deep(.el-empty) {
  padding: 40px 0;
  background: transparent;
}
.detail-dialog :deep(.el-empty__description) {
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .detail-dialog :deep(.el-dialog) {
    width: 100% !important;
    height: 100% !important;
    max-height: 100%;
    border-radius: 0;
    margin: 0;
  }
  .detail-dialog :deep(.el-dialog__body) {
    max-height: calc(100vh - 130px);
  }
}
</style>
