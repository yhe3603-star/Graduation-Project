<template>
  <div class="image-uploader">
    <div class="image-list">
      <draggable
        v-model="imageList"
        item-key="url"
        class="image-grid"
        :class="{ 'single': !multiple }"
        handle=".drag-handle"
        animation="200"
        @end="updateModelValue"
      >
        <template #item="{ element, index }">
          <div class="image-item">
            <el-image
              :src="element.url"
              fit="cover"
              class="image-preview"
              :preview-src-list="[element.url]"
              :initial-index="0"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="image-actions">
              <el-icon
                class="drag-handle"
                title="拖拽排序"
              >
                <Rank />
              </el-icon>
              <el-icon
                class="delete-btn"
                title="删除"
                @click="handleRemove(index)"
              >
                <Delete />
              </el-icon>
            </div>
            <div
              v-if="showName"
              class="image-name"
            >
              {{ element.name }}
            </div>
          </div>
        </template>
      </draggable>

      <el-upload
        v-show="!limitReached"
        ref="uploadRef"
        :action="uploadUrl"
        :headers="headers"
        :data="{ category }"
        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
:disabled="disabled || uploading" class="image-upload-trigger"
      >
        <div
          class="upload-placeholder"
          :class="{ 'is-uploading': uploading }"
        >
          <el-progress
            v-if="uploading"
            type="circle"
            :percentage="uploadProgress"
            :width="50"
          />
          <template v-else>
            <el-icon class="upload-icon">
              <Plus />
            </el-icon>
            <span class="upload-text">上传图片</span>
          </template>
        </div>
      </el-upload>
    </div>
    <div
      v-if="showTip"
      class="upload-tip"
    >
      {{ tipText }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus, Picture, Delete, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { useFileUpload } from '@/composables/useFileUpload'

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  category: { type: String, default: 'plants' },
  limit: { type: Number, default: 9 },
  multiple: { type: Boolean, default: true },
  maxSize: { type: Number, default: 10 },
  disabled: { type: Boolean, default: false },
  showName: { type: Boolean, default: false },
  showTip: { type: Boolean, default: true },
  replaceConfirm: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change', 'success', 'error', 'remove'])

const uploadRef = ref(null)

const {
  fileList: imageList,
  uploading,
  uploadProgress,
  uploadUrl,
  headers,
  limitReached,
  tipText,
  handleBeforeUpload,
  handleSuccess,
  handleError,
  handleRemove,
  updateModelValue,
  clearFiles: clearImages,
} = useFileUpload({
  type: 'image',
  extensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'],
  extensionLabel: 'jpg/jpeg/png/gif/bmp/webp',
  uploadPath: '/upload/image',
  simulateProgress: true,
  props,
  emit,
})

defineExpose({ clearImages, getImages: () => imageList.value })
</script>

<style scoped>
.image-uploader { width: 100%; }
.image-list { display: flex; flex-wrap: wrap; gap: 12px; }
.image-grid { display: contents; }
.image-grid.single { display: flex; }
.image-item { position: relative; width: 120px; height: 120px; border-radius: 8px; overflow: hidden; border: 1px solid #dcdfe6; background: #f5f7fa; }
.image-preview { width: 100%; height: 100%; }
.image-error { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #909399; font-size: 30px; }
.image-actions { position: absolute; top: 0; right: 0; display: flex; gap: 4px; padding: 4px; background: rgba(0, 0, 0, 0.5); border-radius: 0 0 0 8px; opacity: 0; transition: opacity 0.2s; }
.image-item:hover .image-actions { opacity: 1; }
.image-actions .el-icon { color: var(--text-inverse); cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s; }
.image-actions .el-icon:hover { background: rgba(255, 255, 255, 0.2); }
.drag-handle { cursor: move; }
.image-name { position: absolute; bottom: 0; left: 0; right: 0; padding: 4px 8px; background: rgba(0, 0, 0, 0.5); color: var(--text-inverse); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.image-upload-trigger { width: 120px; height: 120px; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 120px; height: 120px; border: 1px dashed #dcdfe6; border-radius: 8px; background: #fafafa; cursor: pointer; transition: all 0.2s; }
.upload-placeholder:hover { border-color: #409eff; background: #ecf5ff; }
.upload-placeholder.is-uploading { cursor: not-allowed; }
.upload-icon { font-size: 28px; color: #909399; margin-bottom: 8px; }
.upload-text { font-size: 12px; color: #909399; }
.upload-tip { margin-top: 8px; color: #909399; font-size: 12px; }
</style>
