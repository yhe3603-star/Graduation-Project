<template>
  <el-dialog 
    :model-value="visible" 
    :title="qa?.question" 
    width="min(700px, 90vw)"
    class="qa-detail-dialog detail-dialog"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="dialog-content">
      <div class="header-section">
        <div class="header-tags">
          <el-tag effect="light">
            {{ qa?.category || '侗药常识' }}
          </el-tag>
        </div>
        <div class="header-stats">
          <span class="stat-item"><el-icon><View /></el-icon>{{ qa?.viewCount || 0 }}</span>
          <span class="stat-item"><el-icon><Star /></el-icon>{{ qa?.favoriteCount || 0 }}</span>
        </div>
      </div>

      <el-divider />

      <div class="qa-body">
        <section class="content-section">
          <h3 class="section-title">
            <el-icon class="q-icon"><QuestionFilled /></el-icon>问题
          </h3>
          <div class="content-box highlight">
            {{ qa?.question }}
          </div>
        </section>

        <section class="content-section">
          <h3 class="section-title">
            <el-icon class="a-icon"><CircleCheckFilled /></el-icon>解答
          </h3>
          <div class="content-box">
            {{ qa?.answer || '暂无解答' }}
          </div>
        </section>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comment-section-wrapper">
      <h3 class="section-title">评论交流</h3>
      <CommentSection
        :comments="qaComments"
        :is-logged-in="userStore.isLoggedIn"
        :user-name="userStore.userName"
        :loading="qaCommentLoading"
        :loading-more="qaCommentLoading"
        :has-more="qaHasMore"
        @post="handleQaCommentPost"
        @reply="handleQaCommentPost"
        @like="handleQaLike"
        @load-more="loadQaMore"
      />
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
          @click="$emit('update:visible', false)"
        >
          关闭
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { watch } from 'vue';
import { View, Star, QuestionFilled, CircleCheckFilled } from '@element-plus/icons-vue';
import request from '@/utils/request';
import { useUserStore } from '@/stores/user';
import CommentSection from '@/components/business/interact/CommentSection.vue';
import { useEntityComments } from '@/composables/useEntityComments';

const props = defineProps({
  visible: { type: Boolean, default: false },
  qa: { type: Object, default: null },
  isFavorited: { type: Boolean, default: false }
});

defineEmits(['update:visible', 'toggle-favorite']);
const userStore = useUserStore();

const {
  comments: qaComments, commentLoading: qaCommentLoading, hasMore: qaHasMore,
  loadComments: loadQaComments, loadMore: loadQaMore, handleCommentPost: handleQaCommentPost, handleLike: handleQaLike
} = useEntityComments('qa', () => props.qa?.id);

watch(() => props.visible, (newVal) => {
  if (newVal && props.qa?.id && userStore.isLoggedIn) {
    request.post('/browse-history/record', null, {
      params: { targetType: 'qa', targetId: props.qa.id }
    }).catch(() => {});
    loadQaComments();
  }
});
</script>

<style scoped>
@import '@/styles/dialog-common.css';

.qa-detail-dialog :deep(.el-dialog__body) {
  overflow-x: hidden;
}

.qa-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-section {
  margin-bottom: 0;
}

.q-icon {
  color: var(--dong-blue, #1A5276);
}

.a-icon {
  color: var(--dong-green, #28B463);
}

.content-box.highlight {
  font-size: 15px;
  color: #333;
  background: linear-gradient(135deg, #e8f4f8, #f0f7fa);
  border-left: 4px solid var(--dong-blue, #1A5276);
}

.favorite-btn.favorited {
  background: linear-gradient(135deg, #e6a23c, #cf9236);
  border-color: #e6a23c;
  color: #fff;
}

@media (max-width: 768px) {
  .qa-body {
    gap: 16px;
  }
  
  .content-box.highlight {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .qa-body {
    gap: 12px;
  }
  
  .content-box.highlight {
    font-size: 13px;
    padding: 10px;
  }
}

.comment-section-wrapper {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
}
</style>
