<template>
  <div
    v-loading="pageLoading"
    class="qa-page module-page"
  >
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

        <div class="qa-list">
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
                  :type="isFavorited(item.id) ? 'warning' : 'default'"
                  @click.stop="toggleFavorite(item)"
                >
                  <el-icon><Star /></el-icon>{{ isFavorited(item.id) ? '已收藏' : '收藏' }}
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
import { QuestionFilled, Star, View } from "@element-plus/icons-vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import QuizDetailDialog from "@/components/business/dialogs/QuizDetailDialog.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import AiChatCard from "@/components/business/display/AiChatCard.vue";
import { extractPageData, extractData } from "@/utils";
import { useDebounceFn } from "@/composables/useDebounce";
import { useUserStore } from "@/stores/user";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 6,
  LARGE: 12,
  SMALL: 3
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();
const request = inject("request");
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const pageLoading = ref(false);
const keyword = ref("");
const allQa = ref([]);
const allQaForStats = ref([]);
const hotQa = ref([]);
const detailVisible = ref(false);
const currentQa = ref(null);
const favorites = ref([]);
const categoryFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);
const totalItems = ref(0);

const filterConfig = [
  { key: "category", label: "分类", options: [{ label: "全部", value: "" }, { label: "侗药常识", value: "侗药常识" }, { label: "侗医疗法", value: "侗医疗法" }, { label: "文化背景", value: "文化背景" }] }
];

const stats = computed(() => {
  const data = allQaForStats.value.length > 0 ? allQaForStats.value : allQa.value;
  const totalViews = data.reduce((sum, q) => sum + (q.viewCount || 0), 0);
  const totalFavorites = data.reduce((sum, q) => sum + (q.favoriteCount || 0), 0);
  return [
    { value: data.length, label: "问题总数" },
    { value: new Set(data.map(q => q.category).filter(Boolean)).size, label: "分类数" },
    { value: totalViews, label: "浏览量" },
    { value: totalFavorites, label: "收藏量" }
  ];
});

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

const isFavorited = (id) => favorites.value.some(f => f.targetId === id && f.type === "qa");
const isCurrentFavorited = computed(() => currentQa.value && isFavorited(currentQa.value.id));

const showDetail = async (item) => {
  currentQa.value = item;
  detailVisible.value = true;
  try {
    await request.post(`/qa/${item.id}/view`);
    const qaIdx = allQa.value.findIndex(q => q.id === item.id);
    if (qaIdx > -1) allQa.value[qaIdx].viewCount = (allQa.value[qaIdx].viewCount || 0) + 1;
  } catch {}
};

const toggleFavorite = async (item) => {
  if (!isLoggedIn.value) { ElMessage.warning("请先登录"); return; }
  try {
    const qaIdx = allQa.value.findIndex(q => q.id === item.id);
    const favIdx = favorites.value.indexOf(item.id)
    if (favIdx > -1) {
      favorites.value.splice(favIdx, 1)
      if (qaIdx > -1) allQa.value[qaIdx].favoriteCount = Math.max(0, (allQa.value[qaIdx].favoriteCount || 1) - 1)
      ElMessage.success("已取消收藏")
    } else {
      await request.post(`/favorites/qa/${item.id}`)
      favorites.value.push({ targetId: item.id, type: "qa" })
      if (qaIdx > -1) allQa.value[qaIdx].favoriteCount = (allQa.value[qaIdx].favoriteCount || 1) + 1
      ElMessage.success("收藏成功")
    }
  } catch (e) {
    logFetchError('收藏操作', e)
    ElMessage.error("操作失败")
  }
};

const toggleFavoriteDetail = () => { if (currentQa.value) toggleFavorite(currentQa.value); };

const loadQaData = async () => {
  pageLoading.value = true;
  try {
    // 并行请求：获取分页数据和所有数据用于统计
    const [pageRes, allRes] = await Promise.all([
      request.get("/qa/list", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          keyword: keyword.value,
          category: categoryFilter.value,
          _t: Date.now()
        }
      }),
      request.get("/qa/list", {
        params: {
          page: 1,
          size: 9999, // 获取足够多的数据以确保包含所有记录
          _t: Date.now()
        }
      })
    ]);
    
    const { records, total } = extractPageData(pageRes);
    allQa.value = records;
    totalItems.value = total;
    hotQa.value = allQa.value.slice(0, 5);
    
    // 加载所有数据用于统计
    const allData = extractPageData(allRes);
    allQaForStats.value = allData.records;
    
    if (isLoggedIn.value) {
      try { favorites.value = extractData(await request.get("/favorites/my")); } catch {}
    }
  } catch { ElMessage.error("加载失败"); }
  finally { pageLoading.value = false; }
};

onMounted(loadQaData);

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
</style>
