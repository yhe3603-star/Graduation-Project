const isDev = import.meta.env.DEV

const logger = {
  log: (...args) => isDev && console.log('[LOG]', ...args),
  warn: (...args) => console.warn('[WARN]', ...args),
  error: (...args) => console.error('[ERROR]', ...args),
  debug: (...args) => isDev && console.debug('[DEBUG]', ...args),
  info: (...args) => isDev && console.info('[INFO]', ...args)
}

export const logUploadError = (type, error) => {
  logger.error(`${type}上传失败:`, error?.message || error)
}

export const logDeleteWarn = (type, path) => {
  logger.warn(`${type}服务器文件删除失败:`, path)
}

export const logAuthWarn = (msg) => {
  logger.warn('权限不足或未登录:', msg)
}

export const logSecurityWarn = (key, value) => {
  logger.warn(`检测到潜在攻击 - 字段: ${key}, 值: ${value}`)
}

export const logFetchError = (context, error) => {
  logger.error(`${context}数据获取失败:`, error?.message || error)
}

export const logOperationWarn = (operation) => {
  logger.warn(`${operation}失败`)
}

export const logAutoPlayWarn = (e) => {
  logger.warn('自动播放失败，可能需要用户交互:', e?.message || e)
}

export const logPermissionWarn = (resource) => {
  logger.warn(`加载${resource}失败，可能没有权限`)
}

export default logger
