<template>
  <div class="settings-container">
    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon><span>基本信息</span>
        </div>
      </template>
      <el-form label-width="80px" class="settings-form">
        <el-form-item label="用户名">
          <el-input :value="userName" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :value="isAdmin ? '管理员' : '普通用户'" disabled />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon><span>修改密码</span>
        </div>
      </template>
      <el-form ref="formRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="settings-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="currentPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item label="验证码" prop="captchaCode">
          <CaptchaInput
            ref="captchaRef"
            v-model="captchaCode"
            @update:captcha-key="$emit('update:captcha-key', $event)"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="$emit('change-password')">
确认修改
</el-button>
          <el-button @click="$emit('reset-password')">
重置
</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="settings-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><SwitchButton /></el-icon><span>退出登录</span>
        </div>
      </template>
      <div class="logout-section">
        <p class="logout-tip">
点击下方按钮将退出当前账号，退出后需要重新登录才能访问个人功能。
</p>
        <el-button type="danger" :loading="logoutLoading" @click="$emit('logout')">
          <el-icon><SwitchButton /></el-icon>退出登录
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { User, Lock, SwitchButton } from '@element-plus/icons-vue'
import CaptchaInput from '@/components/business/interact/CaptchaInput.vue'

const formRef = ref(null)
const captchaRef = ref(null)
defineExpose({ formRef, captchaRef })

const props = defineProps({
  userName: { type: String, default: '' },
  isAdmin: { type: Boolean, default: false },
  passwordForm: { type: Object, required: true },
  passwordRules: { type: Object, required: true },
  passwordLoading: { type: Boolean, default: false },
  logoutLoading: { type: Boolean, default: false }
})

defineEmits(['change-password', 'reset-password', 'logout', 'update:captcha-key'])

/* eslint-disable vue/no-mutating-props */
const currentPassword = computed({
  get: () => props.passwordForm.currentPassword,
  set: (v) => { props.passwordForm.currentPassword = v }
})
const newPassword = computed({
  get: () => props.passwordForm.newPassword,
  set: (v) => { props.passwordForm.newPassword = v }
})
const confirmPassword = computed({
  get: () => props.passwordForm.confirmPassword,
  set: (v) => { props.passwordForm.confirmPassword = v }
})
const captchaCode = computed({
  get: () => props.passwordForm.captchaCode,
  set: (v) => { props.passwordForm.captchaCode = v }
})
/* eslint-enable vue/no-mutating-props */
</script>

<style scoped>
.settings-container { display: flex; flex-direction: column; gap: var(--space-xl); }
.settings-card { border-radius: var(--radius-md); }
.card-header { display: flex; align-items: center; gap: var(--space-sm); font-weight: var(--font-weight-semibold); color: var(--dong-indigo); }
.settings-form { max-width: 400px; }
.logout-section { text-align: center; }
.logout-tip { color: var(--text-muted); font-size: var(--font-size-sm); margin-bottom: var(--space-lg); }

@media (max-width: 768px) { .settings-form { max-width: 100%; } }
@media (max-width: 480px) { .settings-form :deep(.el-form-item__label) { font-size: var(--font-size-sm); } }
</style>
