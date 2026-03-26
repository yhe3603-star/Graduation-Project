import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

const TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000

function decodeJwtPayload(token) {
  try {
    const base64Url = token.split('.')[1]
    if (!base64Url) return null
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch {
    return null
  }
}

function isTokenExpired(token, useBuffer = true) {
  if (!token) return true
  const payload = decodeJwtPayload(token)
  if (!payload || !payload.exp) return true
  const expiryTime = payload.exp * 1000
  if (!useBuffer) {
    return Date.now() >= expiryTime
  }
  return Date.now() >= expiryTime - TOKEN_EXPIRY_BUFFER_MS
}

/** 仅按 JWT exp 判断，用于服务端校验前；避免 5 分钟缓冲导致误判为过期 */
function isJwtExpiredStrict(token) {
  if (!token) return true
  const payload = decodeJwtPayload(token)
  if (!payload || !payload.exp) return true
  return Date.now() >= payload.exp * 1000
}

function getTokenRemainingTime(token) {
  if (!token) return 0
  const payload = decodeJwtPayload(token)
  if (!payload || !payload.exp) return 0
  return Math.max(0, payload.exp * 1000 - Date.now())
}

function safeSetItem(key, value) {
  try {
    sessionStorage.setItem(key, value)
  } catch {
    console.warn('sessionStorage not available, falling back to memory')
  }
}

function safeGetItem(key) {
  try {
    return sessionStorage.getItem(key)
  } catch {
    return null
  }
}

function safeRemoveItem(key) {
  try {
    sessionStorage.removeItem(key)
  } catch {
    // ignore
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(safeGetItem('token') || '')
  const userId = ref(safeGetItem('userId') || '')
  const username = ref(safeGetItem('userName') || '')
  const role = ref(safeGetItem('role') || '')
  const userInfo = ref(null)
  
  const isLoggedIn = computed(() => {
    if (!token.value) return false
    return !isTokenExpired(token.value, false)
  })
  const userName = computed(() => username.value)
  const isAdmin = computed(() => {
    if (!token.value || isTokenExpired(token.value, false)) return false
    const r = role.value
    return !!(r && r.toLowerCase() === 'admin')
  })
  
  function initialize() {
    initializeFromStorage()
    checkTokenExpiry()
    
    window.addEventListener('auth-expired', () => {
      clearAuth()
    })
  }
  
  function checkTokenExpiry() {
    if (token.value && isTokenExpired(token.value)) {
      clearAuth()
    }
  }
  
  function setAuth(data) {
    token.value = data.token || ''
    userId.value = data.id || ''
    username.value = data.username || ''
    role.value = data.role || 'user'
    
    safeSetItem('token', token.value)
    safeSetItem('userId', userId.value)
    safeSetItem('userName', username.value)
    safeSetItem('role', role.value)
  }
  
  function clearAuth() {
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    userInfo.value = null
    
    safeRemoveItem('token')
    safeRemoveItem('userId')
    safeRemoveItem('userName')
    safeRemoveItem('role')
  }
  
  async function fetchUserInfo() {
    if (!token.value || isTokenExpired(token.value)) {
      clearAuth()
      return null
    }
    
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

    if (isJwtExpiredStrict(token.value)) {
      clearAuth()
      return false
    }

    try {
      const res = await request.get('/user/validate')
      if (res.code === 200 && res.data) {
        userId.value = res.data.id
        username.value = res.data.username
        role.value = res.data.role || 'user'
        
        safeSetItem('userId', userId.value)
        safeSetItem('userName', username.value)
        safeSetItem('role', role.value)
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
    const storedToken = safeGetItem('token')
    const storedUserId = safeGetItem('userId')
    const storedUsername = safeGetItem('userName')
    const storedRole = safeGetItem('role')
    
    if (storedToken && !isTokenExpired(storedToken, false)) {
      token.value = storedToken
      userId.value = storedUserId || ''
      username.value = storedUsername || ''
      role.value = storedRole || ''
    } else {
      clearAuth()
    }
  }
  
  return {
    token,
    userId,
    username,
    userName,
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
    initialize,
    initializeFromStorage,
    getTokenRemainingTime
  }
})
