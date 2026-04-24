<template>
  <el-dialog 
    :model-value="visible" 
    :title="knowledge?.title" 
    width="min(800px, 90vw)"
    class="knowledge-detail-dialog"
    @update:model-value="handleDialogClose"
  >
    <div class="dialog-content">
      <div class="knowledge-header">
        <div class="header-tags">
          <el-tag effect="light">
            {{ knowledge?.therapyCategory || '传统疗法' }}
          </el-tag>
          <el-tag
            v-if="knowledge?.diseaseCategory"
            type="info"
            effect="light"
          >
            {{ knowledge?.diseaseCategory }}
          </el-tag>
          <el-tag
            v-if="knowledge?.herbCategory"
            type="warning"
            effect="light"
          >
            {{ knowledge?.herbCategory }}
          </el-tag>
        </div>
        <div class="header-stats">
          <span class="stat-item"><el-icon><View /></el-icon>{{ knowledge?.viewCount || 0 }}</span>
          <span class="stat-item"><el-icon><Star /></el-icon>{{ knowledge?.favoriteCount || 0 }}</span>
        </div>
      </div>

      <el-divider />

      <div class="knowledge-sections">
        <section class="info-section">
          <el-descriptions
            :column="3"
            border
            size="small"
            class="responsive-descriptions"
          >
            <el-descriptions-item label="疗法分类">
              {{ knowledge?.therapyCategory || '暂无' }}
            </el-descriptions-item>
            <el-descriptions-item label="疾病分类">
              {{ knowledge?.diseaseCategory || '暂无' }}
            </el-descriptions-item>
            <el-descriptions-item label="药材分类">
              {{ knowledge?.herbCategory || '暂无' }}
            </el-descriptions-item>
          </el-descriptions>
        </section>

        <section
          v-if="knowledge?.content"
          class="content-section"
        >
          <h3 class="section-title">
            <el-icon><Document /></el-icon>内容介绍
          </h3>
          <div class="content-box">
            {{ knowledge.content }}
          </div>
        </section>

        <section
          v-if="knowledge?.formula"
          class="formula-section"
        >
          <h3 class="section-title">
            <el-icon><Memo /></el-icon>配方组成
          </h3>
          <div class="formula-box">
            <pre>{{ knowledge.formula }}</pre>
          </div>
        </section>

        <section
          v-if="relatedPlants.length"
          class="related-plants-section"
        >
          <h3 class="section-title">
            <el-icon><Cherry /></el-icon>关联药材
          </h3>
          <div class="related-plants-grid">
            <div
              v-for="plant in relatedPlants"
              :key="plant.id"
              class="related-plant-card"
              @click="goToPlant(plant.id)"
            >
              <div class="plant-avatar">
                <el-image
                  v-if="getPlantImage(plant)"
                  :src="getPlantImage(plant)"
                  fit="cover"
                  lazy
                >
                  <template #error>
                    <div class="plant-avatar-placeholder">
                      <el-icon><Cherry /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div
                  v-else
                  class="plant-avatar-placeholder"
                >
                  <el-icon><Cherry /></el-icon>
                </div>
              </div>
              <div class="plant-info">
                <div class="plant-name">
                  {{ plant.nameCn }}
                </div>
                <div
                  v-if="plant.nameDong"
                  class="plant-dong-name"
                >
                  {{ plant.nameDong }}
                </div>
                <el-tag
                  size="small"
                  effect="plain"
                  class="plant-category-tag"
                >
                  {{ plant.category || '药材' }}
                </el-tag>
              </div>
            </div>
          </div>
        </section>

        <section
          v-if="knowledge?.usageMethod"
          class="usage-section"
        >
          <h3 class="section-title">
            <el-icon><FirstAidKit /></el-icon>用法说明
          </h3>
          <div class="usage-box">
            <p
              v-for="(line, index) in parseLines(knowledge.usageMethod)"
              :key="index"
            >
              {{ line }}
            </p>
          </div>
        </section>

        <section
          v-if="parsedSteps.length"
          class="steps-section"
        >
          <h3 class="section-title">
            <el-icon><List /></el-icon>操作步骤
          </h3>
          <div class="steps-flowchart">
            <div
              v-for="(step, index) in parsedSteps"
              :key="index"
              class="step-node"
            >
              <div class="step-connector-area">
                <div class="step-dot">
                  <span class="step-num">{{ index + 1 }}</span>
                </div>
                <div
                  v-if="index < parsedSteps.length - 1"
                  class="step-line"
                />
              </div>
              <div class="step-body">
                <div
                  v-if="step.title"
                  class="step-title"
                >
                  {{ step.title }}
                </div>
                <div class="step-desc">
                  {{ step.content || step }}
                </div>
              </div>
            </div>
          </div>
        </section>

        <section
          v-if="imageList.length"
          class="images-section"
        >
          <h3 class="section-title">
            <el-icon><Picture /></el-icon>步骤示意图
          </h3>
          <ImageCarousel
            :images="imageList"
            height="280px"
          />
        </section>

        <section class="media-section">
          <h3 class="section-title">
            <el-icon><Film /></el-icon>相关资料
          </h3>
          <el-tabs
            v-model="activeMediaTab"
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
                height="280px" 
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
        </section>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          :type="isFavorited ? 'warning' : 'default'"
          class="favorite-btn"
          :class="{ favorited: isFavorited }"
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
import { ref, computed, watch, inject } from 'vue';
import { useRouter } from 'vue-router';
import { View, Star, Document, Memo, FirstAidKit, List, Film, VideoPlay, Cherry, Picture } from '@element-plus/icons-vue';
import VideoPlayer from '@/components/business/media/VideoPlayer.vue';
import DocumentList from '@/components/business/media/DocumentList.vue';
import DocumentPreview from '@/components/business/media/DocumentPreview.vue';
import ImageCarousel from '@/components/business/media/ImageCarousel.vue';
import { parseMediaList, parseDocumentList, downloadDocument } from '@/utils';

const props = defineProps({
  visible: { type: Boolean, default: false },
  knowledge: { type: Object, default: null },
  isFavorited: { type: Boolean, default: false }
});

const emit = defineEmits(['update:visible', 'toggle-favorite']);
const router = useRouter();
const request = inject('request');

const activeMediaTab = ref('video');
const documentList = ref([]);
const documentsLoading = ref(false);
const previewVisible = ref(false);
const previewDoc = ref(null);
const videoPlayerRef = ref(null);
const relatedPlants = ref([]);

const parsedSteps = computed(() => {
  const steps = props.knowledge?.steps;
  if (!steps) return [];
  try {
    const parsed = typeof steps === 'string' ? JSON.parse(steps) : steps;
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return steps.split('\n').filter(s => s.trim());
  }
});

const imageList = computed(() => {
  const images = props.knowledge?.images;
  if (!images) return [];
  const items = parseMediaList(images);
  return items.map(item => typeof item === 'string' ? item : item.url);
});

const parseLines = (text) => text?.split('\n').filter(line => line.trim()) || [];
const videoList = computed(() => parseMediaList(props.knowledge?.videoUrl));
const isVideoTab = computed(() => activeMediaTab.value === 'video');

const getPlantImage = (plant) => {
  if (!plant.images) return null;
  try {
    const imgs = typeof plant.images === 'string' ? JSON.parse(plant.images) : plant.images;
    if (Array.isArray(imgs) && imgs.length > 0) {
      return imgs[0].url || imgs[0].path || imgs[0];
    }
  } catch {}
  return null;
};

const loadRelatedPlants = async () => {
  const rp = props.knowledge?.relatedPlants;
  if (!rp) { relatedPlants.value = []; return; }
  try {
    const ids = typeof rp === 'string' ? JSON.parse(rp) : rp;
    if (!Array.isArray(ids) || ids.length === 0) { relatedPlants.value = []; return; }
    const res = await request.post('/plants/batch', ids);
    relatedPlants.value = res.data || res || [];
  } catch {
    relatedPlants.value = [];
  }
};

const goToPlant = (id) => {
  handleDialogClose(false);
  router.push(`/plants?id=${id}`);
};

const loadDocuments = () => {
  documentsLoading.value = true;
  try {
    documentList.value = parseDocumentList(props.knowledge?.documents);
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
    activeMediaTab.value = videoList.value.length > 0 ? 'video' : 'document';
    loadDocuments();
    loadRelatedPlants();
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

.knowledge-detail-dialog :deep(.el-dialog__body) {
  overflow-x: hidden;
}

.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.knowledge-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-x: hidden;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--dong-blue, #1A5276);
}

.info-section {
  overflow-x: auto;
}

.formula-box {
  padding: 20px;
  background: linear-gradient(135deg, #e8f4f8, #d4e8ed);
  border-radius: 12px;
  border-left: 4px solid var(--dong-blue, #1A5276);
  overflow-x: auto;
}

.formula-box pre {
  margin: 0;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
}

.related-plants-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.related-plant-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f0f9f4, #e8f5e9);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.related-plant-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.15);
  border-color: var(--dong-green, #2E7D32);
}

.plant-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #c8e6c9;
}

.plant-avatar .el-image {
  width: 100%;
  height: 100%;
}

.plant-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dong-green, #2E7D32);
  font-size: 20px;
  background: linear-gradient(135deg, #c8e6c9, #a5d6a7);
}

.plant-info {
  flex: 1;
  min-width: 0;
}

.plant-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plant-dong-name {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plant-category-tag {
  margin-top: 4px;
}

.steps-flowchart {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.step-node {
  display: flex;
  gap: 16px;
  min-height: 56px;
}

.step-connector-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 36px;
  flex-shrink: 0;
}

.step-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--dong-blue, #1A5276), #2980b9);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(26, 82, 118, 0.3);
}

.step-num {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.step-line {
  width: 2px;
  flex: 1;
  min-height: 20px;
  background: linear-gradient(to bottom, var(--dong-blue, #1A5276), #bdc3c7);
  margin: 4px 0;
}

.step-body {
  flex: 1;
  padding: 6px 0 20px 0;
}

.step-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.step-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.usage-box {
  padding: 16px;
  background: #fff8e6;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.usage-box p {
  margin: 0 0 8px 0;
  line-height: 1.8;
  color: var(--text-secondary);
  word-break: break-word;
}

.usage-box p:last-child {
  margin-bottom: 0;
}

.images-section {
  margin-top: 4px;
}

.media-section {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .knowledge-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .knowledge-sections {
    gap: 16px;
  }
  
  .section-title {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .info-section {
    overflow-x: visible;
  }
  
  .formula-box {
    padding: 12px;
  }
  
  .formula-box pre {
    font-size: 12px;
    line-height: 1.6;
  }
  
  .related-plants-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 8px;
  }
  
  .related-plant-card {
    padding: 8px 10px;
  }
  
  .plant-avatar {
    width: 36px;
    height: 36px;
  }
  
  .step-node {
    gap: 10px;
  }
  
  .step-dot {
    width: 30px;
    height: 30px;
  }
  
  .step-num {
    font-size: 12px;
  }
  
  .step-connector-area {
    width: 30px;
  }
  
  .step-body {
    padding: 4px 0 14px 0;
  }
  
  .usage-box {
    padding: 12px;
  }
  
  .usage-box p {
    font-size: 13px;
    line-height: 1.7;
  }
  
  .media-section {
    margin-top: 4px;
  }
}

@media (max-width: 480px) {
  .knowledge-header {
    gap: 8px;
  }
  
  .section-title {
    font-size: 13px;
  }
  
  .formula-box {
    padding: 10px;
  }
  
  .formula-box pre {
    font-size: 11px;
  }
  
  .related-plants-grid {
    grid-template-columns: 1fr;
  }
  
  .usage-box {
    padding: 10px;
  }
  
  .usage-box p {
    font-size: 12px;
  }
}
</style>
