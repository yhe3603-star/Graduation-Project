<template>
  <el-dialog
    :model-value="visible"
    title="资源详情"
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ resource?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="标题">
        {{ resource?.title }}
      </el-descriptions-item>
      <el-descriptions-item label="分类">
        {{ resource?.category }}
      </el-descriptions-item>
      <el-descriptions-item label="类型">
        <el-tag :type="getFileTypeTagType(fileList[0]?.type)">
          {{ getFileTypeText(fileList[0]?.type) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="文件大小">
        {{ formatFileSize(fileList[0]?.size) }}
      </el-descriptions-item>
      <el-descriptions-item label="热度">
        {{ resource?.popularity || 0 }}
      </el-descriptions-item>
      <el-descriptions-item label="下载次数">
        {{ resource?.downloadCount }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(resource?.createdAt) }}
      </el-descriptions-item>
      <el-descriptions-item
        label="描述"
        :span="2"
      >
        {{ resource?.description || '无' }}
      </el-descriptions-item>
    </el-descriptions>

    <div
      v-if="fileList.length > 0"
      class="media-section"
    >
      <el-divider content-position="left">
        资源预览
      </el-divider>

      <div
        v-for="(file, index) in fileList"
        :key="index"
      >
        <div
          v-if="file.type === 'video'"
          class="video-grid"
        >
          <div class="video-item">
            <video
              :src="file.url || file.path"
              controls
              class="preview-video"
              preload="metadata"
            >您的浏览器不支持视频播放</video>
          </div>
        </div>

        <div
          v-else-if="file.type === 'image'"
          class="image-grid"
        >
          <div class="image-item">
            <el-image
              :src="file.url || file.path"
              fit="cover"
              class="preview-image"
              :preview-src-list="[file.url || file.path]"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon><span>加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <div
          v-else-if="canPreviewWithKkFileView(file)"
          class="pdf-container"
        >
          <div
            v-if="previewLoading"
            class="preview-loading"
          >
            <el-icon
              class="is-loading"
              :size="48"
            >
              <Loading />
            </el-icon>
            <p>正在加载预览...</p>
          </div>
          <iframe
            v-show="!previewLoading"
            :src="getKkFileViewUrl(file)"
            class="pdf-viewer"
            @load="onPreviewLoad"
          />
        </div>

        <div
          v-else
          class="document-list"
        >
          <div class="document-item">
            <div class="document-icon">
              <el-icon
                :size="32"
                :color="getFileColor(file.type)"
              >
                <component :is="getFileIcon(file.type)" />
              </el-icon>
            </div>
            <div class="document-info">
              <span
                class="document-name"
                :title="file.name || file.originalFileName || getFileName(file.path)"
              >{{ file.name || file.originalFileName || getFileName(file.path) }}</span>
              <span
                v-if="file.size"
                class="document-size"
              >{{ formatFileSize(file.size) }}</span>
            </div>
            <div class="document-actions">
              <el-button
                type="primary"
                size="small"
                @click="handleDownload(file)"
              >
                <el-icon><Download /></el-icon>下载
              </el-button>
            </div>
          </div>
          <p class="preview-tip">
            该文件类型暂不支持在线预览，请下载后查看
          </p>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button
        v-if="fileList.length > 0"
        @click="handleDownload(fileList[0])"
      >
        <el-icon><Download /></el-icon>下载文件
      </el-button>
      <el-button
        type="primary"
        @click="$emit('update:visible', false)"
      >
        关闭
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { Picture, Download, Loading } from '@element-plus/icons-vue';
import { formatTime, formatFileSize, getFileTypeTagType, getFileTypeText } from '@/utils/adminUtils';
import { getFileName, getFileIcon, getFileColor } from '@/utils';
import { parseMediaList, normalizeUrl } from '@/utils/media';

const KKFILEVIEW_EXTENSIONS = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'];
const KKFILEVIEW_SERVER = import.meta.env.VITE_KKFILEVIEW_URL || '/kkfileview';
const KKFILEVIEW_FILE_HOST = import.meta.env.VITE_KKFILEVIEW_FILE_HOST || '';

const props = defineProps({
  visible: { type: Boolean, default: false },
  resource: { type: Object, default: null }
});

defineEmits(['update:visible']);

const previewLoading = ref(true);

const fileList = computed(() => {
  if (!props.resource?.files) return [];
  try {
    const files = parseMediaList(props.resource.files);
    return files.map(file => ({
      ...file,
      url: normalizeUrl(file.url || file.path),
      type: file.type || 'document'
    }));
  } catch {
    return [];
  }
});

const getFileExtension = (file) => {
  if (!file) return '';
  const path = file.path || file.url || '';
  return path.split('.').pop()?.toLowerCase() || '';
};

const canPreviewWithKkFileView = (file) => {
  const ext = getFileExtension(file);
  return KKFILEVIEW_EXTENSIONS.includes(ext);
};

const getFullFileUrl = (relativeUrl) => {
  if (!relativeUrl) return '';
  if (relativeUrl.startsWith('http://') || relativeUrl.startsWith('https://')) {
    return relativeUrl;
  }
  if (KKFILEVIEW_FILE_HOST) {
    return KKFILEVIEW_FILE_HOST + (relativeUrl.startsWith('/') ? relativeUrl : '/' + relativeUrl);
  }
  const origin = window.location.origin;
  return origin + (relativeUrl.startsWith('/') ? relativeUrl : '/' + relativeUrl);
};

const getKkFileViewUrl = (file) => {
  if (!file) return '';
  const url = normalizeUrl(file.url || file.path);
  const fullUrl = getFullFileUrl(url);
  const base64Url = btoa(unescape(encodeURIComponent(fullUrl)));
  return `${KKFILEVIEW_SERVER}/onlinePreview?url=${base64Url}`;
};

const onPreviewLoad = () => {
  previewLoading.value = false;
};

watch(() => props.visible, (newVal) => {
  if (newVal) {
    previewLoading.value = true;
  }
});

const handleDownload = (file) => {
  if (!file?.path && !file?.url) return;
  const link = document.createElement('a');
  link.href = normalizeUrl(file.url || file.path);
  link.download = file.name || file.originalFileName || getFileName(file.path) || 'resource';
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
</script>

<style scoped>
@import '@/styles/media-common.css';

.media-section { margin-top: 20px; }
.pdf-container { width: 100%; border-radius: 8px; overflow: hidden; border: 1px solid #e8e8e8; position: relative; min-height: 400px; }
.pdf-viewer { width: 100%; height: 500px; border: none; }
.preview-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; color: #999; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: #f9f9f9; z-index: 10; }
.preview-loading p { margin: 0; font-size: 14px; }
.preview-tip { font-size: 13px; color: #909399; margin-top: 12px; text-align: center; }

@media (max-width: 768px) {
  .pdf-viewer { height: 350px; }
  .pdf-container { min-height: 300px; }
}
</style>
