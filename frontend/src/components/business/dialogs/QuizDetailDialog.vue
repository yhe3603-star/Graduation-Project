<template>
  <el-dialog 
    :model-value="visible" 
    :title="qa?.question" 
    width="700px" 
    class="qa-detail-dialog"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="dialog-content">
      <div class="qa-header">
        <el-tag effect="light">
          {{ qa?.category || '侗药常识' }}
        </el-tag>
        <div class="header-stats">
          <span class="stat-item"><el-icon><View /></el-icon>{{ qa?.viewCount || 0 }}</span>
          <span class="stat-item"><el-icon><Star /></el-icon>{{ qa?.favoriteCount || 0 }}</span>
        </div>
      </div>

      <el-divider />

      <div class="qa-body">
        <div class="question-section">
          <div class="section-header">
            <el-icon class="q-icon">
              <QuestionFilled />
            </el-icon>
            <span class="section-title">问题</span>
          </div>
          <p class="content-box highlight">
            {{ qa?.question }}
          </p>
        </div>

        <div class="answer-section">
          <div class="section-header">
            <el-icon class="a-icon">
              <CircleCheckFilled />
            </el-icon>
            <span class="section-title">解答</span>
          </div>
          <p class="content-box">
            {{ qa?.answer || '暂无解答' }}
          </p>
        </div>
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
          @click="$emit('update:visible', false)"
        >
          关闭
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { View, Star, QuestionFilled, CircleCheckFilled } from '@element-plus/icons-vue';

defineProps({
  visible: { type: Boolean, default: false },
  qa: { type: Object, default: null },
  isFavorited: { type: Boolean, default: false }
});

defineEmits(['update:visible', 'toggle-favorite']);
</script>

<style scoped>
@import '@/styles/dialog-common.css';

.qa-header { display: flex; justify-content: space-between; align-items: center; }
.qa-body { display: flex; flex-direction: column; gap: 24px; }
.section-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.q-icon { color: var(--dong-blue, #1A5276); font-size: 20px; }
.a-icon { color: var(--dong-green, #28B463); font-size: 20px; }
.content-box.highlight { font-size: 15px; color: #333; }

@media (max-width: 768px) {
  .qa-header { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
