/**
 * @file 工具函数统一导出
 * @description 统一导出所有工具函数，便于引用和管理
 * @author Dong Medicine Team
 * @version 1.0.0
 * 
 * 工具函数分类说明：
 * 
 * 1. 数据处理
 * - formatTime: 时间格式化
 * - formatFileSize: 文件大小格式化
 * - extractData: 响应数据提取
 * - truncate: 文本截断
 * 
 * 2. 媒体处理
 * - parseMediaList: 媒体列表解析
 * - stringifyMediaList: 媒体列表序列化
 * - getMediaType: 获取媒体类型
 * - separateMediaByType: 按类型分离媒体
 * - downloadMedia: 媒体下载
 * 
 * 3. 文件处理
 * - getFileType: 获取文件类型
 * - getFileIcon: 获取文件图标
 * - getFileColor: 获取文件颜色
 * - getFileName: 获取文件名
 * 
 * 4. 通用工具
 * - debounce: 防抖函数
 * - throttle: 节流函数
 * - deepClone: 深拷贝
 * - isEmpty/isNotEmpty: 空值判断
 * - generateId: 生成唯一ID
 * - sleep: 延迟函数
 * - retry: 重试函数
 * 
 * 5. 常量
 * - PLACEHOLDER_IMG: 占位图片
 * - DEFAULT_AVATAR: 默认头像
 * - DEFAULT_VIDEO_COVER: 默认视频封面
 * - DEFAULT_DOCUMENT: 默认文档图标
 */

export function formatTime(time, options = {}) {
  if (!time) return "-"
  const date = new Date(time)
  const diff = Date.now() - date

  if (options.relative !== false && options.format !== 'full') {
    if (diff < 60000) return "刚刚"
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  }

  const timeStr = date.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" })
  const formats = {
    date: () => date.toLocaleDateString("zh-CN"),
    time: () => timeStr,
    datetime: () => date.toLocaleDateString("zh-CN") + " " + timeStr
  }
  return formats[options.format]?.() ?? date.toLocaleString("zh-CN")
}

export function extractData(res) {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.data)) return res.data
  if (Array.isArray(res.records)) return res.records
  if (res.data?.records && Array.isArray(res.data.records)) return res.data.records
  if (res.data?.data) return Array.isArray(res.data.data) ? res.data.data : []
  if (res.data?.data?.records && Array.isArray(res.data.data.records)) return res.data.data.records
  if (res.data?.list) return Array.isArray(res.data.list) ? res.data.list : []
  return []
}

export function extractPageData(res) {
  if (!res) return { records: [], total: 0, page: 1, size: 12 }
  if (res.data?.records) {
    return {
      records: res.data.records,
      total: res.data.total || res.data.records.length,
      page: res.data.page || res.data.current || 1,
      size: res.data.size || 12
    }
  }
  if (res.data?.data?.records) {
    return {
      records: res.data.data.records,
      total: res.data.data.total || res.data.data.records.length,
      page: res.data.data.page || res.data.data.current || 1,
      size: res.data.data.size || 12
    }
  }
  const records = extractData(res)
  return { records, total: records.length, page: 1, size: 12 }
}

export function getRankClass(index) {
  return ["gold", "silver", "bronze"][index] ?? ""
}

const SCORE_CONFIG = {
  excellent: { min: 90, emoji: "🏆", text: "太棒了！你是侗医药达人！" },
  good: { min: 70, emoji: "😊", text: "不错哦！继续加油！" },
  pass: { min: 60, emoji: "🙂", text: "及格啦！再接再厉！" },
  fail: { min: 0, emoji: "💪", text: "还需努力，多学习侗医药知识吧！" }
}

const getScoreConfig = (score) => {
  for (const [key, cfg] of Object.entries(SCORE_CONFIG)) {
    if (score >= cfg.min) return cfg
  }
  return SCORE_CONFIG.fail
}

export const getScoreLevel = (score) => {
  for (const [key, cfg] of Object.entries(SCORE_CONFIG)) {
    if (score >= cfg.min) return key
  }
  return "fail"
}

export const getScoreEmoji = (score) => getScoreConfig(score).emoji
export const getScoreText = (score) => getScoreConfig(score).text

export const PLACEHOLDER_IMG = "/static/defaults/default-plant.svg"
export const DEFAULT_AVATAR = "/static/defaults/default-avatar.svg"
export const DEFAULT_VIDEO_COVER = "/static/defaults/default-video-cover.svg"
export const DEFAULT_DOCUMENT = "/static/defaults/default-document.svg"

export const getImageUrl = (path) => path ? (path.startsWith('http://') || path.startsWith('https://') ? path : (path.startsWith('/') ? path : '/' + path)) : PLACEHOLDER_IMG

export const getFirstImage = (images) => {
  if (!images) return PLACEHOLDER_IMG
  const list = typeof images === 'string' ? (images.trim().startsWith('[') ? (() => { try { const p = JSON.parse(images); return Array.isArray(p) ? p : [] } catch { return [] } } )() : images.split(',').map(s => s.trim()).filter(Boolean)) : (Array.isArray(images) ? images : [])
  return list.length > 0 ? getImageUrl(typeof list[0] === 'string' ? list[0] : (list[0].url || list[0].path)) : PLACEHOLDER_IMG
}

export function formatFileSize(bytes) {
  if (!bytes) return "0 B"
  const units = ["B", "KB", "MB", "GB"]
  let size = bytes, i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}

export const truncate = (str, length = 100, suffix = '...') =>
  str?.length > length ? str.substring(0, length) + suffix : (str ?? '')

export function debounce(func, wait = 300) {
  let timeout
  return (...args) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}

export function throttle(func, limit = 300) {
  let inThrottle
  return (...args) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (Array.isArray(obj)) return obj.map(deepClone)
  return Object.fromEntries(
    Object.entries(obj).map(([k, v]) => [k, deepClone(v)])
  )
}

export function isEmpty(value) {
  if (value == null) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

export const isNotEmpty = (value) => !isEmpty(value)

export const generateId = () => Date.now().toString(36) + Math.random().toString(36).slice(2)

export const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

export function retry(func, retries = 3, delay = 1000) {
  return new Promise((resolve, reject) => {
    const attempt = (n) => {
      func().then(resolve).catch(err => n === 0 ? reject(err) : setTimeout(() => attempt(n - 1), delay))
    }
    attempt(retries)
  })
}

export {
  normalizeUrl, getFileName, getFileType, getFileTypeDisplay, getMediaType,
  getMediaTypeByExt, getExtensionsByType, parseMediaList, stringifyMediaList,
  downloadMedia, downloadDocument, getFileInfo, getAllFiles, separateMediaByType,
  getFileIcon, getFileColor, getFileTypeName, getFileExt,
  parseImageList, parseVideoList, parseDocumentList,
  stringifyImageList, stringifyVideoList, stringifyDocumentList,
  getResourceUrl, parseResourceFiles, stringifyResourceFiles,
  FILE_ICONS, FILE_COLORS, FILE_TYPE_NAMES
} from './media'

export { logUploadError, logDeleteWarn, logAuthWarn, logSecurityWarn, logFetchError, logOperationWarn, logAutoPlayWarn, logPermissionWarn } from './logger'
