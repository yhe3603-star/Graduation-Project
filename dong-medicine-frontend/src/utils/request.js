import axios from "axios"
import { ElMessage } from "element-plus"
import { logAuthWarn, logSecurityWarn } from '@/utils/logger'
import { sanitize, containsXss, containsSqlInjection, sanitizeForLog } from "./xss"

const pendingRequests = new Map()
let refreshPromise = null
let refreshSubscribers = []

function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

function onRefreshed(token) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

function onRefreshFailed() {
  refreshSubscribers.forEach(callback => callback(null))
  refreshSubscribers = []
  const keysToRemove = ["token", "userId", "userName", "role"]
  keysToRemove.forEach(key => {
    try {
      sessionStorage.removeItem(key)
    } catch {
      // ignore
    }
  })
  window.dispatchEvent(new CustomEvent('auth-expired'))
}

function generateRequestKey(config) {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

function addPendingRequest(config) {
  if (config.cancelToken) return
  
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    const cancel = pendingRequests.get(key)
    cancel(`请求被取消: ${config.url}`)
  }
  
  config.cancelToken = new axios.CancelToken(cancel => {
    if (!pendingRequests.has(key)) {
      pendingRequests.set(key, cancel)
    }
  })
}

function removePendingRequest(config) {
  const key = generateRequestKey(config)
  if (pendingRequests.has(key)) {
    pendingRequests.delete(key)
  }
}

/**
 * @typedef {Object} ApiResponse
 * @property {number} code - 响应状态码（200表示成功）
 * @property {string} msg - 响应消息
 * @property {*} data - 响应数据
 */

/**
 * @typedef {Object} RequestConfig
 * @property {boolean} [skipAuthRefresh=false] - 是否跳过令牌自动刷新
 * @property {boolean} [enableCancel=true] - 是否启用请求取消（重复请求自动取消）
 * @property {number} [__retryCount=0] - 当前重试次数（内部使用）
 */

/**
 * 取消所有待处理请求
 * @returns {void}
 */
export function cancelAllRequests() {
  pendingRequests.forEach((cancel, key) => {
    cancel('请求被取消: 页面切换')
  })
  pendingRequests.clear()
}

/**
 * 根据URL取消匹配的待处理请求
 * @param {string} url - 需要取消的请求URL
 * @returns {void}
 */
export function cancelRequestByUrl(url) {
  pendingRequests.forEach((cancel, key) => {
    if (key.includes(url)) {
      cancel(`请求被取消: ${url}`)
      pendingRequests.delete(key)
    }
  })
}

const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000,
  retryableStatuses: [408, 429, 500, 502, 503, 504],
  retryableMethods: ['get', 'head', 'options']
}

function shouldRetry(error, config) {
  if (!config) return false
  
  const retryCount = config.__retryCount || 0
  if (retryCount >= RETRY_CONFIG.maxRetries) return false
  
  const method = config.method?.toLowerCase()
  if (!RETRY_CONFIG.retryableMethods.includes(method)) return false
  
  if (axios.isCancel(error)) return false
  
  const status = error.response?.status
  if (status && RETRY_CONFIG.retryableStatuses.includes(status)) return true
  
  if (!error.response && error.code === 'ECONNABORTED') return true
  if (!error.response && error.message?.includes('timeout')) return true
  if (!error.response && error.message?.includes('Network Error')) return true
  
  return false
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function getToken() {
  try {
    return sessionStorage.getItem("token")
  } catch {
    return null
  }
}

function setToken(token) {
  try {
    sessionStorage.setItem("token", token)
  } catch {
    // ignore
  }
}

function setAuthData(data) {
  try {
    sessionStorage.setItem("token", data.token)
    sessionStorage.setItem("userId", data.id)
    sessionStorage.setItem("userName", data.username)
    sessionStorage.setItem("role", data.role)
  } catch {
    // ignore
  }
}

async function getOrRefreshToken() {
  if (refreshPromise) {
    return refreshPromise
  }
  
  refreshPromise = refreshToken()
  
  try {
    const result = await refreshPromise
    return result
  } finally {
    refreshPromise = null
  }
}

async function refreshToken() {
  const token = getToken()
  if (!token) return null
  
  try {
    const baseURL = import.meta.env.VITE_API_BASE_URL || "/api"
    const response = await axios.post(
      baseURL + "/user/refresh-token",
      {},
      { headers: { Authorization: "Bearer " + token } }
    )
    
    if (response.data?.code === 200 && response.data?.data?.token) {
      setAuthData(response.data.data)
      return response.data.data.token
    }
    onRefreshFailed()
    return null
  } catch (error) {
    onRefreshFailed()
    return null
  }
}

/**
 * Axios 请求实例，封装了认证、重试、防重复提交、XSS防护等拦截器
 * 
 * 支持的请求方法：
 * - request.get<T>(url, config?): Promise<T> - GET 请求
 * - request.post<T>(url, data?, config?): Promise<T> - POST 请求
 * - request.put<T>(url, data?, config?): Promise<T> - PUT 请求
 * - request.delete<T>(url, config?): Promise<T> - DELETE 请求
 * - request.patch<T>(url, data?, config?): Promise<T> - PATCH 请求
 * 
 * @type {import('axios').AxiosInstance}
 * 
 * @example
 * // GET 请求
 * const data = await request.get('/api/plants')
 * 
 * @example
 * // POST 请求
 * const result = await request.post('/user/login', { username, password })
 * 
 * @example
 * // 带配置的请求（跳过令牌刷新）
 * await request.post('/user/logout', {}, { skipAuthRefresh: true })
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 60000
})

request.interceptors.request.use((config) => {
  if (!config.headers.Authorization) {
    const token = getToken()
    if (token) config.headers.Authorization = "Bearer " + token
  }
  
  if (config.data && typeof config.data === 'object') {
    config.data = sanitizeRequestData(config.data)
  }
  if (config.params && typeof config.params === 'object') {
    config.params = sanitizeRequestData(config.params)
  }

  if (config.enableCancel !== false) {
    addPendingRequest(config)
  }

  config.__retryCount = config.__retryCount || 0
  config._skipAuthRefresh = config.skipAuthRefresh || false

  return config
})

request.interceptors.response.use(
  (res) => {
    removePendingRequest(res.config)
    
    if (res.data?.code !== undefined && res.data.code !== 200) {
      console.log('Response error:', res.data)
      return Promise.reject(res.data)
    }
    return res.data
  },
  async (err) => {
    const config = err.config
    
    removePendingRequest(config)
    
    if (shouldRetry(err, config)) {
      config.__retryCount += 1
      const delay = RETRY_CONFIG.retryDelay * Math.pow(2, config.__retryCount - 1)
      
      console.log(`请求重试 (${config.__retryCount}/${RETRY_CONFIG.maxRetries}): ${config.url}`)
      await sleep(delay)
      
      return request(config)
    }

    const status = err.response?.status
    const msg = err.response?.data?.msg || err.message || "网络错误"

    if (axios.isCancel(err)) {
      console.log('请求被取消:', err.message)
      return Promise.reject(err)
    }

    if (status === 403) {
      const token = getToken()
      
      if (token && !config._retry) {
        config._retry = true
        
        const newToken = await getOrRefreshToken()
        
        if (newToken) {
          config.headers.Authorization = "Bearer " + newToken
          return request(config)
        }
      }
      
      onRefreshFailed()
      logAuthWarn(msg)
      ElMessage.warning("权限不足或登录已过期，请重新登录")
      return Promise.reject(err.response?.data || err)
    }

    if (status === 401) {
      if (config._skipAuthRefresh) {
        return Promise.reject(err.response?.data || err)
      }
      
      const token = getToken()
      
      if (token && !config._retry) {
        config._retry = true
        
        const newToken = await getOrRefreshToken()
        
        if (newToken) {
          onRefreshed(newToken)
          config.headers.Authorization = "Bearer " + newToken
          return request(config)
        } else {
          onRefreshFailed()
          ElMessage.warning("登录已过期，请重新登录")
          return Promise.reject(err)
        }
      }
      
      ["token", "userId", "userName", "role"].forEach(key => {
        try {
          sessionStorage.removeItem(key)
        } catch {
          // ignore
        }
      })
      ElMessage.warning("登录已过期，请重新登录")
      return Promise.reject(err.response?.data || err)
    }

    showErrorMessage(status, msg, err)
    return Promise.reject(err.response?.data || err)
  }
)

function showErrorMessage(status, msg, err) {
  const errorMessages = {
    400: "请求参数有误，请检查输入",
    404: "请求的资源不存在",
    405: "请求方法不允许",
    408: "请求超时，请稍后重试",
    409: "资源冲突，请刷新后重试",
    413: "上传文件过大，请压缩后重试",
    422: "提交数据验证失败",
    429: "请求过于频繁，请稍后重试",
    500: "服务器内部错误，请稍后重试",
    502: "网关错误，请稍后重试",
    503: "服务暂时不可用，请稍后重试",
    504: "网关超时，请稍后重试"
  }
  
  if (status === 429) {
    ElMessage.warning("操作过于频繁，请稍后再试")
    return
  }
  
  const friendlyMsg = errorMessages[status]
  if (friendlyMsg) {
    ElMessage.error(friendlyMsg)
  } else if (msg && msg !== "Network Error" && msg !== "Unknown Error") {
    ElMessage.error(msg)
  } else {
    ElMessage.error("网络异常，请检查网络连接")
  }
}

function sanitizeRequestData(data) {
  if (!data || typeof data !== 'object') return data

  const sanitized = Array.isArray(data) ? [] : {}

  for (const [key, value] of Object.entries(data)) {
    if (typeof value === 'string') {
      if (containsXss(value) || containsSqlInjection(value)) {
        logSecurityWarn(key, sanitizeForLog(value))
        sanitized[key] = sanitize(value)
      } else {
        sanitized[key] = value
      }
    } else if (typeof value === 'object' && value !== null) {
      sanitized[key] = sanitizeRequestData(value)
    } else {
      sanitized[key] = value
    }
  }

  return sanitized
}

export default request
