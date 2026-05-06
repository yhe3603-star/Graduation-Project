import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'

vi.mock('@/utils/media', () => ({
  parseMediaList: vi.fn(() => []),
  getMediaType: vi.fn(() => 'document'),
  separateMediaByType: vi.fn(() => ({ images: [], videos: [], documents: [] })),
  normalizeUrl: vi.fn(url => url ? '/' + url.replace(/^\//, '') : ''),
  getFileName: vi.fn(url => url ? url.split('/').pop() : '')
}))

import { parseMediaList, getMediaType, separateMediaByType } from '@/utils/media'
import {
  useDocumentPreview,
  useMediaTabs,
  useDocumentList,
  useMediaDisplay,
  useFileInfo
} from '@/composables/useMedia'

describe('useDocumentPreview', () => {
  it('should initialize with hidden preview and null document', () => {
    const { previewVisible, previewDocument } = useDocumentPreview()
    expect(previewVisible.value).toBe(false)
    expect(previewDocument.value).toBeNull()
  })

  it('openPreview should set visible and document', () => {
    const { previewVisible, previewDocument, openPreview } = useDocumentPreview()
    openPreview({ filePath: '/files/test.pdf', originalFileName: 'test.pdf', fileType: 'pdf' })
    expect(previewVisible.value).toBe(true)
    expect(previewDocument.value).toEqual({
      filePath: '/files/test.pdf',
      fileName: 'test.pdf',
      fileType: 'pdf'
    })
  })

  it('openPreview should fallback to url and name fields', () => {
    const { previewDocument, openPreview } = useDocumentPreview()
    openPreview({ url: '/img.png', name: 'img.png', type: 'image' })
    expect(previewDocument.value).toEqual({
      filePath: '/img.png',
      fileName: 'img.png',
      fileType: 'image'
    })
  })

  it('closePreview should reset visible and document', () => {
    const { previewVisible, previewDocument, openPreview, closePreview } = useDocumentPreview()
    openPreview({ filePath: '/test.pdf', originalFileName: 'test.pdf', fileType: 'pdf' })
    closePreview()
    expect(previewVisible.value).toBe(false)
    expect(previewDocument.value).toBeNull()
  })
})

describe('useMediaTabs', () => {
  it('should default to "image" tab', () => {
    const { activeTab, isVideoTab } = useMediaTabs()
    expect(activeTab.value).toBe('image')
    expect(isVideoTab.value).toBe(false)
  })

  it('should accept custom default tab', () => {
    const { activeTab } = useMediaTabs('video')
    expect(activeTab.value).toBe('video')
  })

  it('isVideoTab should be true when activeTab is "video"', () => {
    const { activeTab, isVideoTab } = useMediaTabs()
    activeTab.value = 'video'
    expect(isVideoTab.value).toBe(true)
  })

  it('handleTabChange should pause video when switching away from video', () => {
    const { videoPlayerRef, handleTabChange } = useMediaTabs()
    const mockPause = vi.fn()
    videoPlayerRef.value = { pause: mockPause }
    handleTabChange('image')
    expect(mockPause).toHaveBeenCalledTimes(1)
  })

  it('handleTabChange should not pause when staying on video tab', () => {
    const { videoPlayerRef, handleTabChange } = useMediaTabs()
    const mockPause = vi.fn()
    videoPlayerRef.value = { pause: mockPause }
    handleTabChange('video')
    expect(mockPause).not.toHaveBeenCalled()
  })

  it('handleTabChange should handle missing video player ref', () => {
    const { handleTabChange } = useMediaTabs()
    expect(() => handleTabChange('image')).not.toThrow()
  })

  it('pauseVideo should pause when video player ref exists', () => {
    const { videoPlayerRef, pauseVideo } = useMediaTabs()
    const mockPause = vi.fn()
    videoPlayerRef.value = { pause: mockPause }
    pauseVideo()
    expect(mockPause).toHaveBeenCalledTimes(1)
  })

  it('pauseVideo should handle missing video player ref', () => {
    const { pauseVideo } = useMediaTabs()
    expect(() => pauseVideo()).not.toThrow()
  })
})

describe('useDocumentList', () => {
  it('should initialize with empty list and not loading', () => {
    const { documentList, documentsLoading } = useDocumentList(vi.fn())
    expect(documentList.value).toEqual([])
    expect(documentsLoading.value).toBe(false)
  })

  it('loadDocuments should parse docs and set list', () => {
    const docs = [{ name: 'a.pdf' }, { name: 'b.pdf' }]
    const mockParser = vi.fn().mockReturnValue(docs)
    const { documentList, documentsLoading, loadDocuments } = useDocumentList(mockParser)
    loadDocuments(['raw1', 'raw2'])
    expect(mockParser).toHaveBeenCalledWith(['raw1', 'raw2'])
    expect(documentList.value).toEqual(docs)
    expect(documentsLoading.value).toBe(false)
  })

  it('loadDocuments should set loading false even on error', () => {
    const mockParser = vi.fn().mockImplementation(() => { throw new Error('parse error') })
    const { documentsLoading, loadDocuments } = useDocumentList(mockParser)
    expect(() => loadDocuments([])).toThrow()
    expect(documentsLoading.value).toBe(false)
  })
})

describe('useMediaDisplay', () => {
  beforeEach(() => {
    parseMediaList.mockReset().mockImplementation(() => [])
    separateMediaByType.mockReset().mockImplementation(() => ({ images: [], videos: [], documents: [] }))
  })

  it('should return empty arrays and false flags when data is null', () => {
    const data = ref(null)
    const result = useMediaDisplay(data)
    expect(result.allFiles.value).toEqual([])
    expect(result.hasImages.value).toBe(false)
    expect(result.hasVideos.value).toBe(false)
    expect(result.hasDocuments.value).toBe(false)
    expect(result.hasMedia.value).toBe(false)
  })

  it('should parse files and separate by type', () => {
    const files = [
      { url: '/img1.png', type: 'image' },
      { url: '/v1.mp4', type: 'video' }
    ]
    parseMediaList.mockReturnValue(files)
    separateMediaByType.mockReturnValue({
      images: [{ url: '/img1.png', type: 'image' }],
      videos: [{ url: '/v1.mp4', type: 'video' }],
      documents: []
    })

    const data = ref({ files: '["img1.png","v1.mp4"]' })
    const result = useMediaDisplay(data)

    expect(result.allFiles.value).toEqual(files)
    expect(result.hasImages.value).toBe(true)
    expect(result.hasVideos.value).toBe(true)
    expect(result.hasDocuments.value).toBe(false)
    expect(result.hasMedia.value).toBe(true)
    expect(result.imagePreviewList.value).toEqual(['/img1.png'])
  })

  it('should use custom filesField option', () => {
    const customFiles = [{ url: '/custom.png', type: 'image' }]
    parseMediaList.mockReturnValue(customFiles)
    separateMediaByType.mockReturnValue({ images: customFiles, videos: [], documents: [] })

    const data = ref({ media: '["custom.png"]' })
    const result = useMediaDisplay(data, { filesField: 'media' })

    expect(result.allFiles.value).toEqual(customFiles)
    expect(result.hasImages.value).toBe(true)
  })

  it('should return correct imagePreviewList', () => {
    parseMediaList.mockReturnValue([])
    separateMediaByType.mockReturnValue({
      images: [
        { url: '/a.png', type: 'image' },
        { url: '/b.png', type: 'image' }
      ],
      videos: [],
      documents: []
    })

    const data = ref({ files: '[]' })
    const result = useMediaDisplay(data)

    expect(result.imagePreviewList.value).toEqual(['/a.png', '/b.png'])
  })
})

describe('useFileInfo', () => {
  beforeEach(() => {
    parseMediaList.mockReset().mockImplementation(() => [])
    getMediaType.mockReset().mockImplementation(() => 'document')
  })

  it('should return default info when data is null', () => {
    const data = ref(null)
    const result = useFileInfo(data)
    expect(result.fileInfo.value).toEqual({ url: '', path: '', name: '', size: 0, type: 'document' })
    expect(result.fileUrl.value).toBe('')
    expect(result.fileName.value).toBe('')
    expect(result.fileSize.value).toBe(0)
    expect(result.fileType.value).toBe('document')
    expect(result.fileExt.value).toBe('')
  })

  it('should extract first file from files field', () => {
    const fileInfo = { url: '/doc.pdf', path: 'doc.pdf', name: 'doc.pdf', size: 100, type: 'document' }
    parseMediaList.mockReturnValue([fileInfo])

    const data = ref({ files: '["doc.pdf"]' })
    const result = useFileInfo(data)

    expect(result.fileInfo.value).toEqual(fileInfo)
    expect(result.fileUrl.value).toBe('/doc.pdf')
    expect(result.fileName.value).toBe('doc.pdf')
    expect(result.fileSize.value).toBe(100)
  })

  it('should fallback to urlField when filesField has no data', () => {
    parseMediaList.mockReturnValue([])
    getMediaType.mockReturnValue('image')

    const data = ref({ fileUrl: '/photo.jpg' })
    const result = useFileInfo(data)

    expect(result.fileUrl.value).toBe('/photo.jpg')
    expect(result.fileType.value).toBe('image')
    expect(result.fileName.value).toBe('')
  })

  it('should use custom field options', () => {
    parseMediaList.mockReturnValue([])
    getMediaType.mockReturnValue('video')

    const data = ref({ link: '/video.mp4', title: 'My Video', bytes: 500 })
    const result = useFileInfo(data, {
      urlField: 'link',
      nameField: 'title',
      sizeField: 'bytes',
      filesField: 'attachments'
    })

    expect(result.fileInfo.value).toEqual({
      url: '/video.mp4',
      path: '/video.mp4',
      name: 'My Video',
      size: 500,
      type: 'video'
    })
    expect(result.fileName.value).toBe('My Video')
    expect(result.fileSize.value).toBe(500)
  })

  it('fileExt should extract extension from URL', () => {
    parseMediaList.mockReturnValue([{ url: '/report.pdf', path: 'report.pdf', name: 'report.pdf', size: 0, type: 'document' }])

    const data = ref({ files: '["report.pdf"]' })
    const result = useFileInfo(data)

    expect(result.fileExt.value).toBe('pdf')
  })

  it('fileExt should be empty when url is empty', () => {
    parseMediaList.mockReturnValue([])

    const data = ref({ files: '[]' })
    const result = useFileInfo(data)

    expect(result.fileExt.value).toBe('')
  })
})
