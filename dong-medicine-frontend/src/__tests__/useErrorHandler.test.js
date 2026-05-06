import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }
}))

import { ElMessage } from 'element-plus'
import { useErrorHandler } from '@/composables/useErrorHandler'

describe('useErrorHandler', () => {
  let handler

  beforeEach(() => {
    vi.clearAllMocks()
    handler = useErrorHandler()
  })

  describe('handleApiError', () => {
    it('should show fallback message when error is null', () => {
      handler.handleApiError(null)
      expect(ElMessage.error).toHaveBeenCalledWith('操作失败')
    })

    it('should show custom fallback message when error is null', () => {
      handler.handleApiError(null, '自定义错误')
      expect(ElMessage.error).toHaveBeenCalledWith('自定义错误')
    })

    it('should show business message for business error codes', () => {
      const error = { response: { status: 200, data: { code: 1001, msg: '' } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('用户不存在')
    })

    it('should show business code 1002 message', () => {
      const error = { response: { status: 200, data: { code: 1002 } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('用户名已存在')
    })

    it('should show business code 1003 message', () => {
      const error = { response: { status: 200, data: { code: 1003 } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('密码错误')
    })

    it('should show business code 4002 message', () => {
      const error = { response: { status: 200, data: { code: 4002 } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('文件类型不允许')
    })

    it('should show business code 7001 message', () => {
      const error = { response: { status: 200, data: { code: 7001 } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('AI服务暂时不可用')
    })

    it('should prioritize business code over response msg', () => {
      const error = { response: { status: 400, data: { code: 1001, msg: 'Custom msg' } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('用户不存在')
    })

    it('should show data.msg when no business code matches', () => {
      const error = { response: { status: 400, data: { msg: '自定义服务端消息' } } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('自定义服务端消息')
    })

    it('should show http 400 message when no business code and no msg', () => {
      const error = { response: { status: 400, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('请求参数错误')
    })

    it('should show http 401 message', () => {
      const error = { response: { status: 401, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('登录已过期，请重新登录')
    })

    it('should show http 403 message', () => {
      const error = { response: { status: 403, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('权限不足，无法访问')
    })

    it('should show http 404 message', () => {
      const error = { response: { status: 404, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('请求的资源不存在')
    })

    it('should show http 408 message', () => {
      const error = { response: { status: 408, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('请求超时，请重试')
    })

    it('should show http 429 message', () => {
      const error = { response: { status: 429, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('请求过于频繁，请稍后再试')
    })

    it('should show http 500 message', () => {
      const error = { response: { status: 500, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('服务器内部错误')
    })

    it('should show http 502 message', () => {
      const error = { response: { status: 502, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('网关错误')
    })

    it('should show http 503 message', () => {
      const error = { response: { status: 503, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('服务暂时不可用')
    })

    it('should show http 504 message', () => {
      const error = { response: { status: 504, data: {} } }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('网关超时')
    })

    it('should fall through to fallback for unknown status code without msg', () => {
      const error = { response: { status: 418, data: {} } }
      handler.handleApiError(error, '未知错误')
      expect(ElMessage.error).toHaveBeenCalledWith('未知错误')
    })

    it('should show timeout message for timeout error', () => {
      const error = { message: 'timeout of 5000ms exceeded' }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('请求超时，请检查网络连接')
    })

    it('should show network error message', () => {
      const error = { message: 'Network Error' }
      handler.handleApiError(error)
      expect(ElMessage.error).toHaveBeenCalledWith('网络连接失败，请检查网络')
    })

    it('should show fallback for generic error with message', () => {
      const error = { message: 'Some other error' }
      handler.handleApiError(error, '操作失败')
      expect(ElMessage.error).toHaveBeenCalledWith('操作失败')
    })

    it('should show fallback for error with no response and no message', () => {
      handler.handleApiError({}, '默认错误')
      expect(ElMessage.error).toHaveBeenCalledWith('默认错误')
    })
  })

  describe('handleSuccess', () => {
    it('should show default success message', () => {
      handler.handleSuccess()
      expect(ElMessage.success).toHaveBeenCalledWith('操作成功')
    })

    it('should show custom success message', () => {
      handler.handleSuccess('保存成功')
      expect(ElMessage.success).toHaveBeenCalledWith('保存成功')
    })
  })

  describe('handleWarning', () => {
    it('should show warning message', () => {
      handler.handleWarning('注意')
      expect(ElMessage.warning).toHaveBeenCalledWith('注意')
    })
  })

  describe('handleInfo', () => {
    it('should show info message', () => {
      handler.handleInfo('提示信息')
      expect(ElMessage.info).toHaveBeenCalledWith('提示信息')
    })
  })

  describe('withErrorHandling', () => {
    it('should return success result when async fn resolves', async () => {
      const fn = vi.fn().mockResolvedValue('data')
      const result = await handler.withErrorHandling(fn)
      expect(result).toEqual({ success: true, data: 'data' })
      expect(ElMessage.success).not.toHaveBeenCalled()
    })

    it('should show success message when showSuccess is true', async () => {
      const fn = vi.fn().mockResolvedValue('data')
      const result = await handler.withErrorHandling(fn, {
        showSuccess: true,
        successMessage: '保存成功'
      })
      expect(result).toEqual({ success: true, data: 'data' })
      expect(ElMessage.success).toHaveBeenCalledWith('保存成功')
    })

    it('should not show success message when showSuccess is false', async () => {
      const fn = vi.fn().mockResolvedValue('data')
      await handler.withErrorHandling(fn, { successMessage: '保存成功' })
      expect(ElMessage.success).not.toHaveBeenCalled()
    })

    it('should not show success message when no successMessage provided', async () => {
      const fn = vi.fn().mockResolvedValue('data')
      await handler.withErrorHandling(fn, { showSuccess: true })
      expect(ElMessage.success).not.toHaveBeenCalled()
    })

    it('should return failure result when async fn rejects', async () => {
      const error = new Error('fail')
      const fn = vi.fn().mockRejectedValue(error)
      const result = await handler.withErrorHandling(fn)
      expect(result).toEqual({ success: false, error })
    })

    it('should show error message when fn rejects', async () => {
      const error = { response: { status: 500, data: {} } }
      const fn = vi.fn().mockRejectedValue(error)
      await handler.withErrorHandling(fn, { errorMessage: '自定义失败' })
      expect(ElMessage.error).toHaveBeenCalledWith('服务器内部错误')
    })

    it('should use custom error message for non-HTTP errors', async () => {
      const error = { message: 'generic error' }
      const fn = vi.fn().mockRejectedValue(error)
      await handler.withErrorHandling(fn, { errorMessage: '自定义失败' })
      expect(ElMessage.error).toHaveBeenCalledWith('自定义失败')
    })
  })

  describe('exported constants', () => {
    it('should export ERROR_MESSAGES and BUSINESS_MESSAGES', () => {
      expect(handler.ERROR_MESSAGES).toBeDefined()
      expect(handler.ERROR_MESSAGES[400]).toBe('请求参数错误')
      expect(handler.ERROR_MESSAGES[500]).toBe('服务器内部错误')
      expect(handler.BUSINESS_MESSAGES).toBeDefined()
      expect(handler.BUSINESS_MESSAGES[1001]).toBe('用户不存在')
    })
  })
})
