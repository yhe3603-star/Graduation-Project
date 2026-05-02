# 组件目录 (components/)

组件是 Vue 应用的构建块，每个 `.vue` 文件封装了模板、逻辑和样式。本项目采用三层组件架构，按复用程度和业务相关性分为 base、common、business 三层。

## 三层架构

```
┌─────────────────────────────────────────────┐
│           business（业务组件层）              │
│   侗医药专属：植物卡片、知识问答、上传器等     │
├─────────────────────────────────────────────┤
│           common（通用组件层）                │
│   跨页面复用的辅助 UI：骨架屏、加载状态等      │
├─────────────────────────────────────────────┤
│           base（基础组件层）                  │
│   框架级通用组件：错误边界、虚拟列表           │
└─────────────────────────────────────────────┘
```

---

## 一、base/ -- 基础组件层

框架级通用组件，与项目业务完全解耦，可迁移到任何 Vue 3 项目。

### ErrorBoundary.vue

Vue 3 错误边界组件，使用 `onErrorCaptured` 生命周期钩子捕获子组件渲染期间的异常。

**Props：** 无（通过 slot 包裹子组件）

**功能：**
- 捕获子组件树中的 render error，显示友好的错误界面
- 显示错误消息和调用栈（可折叠详情）
- "重试"按钮重置错误状态
- "返回首页"按钮跳转到 `/`
- 在 `App.vue` 中被用于包裹每个路由视图

**用法示例：**
```vue
<ErrorBoundary>
  <router-view />
</ErrorBoundary>
```

### VirtualList.vue

虚拟滚动列表组件，用于渲染大量数据时只渲染可视区域内的 DOM 节点，优化性能。

---

## 二、common/ -- 通用组件层

跨页面复用的辅助 UI 组件，无业务语义。

### PageLoading.vue

全页面级加载状态遮罩。

**Props：** `visible` (Boolean) -- 控制显示/隐藏

**功能：**
- 半透明遮罩层 + 居中 spinner
- `App.vue` 中在路由切换时自动显示（`router.beforeEach` 开启，`router.afterEach` 100ms 后关闭）

### SkeletonGridCard.vue

卡片列表骨架屏，模拟文字卡片加载状态。

**Props：** `count` (Number, default: 8) -- 骨架卡片数量

### SkeletonGridImage.vue

图片卡片骨架屏，模拟带缩略图的卡片加载状态。

**Props：** `count` (Number, default: 8)

### SkeletonListQa.vue

问答列表骨架屏，模拟问答卡片（问题 + 答案预览布局）。

**Props：** `count` (Number, default: 5)

### SkeletonListResource.vue

资源列表骨架屏，模拟资源行（图标 + 信息 + 操作按钮布局）。

**Props：** `count` (Number, default: 5)

---

## 三、business/ -- 业务组件层

侗医药业务专属组件，按功能域分为多个子目录。

### layout/ -- 布局组件

#### AppHeader.vue

全局顶部导航栏。

**Props：**
- `isLoggedIn` (Boolean) -- 用户登录状态
- `userName` (String) -- 显示用户名
- `isAdmin` (Boolean) -- 是否管理员（决定是否显示管理后台入口）

**Events：**
- `logout` -- 用户点击退出
- `show-login` -- 弹出登录对话框
- `show-register` -- 弹出注册对话框

**功能：**
- Logo 区（SVG 侗医药图标 + "侗乡医药"标题 + "非遗数字展示平台"副标题），点击回首页
- 主导航：知识库、传承人、植物图鉴、问答、互动专区、学习资源
- "更多"下拉菜单：节气采药、关于平台、数据可视化、药材对比
- 搜索按钮（跳转到 `/search`）
- 用户区：登录/注册按钮 或 用户头像下拉菜单（个人中心、管理后台[管理员]、退出登录）
- 响应式移动端汉堡菜单
- `activeIndex` 通过 `useRoute()` 计算当前激活的导航项

#### AppFooter.vue

全局底部信息栏。

**功能：**
- 三列布局：关于我们、快速导航（知识库/传承人/药用图鉴/问答社区）、联系方式
- 底部版权信息："2026 侗乡医药非遗数字展示平台"
- 靛蓝渐变背景 + 斜纹装饰纹理

---

### display/ -- 展示组件

#### SearchFilter.vue

搜索 + 分类筛选组合组件，几乎所有列表页使用。

**Props：**
- `modelValue` / `keyword` (String) -- 搜索关键词（支持 v-model）
- `placeholder` (String) -- 搜索框占位文字
- `filters` (Array) -- 筛选配置数组，每项 `{ key, label, options: [{value, label}] }`

**Events：**
- `search` -- 搜索触发
- `filter` -- 筛选条件变更 `{ key, value }`

**功能：**
- 搜索输入框（带搜索图标，el-input large size），输入即触发搜索
- 筛选标签行：每个筛选组以 el-tag 形式展示选项，点击切换选中状态（effect dark/plain）

#### CardGrid.vue

通用卡片网格布局。

**Props：**
- `items` (Array) -- 数据列表
- `title-field` (String) -- 用作标题的字段名
- `desc-length` (Number/String) -- 描述截断长度

**Slots：**
- `footer` -- 卡片底部自定义内容（常用：标签行、统计行、操作按钮）

**功能：** 响应式网格布局，自动调整列数

#### Pagination.vue

统一的分页组件，封装 Element Plus 的 `el-pagination`。

**Props：** `page` (Number)、`size` (Number)、`total` (Number)

**Events：** `update:page`、`update:size`

#### ChartCard.vue

ECharts 图表容器卡片。

**Props：**
- `title` (String) -- 卡片标题
- `option` (Object) -- ECharts 配置对象
- `height` (Number, default: 300) -- 图表高度(px)
- `loading` (Boolean) -- 加载状态

**Slots：** `actions` -- 图表右上角操作区（如类型切换按钮）

**功能：**
- 按需引入 ECharts 组件（`echarts/core`）：BarChart、LineChart、PieChart、RadarChart
- 响应式 resize（监听 window resize）
- 自动销毁实例（onUnmounted）

#### StatCard.vue

统计数字卡片。

**Props：** `icon` (String)、`label` (String)、`value` (Number/String)、`color` (String)

#### AiChatCard.vue

AI 智能对话卡片，展示用户与侗医药 AI 的对话记录。

**功能：**
- 消息列表渲染（用户/助手角色区分）
- Markdown 内容渲染（marked + DOMPurify）
- 打字动画效果
- 复制消息功能
- AI 对话状态指示器

#### KnowledgeGraph.vue

知识图谱可视化组件，展示侗医药知识节点与关联关系。

#### UpdateLogCard.vue / UpdateLogDialog.vue

更新日志卡片和编辑对话框，用于管理后台内容条目的版本记录。

- `UpdateLogCard`：展示日志时间线
- `UpdateLogDialog`：新增/编辑日志条目的弹窗表单

#### PageSidebar.vue

页面侧边栏容器，用于页面内的子导航或筛选面板。

---

### interact/ -- 互动组件

#### CaptchaInput.vue

图形验证码输入组件。

**Props：** `modelValue` (String) -- 验证码输入值

**Events：**
- `update:modelValue` -- v-model 绑定更新
- `update:captcha-key` -- 验证码 key 更新（用于服务端校验）

**功能：**
- 输入框 + 验证码图片（点击刷新）
- 通过 `refreshCaptcha()` 方法暴露给父组件（登录失败后自动刷新）

#### CommentSection.vue

评论区组件。

**Props：** 评论数据、登录状态

**功能：**
- 评论列表（分页）
- 发表评论表单（支持回复嵌套）
- 登录状态检查
- 审核状态提示

#### QuizSection.vue

趣味答题核心组件。

**Props：**
- `is-started`、`finished`、`loading`、`submitting` -- 状态标志
- `questions` (Array) -- 题目列表
- `current` (Number) -- 当前题号
- `answers` (Array) -- 用户答案
- `score`、`correct` -- 成绩
- `formatted-time`、`is-low-time` -- 倒计时显示
- `selected-difficulty` -- 当前难度

**Events：** `start`、`prev`、`next`、`submit`、`retry`、`share`、`select-difficulty`

**功能：**
- 难度选择面板
- 答题进度条 + 题号指示
- 倒计时显示（低于 10 秒警告样式）
- 上一题/下一题导航按钮
- 提交确认（提示未答题数）
- 成绩展示面板（分数、正确率、评价表情）
- 分享按钮（Web Share API / 剪贴板复制）

#### InteractSidebar.vue

互动专区侧边栏，展示用户统计。

**Props：** `quiz-count`、`best-score`、`game-count`、`total-game-score`

#### PlantGame.vue

植物识别游戏组件。

**Props：**
- `current-plant` (Object) -- 当前植物对象
- `options` (Array) -- 选项列表
- `score`、`streak` -- 分数和连击数
- `answered`、`selected-answer`、`finished`、`game-started` -- 状态
- `correct`、`total` -- 正确数和总数
- `is-logged-in` -- 登录状态
- `selected-difficulty` -- 难度
- `formatted-time`、`is-low-time` -- 倒计时

**Events：** `select-difficulty`、`start`、`answer`、`end-game`、`favorite`、`restart`

**功能：**
- 难度选择
- 植物图片展示 + 名称选项
- 答对/答错视觉反馈（绿色/红色高亮）
- 连击计数和额外加分
- 倒计时显示
- 游戏结束统计
- 收藏当前植物按钮

---

### media/ -- 媒体组件

#### ImageCarousel.vue

图片轮播组件，支持缩略图导航和全屏预览。

#### VideoPlayer.vue

视频播放器组件，封装 HTML5 `<video>` 标签。

#### HerbAudio.vue

草药音频播放组件，用于播放草药名称的侗语发音。

#### MediaDisplay.vue

综合媒体展示组件，内部使用 `el-tabs` 切换图片/视频/文档 Tab。

**依赖：** `useMediaTabs`、`useMediaDisplay` composables

#### DocumentList.vue

文档列表组件，展示可预览/下载的文档列表。

#### DocumentPreview.vue

文档预览组件，通过 KKFileView 服务实现 Office/PDF 在线预览。

**功能：**
- 支持 PDF、Word、Excel、PPT、TXT 预览
- iframe 嵌入 KKFileView 预览页面
- 加载状态 + 错误处理
- 全屏预览切换

---

### upload/ -- 上传组件

#### ImageUploader.vue

图片上传组件。

**依赖：** `useFileUpload({ type: 'image' })` composable

**功能：**
- 拖拽/点击上传
- 多图上传
- 预览缩略图
- 排序（vuedraggable 拖拽排序）
- 删除
- 上传进度条
- 图片格式校验（jpg/jpeg/png/gif/webp）

#### VideoUploader.vue

视频上传组件。

**依赖：** `useFileUpload({ type: 'video' })`

**功能：**
- 视频文件上传
- 视频预览播放
- 格式校验（mp4/avi/mov/wmv/flv/mkv）
- 大小限制提示

#### DocumentUploader.vue

文档上传组件。

**依赖：** `useFileUpload({ type: 'document' })`

**功能：**
- 文档文件上传
- 文件类型图标展示
- 格式校验（pdf/doc/docx/xls/xlsx/ppt/pptx/txt）

#### FileUploader.vue

通用文件上传组件，`useFileUpload` 的底层实现。

---

### dialogs/ -- 前台详情弹窗

各模块的详情查看弹窗，从列表页点击卡片时触发。

| 组件 | 对应模块 | 展示内容 |
|------|---------|---------|
| `PlantDetailDialog.vue` | 植物图鉴 | 植物图片轮播、名称（中/侗/学名）、功效、用法、栖息地、形态描述、媒体文件（图片/视频/文档 Tab） |
| `KnowledgeDetailDialog.vue` | 知识库 | 疗法名称、分类、疾病类别、详细内容（支持图片）、关联植物标签 |
| `InheritorDetailDialog.vue` | 传承人 | 姓名、等级、特长、传承谱系、代表案例、联系方式 |
| `QuizDetailDialog.vue` | 题目管理 | 题目内容、选项、正确答案、分类、难度 |
| `ResourceDetailDialog.vue` | 学习资源 | 资源名称、描述、文件类型、大小、在线预览 |

---

### admin/ -- 管理后台组件

#### AdminSidebar.vue

管理后台左侧导航菜单。

**Props：** `active-menu` (String)、`user-name` (String)、`logout-loading` (Boolean)

**Events：** `update:active-menu`、`logout`

**菜单项：** 仪表盘、用户管理、植物管理、知识管理、传承人管理、问答管理、资源管理、反馈管理、题目管理、评论管理

#### AdminDashboard.vue

管理后台仪表盘，展示各模块统计数据概览。

**Props：** `stats`、及各模块数据列表

**Events：** `view-feedback`、`navigate`

**功能：**
- 统计卡片行（总用户数、知识数、植物数等）
- 最近反馈列表
- 最近用户列表
- 图表预览区
- 快捷操作入口

#### AdminDataTable.vue

管理后台通用数据表格。

**Props：**
- `data` (Array) -- 数据源
- `columns` (Array) -- 列配置 `[{ prop, label, width }]`
- `server-pagination` (Boolean) -- 是否服务端分页
- `server-total`、`page`、`page-size` -- 分页参数
- `show-actions` (Boolean) -- 是否显示操作列
- `actions` (Array) -- 操作按钮配置
- `show-add` (Boolean) -- 是否显示新增按钮

**Events：** `page-change`、`size-change`、`view`、`edit`、`delete`、`add`

**Slots：** 自定义列渲染（如 `status`、`actions`）、筛选区

#### admin/dialogs/ -- 管理端详情弹窗

管理后台特有的详情弹窗（与前台 dialogs 对应但有更多管理字段，如反馈处理状态、用户封禁信息等）。

#### admin/forms/ -- 管理端表单弹窗

CRUD 表单弹窗，均使用 `useFormDialog` composable：

| 组件 | 对应模块 |
|------|---------|
| `PlantFormDialog.vue` | 药用植物 |
| `KnowledgeFormDialog.vue` | 知识条目 |
| `InheritorFormDialog.vue` | 传承人 |
| `QaFormDialog.vue` | 问答 |
| `QuizFormDialog.vue` | 题目 |
| `ResourceFormDialog.vue` | 学习资源 |

每个表单弹窗包含：
- 新增/编辑双模式（通过 `isEdit` computed 判断）
- 内嵌更新日志管理（`UpdateLogCard` + `UpdateLogDialog`）
- 数组字段自动序列化/反序列化（如标签、图片列表）
- 表单验证 + 提交 loading 状态
