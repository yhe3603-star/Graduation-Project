<template>
  <div class="captcha-input">
    <el-input
      v-model="inputCode"
      placeholder="请输入验证码"
      maxlength="4"
      clearable
      @input="handleInput"
    >
      <template #prefix>
        <el-icon><Key /></el-icon>
      </template>
    </el-input>
    <div class="captcha-image-wrapper" @click="refreshCaptcha">
      <img
        v-if="captchaImage"
        :src="captchaImage"
        alt="验证码"
        class="captcha-image"
        title="点击刷新验证码"
      >
      <div v-else class="captcha-loading">
        <el-icon class="is-loading">
<Loading />
</el-icon>
        <span>加载中</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Key, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  // 用于表单验证
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'update:captchaKey'])

const inputCode = ref('')
const captchaKey = ref('')
const captchaImage = ref('')
const loading = ref(false)

// 获取验证码
const fetchCaptcha = async () => {
  loading.value = true
  try {
    const res = await request.get('/captcha')
    if (res.code === 200 && res.data) {
      captchaKey.value = res.data.captchaKey
      captchaImage.value = res.data.captchaImage
      emit('update:captchaKey', captchaKey.value)
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  inputCode.value = ''
  emit('update:modelValue', '')
  fetchCaptcha()
}

// 处理输入
const handleInput = (value) => {
  // 只允许输入数字
  const numValue = value.replace(/\D/g, '')
  inputCode.value = numValue
  emit('update:modelValue', numValue)
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal !== inputCode.value) {
    inputCode.value = newVal
  }
})

// 组件挂载时获取验证码
onMounted(() => {
  fetchCaptcha()
})

// 暴露刷新方法供父组件调用
defineExpose({
  refreshCaptcha
})
</script>

<style scoped>
.captcha-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-input .el-input {
  flex: 1;
}

.captcha-image-wrapper {
  width: 120px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--el-border-color);
  transition: border-color 0.2s;
  flex-shrink: 0;
}

.captcha-image-wrapper:hover {
  border-color: var(--el-color-primary);
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.captcha-loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  gap: 4px;
}

.captcha-loading .el-icon {
  font-size: 14px;
}
</style>
