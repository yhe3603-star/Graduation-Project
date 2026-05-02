import { describe, it, expect } from 'vitest'
import {
  getFileType, getFileTypeDisplay, getMediaType, getMediaTypeByExt,
  getFileIcon, getFileColor, getFileTypeName,
  FILE_ICONS, FILE_COLORS, FILE_TYPE_NAMES
} from '@/utils/media'

describe('FILE_ICONS', () => {
  it('should map common types to icon components', () => {
    expect(FILE_ICONS).toHaveProperty('pdf')
    expect(FILE_ICONS).toHaveProperty('word')
    expect(FILE_ICONS).toHaveProperty('excel')
    expect(FILE_ICONS).toHaveProperty('ppt')
    expect(FILE_ICONS).toHaveProperty('image')
    expect(FILE_ICONS).toHaveProperty('video')
  })
})

describe('FILE_COLORS', () => {
  it('should map types to hex colors', () => {
    expect(FILE_COLORS.pdf).toBe('#f56c6c')
    expect(FILE_COLORS.word).toBe('#409eff')
    expect(FILE_COLORS.excel).toBe('#67c23a')
    expect(FILE_COLORS.image).toBe('#28B463')
    expect(FILE_COLORS.video).toBe('#e74c3c')
  })
})

describe('getFileIcon', () => {
  it('should return icon for known type', () => {
    expect(getFileIcon('pdf')).toBe(FILE_ICONS.pdf)
    expect(getFileIcon('word')).toBe(FILE_ICONS.word)
  })

  it('should return default for unknown type', () => {
    const result = getFileIcon('unknown')
    expect(result).toBeDefined()
  })
})

describe('getFileColor', () => {
  it('should return color for known type', () => {
    expect(getFileColor('pdf')).toBe('#f56c6c')
    expect(getFileColor('video')).toBe('#e74c3c')
  })

  it('should return default for unknown type', () => {
    expect(getFileColor('unknown')).toBe('#909399')
  })
})

describe('getFileTypeName', () => {
  it('should return Chinese name for known type', () => {
    expect(getFileTypeName('pdf')).toBe('PDF')
    expect(getFileTypeName('image')).toBe('图片')
    expect(getFileTypeName('video')).toBe('视频')
  })
})

describe('getFileType', () => {
  it('should detect pdf', () => {
    expect(getFileType('document.pdf')).toBe('pdf')
    expect(getFileType('/path/to/file.PDF')).toBe('pdf')
  })

  it('should detect word', () => {
    expect(getFileType('file.docx')).toBe('word')
    expect(getFileType('file.doc')).toBe('word')
  })

  it('should detect excel', () => {
    expect(getFileType('sheet.xlsx')).toBe('excel')
    expect(getFileType('sheet.xls')).toBe('excel')
  })

  it('should detect ppt', () => {
    expect(getFileType('slides.pptx')).toBe('ppt')
    expect(getFileType('slides.ppt')).toBe('ppt')
  })

  it('should return other for null/empty', () => {
    expect(getFileType(null)).toBe('other')
    expect(getFileType('')).toBe('other')
  })

  it('should return other for unknown extensions', () => {
    expect(getFileType('file.xyz')).toBe('other')
  })
})

describe('getFileTypeDisplay', () => {
  it('should return display name', () => {
    expect(getFileTypeDisplay('file.pdf')).toBe('PDF')
    expect(getFileTypeDisplay('file.docx')).toBe('Word')
  })

  it('should return 其他 for null', () => {
    expect(getFileTypeDisplay(null)).toBe('其他')
  })
})

describe('getMediaType', () => {
  it('should detect video', () => {
    expect(getMediaType('video.mp4')).toBe('video')
    expect(getMediaType('movie.avi')).toBe('video')
    expect(getMediaType('clip.mov')).toBe('video')
  })

  it('should detect image', () => {
    expect(getMediaType('photo.jpg')).toBe('image')
    expect(getMediaType('logo.png')).toBe('image')
    expect(getMediaType('icon.svg')).toBe('image')
    expect(getMediaType('anim.gif')).toBe('image')
    expect(getMediaType('bg.webp')).toBe('image')
  })

  it('should detect document', () => {
    expect(getMediaType('doc.pdf')).toBe('document')
    expect(getMediaType('sheet.xlsx')).toBe('document')
  })

  it('should return document for null/empty', () => {
    expect(getMediaType(null)).toBe('document')
    expect(getMediaType('')).toBe('document')
  })
})

describe('getMediaTypeByExt', () => {
  it('should work with extension only', () => {
    expect(getMediaTypeByExt('mp4')).toBe('video')
    expect(getMediaTypeByExt('.mp4')).toBe('video')
    expect(getMediaTypeByExt('jpg')).toBe('image')
    expect(getMediaTypeByExt('pdf')).toBe('document')
  })

  it('should be case-insensitive', () => {
    expect(getMediaTypeByExt('MP4')).toBe('video')
    expect(getMediaTypeByExt('.JPG')).toBe('image')
  })
})
