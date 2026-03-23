import axios from "axios"
import { ElMessage } from "element-plus"
import { logAuthWarn, logSecurityWarn } from '@/utils/logger'
import { sanitize, containsXss, containsSqlInjection, sanitizeForLog } from "./xss"

const pendingRequests = new Map()
let isRefreshing = false
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

export function cancelAllRequests() {
  pendingRequests.forEach((cancel, key) => {
    cancel('请求被取消: 页面切换')
  })
  pendingRequests.clear()
}

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
    return null
  } catch {
    return null
  }
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 60000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  const userId = sessionStorage.getItem("userId")

  if (token) config.headers.Authorization = "Bearer " + token
  if (userId) config.headers["userId"] = userId

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
            logAuthWarn(msg)
            return Promise.reject(err.response?.data || err)
        }

    if (status === 401) {
      const token = getToken()
      
      if (token && !config._retry) {
        config._retry = true
        
        if (!isRefreshing) {
          isRefreshing = true
          
          try {
            const newToken = await refreshToken()
            
            if (newToken) {
              onRefreshed(newToken)
              config.headers.Authorization = "Bearer " + newToken
              return request(config)
            } else {
              onRefreshFailed()
              ElMessage.warning("登录已过期，请重新登录")
              return Promise.reject(err)
            }
          } finally {
            isRefreshing = false
          }
        } else {
          return new Promise((resolve, reject) => {
            subscribeTokenRefresh((newToken) => {
              if (newToken) {
                config.headers.Authorization = "Bearer " + newToken
                resolve(request(config))
              } else {
                reject(err)
              }
            })
          })
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

    ElMessage.error(msg)
    return Promise.reject(err.response?.data || err)
  }
)

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
