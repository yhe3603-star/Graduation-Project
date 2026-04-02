<template>
  <Transition name="page-loading">
    <div
      v-if="visible"
      class="page-loading-overlay"
    >
      <div class="page-loading-content">
        <div class="loading-spinner">
          <div class="spinner-ring" />
          <div class="spinner-ring ring-delay" />
        </div>
        <div class="loading-text">
          <span>加载中</span>
          <span class="loading-dots">
            <i />
            <i />
            <i />
          </span>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});
</script>

<style scoped>
.page-loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9998;
}

.page-loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-spinner {
  position: relative;
  width: 48px;
  height: 48px;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-radius: 50%;
}

.spinner-ring {
  border-top-color: #1A5276;
  border-right-color: #28B463;
  animation: spin 0.8s linear infinite;
}

.spinner-ring.ring-delay {
  inset: 6px;
  border-top-color: #28B463;
  border-right-color: #1A5276;
  animation-direction: reverse;
  animation-duration: 0.6s;
}

.loading-text {
  font-size: 14px;
  color: #1A5276;
  letter-spacing: 1px;
}

.loading-dots {
  display: inline-flex;
  gap: 3px;
  margin-left: 2px;
}

.loading-dots i {
  width: 4px;
  height: 4px;
  background: #28B463;
  border-radius: 50%;
  animation: dot-bounce 1.2s ease-in-out infinite;
}

.loading-dots i:nth-child(1) { animation-delay: 0s; }
.loading-dots i:nth-child(2) { animation-delay: 0.15s; }
.loading-dots i:nth-child(3) { animation-delay: 0.3s; }

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes dot-bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.page-loading-enter-active,
.page-loading-leave-active {
  transition: opacity 0.2s ease;
}

.page-loading-enter-from,
.page-loading-leave-to {
  opacity: 0;
}
</style>
