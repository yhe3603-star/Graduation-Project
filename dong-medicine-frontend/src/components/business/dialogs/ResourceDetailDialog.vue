<template>
  <el-dialog 
    :model-value="visible" 
    :title="resource?.title" 
    width="min(900px, 95vw)"
    destroy-on-close
    class="resource-detail-dialog"
    @update:model-value="handleDialogClose"
  >
    <div class="resource-content">
      <div class="preview-header">
        <el-tag :type="TAG_TYPES[getFileInfo(resource).type] || 'info'">
          {{ TYPE_NAMES[getFileInfo(resource).type] || '其他' }}
        </el-tag>
        <span
          v-if="fileExt"
          class="file-ext"
        >{{ fileExt.toUpperCase() }}</span>
        <span
          v-if="getFileInfo(resource).size"
          class="file-size"
        >{{ formatSize(getFileInfo(resource).size) }}</span>
      </div>
      
      <div class="preview-body">
        <VideoPlayer 
          v-if="isVideo" 
          ref="videoPlayerRef"
          :videos="[resourceUrl]" 
          :auto-play="true"
          height="450px" 
        />
        <ImageCarousel
          v-else-if="isImage"
          :images="[resourceUrl]"
          :auto-play="false"
          height="450px"
        />
        <div
          v-else-if="isDocument"
          class="document-preview-wrapper"
        >
          <DocumentPreview 
            inline
            :document="documentInfo"
            @download="$emit('download')"
          />
        </div>
        <div
          v-else
          class="preview-unavailable"
        >
          <el-icon :size="64">
            <Document />
          </el-icon>
          <p>{{ TYPE_NAMES[getFileInfo(resource).type] || '其他' }}文件暂不支持在线预览</p>
          <p class="preview-tip">
            请下载后查看
          </p>
        </div>
      </div>

      <div
        v-if="resource?.description"
        class="preview-info"
      >
        <h4>资源描述</h4>
        <p>{{ resource.description }}</p>
      </div>

      <div
        v-if="documentList.length > 0"
        class="related-documents"
      >
        <h4>相关文档</h4>
        <DocumentList 
          :documents="documentList" 
          :loading="documentsLoading"
          @document-click="handleDocumentClick"
        />
      </div>
    </div>

    <template #footer>
      <div class="preview-footer">
        <div class="preview-stats">
          <span><el-icon><View /></el-icon> {{ resource?.viewCount || 0 }} 次浏览</span>
          <span><el-icon><Download /></el-icon> {{ resource?.downloadCount || 0 }} 次下载</span>
        </div>
        <div class="preview-actions">
          <el-button
            :type="isFavorited ? 'warning' : 'default'"
            @click="$emit('toggle-favorite')"
          >
            <el-icon><Star /></el-icon>{{ isFavorited ? '取消收藏' : '收藏' }}
          </el-button>
          <el-button
            type="primary"
            @click="$emit('download')"
          >
            <el-icon><Download /></el-icon>下载资源
          </el-button>
          <el-button @click="handleDialogClose(false)">
            关闭
          </el-button>
        </div>
      </div>
    </template>

    <DocumentPreview 
      v-model="relatedDocPreviewVisible"
      :document="relatedDocPreview"
      @download="handleDocumentDownload"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { Document, View, Download, Star } from '@element-plus/icons-vue';
import VideoPlayer from '@/components/business/media/VideoPlayer.vue';
import ImageCarousel from '@/components/business/media/ImageCarousel.vue';
import DocumentList from '@/components/business/media/DocumentList.vue';
import DocumentPreview from '@/components/business/media/DocumentPreview.vue';
import request from '@/utils/request';

const TYPE_NAMES = { video: '视频', document: '文档', image: '图片' };
const TAG_TYPES = { video: 'danger', document: 'primary', image: 'success' };
const FILE_EXTS = {
  video: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'],
  document: ['docx', 'doc', 'pdf', 'pptx', 'ppt', 'xlsx', 'xls', 'txt'],
  image: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
};

const props = defineProps({
  visible: { type: Boolean, default: false },
  resource: { type: Object, default: null },
  isFavorited: { type: Boolean, default: false }
});

const emit = defineEmits(['update:visible', 'toggle-favorite', 'download']);

const documentList = ref([]);
const documentsLoading = ref(false);
const relatedDocPreviewVisible = ref(false);
const relatedDocPreview = ref(null);
const videoPlayerRef = ref(null);

const getFileInfo = (resource) => {
  if (!resource || !resource.files) return { url: '', type: 'document', size: 0 };
  try {
    const files = typeof resource.files === 'string' ? JSON.parse(resource.files) : resource.files;
    if (Array.isArray(files) && files.length > 0) {
      const firstFile = files[0];
      return {
        url: firstFile.path || firstFile.url || '',
        type: firstFile.type || 'document',
        size: firstFile.size || 0
      };
    }
  } catch {}
  return { url: '', type: 'document', size: 0 };
};

const resourceUrl = computed(() => {
  const url = getFileInfo(props.resource).url;
  if (!url) return '';
  return url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/') ? url : `/${url}`;
});

const fileExt = computed(() => getFileInfo(props.resource).url?.split('.').pop()?.toLowerCase() || '');
const isVideo = computed(() => FILE_EXTS.video.includes(fileExt.value) || getFileInfo(props.resource).type === 'video');
const isImage = computed(() => FILE_EXTS.image.includes(fileExt.value) || getFileInfo(props.resource).type === 'image');
const isDocument = computed(() => FILE_EXTS.document.includes(fileExt.value) || getFileInfo(props.resource).type === 'document');

const documentInfo = computed(() => ({
  url: resourceUrl.value,
  type: fileExt.value,
  name: props.resource?.title || '文档'
}));

const formatSize = (bytes) => {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let size = bytes, i = 0;
  while (size >= 1024 && i < units.length - 1) { size /= 1024; i++; }
  return `${size.toFixed(1)} ${units[i]}`;
};

const getFileName = (path) => path?.split('/').pop() || path || '';
const getFileType = (path) => {
  const ext = path?.split('.').pop()?.toLowerCase();
  const typeMap = { pdf: 'pdf', doc: 'word', docx: 'word', xls: 'excel', xlsx: 'excel', ppt: 'ppt', pptx: 'ppt', txt: 'txt' };
  return typeMap[ext] || 'other';
};

const loadDocuments = () => {
  documentsLoading.value = true;
  try {
    const docs = props.resource?.documents;
    const parsed = Array.isArray(docs) ? docs : (typeof docs === 'string' && docs ? JSON.parse(docs) : []);
    documentList.value = parsed.map(path => ({
      fileName: getFileName(path),
      filePath: path,
      fileType: getFileType(path),
      url: path,
      type: getFileType(path)
    }));
  } catch (e) {
    console.warn('解析资源文档列表失败:', e);
    documentList.value = [];
  } finally {
    documentsLoading.value = false;
  }
};

const handleDialogClose = (newVisible) => {
  if (!newVisible && videoPlayerRef.value) videoPlayerRef.value.pause();
  emit('update:visible', newVisible);
};

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadDocuments();
    // Record browse history
    if (props.resource?.id) {
      request.post('/browse-history/record', null, {
        params: { targetType: 'resource', targetId: props.resource.id }
      }).catch(() => {});
    }
  } else if (videoPlayerRef.value) {
    videoPlayerRef.value.pause();
  }
});

const handleDocumentClick = (doc) => {
  relatedDocPreview.value = doc;
  relatedDocPreviewVisible.value = true;
};

const handleDocumentDownload = (doc) => {
  if (doc?.filePath || doc?.url) {
    const link = document.createElement('a');
    link.href = doc.filePath || doc.url;
    link.download = doc.originalFileName || doc.fileName || doc.name || 'document';
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};
</script>

<style scoped>
@import '@/styles/dialog-common.css';

.preview-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border-light); }
.file-ext { font-size: 12px; color: #666; background: #f0f0f0; padding: 2px 8px; border-radius: 4px; font-weight: 500; }
.file-size { font-size: 13px; color: var(--text-muted); }
.preview-body { min-height: 400px; display: flex; align-items: center; justify-content: center; background: #f9f9f9; border-radius: 8px; overflow: hidden; }
.document-preview-wrapper { width: 100%; height: 100%; }
.preview-unavailable { text-align: center; color: var(--text-muted); padding: 40px; }
.preview-unavailable p { margin: 12px 0 0; }
.preview-tip { font-size: 13px; color: var(--text-light); }
.preview-info { margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border-light); }
.preview-info h4 { margin: 0 0 8px; font-size: 14px; color: #333; }
.preview-info p { margin: 0; font-size: 13px; color: #666; line-height: 1.6; }
.related-documents { margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border-light); }
.related-documents h4 { margin: 0 0 12px; font-size: 14px; color: #333; }
.preview-footer { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.preview-stats { display: flex; gap: 20px; color: var(--text-muted); font-size: 13px; }
.preview-stats span { display: flex; align-items: center; gap: 4px; }
.preview-actions { display: flex; gap: 8px; }

@media (max-width: 768px) {
  .preview-header {
    flex-wrap: wrap;
    gap: 8px;
    padding-bottom: 8px;
  }
  
  .file-ext, .file-size {
    font-size: 11px;
  }
  
  .preview-body {
    min-height: 280px;
  }
  
  .preview-unavailable {
    padding: 24px;
  }
  
  .preview-unavailable .el-icon {
    font-size: 48px;
  }
  
  .preview-unavailable p {
    font-size: 13px;
  }
  
  .preview-tip {
    font-size: 12px;
  }
  
  .preview-info, .related-documents {
    margin-top: 12px;
    padding-top: 12px;
  }
  
  .preview-info h4, .related-documents h4 {
    font-size: 13px;
  }
  
  .preview-info p {
    font-size: 12px;
  }
  
  .preview-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .preview-stats {
    width: 100%;
    justify-content: center;
    gap: 16px;
    font-size: 12px;
  }
  
  .preview-actions {
    width: 100%;
    flex-direction: column;
    gap: 8px;
  }
  
  .preview-actions .el-button {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .preview-body {
    min-height: 220px;
  }
  
  .preview-stats {
    gap: 12px;
    font-size: 11px;
  }
}
</style>
