const XSS_PATTERNS = [
  /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
  /<script[^>]*\/>/gi,
  /javascript\s*:/gi,
  /vbscript\s*:/gi,
  /on\w+\s*=/gi,
  /on\w+=/gi,
  /&#x?[0-9a-f]+;?/gi,
  /eval\s*\(/gi,
  /expression\s*\(/gi,
  /<iframe/gi,
  /<object/gi,
  /<embed/gi,
  /<link/gi,
  /<meta/gi,
  /<base/gi,
  /<form/gi,
  /<input/gi,
  /<button/gi,
  /<textarea/gi,
  /<select/gi,
  /<style/gi,
  /data\s*:/gi,
  /srcdoc\s*=/gi,
  /xlink:href\s*=/gi,
  /xmlns\s*=/gi,
  /<svg/gi,
  /<math/gi,
  /<audio/gi,
  /<video/gi,
  /<source/gi
]

const HTML_ENTITIES = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#x27;',
  '/': '&#x2F;'
}

const SQL_INJECTION_PATTERNS = [
  /\b(select|insert|update|delete|drop|create|alter|truncate|exec|execute)\b/gi,
  /\b(union)\b.*\b(select|insert|update|delete)\b/gi,
  /(--|#|\/\*|\*\/|;|\|\|)/g,
  /\b(or|and)\b\s+\d+\s*=\s*\d+/gi,
  /\b(or|and)\b\s+['"]\w+['"]\s*=\s*['"]\w+['"]/gi
]

export function sanitize(input) {
  if (!input || typeof input !== 'string') return input
  return Object.entries(HTML_ENTITIES).reduce((str, [char, entity]) => str.replaceAll(char, entity), input)
}

export function sanitizeHtml(input) {
  if (!input || typeof input !== 'string') return input

  return input
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<script[^>]*\/>/gi, '')
    .replace(/javascript:/gi, '')
    .replace(/on\w+\s*=/gi, 'onxxx=')
    .replace(/eval\s*\(/gi, 'evalxxx(')
    .replace(/expression\s*\(/gi, 'expressionxxx(')
    .replace(/vbscript:/gi, '')
    .replace(/<iframe[^>]*>.*?<\/iframe>/gi, '')
    .replace(/<object[^>]*>.*?<\/object>/gi, '')
    .replace(/<(embed|link|meta)[^>]*\/>/gi, '')
}

export function containsXss(input) {
  return input && typeof input === 'string' && XSS_PATTERNS.some(p => p.test(input))
}

export function containsSqlInjection(input) {
  return input && typeof input === 'string' && SQL_INJECTION_PATTERNS.some(p => p.test(input))
}

export function stripHtmlTags(input) {
  return input && typeof input === 'string' ? input.replace(/<[^>]*>/g, '') : input
}

export function escapeJavaScript(input) {
  if (!input || typeof input !== 'string') return input
  return input
    .replaceAll('\\', '\\\\')
    .replaceAll('"', '\\"')
    .replaceAll("'", "\\'")
    .replaceAll('\n', '\\n')
    .replaceAll('\r', '\\r')
    .replaceAll('\t', '\\t')
}

export function sanitizeUrl(url) {
  if (!url || typeof url !== 'string') return ''

  const sanitized = url.trim()
  const lower = sanitized.toLowerCase()

  if (containsXss(sanitized)) return ''
  if (lower.startsWith('javascript:') || lower.startsWith('data:') || lower.startsWith('vbscript:')) return ''
  if (!sanitized.startsWith('/') && !lower.startsWith('http://') && !lower.startsWith('https://')) return ''

  return sanitized
}

export function sanitizeFileName(fileName) {
  if (!fileName || typeof fileName !== 'string') return ''

  let sanitized = fileName
    .replace(/[\\/:*?"<>|]/g, '_')
    .replace(/\.+/g, '.')
    .replace(/^\.+|\.+$/g, '')

  if (sanitized.length > 255) {
    const lastDot = sanitized.lastIndexOf('.')
    if (lastDot > 0) {
      const ext = sanitized.substring(lastDot)
      sanitized = sanitized.substring(0, 250 - ext.length) + ext
    } else {
      sanitized = sanitized.substring(0, 250)
    }
  }

  return sanitized
}

export function sanitizeForLog(input) {
  if (!input || typeof input !== 'string') return input

  let sanitized = input
    .replaceAll('\n', '\\n')
    .replaceAll('\r', '\\r')
    .replaceAll('\t', '\\t')

  return sanitized.length > 1000 ? sanitized.substring(0, 1000) + '...' : sanitized
}

export function isSafeInput(input, maxLength = 1000) {
  if (!input || typeof input !== 'string') return true
  return input.length <= maxLength && !containsXss(input)
}

export function validateInput(input, options = {}) {
  const { maxLength = 1000, allowHtml = false, checkSqlInjection = true, required = false } = options

  if (!input || typeof input !== 'string') {
    return { valid: !required, error: required ? '输入不能为空' : null }
  }

  if (input.length > maxLength) {
    return { valid: false, error: `输入长度超过限制（最大${maxLength}字符）` }
  }

  if (!allowHtml && containsXss(input)) {
    return { valid: false, error: '输入包含不安全的内容' }
  }

  if (checkSqlInjection && containsSqlInjection(input)) {
    return { valid: false, error: '输入包含非法字符' }
  }

  return { valid: true, error: null }
}

export function sanitizeObject(obj) {
  if (!obj || typeof obj !== 'object') return obj

  const sanitized = Array.isArray(obj) ? [] : {}

  for (const [key, value] of Object.entries(obj)) {
    if (typeof value === 'string') {
      sanitized[key] = sanitize(value)
    } else if (typeof value === 'object' && value !== null) {
      sanitized[key] = sanitizeObject(value)
    } else {
      sanitized[key] = value
    }
  }

  return sanitized
}

export default {
  sanitize, sanitizeHtml, containsXss, containsSqlInjection, stripHtmlTags,
  escapeJavaScript, sanitizeUrl, sanitizeFileName, sanitizeForLog,
  isSafeInput, validateInput, sanitizeObject
}
