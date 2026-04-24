<template>
  <div class="qa-page module-page">
    <div class="module-header">
      <h1>问答社区</h1>
      <p class="subtitle">
        侗医药知识问答 · 疑难解答 · 互动交流
      </p>
    </div>

    <div class="qa-container">
      <div class="qa-main">
        <SearchFilter
          v-model="keyword"
          placeholder="搜索问题..."
          :filters="filterConfig"
          @search="handleSearch"
          @filter="handleFilter"
        />

        <SkeletonListQa
          v-if="pageLoading"
          :count="6"
        />

        <div
          v-else
          class="qa-list"
        >
          <div
            v-for="item in paginatedList"
            :key="item.id"
            class="qa-card"
            @click="showDetail(item)"
          >
            <div class="qa-question">
              <el-icon class="q-icon">
                <QuestionFilled />
              </el-icon>
              <h3>{{ item.question }}</h3>
            </div>
            <div
              v-if="item.answer"
              class="qa-answer-preview"
            >
              <p>{{ (item.answer || '').substring(0, 60) }}...</p>
            </div>
            <div class="qa-footer">
              <el-tag
                size="small"
                effect="light"
              >
                {{ item.category }}
              </el-tag>
              <div class="qa-stats">
                <span class="stat-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                <span class="stat-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
              </div>
              <div class="qa-actions">
                <el-button
                  size="small"
                  :type="isItemFavorited(item.id) ? 'warning' : 'default'"
                  @click.stop="doToggleFavorite(item.id, isItemFavorited(item.id))"
                >
                  <el-icon><Star /></el-icon>{{ isItemFavorited(item.id) ? '已收藏' : '收藏' }}
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  class="action-btn-white"
                  @click.stop="showDetail(item)"
                >
                  <el-icon><View /></el-icon>查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!filteredList.length && !pageLoading"
          description="暂无问答数据"
        />
        <Pagination
          v-if="totalItems > 0"
          :page="currentPage"
          :size="pageSize"
          :total="totalItems"
          @update:page="currentPage = $event; loadQaData();"
          @update:size="pageSize = $event; currentPage = 1; loadQaData();"
        />
      </div>

      <PageSidebar
        title="问答统计"
        :stats="stats"
        hot-title="热门问题"
        :hot-items="hotQa"
        @hot-click="showDetail"
      >
        <AiChatCard />
        <el-card
          class="feedback-entry-card"
          shadow="hover"
          @click="$router.push('/feedback')"
        >
          <div class="feedback-entry">
            <el-icon :size="24"><EditPen /></el-icon>
            <div class="feedback-entry-text">
              <span class="feedback-entry-title">有问题？反馈给我们</span>
              <span class="feedback-entry-desc">提交建议或问题，我们会尽快处理</span>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </PageSidebar>
    </div>

    <QuizDetailDialog 
      v-model:visible="detailVisible" 
      :qa="currentQa" 
      :is-favorited="isCurrentFavorited"
      @toggle-favorite="toggleFavoriteDetail"
    />
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { QuestionFilled, Star, View, EditPen, ArrowRight } from "@element-plus/icons-vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import QuizDetailDialog from "@/components/business/dialogs/QuizDetailDialog.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import SkeletonListQa from "@/components/common/SkeletonListQa.vue";
import AiChatCard from "@/components/business/display/AiChatCard.vue";
import { extractPageData } from "@/utils";
import { useDebounceFn } from "@/composables/useDebounce";
import { useFavorite } from "@/composables/useFavorite";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 6,
  LARGE: 12,
  SMALL: 3
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();
const request = inject("request");

const { isFavorited: isItemFavorited, loadFavorites, toggleFavorite: doToggleFavorite, items: favItems } = useFavorite('qa');

const pageLoading = ref(false);
const keyword = ref("");
const allQa = ref([]);
const hotQa = ref([]);
const detailVisible = ref(false);
const currentQa = ref(null);
const categoryFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);
const totalItems = ref(0);
const statsData = ref({ total: 0, categoryCount: 0, totalViews: 0, totalFavorites: 0 });

const filterConfig = ref([
  { key: "category", label: "分类", options: [{ label: "全部", value: "" }] }
]);

const stats = computed(() => [
  { value: statsData.value.total, label: "问题总数" },
  { value: statsData.value.categoryCount, label: "分类数" },
  { value: statsData.value.totalViews, label: "浏览量" },
  { value: statsData.value.totalFavorites, label: "收藏量" }
]);

const filteredList = computed(() => allQa.value);

const paginatedList = computed(() => allQa.value);

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1;
  loadQaData();
}, DEBOUNCE_DELAY);

const handleSearch = () => {
  debouncedSearch();
};

const handleFilter = (filters) => { 
  categoryFilter.value = filters.category || ""; 
  currentPage.value = 1;
  loadQaData();
};

const isCurrentFavorited = computed(() => currentQa.value && isItemFavorited(currentQa.value.id));

const showDetail = async (item) => {
  currentQa.value = item;
  detailVisible.value = true;
  try {
    await request.post(`/qa/${item.id}/view`);
    const qaIdx = allQa.value.findIndex(q => q.id === item.id);
    if (qaIdx > -1) allQa.value[qaIdx].viewCount = (allQa.value[qaIdx].viewCount || 0) + 1;
  } catch {}
};

const toggleFavoriteDetail = () => { if (currentQa.value) doToggleFavorite(currentQa.value.id, isItemFavorited(currentQa.value.id)); };

const loadQaData = async () => {
  pageLoading.value = true;
  try {
    const [pageRes, statsRes] = await Promise.all([
      request.get("/qa/list", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          keyword: keyword.value,
          category: categoryFilter.value,
          _t: Date.now()
        }
      }),
      request.get("/stats/qa")
    ]);
    
    const { records, total } = extractPageData(pageRes);
    allQa.value = records;
    favItems.value = allQa.value;
    totalItems.value = total;
    hotQa.value = allQa.value.slice(0, 5);
    
    const sd = statsRes.data || statsRes || {};
    statsData.value = { total: sd.total || 0, categoryCount: sd.categoryCount || 0, totalViews: sd.totalViews || 0, totalFavorites: sd.totalFavorites || 0 };
    
    await loadFavorites();
  } catch { ElMessage.error("加载失败"); }
  finally { pageLoading.value = false; }
};

const loadMetadata = async () => {
  try {
    const res = await request.get("/metadata/filters");
    const data = res.data || res || {};
    const qaFilters = data.qa || {};
    const categories = qaFilters.category || [];
    filterConfig.value = [
      { key: "category", label: "分类", options: [{ label: "全部", value: "" }, ...categories.map(c => ({ label: c, value: c }))] }
    ];
  } catch {}
};

onMounted(() => { loadMetadata(); loadQaData(); });

watch(() => route.path, (newPath) => {
  if (newPath === '/qa') loadQaData();
});
</script>

<style scoped>
.qa-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--space-xl);
}

.qa-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.qa-card {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.qa-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.qa-question {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
}

.q-icon {
  color: var(--dong-indigo);
  font-size: var(--font-size-xl);
  margin-top: 2px;
}

.qa-question h3 {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--text-primary);
  line-height: var(--line-height-normal);
}

.qa-answer-preview {
  margin-top: var(--space-md);
  padding-top: var(--space-md);
  border-top: 1px dashed var(--border-light);
}

.qa-answer-preview p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  line-height: var(--line-height-relaxed);
}

.qa-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-md);
}

.qa-stats {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-left: var(--space-md);
}

.qa-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

@media (max-width: 1024px) {
  .qa-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .qa-list {
    gap: var(--space-md);
  }
  
  .qa-card {
    padding: var(--space-lg);
  }
  
  .qa-question {
    gap: var(--space-sm);
  }
  
  .q-icon {
    font-size: var(--font-size-lg);
  }
  
  .qa-question h3 {
    font-size: var(--font-size-base);
  }
  
  .qa-answer-preview {
    margin-top: var(--space-sm);
    padding-top: var(--space-sm);
  }
  
  .qa-answer-preview p {
    font-size: var(--font-size-xs);
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .qa-footer {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }
  
  .qa-stats {
    order: 3;
    width: 100%;
    margin-left: 0;
    margin-top: var(--space-sm);
  }
  
  .qa-actions {
    flex: 1;
    justify-content: flex-end;
  }
  
  .qa-actions .el-button {
    font-size: var(--font-size-xs);
    padding: var(--space-xs) var(--space-sm);
  }
}

@media (max-width: 480px) {
  .qa-actions {
    width: 100%;
    gap: var(--space-xs);
  }
  
  .qa-actions .el-button {
    flex: 1;
  }
}

.feedback-entry-card {
  margin-top: var(--space-md);
  cursor: pointer;
  border-radius: var(--radius-lg);
  border: 1px dashed var(--dong-indigo, #4A148C);
  transition: all 0.25s ease;
}

.feedback-entry-card:hover {
  border-style: solid;
  box-shadow: 0 2px 12px rgba(74, 20, 140, 0.15);
}

.feedback-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--dong-indigo, #4A148C);
}

.feedback-entry-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feedback-entry-title {
  font-size: 14px;
  font-weight: 600;
}

.feedback-entry-desc {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
