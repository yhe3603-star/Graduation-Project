/**
 * @file 文件上传组合式函数
 * @description 提供文件上传的通用逻辑，支持图片、视频、文档三种类型
 * @author Dong Medicine Team
 * @version 1.0.0
 *
 * 功能说明：
 * 1. 统一管理上传状态（文件列表、上传进度、上传中标志）
 * 2. 统一计算上传URL和请求头（含Authorization）
 * 3. 统一处理上传前校验（格式、大小、数量、替换确认）
 * 4. 统一处理上传成功/失败回调
 * 5. 统一处理文件删除（服务器删除 + 列表更新）
 * 6. 统一处理modelValue双向绑定
 *
 * 使用示例：
 * ```js
 * const { fileList, uploading, uploadUrl, headers, ... } = useFileUpload({
 *   type: 'image',
 *   extensions: ['jpg', 'jpeg', 'png'],
 *   extensionLabel: 'jpg/jpeg/png',
 *   uploadPath: '/upload/image',
 *   props,
 *   emit,
 * })
 * ```
 */

import { ref, computed, watch, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResourceUrl, parseMediaList, getMediaType, logDeleteWarn } from '@/utils'

/**
 * 文件上传组合式函数
 * @param {Object} config - 配置对象
 * @param {string} config.type - 文件类型: 'image' | 'video' | 'document'
 * @param {string[]} config.extensions - 允许的扩展名列表（不含点号）
 * @param {string} config.extensionLabel - 扩展名显示文本
 * @param {string} config.uploadPath - 上传API路径，如 '/upload/image'
 * @param {boolean} [config.simulateProgress=false] - 是否模拟进度条（图片上传使用）
 * @param {Object} config.props - 组件props对象
 * @param {Function} config.emit - 组件emit函数
 * @returns {Object} 上传相关的响应式状态和方法
 */
export function useFileUpload(config) {
  const {
    type = 'image',
    extensions = [],
    extensionLabel = '',
    uploadPath = '/upload/image',
    simulateProgress = false,
    props,
    emit,
  } = config

  // 类型标签映射
  const typeLabelMap = { image: '图片', video: '视频', document: '文档' }
  const typeLabel = typeLabelMap[type] || '文件'
  // 量词映射
  const unitMap = { image: '张', video: '个', document: '个' }
  const unit = unitMap[type] || '个'

  // ==================== 响应式状态 ====================
  const fileList = ref([])
  const uploading = ref(false)
  const uploadProgress = ref(0)
  const progressInterval = ref(null)

  // ==================== 计算属性 ====================
  const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}${uploadPath}`)

  const headers = computed(() => ({
    Authorization: sessionStorage.getItem('token') ? `Bearer ${sessionStorage.getItem('token')}` : ''
  }))

  const limitReached = computed(() => fileList.value.length >= props.limit)

  const tipText = computed(() =>
    `支持 ${extensionLabel} 格式，单个${typeLabel}不超过 ${props.maxSize}MB，最多 ${props.limit} ${unit}`
  )

  // ==================== 同步modelValue到fileList ====================
  watch(() => props.modelValue, (newVal) => {
    if (!newVal) { fileList.value = []; return }
    const items = parseMediaList(newVal)
    fileList.value = items.map(item => ({
      url: item.url,
      path: item.path,
      name: item.name || item.originalFileName,
      ...(type === 'document' ? { type: item.type } : {}),
      size: item.size
    }))
  }, { immediate: true })

  // ==================== 上传前校验 ====================
  async function handleBeforeUpload(file) {
    // 格式校验
    const extension = file.name.split('.').pop().toLowerCase()
    if (!extensions.includes(extension)) {
      ElMessage.error(`不支持的${typeLabel}格式: ${extension}`)
      return false
    }

    // 大小校验
    if (file.size > props.maxSize * 1024 * 1024) {
      ElMessage.error(`${typeLabel}大小不能超过 ${props.maxSize}MB`)
      return false
    }

    // 替换确认
    if (props.replaceConfirm && fileList.value.length > 0) {
      try {
        await ElMessageBox.confirm(
          '已存在上传的文件，是否替换为新文件？',
          '替换确认',
          { confirmButtonText: '替换', cancelButtonText: '取消', type: 'warning' }
        )
        // 删除已有文件
        for (const item of fileList.value) {
          if (item?.path && request) {
            const isExternalUrl = item.path.startsWith('http://') || item.path.startsWith('https://')
            if (!isExternalUrl) {
              try { await request.delete('/upload', { params: { filePath: item.path } }) }
              catch { logDeleteWarn(typeLabel, item.path) }
            }
          }
        }
        fileList.value = []
      } catch {
        return false
      }
    }

    // 数量校验
    if (fileList.value.length >= props.limit) {
      ElMessage.warning(`最多只能上传 ${props.limit} ${unit}${typeLabel}`)
      return false
    }

    uploading.value = true
    uploadProgress.value = 0

    // 图片上传使用模拟进度
    if (simulateProgress) {
      progressInterval.value = setInterval(() => {
        if (uploadProgress.value < 90) uploadProgress.value += 10
      }, 100)
    }

    return true
  }

  // ==================== 上传进度（视频/文档使用真实进度） ====================
  function handleProgress(event) {
    uploadProgress.value = Math.round(event.percent)
  }

  // ==================== 上传成功 ====================
  function handleSuccess(response, file) {
    // 清理进度
    if (simulateProgress) {
      if (progressInterval.value) { clearInterval(progressInterval.value); progressInterval.value = null }
      uploadProgress.value = 100
      setTimeout(() => { uploading.value = false; uploadProgress.value = 0 }, 500)
    } else {
      uploading.value = false
      uploadProgress.value = 0
    }

    if (response.code === 200 && response.data) {
      const filePath = response.data.filePath || response.data.fileUrl
      const fileData = {
        url: getResourceUrl(response.data.fileUrl || filePath),
        path: filePath,
        name: response.data.originalFileName || file.name,
        size: response.data.fileSize || file.size || 0
      }
      // 文档类型额外添加type字段
      if (type === 'document') {
        fileData.type = getMediaType(filePath)
      }

      fileList.value.push(fileData)
      updateModelValue()
      emit('success', response.data)
      ElMessage.success(`${typeLabel}上传成功`)
    } else {
      ElMessage.error(response.msg || '上传失败')
      emit('error', response.msg)
    }
  }

  // ==================== 上传失败 ====================
  function handleError(error) {
    if (simulateProgress && progressInterval.value) {
      clearInterval(progressInterval.value)
      progressInterval.value = null
    }
    uploading.value = false
    uploadProgress.value = 0
    console.error(`${typeLabel}上传失败:`, error)
    ElMessage.error(`${typeLabel}上传失败，请重试`)
    emit('error', error)
  }

  // ==================== 删除文件 ====================
  async function handleRemove(index) {
    const removed = fileList.value[index]
    if (removed?.path && request) {
      const isExternalUrl = removed.path.startsWith('http://') || removed.path.startsWith('https://')
      if (!isExternalUrl) {
        try { await request.delete('/upload', { params: { filePath: removed.path } }) }
        catch { logDeleteWarn(typeLabel, removed.path) }
      }
    }
    fileList.value.splice(index, 1)
    updateModelValue()
    emit('remove', removed)
    return removed
  }

  // ==================== 更新modelValue ====================
  function updateModelValue() {
    const items = fileList.value.map(item => {
      const base = { path: item.path, name: item.name, size: item.size }
      if (type === 'document') base.type = item.type
      return base
    })
    emit('update:modelValue', props.multiple ? items : items[0] || null)
    emit('change', items)
  }

  // ==================== 清空文件列表 ====================
  function clearFiles() {
    fileList.value = []
    emit('update:modelValue', props.multiple ? [] : '')
    emit('change', [])
  }

  // ==================== 组件卸载时清理定时器 ====================
  onUnmounted(() => {
    if (progressInterval.value) {
      clearInterval(progressInterval.value)
      progressInterval.value = null
    }
  })

  return {
    // 响应式状态
    fileList,
    uploading,
    uploadProgress,
    // 计算属性
    uploadUrl,
    headers,
    limitReached,
    tipText,
    // 方法
    handleBeforeUpload,
    handleProgress,
    handleSuccess,
    handleError,
    handleRemove,
    updateModelValue,
    clearFiles,
  }
}
