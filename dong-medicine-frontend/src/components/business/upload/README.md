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

## 组件列表

### ImageUploader —— 图片上传器

**用途：** 上传药用植物照片、传承人头像等图片

```
┌────────────────────────────────────┐
│  📷 上传图片                        │
│                                    │
│  ┌──────────┐ ┌──────────┐        │
│  │  ✅       │ │  ✅       │ ┌────┐│
│  │  钩藤.jpg │ │  透骨草.png│ │ +  ││
│  │  1.2MB   │ │  0.8MB   │ │    ││
│  └──────────┘ └──────────┘ └────┘│
│                                    │
│  支持 JPG/PNG，单张不超过 5MB       │
│  最多上传 6 张                      │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<ImageUploader
  v-model:file-list="imageList"
  :max-count="6"
  :max-size="5 * 1024 * 1024"
  :accept="'image/jpeg,image/png'"
  @upload-success="handleUploadSuccess"
  @upload-error="handleUploadError"
/>
```

---

### VideoUploader —— 视频上传器

**用途：** 上传传统疗法演示视频、传承人访谈视频等

```
┌────────────────────────────────────┐
│  🎬 上传视频                        │
│                                    │
│  ┌──────────────────────────────┐  │
│  │                              │  │
│  │    [选择视频文件或拖拽到此处]   │  │
│  │                              │  │
│  └──────────────────────────────┘  │
│                                    │
│  支持 MP4/WEBM，不超过 200MB        │
│  上传中：████████░░░░  67%          │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<VideoUploader
  v-model:file="videoFile"
  :max-size="200 * 1024 * 1024"
  :accept="'video/mp4,video/webm'"
  @upload-progress="handleProgress"
  @upload-success="handleUploadSuccess"
/>
```

---

### DocumentUploader —— 文档上传器

**用途：** 上传学习资源文档（PDF、Word等）

```
┌────────────────────────────────────┐
│  📄 上传文档                        │
│                                    │
│  📄 侗医药入门指南.pdf  ✅ 2.3MB    │
│  📄 传统疗法手册.docx   ✅ 1.8MB    │
│                                    │
│  [选择文档]                         │
│                                    │
│  支持 PDF/DOCX，单个不超过 50MB     │
└────────────────────────────────────┘
```

```vue
<!-- 使用示例 -->
<DocumentUploader
  v-model:file-list="docList"
  :max-size="50 * 1024 * 1024"
  :accept="'.pdf,.docx'"
  @upload-success="handleUploadSuccess"
/>
```

---

### FileUploader —— 通用文件上传器

**用途：** 不限制文件类型的通用上传器，以上三个组件的底层基础

```vue
<!-- 使用示例 -->
<FileUploader
  v-model:file-list="fileList"
  :max-count="10"
  :max-size="100 * 1024 * 1024"
  :accept="'*'"
  @upload-success="handleUploadSuccess"
/>
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
│  · 文件名是否合法？            │
└──────────┬──────────────────┘
           ↓ 通过
┌─────────────────────────────┐
│  后端验证（第二道防线）        │
│  · 再次检查文件类型（防止伪造） │
│  · 检查文件内容（防止恶意文件） │
│  · 病毒扫描                   │
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

### 前端验证示例

```javascript
/**
 * 前端文件验证工具函数
 * @param {File} file - 用户选择的文件
 * @param {Object} rules - 验证规则
 * @returns {string|null} 错误信息，null表示验证通过
 */
const validateFile = (file, rules) => {
  // 检查文件类型
  if (rules.accept && !rules.accept.includes(file.type)) {
    return `不支持的文件类型：${file.type}，请上传 ${rules.accept} 格式的文件`
  }

  // 检查文件大小
  if (rules.maxSize && file.size > rules.maxSize) {
    const maxSizeMB = (rules.maxSize / 1024 / 1024).toFixed(1)
    return `文件大小超过限制，最大允许 ${maxSizeMB}MB`
  }

  // 检查文件名（防止特殊字符）
  if (/[<>:"/\\|?*]/.test(file.name)) {
    return '文件名包含非法字符，请重命名后上传'
  }

  return null  // 验证通过
}
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
  <button @click="uploadFile" :disabled="uploading">
    {{ uploading ? `上传中 ${progress}%` : '上传' }}
  </button>
  <progress v-if="uploading" :value="progress" max="100" />
</template>
```

### 错误3：上传失败没有重试机制

```vue
<script setup>
// ❌ 上传失败后用户只能重新选择文件再上传
const uploadFile = async (file) => {
  await api.upload(file)  // 如果网络抖动失败，用户得重新操作
}

// ✅ 提供重试按钮
const uploadFile = async (file, retryCount = 0) => {
  try {
    await api.upload(file)
  } catch (err) {
    if (retryCount < 3) {
      // 自动重试最多3次
      await uploadFile(file, retryCount + 1)
    } else {
      // 重试失败，提示用户手动重试
      showError('上传失败，请点击重试')
    }
  }
}
</script>
```

### 错误4：没有限制同时上传的文件数量

```vue
<script setup>
// ❌ 用户一次选了50张图片，同时发起50个请求，服务器可能崩溃
const handleFilesChange = (files) => {
  files.forEach(file => upload(file))  // 50个请求同时发出！
}

// ✅ 限制并发数量，比如同时最多3个上传请求
const handleFilesChange = async (files) => {
  for (let i = 0; i < files.length; i += 3) {
    const batch = files.slice(i, i + 3)
    await Promise.all(batch.map(file => upload(file)))
  }
}
</script>
```

---

## 代码审查与改进建议

- [代码重复] 三个上传组件中uploadUrl、headers计算逻辑、handleBeforeUpload校验逻辑、handleRemove删除逻辑、updateModelValue转换逻辑几乎完全相同，应抽取为useFileUpload(config) composable
