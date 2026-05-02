<template>
  <div class="knowledge-page module-page">
    <div class="module-header">
      <h1>非遗医药知识库</h1>
      <p class="subtitle">
        分类检索 · 知识详情 · 收藏分享
      </p>
    </div>

    <div class="knowledge-container">
      <div class="knowledge-main">
        <SearchFilter
          v-model="keyword"
          placeholder="搜索药方或疗法..."
          :filters="filterConfig"
          @search="handleSearch"
          @filter="handleFilter"
        />

        <SkeletonGridCard
          v-if="pageLoading"
          :count="12"
        />

        <div
          v-else
          class="knowledge-list"
        >
          <div
            v-for="item in paginatedList"
            :key="item.id"
            class="knowledge-card"
            @click="showDetail(item)"
          >
            <div class="card-header">
              <div class="card-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="card-meta">
                <el-tag
                  size="small"
                  effect="light"
                >
                  {{ item.therapyCategory || '传统疗法' }}
                </el-tag>
                <span class="popularity"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                <span class="favorite-count"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
              </div>
            </div>
            <h3 class="card-title">
              {{ item.title }}
            </h3>
            <p class="card-desc">
              {{ (item.content || '').substring(0, 80) }}...
            </p>
            <div class="card-footer">
              <el-tag
                v-if="item.diseaseCategory"
                size="small"
                type="info"
              >
                {{ item.diseaseCategory }}
              </el-tag>
              <el-button
                :type="isItemFavorited(item.id) ? 'warning' : 'default'"
                size="small"
                class="favorite-btn"
                :class="{ favorited: isItemFavorited(item.id) }"
                @click.stop="toggleFavorite(item)"
              >
                <el-icon><component :is="isItemFavorited(item.id) ? StarFilled : Star" /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!filteredList.length && !pageLoading"
          description="暂无相关知识"
        />
        <Pagination
          v-if="totalItems > 0"
          :page="currentPage"
          :size="pageSize"
          :total="totalItems"
          @update:page="currentPage = $event; loadKnowledgeData();"
          @update:size="pageSize = $event; currentPage = 1; loadKnowledgeData();"
        />
      </div>

      <PageSidebar
        title="知识统计"
        :stats="stats"
        hot-title="热门知识"
        :hot-items="hotKnowledge"
        @hot-click="showDetail"
      >
        <UpdateLogCard 
          :logs="allUpdateLogs" 
          title="更新日志" 
          :limit="5"
        />
      </PageSidebar>
    </div>

    <KnowledgeDetailDialog 
      v-model:visible="detailVisible" 
      :knowledge="currentItem" 
      :is-favorited="isFavorited"
      @toggle-favorite="toggleFavoriteDetail"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import request from '@/utils/request';
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Bell, Document, Star, StarFilled, View } from "@element-plus/icons-vue";
import KnowledgeDetailDialog from "@/components/business/dialogs/KnowledgeDetailDialog.vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import SkeletonGridCard from "@/components/common/SkeletonGridCard.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import { extractPageData, logFetchError } from "@/utils";
import { useUpdateLog } from "@/composables/useUpdateLog";
import { useDebounceFn } from "@/composables/useDebounce";
import { useFavorite } from "@/composables/useFavorite";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 12,
  LARGE: 24,
  SMALL: 6
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();

const { items: favItems, isFavorited: isItemFavorited, loadFavorites, toggleFavorite: doToggleFavorite } = useFavorite('knowledge');

const pageLoading = ref(false);
const keyword = ref("");
const allKnowledge = ref([]);
const hotKnowledge = ref([]);
const detailVisible = ref(false);
const currentItem = ref(null);

const therapyFilter = ref("");
const diseaseFilter = ref("");
const herbFilter = ref("");

const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);
const totalItems = ref(0);
const statsData = ref({ total: 0, therapyCategoryCount: 0, diseaseCategoryCount: 0, typeCount: 0, totalFavorites: 0, totalViews: 0 });

const filterConfig = ref([
  { key: "therapy", label: "疗法", options: [{ label: "全部", value: "" }] },
  { key: "disease", label: "疾病", type: "success", options: [{ label: "全部", value: "" }] },
  { key: "herb", label: "药材", type: "warning", options: [{ label: "全部", value: "" }] }
]);

const { parseUpdateLog, stringifyUpdateLog, addLog, updateLog, deleteLog, formatLogTime } = useUpdateLog();

const allUpdateLogs = computed(() => {
  const logs = [];
  allKnowledge.value.forEach(item => {
    const itemLogs = parseUpdateLog(item.updateLog);
    itemLogs.forEach(log => {
      logs.push({
        ...log,
        knowledgeId: item.id,
        knowledgeTitle: item.title
      });
    });
  });
  return logs.sort((a, b) => new Date(b.time) - new Date(a.time)).slice(0, 10);
});

const stats = computed(() => [
  { value: statsData.value.total, label: "知识总数" },
  { value: statsData.value.therapyCategoryCount, label: "疗法分类" },
  { value: statsData.value.diseaseCategoryCount, label: "疾病分类" },
  { value: statsData.value.typeCount, label: "类型" },
  { value: statsData.value.totalFavorites, label: "收藏量" },
  { value: statsData.value.totalViews, label: "浏览量" }
]);

const filteredList = computed(() => allKnowledge.value);

const paginatedList = computed(() => allKnowledge.value);

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1;
  loadKnowledgeData();
}, DEBOUNCE_DELAY);

const handleSearch = () => {
  debouncedSearch();
};

const handleFilter = (filters) => { 
  therapyFilter.value = filters.therapy || ""; 
  diseaseFilter.value = filters.disease || ""; 
  herbFilter.value = filters.herb || ""; 
  currentPage.value = 1;
  loadKnowledgeData();
};

const isFavorited = computed(() => currentItem.value && isItemFavorited(currentItem.value.id));

const showDetail = async (item) => {
  currentItem.value = item;
  detailVisible.value = true;
  try {
    await request.post(`/knowledge/${item.id}/view`);
    const idx = allKnowledge.value.findIndex(k => k.id === item.id);
    if (idx > -1) {
      allKnowledge.value[idx].viewCount = (allKnowledge.value[idx].viewCount || 0) + 1;
      statsData.value.totalViews = (statsData.value.totalViews || 0) + 1;
    }
  } catch (e) {
    console.debug('浏览量更新失败:', e);
  }
};

const toggleFavorite = (item) => {
  if (item) doToggleFavorite(item.id, isItemFavorited(item.id));
};

const toggleFavoriteDetail = () => { if (currentItem.value) toggleFavorite(currentItem.value); };

const loadKnowledgeData = async () => {
  pageLoading.value = true;
  try {
    const [pageRes, statsRes] = await Promise.all([
      request.get("/knowledge/list", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          keyword: keyword.value,
          therapy: therapyFilter.value,
          disease: diseaseFilter.value,
          herb: herbFilter.value,
          _t: Date.now()
        }
      }),
      request.get("/stats/knowledge")
    ]);
    
    const { records, total } = extractPageData(pageRes);
    allKnowledge.value = records;
    favItems.value = allKnowledge.value;
    totalItems.value = total;
    hotKnowledge.value = allKnowledge.value.slice(0, 5);
    
    const sd = statsRes.data || statsRes || {};
    statsData.value = { total: sd.total || 0, therapyCategoryCount: sd.therapyCategoryCount || 0, diseaseCategoryCount: sd.diseaseCategoryCount || 0, typeCount: sd.typeCount || 0, totalFavorites: sd.totalFavorites || 0, totalViews: sd.totalViews || 0 };
    
    await loadFavorites();
  } catch (e) {
    logFetchError('知识数据', e);
    ElMessage.error("加载失败");
  }
  finally { pageLoading.value = false; }
};

const loadMetadata = async () => {
  try {
    const res = await request.get("/metadata/filters");
    const data = res.data || res || {};
    const knowledgeFilters = data.knowledge || {};
    const therapyCategories = knowledgeFilters.therapyCategory || [];
    const diseaseCategories = knowledgeFilters.diseaseCategory || [];
    const herbCategories = knowledgeFilters.herbCategory || [];
    filterConfig.value = [
      { key: "therapy", label: "疗法", options: [{ label: "全部", value: "" }, ...therapyCategories.map(c => ({ label: c, value: c }))] },
      { key: "disease", label: "疾病", type: "success", options: [{ label: "全部", value: "" }, ...diseaseCategories.map(c => ({ label: c, value: c }))] },
      { key: "herb", label: "药材", type: "warning", options: [{ label: "全部", value: "" }, ...herbCategories.map(c => ({ label: c, value: c }))] }
    ];
  } catch {}
};

onMounted(() => { loadMetadata(); loadKnowledgeData(); });

watch(() => route.path, (newPath) => {
  if (newPath === '/knowledge') loadKnowledgeData();
});

watch(() => route.query.id, async (id) => {
  if (!id) return
  const numId = Number(id)
  await nextTick()
  const item = allKnowledge.value.find(k => k.id === numId)
  if (item) {
    showDetail(item)
  } else {
    try {
      const res = await request.get(`/knowledge/${numId}`)
      if (res.data) showDetail(res.data)
    } catch {}
  }
}, { immediate: true })
</script>

<style scoped>
.knowledge-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--space-xl);
}

.knowledge-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-xl);
}

.knowledge-card {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.knowledge-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
}

.card-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.popularity,
.favorite-count {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.card-title {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-md);
  color: var(--text-primary);
}

.card-desc {
  margin: 0 0 var(--space-md) 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  line-height: var(--line-height-normal);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.update-card {
  margin-top: var(--space-xl);
  border-radius: var(--radius-md);
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--dong-indigo);
}

.timeline-desc {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
  .knowledge-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .knowledge-list {
    grid-template-columns: 1fr;
    gap: var(--space-md);
  }
  
  .knowledge-card {
    padding: var(--space-lg);
  }
  
  .card-header {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }
  
  .card-meta {
    width: 100%;
    justify-content: flex-start;
  }
  
  .card-title {
    font-size: var(--font-size-base);
  }
  
  .card-desc {
    font-size: var(--font-size-xs);
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .card-footer {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }
}

@media (max-width: 480px) {
  .knowledge-card {
    padding: var(--space-md);
  }
  
  .card-icon {
    width: 32px;
    height: 32px;
  }
}
</style>
