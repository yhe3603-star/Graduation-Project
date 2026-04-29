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

function getTokenExpiryTime(token) {
  if (!token) return null
  const payload = decodeJwtPayload(token)
  if (!payload || !payload.exp) return null
  return payload.exp * 1000
}

function isTokenExpired(token, options = {}) {
  const { useBuffer = true, bufferMs = TOKEN_EXPIRY_BUFFER_MS } = options
  const expiryTime = getTokenExpiryTime(token)
  if (!expiryTime) return false
  
  const now = Date.now()
  return useBuffer ? now >= expiryTime - bufferMs : now >= expiryTime
}

function getTokenRemainingTime(token) {
  const expiryTime = getTokenExpiryTime(token)
  if (!expiryTime) return 0
  return Math.max(0, expiryTime - Date.now())
}

function safeSetItem(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch {
    console.warn('localStorage not available, falling back to memory')
  }
}

function safeGetItem(key) {
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

function safeRemoveItem(key) {
  try {
    localStorage.removeItem(key)
  } catch {
    // ignore
  }
}

/**
 * @typedef {Object} AuthData
 * @property {string} token - 认证令牌
 * @property {string|number} [id] - 用户ID
 * @property {string} [username] - 用户名
 * @property {string} [role] - 用户角色
 */

/**
 * @typedef {Object} LoginResult
 * @property {boolean} success - 是否登录成功
 * @property {AuthData} [data] - 登录成功时返回的认证数据
 * @property {string} [message] - 登录失败时的错误信息
 */

/**
 * @typedef {Object} PasswordChangeResult
 * @property {boolean} success - 是否修改成功
 * @property {string} [message] - 结果信息
 */

/**
 * @typedef {Object} UserInfo
 * @property {string|number} id - 用户ID
 * @property {string} username - 用户名
 * @property {string} role - 用户角色
 * @property {string} [avatar] - 用户头像URL
 * @property {string} [email] - 用户邮箱
 */

export const useUserStore = defineStore('user', () => {
  const token = ref(safeGetItem('token') || '')
  const userId = ref(safeGetItem('userId') || '')
  const username = ref(safeGetItem('userName') || '')
  const role = ref(safeGetItem('role') || '')
  const userInfo = ref(null)
  
  const isLoggedIn = computed(() => {
    if (!token.value) return false
    const payload = decodeJwtPayload(token.value)
    if (!payload) return true
    return !isTokenExpired(token.value, { useBuffer: false })
  })
  const userName = computed(() => username.value)
  const isAdmin = computed(() => {
    if (!token.value) return false
    const payload = decodeJwtPayload(token.value)
    if (!payload) {
      const r = role.value
      return !!(r && r.toLowerCase() === 'admin')
    }
    if (isTokenExpired(token.value, { useBuffer: false })) return false
    const r = role.value
    return !!(r && r.toLowerCase() === 'admin')
  })
  
  /**
   * 初始化用户状态，从存储恢复认证信息并检查令牌有效期
   * @returns {void}
   */
  function initialize() {
    initializeFromStorage()
    checkTokenExpiry()
    
    window.addEventListener('auth-expired', handleAuthExpired)
  }

  function handleAuthExpired() {
    clearAuth()
  }

  function destroy() {
    window.removeEventListener('auth-expired', handleAuthExpired)
  }
  
  function checkTokenExpiry() {
    if (token.value) {
      const payload = decodeJwtPayload(token.value)
      if (payload && isTokenExpired(token.value)) {
        clearAuth()
      }
    }
  }
  
  /**
   * 设置认证数据，将令牌和用户信息写入响应式状态和 sessionStorage
   * @param {AuthData} data - 认证数据对象
   * @returns {void}
   */
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
  
  /**
   * 清除认证数据，重置所有用户状态和 sessionStorage
   * @returns {void}
   */
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
  
  /**
   * 获取当前登录用户的详细信息
   * @returns {Promise<UserInfo|null>} 用户信息对象，未登录或请求失败时返回 null
   */
  async function fetchUserInfo() {
    if (!token.value) {
      clearAuth()
      return null
    }
    
    const payload = decodeJwtPayload(token.value)
    if (payload && isTokenExpired(token.value)) {
      clearAuth()
      return null
    }
    
    try {
      const res = await request.get('/user/me', { skipAuthRefresh: true })
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
  
  /**
   * 验证当前令牌是否有效，有效时同步更新用户信息
   * @returns {Promise<boolean>} 令牌是否有效
   */
  async function validateToken() {
    if (!token.value) {
      clearAuth()
      return false
    }

    const payload = decodeJwtPayload(token.value)
    if (payload && isTokenExpired(token.value, { useBuffer: false })) {
      clearAuth()
      return false
    }

    try {
      const res = await request.get('/user/validate', { skipAuthRefresh: true })
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
  
  /**
   * 用户登录
   * @param {Object} loginData - 登录表单数据
   * @param {string} loginData.username - 用户名
   * @param {string} loginData.password - 密码
   * @returns {Promise<LoginResult>} 登录结果
   */
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
  
  /**
   * 用户退出登录，清除本地认证数据
   * @returns {Promise<void>}
   */
  async function logout() {
    try {
      await request.post('/user/logout', {}, { skipAuthRefresh: true })
    } catch (error) {
      console.debug('退出登录请求失败:', error)
    } finally {
      clearAuth()
    }
  }
  
  /**
   * 修改用户密码，成功后自动清除认证数据需要重新登录
   * @param {Object} data - 修改密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise<PasswordChangeResult>} 修改结果
   */
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
  
  /**
   * 从 sessionStorage 恢复认证状态，令牌过期时自动清除
   * @returns {void}
   */
  function initializeFromStorage() {
    const storedToken = safeGetItem('token')
    const storedUserId = safeGetItem('userId')
    const storedUsername = safeGetItem('userName')
    const storedRole = safeGetItem('role')
    
    if (storedToken) {
      const payload = decodeJwtPayload(storedToken)
      if (!payload || !isTokenExpired(storedToken, { useBuffer: false })) {
        token.value = storedToken
        userId.value = storedUserId || ''
        username.value = storedUsername || ''
        role.value = storedRole || ''
      } else {
        clearAuth()
      }
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
    getTokenRemainingTime,
    destroy
  }
})
