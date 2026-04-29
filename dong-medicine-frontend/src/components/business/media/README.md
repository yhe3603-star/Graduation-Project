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

## 组件列表

### ImageCarousel —— 图片轮播

**用途：** 多张图片自动或手动轮播展示，常用于药用植物的多角度照片

```
┌────────────────────────────────────┐
│  ←  [  🌿 当前图片  ]  →           │
│                                    │
│     ● ○ ○ ○ ○                     │
│     ↑ 当前是第1张                   │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<ImageCarousel
  :images="plantImages"
  :auto-play="true"
  :interval="3000"
  @change="handleImageChange"
/>
```

**适用场景：** 药用植物详情页展示多张照片、首页轮播推荐

---

### VideoPlayer —— 视频播放器

**用途：** 播放侗医药传统疗法的视频教程、传承人访谈等

```
┌────────────────────────────────────┐
│                                    │
│         ▶ [视频播放区域]            │
│                                    │
│  ━━━━━━━●━━━━━━━━━━━  03:24/08:15 │
│  [▶] [🔊] [⛶]                     │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<VideoPlayer
  :src="videoUrl"
  :poster="coverImage"
  :subtitles="subtitleList"
  @play="handlePlay"
  @ended="handleVideoEnded"
/>
```

**适用场景：** 传统疗法视频教程、传承人访谈记录、药浴操作演示

---

### DocumentPreview —— 文档预览

**用途：** 在线预览 PDF、Word 等文档，不需要下载就能查看内容

```
┌────────────────────────────────────┐
│  📄 侗族药用植物图鉴.pdf     [下载] │
│  ┌──────────────────────────────┐  │
│  │                              │  │
│  │   [文档内容预览区域]           │  │
│  │                              │  │
│  │   第1页 / 共25页              │  │
│  └──────────────────────────────┘  │
│  ← 上一页              下一页 →    │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<DocumentPreview
  :url="documentUrl"
  :file-name="'侗族药用植物图鉴.pdf'"
  :file-type="'pdf'"
  @download="handleDownload"
/>
```

**适用场景：** 学习资源在线预览、研究报告查看

---

### DocumentList —— 文档列表

**用途：** 以列表形式展示多个文档，支持分类筛选和搜索

```
┌────────────────────────────────────┐
│  📚 学习资源                        │
│                                    │
│  📄 侗族药用植物图鉴.pdf    2.3MB   │
│  📄 传统疗法操作手册.docx   1.8MB   │
│  📄 侗医药入门指南.pdf      5.1MB   │
│  📄 药用植物采集手册.pdf    3.7MB   │
│                                    │
│  ← 上一页  1  2  3  下一页 →       │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<DocumentList
  :documents="resourceList"
  :loading="isLoading"
  @preview="handlePreview"
  @download="handleDownload"
/>
```

**适用场景：** 学习资源列表页、后台资源管理

---

### MediaDisplay —— 媒体综合展示

**用途：** 根据媒体类型自动选择合适的展示方式（图片用轮播、视频用播放器、文档用预览）

```
                    ┌─ 图片？→ ImageCarousel
媒体数据 → MediaDisplay ── 视频？→ VideoPlayer
                    └─ 文档？→ DocumentPreview

一个组件搞定所有媒体类型，不用手动判断！
```

```vue
<!-- 使用示例 -->
<MediaDisplay
  :media="mediaData"
  @download="handleDownload"
/>

<!-- mediaData 示例 -->
<script setup>
const mediaData = {
  type: 'image',  // 自动判断：image / video / document
  files: [
    { url: '/images/gouteng1.jpg', name: '钩藤-1' },
    { url: '/images/gouteng2.jpg', name: '钩藤-2' }
  ]
}
</script>
```

**适用场景：** 药用植物详情页（可能同时有图片和视频）、知识条目详情页

---

## 常见错误

### 错误1：图片没有设置加载失败的处理

```vue
<template>
  <!-- ❌ 图片加载失败时显示破碎图标，用户体验差 -->
  <img :src="plantImage" />

  <!-- ✅ 加上 @error 处理和 alt 属性 -->
  <img
    :src="plantImage"
    alt="钩藤"
    @error="handleImageError"
  />
</template>

<script setup>
const handleImageError = (e) => {
  // 加载失败时显示默认占位图
  e.target.src = '/images/default-plant.png'
}
</script>
```

### 错误2：视频没有设置封面图

```vue
<template>
  <!-- ❌ 视频加载前显示黑屏，用户不知道视频内容 -->
  <video :src="videoUrl" controls />

  <!-- ✅ 设置封面图（poster），视频加载前显示封面 -->
  <video
    :src="videoUrl"
    :poster="coverImage"
    controls
    preload="metadata"
  />
</template>
```

### 错误3：大文件直接全部加载

```vue
<script setup>
// ❌ 一次性加载所有文档数据，100个PDF的列表会卡死
const allDocs = ref([])
const loadDocs = async () => {
  allDocs.value = await api.getAllDocuments()  // 可能有几百条！
}

// ✅ 分页加载，每次只请求当前页的数据
const docList = ref([])
const currentPage = ref(1)
const loadDocs = async () => {
  const res = await api.getDocuments({ page: currentPage.value, size: 10 })
  docList.value = res.data.records
}
</script>
```

---

## 代码审查与改进建议

- [安全] DocumentPreview.vue中Base64编码URL未做安全校验，可能被利用实现SSRF攻击
- [逻辑] ImageCarousel.vue中watch immediate触发autoplay但DOM可能未就绪
