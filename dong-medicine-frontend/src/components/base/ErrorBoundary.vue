<template>
  <div class="error-boundary">
    <slot v-if="!hasError" />
    <div v-else class="error-fallback">
      <div class="error-content">
        <el-icon :size="64" color="#f56c6c">
          <WarningFilled />
        </el-icon>
        <h2>页面出错了</h2>
        <p class="error-message">
{{ errorMessage }}
</p>
        <div class="error-actions">
          <el-button type="primary" @click="retry">
            <el-icon><Refresh /></el-icon>
            重试
          </el-button>
          <el-button @click="goHome">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
        </div>
        <el-collapse v-if="showDetails" class="error-details">
          <el-collapse-item title="错误详情" name="details">
            <pre>{{ errorStack }}</pre>
          </el-collapse-item>
        </el-collapse>
        <el-button text class="toggle-details" @click="showDetails = !showDetails">
          {{ showDetails ? '隐藏详情' : '显示详情' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled, Refresh, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('')
const errorStack = ref('')
const showDetails = ref(false)

onErrorCaptured((error, instance, info) => {
  console.error('ErrorBoundary captured:', error)
  console.error('Component:', instance)
  console.error('Info:', info)
  
  hasError.value = true
  errorMessage.value = error.message || '未知错误'
  errorStack.value = `${error.stack || ''}\n\nComponent: ${info}`
  
  return false
})

function retry() {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
  showDetails.value = false
  ElMessage.success('页面已重置')
}

function goHome() {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
  showDetails.value = false
  router.push('/')
}
</script>

<style scoped>
.error-boundary {
  width: 100%;
  height: 100%;
}

.error-fallback {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px;
}

.error-content {
  text-align: center;
  max-width: 600px;
}

.error-content h2 {
  margin: 20px 0 10px;
  color: #303133;
  font-size: 24px;
}

.error-message {
  color: #909399;
  margin-bottom: 30px;
  font-size: 14px;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.error-details {
  text-align: left;
  margin-top: 20px;
}

.error-details pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
}

.toggle-details {
  margin-top: 10px;
}
</style>
