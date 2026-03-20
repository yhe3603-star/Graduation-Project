import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('userName') || '')
  const role = ref(localStorage.getItem('role') || '')
  const userInfo = ref(null)
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value && role.value.toUpperCase() === 'ADMIN')
  
  function setAuth(data) {
    token.value = data.token || ''
    userId.value = data.id || ''
    username.value = data.username || ''
    role.value = data.role || 'user'
    
    localStorage.setItem('token', token.value)
    localStorage.setItem('userId', userId.value)
    localStorage.setItem('userName', username.value)
    localStorage.setItem('role', role.value)
  }
  
  function clearAuth() {
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    userInfo.value = null
    
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
    localStorage.removeItem('role')
  }
  
  async function fetchUserInfo() {
    if (!token.value) return null
    
    try {
      const res = await request.get('/user/me')
      if (res.code === 200) {
        userInfo.value = res.data
        return res.data
      }
      return null
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }
  
  async function validateToken() {
    if (!token.value) {
      clearAuth()
      return false
    }
    
    try {
      const res = await request.get('/user/validate')
      if (res.code === 200 && res.data) {
        userId.value = res.data.id
        username.value = res.data.username
        role.value = res.data.role || 'user'
        
        localStorage.setItem('userId', userId.value)
        localStorage.setItem('userName', username.value)
        localStorage.setItem('role', role.value)
        return true
      }
      clearAuth()
      return false
    } catch (error) {
      clearAuth()
      return false
    }
  }
  
  async function login(loginData) {
    try {
      const res = await request.post('/user/login', loginData)
      if (res.code === 200 && res.data) {
        setAuth(res.data)
        return { success: true, data: res.data }
      }
      return { success: false, message: res.msg || '登录失败' }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }
  
  async function logout() {
    try {
      await request.post('/user/logout')
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      clearAuth()
    }
  }
  
  async function changePassword(data) {
    try {
      const res = await request.post('/user/change-password', data)
      if (res.code === 200) {
        clearAuth()
        return { success: true, message: res.data || '密码修改成功' }
      }
      return { success: false, message: res.msg || '密码修改失败' }
    } catch (error) {
      return { success: false, message: error.message || '密码修改失败' }
    }
  }
  
  function initializeFromStorage() {
    const storedToken = localStorage.getItem('token')
    const storedUserId = localStorage.getItem('userId')
    const storedUsername = localStorage.getItem('userName')
    const storedRole = localStorage.getItem('role')
    
    if (storedToken) {
      token.value = storedToken
      userId.value = storedUserId || ''
      username.value = storedUsername || ''
      role.value = storedRole || ''
    }
  }
  
  return {
    token,
    userId,
    username,
    role,
    userInfo,
    isLoggedIn,
    isAdmin,
    setAuth,
    clearAuth,
    fetchUserInfo,
    validateToken,
    login,
    logout,
    changePassword,
    initializeFromStorage
  }
})
