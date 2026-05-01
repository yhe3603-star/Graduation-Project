<template>
  <el-dialog
    :model-value="visible"
    title="传承人详情"
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-descriptions
      :column="2"
      border
    >
      <el-descriptions-item label="ID">
        {{ inheritor?.id }}
      </el-descriptions-item>
      <el-descriptions-item label="姓名">
        {{ inheritor?.name }}
      </el-descriptions-item>
      <el-descriptions-item label="级别">
        <el-tag :type="getLevelTagType(inheritor?.level)">
          {{ inheritor?.level }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="经验年限">
        {{ inheritor?.experienceYears }}年
      </el-descriptions-item>
      <el-descriptions-item
        label="技艺特色"
        :span="2"
      >
        {{ inheritor?.specialties || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="个人简介"
        :span="2"
      >
        {{ inheritor?.bio || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="代表案例"
        :span="2"
      >
        {{ inheritor?.representativeCases || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item
        label="荣誉资质"
        :span="2"
      >
        {{ inheritor?.honors || '暂无' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(inheritor?.createdAt) }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ formatTime(inheritor?.updatedAt) }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="timeline-section">
      <h4 class="section-subtitle">传承生涯</h4>
      <div v-if="timelineItems.length > 0" class="admin-timeline">
        <div
          v-for="(item, index) in timelineItems"
          :key="index"
          class="admin-timeline-item"
        >
          <div class="timeline-marker" :style="{ background: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="timeline-body">
            <div class="timeline-header">
              <el-tag :type="item.tagType" size="small" effect="dark">{{ item.phase }}</el-tag>
              <span v-if="item.year" class="timeline-year">{{ item.year }}</span>
            </div>
            <p class="timeline-text">{{ item.description }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无生涯记录" :image-size="60" />
    </div>

    <MediaDisplay
      v-if="inheritor?.images"
      :files="inheritor.images"
      :show-documents="false"
      image-title="传承人照片"
    />

    <MediaDisplay
      v-if="inheritor?.videos"
      :files="inheritor.videos"
      :show-images="false"
      :show-documents="false"
      video-title="相关视频"
    />

    <MediaDisplay
      v-if="inheritor?.documents"
      :files="inheritor.documents"
      :show-images="false"
      :show-videos="false"
      document-title="资质文档"
    />

    <template #footer>
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
import { computed } from 'vue'
import { Clock, Medal, Reading, School, Trophy } from '@element-plus/icons-vue'
import { formatTime, getLevelTagType } from '@/utils/adminUtils'
import MediaDisplay from '@/components/business/media/MediaDisplay.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  inheritor: { type: Object, default: null }
})

defineEmits(['update:visible'])

const timelineItems = computed(() => {
  const inheritor = props.inheritor
  if (!inheritor) return []

  const bio = inheritor.bio || ''
  const experienceYears = inheritor.experienceYears || 0
  const honors = inheritor.honors || ''
  const specialties = inheritor.specialties || ''

  const parsed = parseTimelineFromBio(bio)

  if (parsed.length > 0) {
    return parsed
  }

  return generateGenericTimeline(inheritor.name, experienceYears, specialties, honors, bio)
})

function parseTimelineFromBio(bio) {
  if (!bio || !bio.trim()) return []

  const phasePatterns = [
    { regex: /学习经历[:：]?\s*([\s\S]*?)(?=行医经历[:：]|荣誉奖项[:：]|传承贡献[:：]|$)/, phase: '学习经历', icon: School, color: '#409EFF', tagType: 'primary' },
    { regex: /行医经历[:：]?\s*([\s\S]*?)(?=荣誉奖项[:：]|传承贡献[:：]|$)/, phase: '行医经历', icon: Clock, color: '#67C23A', tagType: 'success' },
    { regex: /荣誉奖项[:：]?\s*([\s\S]*?)(?=传承贡献[:：]|$)/, phase: '荣誉奖项', icon: Trophy, color: '#E6A23C', tagType: 'warning' },
    { regex: /传承贡献[:：]?\s*([\s\S]*)/, phase: '传承贡献', icon: Medal, color: '#F56C6C', tagType: 'danger' }
  ]

  const items = []
  const phaseMeta = {
    '学习经历': { icon: School, color: '#409EFF', tagType: 'primary' },
    '行医经历': { icon: Clock, color: '#67C23A', tagType: 'success' },
    '荣誉奖项': { icon: Trophy, color: '#E6A23C', tagType: 'warning' },
    '传承贡献': { icon: Medal, color: '#F56C6C', tagType: 'danger' }
  }

  for (const pattern of phasePatterns) {
    const match = bio.match(pattern.regex)
    if (match && match[1] && match[1].trim()) {
      const content = match[1].trim().replace(/[,，;；、\n]+/g, '\n').split('\n').filter(Boolean)
      const meta = phaseMeta[pattern.phase]
      for (const line of content.slice(0, 5)) {
        const yearMatch = line.match(/(\d{4})\s*[年年]/)
        items.push({
          phase: pattern.phase,
          year: yearMatch ? yearMatch[1] : null,
          description: line.replace(/\d{4}\s*[年年]\s*/, '').trim() || line,
          icon: meta.icon,
          color: meta.color,
          tagType: meta.tagType
        })
      }
    }
  }

  return items
}

function generateGenericTimeline(name, years, specialties, honors, bio) {
  const items = []
  const year = new Date().getFullYear()

  if (years > 0) {
    const startYear = year - years
    if (years >= 10) {
      items.push({
        phase: '学习经历',
        year: (startYear - 5).toString(),
        description: `${name || '传承人'}早期跟随师长学习侗医药知识，系统掌握传统诊疗技艺`,
        icon: School,
        color: '#409EFF',
        tagType: 'primary'
      })
    }
    items.push({
      phase: '开始行医',
      year: startYear.toString(),
      description: `${name || '传承人'}于${startYear}年开始从事侗医药实践，至今已有${years}年丰富经验`,
      icon: Clock,
      color: '#67C23A',
      tagType: 'success'
    })
  }

  if (specialties && specialties.trim()) {
    items.push({
      phase: '技艺特色',
      year: null,
      description: `擅长：${specialties.trim()}`,
      icon: Reading,
      color: '#909399',
      tagType: 'info'
    })
  }

  if (honors && honors.trim()) {
    const honorList = honors.split(/[,，;；、\n]/).filter(Boolean)
    for (const honor of honorList.slice(0, 3)) {
      items.push({
        phase: '荣誉奖项',
        year: null,
        description: honor.trim(),
        icon: Trophy,
        color: '#E6A23C',
        tagType: 'warning'
      })
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
    })
  }

  return items
}
</script>

<style scoped>
@import '@/styles/media-common.css';

.timeline-section {
  margin-top: 24px;
}

.section-subtitle {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  padding-left: 12px;
  border-left: 4px solid var(--dong-blue);
}

.admin-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

.admin-timeline-item {
  display: flex;
  gap: var(--space-lg);
  align-items: flex-start;
}

.timeline-marker {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.timeline-body {
  flex: 1;
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--border-light);
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-xs);
}

.timeline-year {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

.timeline-text {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .admin-timeline-item {
    gap: var(--space-md);
  }

  .timeline-marker {
    width: 30px;
    height: 30px;
    font-size: 14px;
  }

  .timeline-body {
    padding: var(--space-sm);
  }
}
</style>
