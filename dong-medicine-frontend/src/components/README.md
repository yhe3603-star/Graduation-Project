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
- 显示错误消息和调用栈（可折叠详情，仅开发环境显示）
- "重试"按钮重置错误状态（`hasError = false`，并弹出成功提示）
- "返回首页"按钮跳转到 `/`
- 使用 `import.meta.env.DEV` 判断是否为开发环境，仅在开发环境显示错误详情折叠面板
- 在 `App.vue` 中被用于包裹每个路由视图

**用法示例：**
```vue
<ErrorBoundary>
  <router-view />
</ErrorBoundary>
```

### VirtualList.vue

虚拟滚动列表组件，用于渲染大量数据时只渲染可视区域内的 DOM 节点，优化性能。

**Props：**
- `items` (Array, required) -- 全部数据列表
- `itemSize` (Number, default: 50) -- 每项高度（像素）
- `buffer` (Number, default: 5) -- 可视区域上下缓冲项数
- `keyField` (String, default: 'id') -- 用作 key 的字段名

**Events：**
- `scroll` -- 滚动事件，参数 `{ scrollTop, startIndex, endIndex }`
- `visible-change` -- 可视范围变化，参数 `{ startIndex, endIndex }`

**Expose：**
- `scrollToIndex(index)` -- 滚动到指定索引
- `scrollToTop()` -- 滚动到顶部
- `updateContainerHeight()` -- 更新容器高度

**核心算法：**
- `totalHeight = items.length * itemSize` -- 计算总高度，用 phantom div 撑开滚动区域
- `visibleCount = ceil(containerHeight / itemSize) + buffer * 2` -- 计算可见项数（含缓冲区）
- `startIndex = max(0, floor(scrollTop / itemSize) - buffer)` -- 计算起始渲染索引
- `offset = startIndex * itemSize` -- 计算内容偏移量，通过 `translateY` 定位
- 监听 `resize` 事件动态更新容器高度

**注意：** `index.js` 仅导出 `ErrorBoundary`，`VirtualList` 需直接引用 `.vue` 文件。

---

## 二、common/ -- 通用组件层

跨页面复用的辅助 UI 组件，无业务语义。

### PageLoading.vue

全页面级加载状态遮罩。

**Props：** `visible` (Boolean, default: false) -- 控制显示/隐藏

**功能：**
- 半透明白色遮罩层（`rgba(255, 255, 255, 0.9)`）+ `backdrop-filter: blur(4px)` 毛玻璃效果
- 双环旋转 spinner：外环靛蓝-绿色（0.8s），内环反向旋转（0.6s），使用品牌色 `#1A5276` 和 `#28B463`
- "加载中"文字 + 三点弹跳动画（0.15s 间隔错开）
- `<Transition name="page-loading">` 包裹，0.2s 淡入淡出过渡
- `App.vue` 中在路由切换时自动显示（`router.beforeEach` 开启，`router.afterEach` 100ms 后关闭）

### SkeletonGridCard.vue

卡片列表骨架屏，模拟文字卡片加载状态（圆形图标 + 标题行 + 描述行 + 底部标签）。

**Props：** `count` (Number, default: 12) -- 骨架卡片数量

**布局：** CSS Grid 响应式布局，`grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))`，768px 以下单列

### SkeletonGridImage.vue

图片卡片骨架屏，模拟带缩略图的卡片加载状态（图片区 + 标题 + 描述 + 底部标签）。

**Props：** `count` (Number, default: 12) -- 骨架卡片数量

**布局：** 与 SkeletonGridCard 相同的 Grid 布局，图片区高度 180px，768px 以下 `minmax(160px, 1fr)` + 图片高度 120px

### SkeletonListQa.vue

问答列表骨架屏，模拟问答卡片（圆形问题图标 + 问题标题 + 答案预览 + 底部统计/操作按钮）。

**Props：** `count` (Number, default: 6) -- 骨架项数量

**布局：** 纵向 Flex 列表，768px 以下底部区域自动换行

### SkeletonListResource.vue

资源列表骨架屏，模拟资源行（圆形图标 + 信息区 + 操作按钮组）。

**Props：** `count` (Number, default: 6) -- 骨架项数量

**布局：** 纵向 Flex 列表，768px 以下操作按钮换行至底部

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
- `modelValue` (String) -- 搜索关键词（支持 v-model）
- `placeholder` (String, default: "搜索...") -- 搜索框占位文字
- `filters` (Array, default: []) -- 筛选配置数组，每项 `{ key, label, options: [{value, label}], type? }`

**Events：**
- `update:modelValue` -- v-model 双向绑定更新
- `search` -- 搜索触发（400ms 防抖）
- `filter` -- 筛选条件变更，参数为当前所有筛选条件对象 `{ key: value }`

**功能：**
- 搜索输入框（带搜索图标，el-input large size），400ms 防抖输入即触发搜索
- 筛选标签行：每个筛选组以 el-tag 形式展示选项，点击切换选中状态（effect dark/plain），再次点击取消选中

#### CardGrid.vue

通用卡片网格布局，支持图片展示、徽章标记、多字段自适应。

**Props：**
- `items` (Array, default: []) -- 数据列表
- `showImage` (Boolean, default: true) -- 是否显示图片区
- `imageField` (String, default: "images") -- 图片字段名
- `imageErrorText` (String, default: "图片") -- 图片加载失败提示文字
- `titleField` (String, default: "title") -- 用作标题的字段名
- `descLength` (Number, default: 50) -- 描述截断长度

**Events：**
- `click` -- 卡片点击，参数为对应 item 对象

**Slots：**
- `footer` -- 卡片底部自定义内容（作用域插槽，提供 `item`）

**功能：**
- 响应式网格布局（`auto-fill, minmax(260px, 1fr)`），768px 以下双列，480px 以下单列
- 图片字段智能解析：支持 JSON 字符串/数组/对象路径/URL 等多种格式，自动补全 `/` 前缀
- 徽章分类映射：`BADGE_CLASS_MAP` 将难度/等级/徽章值映射为 gold/green/blue 样式类
- 标题字段回退链：`item[titleField]` -> `item.nameCn` -> `item.name` -> `item.title` -> "未命名"
- `<transition-group name="card-fade">` 卡片入场/离场动画

#### Pagination.vue

统一的分页组件，封装 Element Plus 的 `el-pagination`。

**Props：**
- `page` (Number, default: 1) -- 当前页码
- `size` (Number, default: 6) -- 每页条数
- `total` (Number, default: 0) -- 总条数
- `pageSizes` (Array, default: [6, 9, 12, 24, 48]) -- 可选每页条数
- `layout` (String, default: "total, sizes, prev, pager, next, jumper") -- 分页布局
- `background` (Boolean, default: true) -- 是否使用背景色

**Events：**
- `update:page` -- 页码更新（v-model:page）
- `update:size` -- 每页条数更新（v-model:size）
- `change` -- 分页变化，参数 `{ page, size }`

**功能：** `total > 0` 时才渲染，768px 以下隐藏总数/条数/跳转控件

#### ChartCard.vue

ECharts 图表容器卡片。

**Props：**
- `title` (String) -- 卡片标题
- `option` (Object, required) -- ECharts 配置对象
- `height` (Number, default: 300) -- 图表高度(px)
- `loading` (Boolean, default: false) -- 加载状态

**Slots：** `actions` -- 图表右上角操作区（如类型切换按钮）

**功能：**
- 按需引入 ECharts 组件（`echarts/core`）：BarChart、LineChart、PieChart、RadarChart
- 响应式 resize（监听 window resize）
- `watch(option, deep)` 深度监听配置变化，自动更新图表
- 自动销毁实例（onUnmounted）
- `defineExpose({ resize, chart })` 暴露 resize 方法和 chart 实例

#### StatCard.vue

统计数字卡片，用于仪表盘等场景的数据概览展示。

**Props：**
- `icon` (Object, required) -- Element Plus 图标组件
- `label` (String, required) -- 统计项标签
- `value` (Number|String, required) -- 统计数值
- `color` (String, required) -- 图标背景色

**功能：** 左侧彩色圆角图标 + 右侧数值/标签，使用 CSS 变量控制间距和字号

#### KnowledgeGraph.vue

知识图谱可视化组件，展示侗医药知识节点与关联关系。

**Props：**
- `plantName` (String, default: '') -- 中心植物名称
- `relatedPlants` (Array, default: []) -- 关联植物列表，每项可含 `nameCn/name/efficacy/category/diseaseCategory/herbCategory/therapyCategory`
- `knowledgeTitle` (String, default: '侗医药知识') -- 中心知识节点标题
- `height` (String|Number, default: '500px') -- 图谱高度
- `width` (String|Number, default: '100%') -- 图谱宽度

**功能：**
- 基于 ECharts Graph 力导向布局（`layout: 'force'`），支持拖拽和漫游
- 三类节点：知识（靛蓝，symbolSize: 50）、药材（绿色，symbolSize: 36）、功效分类（蓝色，symbolSize: 28）
- 自动去重功效分类节点（`efficacySet` Map 去重）
- `ResizeObserver` 监听容器尺寸变化自动 resize
- `watch([relatedPlants, knowledgeTitle, plantName], deep)` 监听数据变化更新图谱
- 点击节点高亮相邻节点（`emphasis.focus: 'adjacency'`）

#### AiChatCard.vue

AI 智能对话卡片，展示用户与侗医药 AI 助手的对话界面。该组件是一个容器组件，负责组合 5 个子组件并管理 WebSocket 连接和会话状态。

**依赖 Composables：**
- `useChatWebSocket()` -- WebSocket 连接管理、消息收发、流式输出控制
- `useChatSessions()` -- 历史会话列表加载/切换/删除
- `useUserStore()` -- 用户登录状态

**子组件（来自 `ai-chat/` 子目录）：**
- `WelcomePanel` -- 欢迎面板，首次进入时展示快捷提问标签
- `ChatMessageList` -- 消息列表，Markdown 渲染 + 流式光标动画
- `ChatInputArea` -- 输入区域，发送/停止按钮切换
- `SessionDrawer` -- 会话抽屉，历史会话列表管理
- `SessionBadge` -- 会话徽章，当前会话状态标识

**功能：**
- WebSocket 实时对话（连接/断开/重连生命周期管理）
- 流式输出（逐 token 渲染 + 闪烁光标 `▌`）
- 历史会话管理（加载/切换/删除/新建）
- 登录状态联动（未登录时发送提示登录）
- `onActivated`/`onDeactivated` 支持 keep-alive 场景

```vue
<!-- 使用示例 -->
<AiChatCard />
```

> 详细的 ai-chat 子组件文档见 [display/ai-chat/README.md](./business/display/ai-chat/README.md)

#### UpdateLogCard.vue

更新日志卡片，以时间线形式展示内容条目的版本更新记录。

**Props：**
- `logs` (Array, default: []) -- 日志列表，每项 `{ id, content, time, operator }`
- `title` (String, default: '更新日志') -- 卡片标题
- `limit` (Number, default: 5) -- 默认显示条数
- `editable` (Boolean, default: false) -- 是否显示编辑/删除按钮
- `emptyText` (String, default: '暂无更新记录') -- 空状态提示

**Events：**
- `add` -- 点击添加日志按钮
- `edit` -- 点击编辑按钮，参数为日志对象
- `delete` -- 确认删除后触发，参数为日志对象

**功能：**
- `el-timeline` 时间线展示，5 色循环节点颜色
- 超过 `limit` 条时显示"查看全部"按钮（展开/收起切换）
- 编辑模式：每条日志显示编辑/删除按钮，删除前弹出确认对话框
- 时间格式化使用 `useUpdateLog` composable 的 `formatLogTime` 方法

#### UpdateLogDialog.vue

更新日志新增/编辑对话框。

**Props：**
- `visible` (Boolean, default: false) -- 对话框显示状态（支持 v-model:visible）
- `editingLog` (Object, default: null) -- 编辑时传入的日志对象，null 为新增模式
- `defaultOperator` (String, default: '管理员') -- 默认操作人

**Events：**
- `update:visible` -- 对话框显示状态更新
- `save` -- 保存日志，参数 `{ id, time, operator, content }`

**功能：**
- 新增/编辑双模式（通过 `editingLog` 是否为 null 判断）
- 表单字段：日期（DatePicker）、操作人（Input）、内容（Textarea，500字限制）
- 打开时自动填充编辑数据或重置表单
- 保存前校验内容非空

#### PageSidebar.vue

页面侧边栏容器，展示数据统计和热门推荐。

**Props：**
- `title` (String, default: "数据统计") -- 统计区标题
- `stats` (Array, default: []) -- 统计数据，每项 `{ value, label }`
- `hotTitle` (String) -- 热门推荐区标题
- `hotItems` (Array, default: []) -- 热门推荐列表，每项支持 `name/title/nameCn/question` 字段

**Events：**
- `hotClick` -- 点击热门项

**Slots：** `default` -- 侧边栏底部额外内容

**功能：**
- 上部统计卡片：2x2 Grid 展示数值/标签
- 下部热门推荐列表：前三名分别使用金/银/铜色排名标记

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
