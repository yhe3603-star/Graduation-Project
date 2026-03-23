<template>
  <div
    v-loading="pageLoading"
    class="feedback-page module-page"
  >
    <div class="module-header">
      <h1>意见反馈</h1>
      <p class="subtitle">
        您的建议是我们前进的动力
      </p>
    </div>

    <div class="feedback-container">
      <div class="feedback-main">
        <el-card
          class="feedback-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <el-icon><EditPen /></el-icon>
              <span>提交反馈</span>
            </div>
          </template>
          <el-form
            ref="formRef"
            :model="feedbackForm"
            :rules="rules"
            label-width="80px"
          >
            <el-form-item
              label="反馈类型"
              prop="type"
            >
              <el-radio-group v-model="feedbackForm.type">
                <el-radio-button value="建议">
                  功能建议
                </el-radio-button>
                <el-radio-button value="问题">
                  问题反馈
                </el-radio-button>
                <el-radio-button value="其他">
                  其他
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              label="标题"
              prop="title"
            >
              <el-input
                v-model="feedbackForm.title"
                placeholder="请简要描述您的反馈"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            <el-form-item
              label="详细描述"
              prop="content"
            >
              <el-input
                v-model="feedbackForm.content"
                type="textarea"
                :rows="6"
                placeholder="请详细描述您的反馈内容..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="联系方式">
              <el-input
                v-model="feedbackForm.contact"
                placeholder="邮箱或手机号（选填）"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="submitting"
                size="large"
                @click="submitFeedback"
              >
                <el-icon><Check /></el-icon>
                提交反馈
              </el-button>
              <el-button
                size="large"
                @click="resetForm"
              >
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card
          v-if="isLoggedIn && myFeedbacks.length"
          class="history-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>我的反馈记录</span>
            </div>
          </template>
          <div class="feedback-list">
            <div
              v-for="item in myFeedbacks"
              :key="item.id"
              class="feedback-item"
            >
              <div class="feedback-header">
                <el-tag
                  :type="getTypeTag(item.type)"
                  size="small"
                >
                  {{ item.type }}
                </el-tag>
                <el-tag
                  :type="getStatusTag(item.status)"
                  size="small"
                >
                  {{ getStatusText(item.status) }}
                </el-tag>
                <span class="feedback-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <h4 class="feedback-title">
                {{ item.title }}
              </h4>
              <p class="feedback-content">
                {{ item.content }}
              </p>
              <div
                v-if="item.reply"
                class="feedback-reply"
              >
                <span class="reply-label">管理员回复：</span>
                <span class="reply-content">{{ item.reply }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="feedback-sidebar">
        <el-card
          class="tips-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>反馈须知</span>
            </div>
          </template>
          <ul class="tips-list">
            <li>请详细描述您遇到的问题或建议</li>
            <li>功能建议请说明具体使用场景</li>
            <li>问题反馈请提供复现步骤</li>
            <li>我们会在3个工作日内处理您的反馈</li>
          </ul>
        </el-card>

        <el-card
          class="stats-card"
          shadow="hover"
        >
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>反馈统计</span>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-value">{{ stats.total }}</span>
              <span class="stat-label">总反馈</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.processed }}</span>
              <span class="stat-label">已处理</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.pending }}</span>
              <span class="stat-label">待处理</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { Check, Clock, DataLine, EditPen, InfoFilled } from "@element-plus/icons-vue";
import { extractData } from "@/utils";
import { useUserStore } from "@/stores/user";

const request = inject("request");
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const pageLoading = ref(false);
const submitting = ref(false);
const formRef = ref(null);
const myFeedbacks = ref([]);
const stats = ref({ total: 0, processed: 0, pending: 0 });

const feedbackForm = ref({ type: "建议", title: "", content: "", contact: "" });

const rules = {
  type: [{ required: true, message: "请选择反馈类型", trigger: "change" }],
  title: [{ required: true, message: "请输入标题", trigger: "blur" }, { min: 5, max: 100, message: "标题长度5-100个字符", trigger: "blur" }],
  content: [{ required: true, message: "请输入详细描述", trigger: "blur" }, { min: 10, max: 500, message: "描述长度10-500个字符", trigger: "blur" }]
};

const getTypeTag = (type) => ({ "建议": "primary", "问题": "danger", "其他": "info" }[type] || "info");
const getStatusTag = (status) => ({ "pending": "warning", "processing": "primary", "resolved": "success" }[status] || "info");
const getStatusText = (status) => ({ "pending": "待处理", "processing": "处理中", "resolved": "已解决" }[status] || "未知");

const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  return date.toLocaleDateString("zh-CN") + " " + date.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" });
};

const submitFeedback = async () => {
  if (!await formRef.value?.validate()) return
  submitting.value = true
  try {
    await request.post("/feedback", feedbackForm.value)
    ElMessage.success("感谢您的反馈，我们会尽快处理！")
    resetForm();
    fetchStats();
    if (isLoggedIn.value) fetchMyFeedbacks();
  } catch (e) {
    logFetchError('提交反馈', e)
    ElMessage.error("提交失败，请稍后重试")
  } finally {
    submitting.value = false
  }
};

const resetForm = () => {
  feedbackForm.value = { type: "建议", title: "", content: "", contact: "" };
  formRef.value?.resetFields();
};

const fetchMyFeedbacks = async () => {
  try { myFeedbacks.value = extractData(await request.get("/feedback/my")); } catch {}
};

const fetchStats = async () => {
  try {
    const res = await request.get("/feedback/stats");
    const data = res.data || res || { total: 0, processed: 0, pending: 0 };
    stats.value = { total: data.total || 0, processed: data.processed || 0, pending: data.pending || 0 };
  } catch {}
};

onMounted(() => {
  fetchStats();
  if (isLoggedIn.value) fetchMyFeedbacks();
});
</script>

<style scoped>
.feedback-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--space-xl);
}

.feedback-card,
.history-card,
.tips-card,
.stats-card {
  border-radius: var(--radius-lg);
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--dong-indigo);
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.feedback-item {
  padding: var(--space-xl);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.feedback-time {
  margin-left: auto;
  font-size: var(--font-size-xs);
  color: var(--text-light);
}

.feedback-title {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-md);
  color: var(--text-primary);
}

.feedback-content {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
}

.feedback-reply {
  margin-top: var(--space-md);
  padding: var(--space-md);
  background: rgba(26, 82, 118, 0.08);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

.reply-label {
  color: var(--dong-indigo);
  font-weight: var(--font-weight-medium);
}

.reply-content {
  color: var(--text-secondary);
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  margin-bottom: var(--space-sm);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-md);
}

.stat-item {
  text-align: center;
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
}

.stat-value {
  display: block;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--dong-indigo);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

@media (max-width: 1024px) {
  .feedback-container {
    grid-template-columns: 1fr;
  }
}
</style>
