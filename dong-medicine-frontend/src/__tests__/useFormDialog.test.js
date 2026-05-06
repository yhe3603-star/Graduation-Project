import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }
}))

vi.mock('@/composables/useUpdateLog', () => ({
  useUpdateLog: vi.fn(() => ({
    parseUpdateLog: vi.fn((data) => {
      if (!data) return []
      if (Array.isArray(data)) return data
      if (typeof data === 'string') {
        try { return JSON.parse(data) } catch { return [] }
      }
      return []
    }),
    stringifyUpdateLog: vi.fn((logs) => {
      if (!logs || !Array.isArray(logs)) return '[]'
      return JSON.stringify(logs)
    }),
    addLog: vi.fn((existing, content, operator = '管理员') => {
      const logs = Array.isArray(existing) ? existing : []
      return [{ id: Date.now(), time: '2025-06-15', content, operator }, ...logs]
    }),
    updateLog: vi.fn((logs, id, content) => {
      const arr = Array.isArray(logs) ? [...logs] : []
      const idx = arr.findIndex(l => l.id === id)
      if (idx > -1) arr[idx] = { ...arr[idx], content }
      return arr
    }),
    deleteLog: vi.fn((logs, id) => {
      const arr = Array.isArray(logs) ? [...logs] : []
      return arr.filter(l => l.id !== id)
    }),
    logDialogVisible: ref(false),
    editingLog: ref(null),
    openLogDialog: vi.fn(),
    closeLogDialog: vi.fn()
  }))
}))

import { ElMessage } from 'element-plus'
import { useFormDialog } from '@/composables/useFormDialog'

const getDefaultForm = () => ({
  id: null,
  name: '',
  tags: [],
  updateLog: ''
})

describe('useFormDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2025-06-15T12:00:00Z'))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('initForm', () => {
    it('should reset to default form when data is null', () => {
      const { form, initForm } = useFormDialog(getDefaultForm)
      form.value = { id: 1, name: 'modified' }
      initForm(null)
      expect(form.value.id).toBeNull()
      expect(form.value.name).toBe('')
    })

    it('should populate form with provided data', () => {
      const { form, initForm } = useFormDialog(getDefaultForm)
      initForm({ id: 1, name: '灵芝' })
      expect(form.value.id).toBe(1)
      expect(form.value.name).toBe('灵芝')
    })

    it('should add updateLog default if not present in data', () => {
      const { form, initForm } = useFormDialog(getDefaultForm)
      initForm({ name: 'test' })
      expect(form.value.updateLog).toBeDefined()
    })

    it('should parse comma-separated string to array for arrayFields', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: '', updateLog: '' })
      const { form, initForm } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      initForm({ name: 'test', tags: 'a,b,c' })
      expect(form.value.tags).toEqual(['a', 'b', 'c'])
    })

    it('should parse JSON string array to array for arrayFields', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: '', updateLog: '' })
      const { form, initForm } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      initForm({ name: 'test', tags: '["x","y","z"]' })
      expect(form.value.tags).toEqual(['x', 'y', 'z'])
    })

    it('should handle null/empty array field values in initForm', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: '', updateLog: '' })
      const { form, initForm } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      initForm({ name: 'test', tags: null })
      expect(form.value.tags).toEqual([])
    })

    it('should return empty array for invalid JSON starting with [ in arrayFields', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: '', updateLog: '' })
      const { form, initForm } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      initForm({ name: 'test', tags: '[invalid' })
      expect(form.value.tags).toEqual([])
    })

    it('should pass through array values unchanged for arrayFields', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: [], updateLog: '' })
      const { form, initForm } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      const tags = ['a', 'b']
      initForm({ name: 'test', tags })
      expect(form.value.tags).toEqual(['a', 'b'])
    })

    it('should filter falsy values from JSON array in arrayFields', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: '', updateLog: '' })
      const { form, initForm } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      initForm({ name: 'test', tags: '["a",null,"b",""]' })
      expect(form.value.tags).toEqual(['a', 'b'])
    })
  })

  describe('isEdit', () => {
    it('should be false when form has no id', () => {
      const { isEdit } = useFormDialog(getDefaultForm)
      expect(isEdit.value).toBe(false)
    })

    it('should be true when form has id', () => {
      const { form, isEdit } = useFormDialog(getDefaultForm)
      form.value.id = 1
      expect(isEdit.value).toBe(true)
    })
  })

  describe('getFormData', () => {
    it('should include auto-generated create log for new form', () => {
      const { getFormData, form } = useFormDialog(getDefaultForm)
      form.value.name = 'test'
      const data = getFormData()
      expect(data.updateLog).toBeDefined()
    })

    it('should include auto-generated update log for existing form', () => {
      const { getFormData, form, initForm } = useFormDialog(getDefaultForm)
      initForm({ id: 1, name: 'test' })
      form.value.updateLog = '[]'
      const data = getFormData()
      expect(data.updateLog).toBeDefined()
    })

    it('should convert array fields to string in output via parseToString', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: ['a', 'b'], updateLog: '' })
      const { getFormData, form } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      form.value.name = 'test'
      const data = getFormData()
      expect(typeof data.tags).toBe('string')
      expect(JSON.parse(data.tags)).toEqual(['a', 'b'])
    })

    it('should convert singleArrayFields to single string', () => {
      const singleArrForm = () => ({ id: null, image: ['url1'], updateLog: '' })
      const { getFormData, form } = useFormDialog(singleArrForm, {
        singleArrayFields: ['image']
      })
      form.value.name = 'test'
      const data = getFormData()
      expect(typeof data.image).toBe('string')
      expect(data.image).toBe('url1')
    })

    it('should handle singleArrayFields with non-array value', () => {
      const singleArrForm = () => ({ id: null, image: 'url1', updateLog: '' })
      const { getFormData, form } = useFormDialog(singleArrForm, {
        singleArrayFields: ['image']
      })
      form.value.name = 'test'
      const data = getFormData()
      expect(data.image).toBe('url1')
    })

    it('should handle singleArrayFields with empty array', () => {
      const singleArrForm = () => ({ id: null, image: [], updateLog: '' })
      const { getFormData, form } = useFormDialog(singleArrForm, {
        singleArrayFields: ['image']
      })
      form.value.name = 'test'
      const data = getFormData()
      expect(data.image).toBe('')
    })

    it('should not add duplicate auto log when recent log exists for today', () => {
      const { getFormData, form } = useFormDialog(getDefaultForm, {
        autoLogMessages: { create: '新增数据', update: '更新数据' }
      })
      // Manually set a log that already has today's date
      const todayLogs = [{ id: 1, time: '2025-06-15', content: 'already logged', operator: '管理员' }]
      form.value.updateLog = JSON.stringify(todayLogs)
      const data = getFormData()
      // Should NOT add another auto log since there's already a today log
      const parsed = JSON.parse(data.updateLog)
      expect(parsed).toHaveLength(1)
      expect(parsed[0].content).toBe('already logged')
    })

    it('should handle empty array field value via parseToString fallback', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: '', updateLog: '' })
      const { getFormData, form } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      form.value.name = 'test'
      form.value.tags = ''
      const data = getFormData()
      expect(data.tags).toBe('')
    })

    it('should handle null array field value via parseToString fallback', () => {
      const arrayFieldsForm = () => ({ id: null, name: '', tags: null, updateLog: '' })
      const { getFormData, form } = useFormDialog(arrayFieldsForm, {
        arrayFields: ['tags']
      })
      form.value.name = 'test'
      const data = getFormData()
      expect(data.tags).toBe('')
    })
  })

  describe('handleSave (validation)', () => {
    it('should return true when validation passes', () => {
      const { handleSave, form } = useFormDialog(getDefaultForm, {
        validate: () => true
      })
      form.value.name = 'valid'
      expect(handleSave()).toBe(true)
    })

    it('should return false and show warning when validation fails with message', () => {
      const { handleSave } = useFormDialog(getDefaultForm, {
        validate: () => '名称不能为空'
      })
      expect(handleSave()).toBe(false)
      expect(ElMessage.warning).toHaveBeenCalledWith('名称不能为空')
    })

    it('should return false without warning when validation returns falsy without message', () => {
      const { handleSave } = useFormDialog(getDefaultForm, {
        validate: () => false
      })
      expect(handleSave()).toBe(false)
      expect(ElMessage.warning).not.toHaveBeenCalled()
    })

    it('should default validate to always pass', () => {
      const { handleSave, form } = useFormDialog(getDefaultForm)
      form.value.name = 'anything'
      expect(handleSave()).toBe(true)
    })
  })

  describe('setSaving', () => {
    it('should set saving state', () => {
      const { saving, setSaving } = useFormDialog(getDefaultForm)
      expect(saving.value).toBe(false)
      setSaving(true)
      expect(saving.value).toBe(true)
      setSaving(false)
      expect(saving.value).toBe(false)
    })
  })

  describe('form state', () => {
    it('should initialize form from getDefaultForm', () => {
      const { form } = useFormDialog(getDefaultForm)
      expect(form.value).toEqual({ id: null, name: '', tags: [], updateLog: '' })
    })

    it('should be reactive when form is modified', () => {
      const { form, isEdit } = useFormDialog(getDefaultForm)
      expect(isEdit.value).toBe(false)
      form.value.id = 42
      expect(isEdit.value).toBe(true)
    })
  })
})
