<template>
  <div class="comment-section">
    <div class="comment-input-area">
      <el-avatar
        :size="40"
        class="user-avatar"
      >
        {{ userName?.charAt(0) || '游' }}
      </el-avatar>
      <div class="input-wrap">
        <el-input 
          v-model="content" 
          :placeholder="replyTo ? `回复 @${replyTo.username}...` : '分享你对侗乡医药的看法...'" 
          type="textarea" 
          :rows="3"
          :disabled="!isLoggedIn"
          resize="none"
        />
        <div class="input-actions">
          <div
            v-if="replyTo"
            class="reply-info"
          >
            <span>回复 <b>@{{ replyTo.username }}</b></span>
            <el-button
              type="info"
              size="small"
              text
              @click="cancelReply"
            >
              取消
            </el-button>
          </div>
          <span
            v-else
            class="char-count"
          >{{ content.length }}/500</span>
          <el-button 
            type="primary" 
            :loading="posting" 
            :disabled="!isLoggedIn || !content.trim()"
            @click="postComment"
          >
            {{ replyTo ? '发送回复' : '发布评论' }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="comment-controls">
      <div class="sort-controls">
        <span class="sort-label">排序方式：</span>
        <el-radio-group
          v-model="sortBy"
          size="small"
        >
          <el-radio-button label="latest">
            最新
          </el-radio-button>
          <el-radio-button label="oldest">
            最早
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div
      v-loading="loading"
      class="comment-list"
    >
      <div
        v-for="comment in paginatedMainComments"
        :key="comment.id"
        class="comment-item main-comment"
      >
        <el-avatar
          :size="40"
          class="comment-avatar"
        >
          {{ comment.username?.charAt(0) || '匿' }}
        </el-avatar>
        <div class="comment-body">
          <div class="comment-header">
            <span class="comment-name">{{ comment.username || '匿名用户' }}</span>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <p class="comment-content">
            {{ comment.content }}
          </p>
          <div class="comment-actions">
            <el-button
              type="primary"
              size="small"
              class="reply-btn"
              @click="setReply(comment)"
            >
              <el-icon><ChatDotRound /></el-icon>
              回复
            </el-button>
          </div>
          
          <!-- 回复列表 -->
          <div
            v-if="getReplies(comment.id).length > 0"
            class="replies-list"
          >
            <div 
              v-for="reply in getReplies(comment.id)" 
              :key="reply.id" 
              class="comment-item reply-item"
            >
              <el-avatar
                :size="32"
                class="comment-avatar reply-avatar"
              >
                {{ reply.username?.charAt(0) || '匿' }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-name">{{ reply.username || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                </div>
                <p class="comment-content">
                  <span class="reply-to">
                    回复 <b>@{{ reply.replyToUsername || comment.username }}</b>：
                  </span>
                  {{ reply.content }}
                </p>
                <div class="comment-actions">
                  <el-button
                    type="primary"
                    size="small"
                    class="reply-btn"
                    @click="setReply(reply)"
                  >
                    <el-icon><ChatDotRound /></el-icon>
                    回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty
        v-if="!comments.length && !loading"
        description="暂无评论，快来发表第一条吧！"
      />
    </div>

    <Pagination 
      v-if="props.total > 0" 
      :page="currentPage" 
      :size="pageSize" 
      :total="props.total" 
      @update:page="handlePageChange" 
      @update:size="handleSizeChange" 
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import { ChatDotRound } from "@element-plus/icons-vue";
import Pagination from "@/components/business/display/Pagination.vue";

const props = defineProps({
  comments: { type: Array, default: () => [] },
  isLoggedIn: Boolean,
  userName: String,
  loading: Boolean,
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 6 }
});

const emit = defineEmits(["post", "reply", "page-change", "size-change"]);

const content = ref("");
const posting = ref(false);
const currentPage = ref(1);
const pageSize = ref(6);
const replyTo = ref(null);
const sortBy = ref("latest");

watch(() => props.page, (val) => { currentPage.value = val; }, { immediate: true });
watch(() => props.size, (val) => { pageSize.value = val; }, { immediate: true });

const mainComments = computed(() => {
  return props.comments.filter(comment => !comment.replyToId);
});

// 按排序方式排序评论
const sortedMainComments = computed(() => {
  const sorted = [...mainComments.value];
  if (sortBy.value === "latest") {
    return sorted.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
  } else {
    return sorted.sort((a, b) => new Date(a.createTime) - new Date(b.createTime));
  }
});

// 分页处理
const paginatedMainComments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return sortedMainComments.value.slice(start, start + pageSize.value);
});

// 获取评论的回复
const getReplies = (commentId) => {
  return props.comments
    .filter(reply => reply.replyToId === commentId)
    .sort((a, b) => new Date(a.createTime) - new Date(b.createTime));
};

const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  const diff = Date.now() - date;
  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  return date.toLocaleDateString("zh-CN");
};

const setReply = (comment) => {
  if (!props.isLoggedIn) {
    ElMessage.warning("请先登录后再回复");
    return;
  }
  replyTo.value = comment;
};

const cancelReply = () => {
  replyTo.value = null;
  content.value = "";
};

const postComment = async () => {
  if (!props.isLoggedIn) {
    ElMessage.warning("请先登录后再发表评论");
    return;
  }
  if (!content.value.trim()) {
    ElMessage.warning("请输入评论内容");
    return;
  }
  
  posting.value = true;
  const replyData = replyTo.value ? { 
    replyToId: replyTo.value.id, 
    replyToUserId: replyTo.value.userId,
    replyToName: replyTo.value.username 
  } : null;
  
  emit("post", content.value, replyData, () => {
    content.value = "";
    replyTo.value = null;
    currentPage.value = 1;
    posting.value = false;
  }, () => {
    posting.value = false;
  });
};

const handlePageChange = (page) => {
  emit("page-change", page);
};

const handleSizeChange = (size) => {
  emit("size-change", size);
};
</script>

<style scoped>
.comment-section { display: flex; flex-direction: column; gap: 24px; }
.comment-input-area { display: flex; gap: 16px; padding: 20px; background: #f8fafb; border-radius: 16px; }
.user-avatar { background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); color: var(--text-inverse); flex-shrink: 0; }
.input-wrap { flex: 1; display: flex; flex-direction: column; gap: 12px; }
.input-actions { display: flex; justify-content: space-between; align-items: center; }
.reply-info { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #666; }
.reply-info b { color: var(--dong-blue); }
.char-count { font-size: 12px; color: #999; }

/* 评论控制栏 */
.comment-controls { display: flex; justify-content: flex-end; align-items: center; gap: 16px; padding: 0 4px; }
.sort-controls { display: flex; align-items: center; gap: 8px; }
.sort-label { font-size: 14px; color: #666; }

/* 评论列表 */
.comment-list { display: flex; flex-direction: column; gap: 20px; min-height: 200px; }

/* 主评论 */
.comment-item { display: flex; gap: 12px; padding: 16px; border-radius: 12px; transition: all 0.3s; }
.main-comment { background: var(--text-inverse); border: 1px solid #e8f4f8; }
.main-comment:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }

/* 回复评论 */
.reply-item { background: #f9fbfc; border: 1px solid #e1f0f5; margin-top: 12px; margin-left: 52px; }
.reply-item:hover { box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); }

/* 头像 */
.comment-avatar { background: linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark)); color: var(--text-inverse); flex-shrink: 0; }
.reply-avatar { background: linear-gradient(135deg, #667eea, #764ba2); }

/* 评论内容 */
.comment-body { flex: 1; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.comment-name { font-size: 14px; font-weight: 600; color: #333; }
.comment-time { font-size: 12px; color: #999; }
.comment-content { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin: 0 0 12px 0; }
.reply-to { color: var(--dong-blue); font-size: 13px; }
.reply-to b { font-weight: 500; }

/* 评论操作 */
.comment-actions { display: flex; gap: 8px; }
.reply-btn { padding: 4px 12px; }

/* 回复列表 */
.replies-list { margin-top: 16px; }

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-input-area { padding: 16px; }
  .comment-item { padding: 12px; }
  .reply-item { margin-left: 44px; }
  .comment-controls { flex-direction: column; align-items: flex-start; gap: 8px; }
  .sort-controls { width: 100%; justify-content: space-between; }
}
</style>
