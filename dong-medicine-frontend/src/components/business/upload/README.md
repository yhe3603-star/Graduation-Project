# 上传组件目录 (upload)

本目录存放文件上传相关的组件。

## 组件列表

| 组件 | 功能描述 |
|------|----------|
| DocumentUploader.vue | 文档上传组件 |
| FileUploader.vue | 通用文件上传组件 |
| ImageUploader.vue | 图片上传组件，支持预览和裁剪 |
| VideoUploader.vue | 视频上传组件，支持进度显示 |

---

## ImageUploader.vue - 图片上传

支持图片预览、裁剪、大小限制等功能。

```vue
<template>
  <div class="image-uploader">
    <!-- 预览区域 -->
    <div v-if="previewUrl" class="preview-area">
      <img :src="previewUrl" class="preview-image">
      <button @click="clearImage" class="clear-btn">×</button>
    </div>
    
    <!-- 上传区域 -->
    <div v-else class="upload-area" @click="triggerUpload">
      <input
        ref="inputRef"
        type="file"
        accept="image/*"
        hidden
        @change="handleFileChange"
      >
      <span class="upload-icon">+</span>
      <span class="upload-text">点击上传图片</span>
    </div>
    
    <!-- 进度条 -->
    <div v-if="uploading" class="progress-bar">
      <div class="progress" :style="{ width: progress + '%' }" />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  maxSize: { type: Number, default: 10 },  // MB
  accept: { type: Array, default: () => ['.jpg', '.jpeg', '.png', '.gif'] }
})

const emit = defineEmits(['success', 'error', 'update:imageUrl'])

const inputRef = ref(null)
const previewUrl = ref('')
const uploading = ref(false)
const progress = ref(0)

const triggerUpload = () => {
  inputRef.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  // 验证文件大小
  if (file.size > props.maxSize * 1024 * 1024) {
    emit('error', `文件大小不能超过${props.maxSize}MB`)
    return
  }
  
  // 预览
  previewUrl.value = URL.createObjectURL(file)
  
  // 上传
  uploading.value = true
  try {
    const url = await uploadFile(file, (p) => {
      progress.value = p
    })
    emit('success', url)
    emit('update:imageUrl', url)
  } catch (err) {
    emit('error', err.message)
  } finally {
    uploading.value = false
  }
}
</script>
```

---

## FileUploader.vue - 通用文件上传

```vue
<template>
  <div class="file-uploader">
    <input
      type="file"
      :accept="accept.join(',')"
      @change="handleFileChange"
    >
    
    <div v-if="uploading" class="upload-progress">
      上传中... {{ progress }}%
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  accept: { type: Array, default: () => [] },
  maxSize: { type: Number, default: 50 }
})

const emit = defineEmits(['success', 'error'])
</script>
```

---

## VideoUploader.vue - 视频上传

```vue
<template>
  <div class="video-uploader">
    <input
      type="file"
      accept="video/*"
      @change="handleFileChange"
    >
    
    <div v-if="uploading" class="upload-progress">
      <div class="progress-bar">
        <div class="progress" :style="{ width: progress + '%' }" />
      </div>
      <span>{{ progress }}%</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  maxSize: { type: Number, default: 100 }  // 视频最大100MB
})
</script>
```

---

**最后更新时间**：2026年4月3日
