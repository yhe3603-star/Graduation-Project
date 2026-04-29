import { describe, it, expect } from 'vitest'
import {
  sanitize, sanitizeHtml, containsXss, containsSqlInjection,
  stripHtmlTags, escapeJavaScript, sanitizeUrl, sanitizeFileName,
  sanitizeForLog, isSafeInput, validateInput, sanitizeObject
} from '@/utils/xss'

describe('xss - sanitize', () => {
  it('应转义HTML特殊字符', () => {
    expect(sanitize('<script>alert(1)</script>')).toContain('&lt;script')
    expect(sanitize('a & b')).toBe('a &amp; b')
    expect(sanitize('"hello"')).toBe('&quot;hello&quot;')
    expect(sanitize("it's")).toBe("it&#x27;s")
  })

  it('应处理null/undefined/非字符串', () => {
    expect(sanitize(null)).toBeNull()
    expect(sanitize(undefined)).toBeUndefined()
    expect(sanitize(123)).toBe(123)
  })

  it('应保留正常中文文本', () => {
    expect(sanitize('侗族医药文化')).toBe('侗族医药文化')
    expect(sanitize('钩藤的药用价值')).toBe('钩藤的药用价值')
  })

  it('应转义斜杠', () => {
    expect(sanitize('a/b')).toBe('a&#x2F;b')
  })
})

describe('xss - sanitizeHtml', () => {
  it('应移除script标签', () => {
    expect(sanitizeHtml('<script>alert(1)</script>')).toBe('')
    expect(sanitizeHtml('<script src="evil.js"/>')).toBe('')
  })

  it('应替换javascript协议为空', () => {
    const result = sanitizeHtml('javascript:alert(1)')
    expect(result).not.toContain('javascript:')
  })

  it('应替换事件处理器', () => {
    const result = sanitizeHtml('<img onerror=alert(1)>')
    expect(result).not.toContain('onerror=')
  })

  it('应移除iframe/object/embed', () => {
    expect(sanitizeHtml('<iframe src="evil">x</iframe>')).toBe('')
    expect(sanitizeHtml('<object data="x">x</object>')).toBe('')
  })

  it('应处理null/undefined', () => {
    expect(sanitizeHtml(null)).toBeNull()
    expect(sanitizeHtml(undefined)).toBeUndefined()
  })
})

describe('xss - containsXss', () => {
  it('应检测script标签', () => {
    expect(containsXss('<script>alert(1)</script>')).toBe(true)
  })

  it('应检测javascript协议', () => {
    expect(containsXss('javascript:alert(1)')).toBe(true)
  })

  it('应检测事件处理器', () => {
    expect(containsXss('onerror=alert(1)')).toBe(true)
    expect(containsXss('onclick=evil()')).toBe(true)
  })

  it('应检测iframe/svg/math', () => {
    expect(containsXss('<iframe src="evil">')).toBe(true)
    expect(containsXss('<svg onload=alert(1)>')).toBe(true)
    expect(containsXss('<math><mtext>')).toBe(true)
  })

  it('应检测eval/expression', () => {
    expect(containsXss('eval(document.cookie)')).toBe(true)
    expect(containsXss('expression(alert(1))')).toBe(true)
  })

  it('应检测大小写混合', () => {
    expect(containsXss('<ScRiPt>alert(1)</ScRiPt>')).toBe(true)
    expect(containsXss('JAVASCRIPT:alert(1)')).toBe(true)
  })

  it('正常文本不应被误判', () => {
    expect(containsXss('侗族医药文化')).toBe(false)
    expect(containsXss('钩藤的药用价值')).toBe(false)
    expect(containsXss('药浴疗法')).toBe(false)
  })

  it('应处理null/undefined/空字符串', () => {
    expect(containsXss(null)).toBeFalsy()
    expect(containsXss(undefined)).toBeFalsy()
    expect(containsXss('')).toBeFalsy()
  })
})

describe('xss - containsSqlInjection', () => {
  it('应检测SQL关键字', () => {
    expect(containsSqlInjection("1 OR 1=1")).toBe(true)
    expect(containsSqlInjection("'; DROP TABLE users;--")).toBe(true)
    expect(containsSqlInjection("UNION SELECT * FROM users")).toBe(true)
  })

  it('应处理null/undefined/空字符串', () => {
    expect(containsSqlInjection(null)).toBeFalsy()
    expect(containsSqlInjection(undefined)).toBeFalsy()
    expect(containsSqlInjection('')).toBeFalsy()
  })

  it('正常文本不应被误判', () => {
    expect(containsSqlInjection('侗族药浴疗法')).toBe(false)
  })
})

describe('xss - stripHtmlTags', () => {
  it('应移除HTML标签', () => {
    expect(stripHtmlTags('<b>bold</b>')).toBe('bold')
    expect(stripHtmlTags('<p>hello</p>')).toBe('hello')
    expect(stripHtmlTags('<div class="x">text</div>')).toBe('text')
  })

  it('应处理null/undefined', () => {
    expect(stripHtmlTags(null)).toBeNull()
    expect(stripHtmlTags(undefined)).toBeUndefined()
  })
})

describe('xss - escapeJavaScript', () => {
  it('应转义JavaScript特殊字符', () => {
    expect(escapeJavaScript('hello "world"')).toBe('hello \\"world\\"')
    expect(escapeJavaScript("it's")).toBe("it\\'s")
    expect(escapeJavaScript('line1\nline2')).toBe('line1\\nline2')
    expect(escapeJavaScript('tab\there')).toBe('tab\\there')
  })

  it('应转义反斜杠', () => {
    expect(escapeJavaScript('path\\to\\file')).toBe('path\\\\to\\\\file')
  })

  it('应处理null/undefined', () => {
    expect(escapeJavaScript(null)).toBeNull()
    expect(escapeJavaScript(undefined)).toBeUndefined()
  })
})

describe('xss - sanitizeUrl', () => {
  it('应清除javascript协议URL', () => {
    expect(sanitizeUrl('javascript:alert(1)')).toBe('')
  })

  it('应清除data协议URL', () => {
    expect(sanitizeUrl('data:text/html,<script>alert(1)</script>')).toBe('')
  })

  it('应清除vbscript协议URL', () => {
    expect(sanitizeUrl('vbscript:msgbox')).toBe('')
  })

  it('应保留合法HTTP URL', () => {
    expect(sanitizeUrl('http://example.com')).toBe('http://example.com')
    expect(sanitizeUrl('https://example.com/api')).toBe('https://example.com/api')
  })

  it('应保留相对路径', () => {
    expect(sanitizeUrl('/api/plants')).toBe('/api/plants')
    expect(sanitizeUrl('/images/photo.jpg')).toBe('/images/photo.jpg')
  })

  it('应处理null/undefined/空字符串', () => {
    expect(sanitizeUrl(null)).toBe('')
    expect(sanitizeUrl(undefined)).toBe('')
    expect(sanitizeUrl('')).toBe('')
  })

  it('应拒绝不合法的协议', () => {
    expect(sanitizeUrl('ftp://example.com')).toBe('')
    expect(sanitizeUrl('file:///etc/passwd')).toBe('')
  })
})

describe('xss - sanitizeFileName', () => {
  it('应替换非法文件名字符', () => {
    expect(sanitizeFileName('file<>name.txt')).toBe('file__name.txt')
    expect(sanitizeFileName('path\\to\\file')).toBe('path_to_file')
    expect(sanitizeFileName('a:b*c?d')).toBe('a_b_c_d')
  })

  it('应截断超长文件名', () => {
    const longName = 'a'.repeat(300) + '.txt'
    expect(sanitizeFileName(longName).length).toBeLessThanOrEqual(255)
  })

  it('应保留扩展名', () => {
    const longName = 'a'.repeat(300) + '.txt'
    expect(sanitizeFileName(longName)).toMatch(/\.txt$/)
  })

  it('应处理null/undefined/空字符串', () => {
    expect(sanitizeFileName(null)).toBe('')
    expect(sanitizeFileName(undefined)).toBe('')
    expect(sanitizeFileName('')).toBe('')
  })
})

describe('xss - sanitizeForLog', () => {
  it('应替换控制字符', () => {
    expect(sanitizeForLog('line1\nline2')).toBe('line1\\nline2')
    expect(sanitizeForLog('tab\there')).toBe('tab\\there')
  })

  it('应截断超长输入', () => {
    const longInput = 'a'.repeat(2000)
    const result = sanitizeForLog(longInput)
    expect(result.length).toBeLessThanOrEqual(1003)
    expect(result.endsWith('...')).toBe(true)
  })

  it('应处理null/undefined', () => {
    expect(sanitizeForLog(null)).toBeNull()
    expect(sanitizeForLog(undefined)).toBeUndefined()
  })
})

describe('xss - isSafeInput', () => {
  it('正常文本应安全', () => {
    expect(isSafeInput('侗族医药')).toBe(true)
  })

  it('XSS内容应不安全', () => {
    expect(isSafeInput('<script>alert(1)</script>')).toBe(false)
  })

  it('超长输入应不安全', () => {
    expect(isSafeInput('a'.repeat(1001), 1000)).toBe(false)
  })

  it('null/undefined应安全', () => {
    expect(isSafeInput(null)).toBe(true)
    expect(isSafeInput(undefined)).toBe(true)
  })
})

describe('xss - validateInput', () => {
  it('required=true时空值应无效', () => {
    const result = validateInput('', { required: true })
    expect(result.valid).toBe(false)
  })

  it('required=false时空值应有效', () => {
    const result = validateInput('', { required: false })
    expect(result.valid).toBe(true)
  })

  it('超长输入应无效', () => {
    const result = validateInput('a'.repeat(101), { maxLength: 100 })
    expect(result.valid).toBe(false)
  })

  it('XSS内容应无效', () => {
    const result = validateInput('<img src=x onerror=alert(1)>')
    expect(result.valid).toBe(false)
  })

  it('SQL注入应无效', () => {
    const result = validateInput("1 OR 1=1")
    expect(result.valid).toBe(false)
  })

  it('正常输入应有效', () => {
    const result = validateInput('侗族药浴')
    expect(result.valid).toBe(true)
  })

  it('allowHtml=true时应允许HTML', () => {
    const result = validateInput('<b>bold</b>', { allowHtml: true })
    expect(result.valid).toBe(true)
  })

  it('checkSqlInjection=false时应跳过SQL检查', () => {
    const result = validateInput('SELECT something', { checkSqlInjection: false, allowHtml: true })
    expect(result.valid).toBe(true)
  })
})

describe('xss - sanitizeObject', () => {
  it('应递归转义对象中所有字符串', () => {
    const obj = { name: '<script>', nested: { value: 'a & b' } }
    const result = sanitizeObject(obj)
    expect(result.name).toContain('&lt;script')
    expect(result.nested.value).toBe('a &amp; b')
  })

  it('应处理数组', () => {
    const arr = ['<script>', 'normal']
    const result = sanitizeObject(arr)
    expect(result[0]).toContain('&lt;script')
    expect(result[1]).toBe('normal')
  })

  it('应保留非字符串值', () => {
    const obj = { count: 42, flag: true }
    const result = sanitizeObject(obj)
    expect(result.count).toBe(42)
    expect(result.flag).toBe(true)
  })

  it('应处理null/undefined', () => {
    expect(sanitizeObject(null)).toBeNull()
    expect(sanitizeObject(undefined)).toBeUndefined()
  })
})
