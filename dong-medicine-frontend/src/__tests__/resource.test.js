import { describe, it, expect } from 'vitest'
import { parseMediaList, parseDocumentList, getResourceUrl, getFileName, getFileType, downloadDocument } from '@/utils'

describe('resource utils', () => {
  describe('parseMediaList', () => {
    it('should return empty array for null/undefined/empty input', () => {
      expect(parseMediaList(null)).toEqual([])
      expect(parseMediaList(undefined)).toEqual([])
      expect(parseMediaList('')).toEqual([])
    })

    it('should parse JSON array string', () => {
      const result = parseMediaList('["path1", "path2"]')
      expect(result).toHaveLength(2)
      expect(result[0].path).toBe('path1')
      expect(result[0].url).toBe('/path1')
    })

    it('should parse comma-separated string', () => {
      const result = parseMediaList('path1, path2, path3')
      expect(result).toHaveLength(3)
      expect(result[0].path).toBe('path1')
      expect(result[1].path).toBe('path2')
      expect(result[2].path).toBe('path3')
    })

    it('should return array as normalized objects', () => {
      const arr = ['path1', 'path2']
      const result = parseMediaList(arr)
      expect(result).toHaveLength(2)
      expect(result[0].path).toBe('path1')
      expect(result[0].url).toBe('/path1')
    })

    it('should filter empty values', () => {
      const result = parseMediaList('path1, , path2')
      expect(result).toHaveLength(2)
      expect(result[0].path).toBe('path1')
      expect(result[1].path).toBe('path2')
    })

    it('should normalize object items', () => {
      const items = [{ path: 'test.pdf', name: 'Test.pdf', size: 1024 }]
      const result = parseMediaList(items)
      expect(result).toHaveLength(1)
      expect(result[0].path).toBe('test.pdf')
      expect(result[0].name).toBe('Test.pdf')
      expect(result[0].size).toBe(1024)
      expect(result[0].url).toBe('/test.pdf')
    })
  })

  describe('parseDocumentList', () => {
    it('should return empty array for null/undefined/empty input', () => {
      expect(parseDocumentList(null)).toEqual([])
      expect(parseDocumentList(undefined)).toEqual([])
      expect(parseDocumentList('')).toEqual([])
    })

    it('should parse JSON array of strings', () => {
      const result = parseDocumentList('["doc1.pdf", "doc2.docx"]')
      expect(result).toHaveLength(2)
      expect(result[0].path).toBe('doc1.pdf')
      expect(result[0].url).toBe('/doc1.pdf')
    })

    it('should parse JSON array of objects', () => {
      const docs = [{ path: 'doc.pdf', name: 'Document.pdf' }]
      const result = parseDocumentList(JSON.stringify(docs))
      expect(result[0].path).toBe('doc.pdf')
      expect(result[0].name).toBe('Document.pdf')
    })
  })

  describe('getResourceUrl', () => {
    it('should return empty string for null/undefined', () => {
      expect(getResourceUrl(null)).toBe('')
      expect(getResourceUrl(undefined)).toBe('')
    })

    it('should return URL as-is for http/https URLs', () => {
      expect(getResourceUrl('http://example.com/file.pdf')).toBe('http://example.com/file.pdf')
      expect(getResourceUrl('https://example.com/file.pdf')).toBe('https://example.com/file.pdf')
    })

    it('should add leading slash for relative paths', () => {
      expect(getResourceUrl('uploads/file.pdf')).toBe('/uploads/file.pdf')
    })

    it('should keep leading slash for absolute paths', () => {
      expect(getResourceUrl('/uploads/file.pdf')).toBe('/uploads/file.pdf')
    })
  })

  describe('getFileName', () => {
    it('should return empty string for null/undefined', () => {
      expect(getFileName(null)).toBe('')
      expect(getFileName(undefined)).toBe('')
    })

    it('should extract filename from path', () => {
      expect(getFileName('/uploads/documents/report.pdf')).toBe('report.pdf')
      expect(getFileName('path/to/file.docx')).toBe('file.docx')
    })

    it('should return input if no path separator', () => {
      expect(getFileName('document.pdf')).toBe('document.pdf')
    })
  })

  describe('getFileType', () => {
    it('should return "pdf" for PDF files', () => {
      expect(getFileType('document.pdf')).toBe('pdf')
      expect(getFileType('FILE.PDF')).toBe('pdf')
    })

    it('should return "word" for Word files', () => {
      expect(getFileType('document.docx')).toBe('word')
      expect(getFileType('document.doc')).toBe('word')
    })

    it('should return "excel" for Excel files', () => {
      expect(getFileType('spreadsheet.xlsx')).toBe('excel')
      expect(getFileType('spreadsheet.xls')).toBe('excel')
    })

    it('should return "ppt" for PowerPoint files', () => {
      expect(getFileType('presentation.pptx')).toBe('ppt')
      expect(getFileType('presentation.ppt')).toBe('ppt')
    })

    it('should return "txt" for text files', () => {
      expect(getFileType('notes.txt')).toBe('txt')
    })

    it('should return "other" for unknown extensions', () => {
      expect(getFileType('file.xyz')).toBe('other')
      expect(getFileType('file')).toBe('other')
    })

    it('should return "other" for null/undefined', () => {
      expect(getFileType(null)).toBe('other')
      expect(getFileType(undefined)).toBe('other')
    })
  })

  describe('downloadDocument', () => {
    it('should return early for null/undefined document', () => {
      expect(() => downloadDocument(null)).not.toThrow()
      expect(() => downloadDocument(undefined)).not.toThrow()
      expect(() => downloadDocument({})).not.toThrow()
    })
  })
})
