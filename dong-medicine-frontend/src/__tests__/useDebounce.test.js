import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useDebounceFn } from '@/composables/useDebounce'

describe('useDebounceFn', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('should call fn after delay', () => {
    const fn = vi.fn()
    const debounced = useDebounceFn(fn, 300)
    debounced('a')
    expect(fn).not.toHaveBeenCalled()
    vi.advanceTimersByTime(300)
    expect(fn).toHaveBeenCalledWith('a')
    expect(fn).toHaveBeenCalledTimes(1)
  })

  it('should debounce rapid calls', () => {
    const fn = vi.fn()
    const debounced = useDebounceFn(fn, 300)
    debounced('a')
    vi.advanceTimersByTime(100)
    debounced('b')
    vi.advanceTimersByTime(100)
    debounced('c')
    vi.advanceTimersByTime(300)
    expect(fn).toHaveBeenCalledTimes(1)
    expect(fn).toHaveBeenCalledWith('c')
  })

  it('cancel should prevent execution', () => {
    const fn = vi.fn()
    const debounced = useDebounceFn(fn, 300)
    debounced('a')
    debounced.cancel()
    vi.advanceTimersByTime(300)
    expect(fn).not.toHaveBeenCalled()
  })

  it('cancel after execution is a no-op', () => {
    const fn = vi.fn()
    const debounced = useDebounceFn(fn, 100)
    debounced('a')
    vi.advanceTimersByTime(100)
    expect(fn).toHaveBeenCalledTimes(1)
    expect(() => debounced.cancel()).not.toThrow()
  })

  it('should clear existing timeout on new call', () => {
    const fn = vi.fn()
    const debounced = useDebounceFn(fn, 200)
    debounced('a')
    vi.advanceTimersByTime(100)
    debounced('b') // resets timer
    vi.advanceTimersByTime(100) // 200ms from first call, but timer was reset
    expect(fn).not.toHaveBeenCalled()
    vi.advanceTimersByTime(100) // 200ms from second call
    expect(fn).toHaveBeenCalledWith('b')
  })
})
