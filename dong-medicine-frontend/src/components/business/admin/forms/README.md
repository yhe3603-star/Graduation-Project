# 管理表单弹窗（Admin Form Dialogs）

## 什么是管理表单弹窗？

类比：**信息录入表**——就像填写入职登记表，你需要在表格里填写各项信息，提交后信息就录入系统了。

管理表单弹窗用于**新增**和**编辑**数据。管理员点击"新增"或"编辑"按钮后，弹出一个带表单的对话框，填写完信息后提交保存。

```
点击"新增"或"编辑" → 弹出表单弹窗 → 填写信息 → 提交保存 → 刷新列表

┌──────────────────────────────────────┐
│  新增药用植物                         │
│                                      │
│  中文名：  [            ] *必填       │
│  侗语名：  [            ]            │
│  学名：    [            ]            │
│  分类：    [木本 ▼]                   │
│  用法：    [内服 ▼]                   │
│  功效：    [                  ]       │
│  图片：    [选择图片] 最多9张          │
│  视频：    [选择视频] 最多3个          │
│  文档：    [拖拽上传] 最多3个          │
│                                      │
│  ── 更新日志 ──                       │
│  [操作记录卡片] [+添加] [编辑] [删除]  │
│                                      │
│           [取消]  [保存]              │
└──────────────────────────────────────┘
```

---

## 表单弹窗列表

| 组件名 | 用途 | 表单字段 | 必填字段 | 有更新日志 |
|--------|------|----------|----------|-----------|
| `PlantFormDialog` | 新增/编辑药用植物 | nameCn, nameDong, scientificName, category, usageWay, habitat, efficacy, story, distribution, images, videos, documents | nameCn | 有 |
| `KnowledgeFormDialog` | 新增/编辑知识条目 | title, type, therapyCategory, diseaseCategory, herbCategory, content, steps, images, videoUrl, documents, relatedPlants | title, type | 有 |
| `InheritorFormDialog` | 新增/编辑传承人 | name, level, experienceYears, specialties, bio, images, videos, representativeCases, honors, documents | name, level | 有 |
| `ResourceFormDialog` | 新增/编辑学习资源 | title, category, files(图片/视频/文档), description | title, category, files | 有 |
| `QaFormDialog` | 新增/编辑问答 | question, category, answer | question | 无 |
| `QuizFormDialog` | 新增/编辑测验题目 | question, category, optionA, optionB, optionC, optionD, correctAnswer, explanation | question | 无 |

---

## 统一的表单模式

所有表单弹窗遵循相同的设计模式：

```
1. props 接收 visible（显示状态）和 data（编辑时的原始数据）
2. v-model:visible 控制弹窗显示
3. 通过 isEdit 计算属性判断新增/编辑模式（!!form.value.id）
4. 打开弹窗时：编辑模式填充数据，新增模式重置为默认值
5. 保存时手动验证必填字段，emit('save', formData) 提交数据
6. 通过 defineExpose 暴露 setSaving 方法供父组件控制保存状态
```

### 通用 Props 接口

```typescript
interface FormDialogProps {
  visible: boolean       // 弹窗是否显示
  data: Object | null    // 编辑时传入的原数据，新增时为 null
}

interface FormDialogEmits {
  'update:visible': (val: boolean) => void  // 关闭弹窗
  'save': (formData: Object) => void        // 提交表单数据
}
```

### 新增/编辑判断逻辑

```javascript
// 不使用 mode prop，而是通过数据中是否有 id 来判断
const isEdit = computed(() => !!form.value.id)

// 弹窗标题根据模式自动切换
:title="isEdit ? '编辑药用植物' : '新增药用植物'"
```

### 数据转换模式

表单弹窗在打开和保存时需要进行数据格式转换：

```
打开弹窗时（后端数据 → 表单数据）：
  JSON字符串 → 数组（用于上传组件的 v-model）
  例：images: parseToArray(props.data.images)   // "[{url:'...'}]" → [{url:'...'}]

保存时（表单数据 → 后端数据）：
  数组 → JSON字符串（用于提交给后端）
  例：images: parseToString(form.value.images)  // [{url:'...'}] → "[{url:'...'}]"
```

### 父组件中使用

```vue
<!-- AdminPlantPage.vue -->
<script setup>
import { ref } from 'vue'
import PlantFormDialog from '@/components/business/admin/forms/PlantFormDialog.vue'

const formVisible = ref(false)
const editData = ref(null)

// 点击新增按钮
const handleAdd = () => {
  editData.value = null
  formVisible.value = true
}

// 点击编辑按钮
const handleEdit = (row) => {
  editData.value = row
  formVisible.value = true
}

// 表单提交
const handleFormSave = async (formData) => {
  try {
    if (formData.id) {
      await request.put(`/admin/plants/${formData.id}`, formData)
    } else {
      await request.post('/admin/plants', formData)
    }
    ElMessage.success(formData.id ? '修改成功' : '新增成功')
    formVisible.value = false
    loadTableData()  // 刷新列表
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<template>
  <AdminDataTable @add="handleAdd" @edit="handleEdit" />

  <PlantFormDialog
    v-model:visible="formVisible"
    :data="editData"
    @save="handleFormSave"
  />
</template>
```

---

## 各组件详细说明

### PlantFormDialog —— 药用植物表单

```
┌──────────────────────────────────────────┐
│  新增药用植物                              │
│                                          │
│  中文名：  [钩藤        ]  侗语名：[gons  ]│
│  学名：    [Uncaria...  ]  分类：  [木本 ▼]│
│  用法：    [内服 ▼]       生境：  [山地  ]│
│  功效：    [息风止痉、清热平肝...        ] │
│  故事：    [侗族老人说...                ] │
│  分布地区：[贵州、湖南、广西...           ] │
│  植物图片：[ImageUploader] 最多9张/10MB   │
│  相关视频：[VideoUploader] 最多3个/100MB  │
│  相关文档：[DocumentUploader] 最多3个/50MB │
│  ── 更新日志 ──                           │
│  [UpdateLogCard] [+添加] [编辑] [删除]    │
│                        [取消]  [保存]     │
└──────────────────────────────────────────┘
```

**表单字段：**

| 字段 | 组件 | 说明 |
|------|------|------|
| nameCn | el-input | 中文名称（必填） |
| nameDong | el-input | 侗语名称 |
| scientificName | el-input | 拉丁学名 |
| category | el-select | 分类选项：木本/草本/其他 |
| usageWay | el-select | 用法选项：内服/外用/内外兼用（默认"内服"） |
| habitat | el-input | 生长环境 |
| efficacy | el-input(textarea) | 主要功效 |
| story | el-input(textarea) | 相关故事或传说 |
| distribution | el-input | 分布地区 |
| images | ImageUploader | 植物图片（最多9张，10MB/张） |
| videos | VideoUploader | 相关视频（最多3个，100MB/个） |
| documents | DocumentUploader | 相关文档（最多3个，50MB/个，支持拖拽） |

**依赖组件：** `ImageUploader`、`VideoUploader`、`DocumentUploader`、`UpdateLogCard`、`UpdateLogDialog`、`useUpdateLog`

---

### KnowledgeFormDialog —— 知识条目表单

```
┌──────────────────────────────────────────┐
│  新增知识条目                              │
│                                          │
│  标题：    [            ]  类型：[药方 ▼]  │
│  疗法分类：[药浴疗法 ▼]   疾病分类：[风湿 ▼]│
│  药材分类：[根茎类 ▼]                     │
│  内容：    [知识内容详情...              ] │
│  步骤：    [1. 准备药材 2. 加水煎煮...    ] │
│  相关图片：[ImageUploader] 最多9张        │
│  演示视频：[VideoUploader] 最多1个        │
│  相关文档：[DocumentUploader] 最多3个     │
│  关联植物：[1,2,3] (植物ID逗号分隔)       │
│  ── 更新日志 ──                           │
│                        [取消]  [保存]     │
└──────────────────────────────────────────┘
```

**表单字段：**

| 字段 | 组件 | 说明 |
|------|------|------|
| title | el-input | 知识标题（必填） |
| type | el-select | 类型选项：药方/疗法/文化/其他（必填） |
| therapyCategory | el-select | 疗法分类：药浴/艾灸/推拿/针灸/其他（可清空） |
| diseaseCategory | el-select | 疾病分类：风湿骨痛/妇科/跌打损伤/消化/呼吸/皮肤/其他（可清空） |
| herbCategory | el-select | 药材分类：根茎类/全草类/叶类/花类/果实种子类/其他（可清空） |
| content | el-input(textarea) | 知识内容 |
| steps | el-input(textarea) | 操作步骤（每行一步） |
| images | ImageUploader | 相关图片（最多9张，picture-card模式） |
| videoUrl | VideoUploader | 演示视频（最多1个，不支持多选） |
| documents | DocumentUploader | 相关文档（最多3个，支持拖拽） |
| relatedPlants | el-input | 关联植物ID（逗号分隔，如"1,2,3"） |

**保存时的特殊处理：** `videoUrl` 字段如果是数组则取第一个元素转为字符串

**依赖组件：** `ImageUploader`、`VideoUploader`、`DocumentUploader`、`UpdateLogCard`、`UpdateLogDialog`、`useUpdateLog`

---

### InheritorFormDialog —— 传承人表单

```
┌──────────────────────────────────────────┐
│  新增传承人                                │
│                                          │
│  姓名：    [吴老医师  ]  级别：[省级 ▼]    │
│  经验年限：[45] (0-100)  技艺特色：[药浴]  │
│  简介：    [吴老医师自幼跟随...          ] │
│  传承人照片：[ImageUploader] 最多9张      │
│  相关视频：  [VideoUploader] 最多3个      │
│  代表案例：  [每个案例一行...            ] │
│  荣誉资质：  [多个荣誉用逗号分隔]          │
│  资质文档：  [DocumentUploader] 最多5个   │
│  ── 更新日志 ──                           │
│                        [取消]  [保存]     │
└──────────────────────────────────────────┘
```

**表单字段：**

| 字段 | 组件 | 说明 |
|------|------|------|
| name | el-input | 传承人姓名（必填） |
| level | el-select | 级别选项：国家级/自治区级/市级/县级（必填） |
| experienceYears | el-input-number | 经验年限（0-100，默认0） |
| specialties | el-input | 技艺特色（如：侗族药浴疗法、侗医推拿） |
| bio | el-input(textarea) | 个人简介 |
| images | ImageUploader | 传承人照片（最多9张） |
| videos | VideoUploader | 相关视频（最多3个） |
| representativeCases | el-input(textarea) | 代表案例（每个案例一行） |
| honors | el-input | 荣誉资质（多个荣誉用逗号分隔） |
| documents | DocumentUploader | 资质文档（最多5个，支持拖拽） |

**依赖组件：** `ImageUploader`、`VideoUploader`、`DocumentUploader`、`UpdateLogCard`、`UpdateLogDialog`、`useUpdateLog`

---

### ResourceFormDialog —— 学习资源表单

```
┌──────────────────────────────────────────┐
│  新增学习资源                              │
│                                          │
│  标题：    [            ]  分类：[入门 ▼]  │
│  资源文件：                               │
│  [图片] [视频] [文档]  ← 文件类型切换      │
│  ⚠ 当前已有图片文件，如需上传视频请先删除   │
│  [ImageUploader / VideoUploader / ...]   │
│  描述：    [资源描述...                  ] │
│  ── 更新日志 ──                           │
│                        [取消]  [保存]     │
└──────────────────────────────────────────┘
```

**表单字段：**

| 字段 | 组件 | 说明 |
|------|------|------|
| title | el-input | 资源标题（必填） |
| category | el-select | 分类选项：入门/进阶/专业/其他（必填，可自定义输入） |
| files | 三种上传组件切换 | 资源文件（必填，至少1个） |
| description | el-input(textarea) | 资源描述 |

**核心逻辑：**
- **文件类型互斥**：通过 `uploadType` (image/video/document) 切换上传组件类型，同一资源只能上传同类型文件
- **跨类型冲突检测**：`hasOtherTypeFiles` 计算属性检测是否已有其他类型的文件，若有则显示警告并禁用当前上传组件
- **文件类型自动检测**：`detectFileTypeFromUrl` 根据文件扩展名判断类型
- **编辑模式初始化**：打开弹窗时通过 `parseFilesToTypeArrays` 将已有文件按类型分发到三个上传组件
- **保存时合并**：将 imageFiles/videoFiles/documentFiles 合并为统一的 files 数组，使用 `stringifyMediaList` 序列化

**分类选项支持自定义输入**：`allow-create` + `filterable` 属性允许管理员输入不在预设列表中的分类

**依赖组件：** `ImageUploader`、`VideoUploader`、`DocumentUploader`、`UpdateLogCard`、`UpdateLogDialog`、`useUpdateLog`、`@/utils/media`

---

### QaFormDialog —— 问答表单

```
┌──────────────────────────────────────┐
│  问答                                 │
│                                      │
│  问题：  [侗族药浴有哪些功效？       ] │
│  分类：  [侗药常识 ▼]                 │
│  答案：  [侗族药浴具有祛风除湿...     ] │
│                                      │
│                [取消]  [保存]         │
└──────────────────────────────────────┘
```

**表单字段：**

| 字段 | 组件 | 说明 |
|------|------|------|
| question | el-input(textarea) | 问题内容 |
| category | el-select | 分类选项：侗药常识/侗医疗法/文化背景/其他 |
| answer | el-input(textarea) | 答案内容 |

**特点：** 结构最简单的表单弹窗，无文件上传、无更新日志、无必填验证

---

### QuizFormDialog —— 测验题目表单

```
┌──────────────────────────────────────┐
│  测验题目                              │
│                                      │
│  题目：    [以下哪种是侗族常用药材？  ] │
│  分类：    [侗药常识 ▼]               │
│  选项A：  [钩藤]                      │
│  选项B：  [银杏]                      │
│  选项C：  [人参]                      │
│  选项D：  [灵芝]                      │
│  正确答案：[A ▼]                      │
│  解析：    [钩藤是侗族地区最常见的... ] │
│                                      │
│                [取消]  [保存]         │
└──────────────────────────────────────┘
```

**表单字段：**

| 字段 | 组件 | 说明 |
|------|------|------|
| question | el-input(textarea) | 题目内容 |
| category | el-select | 分类选项：侗药常识/侗医疗法/文化背景 |
| optionA-D | el-input | 四个选项 |
| correctAnswer | el-select | 正确答案：A/B/C/D（默认A） |
| explanation | el-input(textarea) | 答案解析（可选） |

**核心逻辑：**
- **选项数据转换**：编辑模式下，从后端的 `options` 或 `optionList` 数组中提取4个选项分别填入 optionA-D 字段
- **保存时重组**：将 optionA-D 合并为 `options` 数组提交给后端
- **答案字段兼容**：读取时兼容 `correctAnswer` 和 `answer` 两种字段名

```javascript
// 编辑模式：数组 → 独立字段
form.value = {
  optionA: options[0] || "",
  optionB: options[1] || "",
  optionC: options[2] || "",
  optionD: options[3] || "",
}

// 保存时：独立字段 → 数组
emit('save', {
  options: [form.value.optionA, form.value.optionB, form.value.optionC, form.value.optionD],
})
```

---

## 更新日志系统

PlantFormDialog、KnowledgeFormDialog、InheritorFormDialog、ResourceFormDialog 四个表单集成了更新日志功能：

```
┌──────────────────────────────────────┐
│  ── 更新日志 ──                       │
│                                      │
│  📅 2025-03-15  管理员               │
│     新增药用植物                      │
│  📅 2025-04-20  管理员               │
│     更新药用植物信息                  │
│                                      │
│  [+ 添加日志]                         │
└──────────────────────────────────────┘
```

**工作原理：**
1. 使用 `useUpdateLog` composable 提供 `parseUpdateLog`、`stringifyUpdateLog`、`addLog`、`updateLog`、`deleteLog` 等方法
2. 日志数据存储在 `updateLog` 字段中（JSON字符串格式）
3. `UpdateLogCard` 组件展示日志列表，支持添加、编辑、删除操作
4. `UpdateLogDialog` 弹窗用于添加/编辑单条日志
5. 保存时自动追加一条操作日志（如"新增药用植物"/"更新药用植物信息"），同一天不重复追加

**自动日志追加逻辑：**

```javascript
const handleSave = () => {
  // 验证必填字段...

  // 自动追加操作日志
  const autoLog = isEdit.value ? '更新药用植物信息' : '新增药用植物'
  const currentLogs = parseUpdateLog(form.value.updateLog)
  const hasRecentLog = currentLogs.length > 0 &&
    currentLogs[0].time === new Date().toISOString().split('T')[0]

  // 同一天已有日志则不重复追加
  const finalUpdateLog = hasRecentLog
    ? form.value.updateLog
    : stringifyUpdateLog(addLog(form.value.updateLog, autoLog, '管理员'))

  emit('save', { ...form.value, updateLog: finalUpdateLog })
}
```

---

## defineExpose 暴露的方法

PlantFormDialog、KnowledgeFormDialog、InheritorFormDialog、ResourceFormDialog 通过 `defineExpose` 暴露 `setSaving` 方法：

```javascript
defineExpose({ setSaving: (val) => { saving.value = val } })
```

**用途：** 父组件可以在异步保存操作开始/结束时控制保存按钮的 loading 状态：

```javascript
// 父组件中
const formRef = ref(null)

const handleSave = async (formData) => {
  formRef.value.setSaving(true)   // 显示 loading
  try {
    await request.post('/admin/plants', formData)
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    formRef.value.setSaving(false) // 取消 loading
  }
}
```

---

## 常见错误

### 错误1：编辑时直接修改了原始数据

```vue
<script setup>
// ❌ 编辑时直接引用原对象，修改表单会同时修改列表中的数据
const handleEdit = (row) => {
  formData.value = row  // formData 和 row 指向同一个对象！
  // 用户在表单里修改名称，列表里的名称也跟着变了
  // 如果用户取消编辑，列表里的名称已经被改了，无法恢复！
}

// ✅ 用展开运算符复制一份数据，修改表单不影响原数据
watch(() => props.visible, (val) => {
  if (val) {
    form.value = props.data
      ? { ...props.data, images: parseToArray(props.data.images) }
      : getDefaultForm()
  }
})
</script>
```

### 错误2：忘记转换 JSON 字段格式

```vue
<script setup>
// ❌ 直接将后端返回的 JSON 字符串传给上传组件，组件无法识别
form.value = { ...props.data }  // images 是 "[{url:'...'}]" 字符串

// ✅ 打开时解析为数组，保存时序列化为字符串
form.value = {
  ...props.data,
  images: parseToArray(props.data.images),   // 字符串 → 数组
}
// 保存时
emit('save', {
  ...form.value,
  images: parseToString(form.value.images),  // 数组 → 字符串
})
</script>
```

### 错误3：ResourceFormDialog 中同时上传不同类型文件

```vue
<script setup>
// ❌ 同时上传图片和视频，保存时文件类型判断混乱
// ResourceFormDialog 设计为同一资源只能有一种文件类型

// ✅ 组件已内置互斥检测：
// - hasOtherTypeFiles 计算属性检测跨类型冲突
// - 冲突时显示警告并禁用当前上传组件
// - 保存时以第一个文件的类型作为资源类型
</script>
```

---

## 代码审查与改进建议

- [一致性] QaFormDialog 和 QuizFormDialog 没有集成更新日志功能，与其他4个表单行为不一致
- [一致性] QaFormDialog 没有必填验证，QuizFormDialog 也没有，可能导致提交空数据
- [复用] parseToArray/parseToString 在 PlantFormDialog、KnowledgeFormDialog、InheritorFormDialog 中重复定义，应抽取为公共工具函数
- [体验] ResourceFormDialog 的文件类型互斥机制对用户不够友好，建议改为自动检测类型而非手动切换
- [验证] 所有表单均未使用 el-form 的 rules 验证规则，仅靠手动 ElMessage.warning 提示，建议统一使用 el-form 的内置验证
