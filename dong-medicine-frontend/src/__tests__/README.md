# 测试目录 (__tests__)

本目录存放单元测试文件，用于测试代码的正确性。

## 目录

- [什么是单元测试？](#什么是单元测试)
- [目录结构](#目录结构)
- [测试文件列表](#测试文件列表)
- [如何运行测试](#如何运行测试)

---

## 什么是单元测试？

**单元测试**是对代码中最小可测试单元进行验证的过程。它就像"体检"——检查身体的每个部位是否正常工作。

### 为什么需要单元测试？

```
┌─────────────────────────────────────────────────────────────────┐
│                     单元测试的作用                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 发现问题：在开发阶段就能发现bug，而不是上线后才发现           │
│                                                                 │
│  2. 重构保障：修改代码后，运行测试确保功能正常                    │
│                                                                 │
│  3. 文档作用：测试用例展示了代码的使用方式                       │
│                                                                 │
│  4. 提高信心：有测试覆盖的代码，修改时更有信心                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
__tests__/
│
├── adminUtils.test.js                 # 管理后台工具函数测试
└── resource.test.js                   # 资源相关测试
```

---

## 测试文件列表

| 测试文件 | 被测试文件 | 测试内容 |
|---------|-----------|----------|
| adminUtils.test.js | utils/adminUtils.js | 管理后台工具函数 |
| resource.test.js | - | 资源相关功能 |

---

## 如何运行测试

```bash
# 运行所有测试
npm run test:run

# 运行特定测试文件
npm run test:run adminUtils.test.js

# 运行测试并生成覆盖率报告
npm run test:coverage
```

---

## 测试示例

### 基本测试结构

```javascript
// adminUtils.test.js
import { describe, it, expect } from 'vitest'
import { formatFileSize, truncate } from '@/utils/adminUtils'

describe('formatFileSize', () => {
  it('应该正确格式化字节', () => {
    expect(formatFileSize(0)).toBe('0 B')
    expect(formatFileSize(1024)).toBe('1 KB')
    expect(formatFileSize(1048576)).toBe('1 MB')
  })
  
  it('应该处理无效输入', () => {
    expect(formatFileSize(null)).toBe('0 B')
    expect(formatFileSize(-1)).toBe('0 B')
  })
})

describe('truncate', () => {
  it('应该截断长文本', () => {
    expect(truncate('这是一个很长的文本', 5)).toBe('这是一个...')
  })
  
  it('不应该截断短文本', () => {
    expect(truncate('短文本', 10)).toBe('短文本')
  })
})
```

### 测试组合式函数

```javascript
// useCounter.test.js
import { describe, it, expect } from 'vitest'
import { useCounter } from '@/composables/useCounter'

describe('useCounter', () => {
  it('应该正确初始化', () => {
    const { count } = useCounter(10)
    expect(count.value).toBe(10)
  })
  
  it('应该正确增加', () => {
    const { count, increment } = useCounter(0)
    increment()
    expect(count.value).toBe(1)
  })
})
```

---

## 最佳实践

### 1. 测试命名清晰

```javascript
// ✅ 好的做法：描述清楚测试内容
it('当用户名为空时应该抛出错误', () => { ... })

// ❌ 不好的做法：命名不清晰
it('test1', () => { ... })
```

### 2. 一个测试只测一件事

```javascript
// ✅ 好的做法：一个测试一个断言
it('应该返回正确的文件大小', () => {
  expect(formatFileSize(1024)).toBe('1 KB')
})

// ❌ 不好的做法：一个测试多个断言
it('测试所有功能', () => {
  expect(formatFileSize(1024)).toBe('1 KB')
  expect(formatFileSize(2048)).toBe('2 KB')
  expect(truncate('abc', 2)).toBe('ab...')
})
```

### 3. 测试边界情况

```javascript
describe('formatFileSize', () => {
  it('应该处理0', () => {
    expect(formatFileSize(0)).toBe('0 B')
  })
  
  it('应该处理负数', () => {
    expect(formatFileSize(-1)).toBe('0 B')
  })
  
  it('应该处理null', () => {
    expect(formatFileSize(null)).toBe('0 B')
  })
})
```

---

**相关文档**

- [Vitest 官方文档](https://vitest.dev/)
- [Vue Test Utils](https://test-utils.vuejs.org/)

---

**最后更新时间**：2026年4月3日
