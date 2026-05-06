import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref, nextTick } from 'vue'

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }
}))

vi.mock('@/utils/logger', () => ({
  logFetchError: vi.fn(),
  default: { log: vi.fn(), warn: vi.fn(), error: vi.fn() }
}))

import { usePagination, useFilter, useStats, useCountdown } from '@/composables/useInteraction'

describe('usePagination', () => {
  it('should have default values', () => {
    const { currentPage, pageSize } = usePagination()
    expect(currentPage.value).toBe(1)
    expect(pageSize.value).toBe(12)
  })

  it('should accept custom default size', () => {
    const { pageSize } = usePagination(20)
    expect(pageSize.value).toBe(20)
  })

  it('should paginate a list correctly', () => {
    const { currentPage, pageSize, paginatedList } = usePagination(5)
    const list = Array.from({ length: 20 }, (_, i) => `item-${i}`)
    const page1 = paginatedList(list)
    expect(page1).toEqual(list.slice(0, 5))
  })

  it('should return correct page when page changes', () => {
    const { currentPage, pageSize, paginatedList } = usePagination(3)
    const list = Array.from({ length: 10 }, (_, i) => `item-${i}`)
    currentPage.value = 2
    const page2 = paginatedList(list)
    expect(page2).toEqual(['item-3', 'item-4', 'item-5'])
  })

  it('should return partial last page', () => {
    const { currentPage, paginatedList } = usePagination(5)
    const list = Array.from({ length: 12 }, (_, i) => `item-${i}`)
    currentPage.value = 3
    const page3 = paginatedList(list)
    expect(page3).toEqual(['item-10', 'item-11'])
  })

  it('should return empty array when list is empty', () => {
    const { paginatedList } = usePagination(5)
    expect(paginatedList([])).toEqual([])
  })

  it('resetPage should set current page to 1', () => {
    const { currentPage, resetPage } = usePagination()
    currentPage.value = 5
    resetPage()
    expect(currentPage.value).toBe(1)
  })
})

describe('useFilter', () => {
  it('should return all items when no keyword or filters set', () => {
    const items = ref([
      { name: '灵芝', category: '草药' },
      { name: '枸杞', category: '草药' }
    ])
    const { filteredList } = useFilter(items, ['name'])
    expect(filteredList.value).toHaveLength(2)
  })

  it('should filter by keyword across specified fields', () => {
    const items = ref([
      { name: '灵芝', desc: '一种真菌' },
      { name: '枸杞', desc: '红色果实' }
    ])
    const { keyword, filteredList } = useFilter(items, ['name', 'desc'])
    keyword.value = '灵芝'
    expect(filteredList.value).toHaveLength(1)
    expect(filteredList.value[0].name).toBe('灵芝')
  })

  it('should filter by keyword in desc field', () => {
    const items = ref([
      { name: '灵芝', desc: '一种真菌' },
      { name: '枸杞', desc: '红色果实' }
    ])
    const { keyword, filteredList } = useFilter(items, ['name', 'desc'])
    keyword.value = '真菌'
    expect(filteredList.value).toHaveLength(1)
    expect(filteredList.value[0].name).toBe('灵芝')
  })

  it('should return empty array when keyword matches nothing', () => {
    const items = ref([{ name: '灵芝' }])
    const { keyword, filteredList } = useFilter(items, ['name'])
    keyword.value = '不存在'
    expect(filteredList.value).toHaveLength(0)
  })

  it('should filter by key-value filters', () => {
    const items = ref([
      { name: '灵芝', status: 'active' },
      { name: '枸杞', status: 'inactive' }
    ])
    const { filters, filteredList } = useFilter(items, ['name'])
    filters.value = { status: 'active' }
    expect(filteredList.value).toHaveLength(1)
    expect(filteredList.value[0].name).toBe('灵芝')
  })

  it('should combine keyword and key-value filters', () => {
    const items = ref([
      { name: '灵芝', category: '草药', status: 'active' },
      { name: '枸杞', category: '草药', status: 'active' },
      { name: '灵芝粉', category: '成品', status: 'inactive' }
    ])
    const { keyword, filters, filteredList } = useFilter(items, ['name'])
    keyword.value = '灵芝'
    filters.value = { status: 'active' }
    expect(filteredList.value).toHaveLength(1)
    expect(filteredList.value[0].name).toBe('灵芝')
  })

  it('setFilter should set individual filter value', () => {
    const items = ref([{ name: 'a', type: 'x' }, { name: 'b', type: 'y' }])
    const { setFilter, filteredList } = useFilter(items, ['name'])
    setFilter('type', 'x')
    expect(filteredList.value).toHaveLength(1)
  })

  it('clearFilters should reset keyword and filters', () => {
    const items = ref([{ name: 'test' }])
    const { keyword, filters, clearFilters, filteredList } = useFilter(items, ['name'])
    keyword.value = 'no match'
    expect(filteredList.value).toHaveLength(0)
    clearFilters()
    expect(keyword.value).toBe('')
    expect(filters.value).toEqual({})
    expect(filteredList.value).toHaveLength(1)
  })

  it('should handle empty items array', () => {
    const items = ref([])
    const { filteredList } = useFilter(items, ['name'])
    expect(filteredList.value).toEqual([])
  })
})

describe('useStats', () => {
  it('should return count for count config', () => {
    const items = ref([{ id: 1 }, { id: 2 }, { id: 3 }])
    const config = [{ value: 'count', label: '总数' }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 3, label: '总数' }])
  })

  it('should compute views total', () => {
    const items = ref([
      { name: 'a', viewCount: 10 },
      { name: 'b', viewCount: 20 }
    ])
    const config = [{ value: 'views', label: '浏览量' }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 30, label: '浏览量' }])
  })

  it('should compute favorites total', () => {
    const items = ref([
      { name: 'a', favoriteCount: 5 },
      { name: 'b', favoriteCount: 15 }
    ])
    const config = [{ value: 'favorites', label: '收藏量' }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 20, label: '收藏量' }])
  })

  it('should use custom compute function', () => {
    const items = ref([{ age: 10 }, { age: 20 }, { age: 30 }])
    const config = [{ value: 'custom', label: '平均年龄', compute: (list) => list.reduce((s, i) => s + i.age, 0) / list.length }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 20, label: '平均年龄' }])
  })

  it('should return 0 for unknown value type', () => {
    const items = ref([{ id: 1 }])
    const config = [{ value: 'unknown', label: '未知' }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 0, label: '未知' }])
  })

  it('should handle multiple config entries', () => {
    const items = ref([
      { name: 'a', viewCount: 10, favoriteCount: 5 }
    ])
    const config = [
      { value: 'count', label: '总数' },
      { value: 'views', label: '浏览' },
      { value: 'favorites', label: '收藏' }
    ]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([
      { value: 1, label: '总数' },
      { value: 10, label: '浏览' },
      { value: 5, label: '收藏' }
    ])
  })

  it('should handle items missing viewCount/favoriteCount', () => {
    const items = ref([{ name: 'a' }, { name: 'b' }])
    const config = [{ value: 'views', label: '浏览量' }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 0, label: '浏览量' }])
  })

  it('should handle non-array items.value gracefully', () => {
    const items = ref(null)
    const config = [{ value: 'count', label: '总数' }]
    const stats = useStats(items, config)
    expect(stats.value).toEqual([{ value: 0, label: '总数' }])
  })

  it('should return empty array for empty config', () => {
    const items = ref([])
    const stats = useStats(items, [])
    expect(stats.value).toEqual([])
  })
})

describe('useCountdown', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('should initialize with default 3 minutes', () => {
    const { remainingSeconds, formattedTime, isRunning, isExpired } = useCountdown()
    expect(remainingSeconds.value).toBe(180)
    expect(formattedTime.value).toBe('03:00')
    expect(isRunning.value).toBe(false)
    expect(isExpired.value).toBe(false)
  })

  it('should initialize with custom duration', () => {
    const { remainingSeconds, formattedTime } = useCountdown(5)
    expect(remainingSeconds.value).toBe(300)
    expect(formattedTime.value).toBe('05:00')
  })

  it('should start counting down', () => {
    const { start, remainingSeconds, isRunning } = useCountdown(1)
    start()
    expect(isRunning.value).toBe(true)
    vi.advanceTimersByTime(3000)
    expect(remainingSeconds.value).toBe(57)
  })

  it('should not restart if already running', () => {
    const { start, isRunning } = useCountdown(1)
    start()
    const firstTimerCount = vi.getTimerCount()
    start() // second start should be a no-op
    expect(vi.getTimerCount()).toBe(firstTimerCount)
    expect(isRunning.value).toBe(true)
  })

  it('should stop the countdown', () => {
    const { start, stop, isRunning, remainingSeconds } = useCountdown(1)
    start()
    vi.advanceTimersByTime(5000)
    stop()
    const valueAfterStop = remainingSeconds.value
    expect(isRunning.value).toBe(false)
    vi.advanceTimersByTime(5000)
    expect(remainingSeconds.value).toBe(valueAfterStop)
  })

  it('should expire when countdown reaches zero', () => {
    const { start, remainingSeconds, isExpired, isRunning } = useCountdown(1)
    start()
    // advance to 60s (totalSeconds reaches 0), then 1 more second for the expire check callback
    vi.advanceTimersByTime(61000)
    expect(remainingSeconds.value).toBe(0)
    expect(isExpired.value).toBe(true)
    expect(isRunning.value).toBe(false)
  })

  it('should reset to original duration', () => {
    const { start, reset, remainingSeconds, isExpired } = useCountdown(2)
    start()
    vi.advanceTimersByTime(30000)
    reset()
    expect(remainingSeconds.value).toBe(120)
    expect(isExpired.value).toBe(false)
  })

  it('should reset to a new duration', () => {
    const { start, reset, remainingSeconds } = useCountdown(2)
    start()
    vi.advanceTimersByTime(10000)
    reset(5)
    expect(remainingSeconds.value).toBe(300)
  })

  it('should force expire immediately', () => {
    const { start, forceExpire, remainingSeconds, isExpired, isRunning } = useCountdown(5)
    start()
    forceExpire()
    expect(remainingSeconds.value).toBe(0)
    expect(isExpired.value).toBe(true)
    expect(isRunning.value).toBe(false)
  })

  it('formattedTime should pad correctly', () => {
    const { start, formattedTime } = useCountdown(1)
    start()
    vi.advanceTimersByTime(55000)
    expect(formattedTime.value).toBe('00:05')
  })

  it('isLowTime should be true when <= 10 seconds and > 0', () => {
    const { start, isLowTime } = useCountdown(1)
    start()
    expect(isLowTime.value).toBe(false)
    vi.advanceTimersByTime(51000) // 9 seconds remaining
    expect(isLowTime.value).toBe(true)
  })

  it('isLowTime should be false at zero seconds', () => {
    const { start, isLowTime } = useCountdown(1)
    start()
    vi.advanceTimersByTime(60000)
    expect(isLowTime.value).toBe(false)
  })

  it('stop should be a no-op when not running', () => {
    const { stop, isRunning } = useCountdown(1)
    expect(() => stop()).not.toThrow()
    expect(isRunning.value).toBe(false)
  })
})
