/**
 * 数据缓存工具
 * 
 * 提供内存缓存和 sessionStorage 缓存两层缓存机制
 * - 内存缓存：快速访问，页面刷新后失效
 * - sessionStorage 缓存：持久化到会话结束
 */

const memoryCache = new Map()

const DEFAULT_TTL = 5 * 60 * 1000

const CACHE_PREFIX = 'dong_medicine_cache_'

let cleanupInterval = null

const cacheConfig = {
  plants: { ttl: 10 * 60 * 1000, storage: 'session' },
  knowledge: { ttl: 10 * 60 * 1000, storage: 'session' },
  inheritors: { ttl: 10 * 60 * 1000, storage: 'session' },
  resources: { ttl: 5 * 60 * 1000, storage: 'session' },
  categories: { ttl: 30 * 60 * 1000, storage: 'session' },
  quizQuestions: { ttl: 5 * 60 * 1000, storage: 'memory' },
  leaderboard: { ttl: 2 * 60 * 1000, storage: 'memory' },
  userInfo: { ttl: 30 * 60 * 1000, storage: 'session' }
}

function getStorageKey(key) {
  return CACHE_PREFIX + key
}

function isExpired(item) {
  if (!item || !item.expireAt) return true
  return Date.now() > item.expireAt
}

function getFromMemory(key) {
  const item = memoryCache.get(key)
  if (!item) return null
  if (isExpired(item)) {
    memoryCache.delete(key)
    return null
  }
  return item.data
}

function setToMemory(key, data, ttl) {
  memoryCache.set(key, {
    data,
    expireAt: Date.now() + ttl
  })
}

function getFromSession(key) {
  try {
    const storageKey = getStorageKey(key)
    const item = sessionStorage.getItem(storageKey)
    if (!item) return null
    const parsed = JSON.parse(item)
    if (isExpired(parsed)) {
      sessionStorage.removeItem(storageKey)
      return null
    }
    return parsed.data
  } catch (e) {
    console.warn('SessionStorage 缓存读取失败:', e)
    return null
  }
}

function setToSession(key, data, ttl) {
  try {
    const storageKey = getStorageKey(key)
    sessionStorage.setItem(storageKey, JSON.stringify({
      data,
      expireAt: Date.now() + ttl
    }))
  } catch (e) {
    console.warn('SessionStorage 缓存写入失败:', e)
  }
}

export const cache = {
  get(key, cacheType = 'memory') {
    if (cacheType === 'session') {
      const sessionData = getFromSession(key)
      if (sessionData !== null) return sessionData
    }
    return getFromMemory(key)
  },

  set(key, data, ttl = DEFAULT_TTL, storage = 'memory') {
    setToMemory(key, data, ttl)
    if (storage === 'session') {
      setToSession(key, data, ttl)
    }
  },

  getWithConfig(key, configKey) {
    const config = cacheConfig[configKey]
    if (!config) return this.get(key)
    return this.get(key, config.storage)
  },

  setWithConfig(key, data, configKey) {
    const config = cacheConfig[configKey]
    if (!config) {
      this.set(key, data)
      return
    }
    this.set(key, data, config.ttl, config.storage)
  },

  remove(key) {
    memoryCache.delete(key)
    try {
      sessionStorage.removeItem(getStorageKey(key))
    } catch (e) {
      console.warn('SessionStorage 缓存删除失败:', e)
    }
  },

  clear() {
    memoryCache.clear()
    try {
      const keysToRemove = []
      for (let i = 0; i < sessionStorage.length; i++) {
        const key = sessionStorage.key(i)
        if (key && key.startsWith(CACHE_PREFIX)) {
          keysToRemove.push(key)
        }
      }
      keysToRemove.forEach(key => sessionStorage.removeItem(key))
    } catch (e) {
      console.warn('SessionStorage 缓存清空失败:', e)
    }
  },

  clearExpired() {
    for (const [key, item] of memoryCache.entries()) {
      if (isExpired(item)) {
        memoryCache.delete(key)
      }
    }
    try {
      const keysToRemove = []
      for (let i = 0; i < sessionStorage.length; i++) {
        const key = sessionStorage.key(i)
        if (key && key.startsWith(CACHE_PREFIX)) {
          const item = sessionStorage.getItem(key)
          if (item) {
            try {
              const parsed = JSON.parse(item)
              if (isExpired(parsed)) {
                keysToRemove.push(key)
              }
            } catch (e) {
              console.warn('解析缓存项失败:', key, e)
              keysToRemove.push(key)
            }
          }
        }
      }
      keysToRemove.forEach(key => sessionStorage.removeItem(key))
    } catch (e) {
      console.warn('清理过期缓存失败:', e)
    }
  }
}

export function createCachedFetcher(fetcher, configKey) {
  return async function cachedFetch(...args) {
    const cacheKey = `${configKey}_${JSON.stringify(args)}`
    const cachedData = cache.getWithConfig(cacheKey, configKey)
    if (cachedData !== null) {
      return cachedData
    }
    const data = await fetcher(...args)
    cache.setWithConfig(cacheKey, data, configKey)
    return data
  }
}

export const startCacheCleanup = () => {
  if (!cleanupInterval) {
    cleanupInterval = setInterval(() => cache.clearExpired(), 60 * 1000)
  }
}

export const stopCacheCleanup = () => {
  if (cleanupInterval) {
    clearInterval(cleanupInterval)
    cleanupInterval = null
  }
}

startCacheCleanup()

export default cache
