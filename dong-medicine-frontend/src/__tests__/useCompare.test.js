import { describe, it, expect, beforeEach } from 'vitest'
import { useCompare } from '@/composables/useCompare'

describe('useCompare', () => {
  let compare

  beforeEach(() => {
    localStorage.clear()
    compare = useCompare()
    compare.clearCompare()
  })

  it('should start with empty list', () => {
    expect(compare.compareList.value).toEqual([])
  })

  it('addToCompare should add plant', () => {
    const result = compare.addToCompare({ id: 1, nameCn: '金银花' })
    expect(result).toBe(true)
    expect(compare.compareList.value).toHaveLength(1)
    expect(compare.isInCompare(1)).toBe(true)
  })

  it('should not add duplicate', () => {
    compare.addToCompare({ id: 1, nameCn: '金银花' })
    const result = compare.addToCompare({ id: 1, nameCn: '金银花' })
    expect(result).toBe(false)
    expect(compare.compareList.value).toHaveLength(1)
  })

  it('should enforce MAX_COMPARE of 3', () => {
    compare.addToCompare({ id: 1 })
    compare.addToCompare({ id: 2 })
    compare.addToCompare({ id: 3 })
    const result = compare.addToCompare({ id: 4 })
    expect(result).toBe(false)
    expect(compare.compareList.value).toHaveLength(3)
  })

  it('should reject null/undefined plant', () => {
    expect(compare.addToCompare(null)).toBe(false)
    expect(compare.addToCompare({})).toBe(false)
    expect(compare.compareList.value).toHaveLength(0)
  })

  it('removeFromCompare should remove by id', () => {
    compare.addToCompare({ id: 1 })
    compare.addToCompare({ id: 2 })
    compare.removeFromCompare(1)
    expect(compare.compareList.value).toHaveLength(1)
    expect(compare.isInCompare(1)).toBe(false)
    expect(compare.isInCompare(2)).toBe(true)
  })

  it('removeFromCompare non-existent id is a no-op', () => {
    compare.addToCompare({ id: 1 })
    compare.removeFromCompare(99)
    expect(compare.compareList.value).toHaveLength(1)
  })

  it('clearCompare should empty the list', () => {
    compare.addToCompare({ id: 1 })
    compare.addToCompare({ id: 2 })
    compare.clearCompare()
    expect(compare.compareList.value).toEqual([])
  })

  it('should persist to localStorage', () => {
    compare.addToCompare({ id: 1, nameCn: '金银花' })
    const stored = JSON.parse(localStorage.getItem('dong_compare_plants'))
    expect(stored).toHaveLength(1)
    expect(stored[0].id).toBe(1)
  })

  it('add should persist after remove', () => {
    compare.addToCompare({ id: 10 })
    compare.removeFromCompare(10)
    const stored = JSON.parse(localStorage.getItem('dong_compare_plants'))
    expect(stored).toEqual([])
  })

  it('MAX_COMPARE should be 3', () => {
    expect(compare.MAX_COMPARE).toBe(3)
  })
})
