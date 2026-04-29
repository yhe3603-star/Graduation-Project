# 单元测试目录 (__tests__/)

> 类比：体检。医生不会只看你的外表就判断你健不健康，而是要一项一项检查——验血、拍片、量血压。单元测试也一样，它一项一项检查你的代码函数，确保每个"器官"都正常工作。

---

## 一、什么是单元测试？

**单元测试（Unit Test）** 就是给代码中的最小功能单元（一个函数、一个模块）写一段"考试题"，验证它的输出是否符合预期。

```javascript
// 你写了一个加法函数
function add(a, b) {
  return a + b
}

// 单元测试就是给它出题
add(1, 2) === 3    // 第一题：1+2 应该等于 3
add(0, 0) === 0    // 第二题：0+0 应该等于 0
add(-1, 1) === 0   // 第三题：-1+1 应该等于 0
```

如果所有题目都答对了，说明这个函数是正确的。如果有一题答错了，说明代码有 bug。

---

## 二、为什么要写测试？

| 原因 | 说明 | 类比 |
|------|------|------|
| **尽早发现 bug** | 写完代码立刻测试，比上线后用户发现好得多 | 体检早发现早治疗 |
| **放心改代码** | 改了代码跑一遍测试，全过就说明没改坏 | 改了零件后试车 |
| **活文档** | 测试代码就是最好的使用文档，看测试就知道函数怎么用 | 说明书 |
| **毕业设计加分** | 有测试的项目显得更专业、更严谨 | 论文有数据支撑 |

**不写测试的后果**：改了一个地方，另一个地方悄悄坏了，你完全不知道，直到用户投诉。

---

## 三、测试框架介绍

本项目使用的技术栈：

| 工具 | 版本 | 用途 | 类比 |
|------|------|------|------|
| **Vitest** | 1.0 | 测试运行器，负责执行测试、报告结果 | 监考老师 |
| **jsdom** | 24.0 | 在 Node.js 中模拟浏览器环境 | 模拟考场 |
| **@vitest/coverage-v8** | 1.0 | 统计代码覆盖率 | 体检报告单 |

### 为什么选 Vitest？

- **Vite 原生集成**：不需要额外配置，和项目构建工具无缝配合
- **速度快**：利用 Vite 的转换管线，测试启动飞快
- **API 兼容 Jest**：如果你学过 Jest，Vitest 几乎一样

---

## 四、当前测试文件

### 4.1 adminUtils.test.js - 管理后台工具函数测试

测试对象：[adminUtils.js](../utils/adminUtils.js)

| 测试分组 | 测试内容 | 测试函数 |
|----------|----------|----------|
| `formatTime` | 时间格式化 | null/undefined 返回 `"-"`，正常日期格式化 |
| `formatFileSize` | 文件大小格式化 | 0 字节、KB、MB、GB 的正确转换 |
| `getDifficultyTagType` | 难度标签类型 | easy->success, medium->warning, hard->danger |
| `getDifficultyText` | 难度文字 | easy->入门, medium->进阶, hard->专业 |
| `getLevelTagType` | 级别标签类型 | 省级->warning, 自治区级->success |
| `createTagGetter` | 标签获取器工厂 | 自定义映射、默认值 |

### 4.2 resource.test.js - 资源工具函数测试

测试对象：[utils/index.js](../utils/index.js)

| 测试分组 | 测试内容 | 测试函数 |
|----------|----------|----------|
| `parseMediaList` | 媒体列表解析 | null/undefined、JSON 字符串、逗号分隔、数组 |
| `parseDocumentList` | 文档列表解析 | JSON 字符串数组、JSON 对象数组 |
| `getResourceUrl` | 资源 URL 处理 | http/https 原样返回、相对路径加前导斜杠 |
| `getFileName` | 文件名提取 | 从路径中提取文件名 |
| `getFileType` | 文件类型判断 | pdf/word/excel/ppt/txt/other |
| `downloadDocument` | 文档下载 | null/undefined 不报错 |

---

## 五、如何运行测试

### 5.1 运行所有测试（一次性）

```bash
npm run test:run
```

输出示例：

```
 ✓ __tests__/adminUtils.test.js (12 tests) 15ms
 ✓ __tests__/resource.test.js (18 tests) 22ms

 Test Files  2 passed (2)
      Tests  30 passed (30)
   Start at  09:30:15
   Duration  1.23s
```

- `passed` = 全部通过，代码没问题
- `failed` = 有测试没通过，需要修复

### 5.2 运行测试并生成覆盖率报告

```bash
npm run test:coverage
```

覆盖率报告会告诉你：代码有多少比例被测试覆盖了。

```
 % Coverage report from v8
-----------------------------|---------|----------|---------|---------|
 File                        | % Stmts | % Branch | % Funcs | % Lines |
-----------------------------|---------|----------|---------|---------|
 All files                   |   85.7  |    72.3  |   90.0  |   86.5  |
  utils/adminUtils.js        |   92.3  |    80.0  |  100.0  |   93.1  |
  utils/index.js             |   78.1  |    64.5  |   80.0  |   79.8  |
-----------------------------|---------|----------|---------|---------|
```

### 5.3 监听模式（开发时推荐）

```bash
npm run test
```

这个命令会**持续监听文件变化**，你每次保存代码，测试自动重新运行。就像有一个助手在旁边，你改了代码他立刻帮你检查。

---

## 六、如何写一个测试（手把手教学）

写测试遵循 **AAA 模式**：Arrange（准备）-> Act（执行）-> Assert（断言）。

### 6.1 最简单的例子

假设你要测试一个格式化文件大小的函数：

```javascript
// 被测试的函数（在 utils/index.js 中）
export function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return `${bytes.toFixed(1)} B`
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1073741824) return `${(bytes / 1048576).toFixed(1)} MB`
  return `${(bytes / 1073741824).toFixed(1)} GB`
}
```

### 6.2 编写测试文件

在 `__tests__/` 目录下新建 `formatFileSize.test.js`：

```javascript
// 1. 导入测试工具和被测函数
import { describe, it, expect } from 'vitest'
import { formatFileSize } from '@/utils'

// 2. describe：把相关测试分组（类似文件夹）
describe('formatFileSize', () => {

  // 3. it：一个测试用例（类似一道题）
  it('should return "0 B" for 0 bytes', () => {
    // Arrange（准备）：定义输入
    const input = 0

    // Act（执行）：调用函数
    const result = formatFileSize(input)

    // Assert（断言）：验证结果
    expect(result).toBe('0 B')
  })

  it('should format bytes correctly', () => {
    expect(formatFileSize(500)).toBe('500.0 B')
  })

  it('should format kilobytes correctly', () => {
    expect(formatFileSize(1024)).toBe('1.0 KB')
  })

  it('should format megabytes correctly', () => {
    expect(formatFileSize(1048576)).toBe('1.0 MB')
  })

  it('should return "0 B" for null/undefined', () => {
    // 边界情况测试：传入异常值
    expect(formatFileSize(null)).toBe('0 B')
    expect(formatFileSize(undefined)).toBe('0 B')
  })
})
```

### 6.3 AAA 模式详解

```javascript
it('should format kilobytes correctly', () => {
  // Arrange（准备）：设置测试数据
  const input = 2048

  // Act（执行）：调用被测函数
  const result = formatFileSize(input)

  // Assert（断言）：检查结果是否符合预期
  expect(result).toBe('2.0 KB')
})
```

| 步骤 | 做什么 | 类比 |
|------|--------|------|
| **Arrange** | 准备输入数据 | 出题：准备考卷 |
| **Act** | 调用被测函数 | 答题：学生做题 |
| **Assert** | 验证结果 | 批改：对答案 |

### 6.4 常用的断言方法

```javascript
// 相等判断
expect(result).toBe('2.0 KB')           // 严格相等（===）
expect(result).toEqual({ a: 1, b: 2 }) // 深度相等（对象/数组）

// 真假判断
expect(result).toBeTruthy()   // 结果为真（非 null、非 0、非 ''）
expect(result).toBeFalsy()    // 结果为假
expect(result).toBeNull()     // 结果为 null
expect(result).toBeUndefined() // 结果为 undefined

// 包含判断
expect(result).toContain('KB')          // 字符串/数组包含
expect(result).toHaveLength(3)          // 数组/字符串长度

// 异常判断
expect(() => fn()).toThrow()            // 函数调用抛出异常
expect(() => fn()).not.toThrow()        // 函数调用不抛异常

// 取反：在任意断言前加 .not
expect(result).not.toBe('error')
```

---

## 七、测试分组技巧

当测试用例很多时，用 `describe` 嵌套分组，让测试报告更清晰：

```javascript
describe('adminUtils', () => {           // 第一层：按文件分组
  describe('formatTime', () => {         // 第二层：按函数分组
    it('should return "-" for null', () => { ... })
    it('should format valid date string', () => { ... })
    it('should format Date object', () => { ... })
  })

  describe('formatFileSize', () => {     // 第二层：另一个函数
    it('should return "0 B" for 0 bytes', () => { ... })
    it('should format kilobytes correctly', () => { ... })
  })
})
```

运行后的报告：

```
adminUtils
  formatTime
    ✓ should return "-" for null
    ✓ should format valid date string
    ✓ should format Date object
  formatFileSize
    ✓ should return "0 B" for 0 bytes
    ✓ should format kilobytes correctly
```

一目了然！

---

## 八、测试覆盖率详解

### 8.1 什么是覆盖率？

覆盖率 = 被测试执行过的代码 / 总代码量 * 100%

```
覆盖率 80% 意味着：100 行代码中有 80 行在测试中被执行过，20 行从未被测试到。
```

### 8.2 覆盖率的四个维度

| 维度 | 含义 | 类比 |
|------|------|------|
| **Statements（语句覆盖率）** | 有多少行代码被执行 | 体检做了多少项检查 |
| **Branches（分支覆盖率）** | if-else 分支走了多少 | 每个岔路都走过没 |
| **Functions（函数覆盖率）** | 有多少函数被调用 | 每个器官都检查了没 |
| **Lines（行覆盖率）** | 有多少行被执行 | 和 Statements 类似 |

### 8.3 覆盖率多少算合格？

| 覆盖率 | 评价 | 建议 |
|--------|------|------|
| 0-30% | 很低 | 至少把核心工具函数测了 |
| 30-60% | 一般 | 毕设够用，但可以更好 |
| 60-80% | 不错 | 比较专业的水平 |
| 80%+ | 优秀 | 工业级项目标准 |
| 100% | 完美 | 理想状态，但不必强求 |

**注意**：覆盖率 100% 不代表没有 bug。它只说明代码被执行了，不代表所有情况都测到了。

---

## 九、本项目测试的实际代码解读

以 [adminUtils.test.js](./adminUtils.test.js) 中的 `createTagGetter` 测试为例：

```javascript
describe('createTagGetter', () => {
  it('should create a function that returns correct tag type', () => {
    // Arrange：创建一个自定义映射
    const getTag = createTagGetter({ high: 'danger', medium: 'warning', low: 'success' })

    // Assert：验证映射是否正确
    expect(getTag('high')).toBe('danger')
    expect(getTag('medium')).toBe('warning')
    expect(getTag('low')).toBe('success')
  })

  it('should return default value for unknown key', () => {
    // Arrange：创建映射，指定默认值为 'info'
    const getTag = createTagGetter({ high: 'danger' }, 'info')

    // Assert：未知键返回默认值
    expect(getTag('unknown')).toBe('info')
  })
})
```

这段测试验证了 `createTagGetter` 的两个核心能力：
1. 能根据映射表返回正确的标签类型
2. 遇到映射表中没有的键，能返回默认值

---

## 十、常见问题

### Q1：测试文件应该放在哪里？

本项目采用**独立目录**方式，所有测试文件放在 `src/__tests__/` 下，文件名以 `.test.js` 结尾。

另一种方式是**就近放置**，测试文件和源文件放在一起：

```
utils/
  adminUtils.js
  adminUtils.test.js    <-- 和源文件同目录
```

两种方式都可以，本项目选择独立目录是为了保持源码目录整洁。

### Q2：什么时候写测试？

| 时机 | 推荐程度 | 说明 |
|------|----------|------|
| 写完函数立刻写 | 最推荐 | 趁着还记得逻辑，马上测试 |
| 写代码之前先写测试（TDD） | 进阶 | 先写测试，再写代码让测试通过 |
| 项目写完再补测试 | 不推荐 | 容易遗漏，而且写的时候已经忘了细节 |

### Q3：每个函数都要写测试吗？

不需要。优先测试这些：

| 优先级 | 测试什么 | 原因 |
|--------|----------|------|
| 高 | 工具函数（utils/） | 纯函数，最容易测，bug 影响面广 |
| 高 | 数据处理逻辑 | 格式化、解析、转换等容易出错 |
| 中 | 组合式函数（composables/） | 核心业务逻辑 |
| 低 | UI 组件 | 需要额外工具（@vue/test-utils），成本高 |
| 不测 | 第三方库的代码 | 不是你写的，不用你测 |

### Q4：测试失败了怎么办？

```
FAIL  __tests__/adminUtils.test.js > formatFileSize > should format bytes correctly
AssertionError: expected "500 B" to be "500.0 B"
```

1. **看错误信息**：期望 `500.0 B`，实际得到 `500 B`，少了一个小数位
2. **找到源代码**：去 `formatFileSize` 函数里看，发现 bytes < 1024 的分支没有 `.toFixed(1)`
3. **修复代码**：加上 `.toFixed(1)`
4. **重新运行测试**：确认修复成功

### Q5：npm run test 和 npm run test:run 有什么区别？

| 命令 | 行为 | 什么时候用 |
|------|------|------------|
| `npm run test` | 监听模式，文件变化自动重跑 | 开发时 |
| `npm run test:run` | 跑一次就结束 | CI/CD 或最终检查 |
| `npm run test:coverage` | 跑一次并生成覆盖率报告 | 检查测试质量 |

---

## 代码审查与改进建议

- [覆盖度] 测试文件仅覆盖adminUtils和resource，缺少对核心composable(useFavorite/useInteraction/useMedia等)的测试
- [覆盖度] 缺少对关键组件(AiChatCard/CommentSection/PlantGame等)的测试
