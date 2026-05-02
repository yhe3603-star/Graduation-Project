import { ref, computed } from 'vue'
import { parseMediaList, getMediaType, separateMediaByType, normalizeUrl, getFileName } from '@/utils/media'

export function useDocumentPreview() {
  const previewVisible = ref(false)
  const previewDocument = ref(null)

  const openPreview = (doc) => {
    previewDocument.value = {
      filePath: doc.filePath || doc.url,
      fileName: doc.originalFileName || doc.fileName || doc.name,
      fileType: doc.fileType || doc.type
    }
    previewVisible.value = true
  }

  const closePreview = () => {
    previewVisible.value = false
    previewDocument.value = null
  }

  return { previewVisible, previewDocument, openPreview, closePreview }
}

export function useMediaTabs(defaultTab = 'image') {
  const activeTab = ref(defaultTab)
  const videoPlayerRef = ref(null)

  const isVideoTab = computed(() => activeTab.value === 'video')

  const handleTabChange = (tabName) => {
    if (tabName !== 'video' && videoPlayerRef.value) {
      videoPlayerRef.value.pause()
    }
  }

  const pauseVideo = () => {
    if (videoPlayerRef.value) videoPlayerRef.value.pause()
  }

  return { activeTab, videoPlayerRef, isVideoTab, handleTabChange, pauseVideo }
}

export function useDocumentList(parseDocumentList) {
  const documentList = ref([])
  const documentsLoading = ref(false)

  const loadDocuments = (docs) => {
    documentsLoading.value = true
    try {
      documentList.value = parseDocumentList(docs)
    } finally {
      documentsLoading.value = false
    }
  }

  return { documentList, documentsLoading, loadDocuments }
}

export function useMediaDisplay(data, options = {}) {
  const { filesField = 'files' } = options

  const allFiles = computed(() => {
    if (!data.value) return []
    const filesData = data.value[filesField]
    return parseMediaList(filesData)
  })

  const imageList = computed(() => separateMediaByType(allFiles.value).images)
  const videoList = computed(() => separateMediaByType(allFiles.value).videos)
  const documentList = computed(() => separateMediaByType(allFiles.value).documents)

  const imagePreviewList = computed(() => imageList.value.map(img => img.url))

  const hasImages = computed(() => imageList.value.length > 0)
  const hasVideos = computed(() => videoList.value.length > 0)
  const hasDocuments = computed(() => documentList.value.length > 0)
  const hasMedia = computed(() => hasImages.value || hasVideos.value || hasDocuments.value)

  return { allFiles, imageList, videoList, documentList, imagePreviewList, hasImages, hasVideos, hasDocuments, hasMedia }
}

export function useFileInfo(data, options = {}) {
  const { filesField = 'files', urlField = 'fileUrl', nameField = 'fileName', sizeField = 'fileSize' } = options

  const fileInfo = computed(() => {
    if (!data.value) return { url: '', path: '', name: '', size: 0, type: 'document' }
    
    if (data.value[filesField]) {
      const files = parseMediaList(data.value[filesField])
      if (files.length > 0) return files[0]
    }
    
    if (data.value[urlField]) {
      return {
        url: normalizeUrl(data.value[urlField]),
        path: data.value[urlField],
        name: data.value[nameField] || '',
        size: data.value[sizeField] || 0,
        type: getMediaType(data.value[urlField])
      }
    }
    
    return { url: '', path: '', name: '', size: 0, type: 'document' }
  })

  const fileUrl = computed(() => fileInfo.value.url)
  const fileName = computed(() => fileInfo.value.name)
  const fileSize = computed(() => fileInfo.value.size)
  const fileType = computed(() => fileInfo.value.type)
  const fileExt = computed(() => {
    const url = fileInfo.value.url
    if (!url) return ''
    return url.split('.').pop()?.toLowerCase() || ''
  })

  return { fileInfo, fileUrl, fileName, fileSize, fileType, fileExt }
}
