# 媒体组件（Media Components）

## 什么是媒体组件？

类比：**多媒体播放器**——有的播放音乐，有的播放视频，有的展示文档。它们专门处理图片、视频、文档等媒体内容的展示。

侗医药平台有大量多媒体内容：药用植物的高清照片、传统疗法的视频教程、学习资源的PDF文档。媒体组件就是这些内容的"播放器"。

```
媒体数据（图片/视频/文档）
        ↓
  [媒体组件] → 在页面上漂亮地展示出来
        ↑
  处理加载、预览、翻页等技术细节
```

---

## 文件结构

```
media/
├── MediaDisplay.vue     # 媒体综合展示（图片+视频+文档一体化）
├── VideoPlayer.vue      # 视频播放器（支持多视频轮播）
├── ImageCarousel.vue    # 图片轮播（自动/手动切换）
├── DocumentPreview.vue  # 文档在线预览（kkFileView + TXT直读）
├── DocumentList.vue     # 文档列表展示
├── HerbAudio.vue        # 侗语语音播报（Web Speech API）
├── index.js             # 统一导出
└── README.md            # 本文档
```

> **注意**：`index.js` 当前仅导出 `ImageCarousel`、`VideoPlayer`、`DocumentPreview`、`DocumentList` 四个组件，`MediaDisplay` 和 `HerbAudio` 未在 index.js 中统一导出，需在使用处单独引入。

---

## 组件列表

### MediaDisplay —— 媒体综合展示

**用途：** 根据媒体类型自动分类展示，一个组件同时处理图片、视频、文档三种媒体

```
                    ┌─ 图片？→ 图片网格（支持点击预览大图）
媒体数据 → MediaDisplay ── 视频？→ 原生视频播放器
                    └─ 文档？→ 文档列表（预览 + 下载）

一个组件搞定所有媒体类型，不用手动判断！
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| files | String/Array | `''` | 媒体文件数据，支持 JSON 字符串或数组，通过 `parseMediaList` 解析 |
| showImages | Boolean | `true` | 是否显示图片区域 |
| showVideos | Boolean | `true` | 是否显示视频区域 |
| showDocuments | Boolean | `true` | 是否显示文档区域 |
| showDivider | Boolean | `true` | 是否显示区域分隔线 |
| imageTitle | String | `'相关图片'` | 图片区域标题 |
| videoTitle | String | `'相关视频'` | 视频区域标题 |
| documentTitle | String | `'相关文档'` | 文档区域标题 |

#### 核心逻辑

- **媒体分类**：使用 `parseMediaList` 解析输入数据，再通过 `separateMediaByType` 按类型分离为 images/videos/documents
- **图片预览**：使用 `el-image` 的 `preview-src-list` 实现点击放大预览
- **视频播放**：使用原生 `<video>` 标签，带 controls 控制条
- **文档操作**：支持预览（通过 `DocumentPreview` 组件）和下载（通过 `downloadMedia` 工具函数）
- **文档图标**：根据文件类型动态显示不同图标和颜色（`getFileIcon`/`getFileColor`）
- **预览判断**：`canPreview` 检查文件扩展名是否在可预览列表中（pdf/doc/docx/xls/xlsx/ppt/pptx/txt）

```vue
<!-- 使用示例 -->
<MediaDisplay
  :files="plantDetail.images"
  :show-divider="true"
  image-title="植物图片"
  video-title="相关视频"
  document-title="研究文档"
/>
```

---

### ImageCarousel —— 图片轮播

**用途：** 多张图片自动或手动轮播展示，常用于药用植物的多角度照片

```
┌────────────────────────────────────┐
│  ←  [  🌿 当前图片  ]  →           │
│                                    │
│     ● ○ ○ ○ ○                     │
│     ↑ 当前是第1张                   │
│                                    │
│     1 / 5                          │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| images | Array/String | `[]` | 图片数据，支持 JSON 字符串或 URL 数组 |
| autoPlay | Boolean | `true` | 是否自动播放 |
| autoPlayInterval | Number | `4000` | 自动播放间隔（毫秒） |
| height | String | `'320px'` | 轮播区域高度 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| prevSlide() | 上一张 |
| nextSlide() | 下一张 |
| startAutoPlay() | 开始自动播放 |
| stopAutoPlay() | 停止自动播放 |

#### 核心逻辑

- **数据解析**：使用 `parseMediaList` 统一处理字符串/数组输入，提取 URL 列表
- **自动播放**：`startAutoPlay` 使用 `setInterval` 定时切换，`onUnmounted` 时自动清理定时器
- **循环轮播**：`nextSlide` 到达末尾后回到第一张，`prevSlide` 到达开头后跳到最后一张
- **点击预览**：使用 `el-image` 的 `preview-src-list` 实现点击查看大图
- **过渡动画**：使用 `transition-group` 的 `slide-fade` 实现切换动画
- **指示器**：底部圆点指示器，点击可跳转到对应图片

```vue
<!-- 使用示例 -->
<ImageCarousel
  :images="plantImages"
  :auto-play="true"
  :auto-play-interval="4000"
  height="320px"
/>
```

**适用场景：** 药用植物详情页展示多张照片、首页轮播推荐

---

### VideoPlayer —— 视频播放器

**用途：** 播放侗医药传统疗法的视频教程、传承人访谈等，支持多视频轮播切换

```
┌────────────────────────────────────┐
│  ←  [  ▶ 视频播放区域  ]  →        │
│                                    │
│     ● ○ ○                          │
│     1 / 3                          │
│                                    │
│  ━━━━━━━●━━━━━━━━━━━  03:24/08:15 │
│  [▶] [🔊] [⛶]                     │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| videos | Array/String | `[]` | 视频数据，支持 JSON 字符串或 URL 数组 |
| height | String | `'400px'` | 视频最大高度 |
| autoPlay | Boolean | `true` | 是否自动播放 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| video-play | `(index)` | 视频开始播放 |
| video-pause | `(index)` | 视频暂停 |
| video-ended | `(index)` | 视频播放结束 |

#### 暴露方法

| 方法 | 说明 |
|------|------|
| play() | 播放当前视频 |
| pause() | 暂停当前视频 |
| prevVideo() | 上一个视频 |
| nextVideo() | 下一个视频 |
| switchToVideo(index) | 切换到指定索引的视频 |
| getCurrentIndex() | 获取当前视频索引 |

#### 核心逻辑

- **多视频轮播**：当 `videoList.length > 1` 时显示左右切换按钮和指示器
- **自动播放**：视频加载完成后（`canplay` 事件）自动播放，播放失败时 console.warn 提示
- **切换逻辑**：切换视频时先暂停当前视频，更新索引后延迟 100ms 播放新视频
- **视频重载**：`videoList` 变化时重置索引为 0 并调用 `video.load()`
- **组件卸载**：`onUnmounted` 时暂停视频并移除事件监听器，防止内存泄漏

```vue
<!-- 使用示例 -->
<VideoPlayer
  :videos="videoList"
  height="400px"
  :auto-play="true"
  @video-play="handlePlay"
  @video-ended="handleVideoEnded"
/>
```

**适用场景：** 传统疗法视频教程、传承人访谈记录、药浴操作演示

---

### DocumentPreview —— 文档在线预览

**用途：** 在线预览 PDF、Word、Excel、PPT 等文档，支持弹窗和内嵌两种模式

```
┌────────────────────────────────────┐
│  📄 侗族药用植物图鉴.pdf  [PDF] 2.3MB│
│  ┌──────────────────────────────┐  │
│  │                              │  │
│  │   [kkFileView 文档预览区域]   │  │
│  │                              │  │
│  └──────────────────────────────┘  │
│                                    │
│              [下载文件]  [关闭]     │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| modelValue | Boolean | `false` | 控制弹窗显示/隐藏（v-model） |
| document | Object | `null` | 文档对象，含 url/path、name、size、type、originalFileName 等字段 |
| inline | Boolean | `false` | 是否使用内嵌模式（非弹窗） |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | `(value)` | 弹窗显示状态变化 |
| download | `(document)` | 点击下载按钮 |
| close | - | 关闭预览 |

#### 核心逻辑

- **预览方式一：kkFileView**：对 PDF/Office 文档（pdf/doc/docx/xls/xlsx/ppt/pptx），通过 kkFileView 服务在线预览
  - URL 编码：`btoa(unescape(encodeURIComponent(fullUrl)))` 将完整文件 URL 转为 Base64
  - 预览地址：`${KKFILEVIEW_SERVER}/onlinePreview?url=${base64Url}`
  - 服务器地址：通过 `VITE_KKFILEVIEW_URL` 环境变量配置，默认 `/kkfileview`
  - 文件主机：通过 `VITE_KKFILEVIEW_FILE_HOST` 环境变量配置相对路径的域名前缀
- **预览方式二：TXT 直读**：对 TXT 文件使用 `fetch` 获取文本内容，在 `<pre>` 标签中展示
- **不支持预览**：其他格式显示"暂不支持在线预览"提示
- **下载功能**：
  - 已登录用户：通过 `/api/resources/download/{id}` 下载（带 Authorization header）
  - 未登录用户：提示登录（调用 `inject('showLoginDialog')`）
  - 无 id 的文档：直接创建 `<a>` 标签下载
- **双模式**：`inline=false` 使用 `el-dialog` 弹窗，`inline=true` 直接内嵌在页面中

```vue
<!-- 弹窗模式 -->
<DocumentPreview
  v-model="previewVisible"
  :document="currentDocument"
  @download="handleDownload"
/>

<!-- 内嵌模式 -->
<DocumentPreview
  :inline="true"
  :document="currentDocument"
/>
```

**适用场景：** 学习资源在线预览、研究报告查看、后台文档预览

---

### DocumentList —— 文档列表

**用途：** 以列表形式展示多个文档，显示文件类型、大小、上传时间等信息

```
┌────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ 📄 侗族药用植物图鉴.pdf       │  │
│  │ PDF  2.3MB  2025-04-20  [预览]│  │
│  ├──────────────────────────────┤  │
│  │ 📄 传统疗法操作手册.docx      │  │
│  │ DOCX  1.8MB  2025-04-18 [预览]│  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| documents | Array | `[]` | 文档数据列表，每项含 id、originalFileName/fileName/name、type、size、uploadTime、description |
| loading | Boolean | `false` | 加载状态 |
| showActions | Boolean | `true` | 是否显示操作按钮 |

#### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| document-click | `(doc)` | 点击文档项（整行或预览按钮） |

#### 核心逻辑

- **文件图标**：当前统一使用 `Document` 图标（`getFileIcon` 函数固定返回 `Document`）
- **时间格式化**：使用 `formatTime(dateStr, { format: 'date' })` 格式化上传时间
- **文件大小**：使用 `formatFileSize` 格式化文件大小
- **悬停效果**：鼠标悬停时文档项右移 4px 并显示蓝色边框

```vue
<!-- 使用示例 -->
<DocumentList
  :documents="resourceList"
  :loading="isLoading"
  :show-actions="true"
  @document-click="handleDocumentClick"
/>
```

**适用场景：** 学习资源列表页、后台资源管理

---

### HerbAudio —— 侗语语音播报

**用途：** 使用浏览器 Web Speech API 播报药用植物的侗语名称，帮助用户了解侗语发音

```
┌──────────────────────────┐
│  🔊 [侗语发音]            │
│     ↑ 点击播放/暂停       │
└──────────────────────────┘

播放中：
┌──────────────────────────┐
│  ⏸ [播放中...]  ← 脉冲动画 │
└──────────────────────────┘
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| plantName | String | `''` | 要播报的植物名称（侗语名或中文名） |
| plantId | Number/String | `null` | 植物ID（预留字段） |
| showLabel | Boolean | `true` | 是否显示文字标签 |
| iconSize | Number/String | `18` | 图标大小 |
| buttonLabel | String | `'侗语发音'` | 按钮文字标签 |

#### 核心逻辑

- **浏览器兼容性检测**：`supported` 检查 `window.speechSynthesis` 是否存在，不支持时按钮禁用并显示提示
- **语音选择**：`getChineseVoice` 按优先级选择中文语音 —— Tingting > Xiaoxiao > zh-CN > zh > zh-HK > zh 开头
- **语音参数**：`lang='zh-CN'`，`rate=0.85`（稍慢便于听清），`pitch=1.0`，`volume=1.0`
- **播放控制**：`togglePlay` 切换播放/停止，`startPlay` 开始播报，`stopPlay` 停止播报
- **空值处理**：植物名为"暂无侗语名称"或"暂无"时不播报
- **脉冲动画**：播放中按钮显示绿色背景和 `audio-pulse` 动画
- **组件卸载**：`onUnmounted` 时停止播报，防止语音继续播放

```vue
<!-- 使用示例 -->
<HerbAudio
  plant-name="钩藤"
  :show-label="true"
  button-label="侗语发音"
/>
```

**适用场景：** 药用植物详情页、药用图鉴列表（辅助了解侗语发音）

---

## 依赖关系

```
MediaDisplay.vue
  ├── @element-plus/icons-vue (Picture, Download, View)
  ├── @/utils (parseMediaList, separateMediaByType, downloadMedia, getFileIcon, getFileColor, formatFileSize)
  └── ./DocumentPreview.vue

VideoPlayer.vue
  ├── @element-plus/icons-vue (ArrowLeft, ArrowRight, VideoPlay)
  ├── element-plus (ElMessage)
  ├── @/utils/logger (logAutoPlayWarn)
  └── @/utils/media (parseMediaList)

ImageCarousel.vue
  ├── @element-plus/icons-vue (ArrowLeft, ArrowRight, Picture, Loading)
  └── @/utils (parseMediaList)

DocumentPreview.vue
  ├── @element-plus/icons-vue (Document, Download, Loading)
  ├── element-plus (ElMessage, ElMessageBox)
  ├── @/utils/adminUtils (formatFileSize)
  └── @/utils/media (normalizeUrl)

DocumentList.vue
  ├── @element-plus/icons-vue (Document, Loading)
  ├── @/utils/adminUtils (formatFileSize)
  └── @/utils/index (formatTime)

HerbAudio.vue
  └── @element-plus/icons-vue (VideoPause, VideoPlay)
```

---

## 常见错误

### 错误1：图片没有设置加载失败的处理

```vue
<template>
  <!-- ❌ 图片加载失败时显示破碎图标，用户体验差 -->
  <img :src="plantImage" />

  <!-- ✅ ImageCarousel 和 MediaDisplay 都已配置 error 插槽 -->
  <el-image :src="plantImage" fit="contain">
    <template #error>
      <div class="image-error">
        <el-icon><Picture /></el-icon><span>图片加载失败</span>
      </div>
    </template>
  </el-image>
</template>
```

### 错误2：视频没有处理自动播放失败

```vue
<script setup>
// ❌ 浏览器可能阻止自动播放（需要用户交互后才能播放），直接 play() 会抛异常
videoRef.value.play()

// ✅ VideoPlayer 已处理：catch 异常并 console.warn
videoRef.value.play().catch(e => {
  console.warn('自动播放失败，可能需要用户交互:', e)
})
</script>
```

### 错误3：DocumentPreview 的 kkFileView URL 拼接错误

```javascript
// ❌ 直接拼接相对路径，kkFileView 服务无法访问
const url = `/files/doc.pdf`
const previewUrl = `${kkServer}/onlinePreview?url=${url}`

// ✅ 必须转为完整 URL 再 Base64 编码
const fullUrl = getFullFileUrl(relativeUrl)  // 补全为 http://xxx/files/doc.pdf
const base64Url = btoa(unescape(encodeURIComponent(fullUrl)))
const previewUrl = `${kkServer}/onlinePreview?url=${base64Url}`
```

### 错误4：HerbAudio 在组件卸载后语音仍在播放

```vue
<script setup>
// ❌ 组件卸载后语音继续播放，用户无法停止
// 缺少 onUnmounted 清理！

// ✅ HerbAudio 已在 onUnmounted 中调用 stopPlay()
onUnmounted(() => {
  stopPlay()
})
</script>
```

---

## 代码审查与改进建议

- [安全] DocumentPreview.vue 中 Base64 编码 URL 未做安全校验，可能被利用实现 SSRF 攻击
- [逻辑] ImageCarousel.vue 中 watch immediate 触发 autoplay 但 DOM 可能未就绪
- [导出] MediaDisplay.vue 和 HerbAudio.vue 未在 index.js 中统一导出，使用时需单独引入
- [功能] DocumentList.vue 的 getFileIcon 固定返回 Document 图标，未根据文件类型显示不同图标
- [兼容性] HerbAudio.vue 依赖浏览器原生 speechSynthesis API，部分浏览器（如 Firefox Linux 版）不支持中文语音
