# 管理组件（Admin Components）

## 什么是管理组件？

类比：**后台管理工具箱**——前台是给普通用户看的展示厅，后台是管理员专用的工具间。工具间里有仪表盘（看数据总览）、数据表格（管理所有数据）、侧边栏（切换管理功能）。

管理组件只在后台管理页面使用，普通用户看不到。它们负责数据的增删改查（CRUD）和系统管理。

```
前台（用户看到的）              后台（管理员看到的）
┌──────────────────┐          ┌──────────────────────────┐
│  药用植物展示     │          │  数据仪表盘               │
│  知识问答         │          │  数据管理表格              │
│  传承人介绍       │          │  信息录入表单              │
│  学习资源         │          │  详情查看弹窗              │
└──────────────────┘          └──────────────────────────┘
   普通用户浏览                    管理员操作
```

---

## 组件列表

### AdminDashboard —— 管理仪表盘

**用途：** 后台首页，展示平台数据总览、数据趋势图表、最新反馈和快捷操作

```
┌──────────────────────────────────────────────────────────────┐
│  平台数据总览（10个统计卡片）                                   │
│                                                              │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐              │
│  │ 128  │ │  45  │ │  23  │ │ 892  │ │  56  │              │
│  │用户总数│ │知识条目│ │传承人 │ │药用植物│ │问答数据│              │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘              │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐              │
│  │  34  │ │  12  │ │  89  │ │  67  │ │  23  │              │
│  │资源文件│ │答题题目│ │评论数量│ │系统日志│ │反馈总数│              │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 数据趋势                                              │   │
│  │ [平台访问趋势] [用户增长] [内容浏览] [搜索热词]         │   │
│  │ ┌──────────────────────────────────────────────────┐ │   │
│  │ │  ECharts 图表区域（按需加载）                      │ │   │
│  │ └──────────────────────────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────┐ ┌────────────────────┐     │
│  │ 最新反馈（分页列表）          │ │ 快捷操作            │     │
│  │ 👤 张三  反馈内容...  [待处理]│ │ [用户管理]          │     │
│  │ 👤 李四  反馈内容...  [处理中]│ │ [答题管理]          │     │
│  │ 👤 王五  反馈内容...  [已处理]│ │ [评论管理]          │     │
│  │            ← 1 2 3 →        │ │ [日志管理]          │     │
│  └─────────────────────────────┘ │ [管理知识]          │     │
│                                  │ [管理传承人]        │     │
│                                  │ [管理植物]          │     │
│                                  │ [管理问答]          │     │
│                                  │ [管理资源]          │     │
│                                  │ [反馈管理]          │     │
│                                  └────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

**Props 接口：**

| Prop | 类型 | 说明 |
|------|------|------|
| `stats` | Object | 后端返回的统计数据对象，优先取其字段值 |
| `users` | Array | 用户列表（stats 无值时用 length 兜底） |
| `knowledge` | Array | 知识条目列表 |
| `inheritors` | Array | 传承人列表 |
| `plants` | Array | 药用植物列表 |
| `qa` | Array | 问答列表 |
| `resources` | Array | 资源列表 |
| `feedback` | Array | 反馈列表 |
| `quiz` | Array | 测验题目列表 |
| `comments` | Array | 评论列表 |

**Emits：** `view-feedback`（查看全部反馈）、`navigate`（快捷操作导航，参数为菜单名）

**核心逻辑：**
- **统计卡片**：10个指标卡片，数据来源优先取 `stats[key]`，否则用对应数组的 `length` 兜底
- **ECharts 图表按需加载**：4个图表标签页（平台访问趋势、用户增长、内容浏览、搜索热词），仅在标签首次激活时加载对应图表，避免一次性渲染所有图表
- **图表实例管理**：所有 ECharts 实例存入 `chartInstances` 数组，组件卸载时统一 `dispose` 释放内存
- **反馈列表**：按时间倒序排列，支持分页（`el-pagination`，可选5/10/20条/页）
- **日志统计**：额外调用 `/admin/logs/stats` 接口获取日志数量，权限不足时使用 `logPermissionWarn` 记录警告

**依赖：** `echarts/core`（按需引入 LineChart、BarChart 等）、`@/utils/logger`、`@/utils/request`

```vue
<!-- 使用示例 -->
<AdminDashboard
  :stats="platformStats"
  :users="users"
  :knowledge="knowledgeList"
  :inheritors="inheritorList"
  :plants="plantList"
  :qa="qaList"
  :resources="resourceList"
  :feedback="feedbackList"
  :quiz="quizList"
  :comments="commentList"
  @view-feedback="handleViewFeedback"
  @navigate="handleNavigate"
/>
```

---

### AdminDataTable —— 管理数据表格

**用途：** 以表格形式展示和管理数据，支持搜索、分页、CSV导出、自定义列和操作

```
┌──────────────────────────────────────────────────────────────┐
│  🔍 [搜索...]                      [⬇ 导出CSV] [+ 添加XXX]  │
│                                                              │
│  ID │ 名称        │ 分类   │ 状态     │ 操作                  │
│  ───┼────────────┼───────┼─────────┼──────────────          │
│  1  │ 钩藤        │ 木本   │ 已审核   │ 查看 编辑 删除         │
│  2  │ 透骨草      │ 草本   │ 待审核   │ 查看 编辑 删除         │
│  3  │ 九节茶      │ 草本   │ 已拒绝   │ 查看 编辑 删除         │
│                                                              │
│  ← 上一页  1  2  3  4  5  下一页 →   共 128 条               │
└──────────────────────────────────────────────────────────────┘
```

**Props 接口：**

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | String | `""` | 表格标题（用于"添加xxx"按钮文案） |
| `titleName` | String | `"名称"` | 标题列的表头名称 |
| `entity` | String | `""` | 实体名，用于CSV导出API路径（如 `"plants"`） |
| `data` | Array | `[]` | 表格数据 |
| `columns` | Array | `[]` | 自定义列配置 |
| `showAdd` | Boolean | `true` | 是否显示"添加"按钮 |
| `showEdit` | Boolean | `true` | 是否显示"编辑"按钮 |
| `showTitle` | Boolean | `true` | 是否显示标题列 |
| `actionWidth` | String | `"200"` | 操作列宽度 |
| `serverPagination` | Boolean | `false` | 是否使用服务端分页 |
| `serverTotal` | Number | `0` | 服务端分页总数 |
| `page` | Number | `1` | 当前页码（服务端分页时由父组件控制） |
| `pageSize` | Number | `12` | 每页条数 |

**Emits：** `add`、`edit`、`delete`、`view`、`selection-change`、`page-change`、`size-change`

**核心逻辑：**
- **前端搜索过滤**：`filteredData` 根据 `searchKey` 在 title/nameCn/name/question/content 字段中模糊匹配
- **双模式分页**：
  - 前端分页（默认）：`paginatedData` 对 `filteredData` 进行切片分页
  - 服务端分页：`serverPagination=true` 时，分页参数由父组件通过 props 传入，通过 emit 通知翻页
- **CSV导出**：调用 `/admin/export/{entity}?format=csv` 接口，以 Blob 方式下载，文件名包含日期时间戳
- **状态标签映射**：内置 `TAG_TYPES` 和 `STATUS_TEXTS` 映射表，支持难度等级、审核状态、用户角色等常见状态的颜色和文本转换
- **自定义列插槽**：`columns` 中可配置 `slotName` 字段，父组件通过 `<template #slotName="{ row }">` 自定义列渲染
- **操作列插槽**：默认提供查看/编辑/删除按钮，可通过 `<template #actions="{ row }">` 完全自定义

**列配置示例：**

```javascript
const columns = [
  { prop: 'category', label: '分类', width: '100' },
  { prop: 'status', label: '状态', type: 'tag', width: '100' },
  { prop: 'efficacy', label: '功效', minWidth: '200' },
  { prop: 'customField', label: '自定义', slotName: 'customSlot' }
]
```

**依赖：** `@/components/business/display/Pagination.vue`、`@/utils/request`

```vue
<!-- 使用示例 -->
<AdminDataTable
  title="药用植物"
  entity="plants"
  :columns="tableColumns"
  :data="tableData"
  :show-add="true"
  :show-edit="true"
  @add="handleAdd"
  @edit="handleEdit"
  @delete="handleDelete"
  @view="handleView"
>
  <template #customSlot="{ row }">
    <el-tag>{{ row.customField }}</el-tag>
  </template>
</AdminDataTable>
```

---

### AdminSidebar —— 管理侧边栏

**用途：** 后台管理页面的导航菜单，固定在页面左侧，包含功能菜单、用户信息和退出按钮

```
┌──────────────┬─────────────────────────────────────┐
│ ⚙ 管理后台    │                                     │
│ Admin Panel  │                                     │
│ ──────────── │                                     │
│              │                                     │
│ 📊 数据概览   │         管理内容区域                  │
│ 👤 用户管理   │                                     │
│ 📄 知识管理   │                                     │
│ 🧑 传承人管理 │                                     │
│ 🖼 植物管理   │                                     │
│ 💬 问答管理   │                                     │
│ 📁 资源管理   │                                     │
│ ✏️ 答题管理   │                                     │
│ 💭 评论管理   │                                     │
│ 📝 反馈管理   │                                     │
│ 🎫 日志管理   │                                     │
│ ──────────── │                                     │
│ 👤 管理员     │                                     │
│   管理员      │                                     │
│ [退出]       │                                     │
└──────────────┴─────────────────────────────────────┘
```

**Props 接口：**

| Prop | 类型 | 说明 |
|------|------|------|
| `activeMenu` | String | 当前激活的菜单项索引 |
| `userName` | String | 管理员用户名（显示在底部） |
| `logoutLoading` | Boolean | 退出按钮加载状态 |

**Emits：** `update:active-menu`（菜单切换）、`logout`（退出登录）

**核心逻辑：**
- **固定定位**：`position: fixed; left: 0; top: 0; bottom: 0; width: 240px`，始终固定在页面左侧
- **渐变背景**：`linear-gradient(180deg, var(--dong-blue), var(--dong-indigo-dark))`，从侗蓝到靛蓝的渐变
- **11个菜单项**：数据概览、用户管理、知识管理、传承人管理、植物管理、问答管理、资源管理、答题管理、评论管理、反馈管理、日志管理
- **用户信息区**：底部显示管理员头像（首字母）和角色标签
- **退出按钮**：带 loading 状态，防止重复点击

**菜单索引映射：**

| 索引 | 菜单项 | 图标 |
|------|--------|------|
| `dashboard` | 数据概览 | DataLine |
| `users` | 用户管理 | User |
| `knowledge` | 知识管理 | Document |
| `inheritors` | 传承人管理 | Avatar |
| `plants` | 植物管理 | Picture |
| `qa` | 问答管理 | ChatDotRound |
| `resources` | 资源管理 | Folder |
| `quiz` | 答题管理 | EditPen |
| `comments` | 评论管理 | ChatDotRound |
| `feedback` | 反馈管理 | ChatLineSquare |
| `logs` | 日志管理 | Tickets |

```vue
<!-- 使用示例 -->
<AdminSidebar
  :active-menu="currentMenu"
  :user-name="adminName"
  :logout-loading="logoutLoading"
  @update:active-menu="handleMenuChange"
  @logout="handleLogout"
/>
```

---

## 子目录结构

```
admin/
├── AdminDashboard.vue      仪表盘（数据总览 + ECharts图表 + 反馈列表 + 快捷操作）
├── AdminDataTable.vue      数据表格（搜索 + 分页 + CSV导出 + 自定义列）
├── AdminSidebar.vue        侧边栏（11项导航菜单 + 用户信息 + 退出）
├── forms/                  表单弹窗（新增/编辑数据）
│   ├── PlantFormDialog.vue
│   ├── KnowledgeFormDialog.vue
│   ├── InheritorFormDialog.vue
│   ├── ResourceFormDialog.vue
│   ├── QaFormDialog.vue
│   └── QuizFormDialog.vue
└── dialogs/                详情弹窗（查看数据详情）
    ├── UserDetailDialog.vue
    ├── PlantDetailDialog.vue
    ├── KnowledgeDetailDialog.vue
    ├── InheritorDetailDialog.vue
    ├── QaDetailDialog.vue
    ├── QuizDetailDialog.vue
    ├── ResourceDetailDialog.vue
    ├── CommentDetailDialog.vue
    ├── FeedbackDetailDialog.vue
    └── LogDetailDialog.vue
```

| 子目录 | 用途 | 类比 |
|--------|------|------|
| `forms/` | 新增和编辑数据的表单弹窗 | 信息录入表，填写后提交 |
| `dialogs/` | 查看数据详情的只读弹窗（部分含操作按钮） | 档案查看窗口，主要只读展示 |

---

## forms/ 和 dialogs/ 的区别

```
forms/（表单弹窗）              dialogs/（详情弹窗）
┌──────────────────┐          ┌──────────────────┐
│ 编辑药用植物      │          │ 药用植物详情       │
│                  │          │                  │
│ 名称：[钩藤    ] │ ← 可编辑  │ 名称：钩藤        │ ← 只读
│ 侗语名：[gons  ] │          │ 侗语名：gons jinl │
│ 功效：[息风止痉 ] │          │ 功效：息风止痉     │
│                  │          │                  │
│ [取消] [保存]     │          │ [关闭]            │
└──────────────────┘          └──────────────────┘
  用 el-form 编辑                用 el-descriptions 展示
  可以修改数据                    只能看不能改（部分有操作按钮）
```

---

## 常见错误

### 错误1：管理页面没有权限控制

```vue
<script setup>
// ❌ 任何人都能访问管理页面，普通用户也能删除数据！

// ✅ 在路由守卫中检查用户角色
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
if (userStore.role !== 'admin') {
  // 不是管理员，跳转到首页
  router.push('/')
  ElMessage.error('无权访问管理页面')
}
</script>
```

### 错误2：删除操作没有二次确认

```vue
<script setup>
// ❌ 点一下就删除了，手滑误点无法挽回
const handleDelete = (id) => {
  api.deletePlant(id)
}

// ✅ 弹出确认框，用户确认后才删除
import { ElMessageBox } from 'element-plus'

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条数据吗？删除后不可恢复！',
      '警告',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    await api.deletePlant(id)
    ElMessage.success('删除成功')
  } catch {
    // 用户点击了"取消"，什么都不做
  }
}
</script>
```

### 错误3：表格数据修改后没有刷新

```vue
<script setup>
// ❌ 编辑或删除数据后，表格还显示旧数据
const handleEdit = async (data) => {
  await api.updatePlant(data)
  // 缺少刷新表格的代码！
}

// ✅ 操作完成后重新加载表格数据
const handleEdit = async (data) => {
  await api.updatePlant(data)
  ElMessage.success('修改成功')
  loadTableData()  // 重新加载表格
}
</script>
```

---

## 代码审查与改进建议

- [组件设计] AdminDataTable.vue 搜索逻辑硬编码字段名(title/nameCn/name/question/content)，无法根据数据类型自定义，建议通过 prop 传入可搜索字段列表
- [组件设计] AdminDataTable.vue 状态映射(TAG_TYPES/STATUS_TEXTS)硬编码在通用组件中，违反组件通用性原则，建议通过 prop 传入或使用 provide/inject
- [组件设计] AdminDashboard.vue 的10个统计卡片数据来源混合使用 stats 对象和数组 length，逻辑不够清晰，建议统一数据来源
- [性能] AdminDashboard.vue 虽然已按需引入 echarts 模块，但图表配置对象(setOption)内联在方法中，可考虑抽取为配置文件
