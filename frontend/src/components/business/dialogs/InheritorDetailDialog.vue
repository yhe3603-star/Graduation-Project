<template>
  <el-dialog 
    :model-value="visible" 
    :title="inheritor?.name" 
    width="min(800px, 90vw)"
    class="inheritor-detail-dialog"
    @update:model-value="handleDialogClose"
  >
    <div class="dialog-content">
      <div class="header-section">
        <div class="avatar-section">
          <div class="avatar-circle">
            {{ (inheritor?.name || '传').charAt(0) }}
          </div>
          <div class="level-badge">
            <el-tag
              :type="LEVEL_TYPES[inheritor?.level] || 'info'"
              effect="dark"
              size="large"
            >
              {{ inheritor?.level }}
            </el-tag>
          </div>
        </div>
        <div class="info-section">
          <h2 class="inheritor-name">
            {{ inheritor?.name }}
          </h2>
          <p class="specialty">
            {{ inheritor?.specialties }}
          </p>
          <div class="meta-info">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              从业 {{ inheritor?.experienceYears || 0 }} 年
            </span>
          </div>
        </div>
      </div>

      <el-divider />

      <el-descriptions
        :column="1"
        border
        class="detail-section"
      >
        <el-descriptions-item label="个人简介">
          {{ inheritor?.bio || '暂无简介' }}
        </el-descriptions-item>
        <el-descriptions-item label="代表案例">
          {{ inheritor?.representativeCases || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="荣誉资质">
          {{ inheritor?.honors || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="media-section">
        <el-divider content-position="left">
          <el-icon><Film /></el-icon>
          媒体资料
        </el-divider>

        <el-tabs
          v-model="activeMediaTab"
          class="media-tabs"
          @tab-change="handleTabChange"
        >
          <el-tab-pane name="video">
            <template #label>
              <span class="tab-label"><el-icon><VideoPlay /></el-icon>视频</span>
            </template>
            <VideoPlayer 
              ref="videoPlayerRef"
              :videos="videoList" 
              :auto-play="isVideoTab"
              height="400px" 
            />
          </el-tab-pane>

          <el-tab-pane name="image">
            <template #label>
              <span class="tab-label"><el-icon><Picture /></el-icon>图片</span>
            </template>
            <ImageCarousel
              :images="imageList"
              :auto-play="true"
              height="320px"
            />
          </el-tab-pane>

          <el-tab-pane name="document">
            <template #label>
              <span class="tab-label"><el-icon><Document /></el-icon>文档</span>
            </template>
            <DocumentList 
              :documents="documentList" 
              :loading="documentsLoading"
              @document-click="handleDocumentClick"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          :type="isFavorited ? 'warning' : 'default'"
          @click="$emit('toggle-favorite')"
        >
          <el-icon><Star /></el-icon>{{ isFavorited ? '取消收藏' : '收藏' }}
        </el-button>
        <el-button
          type="primary"
          @click="handleDialogClose(false)"
        >
          关闭
        </el-button>
      </div>
    </template>

    <DocumentPreview 
      v-model="previewVisible"
      :document="previewDoc"
      @download="handleDocumentDownload"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { Clock, Film, VideoPlay, Picture, Star, Document } from '@element-plus/icons-vue';
import VideoPlayer from '@/components/business/media/VideoPlayer.vue';
import ImageCarousel from '@/components/business/media/ImageCarousel.vue';
import DocumentList from '@/components/business/media/DocumentList.vue';
import DocumentPreview from '@/components/business/media/DocumentPreview.vue';
import { parseMediaList, parseDocumentList, downloadDocument } from '@/utils';

const LEVEL_TYPES = { '省级': 'warning', '自治区级': 'success', '州级': 'primary', '市级': 'primary', '县级': 'info' };

const props = defineProps({
  visible: { type: Boolean, default: false },
  inheritor: { type: Object, default: null },
  isFavorited: { type: Boolean, default: false }
});

const emit = defineEmits(['update:visible', 'toggle-favorite']);

const activeMediaTab = ref('video');
const documentList = ref([]);
const documentsLoading = ref(false);
const previewVisible = ref(false);
const previewDoc = ref(null);
const videoPlayerRef = ref(null);

const videoList = computed(() => parseMediaList(props.inheritor?.videos));
const imageList = computed(() => parseMediaList(props.inheritor?.images));
const isVideoTab = computed(() => activeMediaTab.value === 'video');

const loadDocuments = () => {
  documentsLoading.value = true;
  try {
    documentList.value = parseDocumentList(props.inheritor?.documents);
  } finally {
    documentsLoading.value = false;
  }
};

const handleTabChange = (tabName) => {
  if (tabName !== 'video' && videoPlayerRef.value) videoPlayerRef.value.pause();
};

const handleDialogClose = (newVisible) => {
  if (!newVisible && videoPlayerRef.value) videoPlayerRef.value.pause();
  emit('update:visible', newVisible);
};

watch(() => props.visible, (newVal) => {
  if (newVal) {
    activeMediaTab.value = videoList.value?.length > 0 ? 'video' : 'image';
    loadDocuments();
  } else if (videoPlayerRef.value) {
    videoPlayerRef.value.pause();
  }
});

const handleDocumentClick = (doc) => {
  previewDoc.value = doc;
  previewVisible.value = true;
};

const handleDocumentDownload = downloadDocument;
</script>

<style scoped>
@import '@/styles/dialog-common.css';

.inheritor-detail-dialog :deep(.el-dialog__body) {
  overflow-x: hidden;
}

.inheritor-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: var(--text-primary);
}

.specialty {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.meta-info {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.detail-section {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 16px;
  }
  
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .inheritor-name {
    font-size: 20px;
  }
  
  .specialty {
    font-size: 13px;
  }
  
  .meta-info {
    justify-content: center;
    gap: 12px;
  }
  
  .detail-section {
    margin-top: 12px;
  }
  
  .detail-section :deep(.el-descriptions__table) {
    display: block;
  }
  
  .detail-section :deep(.el-descriptions__row) {
    display: block;
    margin-bottom: 8px;
  }
  
  .detail-section :deep(.el-descriptions__cell) {
    display: block;
    width: 100%;
    padding: 8px 12px;
  }
}

@media (max-width: 480px) {
  .inheritor-name {
    font-size: 18px;
  }
  
  .specialty {
    font-size: 12px;
  }
}
</style>
