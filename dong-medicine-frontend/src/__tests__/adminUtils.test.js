import { describe, it, expect } from 'vitest'
import { formatTime, formatFileSize, getDifficultyTagType, getDifficultyText, getLevelTagType, createTagGetter } from '@/utils/adminUtils'

describe('adminUtils', () => {
  describe('formatTime', () => {
    it('should return "-" for null/undefined/empty input', () => {
      expect(formatTime(null)).toBe('-')
      expect(formatTime(undefined)).toBe('-')
      expect(formatTime('')).toBe('-')
    })

    it('should format valid date string', () => {
      const result = formatTime('2024-01-15T10:30:00')
      expect(result).toContain('2024')
      expect(result).toContain('15')
    })

    it('should format Date object', () => {
      const date = new Date('2024-06-20T14:00:00')
      const result = formatTime(date)
      expect(result).toContain('2024')
    })

    it('should handle custom format', () => {
      const result = formatTime('2024-01-15', { format: 'date' })
      expect(result).toContain('2024')
    })
  })

  describe('formatFileSize', () => {
    it('should return "0 B" for 0 bytes', () => {
      expect(formatFileSize(0)).toBe('0 B')
    })

    it('should return "0 B" for null/undefined', () => {
      expect(formatFileSize(null)).toBe('0 B')
      expect(formatFileSize(undefined)).toBe('0 B')
    })

    it('should format bytes correctly', () => {
      expect(formatFileSize(500)).toBe('500.0 B')
    })

    it('should format kilobytes correctly', () => {
      expect(formatFileSize(1024)).toBe('1.0 KB')
      expect(formatFileSize(2048)).toBe('2.0 KB')
    })

    it('should format megabytes correctly', () => {
      expect(formatFileSize(1048576)).toBe('1.0 MB')
      expect(formatFileSize(1572864)).toBe('1.5 MB')
    })

    it('should format gigabytes correctly', () => {
      expect(formatFileSize(1073741824)).toBe('1.0 GB')
    })
  })

  describe('getDifficultyTagType', () => {
    it('should return "success" for easy/beginner', () => {
      expect(getDifficultyTagType('easy')).toBe('success')
      expect(getDifficultyTagType('beginner')).toBe('success')
    })

    it('should return "warning" for medium/intermediate/advanced', () => {
      expect(getDifficultyTagType('medium')).toBe('warning')
      expect(getDifficultyTagType('intermediate')).toBe('warning')
      expect(getDifficultyTagType('advanced')).toBe('warning')
    })

    it('should return "danger" for hard/professional', () => {
      expect(getDifficultyTagType('hard')).toBe('danger')
      expect(getDifficultyTagType('professional')).toBe('danger')
    })

    it('should return "info" for unknown difficulty', () => {
      expect(getDifficultyTagType('unknown')).toBe('info')
      expect(getDifficultyTagType('')).toBe('info')
    })
  })

  describe('getDifficultyText', () => {
    it('should return "入门" for easy/beginner', () => {
      expect(getDifficultyText('easy')).toBe('入门')
      expect(getDifficultyText('beginner')).toBe('入门')
    })

    it('should return "进阶" for medium/intermediate/advanced', () => {
      expect(getDifficultyText('medium')).toBe('进阶')
      expect(getDifficultyText('intermediate')).toBe('进阶')
    })

    it('should return "专业" for hard/professional', () => {
      expect(getDifficultyText('hard')).toBe('专业')
      expect(getDifficultyText('professional')).toBe('专业')
    })

    it('should return original value for unknown difficulty', () => {
      expect(getDifficultyText('unknown')).toBe('unknown')
    })
  })

  describe('getLevelTagType', () => {
    it('should return correct tag type for known levels', () => {
      expect(getLevelTagType('省级')).toBe('warning')
      expect(getLevelTagType('自治区级')).toBe('success')
      expect(getLevelTagType('州级')).toBe('primary')
      expect(getLevelTagType('市级')).toBe('primary')
    })

    it('should return "info" for unknown level', () => {
      expect(getLevelTagType('unknown')).toBe('info')
    })
  })

  describe('createTagGetter', () => {
    it('should create a function that returns correct tag type', () => {
      const getTag = createTagGetter({ high: 'danger', medium: 'warning', low: 'success' })
      expect(getTag('high')).toBe('danger')
      expect(getTag('medium')).toBe('warning')
      expect(getTag('low')).toBe('success')
    })

    it('should return default value for unknown key', () => {
      const getTag = createTagGetter({ high: 'danger' }, 'info')
      expect(getTag('unknown')).toBe('info')
    })
  })
})
