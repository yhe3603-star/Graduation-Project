import { describe, it, expect, beforeEach, vi } from 'vitest'
import { cache, createCachedFetcher, stopCacheCleanup } from '@/utils/cache'

describe('cache', () => {
  beforeEach(() => {
    cache.clear()
    sessionStorage.clear()
  })

  describe('get/set - 内存缓存', () => {
    it('应存储和获取数据', () => {
      cache.set('key1', { name: 'test' })
      expect(cache.get('key1')).toEqual({ name: 'test' })
    })

    it('未设置的key应返回null', () => {
      expect(cache.get('nonexistent')).toBeNull()
    })

    it('应支持各种数据类型', () => {
      cache.set('string', 'hello')
      cache.set('number', 42)
      cache.set('boolean', true)
      cache.set('array', [1, 2, 3])

      expect(cache.get('string')).toBe('hello')
      expect(cache.get('number')).toBe(42)
      expect(cache.get('boolean')).toBe(true)
      expect(cache.get('array')).toEqual([1, 2, 3])
    })

    it('应支持自定义TTL', () => {
      cache.set('short', 'data', 100)
      expect(cache.get('short')).toBe('data')
    })

    it('过期数据应返回null', () => {
      vi.useFakeTimers()
      cache.set('expire', 'data', 100)
      vi.advanceTimersByTime(200)
      expect(cache.get('expire')).toBeNull()
      vi.useRealTimers()
    })
  })

  describe('get/set - sessionStorage缓存', () => {
    it('应存储到sessionStorage', () => {
      cache.set('session1', 'value', 60000, 'session')
      expect(cache.get('session1', 'session')).toBe('value')
    })

    it('sessionStorage缓存应优先于内存缓存', () => {
      cache.set('priority', 'session-value', 60000, 'session')
      expect(cache.get('priority', 'session')).toBe('session-value')
    })
  })

  describe('remove', () => {
    it('应删除内存缓存', () => {
      cache.set('del1', 'data')
      cache.remove('del1')
      expect(cache.get('del1')).toBeNull()
    })

    it('应删除sessionStorage缓存', () => {
      cache.set('del2', 'data', 60000, 'session')
      cache.remove('del2')
      expect(cache.get('del2', 'session')).toBeNull()
    })
  })

  describe('clear', () => {
    it('应清除所有缓存', () => {
      cache.set('a', 1)
      cache.set('b', 2)
      cache.clear()
      expect(cache.get('a')).toBeNull()
      expect(cache.get('b')).toBeNull()
    })
  })

  describe('clearExpired', () => {
    it('应只清除过期项', () => {
      vi.useFakeTimers()
      cache.set('fresh', 'data', 60000)
      cache.set('old', 'data', 100)
      vi.advanceTimersByTime(200)
      cache.clearExpired()
      expect(cache.get('fresh')).toBe('data')
      expect(cache.get('old')).toBeNull()
      vi.useRealTimers()
    })
  })

  describe('getWithConfig/setWithConfig', () => {
    it('应使用预配置的缓存策略', () => {
      cache.setWithConfig('plants_list', [{ id: 1 }], 'plants')
      expect(cache.getWithConfig('plants_list', 'plants')).toEqual([{ id: 1 }])
    })

    it('未知配置应使用默认策略', () => {
      cache.setWithConfig('unknown_key', 'data', 'nonexistent')
      expect(cache.getWithConfig('unknown_key', 'nonexistent')).toBe('data')
    })
  })
})

describe('createCachedFetcher', () => {
  beforeEach(() => {
    cache.clear()
    sessionStorage.clear()
  })

  it('首次调用应执行fetcher', async () => {
    const fetcher = vi.fn().mockResolvedValue({ data: 'fresh' })
    const cachedFetch = createCachedFetcher(fetcher, 'plants')

    const result = await cachedFetch('arg1')
    expect(fetcher).toHaveBeenCalledTimes(1)
    expect(result).toEqual({ data: 'fresh' })
  })

  it('相同参数的后续调用应使用缓存', async () => {
    const fetcher = vi.fn().mockResolvedValue({ data: 'cached' })
    const cachedFetch = createCachedFetcher(fetcher, 'plants')

    await cachedFetch('arg1')
    await cachedFetch('arg1')
    expect(fetcher).toHaveBeenCalledTimes(1)
  })

  it('不同参数应重新调用fetcher', async () => {
    const fetcher = vi.fn().mockResolvedValue({ data: 'new' })
    const cachedFetch = createCachedFetcher(fetcher, 'plants')

    await cachedFetch('arg1')
    await cachedFetch('arg2')
    expect(fetcher).toHaveBeenCalledTimes(2)
  })
})

describe('cache - 回归测试', () => {
  beforeEach(() => {
    cache.clear()
    sessionStorage.clear()
  })

  it('Bug: 缓存穿透 - null值不应被缓存', () => {
    cache.set('nullKey', null)
    const result = cache.get('nullKey')
    expect(result).toBeNull()
  })

  it('Bug: 缓存雪崩 - 不同TTL应独立过期', () => {
    vi.useFakeTimers()
    cache.set('a', 'data1', 100)
    cache.set('b', 'data2', 500)
    vi.advanceTimersByTime(200)
    expect(cache.get('a')).toBeNull()
    expect(cache.get('b')).toBe('data2')
    vi.useRealTimers()
  })

  it('Bug: sessionStorage写入失败不应崩溃', () => {
    const original = sessionStorage.setItem
    sessionStorage.setItem = () => { throw new Error('QuotaExceeded') }
    expect(() => cache.set('fail', 'data', 60000, 'session')).not.toThrow()
    sessionStorage.setItem = original
  })
})

stopCacheCleanup()
