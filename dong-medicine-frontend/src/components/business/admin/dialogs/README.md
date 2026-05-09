# 管理详情弹窗（Admin Detail Dialogs）

## 什么是管理详情弹窗？

类比：**档案查看窗口**——就像在档案馆调阅一份档案，你只能看不能改。如果需要修改，要回到编辑表单去操作。但有些档案窗口也提供了快捷操作按钮（如封禁用户、审核评论、回复反馈）。

管理详情弹窗用于在后台管理页面**只读查看**数据的完整信息。和前台用户看到的详情弹窗不同，管理详情弹窗展示更多管理相关的字段（如创建时间、更新时间、状态等），部分弹窗还包含管理操作按钮。

```
前台详情弹窗（用户看到的）        管理详情弹窗（管理员看到的）
┌──────────────────┐          ┌──────────────────────┐
│ 钩藤              │          │ 植物详情（ID: 1）      │
│ 侗语名：gons jinl│          │ ID：1                 │
│ 功效：息风止痉    │          │ 中文名：钩藤           │
│ 主治：头痛眩晕    │          │ 侗语名：gons jinl 🔊  │
│                  │          │ 学名：Uncaria...      │
│  [⭐ 收藏] [关闭] │          │ 分类：木本             │
│                  │          │ 用法：内服             │
│                  │          │ 功效：息风止痉         │
│                  │          │ 创建时间：2025-03-15   │ ← 管理字段
│                  │          │                      │
│                  │          │ [植物图片] [相关视频]  │ ← 媒体展示
│                  │          │                      │
│                  │          │              [关闭]   │
└──────────────────┘          └──────────────────────┘
  有收藏/浏览历史               有管理字段和媒体展示
  多媒体标签页交互               el-descriptions 只读展示
```

---

## 详情弹窗列表

| 组件名 | 用途 | 数据 prop | 展示的关键信息 | 操作按钮 |
|--------|------|-----------|----------------|----------|
| `UserDetailDialog` | 查看用户详情 | `user` | ID、用户名、角色、状态、创建时间 | 封禁/解封 |
| `PlantDetailDialog` | 查看药用植物详情 | `plant` | ID、中文名、侗语名(含语音)、学名、分类、用法、生境、功效、故事、分布、创建时间、媒体资源 | 关闭 |
| `KnowledgeDetailDialog` | 查看知识条目详情 | `knowledge` | ID、标题、类型、疗法/疾病分类、热度、内容、步骤、创建时间、媒体资源 | 关闭 |
| `InheritorDetailDialog` | 查看传承人详情 | `inheritor` | ID、姓名、级别、经验年限、技艺特色、简介、代表案例、荣誉资质、创建/更新时间、传承生涯时间线、媒体资源 | 关闭 |
| `QaDetailDialog` | 查看问答详情 | `qa` | ID、分类、问题、答案、热度、创建时间 | 关闭 |
| `QuizDetailDialog` | 查看测验题目详情 | `quiz` | ID、分类、正确答案、题目、选项A-D、解析 | 关闭 |
| `ResourceDetailDialog` | 查看学习资源详情 | `resource` | ID、标题、分类、类型、文件大小、热度、下载次数、创建时间、描述、文件预览(含kkFileView) | 下载文件、关闭 |
| `CommentDetailDialog` | 查看评论详情 | `comment` | ID、用户名、目标类型、目标ID、内容、时间、状态 | 审核通过、删除 |
| `FeedbackDetailDialog` | 查看反馈详情 | `feedback` | ID、用户名、类型、标题、内容、联系方式、提交时间、状态、回复 | 回复并处理 |
| `LogDetailDialog` | 查看系统日志详情 | `log` | ID、用户ID/名、模块、操作类型、状态、操作描述、请求方法/参数、IP、耗时、操作时间、错误信息 | 关闭 |

---

## 统一的详情弹窗模式

所有管理详情弹窗遵循相同的设计模式：

```
1. props 接收 visible 和具体数据对象
2. 使用 el-descriptions 组件只读展示字段
3. 状态字段使用 el-tag 组件高亮显示
4. 时间字段使用 formatTime 工具函数格式化
5. 部分弹窗底部提供管理操作按钮
6. 仅 emit 'update:visible' 和操作相关事件
```

### 通用 Props 接口

```typescript
interface AdminDetailDialogProps {
  visible: boolean       // 弹窗是否显示
  xxx: Object | null     // 具体数据对象(user/plant/knowledge/inheritor/qa/quiz/resource/comment/feedback/log)
}

interface AdminDetailDialogEmits {
  'update:visible': (val: boolean) => void  // 关闭弹窗
  // 部分组件额外 emit 操作事件
}
```

### 父组件中使用

```vue
<script setup>
import { ref } from 'vue'
import PlantDetailDialog from '@/components/business/admin/dialogs/PlantDetailDialog.vue'
import PlantFormDialog from '@/components/business/admin/forms/PlantFormDialog.vue'

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref(null)

// 表单弹窗
const formVisible = ref(false)
const editData = ref(null)

// 查看详情
const handleView = (row) => {
  detailData.value = row
  detailVisible.value = true
}

// 从详情弹窗跳转到编辑表单
const handleEditFromDetail = () => {
  detailVisible.value = false  // 关闭详情弹窗
  editData.value = detailData.value
  formVisible.value = true     // 打开编辑表单
}
</script>

<template>
  <AdminDataTable @view="handleView" @edit="handleEditFromDetail" />

  <!-- 详情弹窗 -->
  <PlantDetailDialog
    v-model:visible="detailVisible"
    :plant="detailData"
  />

  <!-- 表单弹窗 -->
  <PlantFormDialog
    v-model:visible="formVisible"
    :data="editData"
    @save="handleFormSave"
  />
</template>
```

---

## 各组件详细说明

### UserDetailDialog —— 用户详情

展示用户基本信息，提供封禁/解封操作。

```
┌──────────────────────────────────────┐
│  用户详情                             │
│                                      │
│  ID：        1                       │
│  用户名：    admin                   │
│  角色：      [管理员]                 │
│  状态：      [正常]                   │
│  创建时间：  2025-03-15 10:30:00     │
│                                      │
│  ⚠ 该用户已被封禁（封禁状态时显示）    │
│                                      │
│  [封禁用户]                [关闭]     │
└──────────────────────────────────────┘
```

**核心逻辑：**
- 封禁状态用户顶部显示红色警告（`el-alert type="error"`）
- 角色标签：admin 显示 warning 类型"管理员"，其他显示 info 类型"普通用户"
- 状态标签：banned 显示 danger 类型"已封禁"，其他显示 success 类型"正常"
- 封禁/解封操作前弹出 `ElMessageBox.confirm` 二次确认
- 管理员账户不显示封禁按钮（`user?.role !== 'admin'`）

**Emits：** `update:visible`、`ban`、`unban`

**依赖：** `@/utils/adminUtils`（formatTime）

---

### PlantDetailDialog —— 药用植物详情

展示药用植物的全部信息，包含侗语语音播放和媒体资源展示。

```
┌──────────────────────────────────────────┐
│  植物详情                                 │
│                                          │
│  ID：1          中文名：钩藤              │
│  侗语名：gons jinl 🔊  学名：Uncaria...  │
│  分类：木本      用法：内服               │
│  生长环境：山地                           │
│  功效：息风止痉、清热平肝                 │
│  药用故事：侗族老人说...                  │
│  地域分布：贵州、湖南、广西               │
│  创建时间：2025-03-15                     │
│                                          │
│  ── 植物图片 ──                           │
│  [MediaDisplay]                          │
│  ── 相关视频 ──                           │
│  [MediaDisplay]                          │
│  ── 相关文档 ──                           │
│  [MediaDisplay]                          │
│                              [关闭]      │
└──────────────────────────────────────────┘
```

**核心逻辑：**
- 侗语名旁集成 `HerbAudio` 语音播放组件
- 媒体资源使用 `MediaDisplay` 组件分别展示图片、视频、文档
- 仅当对应字段有值时才显示 MediaDisplay 区域

**Emits：** `update:visible`

**依赖：** `@/utils/adminUtils`（formatTime）、`MediaDisplay`、`HerbAudio`

---

### KnowledgeDetailDialog —— 知识条目详情

展示知识条目的完整信息，包含媒体资源展示。

```
┌──────────────────────────────────────────┐
│  知识详情                                 │
│                                          │
│  ID：1          标题：侗族药浴疗法        │
│  类型：药方      疗法分类：药浴疗法       │
│  疾病分类：风湿骨痛  热度：256            │
│  内容：侗族药浴是侗族人民世代相传的...    │
│  步骤：1.准备药材 2.加水煎煮 3.熏蒸患处   │
│  创建时间：2025-03-15                     │
│                                          │
│  ── 相关图片 ──                           │
│  [MediaDisplay]                          │
│  ── 演示视频 ──                           │
│  [MediaDisplay]                          │
│  ── 相关文档 ──                           │
│  [MediaDisplay]                          │
│                              [关闭]      │
└──────────────────────────────────────────┘
```

**Emits：** `update:visible`

**依赖：** `@/utils/adminUtils`（formatTime）、`MediaDisplay`

---

### InheritorDetailDialog —— 传承人详情

展示传承人的完整信息，包含级别标签、传承生涯时间线和媒体资源。

```
┌──────────────────────────────────────────┐
│  传承人详情                               │
│                                          │
│  ID：1          姓名：吴老医师            │
│  级别：[省级]    经验年限：45年           │
│  技艺特色：侗族药浴疗法、侗医推拿         │
│  个人简介：吴老医师自幼跟随...            │
│  代表案例：治愈风湿骨痛患者数百例...       │
│  荣誉资质：省级非遗传承人...              │
│  创建时间：2025-03-15                     │
│  更新时间：2025-04-20                     │
│                                          │
│  ── 传承生涯 ──                           │
│  ● 学习经历  1976                        │
│  │  早期跟随师长学习侗医药知识...         │
│  ● 开始行医  1981                        │
│  │  于1981年开始从事侗医药实践...         │
│  ● 技艺特色                               │
│  │  擅长：侗族药浴疗法、侗医推拿          │
│  ● 荣誉奖项                               │
│     省级非遗传承人                        │
│                                          │
│  ── 传承人照片 ──                         │
│  [MediaDisplay]                          │
│  ── 相关视频 ──                           │
│  [MediaDisplay]                          │
│  ── 资质文档 ──                           │
│  [MediaDisplay]                          │
│                              [关闭]      │
└──────────────────────────────────────────┘
```

**核心逻辑：**
- **时间线智能生成**：与前台 InheritorDetailDialog 相同的双策略算法
  - 策略1：`parseTimelineFromBio` —— 从简介中按正则匹配"学习经历/行医经历/荣誉奖项/传承贡献"段落
  - 策略2：`generateGenericTimeline` —— 根据经验年限、擅长领域、荣誉等自动生成
- **级别标签映射**：使用 `getLevelTagType` 工具函数（来自 adminUtils）
- **时间线样式**：使用 `admin-timeline` 样式，与前台 `milestone-timeline` 有所不同

**Emits：** `update:visible`

**依赖：** `@/utils/adminUtils`（formatTime、getLevelTagType）、`MediaDisplay`

---

### QaDetailDialog —— 问答详情

展示问答条目的问题与答案。

```
┌──────────────────────────────────────┐
│  问答详情                             │
│                                      │
│  ID：1          分类：侗药常识         │
│  问题：侗族药浴有哪些功效？           │
│  答案：侗族药浴具有祛风除湿、活血化瘀  │
│  热度：128                           │
│  创建时间：2025-03-15                 │
│                              [关闭]  │
└──────────────────────────────────────┘
```

**Emits：** `update:visible`

**依赖：** `@/utils/adminUtils`（formatTime）

---

### QuizDetailDialog —— 测验题目详情

展示测验题目的题目、选项和答案。

```
┌──────────────────────────────────────┐
│  题目详情                             │
│                                      │
│  ID：1          分类：侗药常识         │
│  正确答案：A                          │
│  题目：以下哪种是侗族常用药材？       │
│  选项A：钩藤                          │
│  选项B：银杏                          │
│  选项C：人参                          │
│  选项D：灵芝                          │
│  解析：钩藤是侗族地区最常见的...       │
│                              [关闭]  │
└──────────────────────────────────────┘
```

**核心逻辑：**
- 兼容两种选项字段名：`options` 和 `optionList`
- 兼容两种答案字段名：`correctAnswer` 和 `answer`
- 无选项时显示 `-`

**Emits：** `update:visible`

**依赖：** 无（最简单的详情弹窗）

---

### ResourceDetailDialog —— 学习资源详情

展示学习资源的完整信息，包含文件预览（支持 kkFileView 在线预览）和下载功能。

```
┌──────────────────────────────────────────────┐
│  资源详情                                     │
│                                              │
│  ID：1          标题：侗族药浴入门教程        │
│  分类：入门      类型：[视频]                 │
│  文件大小：156.3 MB  热度：89                 │
│  下载次数：34       创建时间：2025-03-15      │
│  描述：本教程详细介绍了侗族药浴的基本方法...  │
│                                              │
│  ── 资源预览 ──                               │
│  ┌──────────────────────────────────────┐    │
│  │  视频播放器 / 图片预览 / kkFileView   │    │
│  │  (PDF/Word/Excel/PPT 在线预览)       │    │
│  └──────────────────────────────────────┘    │
│                                              │
│  [⬇ 下载文件]                      [关闭]    │
└──────────────────────────────────────────────┘
```

**核心逻辑：**
- **文件列表解析**：使用 `parseMediaList` 解析 `resource.files` 字段，通过 `normalizeUrl` 标准化URL
- **文件类型检测**：根据扩展名判断文件类型，决定预览方式
- **kkFileView 在线预览**：支持 PDF、Word、Excel、PPT 文件的在线预览
  - 预览URL生成：`/kkfileview/onlinePreview?url={base64编码的文件URL}`
  - 文件URL支持通过环境变量 `VITE_KKFILEVIEW_FILE_HOST` 配置文件服务器地址
  - 预览加载中显示 loading 动画
- **视频预览**：使用原生 `<video>` 标签直接播放
- **图片预览**：使用 `el-image` 组件，支持点击放大
- **不支持预览的文件**：显示文件图标、名称、大小和下载按钮
- **下载功能**：创建临时 `<a>` 标签触发下载

**环境变量：**

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_KKFILEVIEW_URL` | kkFileView 服务地址 | `/kkfileview` |
| `VITE_KKFILEVIEW_FILE_HOST` | 文件服务器地址（用于kkFileView访问） | 空字符串（使用当前域名） |

**Emits：** `update:visible`

**依赖：** `@/utils/adminUtils`（formatTime、formatFileSize、getFileTypeTagType、getFileTypeText）、`@/utils`（getFileName、getFileIcon、getFileColor）、`@/utils/media`（parseMediaList、normalizeUrl）

---

### CommentDetailDialog —— 评论详情

展示评论信息，提供审核和删除操作。

```
┌──────────────────────────────────────┐
│  评论详情                             │
│                                      │
│  ID：1          用户：张三             │
│  目标类型：plant  目标ID：5           │
│  评论内容：这个药材介绍很详细！        │
│  时间：2025-03-15 10:30:00           │
│  状态：[待审核]                       │
│                                      │
│  [审核通过]  [删除]          [关闭]   │
└──────────────────────────────────────┘
```

**核心逻辑：**
- 状态标签：approved 显示 success"已审核"，pending 显示 warning"待审核"，其他显示 danger"已拒绝"
- 审核通过按钮仅在状态非 approved 时显示
- 审核和删除操作后自动关闭弹窗

**Emits：** `update:visible`、`approve`、`delete`

**依赖：** `@/utils/adminUtils`（formatTime）

---

### FeedbackDetailDialog —— 反馈详情

展示反馈信息，提供回复并处理功能。

```
┌──────────────────────────────────────┐
│  反馈详情                             │
│                                      │
│  ID：1          用户：张三             │
│  类型：建议      标题：增加搜索功能    │
│  内容：希望能增加按功效搜索的功能...   │
│  联系方式：zhangsan@email.com         │
│  提交时间：2025-03-15                 │
│  状态：[待处理]                       │
│  回复：感谢您的建议，我们已规划...     │
│                                      │
│  [输入回复内容...]                    │
│                                      │
│                      [回复并处理] [关闭]│
└──────────────────────────────────────┘
```

**核心逻辑：**
- 状态标签使用 `getFeedbackStatusTag` 和 `getFeedbackStatusText` 工具函数
- 已有回复时显示回复内容
- 未处理的反馈显示回复输入框（`el-input textarea`）
- "回复并处理"按钮在回复内容为空时禁用
- 打开弹窗时自动清空回复输入框

**Emits：** `update:visible`、`reply`

**依赖：** `@/utils/adminUtils`（formatTime、getFeedbackStatusTag、getFeedbackStatusText）

---

### LogDetailDialog —— 系统日志详情

展示系统操作日志的完整信息，包含请求参数和错误信息。

```
┌──────────────────────────────────────────────┐
│  日志详情                                     │
│                                              │
│  ID：1          用户ID：5      用户名：admin   │
│  模块：[PLANT]   操作类型：[CREATE]            │
│  状态：[成功]                                  │
│  操作描述：新增药用植物钩藤                    │
│  请求方法：POST                               │
│  请求参数：                                   │
│  ┌──────────────────────────────────────┐    │
│  │ { "nameCn": "钩藤", "category": "木本" } │    │
│  └──────────────────────────────────────┘    │
│  IP地址：192.168.1.100  执行耗时：45ms        │
│  操作时间：2025-03-15 10:30:00               │
│  错误信息：                                   │
│  ┌──────────────────────────────────────┐    │
│  │ NullPointerException: ...             │    │
│  └──────────────────────────────────────┘    │
│                                    [关闭]    │
└──────────────────────────────────────────────┘
```

**核心逻辑：**
- **模块标签映射**：`MODULE_TYPES = { USER: 'primary', PLANT: 'success', KNOWLEDGE: 'warning', INHERITOR: 'info', RESOURCE: 'danger', ... }`
- **操作类型标签映射**：`TYPE_TYPES = { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', QUERY: '' }`
- **状态标签**：success 为绿色"成功"，否则为红色"失败"
- **请求参数格式化**：`formatParams` 尝试 JSON.parse 后 JSON.stringify 美化输出，解析失败则原样显示
- **错误信息**：仅在 `log.errorMsg` 有值时显示，使用红色背景样式
- **参数和错误区域**：使用等宽字体、可滚动容器，最大高度200px

**Emits：** `update:visible`

**依赖：** 无外部依赖，所有工具函数内联定义

---

## 共享工具函数

大部分详情弹窗使用 `@/utils/adminUtils` 提供的公共工具函数：

```javascript
import { formatTime, formatFileSize, getFileTypeTagType, getFileTypeText,
         getLevelTagType, getFeedbackStatusTag, getFeedbackStatusText } from '@/utils/adminUtils'
```

| 函数 | 用途 | 使用的组件 |
|------|------|-----------|
| `formatTime` | 时间格式化（ISO字符串 → 本地时间） | UserDetailDialog, PlantDetailDialog, KnowledgeDetailDialog, InheritorDetailDialog, QaDetailDialog, ResourceDetailDialog, CommentDetailDialog, FeedbackDetailDialog |
| `formatFileSize` | 文件大小格式化（字节 → B/KB/MB/GB） | ResourceDetailDialog |
| `getFileTypeTagType` | 文件类型 → Tag颜色 | ResourceDetailDialog |
| `getFileTypeText` | 文件类型 → 中文名 | ResourceDetailDialog |
| `getLevelTagType` | 传承人级别 → Tag颜色 | InheritorDetailDialog |
| `getFeedbackStatusTag` | 反馈状态 → Tag颜色 | FeedbackDetailDialog |
| `getFeedbackStatusText` | 反馈状态 → 中文名 | FeedbackDetailDialog |

> 注意：LogDetailDialog 和 QuizDetailDialog 未使用 adminUtils，相关工具函数内联定义在组件中

---

## 详情弹窗 vs 表单弹窗

```
详情弹窗（dialogs/）              表单弹窗（forms/）
┌──────────────────┐          ┌──────────────────┐
│ 只读查看          │          │ 可编辑            │
│                  │          │                  │
│ 名称：钩藤        │          │ 名称：[钩藤    ]  │
│ 功效：息风止痉    │          │ 功效：[息风止痉 ] │
│ 状态：[已审核]   │          │ 状态：[已审核 ▼]  │
│                  │          │                  │
│ [审核] [删除]    │          │ [取消] [保存]     │
│ [关闭]           │          │                  │
└──────────────────┘          └──────────────────┘
  用 el-descriptions 展示       用 el-form 编辑
  主要只读，部分有操作按钮       可以修改数据
  emit 操作事件(ban/approve等)  emit save 事件
```

**典型工作流：** 列表 → 点击"查看" → 详情弹窗 → (可选)点击操作按钮 → 刷新列表

---

## 常见错误

### 错误1：在详情弹窗里直接编辑数据

```vue
<template>
  <!-- ❌ 详情弹窗应该是只读的，不应该有输入框 -->
  <el-descriptions-item label="名称">
    <el-input v-model="data.name" />  <!-- 不应该在详情弹窗里放输入框！ -->
  </el-descriptions-item>

  <!-- ✅ 详情弹窗只展示文本，编辑功能交给表单弹窗 -->
  <el-descriptions-item label="名称">
    {{ data.name }}  <!-- 只读展示 -->
  </el-descriptions-item>
</template>
```

> 例外：FeedbackDetailDialog 中的回复输入框是合理的，因为"回复并处理"是详情查看的延伸操作，不是编辑反馈本身。

### 错误2：删除/审核后没有关闭弹窗

```vue
<script setup>
// ❌ 操作后弹窗还开着，显示已变更的数据
const handleApprove = (comment) => {
  emit('approve', comment)
  // 缺少关闭弹窗的代码！
}

// ✅ 操作后关闭弹窗
const handleApprove = () => {
  emit('approve', props.comment)
  emit('update:visible', false)  // 关闭弹窗
}
</script>
```

### 错误3：状态字段没有用 Tag 组件高亮

```vue
<template>
  <!-- ❌ 状态用纯文本显示，不够醒目 -->
  <el-descriptions-item label="状态">
    {{ data.status === 'banned' ? '已封禁' : '正常' }}
  </el-descriptions-item>

  <!-- ✅ 用 Tag 组件，不同状态用不同颜色，一目了然 -->
  <el-descriptions-item label="状态">
    <el-tag :type="data.status === 'banned' ? 'danger' : 'success'">
      {{ data.status === 'banned' ? '已封禁' : '正常' }}
    </el-tag>
  </el-descriptions-item>
</template>
```

---

## 代码审查与改进建议

- [一致性] LogDetailDialog 内联定义了 formatTime、getModuleTagType、getTypeTagType 等函数，未使用 adminUtils，与其他组件风格不一致
- [一致性] QuizDetailDialog 是唯一没有任何工具函数导入的组件，也没有 formatTime，缺少创建时间展示
- [复用] InheritorDetailDialog 的时间线生成逻辑（parseTimelineFromBio/generateGenericTimeline）与前台 InheritorDetailDialog 完全重复，应抽取为 composable
- [安全] ResourceDetailDialog 的 kkFileView URL 生成使用 btoa 编码，未做 URL 白名单校验，存在潜在的 SSRF 风险
- [体验] 大部分详情弹窗只有"关闭"按钮，没有"编辑"快捷入口，用户需要关闭弹窗后再在表格中找到对应行点击编辑
