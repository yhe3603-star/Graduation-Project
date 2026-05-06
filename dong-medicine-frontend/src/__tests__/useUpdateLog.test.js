import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useUpdateLog, useUpdateLogDisplay } from '@/composables/useUpdateLog'

describe('useUpdateLog', () => {
  let updateLog

  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2025-06-15T12:00:00Z'))
    updateLog = useUpdateLog()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('parseUpdateLog', () => {
    it('should return empty array for null/undefined/empty input', () => {
      expect(updateLog.parseUpdateLog(null)).toEqual([])
      expect(updateLog.parseUpdateLog(undefined)).toEqual([])
      expect(updateLog.parseUpdateLog('')).toEqual([])
    })

    it('should return the array if input is already an array', () => {
      const arr = [{ id: 1, content: 'test' }]
      expect(updateLog.parseUpdateLog(arr)).toBe(arr)
    })

    it('should parse JSON string starting with [', () => {
      const jsonStr = JSON.stringify([{ id: 1, content: 'test' }])
      expect(updateLog.parseUpdateLog(jsonStr)).toEqual([{ id: 1, content: 'test' }])
    })

    it('should return empty array for invalid JSON starting with [', () => {
      expect(updateLog.parseUpdateLog('[invalid json')).toEqual([])
    })

    it('should wrap JSON object string as plain log entry (not array)', () => {
      const result = updateLog.parseUpdateLog('{"key":"value"}')
      expect(result).toHaveLength(1)
      expect(result[0].content).toBe('{"key":"value"}')
      expect(result[0].operator).toBe('系统')
    })

    it('should convert plain string to a single log entry with today date', () => {
      const result = updateLog.parseUpdateLog('some log content')
      expect(result).toHaveLength(1)
      expect(result[0]).toEqual({
        time: '2025-06-15',
        content: 'some log content',
        operator: '系统'
      })
    })

    it('should trim whitespace before checking string', () => {
      const result = updateLog.parseUpdateLog('  plain text  ')
      expect(result).toHaveLength(1)
      expect(result[0].content).toBe('plain text')
    })

    it('should return empty array for non-string non-array truthy input', () => {
      expect(updateLog.parseUpdateLog(123)).toEqual([])
      expect(updateLog.parseUpdateLog(true)).toEqual([])
    })
  })

  describe('stringifyUpdateLog', () => {
    it('should return "[]" for null/undefined input', () => {
      expect(updateLog.stringifyUpdateLog(null)).toBe('[]')
      expect(updateLog.stringifyUpdateLog(undefined)).toBe('[]')
    })

    it('should return "[]" for non-array input', () => {
      expect(updateLog.stringifyUpdateLog('string')).toBe('[]')
      expect(updateLog.stringifyUpdateLog(123)).toBe('[]')
    })

    it('should serialize array to JSON string', () => {
      const logs = [{ id: 1, content: 'test' }]
      expect(updateLog.stringifyUpdateLog(logs)).toBe(JSON.stringify(logs))
    })

    it('should filter out falsy values', () => {
      const logs = [{ id: 1 }, null, { id: 2 }, undefined]
      const result = JSON.parse(updateLog.stringifyUpdateLog(logs))
      expect(result).toEqual([{ id: 1 }, { id: 2 }])
    })
  })

  describe('addLog', () => {
    it('should add a new log entry at the beginning', () => {
      const existing = [{ id: 1, content: 'old', time: '2025-01-01' }]
      const result = updateLog.addLog(existing, 'new log')
      expect(result).toHaveLength(2)
      expect(result[0].content).toBe('new log')
      expect(result[0].operator).toBe('管理员')
      expect(result[0].time).toBe('2025-06-15')
      expect(result[0].id).toBeDefined()
    })

    it('should use custom operator', () => {
      const result = updateLog.addLog([], 'test', '自定义操作员')
      expect(result[0].operator).toBe('自定义操作员')
    })

    it('should handle null/empty existing logs', () => {
      const result = updateLog.addLog(null, 'first log')
      expect(result).toHaveLength(1)
      expect(result[0].content).toBe('first log')
    })

    it('should handle string existing logs', () => {
      const result = updateLog.addLog('old string log', 'new log')
      expect(result).toHaveLength(2)
    })
  })

  describe('updateLog', () => {
    it('should update log content by id', () => {
      const existing = [{ id: 1, content: 'old', time: '2025-01-01' }]
      const result = updateLog.updateLog(existing, 1, 'new content')
      expect(result[0].content).toBe('new content')
      expect(result[0].time).toBe('2025-01-01')
    })

    it('should update log content by time', () => {
      const existing = [{ id: 1, content: 'old', time: '2025-01-01' }]
      const result = updateLog.updateLog(existing, '2025-01-01', 'updated')
      expect(result[0].content).toBe('updated')
    })

    it('should return same array if log not found', () => {
      const existing = [{ id: 1, content: 'old' }]
      const result = updateLog.updateLog(existing, 999, 'new')
      expect(result).toHaveLength(1)
      expect(result[0].content).toBe('old')
    })
  })

  describe('deleteLog', () => {
    it('should delete log by id', () => {
      const existing = [
        { id: 1, content: 'keep' },
        { id: 2, content: 'delete' }
      ]
      const result = updateLog.deleteLog(existing, 2)
      expect(result).toHaveLength(1)
      expect(result[0].id).toBe(1)
    })

    it('should delete log by time', () => {
      const existing = [
        { id: 1, content: 'keep' },
        { id: 2, content: 'delete', time: '2025-01-01' }
      ]
      const result = updateLog.deleteLog(existing, '2025-01-01')
      expect(result).toHaveLength(1)
    })

    it('should return all logs if id not found', () => {
      const existing = [{ id: 1, content: 'test' }]
      const result = updateLog.deleteLog(existing, 999)
      expect(result).toHaveLength(1)
    })
  })

  describe('generateAutoLog', () => {
    it('should generate create log', () => {
      expect(updateLog.generateAutoLog('create', '药材')).toBe('新增药材')
    })

    it('should generate update log', () => {
      expect(updateLog.generateAutoLog('update', '知识')).toBe('更新知识')
    })

    it('should generate delete log', () => {
      expect(updateLog.generateAutoLog('delete', '资源')).toBe('删除资源')
    })

    it('should generate default log for unknown action', () => {
      expect(updateLog.generateAutoLog('unknown', '数据')).toBe('操作数据')
    })

    it('should append details when provided', () => {
      expect(updateLog.generateAutoLog('create', '药材', '新增了灵芝')).toBe('新增药材：新增了灵芝')
    })

    it('should not append details when empty', () => {
      expect(updateLog.generateAutoLog('update', '药材', '')).toBe('更新药材')
    })
  })

  describe('formatLogTime', () => {
    it('should return empty string for falsy input', () => {
      expect(updateLog.formatLogTime(null)).toBe('')
      expect(updateLog.formatLogTime('')).toBe('')
      expect(updateLog.formatLogTime(undefined)).toBe('')
    })

    it('should extract date from ISO datetime string', () => {
      expect(updateLog.formatLogTime('2025-06-15T12:00:00Z')).toBe('2025-06-15')
    })

    it('should return plain date string as-is', () => {
      expect(updateLog.formatLogTime('2025-06-15')).toBe('2025-06-15')
    })
  })

  describe('dialog state', () => {
    it('openLogDialog should set dialog visible and editing log', () => {
      const log = { id: 1, content: 'test' }
      updateLog.openLogDialog(log)
      expect(updateLog.logDialogVisible.value).toBe(true)
      expect(updateLog.editingLog.value).toStrictEqual(log)
      expect(updateLog.newLogContent.value).toBe('test')
    })

    it('openLogDialog with no log should reset content', () => {
      updateLog.openLogDialog()
      expect(updateLog.logDialogVisible.value).toBe(true)
      expect(updateLog.editingLog.value).toBeNull()
      expect(updateLog.newLogContent.value).toBe('')
    })

    it('closeLogDialog should reset all dialog state', () => {
      updateLog.openLogDialog({ id: 1, content: 'test' })
      updateLog.closeLogDialog()
      expect(updateLog.logDialogVisible.value).toBe(false)
      expect(updateLog.editingLog.value).toBeNull()
      expect(updateLog.newLogContent.value).toBe('')
    })
  })

  describe('saveLog', () => {
    it('should return null if content is empty', () => {
      updateLog.newLogContent.value = ''
      const result = updateLog.saveLog([])
      expect(result).toBeNull()
    })

    it('should add new log when not editing', () => {
      updateLog.newLogContent.value = 'new entry'
      const result = updateLog.saveLog([])
      expect(result).toHaveLength(1)
      expect(result[0].content).toBe('new entry')
    })

    it('should update existing log when editing', () => {
      const existing = [{ id: 1, content: 'old', time: '2025-01-01' }]
      updateLog.openLogDialog(existing[0])
      updateLog.newLogContent.value = 'updated'
      const result = updateLog.saveLog(existing)
      expect(result).toHaveLength(1)
      expect(result[0].content).toBe('updated')
    })
  })
})

describe('useUpdateLogDisplay', () => {
  it('should compute logs from a ref value', async () => {
    const { ref } = await import('vue')
    const logData = ref([{ time: '2025-01-01', content: 'test' }])
    const display = useUpdateLogDisplay(logData)
    expect(display.logs.value).toHaveLength(1)
    expect(display.recentLogs.value).toHaveLength(1)
    expect(display.hasLogs.value).toBe(true)
  })

  it('should limit recentLogs to 5 entries', async () => {
    const { ref } = await import('vue')
    const logs = Array.from({ length: 10 }, (_, i) => ({ time: `2025-01-${i + 1}`, content: `log ${i}` }))
    const logData = ref(logs)
    const display = useUpdateLogDisplay(logData)
    expect(display.recentLogs.value).toHaveLength(5)
    expect(display.hasLogs.value).toBe(true)
  })

  it('should handle empty ref', async () => {
    const { ref } = await import('vue')
    const logData = ref(null)
    const display = useUpdateLogDisplay(logData)
    expect(display.logs.value).toEqual([])
    expect(display.hasLogs.value).toBe(false)
  })
})
