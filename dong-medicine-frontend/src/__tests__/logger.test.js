import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  logUploadError, logDeleteWarn, logAuthWarn, logSecurityWarn,
  logFetchError, logOperationWarn, logAutoPlayWarn, logPermissionWarn
} from '@/utils/logger'

describe('formatter functions', () => {
  beforeEach(() => {
    vi.stubGlobal('console', { warn: vi.fn(), error: vi.fn() })
  })

  it('logFetchError should format error message', () => {
    logFetchError('首页数据', new Error('timeout'))
    expect(console.error).toHaveBeenCalledWith('[ERROR]', '首页数据数据获取失败:', 'timeout')
  })

  it('logFetchError should handle string error', () => {
    logFetchError('导出', 'network error')
    expect(console.error).toHaveBeenCalledWith('[ERROR]', '导出数据获取失败:', 'network error')
  })

  it('logUploadError should format', () => {
    logUploadError('图片', new Error('size exceeded'))
    expect(console.error).toHaveBeenCalledWith('[ERROR]', '图片上传失败:', 'size exceeded')
  })

  it('logDeleteWarn should format', () => {
    logDeleteWarn('文件', '/path/to/file.pdf')
    expect(console.warn).toHaveBeenCalledWith('[WARN]', '文件服务器文件删除失败:', '/path/to/file.pdf')
  })

  it('logAuthWarn should format', () => {
    logAuthWarn('token expired')
    expect(console.warn).toHaveBeenCalledWith('[WARN]', '权限不足或未登录:', 'token expired')
  })

  it('logSecurityWarn should format with field name and value', () => {
    logSecurityWarn('username', '<script>alert(1)</script>')
    expect(console.warn).toHaveBeenCalledWith('[WARN]', '检测到潜在攻击 - 字段: username, 值: <script>alert(1)</script>')
  })

  it('logOperationWarn should format', () => {
    logOperationWarn('删除文件')
    expect(console.warn).toHaveBeenCalledWith('[WARN]', '删除文件失败')
  })

  it('logAutoPlayWarn should format', () => {
    logAutoPlayWarn(new Error('not allowed'))
    expect(console.warn).toHaveBeenCalledWith('[WARN]', '自动播放失败，可能需要用户交互:', 'not allowed')
  })

  it('logPermissionWarn should format', () => {
    logPermissionWarn('管理后台')
    expect(console.warn).toHaveBeenCalledWith('[WARN]', '加载管理后台失败，可能没有权限')
  })

  it('logUploadError should handle non-Error objects', () => {
    logUploadError('视频', 'File too large')
    expect(console.error).toHaveBeenCalledWith('[ERROR]', '视频上传失败:', 'File too large')
  })

  it('logFetchError should handle null/undefined error', () => {
    logFetchError('登录', null)
    expect(console.error).toHaveBeenCalledWith('[ERROR]', '登录数据获取失败:', null)
  })
})
