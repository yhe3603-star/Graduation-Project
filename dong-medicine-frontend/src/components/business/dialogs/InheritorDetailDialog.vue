<template>
  <BaseDetailDialog 
    :model-value="visible" 
    :title="inheritor?.name" 
    width="min(800px, 90vw)"
    dialog-class="inheritor-detail-dialog"
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

      <div class="timeline-section">
        <el-divider content-position="left">
          <el-icon><Clock /></el-icon>
          传承生涯
        </el-divider>
        <div v-if="timelineItems.length > 0" class="milestone-timeline">
          <div
            v-for="(item, index) in timelineItems"
            :key="index"
            class="timeline-item"
            :class="{ 'timeline-left': index % 2 === 0, 'timeline-right': index % 2 !== 0 }"
          >
            <div class="timeline-dot" :style="{ background: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="timeline-content">
              <div class="timeline-tag">
                <el-tag :type="item.tagType" size="small" effect="dark">
{{ item.phase }}
</el-tag>
                <span v-if="item.year" class="timeline-year">{{ item.year }}</span>
              </div>
              <p class="timeline-desc">
{{ item.description }}
</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无生涯记录" :image-size="80" />
      </div>

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
  </BaseDetailDialog>
</template>

<script setup>
import BaseDetailDialog from '@/components/base/BaseDetailDialog.vue'
import { ref, computed, watch } from 'vue';
import { Clock, Film, Medal, Reading, School, Star, Trophy, VideoPlay, Picture, Document } from '@element-plus/icons-vue';
import VideoPlayer from '@/components/business/media/VideoPlayer.vue';
import ImageCarousel from '@/components/business/media/ImageCarousel.vue';
import DocumentList from '@/components/business/media/DocumentList.vue';
import DocumentPreview from '@/components/business/media/DocumentPreview.vue';
import { parseMediaList, parseDocumentList, downloadDocument } from '@/utils';
import request from '@/utils/request';

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

const timelineItems = computed(() => {
  const inheritor = props.inheritor;
  if (!inheritor) return [];

  const bio = inheritor.bio || '';
  const experienceYears = inheritor.experienceYears || 0;
  const honors = inheritor.honors || '';
  const specialties = inheritor.specialties || '';

  const parsed = parseTimelineFromBio(bio);

  if (parsed.length > 0) {
    return parsed;
  }

  return generateGenericTimeline(inheritor.name, experienceYears, specialties, honors, bio);
});

function parseTimelineFromBio(bio) {
  if (!bio || !bio.trim()) return [];

  const phasePatterns = [
    { regex: /学习经历[:：]?\s*([\s\S]*?)(?=行医经历[:：]|荣誉奖项[:：]|传承贡献[:：]|$)/, phase: '学习经历', icon: School, color: '#409EFF', tagType: 'primary' },
    { regex: /行医经历[:：]?\s*([\s\S]*?)(?=荣誉奖项[:：]|传承贡献[:：]|$)/, phase: '行医经历', icon: Clock, color: '#67C23A', tagType: 'success' },
    { regex: /荣誉奖项[:：]?\s*([\s\S]*?)(?=传承贡献[:：]|$)/, phase: '荣誉奖项', icon: Trophy, color: '#E6A23C', tagType: 'warning' },
    { regex: /传承贡献[:：]?\s*([\s\S]*)/, phase: '传承贡献', icon: Medal, color: '#F56C6C', tagType: 'danger' }
  ];

  const items = [];
  const phaseMeta = {
    '学习经历': { icon: School, color: '#409EFF', tagType: 'primary' },
    '行医经历': { icon: Clock, color: '#67C23A', tagType: 'success' },
    '荣誉奖项': { icon: Trophy, color: '#E6A23C', tagType: 'warning' },
    '传承贡献': { icon: Medal, color: '#F56C6C', tagType: 'danger' }
  };

  for (const pattern of phasePatterns) {
    const match = bio.match(pattern.regex);
    if (match && match[1] && match[1].trim()) {
      const content = match[1].trim().replace(/[,，;；、\n]+/g, '\n').split('\n').filter(Boolean);
      const meta = phaseMeta[pattern.phase];
      for (const line of content.slice(0, 5)) {
        const yearMatch = line.match(/(\d{4})\s*[年年]/);
        items.push({
          phase: pattern.phase,
          year: yearMatch ? yearMatch[1] : null,
          description: line.replace(/\d{4}\s*[年年]\s*/, '').trim() || line,
          icon: meta.icon,
          color: meta.color,
          tagType: meta.tagType
        });
      }
    }
  }

  return items;
}

function generateGenericTimeline(name, years, specialties, honors, bio) {
  const items = [];
  const year = new Date().getFullYear();

  if (years > 0) {
    const startYear = year - years;
    items.push({
      phase: '开始行医',
      year: startYear.toString(),
      description: `${name || '传承人'}于${startYear}年开始从事侗医药实践，至今已有${years}年丰富经验`,
      icon: Clock,
      color: '#67C23A',
      tagType: 'success'
    });

    if (years >= 10) {
      const midYear = startYear + Math.floor(years / 2);
      items.splice(0, 0, {
        phase: '学习经历',
        year: (startYear - 5).toString(),
        description: `${name || '传承人'}早期跟随师长学习侗医药知识，系统掌握传统诊疗技艺`,
        icon: School,
        color: '#409EFF',
        tagType: 'primary'
      });
    }
  }

  if (specialties && specialties.trim()) {
    items.push({
      phase: '技艺特色',
      year: null,
      description: `擅长：${specialties.trim()}`,
      icon: Reading,
      color: '#909399',
      tagType: 'info'
    });
  }

  if (honors && honors.trim()) {
    const honorList = honors.split(/[,，;；、\n]/).filter(Boolean);
    for (const honor of honorList.slice(0, 3)) {
      items.push({
        phase: '荣誉奖项',
        year: null,
        description: honor.trim(),
        icon: Trophy,
        color: '#E6A23C',
        tagType: 'warning'
      });
    }
  }

  if (bio && bio.trim()) {
    items.push({
      phase: '传承贡献',
      year: null,
      description: bio.substring(0, 80) + (bio.length > 80 ? '...' : ''),
      icon: Medal,
      color: '#F56C6C',
      tagType: 'danger'
    });
  }

  return items;
}

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
    // Record browse history
    if (props.inheritor?.id) {
      request.post('/browse-history/record', null, {
        params: { targetType: 'inheritor', targetId: props.inheritor.id }
      }).catch(() => {});
    }
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

.timeline-section {
  margin-top: 24px;
}

.milestone-timeline {
  position: relative;
  padding: var(--space-lg) 0;
}

.timeline-item {
  display: flex;
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
  position: relative;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 19px;
  top: 40px;
  bottom: -24px;
  width: 2px;
  background: var(--border-light);
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-content {
  flex: 1;
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--border-light);
  transition: all var(--transition-fast);
}

.timeline-content:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.timeline-tag {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.timeline-year {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-weight: var(--font-weight-medium);
}

.timeline-desc {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
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
