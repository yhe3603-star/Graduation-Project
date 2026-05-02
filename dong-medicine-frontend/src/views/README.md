# 页面组件目录 (views/)

页面组件是直接对应一个 URL 路由的 Vue 组件，每个 `.vue` 文件代表一个完整的页面。它们组装各种子组件（来自 `components/`），通过 `composables/` 复用有状态逻辑，通过 `utils/` 调用工具函数。

## 全部 16 个页面

| 页面 | 路由 | 权限 | 功能说明 |
|------|------|------|---------|
| `Home.vue` | `/` | 公开 | 平台首页，英雄区 + 快捷导航 + 每周精选 + 最新更新 |
| `Plants.vue` | `/plants` | 公开 | 药用资源图鉴，药材搜索/筛选/收藏/对比 |
| `Knowledge.vue` | `/knowledge` | 公开 | 非遗医药知识库，药方疗法搜索/分类/收藏 |
| `Inheritors.vue` | `/inheritors` | 公开 | 传承人风采，谱系展示/技艺介绍/等级标识 |
| `Qa.vue` | `/qa` | 公开 (keepAlive) | 问答社区，问题搜索/浏览/收藏 |
| `Resources.vue` | `/resources` | 公开 | 学习资源，文件预览/下载/收藏/批量选择 |
| `Interact.vue` | `/interact` | 公开 | 文化互动专区，趣味答题 + 植物识别游戏 + 评论 |
| `SolarTerms.vue` | `/solar-terms` | 公开 (keepAlive) | 二十四节气采药，季节切换/时令药材/养生提示 |
| `About.vue` | `/about` | 公开 | 平台介绍，选题背景/平台特色/功能模块导航 |
| `Feedback.vue` | `/feedback` | 公开 | 意见反馈表单（类型/标题/内容/联系方式） |
| `PlantCompare.vue` | `/compare` | 公开 (keepAlive) | 药材多维度对比表（属性行 x 药材列） |
| `Admin.vue` | `/admin` | 需登录 + 管理员 | 管理后台（仪表盘/用户/植物/知识/传承人/问答/资源/反馈/题目/评论） |
| `PersonalCenter.vue` | `/personal` | 需登录 | 个人中心（我的收藏/答题记录/评论/账号设置/密码修改） |
| `GlobalSearch.vue` | `/search` | 公开 | 全局搜索（自动补全/搜索历史/热门搜索/分类结果） |
| `Visual.vue` | `/visual` | 公开 | 数据可视化（统计卡片/柱状图/饼图/地域分布图） |
| `NotFound.vue` | `/:pathMatch(.*)*` | 公开 | 404 页面（带装饰圆圈动画） |

---

## Home.vue -- 首页

**路由：** `/` | **权限：** 公开

平台的门户页面，由多个区块组成：

- **英雄区（Hero Section）**：展示平台标题"非遗视角下侗乡医药数字展示平台"、副标题"保护 · 传承 · 活态传播"、统计数据卡片（植物数量、知识条目、传承人数量等，数据来自 `homeConfig.js`）、"立即探索"和"了解非遗"两个 CTA 按钮
- **快捷导航区**：7 个功能模块入口卡片（知识库、传承人、植物图鉴、问答、互动、节气、可视化），每个有独立图标和渐变色
- **每周精选区**：展示本周推荐的侗医药知识
- **最新更新区**：展示平台最近更新的内容条目

**使用的 composable：** 收藏功能、媒体显示、更新日志展示

---

## Plants.vue -- 药用资源图鉴

**路由：** `/plants` | **权限：** 公开

药材图鉴页面，展示黔东南道地药材和侗医传统药用植物：

- **搜索筛选**：使用 `SearchFilter` 组件，支持关键词搜索 + 分类/用法筛选
- **卡片网格**：使用 `CardGrid` 组件展示药材缩略图、名称、用法标签、栖息地、浏览/收藏计数
- **收藏功能**：使用 `useFavorite('plant')` composable，每张卡片上有星形收藏按钮
- **对比功能**：使用 `useCompare()` composable，可将药材加入对比列表（最多 3 项），通过 `compareList` 全局共享状态
- **骨架屏**：使用 `SkeletonGridImage` 在加载时显示 12 个占位卡片
- **分页**：使用 `Pagination` 组件实现服务端分页
- **详情弹窗**：点击卡片打开 `PlantDetailDialog` 查看详细信息

**使用的 composable：** `useFavorite`、`useCompare`、`useMedia`

---

## Knowledge.vue -- 非遗医药知识库

**路由：** `/knowledge` | **权限：** 公开

展示侗医药传统疗法和药方知识：

- **搜索筛选**：`SearchFilter` 组件，支持分类/疗法类别/疾病类别多维度筛选
- **列表卡片**：每个条目展示疗法类别标签、标题、内容摘要（截断 80 字符）、疾病分类、浏览量/收藏量
- **骨架屏**：`SkeletonGridCard`（12 个占位卡片）
- **收藏**：使用 `useFavorite('knowledge')`
- **详情弹窗**：`KnowledgeDetailDialog` 展示完整药方或疗法内容
- **分页**：服务端分页

---

## Inheritors.vue -- 传承人风采

**路由：** `/inheritors` | **权限：** 公开

展示侗医药非遗传承人信息：

- **搜索筛选**：按姓名/技艺搜索 + 等级筛选
- **网格卡片**：每张卡片展示头像首字、姓名、等级标签（带颜色区分：自治区级 success / 省级 warning / 市级 primary）、特长、自治区级徽章、浏览/收藏统计
- **等级区分**：通过 `getLevelType()` 根据等级返回 Element Plus tag type
- **收藏**：`useFavorite('inheritor')`
- **详情弹窗**：`InheritorDetailDialog`（展示传承谱系、技艺特色、代表案例）
- **骨架屏**：`SkeletonGridCard` + 空状态

---

## Qa.vue -- 问答社区

**路由：** `/qa` | **权限：** 公开 | **keepAlive: true**

侗医药知识问答平台：

- **搜索筛选**：问题关键词搜索 + 分类筛选
- **问答卡片**：问题标题（带问号图标）、答案预览（截断 60 字符）、分类标签、浏览/收藏统计、操作按钮（收藏、查看详情）
- **骨架屏**：`SkeletonListQa`（6 个占位卡片）
- **收藏**：`useFavorite('qa')`
- **详情弹窗**：`QaDetailDialog`
- **keepAlive**：页面被缓存，切换离开后状态保留

---

## Resources.vue -- 学习资源

**路由：** `/resources` | **权限：** 公开

侗医药学习资料库，支持多种文件类型：

- **搜索筛选**：资源名称搜索 + 分类筛选
- **资源卡片**：每项展示文件类型图标（视频/文档/图片）、标题、描述摘要、分类标签、文件类型/扩展名/大小、浏览/收藏/下载统计
- **复选框选择**：支持多选（用于批量操作）
- **批量操作栏**：选中资源后出现的操作工具栏
- **操作按钮**：收藏、预览、下载
- **文档预览**：使用 `DocumentPreview` 组件，支持 KKFileView 在线预览
- **骨架屏**：`SkeletonListResource`
- **分页**：服务端分页

**使用的 composable：** `useFavorite`、`useMedia`

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

3. **评论区**（CommentSection 组件）：
   - 评论列表分页展示
   - 支持回复功能（嵌套评论）
   - 评论需要审核
   - **使用的 composable：** `useComments`

**侧边栏**（InteractSidebar）：展示答题/游戏统计数据

---

## SolarTerms.vue -- 节气采药

**路由：** `/solar-terms` | **权限：** 公开 | **keepAlive: true**

展示二十四节气与传统侗医药采药时令：

- **季节切换**：春夏秋冬四个 Tab，带颜色主题（春绿/夏红/秋金/冬蓝）
- **节气卡片**：使用 `<transition-group>` 动画切换
  - 每张卡片包含：节气图标字符、中文名、拼音、日期范围、序号
  - 描述文字、时令药材标签（点击可跳转到植物图鉴搜索）
  - 侗医药习俗说明、养生提示（el-alert 展示）
- **keepAlive**：缓存页面状态

---

## About.vue -- 关于平台

**路由：** `/about` | **权限：** 公开

平台介绍页，包含三个主要区块：

1. **选题背景**：阐述侗乡医药的非遗地位、传承危机、平台建设目标
2. **平台特色**：4 个特色卡片（数字化归档、知识图谱、互动体验、节气养生），带渐变色图标
3. **功能模块导航**：可点击跳转到各功能页面

---

## Feedback.vue -- 意见反馈

**路由：** `/feedback` | **权限：** 公开

用户反馈表单：

- **表单字段**：反馈类型（功能建议/问题反馈/其他，el-radio-group）、标题（max 100 字）、详细描述（textarea，max 500 字）、联系方式（选填）
- **表单验证**：必填项校验
- **提交**：带 loading 状态，成功后重置表单

---

## PlantCompare.vue -- 药材对比

**路由：** `/compare` | **权限：** 公开 | **keepAlive: true**

多维度药材对比工具：

- **对比表格**：属性为行（名称、学名、侗语名、功效、用法、栖息地等）、药材为列
- **添加/移除**：通过搜索弹窗添加药材（最多 3 个），每个列有移除按钮
- **清空**：一键清空所有对比
- **图片**：每列显示药材缩略图
- **数据来源**：`useCompare()` composable 通过 localStorage 持久化对比列表
- **keepAlive**：缓存对比状态

---

## Admin.vue -- 管理后台

**路由：** `/admin` | **权限：** 需登录 + 管理员角色

平台管理后台，左侧 `AdminSidebar` 导航 + 右侧内容区：

- **仪表盘**：`AdminDashboard` 展示各模块统计数据，点击可跳转到对应管理页
- **标准数据表**：用户/植物/知识/传承人/问答/资源/反馈/题目/评论，使用 `AdminDataTable` 组件
  - 服务端分页、排序
  - 行操作（查看、编辑、删除）
  - 用户管理特有：封禁/解封操作
- **表单弹窗**：PlantFormDialog、KnowledgeFormDialog、InheritorFormDialog、QaFormDialog、QuizFormDialog、ResourceFormDialog，均使用 `useFormDialog` composable
  - 支持新增/编辑
  - 更新日志内嵌管理
  - 数组字段自动序列化/反序列化
- **详情弹窗**：UserDetailDialog、FeedbackDetailDialog、LogDetailDialog 等

**使用的 composable：** `useAdminData`（管理全部 CRUD 操作）

---

## PersonalCenter.vue -- 个人中心

**路由：** `/personal` | **权限：** 需登录

用户个人信息管理中心：

- **个人信息卡**：头像（用户名首字）、用户名、角色标签、学习统计摘要（总答题次数/平均得分/植物游戏次数/总分）
- **快捷操作**：我的收藏、答题记录、我的评论、账号设置（4 个可跳转卡片）
- **我的收藏**：按类型筛选（全部/药材/知识/传承人/资源/问答），分页展示，点击可跳转到详情
- **答题记录**：答题+植物游戏记录合并按时间排序，展示难度、得分、正确率、时间
- **我的评论**：分页展示历史评论
- **账号设置**：修改密码表单（需验证码），修改成功后自动退出要求重新登录
- **退出登录**：带确认弹窗

**使用的 composable：** `usePersonalCenter`

---

## GlobalSearch.vue -- 全局搜索

**路由：** `/search` | **权限：** 公开

全平台统一搜索入口：

- **搜索框**：`el-autocomplete` 带搜索建议（从服务端获取联想词）
- **搜索历史**：localStorage 存储最近搜索关键词，可删除单条或清除全部
- **热门搜索**：展示热门关键词标签，点击直接搜索
- **搜索结果**：分 Tab 展示各类结果（全部/植物/知识/传承人/问答/资源）
- **无结果处理**：展示搜索引导和热门关键词

---

## Visual.vue -- 数据可视化

**路由：** `/visual` | **权限：** 公开

基于 ECharts 的数据可视化大屏：

- **统计卡片行**：`StatCard` 展示植物、知识、传承人、问答、资源的总数统计
- **药方使用频次**：`ChartCard` 支持柱状图/折线图切换（el-radio-group）
- **疗法分类占比**：`ChartCard` 饼图
- **传承人等级分布**：`ChartCard` 柱状图
- **药用植物分类统计**：`ChartCard` 柱状图
- **地域分布**：`ChartCard` 柱状图 + 地区下拉筛选
- **刷新按钮**：手动刷新所有图表数据

**使用的 composable：** `useVisualData`（从 `/stats/chart` 获取图表数据）、`useInteraction`（统计数据计算）

**图表配置工具：** `utils/chartConfig.js` 提供 `createBarSeries`、`createPieSeries`、`createLineSeries`、`createRadarSeries` 等工厂函数

---

## NotFound.vue -- 404 页面

**路由：** `/:pathMatch(.*)*` | **权限：** 公开

简洁的 404 页面：

- **视觉**：大号"404"数字（150px，带文字阴影），标题"页面未找到"，描述文字
- **装饰**：3 个半透明装饰圆形（靛蓝、青绿、金色），营造轻松氛围
- **响应式**：移动端缩小字号
- **无头部/底部导航**：`App.vue` 中通过 `isNotFoundPage` computed 判断，隐藏 Header 和 Footer
