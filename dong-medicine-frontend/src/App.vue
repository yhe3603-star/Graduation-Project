<template>
  <div
    class="dong-app"
    :class="{ 'admin-layout': isAdminPage }"
  >
    <AppHeader
      v-if="!isAdminPage && !isNotFoundPage"
      :is-logged-in="isLoggedIn"
      :user-name="userName"
      :is-admin="isAdmin"
      @logout="logout"
      @show-login="showLoginDialog = true"
      @show-register="showRegisterDialog = true"
    />

    <main class="dong-main">
      <router-view v-slot="{ Component }">
        <transition
          name="page-fade"
          mode="out-in"
        >
          <ErrorBoundary>
            <component :is="Component" />
          </ErrorBoundary>
        </transition>
      </router-view>
    </main>

    <AppFooter v-if="!isAdminPage && !isNotFoundPage" />

    <el-dialog
      v-model="showLoginDialog"
      title="登录"
      width="400px"
      :close-on-click-modal="false"
      @closed="resetLoginForm"
    >
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="User"
            placeholder="用户名"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            prefix-icon="Lock"
            placeholder="密码"
            show-password
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <CaptchaInput
            ref="loginCaptchaRef"
            v-model="loginForm.captchaCode"
            @update:captcha-key="loginForm.captchaKey = $event"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="loginLoading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showRegisterDialog"
      title="注册"
      width="400px"
      :close-on-click-modal="false"
      @closed="resetRegisterForm"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="0"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            prefix-icon="User"
            placeholder="用户名"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            prefix-icon="Lock"
            placeholder="密码"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            prefix-icon="Lock"
            placeholder="确认密码"
            show-password
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <CaptchaInput
            ref="registerCaptchaRef"
            v-model="registerForm.captchaCode"
            @update:captcha-key="registerForm.captchaKey = $event"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="registerLoading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, provide, onMounted, computed, watch } from "vue"
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from "element-plus"
import AppHeader from "@/components/business/layout/AppHeader.vue"
import AppFooter from "@/components/business/layout/AppFooter.vue"
import ErrorBoundary from "@/components/base/ErrorBoundary.vue"
import CaptchaInput from "@/components/business/interact/CaptchaInput.vue"
import request from "@/utils/request"
import { useUserStore } from "@/stores/user"
import { logFetchError } from "@/utils"

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userName = computed(() => userStore.userName)
const isAdmin = computed(() => userStore.isAdmin)
const isAdminPage = computed(() => route.path === "/admin")
const isNotFoundPage = computed(() => route.name === "NotFound")

onMounted(() => {
  userStore.initialize()
})

watch(() => userStore.isLoggedIn, (newVal) => {
  if (!newVal && route.meta.requiresAuth) {
    router.push("/")
  }
})

provide("request", request)
provide("isLoggedIn", isLoggedIn)
provide("userName", userName)
provide("updateUserState", () => userStore.initialize())

const showLoginDialog = ref(false)
const showRegisterDialog = ref(false)

provide("showLoginDialog", () => { showLoginDialog.value = true })

const loginLoading = ref(false)
const registerLoading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loginCaptchaRef = ref(null)
const registerCaptchaRef = ref(null)
const loginForm = ref({ username: "", password: "", captchaKey: "", captchaCode: "" })
const registerForm = ref({ username: "", password: "", confirmPassword: "", captchaKey: "", captchaCode: "" })

const loginRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  captchaCode: [{ required: true, message: "请输入验证码", trigger: "blur" }]
}
const registerRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }, { min: 3, max: 20, message: "用户名长度3-20个字符", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" }, 
    { min: 8, max: 50, message: "密码长度为8-50位", trigger: "blur" },
    { 
      validator: (_, v, cb) => {
        if (!/[a-zA-Z]/.test(v)) cb(new Error("密码必须包含字母"))
        else if (!/[0-9]/.test(v)) cb(new Error("密码必须包含数字"))
        else if (/\s/.test(v)) cb(new Error("密码不能包含空格"))
        else cb()
      },
      trigger: "blur"
    }
  ],
  confirmPassword: [{ required: true, message: "请确认密码", trigger: "blur" }, {
    validator: (_, v, cb) => v !== registerForm.value.password ? cb(new Error("两次密码不一致")) : cb(),
    trigger: "blur"
  }],
  captchaCode: [{ required: true, message: "请输入验证码", trigger: "blur" }]
}

const resetLoginForm = () => {
  loginForm.value = { username: "", password: "", captchaKey: "", captchaCode: "" }
  loginFormRef.value?.resetFields()
  loginCaptchaRef.value?.refreshCaptcha()
}

const resetRegisterForm = () => {
  registerForm.value = { username: "", password: "", confirmPassword: "", captchaKey: "", captchaCode: "" }
  registerFormRef.value?.resetFields()
  registerCaptchaRef.value?.refreshCaptcha()
}

const handleLogin = async () => {
  if (!await loginFormRef.value?.validate()) return
  loginLoading.value = true
  try {
    const res = await request.post("/user/login", loginForm.value)
    if (res.code === 200 && res.data) {
      userStore.setAuth(res.data)
      ElMessage.success("登录成功")
      showLoginDialog.value = false
      loginForm.value = { username: "", password: "", captchaKey: "", captchaCode: "" }
    } else {
      ElMessage.error(res.msg || "登录失败")
      loginCaptchaRef.value?.refreshCaptcha()
    }
  } catch (e) {
    logFetchError('登录', e)
    console.log('Login error:', e)
    ElMessage.error(e.msg || e.message || "登录失败，请重试")
    loginCaptchaRef.value?.refreshCaptcha()
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  if (!await registerFormRef.value?.validate()) return
  registerLoading.value = true
  try {
    await request.post("/user/register", { 
      username: registerForm.value.username, 
      password: registerForm.value.password,
      confirmPassword: registerForm.value.confirmPassword,
      captchaKey: registerForm.value.captchaKey,
      captchaCode: registerForm.value.captchaCode
    })
    ElMessage.success("注册成功，请登录")
    showRegisterDialog.value = false
    showLoginDialog.value = true
    loginForm.value.username = registerForm.value.username
    registerForm.value = { username: "", password: "", confirmPassword: "", captchaKey: "", captchaCode: "" }
  } catch (e) {
    logFetchError('注册', e)
    ElMessage.error(e.msg || "注册失败，请重试")
    registerCaptchaRef.value?.refreshCaptcha()
  } finally {
    registerLoading.value = false
  }
}

const logout = async () => {
  try {
    await userStore.logout()
    ElMessage.success("已退出登录")
  } catch (e) {
    console.debug('退出登录失败:', e)
    ElMessage.success("已退出登录")
  }
  router.push("/")
}
</script>

<style>
:root { --dong-blue: #1A5276; --dong-green: #28B463; --dong-light: var(--bg-rice); }
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: "Microsoft YaHei", "PingFang SC", sans-serif; background: var(--dong-light); }
.dong-app { min-height: 100vh; display: flex; flex-direction: column; }
.dong-app.admin-layout { background: #f5f7fa; }
.dong-main { flex: 1; }
.page-fade-enter-active, .page-fade-leave-active { transition: opacity 0.2s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }
</style>
