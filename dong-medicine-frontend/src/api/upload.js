/**
 * @file 文件上传相关API
 */
import request from '@/utils/request'

/** 上传文件 */
export function uploadFile(formData, config) {
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    ...config
  })
}

/** 删除已上传的文件 */
export function deleteUploadedFile(filePath) {
  return request.delete('/upload', { params: { filePath } })
}
