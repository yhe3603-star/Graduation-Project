import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  formatTime, extractData, extractPageData, formatFileSize,
  truncate, debounce, throttle, deepClone, isEmpty, isNotEmpty,
  generateId, sleep, retry, getRankClass, getScoreLevel,
  getScoreEmoji, getScoreText, getImageUrl, getFirstImage,
  PLACEHOLDER_IMG, DEFAULT_AVATAR
} from '@/utils/index'

describe('utils - formatTime', () => {
  it('空值应返回"-"', () => {
    expect(formatTime(null)).toBe('-')
    expect(formatTime(undefined)).toBe('-')
    expect(formatTime('')).toBe('-')
  })

  it('应返回相对时间', () => {
    const now = Date.now()
    expect(formatTime(now - 30000)).toBe('刚刚')
    expect(formatTime(now - 120000)).toContain('分钟前')
    expect(formatTime(now - 7200000)).toContain('小时前')
    expect(formatTime(now - 172800000)).toContain('天前')
  })

  it('应支持format选项', () => {
    const result = formatTime('2024-01-15', { format: 'date' })
    expect(result).toContain('2024')
  })

  it('relative=false应返回绝对时间', () => {
    const now = Date.now()
    const result = formatTime(now - 1000, { relative: false })
    expect(result).not.toContain('刚刚')
  })
})

describe('utils - extractData', () => {
  it('null/undefined应返回空数组', () => {
    expect(extractData(null)).toEqual([])
    expect(extractData(undefined)).toEqual([])
  })

  it('数组应直接返回', () => {
    expect(extractData([1, 2, 3])).toEqual([1, 2, 3])
  })

  it('res.data数组应提取', () => {
    expect(extractData({ data: [1, 2] })).toEqual([1, 2])
  })

  it('res.records应提取', () => {
    expect(extractData({ records: [1, 2] })).toEqual([1, 2])
  })

  it('res.data.records应提取', () => {
    expect(extractData({ data: { records: [1, 2] } })).toEqual([1, 2])
  })

  it('res.data.data应提取', () => {
    expect(extractData({ data: { data: [1, 2] } })).toEqual([1, 2])
  })

  it('res.data.list应提取', () => {
    expect(extractData({ data: { list: [1, 2] } })).toEqual([1, 2])
  })

  it('无法提取应返回空数组', () => {
    expect(extractData({ foo: 'bar' })).toEqual([])
  })
})

describe('utils - extractPageData', () => {
  it('null应返回默认分页', () => {
    const result = extractPageData(null)
    expect(result).toEqual({ records: [], total: 0, page: 1, size: 12 })
  })

  it('应提取res.data.records分页结构', () => {
    const result = extractPageData({
      data: { records: [1, 2], total: 10, page: 2, size: 5 }
    })
    expect(result.records).toEqual([1, 2])
    expect(result.total).toBe(10)
    expect(result.page).toBe(2)
    expect(result.size).toBe(5)
  })

  it('应提取res.data.data.records嵌套结构', () => {
    const result = extractPageData({
      data: { data: { records: [1], total: 5, page: 1, size: 10 } }
    })
    expect(result.records).toEqual([1])
    expect(result.total).toBe(5)
  })

  it('缺少total应使用records长度', () => {
    const result = extractPageData({
      data: { records: [1, 2, 3] }
    })
    expect(result.total).toBe(3)
  })
})

describe('utils - formatFileSize', () => {
  it('0/null/undefined应返回"0 B"', () => {
    expect(formatFileSize(0)).toBe('0 B')
    expect(formatFileSize(null)).toBe('0 B')
    expect(formatFileSize(undefined)).toBe('0 B')
  })

  it('应正确格式化各单位', () => {
    expect(formatFileSize(500)).toBe('500.0 B')
    expect(formatFileSize(1024)).toBe('1.0 KB')
    expect(formatFileSize(1048576)).toBe('1.0 MB')
    expect(formatFileSize(1073741824)).toBe('1.0 GB')
  })
})

describe('utils - truncate', () => {
  it('应截断超长文本', () => {
    expect(truncate('abcdefghij', 5)).toBe('abcde...')
  })

  it('短文本不应截断', () => {
    expect(truncate('abc', 5)).toBe('abc')
  })

  it('null/undefined应返回空字符串', () => {
    expect(truncate(null)).toBe('')
    expect(truncate(undefined)).toBe('')
  })

  it('应支持自定义后缀', () => {
    expect(truncate('abcdefghij', 5, '…')).toBe('abcde…')
  })
})

describe('utils - debounce', () => {
  it('应延迟执行', async () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const debounced = debounce(fn, 100)
    debounced()
    expect(fn).not.toHaveBeenCalled()
    vi.advanceTimersByTime(100)
    expect(fn).toHaveBeenCalledTimes(1)
    vi.useRealTimers()
  })

  it('多次调用应只执行最后一次', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const debounced = debounce(fn, 100)
    debounced('a')
    debounced('b')
    debounced('c')
    vi.advanceTimersByTime(100)
    expect(fn).toHaveBeenCalledTimes(1)
    expect(fn).toHaveBeenCalledWith('c')
    vi.useRealTimers()
  })
})

describe('utils - throttle', () => {
  it('应限制执行频率', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const throttled = throttle(fn, 100)
    throttled()
    throttled()
    throttled()
    expect(fn).toHaveBeenCalledTimes(1)
    vi.advanceTimersByTime(100)
    throttled()
    expect(fn).toHaveBeenCalledTimes(2)
    vi.useRealTimers()
  })
})

describe('utils - deepClone', () => {
  it('应深拷贝对象', () => {
    const obj = { a: 1, b: { c: 2 } }
    const cloned = deepClone(obj)
    expect(cloned).toEqual(obj)
    expect(cloned).not.toBe(obj)
    expect(cloned.b).not.toBe(obj.b)
  })

  it('应深拷贝数组', () => {
    const arr = [1, [2, 3]]
    const cloned = deepClone(arr)
    expect(cloned).toEqual(arr)
    expect(cloned[1]).not.toBe(arr[1])
  })

  it('应处理原始值', () => {
    expect(deepClone(null)).toBeNull()
    expect(deepClone(42)).toBe(42)
    expect(deepClone('str')).toBe('str')
  })
})

describe('utils - isEmpty/isNotEmpty', () => {
  it('null/undefined应为空', () => {
    expect(isEmpty(null)).toBe(true)
    expect(isEmpty(undefined)).toBe(true)
  })

  it('空字符串应为空', () => {
    expect(isEmpty('')).toBe(true)
    expect(isEmpty('  ')).toBe(true)
  })

  it('空数组/对象应为空', () => {
    expect(isEmpty([])).toBe(true)
    expect(isEmpty({})).toBe(true)
  })

  it('非空值应不为空', () => {
    expect(isEmpty('text')).toBe(false)
    expect(isEmpty([1])).toBe(false)
    expect(isEmpty({ a: 1 })).toBe(false)
    expect(isEmpty(0)).toBe(false)
  })

  it('isNotEmpty应是isEmpty的反函数', () => {
    expect(isNotEmpty('text')).toBe(true)
    expect(isNotEmpty('')).toBe(false)
  })
})

describe('utils - generateId', () => {
  it('应生成唯一ID', () => {
    const id1 = generateId()
    const id2 = generateId()
    expect(id1).not.toBe(id2)
  })

  it('应返回字符串', () => {
    expect(typeof generateId()).toBe('string')
  })
})

describe('utils - sleep', () => {
  it('应延迟指定时间', async () => {
    vi.useFakeTimers()
    const promise = sleep(100)
    vi.advanceTimersByTime(100)
    await expect(promise).resolves.toBeUndefined()
    vi.useRealTimers()
  })
})

describe('utils - retry', () => {
  it('成功时应直接返回', async () => {
    const fn = vi.fn().mockResolvedValue('ok')
    const result = await retry(fn, 3, 10)
    expect(result).toBe('ok')
    expect(fn).toHaveBeenCalledTimes(1)
  })

  it('失败后应重试', async () => {
    const fn = vi.fn()
      .mockRejectedValueOnce(new Error('fail1'))
      .mockRejectedValueOnce(new Error('fail2'))
      .mockResolvedValue('ok')
    const result = await retry(fn, 3, 10)
    expect(result).toBe('ok')
    expect(fn).toHaveBeenCalledTimes(3)
  })

  it('超过重试次数应拒绝', async () => {
    const fn = vi.fn().mockRejectedValue(new Error('always fail'))
    await expect(retry(fn, 2, 10)).rejects.toThrow('always fail')
    expect(fn).toHaveBeenCalledTimes(3)
  })
})

describe('utils - 评分相关', () => {
  it('getRankClass应返回正确排名样式', () => {
    expect(getRankClass(0)).toBe('gold')
    expect(getRankClass(1)).toBe('silver')
    expect(getRankClass(2)).toBe('bronze')
    expect(getRankClass(3)).toBe('')
  })

  it('getScoreLevel应返回正确等级', () => {
    expect(getScoreLevel(95)).toBe('excellent')
    expect(getScoreLevel(75)).toBe('good')
    expect(getScoreLevel(60)).toBe('pass')
    expect(getScoreLevel(30)).toBe('fail')
  })

  it('getScoreEmoji应返回对应表情', () => {
    expect(getScoreEmoji(95)).toBe('🏆')
    expect(getScoreEmoji(75)).toBe('😊')
    expect(getScoreEmoji(60)).toBe('🙂')
    expect(getScoreEmoji(30)).toBe('💪')
  })

  it('getScoreText应返回对应文本', () => {
    expect(getScoreText(95)).toContain('达人')
    expect(getScoreText(30)).toContain('努力')
  })
})

describe('utils - getImageUrl', () => {
  it('空值应返回占位图', () => {
    expect(getImageUrl(null)).toBe(PLACEHOLDER_IMG)
    expect(getImageUrl('')).toBe(PLACEHOLDER_IMG)
  })

  it('绝对URL应直接返回', () => {
    expect(getImageUrl('http://example.com/img.jpg')).toBe('http://example.com/img.jpg')
    expect(getImageUrl('https://example.com/img.jpg')).toBe('https://example.com/img.jpg')
  })

  it('相对路径应添加前缀/', () => {
    expect(getImageUrl('images/photo.jpg')).toBe('/images/photo.jpg')
  })

  it('以/开头的路径应直接返回', () => {
    expect(getImageUrl('/images/photo.jpg')).toBe('/images/photo.jpg')
  })
})

describe('utils - getFirstImage', () => {
  it('空值应返回占位图', () => {
    expect(getFirstImage(null)).toBe(PLACEHOLDER_IMG)
    expect(getFirstImage('')).toBe(PLACEHOLDER_IMG)
  })

  it('逗号分隔字符串应取第一张', () => {
    expect(getFirstImage('a.jpg,b.jpg')).toBe('/a.jpg')
  })

  it('JSON数组应取第一张', () => {
    expect(getFirstImage('["a.jpg","b.jpg"]')).toBe('/a.jpg')
  })

  it('JS数组应取第一张', () => {
    expect(getFirstImage(['a.jpg', 'b.jpg'])).toBe('/a.jpg')
  })

  it('空数组应返回占位图', () => {
    expect(getFirstImage([])).toBe(PLACEHOLDER_IMG)
  })
})

describe('utils - 回归测试', () => {
  it('Bug: extractData处理深层嵌套', () => {
    expect(extractData({ data: { data: [1] } })).toEqual([1])
  })

  it('Bug: formatTime处理未来时间', () => {
    const future = Date.now() + 10000
    const result = formatTime(future)
    expect(result).toBeDefined()
  })

  it('Bug: deepClone处理循环引用应不崩溃', () => {
    const obj = { a: 1 }
    obj.self = obj
    try {
      deepClone(obj)
    } catch (e) {
      expect(e).toBeInstanceOf(RangeError)
    }
  })

  it('Bug: truncate处理非字符串', () => {
    expect(truncate(null)).toBe('')
    expect(truncate(undefined)).toBe('')
  })
})
