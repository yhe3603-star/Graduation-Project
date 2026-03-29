# 侗乡医药数字展示平台前端

> 基于 Vue 3 + Vite 的侗族医药文化遗产数字化展示平台前端应用

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [页面组件](#页面组件)
- [业务组件](#业务组件)
- [组合式函数](#组合式函数)
- [路由配置](#路由配置)
- [状态管理](#状态管理)
- [工具函数](#工具函数)
- [安全机制](#安全机制)
- [样式系统](#样式系统)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)

---

## 项目概述

本项目是侗乡医药数字展示平台的前端应用，提供用户界面展示、交互功能、数据可视化等功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 信息展示 | 植物、传承人、知识、资源展示 |
| 用户交互 | 评论、收藏、反馈 |
| 游戏化学习 | 趣味答题、植物识别游戏 |
| 数据可视化 | ECharts 图表统计 |
| 后台管理 | 内容管理、用户管理、数据统计 |

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 前端框架，使用 Composition API |
| Vite | 5.0+ | 构建工具，开发服务器 |
| Vue Router | 4.2+ | 路由管理，支持路由守卫 |
| Pinia | 2.3+ | 状态管理 |
| Element Plus | 2.4+ | UI 组件库 |
| Axios | 1.6+ | HTTP 请求客户端 |
| ECharts | 5.4+ | 数据可视化图表 |
| Vitest | 1.0+ | 单元测试框架 |

---

## 项目结构

```
dong-medicine-frontend/
│
├── public/                              # 静态资源目录
│   └── favicon.ico                      # 网站图标
│
├── src/
│   │
│   ├── views/                           # 页面组件 (14个)
│   │   ├── Home.vue                     # 首页
│   │   ├── Plants.vue                   # 药用植物页面
│   │   ├── Inheritors.vue               # 传承人页面
│   │   ├── Knowledge.vue                # 知识库页面
│   │   ├── Qa.vue                       # 问答社区页面
│   │   ├── Resources.vue                # 学习资源页面
│   │   ├── Interact.vue                 # 互动专区页面
│   │   ├── Visual.vue                   # 数据可视化页面
│   │   ├── PersonalCenter.vue           # 个人中心页面
│   │   ├── Admin.vue                    # 管理后台页面
│   │   ├── About.vue                    # 关于页面
│   │   ├── Feedback.vue                 # 意见反馈页面
│   │   ├── GlobalSearch.vue             # 全局搜索页面
│   │   └── NotFound.vue                 # 404页面
│   │
│   ├── components/                      # 组件目录
│   │   │
│   │   ├── base/                        # 基础组件
│   │   │   ├── ErrorBoundary.vue        # 错误边界组件
│   │   │   ├── VirtualList.vue          # 虚拟列表组件
│   │   │   └── index.js
│   │   │
│   │   ├── common/                      # 通用组件
│   │   │   └── SkeletonGrid.vue         # 骨架屏网格
│   │   │
│   │   └── business/                    # 业务组件
│   │       │
│   │       ├── layout/                  # 布局组件
│   │       │   ├── AppHeader.vue        # 应用头部
│   │       │   └── AppFooter.vue        # 应用底部
│   │       │
│   │       ├── display/                 # 展示组件
│   │       │   ├── AiChatCard.vue       # AI对话卡片
│   │       │   ├── CardGrid.vue         # 卡片网格
│   │       │   ├── ChartCard.vue        # 图表卡片
│   │       │   ├── PageSidebar.vue      # 页面侧边栏
│   │       │   ├── Pagination.vue       # 分页组件
│   │       │   ├── SearchFilter.vue     # 搜索过滤
│   │       │   ├── UpdateLogCard.vue    # 更新日志卡片
│   │       │   ├── UpdateLogDialog.vue  # 更新日志对话框
│   │       │   └── index.js
│   │       │
│   │       ├── interact/                # 交互组件
│   │       │   ├── CaptchaInput.vue     # 验证码输入
│   │       │   ├── CommentSection.vue   # 评论组件
│   │       │   ├── InteractSidebar.vue  # 互动侧边栏
│   │       │   ├── PlantGame.vue        # 植物识别游戏
│   │       │   ├── QuizSection.vue      # 趣味答题
│   │       │   └── index.js
│   │       │
│   │       ├── media/                   # 媒体组件
│   │       │   ├── DocumentList.vue     # 文档列表
│   │       │   ├── DocumentPreview.vue  # 文档预览
│   │       │   ├── ImageCarousel.vue    # 图片轮播
│   │       │   ├── MediaDisplay.vue     # 媒体展示
│   │       │   ├── VideoPlayer.vue      # 视频播放
│   │       │   └── index.js
│   │       │
│   │       ├── upload/                  # 上传组件
│   │       │   ├── DocumentUploader.vue # 文档上传
│   │       │   ├── FileUploader.vue     # 通用文件上传
│   │       │   ├── ImageUploader.vue    # 图片上传
│   │       │   ├── VideoUploader.vue    # 视频上传
│   │       │   └── index.js
│   │       │
│   │       ├── dialogs/                 # 详情对话框
│   │       │   ├── InheritorDetailDialog.vue
│   │       │   ├── KnowledgeDetailDialog.vue
│   │       │   ├── PlantDetailDialog.vue
│   │       │   ├── QuizDetailDialog.vue
│   │       │   └── ResourceDetailDialog.vue
│   │       │
│   │       └── admin/                   # 管理后台组件
│   │           ├── dialogs/             # 管理对话框
│   │           │   ├── CommentDetailDialog.vue
│   │           │   ├── FeedbackDetailDialog.vue
│   │           │   ├── InheritorDetailDialog.vue
│   │           │   ├── KnowledgeDetailDialog.vue
│   │           │   ├── LogDetailDialog.vue
│   │           │   ├── PlantDetailDialog.vue
│   │           │   ├── QaDetailDialog.vue
│   │           │   ├── QuizDetailDialog.vue
│   │           │   ├── ResourceDetailDialog.vue
│   │           │   └── UserDetailDialog.vue
│   │           │
│   │           ├── forms/               # 管理表单
│   │           │   ├── InheritorFormDialog.vue
│   │           │   ├── KnowledgeFormDialog.vue
│   │           │   ├── PlantFormDialog.vue
│   │           │   ├── QaFormDialog.vue
│   │           │   ├── QuizFormDialog.vue
│   │           │   └── ResourceFormDialog.vue
│   │           │
│   │           ├── AdminDashboard.vue   # 管理仪表盘
│   │           ├── AdminDataTable.vue   # 数据表格
│   │           └── AdminSidebar.vue     # 管理侧边栏
│   │
│   ├── composables/                     # 组合式函数 (11个)
│   │   ├── index.js                     # 导出入口
│   │   ├── useAdminData.js              # 管理后台数据
│   │   ├── useDebounce.js               # 防抖函数
│   │   ├── useErrorHandler.js           # 错误处理
│   │   ├── useFavorite.js               # 收藏功能
│   │   ├── useFormDialog.js             # 表单对话框
│   │   ├── useInteraction.js            # 交互功能
│   │   ├── useMedia.js                  # 媒体处理
│   │   ├── usePersonalCenter.js         # 个人中心
│   │   ├── usePlantGame.js              # 植物游戏
│   │   ├── useQuiz.js                   # 答题逻辑
│   │   └── useUpdateLog.js              # 更新日志
│   │
│   ├── router/                          # 路由配置
│   │   └── index.js                     # 路由定义 + 守卫
│   │
│   ├── stores/                          # 状态管理
│   │   ├── index.js                     # Pinia 实例
│   │   └── user.js                      # 用户状态
│   │
│   ├── utils/                           # 工具函数 (7个)
│   │   ├── index.js                     # 通用工具
│   │   ├── adminUtils.js                # 管理后台工具
│   │   ├── cache.js                     # 缓存工具
│   │   ├── logger.js                    # 日志工具
│   │   ├── media.js                     # 媒体工具
│   │   ├── request.js                   # Axios 封装
│   │   └── xss.js                       # XSS 防护
│   │
│   ├── styles/                          # 样式文件 (9个)
│   │   ├── index.css                    # 样式入口
│   │   ├── variables.css                # CSS 变量
│   │   ├── base.css                     # 基础样式
│   │   ├── components.css               # 组件样式
│   │   ├── pages.css                    # 页面样式
│   │   ├── common.css                   # 通用样式
│   │   ├── home.css                     # 首页样式
│   │   ├── media-common.css             # 媒体样式
│   │   └── dialog-common.css            # 对话框样式
│   │
│   ├── config/                          # 配置文件
│   │   └── homeConfig.js                # 首页配置
│   │
│   ├── directives/                      # 自定义指令
│   │   └── index.js
│   │
│   ├── __tests__/                       # 测试文件
│   │
│   ├── App.vue                          # 根组件
│   └── main.js                          # 入口文件
│
├── index.html                           # HTML 模板
├── package.json                         # 项目配置
├── vite.config.js                       # Vite 配置
├── vitest.config.js                     # 测试配置
├── Dockerfile                           # Docker 构建文件
├── nginx.conf                           # Nginx 配置
└── default.conf                         # Nginx 默认配置
```

---

## 页面组件

| 页面 | 路由 | 功能描述 |
|------|------|----------|
| Home.vue | `/` | 首页，展示平台核心功能入口、统计数据、传承人风采、快速导航 |
| Plants.vue | `/plants` | 药用资源图鉴，展示黔东南道地药材图文详解，支持分类筛选 |
| Inheritors.vue | `/inheritors` | 传承人风采展示，按级别筛选、展示传承谱系与技艺特色 |
| Knowledge.vue | `/knowledge` | 非遗医药知识库，支持分类检索、搜索过滤、收藏功能 |
| Qa.vue | `/qa` | 问答社区，侗医药知识问答、疑难解答、互动交流 |
| Interact.vue | `/interact` | 文化互动专区，包含趣味答题、植物识别游戏、评论交流 |
| Resources.vue | `/resources` | 学习资源库，支持视频/文档/图片资源的预览、下载、收藏 |
| Visual.vue | `/visual` | 数据可视化，展示药方频次、疗法分类、传承人分布等统计图表 |
| PersonalCenter.vue | `/personal` | 个人中心（需登录），管理收藏、答题记录、评论历史、账号设置 |
| Admin.vue | `/admin` | 管理后台（需管理员权限），数据管理、用户管理、评论审核、日志查看 |
| About.vue | `/about` | 关于页面，介绍选题背景、平台特色、功能模块、侗医文化 |
| Feedback.vue | `/feedback` | 意见反馈，用户提交功能建议、问题反馈 |
| GlobalSearch.vue | `/search` | 全局搜索，跨知识、植物、传承人、问答的统一搜索入口 |
| NotFound.vue | `/:pathMatch(.*)*` | 404页面 |

---

## 业务组件

### 布局组件 (layout/)

| 组件 | 用途 |
|------|------|
| AppHeader.vue | 应用头部，导航栏、登录状态、全局搜索入口 |
| AppFooter.vue | 应用底部，版权信息、友情链接 |

### 展示组件 (display/)

| 组件 | 用途 |
|------|------|
| AiChatCard.vue | AI 对话卡片，集成智能问答 |
| CardGrid.vue | 卡片网格组件，通用卡片列表展示 |
| ChartCard.vue | 图表卡片组件，封装 ECharts 图表 |
| PageSidebar.vue | 页面侧边栏，展示统计和热门内容 |
| Pagination.vue | 分页组件 |
| SearchFilter.vue | 搜索过滤组件 |
| UpdateLogCard.vue | 更新日志卡片 |
| UpdateLogDialog.vue | 更新日志对话框 |

### 交互组件 (interact/)

| 组件 | 用途 |
|------|------|
| CaptchaInput.vue | 验证码输入组件 |
| CommentSection.vue | 评论组件，支持发表评论、回复 |
| InteractSidebar.vue | 互动侧边栏，展示排行榜和统计 |
| PlantGame.vue | 植物识别游戏，根据图片识别药材名称 |
| QuizSection.vue | 趣味答题组件，支持计时、评分 |

### 媒体组件 (media/)

| 组件 | 用途 |
|------|------|
| DocumentList.vue | 文档列表组件 |
| DocumentPreview.vue | 文档预览组件 |
| ImageCarousel.vue | 图片轮播组件 |
| MediaDisplay.vue | 媒体展示组件 |
| VideoPlayer.vue | 视频播放组件 |

### 上传组件 (upload/)

| 组件 | 用途 |
|------|------|
| DocumentUploader.vue | 文档上传组件 |
| FileUploader.vue | 通用文件上传组件 |
| ImageUploader.vue | 图片上传组件 |
| VideoUploader.vue | 视频上传组件 |

### 详情对话框组件 (dialogs/)

| 组件 | 用途 |
|------|------|
| PlantDetailDialog.vue | 药材详情对话框 |
| KnowledgeDetailDialog.vue | 知识详情对话框 |
| InheritorDetailDialog.vue | 传承人详情对话框 |
| ResourceDetailDialog.vue | 资源详情对话框 |
| QuizDetailDialog.vue | 答题详情对话框 |

### 管理后台组件 (admin/)

| 组件 | 用途 |
|------|------|
| AdminDashboard.vue | 管理仪表盘，数据概览 |
| AdminDataTable.vue | 数据表格组件，通用 CRUD 表格 |
| AdminSidebar.vue | 管理侧边栏导航 |

### 管理后台对话框 (admin/dialogs/)

| 组件 | 用途 |
|------|------|
| UserDetailDialog.vue | 用户详情对话框 |
| PlantDetailDialog.vue | 植物详情对话框 |
| KnowledgeDetailDialog.vue | 知识详情对话框 |
| InheritorDetailDialog.vue | 传承人详情对话框 |
| ResourceDetailDialog.vue | 资源详情对话框 |
| QaDetailDialog.vue | 问答详情对话框 |
| QuizDetailDialog.vue | 题目详情对话框 |
| CommentDetailDialog.vue | 评论详情对话框 |
| FeedbackDetailDialog.vue | 反馈详情对话框 |
| LogDetailDialog.vue | 日志详情对话框 |

### 管理后台表单 (admin/forms/)

| 组件 | 用途 |
|------|------|
| PlantFormDialog.vue | 药材表单对话框 |
| KnowledgeFormDialog.vue | 知识表单对话框 |
| InheritorFormDialog.vue | 传承人表单对话框 |
| ResourceFormDialog.vue | 资源表单对话框 |
| QaFormDialog.vue | 问答表单对话框 |
| QuizFormDialog.vue | 题目表单对话框 |

---

## 组合式函数

| 函数 | 功能描述 |
|------|----------|
| useAdminData | 管理后台数据管理，包含数据获取、对话框管理、CRUD 操作 |
| useQuiz | 趣味答题功能，题目加载、答案提交、计时、评分 |
| usePlantGame | 植物识别游戏，难度设置、答题逻辑、分数计算 |
| useInteraction | 交互功能，包含倒计时、评论、分页、过滤、统计 |
| useFavorite | 收藏功能，收藏状态管理、收藏/取消收藏操作 |
| useMedia | 媒体处理，文档预览、媒体显示 |
| usePersonalCenter | 个人中心功能，用户信息、收藏管理、密码修改 |
| useFormDialog | 表单对话框通用逻辑 |
| useUpdateLog | 更新日志解析与管理 |
| useDebounce | 防抖函数 |
| useErrorHandler | 错误处理 |

---

## 路由配置

| 路由路径 | 名称 | 组件 | 权限要求 |
|---------|------|------|----------|
| `/` | Home | Home.vue | 无 |
| `/plants` | Plants | Plants.vue | 无 |
| `/inheritors` | Inheritors | Inheritors.vue | 无 |
| `/knowledge` | Knowledge | Knowledge.vue | 无 |
| `/qa` | Qa | Qa.vue | 无 |
| `/interact` | Interact | Interact.vue | 无 |
| `/resources` | Resources | Resources.vue | 无 |
| `/visual` | Visual | Visual.vue | 无 |
| `/personal` | Personal | PersonalCenter.vue | 需登录 |
| `/admin` | Admin | Admin.vue | 需登录 + 管理员权限 |
| `/about` | About | About.vue | 无 |
| `/feedback` | Feedback | Feedback.vue | 无 |
| `/search` | Search | GlobalSearch.vue | 无 |
| `/:pathMatch(.*)*` | NotFound | NotFound.vue | 无 |

### 路由守卫功能

```javascript
// router/index.js

// 本地Token过期检查
function isTokenLocallyExpired(userStore) {
  const token = userStore.token
  if (!token) return true
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}

// Token验证缓存（60秒）
const VALIDATION_CACHE_TTL = 60 * 1000

// 路由守卫逻辑
router.beforeEach(async (to, from, next) => {
  // 1. 初始化用户状态
  // 2. 检查是否需要认证
  // 3. 本地Token过期检查
  // 4. 服务端Token验证（带缓存）
  // 5. 管理员权限验证
})
```

---

## 状态管理

### 用户状态 (stores/user.js)

| 状态/方法 | 描述 |
|----------|------|
| `token` | 用户认证令牌 |
| `userId` | 用户 ID |
| `username` | 用户名 |
| `role` | 用户角色 |
| `userInfo` | 用户详细信息 |
| `isLoggedIn` | 登录状态（计算属性） |
| `isAdmin` | 是否管理员（计算属性） |
| `setAuth()` | 设置认证信息 |
| `clearAuth()` | 清除认证信息 |
| `login()` | 登录方法 |
| `logout()` | 登出方法 |
| `validateToken()` | 验证 Token 有效性 |
| `changePassword()` | 修改密码 |
| `fetchUserInfo()` | 获取用户信息 |

### Token过期判断

```javascript
// stores/user.js

// 统一的Token过期判断函数
function isTokenExpired(token, options = {}) {
  const { useBuffer = true, bufferMs = 5 * 60 * 1000 } = options
  const expiryTime = getTokenExpiryTime(token)
  if (!expiryTime) return true
  
  const now = Date.now()
  return useBuffer ? now >= expiryTime - bufferMs : now >= expiryTime
}
```

---

## 工具函数

### 通用工具 (utils/index.js)

| 函数 | 描述 |
|------|------|
| `formatTime()` | 时间格式化，支持相对时间 |
| `extractData()` | 响应数据提取，兼容多种数据结构 |
| `getRankClass()` | 获取排名样式类 |
| `formatFileSize()` | 文件大小格式化 |
| `truncate()` | 文本截断 |
| `debounce()` | 防抖函数 |
| `throttle()` | 节流函数 |
| `deepClone()` | 深拷贝 |
| `isEmpty/isNotEmpty()` | 空值判断 |
| `generateId()` | 生成唯一 ID |
| `sleep()` | 延迟函数 |
| `retry()` | 重试函数 |

### Axios 封装 (utils/request.js)

```javascript
// 核心功能
- 请求/响应拦截器
- Token 自动注入
- Token 刷新机制（Promise缓存，避免竞态）
- 请求取消（防重复提交）
- 自动重试机制
- XSS/SQL 注入防护
- 统一错误处理

// Token刷新机制
let refreshPromise = null

async function getOrRefreshToken() {
  if (refreshPromise) {
    return refreshPromise  // 多个401请求共享同一个刷新Promise
  }
  
  refreshPromise = refreshToken()
  try {
    return await refreshPromise
  } finally {
    refreshPromise = null
  }
}
```

### 错误提示优化

```javascript
// 根据HTTP状态码提供友好提示
const errorMessages = {
  400: "请求参数有误，请检查输入",
  404: "请求的资源不存在",
  429: "请求过于频繁，请稍后重试",
  500: "服务器内部错误，请稍后重试",
  // ...
}
```

### 缓存工具 (utils/cache.js)

| 函数 | 描述 |
|------|------|
| `setCache()` | 设置缓存 |
| `getCache()` | 获取缓存 |
| `removeCache()` | 移除缓存 |
| `clearCache()` | 清除所有缓存 |

### 媒体工具 (utils/media.js)

| 函数 | 描述 |
|------|------|
| `getFileIcon()` | 获取文件图标 |
| `isImageFile()` | 判断是否图片 |
| `isVideoFile()` | 判断是否视频 |
| `isDocumentFile()` | 判断是否文档 |

### 日志工具 (utils/logger.js)

| 函数 | 描述 |
|------|------|
| `logInfo()` | 信息日志 |
| `logWarn()` | 警告日志 |
| `logError()` | 错误日志 |
| `logAuthWarn()` | 认证警告 |
| `logSecurityWarn()` | 安全警告 |

---

## 安全机制

### XSS 防护 (utils/xss.js)

```javascript
// 覆盖 30+ 危险模式
const XSS_PATTERNS = [
  /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
  /<script[^>]*\/>/gi,
  /javascript\s*:/gi,
  /vbscript\s*:/gi,
  /on\w+\s*=/gi,           // 事件处理器（含空格）
  /on\w+=/gi,              // 事件处理器（无空格）
  /&#x?[0-9a-f]+;?/gi,     // HTML实体编码
  /eval\s*\(/gi,
  /expression\s*\(/gi,
  /<iframe/gi,
  /<object/gi,
  /<embed/gi,
  // ... 更多模式
]

// 检测XSS
export function containsXss(input) {
  return XSS_PATTERNS.some(pattern => pattern.test(input))
}

// 清理XSS
export function sanitize(input) {
  let result = input
  XSS_PATTERNS.forEach(pattern => {
    result = result.replace(pattern, '')
  })
  return result
}
```

### SQL注入防护

```javascript
// 检测SQL注入模式
const SQL_INJECTION_PATTERNS = [
  /('|")\s*(OR|AND)\s*('|")/i,
  /UNION\s+SELECT/i,
  /;\s*(DROP|DELETE|UPDATE|INSERT)/i,
  // ...
]

export function containsSqlInjection(input) {
  return SQL_INJECTION_PATTERNS.some(pattern => pattern.test(input))
}
```

### 密码验证规则

```javascript
// 统一密码规则（与后端一致）
const passwordRules = [
  { required: true, message: '请输入密码' },
  { min: 8, max: 50, message: '密码长度为8-50位' },
  { 
    validator: (rule, value, callback) => {
      if (!/[a-zA-Z]/.test(value)) {
        callback(new Error('密码必须包含字母'))
      } else if (!/[0-9]/.test(value)) {
        callback(new Error('密码必须包含数字'))
      } else if (/\s/.test(value)) {
        callback(new Error('密码不能包含空格'))
      } else {
        callback()
      }
    }
  }
]
```

---

## 样式系统

### CSS 变量 (styles/variables.css)

```css
:root {
  /* 品牌色 - 侗族文化特色 */
  --dong-indigo: #1A5276;        /* 靛蓝 - 主色调 */
  --dong-jade: #28B463;          /* 青绿 - 辅助色 */
  --dong-gold: #c9a227;          /* 金铜 - 强调色 */
  
  /* 功能色 */
  --color-primary: #1A5276;
  --color-success: #28B463;
  --color-warning: #f5a623;
  --color-danger: #e74c3c;
  
  /* 间距系统 */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
  
  /* 圆角系统 */
  --radius-xs: 4px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  
  /* 阴影系统 */
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 8px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 16px rgba(0, 0, 0, 0.1);
}
```

### 样式文件说明

| 文件 | 描述 |
|------|------|
| variables.css | CSS 变量定义，设计系统 |
| base.css | 基础样式，重置样式、排版、通用布局 |
| components.css | 组件样式，卡片、按钮、表单等 |
| pages.css | 页面样式，各页面通用样式 |
| common.css | 通用样式 |
| home.css | 首页专用样式 |
| media-common.css | 媒体相关通用样式 |
| dialog-common.css | 对话框通用样式 |

---

## 快速开始

### 环境要求

- Node.js 18+
- npm 9+

### 安装步骤

```bash
# 1. 进入项目目录
cd dong-medicine-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

### 常用命令

```bash
# 开发模式
npm run dev

# 代码检查
npm run lint

# 运行测试
npm run test:run

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

### 访问地址

- 开发地址: http://localhost:5173
- 生产构建: `dist/` 目录

---

## Docker 部署

### 构建镜像

```bash
docker build -t dong-medicine-frontend .
```

### 运行容器

```bash
docker run -d \
  --name dong-medicine-frontend \
  -p 80:80 \
  dong-medicine-frontend
```

### 使用 Docker Compose

```bash
# 在项目根目录执行
docker-compose up -d --build
```

### Nginx 配置说明

前端使用 Nginx 作为 Web 服务器，配置文件：

- `nginx.conf` - Nginx 主配置
- `default.conf` - 站点配置，包含：
  - 静态资源服务
  - API 请求代理转发
  - Gzip 压缩
  - 缓存策略

---

## 目录说明

| 目录 | 说明 |
|------|------|
| `views/` | 页面组件，对应路由 |
| `components/` | 可复用组件 |
| `composables/` | 组合式函数，复用逻辑 |
| `stores/` | Pinia 状态管理 |
| `router/` | 路由配置 |
| `utils/` | 工具函数 |
| `styles/` | 全局样式 |
| `config/` | 应用配置 |
| `directives/` | 自定义指令 |
| `__tests__/` | 测试文件 |

---

**最后更新时间**：2026年3月30日
