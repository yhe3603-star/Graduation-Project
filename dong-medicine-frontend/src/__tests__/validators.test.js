import { describe, it, expect } from 'vitest'
import { createPasswordValidator } from '@/utils/validators'

describe('validators - createPasswordValidator', () => {
  const { password, confirmPassword } = createPasswordValidator()

  describe('password规则', () => {
    it('应有required规则', () => {
      const requiredRule = password.find(r => r.required === true)
      expect(requiredRule).toBeDefined()
      expect(requiredRule.message).toBe('请输入密码')
    })

    it('应有长度规则', () => {
      const lengthRule = password.find(r => r.min !== undefined)
      expect(lengthRule).toBeDefined()
      expect(lengthRule.min).toBe(8)
      expect(lengthRule.max).toBe(50)
    })

    it('应有自定义验证器', () => {
      const validatorRule = password.find(r => r.validator !== undefined)
      expect(validatorRule).toBeDefined()
    })

    it('密码缺少字母应验证失败', () => {
      const validatorRule = password.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, '12345678', (e) => { error = e || null })
      expect(error).toBeInstanceOf(Error)
      expect(error.message).toBe('密码必须包含字母')
    })

    it('密码缺少数字应验证失败', () => {
      const validatorRule = password.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, 'abcdefgh', (e) => { error = e || null })
      expect(error).toBeInstanceOf(Error)
      expect(error.message).toBe('密码必须包含数字')
    })

    it('密码包含空格应验证失败', () => {
      const validatorRule = password.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, 'abc 1234', (e) => { error = e || null })
      expect(error).toBeInstanceOf(Error)
      expect(error.message).toBe('密码不能包含空格')
    })

    it('合法密码应验证通过', () => {
      const validatorRule = password.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, 'Test1234', (e) => { error = e || null })
      expect(error).toBeNull()
    })

    it('空值应跳过验证', () => {
      const validatorRule = password.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, '', (e) => { error = e || null })
      expect(error).toBeNull()
    })
  })

  describe('confirmPassword规则', () => {
    it('应有required规则', () => {
      const formRef = { value: { newPassword: 'Test1234' } }
      const rules = confirmPassword(formRef)
      const requiredRule = rules.find(r => r.required === true)
      expect(requiredRule).toBeDefined()
    })

    it('密码一致应验证通过', () => {
      const formRef = { value: { newPassword: 'Test1234' } }
      const rules = confirmPassword(formRef)
      const validatorRule = rules.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, 'Test1234', (e) => { error = e || null })
      expect(error).toBeNull()
    })

    it('密码不一致应验证失败', () => {
      const formRef = { value: { newPassword: 'Test1234' } }
      const rules = confirmPassword(formRef)
      const validatorRule = rules.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, 'Different1', (e) => { error = e || null })
      expect(error).toBeInstanceOf(Error)
      expect(error.message).toBe('两次输入的密码不一致')
    })

    it('应支持自定义字段名', () => {
      const formRef = { value: { password: 'Test1234' } }
      const rules = confirmPassword(formRef, 'password')
      const validatorRule = rules.find(r => r.validator !== undefined)
      let error = null
      validatorRule.validator(null, 'Test1234', (e) => { error = e || null })
      expect(error).toBeNull()
    })
  })

  describe('自定义选项', () => {
    it('应支持自定义最小长度', () => {
      const { password } = createPasswordValidator({ minLength: 6 })
      const lengthRule = password.find(r => r.min !== undefined)
      expect(lengthRule.min).toBe(6)
    })

    it('应支持自定义最大长度', () => {
      const { password } = createPasswordValidator({ maxLength: 20 })
      const lengthRule = password.find(r => r.min !== undefined)
      expect(lengthRule.max).toBe(20)
    })
  })
})
