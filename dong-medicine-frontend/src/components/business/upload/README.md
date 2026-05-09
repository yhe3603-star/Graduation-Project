# 上传组件（Upload Components）

## 什么是上传组件？

类比：**快递寄件台**——你把包裹放到寄件台上，工作人员会检查包裹是否合规（不能寄违禁品），然后安全地送到目的地。

上传组件负责把用户选择的文件安全地送到服务器。在"寄件"之前，它会先做前端检查（文件类型对不对？大小超没超？），通过后才真正上传。

```
用户选择文件
    ↓
[前端检查] → 类型？大小？数量？ → 不通过 → 提示错误
    ↓ 通过
[上传到服务器] → 服务器再检查一遍 → 返回文件URL
    ↓
[展示上传结果]
```

---

## 文件结构

```
upload/
├── FileUploader.vue      # 通用文件上传器（基于 el-upload，支持多种列表样式和拖拽）
├── ImageUploader.vue     # 图片上传器（网格展示 + 拖拽排序 + 模拟进度）
├── VideoUploader.vue     # 视频上传器（视频预览 + 真实进度）
├── DocumentUploader.vue  # 文档上传器（文件图标 + 文档预览 + 拖拽上传）
├── index.js              # 统一导出
└── README.md             # 本文档
```

---

## 架构设计

上传组件采用**两层架构**：

```
┌──────────────────────────────────────────────────────┐
│  专用上传组件层                                       │
│  ImageUploader / VideoUploader / DocumentUploader    │
│  ┌─────────────────────────────────────────────────┐ │
│  │  各自的 UI 展示逻辑                              │ │
│  │  · 图片网格 / 视频卡片 / 文档列表                 │ │
│  │  · 各自特有的交互（拖拽排序、视频预览、文档预览）  │ │
│  └──────────────────────┬──────────────────────────┘ │
│                         │ 调用                        │
│  ┌──────────────────────▼──────────────────────────┐ │
│  │  useFileUpload composable（共享逻辑层）           │ │
│  │  · 上传URL和请求头计算                            │ │
│  │  · 文件格式/大小/数量校验                         │ │
│  │  · 上传成功/失败处理                              │ │
│  │  · 文件删除（服务器 + 列表）                      │ │
│  │  · modelValue 双向绑定                           │ │
│  └─────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────┤
│  FileUploader（独立组件，直接使用 el-upload）          │
│  · 不使用 useFileUpload，自行实现全部逻辑              │
│  · 支持更多 el-upload 原生配置项                      │
└──────────────────────────────────────────────────────┘
```

> **关键区别**：`ImageUploader`/`VideoUploader`/`DocumentUploader` 三个专用组件共享 `useFileUpload` composable 的逻辑，而 `FileUploader` 是独立组件，直接封装 `el-upload`，提供更灵活的配置。

---

## 组件列表

### ImageUploader —— 图片上传器

**用途：** 上传药用植物照片、传承人头像等图片，支持拖拽排序和网格预览

```
┌────────────────────────────────────┐
│  ┌──────────┐ ┌──────────┐        │
│  │  ✅       │ │  ✅       │ ┌────┐│
│  │  钩藤.jpg │ │  透骨草.png│ │ +  ││
│  │  ↕ 🗑     │ │  ↕ 🗑     │ │    ││
│  └──────────┘ └──────────┘ └────┘│
│                                    │
│  支持 jpg/jpeg/png/gif/bmp/webp格式│
│  单个图片不超过 10MB，最多 9 张     │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | String/Array | `''` | 已上传文件数据（v-model 双向绑定） |
| category | String | `'plants'` | 上传分类目录 |
| limit | Number | `9` | 最大上传数量 |
| multiple | Boolean | `true` | 是否允许多选 |
| maxSize | Number | `10` | 单文件最大大小（MB） |
| disabled | Boolean | `false` | 是否禁用 |
| showName | Boolean | `false` | 是否显示文件名 |
| showTip | Boolean | `true` | 是否显示提示文字 |
| replaceConfirm | Boolean | `false` | 替换文件时是否弹出确认框 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | `(value)` | 文件列表更新 |
| change | `(items)` | 文件列表变化 |
| success | `(data)` | 上传成功 |
| error | `(error)` | 上传失败 |
| remove | `(item)` | 删除文件 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| clearImages() | 清空所有已上传图片 |
| getImages() | 获取当前图片列表 |

#### 核心逻辑

- **useFileUpload composable**：共享上传逻辑，配置 `type='image'`，`simulateProgress=true`（图片上传使用模拟进度条）
- **拖拽排序**：使用 `vuedraggable` 组件实现图片拖拽排序，拖拽结束触发 `updateModelValue`
- **图片预览**：使用 `el-image` 的 `preview-src-list` 实现点击放大
- **操作按钮**：悬停显示拖拽手柄（↕）和删除按钮（🗑）
- **上传进度**：模拟进度条（每 100ms 增加 10%，到 90% 停止），上传完成后跳到 100%

```vue
<!-- 使用示例 -->
<ImageUploader
  v-model="imageList"
  category="plants"
  :limit="9"
  :max-size="10"
  :show-name="false"
  @success="handleUploadSuccess"
  @remove="handleRemove"
/>
```

---

### VideoUploader —— 视频上传器

**用途：** 上传传统疗法演示视频、传承人访谈视频等，支持视频预览和真实进度

```
┌────────────────────────────────────┐
│  ┌──────────────────┐              │
│  │ ▶ [视频缩略图]    │  视频名.mp4  │
│  │                  │      [删除]  │
│  └──────────────────┘              │
│  ┌──────────────────┐              │
│  │   🎬 上传视频     │              │
│  │   上传中... 67%   │              │
│  └──────────────────┘              │
│                                    │
│  支持 mp4/avi/mov/wmv/flv/mkv格式  │
│  单个视频不超过 100MB，最多 5 个    │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | String/Array | `''` | 已上传文件数据（v-model 双向绑定） |
| category | String | `'plants'` | 上传分类目录 |
| limit | Number | `5` | 最大上传数量 |
| multiple | Boolean | `true` | 是否允许多选 |
| maxSize | Number | `100` | 单文件最大大小（MB） |
| disabled | Boolean | `false` | 是否禁用 |
| showTip | Boolean | `true` | 是否显示提示文字 |
| replaceConfirm | Boolean | `false` | 替换文件时是否弹出确认框 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | `(value)` | 文件列表更新 |
| change | `(items)` | 文件列表变化 |
| success | `(data)` | 上传成功 |
| error | `(error)` | 上传失败 |
| remove | `(item)` | 删除文件 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| clearVideos() | 清空所有已上传视频 |
| getVideos() | 获取当前视频列表 |

#### 核心逻辑

- **useFileUpload composable**：配置 `type='video'`，`simulateProgress=false`（使用真实上传进度）
- **视频预览**：点击视频缩略图或播放图标，在 `el-dialog` 中播放视频
- **上传进度**：使用 el-upload 的 `on-progress` 事件获取真实上传百分比
- **视频缩略图**：使用 `<video>` 标签展示视频第一帧

```vue
<!-- 使用示例 -->
<VideoUploader
  v-model="videoList"
  category="plants"
  :limit="5"
  :max-size="100"
  @success="handleUploadSuccess"
  @remove="handleRemove"
/>
```

---

### DocumentUploader —— 文档上传器

**用途：** 上传学习资源文档（PDF、Word、Excel、PPT、TXT），支持拖拽上传和文档预览

```
┌────────────────────────────────────┐
│  📄 侗医药入门指南.pdf  ✅ 2.3MB    │
│                          [预览][删除]│
│  📄 传统疗法手册.docx   ✅ 1.8MB    │
│                          [预览][删除]│
│                                    │
│  ┌──────────────────────────────┐  │
│  │  📤 拖拽文件到此处或点击上传   │  │
│  └──────────────────────────────┘  │
│  支持 pdf/docx/doc/xlsx/xls/...   │
│  单个文档不超过 50MB，最多 5 个     │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | String/Array | `''` | 已上传文件数据（v-model 双向绑定） |
| category | String | `'common'` | 上传分类目录 |
| limit | Number | `5` | 最大上传数量 |
| multiple | Boolean | `true` | 是否允许多选 |
| maxSize | Number | `50` | 单文件最大大小（MB） |
| disabled | Boolean | `false` | 是否禁用 |
| showTip | Boolean | `true` | 是否显示提示文字 |
| drag | Boolean | `false` | 是否启用拖拽上传区域 |
| buttonText | String | `'上传文档'` | 非拖拽模式下的按钮文字 |
| replaceConfirm | Boolean | `false` | 替换文件时是否弹出确认框 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | `(value)` | 文件列表更新 |
| change | `(items)` | 文件列表变化 |
| success | `(data)` | 上传成功 |
| error | `(error)` | 上传失败 |
| remove | `(item)` | 删除文件 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| clearDocuments() | 清空所有已上传文档 |
| getDocuments() | 获取当前文档列表 |

#### 核心逻辑

- **useFileUpload composable**：配置 `type='document'`，`simulateProgress=false`
- **文档预览**：点击预览按钮，构造文档对象并调用 `DocumentPreview` 组件（来自 `../media/DocumentPreview.vue`）
- **文件图标**：根据文件类型显示不同图标和颜色（`getFileIcon`/`getFileColor`）
- **双模式上传**：`drag=false` 显示按钮式上传，`drag=true` 显示拖拽区域
- **文件大小格式化**：使用 `formatFileSize` 显示人类可读的文件大小

```vue
<!-- 按钮模式 -->
<DocumentUploader
  v-model="docList"
  category="resources"
  :limit="5"
  :max-size="50"
  button-text="上传文档"
  @success="handleUploadSuccess"
/>

<!-- 拖拽模式 -->
<DocumentUploader
  v-model="docList"
  category="resources"
  :drag="true"
  :max-size="50"
  @success="handleUploadSuccess"
/>
```

---

### FileUploader —— 通用文件上传器

**用途：** 不限制文件类型的通用上传器，直接封装 `el-upload`，提供更灵活的配置选项

```
┌────────────────────────────────────┐
│  [选择文件]                        │
│                                    │
│  📄 file1.jpg  ✅                  │
│  📄 file2.pdf  ✅                  │
│                                    │
│  支持 图片、视频、文档格式           │
│  单个文件不超过 10MB，最多上传 5 个  │
└────────────────────────────────────┘

拖拽模式：
┌────────────────────────────────────┐
│  📤 拖拽文件到此处或 点击上传       │
│  支持 图片、视频、文档格式           │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | String/Array | `''` | 已上传文件数据（v-model 双向绑定） |
| fileType | String | `'image'` | 文件类型限制：`'image'`/`'video'`/`'document'`/`'all'` |
| category | String | `'common'` | 上传分类目录 |
| limit | Number | `5` | 最大上传数量 |
| multiple | Boolean | `true` | 是否允许多选 |
| maxSize | Number | `10` | 单文件最大大小（MB），各类型默认值不同（图片10/视频100/文档50/全部100） |
| disabled | Boolean | `false` | 是否禁用 |
| listType | String | `'text'` | 文件列表样式：`'text'`/`'picture'`/`'picture-card'` |
| autoUpload | Boolean | `true` | 是否选择文件后自动上传 |
| showFileList | Boolean | `true` | 是否显示文件列表 |
| drag | Boolean | `false` | 是否启用拖拽上传 |
| buttonText | String | `'选择文件'` | 上传按钮文字 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | `(value)` | 文件列表更新（multiple 返回数组，否则返回字符串） |
| success | `(data)` | 上传成功 |
| error | `(error)` | 上传失败 |
| remove | `(file)` | 删除文件 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| clearFiles() | 清空所有已上传文件 |
| submit() | 手动提交上传（autoUpload=false 时使用） |

#### 核心逻辑

- **文件类型配置**：`CONFIG` 对象定义了各类型允许的扩展名、accept 字符串、类型名称和默认大小限制
- **上传地址**：根据 `fileType` 动态拼接上传 URL（如 `/api/upload/image`、`/api/upload/video`）
- **请求头**：自动从 `localStorage` 获取 token 添加到 `Authorization` 头
- **前端校验**：`handleBeforeUpload` 检查文件扩展名和大小
- **预览功能**：`handlePreview` 根据文件扩展名判断预览类型（图片/视频/PDF/其他），在 `el-dialog` 中展示
- **modelValue 同步**：`watch modelValue` 将外部值转为 el-upload 的 fileList 格式，上传/删除时反向更新

```vue
<!-- 图片上传（picture-card 样式） -->
<FileUploader
  v-model="imageUrls"
  file-type="image"
  list-type="picture-card"
  :limit="6"
  :max-size="10"
/>

<!-- 拖拽上传所有类型 -->
<FileUploader
  v-model="fileUrls"
  file-type="all"
  :drag="true"
  :limit="10"
  :max-size="100"
/>

<!-- 手动上传 -->
<FileUploader
  ref="uploaderRef"
  v-model="fileUrls"
  :auto-upload="false"
  @success="handleSuccess"
/>
<button @click="uploaderRef?.submit()">手动上传</button>
```

---

## useFileUpload Composable 详解

三个专用上传组件共享的 `useFileUpload` 组合式函数，位于 `@/composables/useFileUpload.js`：

```
useFileUpload(config)
  │
  ├── 输入配置
  │   ├── type: 'image' | 'video' | 'document'
  │   ├── extensions: ['jpg', 'jpeg', 'png', ...]
  │   ├── extensionLabel: 'jpg/jpeg/png/...'
  │   ├── uploadPath: '/upload/image'
  │   ├── simulateProgress: true | false
  │   ├── props: 组件props
  │   └── emit: 组件emit
  │
  ├── 返回值
  │   ├── fileList: ref([])         → 文件列表
  │   ├── uploading: ref(false)     → 上传中状态
  │   ├── uploadProgress: ref(0)    → 上传进度
  │   ├── uploadUrl: computed       → 上传地址
  │   ├── headers: computed         → 请求头（含 Authorization）
  │   ├── limitReached: computed    → 是否达到数量上限
  │   ├── tipText: computed         → 提示文字
  │   ├── handleBeforeUpload()      → 上传前校验
  │   ├── handleProgress()          → 上传进度回调
  │   ├── handleSuccess()           → 上传成功处理
  │   ├── handleError()             → 上传失败处理
  │   ├── handleRemove()            → 删除文件（服务器+列表）
  │   ├── updateModelValue()        → 更新双向绑定值
  │   └── clearFiles()              → 清空文件列表
  │
  └── 核心流程
      ├── modelValue → fileList 同步（watch + parseMediaList）
      ├── 上传前校验（格式 → 大小 → 替换确认 → 数量）
      ├── 上传成功（fileList.push + updateModelValue + emit success）
      ├── 删除文件（request.delete 服务器文件 + fileList.splice）
      └── 组件卸载清理（clearInterval 进度定时器）
```

### 上传前校验流程

```
用户选择文件
    ↓
┌─────────────────────────────┐
│  1. 格式校验                  │
│  文件扩展名是否在允许列表中？  │ → 不通过 → ElMessage.error
├─────────────────────────────┤
│  2. 大小校验                  │
│  文件大小是否超过 maxSize MB？ │ → 不通过 → ElMessage.error
├─────────────────────────────┤
│  3. 替换确认（可选）           │
│  replaceConfirm 且已有文件？  │ → 用户取消 → return false
│  → 删除已有文件（含服务器删除）│
├─────────────────────────────┤
│  4. 数量校验                  │
│  已上传数量 >= limit？        │ → 不通过 → ElMessage.warning
├─────────────────────────────┤
│  5. 通过校验，开始上传         │
│  uploading = true             │
│  启动进度条（模拟或真实）      │
└─────────────────────────────┘
```

---

## 上传安全：前端验证 + 后端验证

上传文件必须做**双重验证**，就像机场安检：先查登机牌（前端），再过安检门（后端）。

```
用户选择文件
    ↓
┌─────────────────────────────┐
│  前端验证（第一道防线）        │
│  · 文件类型是否允许？          │
│  · 文件大小是否超限？          │
│  · 文件数量是否超限？          │
└──────────┬──────────────────┘
           ↓ 通过
┌─────────────────────────────┐
│  后端验证（第二道防线）        │
│  · 再次检查文件类型（防止伪造） │
│  · 检查文件内容（防止恶意文件） │
│  · 文件重命名（防止路径攻击）   │
└──────────┬──────────────────┘
           ↓ 通过
       保存文件，返回URL
```

### 为什么需要双重验证？

```javascript
// 前端验证可以被绕过！恶意用户可以：
// 1. 打开浏览器开发者工具，修改前端代码
// 2. 直接用 Postman 等工具发送请求，跳过前端验证

// 所以后端必须再做一遍验证，不能信任前端传来的任何数据！
```

---

## 依赖关系

```
ImageUploader.vue
  ├── @element-plus/icons-vue (Plus, Picture, Delete, Rank)
  ├── vuedraggable
  └── @/composables/useFileUpload (type='image', simulateProgress=true)

VideoUploader.vue
  ├── @element-plus/icons-vue (VideoPlay, VideoCamera, Delete)
  └── @/composables/useFileUpload (type='video', simulateProgress=false)

DocumentUploader.vue
  ├── @element-plus/icons-vue (Document, Upload, UploadFilled, Delete, View, Download)
  ├── @/utils (getFileIcon, getFileColor)
  ├── @/utils/adminUtils (formatFileSize)
  ├── @/composables/useFileUpload (type='document', simulateProgress=false)
  └── ../media/DocumentPreview.vue

FileUploader.vue
  ├── @element-plus/icons-vue (Plus, Upload, UploadFilled, Document)
  ├── element-plus (ElMessage)
  └── @/utils (getResourceUrl, getFileName, logUploadError)

useFileUpload.js (composable)
  ├── vue (ref, computed, watch, onUnmounted)
  ├── element-plus (ElMessage, ElMessageBox)
  ├── @/utils/request
  └── @/utils (getResourceUrl, parseMediaList, getMediaType, logDeleteWarn)
```

---

## 常见错误

### 错误1：只做前端验证，不做后端验证

```
❌ 只在前端检查文件类型：
前端：accept="image/jpeg"  → 恶意用户可以绕过，上传 .exe 文件！

✅ 前端 + 后端双重检查：
前端：accept="image/jpeg"  → 拦截大部分普通用户
后端：检查文件头（magic bytes）→ 拦截恶意用户
```

### 错误2：上传大文件没有进度提示

```vue
<template>
  <!-- ❌ 大文件上传时没有任何提示，用户以为页面卡死了 -->
  <button @click="uploadFile">上传</button>

  <!-- ✅ 显示上传进度 -->
  <el-progress
    v-if="uploading"
    :percentage="uploadProgress"
    type="circle"
  />
  <button @click="uploadFile" :disabled="uploading">
    {{ uploading ? `上传中 ${uploadProgress}%` : '上传' }}
  </button>
</template>
```

### 错误3：删除文件时没有通知服务器

```vue
<script setup>
// ❌ 只从前端列表移除，服务器上的文件成为孤儿文件，浪费存储空间
const handleRemove = (index) => {
  fileList.value.splice(index, 1)
}

// ✅ useFileUpload 已实现：先调用服务器删除接口，再从前端列表移除
const handleRemove = async (index) => {
  const removed = fileList.value[index]
  // 通知服务器删除文件
  await request.delete('/upload', { params: { filePath: removed.path } })
  fileList.value.splice(index, 1)
  updateModelValue()
}
</script>
```

### 错误4：modelValue 格式不匹配

```vue
<script setup>
// ❌ ImageUploader 的 modelValue 期望的是文件对象数组或 JSON 字符串
// 直接传 URL 字符串会导致解析失败
const imageList = ref('/uploads/img1.jpg')

// ✅ 传正确的格式：文件对象数组
const imageList = ref([
  { path: '/uploads/img1.jpg', name: 'img1.jpg', size: 1024000 }
])

// 或 JSON 字符串（useFileUpload 内部会通过 parseMediaList 解析）
</script>
```

---

## 代码审查与改进建议

- [安全] FileUploader.vue 的 headers 直接从 localStorage 读取 token，应使用统一的请求拦截器
- [安全] FileUploader.vue 的 handlePreview 直接使用文件 URL 展示，未做权限校验
- [代码重复] FileUploader.vue 未使用 useFileUpload composable，与三个专用组件的逻辑存在重复
- [类型] modelValue 的类型为 String/Array 双类型，增加了使用复杂度，建议统一为数组类型
- [体验] ImageUploader 的模拟进度条到 90% 就停止，如果上传很慢用户会以为卡住了
