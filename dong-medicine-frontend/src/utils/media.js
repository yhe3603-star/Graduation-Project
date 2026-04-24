/**
 * @file 媒体处理工具函数
 * @description 提供媒体文件相关的处理功能，包括类型判断、URL处理、文件解析等
 * @author Dong Medicine Team
 * @version 1.0.0
 * 
 * @module utils/media
 * 
 * @requires @element-plus/icons-vue
 * 
 * 功能说明：
 * 1. 文件类型判断：根据文件扩展名判断文件类型
 * 2. 媒体类型分类：将文件分为视频、图片、文档三类
 * 3. URL处理：规范化URL路径
 * 4. 媒体列表解析：解析JSON或逗号分隔的媒体列表
 * 5. 文件下载：触发文件下载
 * 
 * 支持的文件类型：
 * - 视频：mp4, avi, mov, wmv, flv, mkv
 * - 图片：jpg, jpeg, png, gif, bmp, webp, svg
 * - 文档：pdf, doc, docx, xls, xlsx, ppt, pptx, txt
 */

import { Document, DocumentCopy, Tickets, DataBoard, Picture, VideoPlay, FolderOpened } from '@element-plus/icons-vue'

const VIDEO_EXTS = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv']
const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
const DOC_EXTS = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt']

const FILE_TYPE_MAP = {
  pdf: 'pdf',
  doc: 'word', docx: 'word',
  xls: 'excel', xlsx: 'excel',
  ppt: 'ppt', pptx: 'ppt',
  txt: 'txt'
}

const FILE_TYPE_DISPLAY_MAP = {
  pdf: 'PDF',
  doc: 'Word', docx: 'Word',
  xls: 'Excel', xlsx: 'Excel',
  ppt: 'PPT', pptx: 'PPT',
  txt: 'TXT'
}

const MEDIA_TYPE_MAP = {
  video: VIDEO_EXTS,
  image: IMAGE_EXTS,
  document: DOC_EXTS
}

export const FILE_ICONS = {
  pdf: Tickets,
  word: DocumentCopy,
  excel: DataBoard,
  ppt: Document,
  txt: Document,
  image: Picture,
  video: VideoPlay,
  audio: FolderOpened,
  other: Document
}

export const FILE_COLORS = {
  pdf: '#f56c6c',
  word: '#409eff',
  excel: '#67c23a',
  ppt: '#e6a23c',
  txt: '#909399',
  image: '#28B463',
  video: '#e74c3c',
  audio: '#9b59b6',
  other: '#909399'
}

export const FILE_TYPE_NAMES = {
  pdf: 'PDF',
  word: 'Word',
  excel: 'Excel',
  ppt: 'PPT',
  txt: 'TXT',
  image: '图片',
  video: '视频',
  audio: '音频',
  other: '其他'
}

/**
 * 获取文件图标组件
 * @param {string} type - 文件类型
 * @returns {Component} Element Plus图标组件
 */
export const getFileIcon = (type) => FILE_ICONS[type] || Document

/**
 * 获取文件颜色
 * @param {string} type - 文件类型
 * @returns {string} 十六进制颜色值
 */
export const getFileColor = (type) => FILE_COLORS[type] || '#909399'

/**
 * 获取文件类型显示名称
 * @param {string} type - 文件类型
 * @returns {string} 显示名称
 */
export const getFileTypeName = (type) => FILE_TYPE_NAMES[type] || '其他'

/**
 * 根据文件路径获取文件类型
 * @param {string} path - 文件路径
 * @returns {string} 文件类型（pdf/word/excel/ppt/txt/other）
 */
export const getFileType = (path) => {
  if (!path) return 'other'
  const ext = path.split('.').pop()?.toLowerCase()
  return FILE_TYPE_MAP[ext] || 'other'
}

/**
 * 根据文件路径获取文件类型显示名称
 * @param {string} path - 文件路径
 * @returns {string} 文件类型显示名称
 */
export const getFileTypeDisplay = (path) => {
  if (!path) return '其他'
  const ext = path.split('.').pop()?.toLowerCase()
  return FILE_TYPE_DISPLAY_MAP[ext] || '其他'
}

/**
 * 根据文件路径获取媒体类型
 * @param {string} path - 文件路径
 * @returns {string} 媒体类型（video/image/document）
 */
export const getMediaType = (path) => {
  if (!path) return 'document'
  const ext = path.split('.').pop()?.toLowerCase()
  if (VIDEO_EXTS.includes(ext)) return 'video'
  if (IMAGE_EXTS.includes(ext)) return 'image'
  return 'document'
}

/**
 * 根据扩展名获取媒体类型
 * @param {string} ext - 文件扩展名
 * @returns {string} 媒体类型
 */
export const getMediaTypeByExt = (ext) => {
  if (!ext) return 'document'
  const lowerExt = ext.toLowerCase().replace('.', '')
  if (VIDEO_EXTS.includes(lowerExt)) return 'video'
  if (IMAGE_EXTS.includes(lowerExt)) return 'image'
  if (DOC_EXTS.includes(lowerExt)) return 'document'
  return 'document'
}

/**
 * 根据媒体类型获取支持的扩展名列表
 * @param {string} type - 媒体类型
 * @returns {Array<string>} 扩展名数组
 */
export const getExtensionsByType = (type) => MEDIA_TYPE_MAP[type] || []

/**
 * 规范化URL路径
 * @param {string} path - 原始路径
 * @returns {string} 规范化后的路径
 */
export const normalizeUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return path.startsWith('/') ? path : '/' + path
}

/**
 * 获取文件扩展名
 * @param {string} path - 文件路径
 * @returns {string} 文件扩展名（不含点）
 */
export const getFileExt = (path) => {
  if (!path) return ''
  const lastDot = path.lastIndexOf('.')
  if (lastDot === -1) return ''
  return path.substring(lastDot + 1).toLowerCase()
}

/**
 * 从URL中提取文件名
 * @param {string} url - 文件URL
 * @returns {string} 文件名
 */
export const getFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

/**
 * 解析媒体列表数据
 * @param {string|Array|Object} data - 媒体数据（JSON字符串、数组或对象）
 * @returns {Array<Object>} 标准化的媒体对象数组
 * @example
 * parseMediaList('["path1", "path2"]')
 * parseMediaList('path1, path2, path3')
 * parseMediaList([{path: 'path1', name: 'file1'}])
 */
export function parseMediaList(data) {
  if (!data) return []
  
  let items = []
  if (Array.isArray(data)) {
    items = data.filter(Boolean)
  } else if (typeof data === 'string' && data) {
    const trimmed = data.trim()
    if (trimmed.startsWith('[')) {
      try {
        const parsed = JSON.parse(trimmed)
        items = Array.isArray(parsed) ? parsed.filter(Boolean) : []
      } catch {
        return []
      }
    } else {
      items = trimmed.split(',').map(v => v.trim()).filter(Boolean)
    }
  } else if (typeof data === 'object' && data !== null) {
    items = [data]
  } else {
    return []
  }
  
  return items.map(item => {
    let path, originalName, fileSize, mediaType
    
    if (typeof item === 'string') {
      path = item
      originalName = getFileName(item)
      fileSize = 0
      mediaType = getMediaType(path)
    } else if (typeof item === 'object' && item !== null) {
      path = item.path || item.url || item.filePath || ''
      originalName = item.name || item.originalFileName || item.fileName || getFileName(path)
      fileSize = item.size || item.fileSize || 0
      const rawType = item.type || item.mediaType
      if (rawType && ['video', 'image', 'document'].includes(rawType)) {
        mediaType = rawType
      } else {
        mediaType = getMediaType(path)
      }
    } else {
      return null
    }
    
    return {
      url: normalizeUrl(path),
      path: path,
      name: originalName,
      originalFileName: originalName,
      size: fileSize,
      type: mediaType
    }
  }).filter(Boolean)
}

/**
 * 序列化媒体列表为JSON字符串
 * @param {Array} items - 媒体对象数组
 * @returns {string} JSON字符串
 */
export function stringifyMediaList(items) {
  if (!items || !Array.isArray(items)) return '[]'
  
  const formatted = items.map(item => {
    if (typeof item === 'string') {
      return { path: item, name: getFileName(item), size: 0, type: getMediaType(item) }
    }
    return {
      path: item.path || item.url || '',
      name: item.name || item.originalFileName || getFileName(item.path || ''),
      size: item.size || 0,
      type: item.type || getMediaType(item.path || '')
    }
  })
  
  return JSON.stringify(formatted)
}

/**
 * 触发媒体文件下载
 * @param {Object} media - 媒体对象
 * @param {string} media.url - 文件URL
 * @param {string} media.path - 文件路径
 * @param {string} media.name - 文件名
 */
export const downloadMedia = (media) => {
  if (!media?.url && !media?.path) return
  const link = document.createElement('a')
  link.href = normalizeUrl(media.url || media.path)
  link.download = media.name || media.originalFileName || 'file'
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 获取文件信息
 * @param {Object} data - 包含文件信息的对象
 * @returns {Object} 标准化的文件信息对象
 */
export function getFileInfo(data) {
  if (!data) return { url: '', path: '', name: '', size: 0, type: 'document' }
  
  const files = parseMediaList(data.files || data)
  if (files.length > 0) {
    return files[0]
  }
  
  if (data.fileUrl) {
    return {
      url: normalizeUrl(data.fileUrl),
      path: data.fileUrl,
      name: data.fileName || getFileName(data.fileUrl),
      size: data.fileSize || 0,
      type: getMediaType(data.fileUrl)
    }
  }
  
  return { url: '', path: '', name: '', size: 0, type: 'document' }
}

/**
 * 获取所有文件列表
 * @param {Object} data - 包含文件信息的对象
 * @returns {Array} 文件信息数组
 */
export function getAllFiles(data) {
  if (!data) return []
  
  if (data.files) {
    return parseMediaList(data.files)
  }
  
  const fileInfo = getFileInfo(data)
  return fileInfo.url ? [fileInfo] : []
}

/**
 * 按类型分离媒体文件
 * @param {Array} files - 文件数组
 * @returns {Object} 分离后的对象 { images, videos, documents }
 */
export function separateMediaByType(files) {
  const result = { images: [], videos: [], documents: [] }
  
  if (!files || !Array.isArray(files)) return result
  
  files.forEach(file => {
    const type = file.type || getMediaType(file.path || file.url || '')
    const fileObj = {
      ...file,
      url: normalizeUrl(file.url || file.path),
      type
    }
    
    if (type === 'image') {
      result.images.push(fileObj)
    } else if (type === 'video') {
      result.videos.push(fileObj)
    } else {
      result.documents.push(fileObj)
    }
  })
  
  return result
}

export const parseMediaPaths = parseMediaList
export const parseImageList = parseMediaList
export const parseVideoList = parseMediaList
/** @see parseMediaList - 解析文档列表，功能与 parseMediaList 相同 */
export const parseDocumentList = parseMediaList

export const stringifyImageList = stringifyMediaList
export const stringifyVideoList = stringifyMediaList
export const stringifyDocumentList = stringifyMediaList

export const downloadDocument = downloadMedia

export const getResourceUrl = normalizeUrl

export function parseResourceFiles(data) {
  return getAllFiles(data)
}

export function stringifyResourceFiles(files) {
  return stringifyMediaList(files)
}
