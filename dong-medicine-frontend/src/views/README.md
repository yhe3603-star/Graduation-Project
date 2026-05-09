# 页面组件目录 (views/)

页面组件是直接对应一个 URL 路由的 Vue 组件，每个 `.vue` 文件代表一个完整的页面。它们组装各种子组件（来自 `components/`），通过 `composables/` 复用有状态逻辑，通过 `utils/` 调用工具函数。

## 全部 14 个页面

| 页面 | 路由 | 权限 | 功能说明 |
|------|------|------|---------|
| `Home.vue` | `/` | 公开 | 平台首页，英雄区 + 每周精选 + 热门搜索词云 + 最新更新 + 核心功能 + 拓展功能 + CTA |
| `Plants.vue` | `/plants` | 公开 | 药用资源图鉴，药材搜索/筛选/收藏 + 侧边栏统计与更新日志 |
| `Knowledge.vue` | `/knowledge` | 公开 | 非遗医药知识库，药方疗法搜索/分类/收藏 + 侧边栏统计与更新日志 |
| `Inheritors.vue` | `/inheritors` | 公开 | 传承人风采，谱系展示/技艺介绍/等级标识 + 侧边栏统计与更新日志 |
| `Qa.vue` | `/qa` | 公开 (keepAlive) | 问答社区，问题搜索/浏览/收藏 + AI聊天入口 + 反馈入口 |
| `Resources.vue` | `/resources` | 公开 | 学习资源，文件预览/下载/收藏/批量选择下载 + 侧边栏统计与更新日志 |
| `Interact.vue` | `/interact` | 公开 | 文化互动专区，趣味答题 + 植物识别游戏 + 评论交流 |
| `About.vue` | `/about` | 公开 | 平台介绍，选题背景/平台特色/功能模块/平台数据/侗医文化/合规声明 |
| `Feedback.vue` | `/feedback` | 公开 | 意见反馈表单 + 我的反馈记录 + 反馈须知/统计侧边栏 |
| `Admin.vue` | `/admin` | 需登录 + 管理员 | 管理后台（仪表盘/用户/植物/知识/传承人/问答/资源/反馈/题目/评论/日志） |
| `PersonalCenter.vue` | `/personal` | 需登录 | 个人中心（学习统计/我的收藏/答题记录/浏览历史/修改密码） |
| `GlobalSearch.vue` | `/search` | 公开 | 全局搜索（自动补全/搜索历史/热门搜索/分类结果/关键词高亮） |
| `Visual.vue` | `/visual` | 公开 | 数据可视化（统计卡片/柱状图/饼图/雷达图/地域分布图/热度排行） |
| `NotFound.vue` | `/:pathMatch(.*)*` | 公开 | 404 页面（带装饰圆圈） |

### 子组件目录

| 目录 | 文件 | 功能 |
|------|------|------|
| `personal-center/` | `ProfileSection.vue` | 个人信息卡（头像、用户名、角色、学习统计摘要、快捷操作） |
| `personal-center/` | `StatsDashboard.vue` | 学习统计仪表盘（答题/游戏/收藏/浏览数据 + ECharts 得分趋势图） |
| `personal-center/` | `FavoritesPanel.vue` | 我的收藏面板（按类型筛选、分页、跳转详情） |
| `personal-center/` | `QuizHistoryPanel.vue` | 答题记录面板（答题+游戏记录合并按时间排序） |
| `personal-center/` | `BrowseHistoryPanel.vue` | 浏览历史面板（分页展示、点击跳转） |
| `personal-center/` | `SettingsPanel.vue` | 账号设置面板（基本信息展示 + 修改密码表单含验证码） |

---

## Home.vue -- 首页

**路由：** `/` | **权限：** 公开

平台的门户页面，由多个区块组成：

- **英雄区（Hero Section）**：展示非遗徽章（"广西壮族自治区级非物质文化遗产"）、平台标题"非遗视角下侗乡医药数字展示平台"、副标题"保护 · 传承 · 活态传播"、描述文字、统计数据卡片（植物数量、经典药方、传承人数量、核心疗法，数据来自 `/stats/overview`）、"立即探索"和"了解非遗"两个 CTA 按钮
- **每周精选区**：从植物/知识/传承人数据中按浏览量+收藏量排序，随机选取一条展示，包含图片、标签、标题、描述、浏览/收藏统计
- **热门搜索区**：从 `/search/hot` 获取热门关键词，渲染为词云（字号/透明度/颜色按热度映射），点击跳转全局搜索
- **最新更新区**：合并植物/知识/传承人/问答数据按更新时间排序取前 6 条，展示图片、标签、标题、描述、更新时间
- **核心功能区**：4 个功能模块入口卡片（知识库、传承人、药用图鉴、问答社区），每个有独立图标和渐变色，展示条目数量
- **拓展功能区**：3 个拓展功能入口（文化互动、学习资源、数据可视化），列表式布局
- **CTA 区**：底部号召行动区，"探索侗乡医药文化" + "开始互动体验"按钮

**数据加载：** `onMounted` 中并行请求 `/stats/overview`、植物/传承人/知识/问答列表，失败时使用默认统计数据

**使用的工具：** `extractPageData`、`logFetchError`、`getFirstImage`、`formatTime`（来自 `@/utils`）

---

## Plants.vue -- 药用资源图鉴

**路由：** `/plants` | **权限：** 公开

药材图鉴页面，展示黔东南道地药材和侗医传统药用植物：

- **搜索筛选**：使用 `SearchFilter` 组件，支持关键词搜索 + 分类/用法筛选（筛选项从 `/metadata/filters` 动态加载）
- **卡片网格**：使用 `CardGrid` 组件展示药材缩略图、名称、用法标签、栖息地、浏览/收藏计数
- **收藏功能**：使用 `useFavorite('plant')` composable，每张卡片上有星形收藏按钮
- **骨架屏**：使用 `SkeletonGridImage` 在加载时显示 12 个占位卡片
- **分页**：使用 `Pagination` 组件实现服务端分页
- **详情弹窗**：点击卡片打开 `PlantDetailDialog` 查看详细信息
- **侧边栏**：`PageSidebar` 展示药材统计（总数/分类数/收藏量/浏览量）、热门药材、`UpdateLogCard` 更新日志
- **路由查询支持**：监听 `route.query.id`，支持通过 URL 参数直接打开详情弹窗

**使用的 composable：** `useFavorite`、`useUpdateLog`、`useDebounceFn`

---

## Knowledge.vue -- 非遗医药知识库

**路由：** `/knowledge` | **权限：** 公开

展示侗医药传统疗法和药方知识：

- **搜索筛选**：`SearchFilter` 组件，支持疗法类别/疾病类别/药材类别三维度筛选（筛选项从 `/metadata/filters` 动态加载）
- **列表卡片**：每个条目展示疗法类别标签、标题、内容摘要（截断 80 字符）、疾病分类、浏览量/收藏量
- **骨架屏**：`SkeletonGridCard`（12 个占位卡片）
- **收藏**：使用 `useFavorite('knowledge')`
- **详情弹窗**：`KnowledgeDetailDialog` 展示完整药方或疗法内容
- **分页**：服务端分页
- **侧边栏**：`PageSidebar` 展示知识统计（总数/疗法分类/疾病分类/类型/收藏量/浏览量）、热门知识、`UpdateLogCard` 更新日志
- **路由查询支持**：监听 `route.query.id`，支持通过 URL 参数直接打开详情弹窗

**使用的 composable：** `useFavorite`、`useUpdateLog`、`useDebounceFn`

---

## Inheritors.vue -- 传承人风采

**路由：** `/inheritors` | **权限：** 公开

展示侗医药非遗传承人信息：

- **搜索筛选**：按姓名/技艺搜索 + 等级筛选（筛选项从 `/metadata/filters` 动态加载）
- **网格卡片**：每张卡片展示头像首字、姓名、等级标签（带颜色区分：国家级 danger / 自治区级 success / 市级 primary / 县级 info）、特长、自治区级徽章、浏览/收藏统计
- **等级区分**：通过 `getLevelType()` 根据等级返回 Element Plus tag type
- **收藏**：`useFavorite('inheritor')`
- **详情弹窗**：`InheritorDetailDialog`（展示传承谱系、技艺特色、代表案例）
- **骨架屏**：`SkeletonGridCard` + 空状态
- **侧边栏**：`PageSidebar` 展示传承人统计（总数/自治区级/市级/县级/收藏量/浏览量）、代表传承人、`UpdateLogCard` 更新日志
- **路由查询支持**：监听 `route.query.id`，支持通过 URL 参数直接打开详情弹窗

**使用的 composable：** `useFavorite`、`useUpdateLog`、`useDebounceFn`

---

## Qa.vue -- 问答社区

**路由：** `/qa` | **权限：** 公开 | **keepAlive: true**

侗医药知识问答平台：

- **搜索筛选**：问题关键词搜索 + 分类筛选（筛选项从 `/metadata/filters` 动态加载）
- **问答卡片**：问题标题（带问号图标）、答案预览（截断 60 字符）、分类标签、浏览/收藏统计、操作按钮（收藏、查看详情）
- **骨架屏**：`SkeletonListQa`（6 个占位卡片）
- **收藏**：`useFavorite('qa')`
- **详情弹窗**：`QuizDetailDialog`
- **侧边栏**：`PageSidebar` 展示问答统计（问题总数/分类数/浏览量/收藏量）、热门问题、`AiChatCard` AI 聊天入口、反馈入口卡片（跳转 `/feedback`）
- **keepAlive**：页面被缓存，切换离开后状态保留
- **路由查询支持**：监听 `route.query.id`，支持通过 URL 参数直接打开详情弹窗

---

## Resources.vue -- 学习资源

**路由：** `/resources` | **权限：** 公开

侗医药学习资料库，支持多种文件类型：

- **搜索筛选**：资源名称搜索 + 难度/类型筛选（筛选项从 `/metadata/filters` 动态加载）
- **资源卡片**：每项展示文件类型图标（视频/文档/图片，颜色区分）、标题、描述摘要、分类标签、文件类型/扩展名/大小、浏览/收藏/下载统计
- **复选框选择**：支持多选（用于批量操作）
- **批量操作栏**：选中资源后出现浮动操作工具栏，支持批量下载（POST `/resources/batch-download`，返回 zip 文件）
- **操作按钮**：收藏、预览、下载（下载需登录，未登录弹窗提示）
- **文档预览**：使用 `ResourceDetailDialog` 组件预览资源详情
- **骨架屏**：`SkeletonListResource`
- **分页**：服务端分页
- **侧边栏**：`PageSidebar` 展示资源统计（总数/视频/文档/图片/收藏量/浏览量/下载量/总大小）、热门资源、`UpdateLogCard` 更新日志
- **路由查询支持**：监听 `route.query.id`，支持通过 URL 参数直接打开详情弹窗

**使用的 composable：** `useFavorite`、`useUpdateLog`、`useDebounceFn`

**使用的 store：** `useUserStore`（判断登录状态）

---

## Interact.vue -- 文化互动专区

**路由：** `/interact` | **权限：** 公开

综合互动页面，包含三个 Tab：

1. **趣味答题**（QuizSection 组件）：
   - 难度选择（初级 10 题 / 中级 20 题 / 高级 30 题）
   - 每题限时（使用 `useCountdown` 倒计时）
   - 答题进度条 + 上一题/下一题导航
   - 时间到自动提交
   - 成绩展示 + 分享功能（复制或 Web Share API）
   - 登录用户记录保存到服务端
   - **使用的 composable：** `useQuiz`

2. **植物识别**（PlantGame 组件）：
   - 难度选择（影响选项数量：easy 3 个 / medium 4 个 / hard 5 个）
   - 展示植物图片，从选项中选名称
   - 连击加分机制（连续正确 bonus +2/题，上限 +10）
   - 限时游戏（使用 `useCountdown`，时间到自动结束）
   - 答题后 1-1.5 秒自动切换下一题
   - 登录用户成绩记录到服务端
   - **使用的 composable：** `usePlantGame`

3. **评论交流**（CommentSection 组件）：
   - 评论列表分页展示
   - 支持回复功能（嵌套评论）
   - 评论需要审核
   - **使用的 composable：** `useComments`（来自 `useInteraction`）

**侧边栏**（InteractSidebar）：展示答题/游戏统计数据，提交成绩后自动刷新排行榜

---

## About.vue -- 关于平台

**路由：** `/about` | **权限：** 公开

平台介绍页，包含六个主要区块：

1. **英雄区**：展示非遗徽章、标题"侗乡医药"、副标题、描述
2. **选题背景**：阐述侗乡医药的非遗地位、传承危机、平台建设目标
3. **平台特色**：4 个特色卡片（知识体系化、传承人风采、图文联动、互动传播），带渐变色图标，数据从服务端动态获取
4. **功能模块导航**：7 个可点击跳转的功能模块入口（知识库、传承人、药用图鉴、问答社区、学习资源、文化互动、数据可视化）
5. **平台数据**：6 项统计数据卡片（药用植物、经典药方、核心疗法、传承人、问答知识、学习资源），数据从服务端动态获取
6. **侗医文化**：4 个文化特色卡片（就地取材、内外兼治、口传心授、整体观念）
7. **合规与声明**：4 个声明卡片（隐私保护、医疗免责声明、版权声明、可持续运营）

**数据加载：** `onMounted` 中并行请求植物/传承人/问答/资源的总数统计

---

## Feedback.vue -- 意见反馈

**路由：** `/feedback` | **权限：** 公开

用户反馈页面：

- **左侧主区域**：
  - **反馈表单**：反馈类型（功能建议/问题反馈/其他，el-radio-button）、标题（max 100 字）、详细描述（textarea，max 500 字）、联系方式（选填）
  - **表单验证**：必填项校验 + 长度校验
  - **提交**：带 loading 状态，成功后重置表单并刷新统计
  - **我的反馈记录**：登录用户展示历史反馈列表，包含类型标签、状态标签（待处理/处理中/已解决）、标题、内容、管理员回复
- **右侧侧边栏**：
  - **反馈须知**：4 条提示信息
  - **反馈统计**：总反馈数、已处理、待处理

**使用的 store：** `useUserStore`（判断登录状态）

---

## Admin.vue -- 管理后台

**路由：** `/admin` | **权限：** 需登录 + 管理员角色

平台管理后台，左侧 `AdminSidebar` 导航 + 右侧内容区：

- **仪表盘**：`AdminDashboard` 展示各模块统计数据，点击可跳转到对应管理页
- **标准数据表**：知识/传承人/植物/问答/资源/题目，使用 `AdminDataTable` 组件
  - 服务端分页、排序
  - 行操作（查看、编辑、删除）
  - 资源表特有：文件类型/文件大小列
  - 题目表特有：正确答案列
- **用户管理**：独立的 `AdminDataTable`，特有封禁/解封操作
- **评论管理**：独立的 `AdminDataTable`，特有审核通过/拒绝操作
- **反馈管理**：独立的 `AdminDataTable`，特有处理操作 + 回复功能
- **日志管理**：使用 `VirtualList` 虚拟滚动组件展示操作日志
  - 全选/单选 + 批量删除 + 清空所有日志
  - 日志列：模块、类型、用户、操作、IP、耗时、状态、时间
  - 慢请求（>1000ms）红色高亮
- **表单弹窗**：PlantFormDialog、KnowledgeFormDialog、InheritorFormDialog、QaFormDialog、QuizFormDialog、ResourceFormDialog，均使用 `defineAsyncComponent` 异步加载
- **详情弹窗**：UserDetailDialog、FeedbackDetailDialog、LogDetailDialog、CommentDetailDialog 等，均使用 `defineAsyncComponent` 异步加载

**使用的 composable：** `useAdminData`（管理全部 CRUD 操作 + 分页）、`useAdminDialogs`（弹窗状态管理）、`useAdminActions`（删除确认、评论审核、反馈回复）

**使用的工具：** `adminUtils.js`（表格列配置 TABLE_CONFIGS、标签映射、格式化函数）、`media.js`（文件解析）

---

## PersonalCenter.vue -- 个人中心

**路由：** `/personal` | **权限：** 需登录

用户个人信息管理中心，左侧 `ProfileSection` + 右侧 Tab 内容区：

- **未登录状态**：显示空状态提示 + "立即登录"按钮
- **个人信息卡**（ProfileSection）：头像（用户名首字）、用户名、角色标签、学习统计摘要（总答题次数/平均得分/植物游戏次/收藏总数/浏览总数）、快捷操作入口
- **学习统计**（StatsDashboard）：答题/游戏/收藏/浏览数据汇总 + ECharts 得分趋势折线图
- **我的收藏**（FavoritesPanel）：按类型筛选（全部/药材/知识/传承人/资源/问答），分页展示，点击可跳转到详情
- **答题记录**（QuizHistoryPanel）：答题+植物游戏记录合并按时间排序，展示难度、得分、正确率、时间
- **浏览历史**（BrowseHistoryPanel）：分页展示浏览记录，点击跳转到对应详情
- **修改密码**（SettingsPanel）：基本信息展示 + 修改密码表单（当前密码/新密码/确认密码/验证码），修改成功后自动退出要求重新登录

**使用的 composable：** `usePersonalCenter`（收藏/记录/密码/退出）、`useStudyStats`（学习统计计算+图表）、`useBrowseHistory`（浏览历史加载）

---

## GlobalSearch.vue -- 全局搜索

**路由：** `/search` | **权限：** 公开

全平台统一搜索入口：

- **搜索框**：`el-autocomplete` 带搜索建议（从 `/search/suggest` 获取联想词）
- **搜索历史**：localStorage 存储最近搜索关键词（最多 10 条），可删除单条或清除全部
- **热门搜索**：展示热门关键词标签，点击直接搜索
- **搜索执行**：并行请求 5 个模块的搜索接口（knowledge/search、plants/search、inheritors/search、qa/search、resources/search）
- **搜索结果**：分 Tab 展示各类结果（全部/知识/植物/传承人/问答/资源），Tab 标签显示各类型结果数量
- **关键词高亮**：搜索结果中匹配关键词用 `<em class="highlight">` 标签高亮显示
- **无结果处理**：展示推荐关键词（从预设列表随机取 6 个）
- **分页**：全部结果客户端分页，分类结果服务端分页
- **URL 参数支持**：支持 `?q=keyword` 直接触发搜索

---

## Visual.vue -- 数据可视化

**路由：** `/visual` | **权限：** 公开

基于 ECharts 的数据可视化大屏：

- **统计卡片行**：5 个 `StatCard` 展示药用植物、知识条目、传承人、问答数据、学习资源的总数统计
- **药方使用频次**：`ChartCard` 支持柱状图/折线图切换（el-radio-group）
- **疗法分类占比**：`ChartCard` 饼图（环形图）
- **传承人等级分布**：`ChartCard` 柱状图（国家级/省级/市级/县级，各色渐变）
- **药用植物分类统计**：`ChartCard` 柱状图
- **药用植物地域分布**：`ChartCard` 柱状图 + 地区下拉筛选
- **药用植物分布占比**：`ChartCard` 饼图
- **药方/疗法热度排行**：`ChartCard` 多色柱状图
- **问答分类热度**：`ChartCard` 雷达图
- **刷新按钮**：手动刷新所有图表数据

**使用的 composable：** `useVisualData`（从 `/stats/chart` 获取图表数据）

**图表配置工具：** `utils/chartConfig.js` 提供 `createBarSeries`、`createMultiColorBarSeries`、`createPieSeries`、`createRadarSeries` 等工厂函数，以及 `baseTooltip`、`baseGrid`、`baseXAxis`、`baseYAxis` 基础配置常量和 `GRADIENT_COLORS` 渐变色预设

---

## NotFound.vue -- 404 页面

**路由：** `/:pathMatch(.*)*` | **权限：** 公开

简洁的 404 页面：

- **视觉**：大号"404"数字（150px，带文字阴影），标题"页面未找到"，描述文字
- **装饰**：3 个半透明装饰圆形（靛蓝、青绿、金色），营造轻松氛围
- **响应式**：移动端缩小字号
- **无头部/底部导航**：`App.vue` 中通过 `isNotFoundPage` computed 判断，隐藏 Header 和 Footer
