<template>
  <el-dialog 
    :model-value="visible" 
    :title="plant?.nameCn" 
    width="min(800px, 90vw)"
    class="plant-detail-dialog"
    @update:model-value="handleDialogClose"
  >
    <div class="dialog-content">
      <div class="plant-header">
        <div class="header-tags">
          <el-tag effect="light">
            {{ plant?.category }}
          </el-tag>
          <el-tag
            v-if="plant?.habitat"
            type="info"
            effect="light"
          >
            {{ plant?.habitat }}
          </el-tag>
        </div>
        <div class="header-stats">
          <span class="stat-item"><el-icon><View /></el-icon>{{ plant?.viewCount || 0 }}</span>
          <span class="stat-item"><el-icon><Star /></el-icon>{{ plant?.favoriteCount || 0 }}</span>
        </div>
      </div>

      <el-divider />

      <div class="media-section">
        <el-tabs
          v-model="activeTab"
          class="media-tabs"
          @tab-change="handleTabChange"
        >
          <el-tab-pane
            v-if="videoList.length > 0"
            name="video"
          >
            <template #label>
              <span class="tab-label"><el-icon><VideoPlay /></el-icon>视频</span>
            </template>
            <VideoPlayer 
              ref="videoPlayerRef"
              :videos="videoList" 
              :auto-play="isVideoTab"
              height="320px" 
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

      <div class="info-section">
        <el-descriptions
          :column="2"
          border
          size="small"
        >
          <el-descriptions-item label="中文名">
            {{ plant?.nameCn }}
          </el-descriptions-item>
          <el-descriptions-item label="侗语名">
            <div class="dong-name-row">
              <span>{{ plant?.nameDong || '暂无' }}</span>
              <HerbAudio
                v-if="plant?.nameDong"
                :plant-name="plant?.nameDong"
                :plant-id="plant?.id"
                :show-label="false"
                :icon-size="16"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="学名">
            {{ plant?.scientificName || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            <el-tag size="small">
              {{ plant?.category }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用法">
            {{ plant?.usageWay }}
          </el-descriptions-item>
          <el-descriptions-item label="产地">
            {{ plant?.habitat }}
          </el-descriptions-item>
          <el-descriptions-item
            label="功效"
            :span="2"
          >
            <span class="efficacy-text">{{ plant?.efficacy }}</span>
          </el-descriptions-item>
        </el-descriptions>
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
import { View, Star, Document, VideoPlay, Picture } from '@element-plus/icons-vue';
import VideoPlayer from '@/components/business/media/VideoPlayer.vue';
import ImageCarousel from '@/components/business/media/ImageCarousel.vue';
import DocumentList from '@/components/business/media/DocumentList.vue';
import DocumentPreview from '@/components/business/media/DocumentPreview.vue';
import { parseMediaList, parseDocumentList, downloadDocument } from '@/utils';
import HerbAudio from '@/components/business/media/HerbAudio.vue';
import request from '@/utils/request';

const props = defineProps({
  visible: { type: Boolean, default: false },
  plant: { type: Object, default: null },
  isFavorited: { type: Boolean, default: false }
});

const emit = defineEmits(['update:visible', 'toggle-favorite']);

const activeTab = ref('image');
const documentList = ref([]);
const documentsLoading = ref(false);
const previewVisible = ref(false);
const previewDoc = ref(null);
const videoPlayerRef = ref(null);

const imageList = computed(() => parseMediaList(props.plant?.images));
const videoList = computed(() => parseMediaList(props.plant?.videos));
const isVideoTab = computed(() => activeTab.value === 'video');

const loadDocuments = () => {
  documentsLoading.value = true;
  try {
    documentList.value = parseDocumentList(props.plant?.documents);
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
    activeTab.value = videoList.value.length > 0 ? 'video' : 'image';
    loadDocuments();
    if (videoPlayerRef.value) videoPlayerRef.value.switchToVideo(0);
    // Record browse history
    if (props.plant?.id) {
      request.post('/browse-history/record', null, {
        params: { targetType: 'plant', targetId: props.plant.id }
      }).catch(() => {});
    }
  } else if (videoPlayerRef.value) {
    videoPlayerRef.value.pause();
  }
});

watch(() => props.plant?.id, (newId, oldId) => {
  if (newId && newId !== oldId && props.visible) {
    activeTab.value = videoList.value.length > 0 ? 'video' : 'image';
    loadDocuments();
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

.plant-detail-dialog :deep(.el-dialog__body) {
  overflow-x: hidden;
}

.plant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.media-section {
  margin-bottom: 20px;
}

.info-section {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.dong-name-row {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.efficacy-text {
  line-height: 1.6;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .plant-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .media-section {
    margin-bottom: 16px;
  }
  
  .info-section {
    padding: 12px;
  }
  
  .info-section :deep(.el-descriptions) {
    display: block;
  }
  
  .info-section :deep(.el-descriptions__table) {
    display: block;
  }
  
  .info-section :deep(.el-descriptions__row) {
    display: block;
    margin-bottom: 8px;
  }
  
  .info-section :deep(.el-descriptions__cell) {
    display: block;
    width: 100%;
    padding: 8px 12px;
  }
  
  .efficacy-text {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .plant-header {
    gap: 8px;
  }
  
  .info-section {
    padding: 10px;
  }
}
</style>
