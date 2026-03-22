import { ElMessage } from 'element-plus'

const ERROR_MESSAGES = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '权限不足，无法访问',
  404: '请求的资源不存在',
  408: '请求超时，请重试',
  429: '请求过于频繁，请稍后再试',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂时不可用',
  504: '网关超时'
}

const BUSINESS_MESSAGES = {
  1001: '用户不存在',
  1002: '用户名已存在',
  1003: '密码错误',
  1004: '密码强度不足',
  1005: '登录已过期',
  1006: '无效的登录凭证',
  1007: '权限不足',
  2001: '资源不存在',
  3001: '参数错误',
  3002: '缺少必要参数',
  4001: '文件上传失败',
  4002: '文件类型不允许',
  4003: '文件大小超出限制',
  5001: '重复操作',
  5002: '操作过于频繁',
  6001: '数据库操作失败',
  7001: 'AI服务暂时不可用',
  9001: '系统繁忙，请稍后再试'
}

export function useErrorHandler() {
  const handleApiError = (error, fallbackMessage = '操作失败') => {
    if (!error) {
      ElMessage.error(fallbackMessage)
      return
    }

    if (error.response) {
      const { status, data } = error.response
      const businessCode = data?.code
      
      if (businessCode && BUSINESS_MESSAGES[businessCode]) {
        ElMessage.error(BUSINESS_MESSAGES[businessCode])
        return
      }
      
      if (data?.msg) {
        ElMessage.error(data.msg)
        return
      }
      
      if (ERROR_MESSAGES[status]) {
        ElMessage.error(ERROR_MESSAGES[status])
        return
      }
    }
    
    if (error.message) {
      if (error.message.includes('timeout')) {
        ElMessage.error('请求超时，请检查网络连接')
        return
      }
      if (error.message.includes('Network Error')) {
        ElMessage.error('网络连接失败，请检查网络')
        return
      }
    }
    
    ElMessage.error(fallbackMessage)
  }

  const handleSuccess = (message = '操作成功') => {
    ElMessage.success(message)
  }

  const handleWarning = (message) => {
    ElMessage.warning(message)
  }

  const handleInfo = (message) => {
    ElMessage.info(message)
  }

  const withErrorHandling = async (fn, options = {}) => {
    const { 
      successMessage, 
      errorMessage = '操作失败',
      showSuccess = false,
      showLoading = false
    } = options

    try {
      const result = await fn()
      if (showSuccess && successMessage) {
        handleSuccess(successMessage)
      }
      return { success: true, data: result }
    } catch (error) {
      handleApiError(error, errorMessage)
      return { success: false, error }
    }
  }

  return {
    handleApiError,
    handleSuccess,
    handleWarning,
    handleInfo,
    withErrorHandling,
    ERROR_MESSAGES,
    BUSINESS_MESSAGES
  }
}

export default useErrorHandler
