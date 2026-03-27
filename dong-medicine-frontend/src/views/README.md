# Views 页面组件目录

本目录包含项目的所有页面级组件，每个页面对应一个路由。

## 目录结构

```
views/
├── Home.vue              # 首页
├── Plants.vue            # 药用植物页面
├── Inheritors.vue        # 传承人页面
├── Knowledge.vue         # 知识库页面
├── Qa.vue                # 问答社区页面
├── Resources.vue         # 学习资源页面
├── Interact.vue          # 互动专区页面
├── Visual.vue            # 数据可视化页面
├── PersonalCenter.vue    # 个人中心页面
├── Admin.vue             # 管理后台页面
├── About.vue             # 关于页面
├── Feedback.vue          # 意见反馈页面
├── GlobalSearch.vue      # 全局搜索页面
└── NotFound.vue          # 404页面
```

## 页面说明

### Home.vue - 首页

**路由**: `/`

**功能**:
- 平台核心功能入口展示
- 统计数据概览（植物数、传承人数、知识条目数）
- 传承人风采轮播展示
- 快速导航卡片
- AI智能问答入口

**依赖组件**:
- `AiChatCard.vue` - AI对话卡片
- `CardGrid.vue` - 卡片网格
- `UpdateLogCard.vue` - 更新日志

---

### Plants.vue - 药用植物页面

**路由**: `/plants`

**功能**:
- 药用植物列表展示（卡片网格布局）
- 分类筛选（按药材分类）
- 用法方式筛选
- 关键词搜索
- 植物详情查看（对话框）
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `PlantDetailDialog.vue` - 植物详情对话框
- `Pagination.vue` - 分页

---

### Inheritors.vue - 传承人页面

**路由**: `/inheritors`

**功能**:
- 传承人列表展示
- 按级别筛选（国家级/省级/市级/县级）
- 关键词搜索
- 传承人详情查看
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `InheritorDetailDialog.vue` - 传承人详情对话框
- `Pagination.vue` - 分页

---

### Knowledge.vue - 知识库页面

**路由**: `/knowledge`

**功能**:
- 知识条目列表展示
- 分类筛选（疗法分类、疾病分类）
- 关键词搜索
- 知识详情查看
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `KnowledgeDetailDialog.vue` - 知识详情对话框
- `PageSidebar.vue` - 侧边栏

---

### Qa.vue - 问答社区页面

**路由**: `/qa`

**功能**:
- 常见问答列表
- 分类筛选
- 关键词搜索
- 问答详情查看

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `Pagination.vue` - 分页

---

### Resources.vue - 学习资源页面

**路由**: `/resources`

**功能**:
- 学习资源列表展示
- 分类筛选（视频/文档/图片）
- 文件类型筛选
- 资源预览（视频播放、文档预览）
- 资源下载
- 收藏功能

**依赖组件**:
- `CardGrid.vue` - 卡片网格
- `SearchFilter.vue` - 搜索过滤
- `ResourceDetailDialog.vue` - 资源详情对话框
- `VideoPlayer.vue` - 视频播放
- `DocumentPreview.vue` - 文档预览

---

### Interact.vue - 互动专区页面

**路由**: `/interact`

**功能**:
- 趣味答题模块
- 植物识别游戏
- 评论交流
- 排行榜展示

**依赖组件**:
- `QuizSection.vue` - 答题组件
- `PlantGame.vue` - 植物游戏
- `CommentSection.vue` - 评论组件
- `InteractSidebar.vue` - 互动侧边栏

---

### Visual.vue - 数据可视化页面

**路由**: `/visual`

**功能**:
- 药方频次统计图表
- 疗法分类统计
- 传承人分布地图
- 平台数据趋势

**依赖组件**:
- `ChartCard.vue` - 图表卡片（ECharts）

---

### PersonalCenter.vue - 个人中心页面

**路由**: `/personal` (需登录)

**功能**:
- 用户信息展示与修改
- 我的收藏管理
- 答题记录查看
- 评论历史查看
- 密码修改

**依赖组件**:
- `usePersonalCenter.js` - 个人中心逻辑

---

### Admin.vue - 管理后台页面

**路由**: `/admin` (需管理员权限)

**功能**:
- 数据统计仪表盘
- 用户管理（列表、封禁、角色分配）
- 内容管理（植物、知识、传承人、资源、问答）
- 评论审核
- 反馈处理
- 操作日志查看

**依赖组件**:
- `AdminDashboard.vue` - 仪表盘
- `AdminDataTable.vue` - 数据表格
- `AdminSidebar.vue` - 侧边栏
- `admin/dialogs/*` - 详情对话框
- `admin/forms/*` - 表单对话框

---

### About.vue - 关于页面

**路由**: `/about`

**功能**:
- 选题背景介绍
- 平台特色说明
- 功能模块介绍
- 侗医文化背景

---

### Feedback.vue - 意见反馈页面

**路由**: `/feedback`

**功能**:
- 反馈表单提交
- 反馈类型选择
- 反馈记录查看

---

### GlobalSearch.vue - 全局搜索页面

**路由**: `/search`

**功能**:
- 跨模块统一搜索
- 搜索结果分类展示
- 搜索历史记录

---

### NotFound.vue - 404页面

**路由**: `/:pathMatch(.*)*`

**功能**:
- 404错误提示
- 返回首页链接

## 开发规范

1. **命名规范**: 页面组件使用大驼峰命名法
2. **路由配置**: 在 `router/index.js` 中配置路由
3. **权限控制**: 通过路由 `meta` 字段配置权限要求
4. **状态管理**: 使用 Pinia 管理页面状态
5. **组合式函数**: 复杂逻辑抽取到 `composables/` 目录

## 页面与路由映射

| 页面组件 | 路由路径 | 权限要求 |
|---------|---------|---------|
| Home.vue | `/` | 无 |
| Plants.vue | `/plants` | 无 |
| Inheritors.vue | `/inheritors` | 无 |
| Knowledge.vue | `/knowledge` | 无 |
| Qa.vue | `/qa` | 无 |
| Resources.vue | `/resources` | 无 |
| Interact.vue | `/interact` | 无 |
| Visual.vue | `/visual` | 无 |
| PersonalCenter.vue | `/personal` | 需登录 |
| Admin.vue | `/admin` | 需登录 + 管理员 |
| About.vue | `/about` | 无 |
| Feedback.vue | `/feedback` | 无 |
| GlobalSearch.vue | `/search` | 无 |
| NotFound.vue | `/:pathMatch(.*)*` | 无 |

---

**最后更新时间**: 2026年3月27日
