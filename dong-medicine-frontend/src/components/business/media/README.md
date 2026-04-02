# 媒体组件目录 (media)

本目录存放媒体文件相关的组件，如图片、视频、文档等。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| DocumentList.vue | 文档列表组件 |
| DocumentPreview.vue | 文档预览组件，支持PDF、Word等 |
| ImageCarousel.vue | 图片轮播组件 |
| MediaDisplay.vue | 媒体展示组件，根据类型自动选择 |
| VideoPlayer.vue | 视频播放组件 |

---

## ImageCarousel.vue - 图片轮播

支持自动播放、手动切换的图片轮播组件。

```vue
<template>
  <div class="image-carousel">
    <div class="carousel-container">
      <img :src="images[currentIndex]" class="carousel-image">
    </div>
    
    <button class="carousel-prev" @click="prev">‹</button>
    <button class="carousel-next" @click="next">›</button>
    
    <div class="carousel-dots">
      <span
        v-for="(img, index) in images"
        :key="index"
        :class="{ active: index === currentIndex }"
        @click="goTo(index)"
      />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  images: { type: Array, default: () => [] },
  autoPlay: { type: Boolean, default: true },
  interval: { type: Number, default: 3000 }
})

const currentIndex = ref(0)

const next = () => {
  currentIndex.value = (currentIndex.value + 1) % props.images.length
}

const prev = () => {
  currentIndex.value = (currentIndex.value - 1 + props.images.length) % props.images.length
}
</script>
```

---

## VideoPlayer.vue - 视频播放

基于HTML5 video的视频播放组件。

```vue
<template>
  <div class="video-player">
    <video
      ref="videoRef"
      :src="src"
      :poster="poster"
      controls
      @play="onPlay"
      @pause="onPause"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  src: { type: String, required: true },
  poster: { type: String, default: '' }
})

const emit = defineEmits(['play', 'pause', 'ended'])
</script>
```

---

## DocumentPreview.vue - 文档预览

支持PDF、Word等文档的在线预览。

```vue
<template>
  <div class="document-preview">
    <!-- PDF预览 -->
    <iframe v-if="isPdf" :src="url" class="pdf-viewer" />
    
    <!-- 图片预览 -->
    <img v-else-if="isImage" :src="url" class="image-viewer">
    
    <!-- 其他类型提示下载 -->
    <div v-else class="download-tip">
      <p>该文件类型不支持在线预览</p>
      <a :href="url" download>下载文件</a>
    </div>
  </div>
</template>
```

---

## MediaDisplay.vue - 媒体展示

根据文件类型自动选择展示方式。

```vue
<template>
  <div class="media-display">
    <VideoPlayer v-if="isVideo" :src="url" />
    <ImageCarousel v-else-if="isImage" :images="[url]" />
    <DocumentPreview v-else :url="url" :type="type" />
  </div>
</template>

<script setup>
const props = defineProps({
  url: { type: String, required: true },
  type: { type: String, default: '' }
})

const isVideo = computed(() => 
  ['mp4', 'webm', 'ogg'].includes(props.type)
)

const isImage = computed(() => 
  ['jpg', 'jpeg', 'png', 'gif'].includes(props.type)
)
</script>
```

---

**最后更新时间**：2026年4月3日
