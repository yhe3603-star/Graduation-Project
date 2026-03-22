<template>
  <div
    v-loading="pageLoading"
    class="knowledge-page module-page"
  >
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

        <div class="knowledge-list">
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
                :type="favorites.includes(item.id) ? 'warning' : 'default'"
                size="small"
                class="favorite-btn"
                :class="{ favorited: favorites.includes(item.id) }"
                @click.stop="toggleFavorite(item)"
              >
                <el-icon><component :is="favorites.includes(item.id) ? 'StarFilled' : 'Star'" /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!filteredList.length && !pageLoading"
          description="暂无相关知识"
        />
        <Pagination
          v-if="filteredList.length"
          :page="currentPage"
          :size="pageSize"
          :total="filteredList.length"
          @update:page="currentPage = $event"
          @update:size="pageSize = $event"
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
import { computed, inject, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Bell, Document, Star, StarFilled, View } from "@element-plus/icons-vue";
import KnowledgeDetailDialog from "@/components/business/dialogs/KnowledgeDetailDialog.vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import { extractData } from "@/utils";
import { useUpdateLog } from "@/composables/useUpdateLog";
import { useDebounceFn } from "@/composables/useDebounce";
import { useUserStore } from "@/stores/user";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 12,
  LARGE: 24,
  SMALL: 6
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();
const request = inject("request");
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const pageLoading = ref(false);
const keyword = ref("");
const allKnowledge = ref([]);
const hotKnowledge = ref([]);
const favorites = ref([]);
const detailVisible = ref(false);
const currentItem = ref(null);

const therapyFilter = ref("");
const diseaseFilter = ref("");

const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);

const filterConfig = [
  { key: "therapy", label: "疗法", options: [{ label: "全部", value: "" }, { label: "药浴疗法", value: "药浴疗法" }, { label: "艾灸疗法", value: "艾灸疗法" }, { label: "推拿疗法", value: "推拿疗法" }] },
  { key: "disease", label: "疾病", type: "success", options: [{ label: "全部", value: "" }, { label: "风湿骨痛", value: "风湿骨痛" }, { label: "妇科疾病", value: "妇科疾病" }, { label: "儿科疾病", value: "儿科疾病" }] }
];

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

const stats = computed(() => {
  const totalViews = allKnowledge.value.reduce((sum, k) => sum + (k.viewCount || 0), 0);
  const totalFavorites = allKnowledge.value.reduce((sum, k) => sum + (k.favoriteCount || 0), 0);
  return [
    { value: allKnowledge.value.length, label: "知识总数" },
    { value: new Set(allKnowledge.value.map(k => k.therapyCategory).filter(Boolean)).size, label: "疗法分类" },
    { value: new Set(allKnowledge.value.map(k => k.diseaseCategory).filter(Boolean)).size, label: "疾病分类" },
    { value: new Set(allKnowledge.value.map(k => k.type).filter(Boolean)).size, label: "类型" },
    { value: totalFavorites, label: "收藏量" },
    { value: totalViews, label: "浏览量" }
  ];
});

const filteredList = computed(() => {
  let result = allKnowledge.value;
  const kw = keyword.value.toLowerCase();
  if (kw) {
    result = result.filter(k => 
      k.title?.toLowerCase().includes(kw) || 
      k.content?.toLowerCase().includes(kw)
    );
  }
  if (therapyFilter.value) result = result.filter(k => k.therapyCategory === therapyFilter.value);
  if (diseaseFilter.value) result = result.filter(k => k.diseaseCategory === diseaseFilter.value);
  return result;
});

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1;
}, DEBOUNCE_DELAY);

const handleSearch = () => {
  debouncedSearch();
};

const handleFilter = (filters) => { therapyFilter.value = filters.therapy || ""; diseaseFilter.value = filters.disease || ""; currentPage.value = 1; };

const isFavorited = computed(() => currentItem.value && favorites.value.includes(currentItem.value.id));

const showDetail = async (item) => {
  currentItem.value = item;
  detailVisible.value = true;
  try {
    await request.post(`/knowledge/${item.id}/view`);
    const idx = allKnowledge.value.findIndex(k => k.id === item.id);
    if (idx > -1) allKnowledge.value[idx].viewCount = (allKnowledge.value[idx].viewCount || 0) + 1;
  } catch {}
};

const toggleFavorite = async (item) => {
  if (!isLoggedIn.value) { ElMessage.warning("请先登录"); return; }
  try {
    const idx = allKnowledge.value.findIndex(k => k.id === item.id);
    const favIdx = favorites.value.indexOf(item.id);
    if (favIdx > -1) {
      await request.delete(`/favorites/knowledge/${item.id}`);
      favorites.value.splice(favIdx, 1);
      if (idx > -1) allKnowledge.value[idx].favoriteCount = Math.max(0, (allKnowledge.value[idx].favoriteCount || 1) - 1);
      ElMessage.success("已取消收藏");
    } else {
      await request.post(`/favorites/knowledge/${item.id}`);
      favorites.value.push(item.id);
      if (idx > -1) allKnowledge.value[idx].favoriteCount = (allKnowledge.value[idx].favoriteCount || 0) + 1;
      ElMessage.success("收藏成功");
    }
  } catch { ElMessage.error("操作失败"); }
};

const toggleFavoriteDetail = () => { if (currentItem.value) toggleFavorite(currentItem.value); };

const loadKnowledgeData = async () => {
  pageLoading.value = true;
  try {
    const res = await request.get("/knowledge/list", { params: { _t: Date.now() } });
    allKnowledge.value = extractData(res);
    hotKnowledge.value = allKnowledge.value.slice(0, 5);
    if (isLoggedIn.value) {
      try {
        const favData = extractData(await request.get("/favorites/my"));
        favorites.value = favData.filter(f => f.type === "knowledge").map(f => f.targetId);
      } catch {}
    }
  } catch { ElMessage.error("加载失败"); }
  finally { pageLoading.value = false; }
};

onMounted(loadKnowledgeData);

watch(() => route.path, (newPath) => {
  if (newPath === '/knowledge') loadKnowledgeData();
});
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
