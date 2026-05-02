<template>
  <div class="plants-page module-page">
    <div class="module-header">
      <h1>药用资源图鉴</h1>
      <p class="subtitle">
        黔东南道地药材 · 侗医传统药用 · 图文详解
      </p>
    </div>

    <div class="plants-container">
      <div class="plants-main">
        <SearchFilter
          v-model="keyword"
          placeholder="搜索药材名称、功效、侗语名..."
          :filters="filterConfig"
          @search="handleSearch"
          @filter="handleFilter"
        />

        <SkeletonGridImage
          v-if="pageLoading"
          :count="12"
        />

        <CardGrid
          v-else
          :items="paginatedList"
          title-field="nameCn"
          desc-length="40"
          @click="showDetail"
        >
          <template #footer="{ item }">
            <el-tag
              size="small"
              effect="light"
            >
              <el-icon><Location /></el-icon>{{ item.usageWay }}
            </el-tag>
            <span class="habitat">{{ (item.habitat || '').substring(0, 12) }}...</span>
            <div class="card-stats">
              <span class="stat-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
              <span class="stat-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
            </div>
            <el-button
              :type="isItemFavorited(item.id) ? 'warning' : 'default'"
              size="small"
              class="favorite-btn"
              :class="{ favorited: isItemFavorited(item.id) }"
              @click.stop="toggleFavoriteCard(item)"
            >
              <el-icon><component :is="isItemFavorited(item.id) ? StarFilled : Star" /></el-icon>
            </el-button>
          </template>
        </CardGrid>

        <el-empty
          v-if="!filteredList.length && !pageLoading"
          description="暂无药材数据"
          :image-size="120"
        />
        <Pagination
          v-if="totalItems > 0"
          :page="currentPage"
          :size="pageSize"
          :total="totalItems"
          @update:page="currentPage = $event; loadPlantsData();"
          @update:size="pageSize = $event; currentPage = 1; loadPlantsData();"
        />
      </div>

      <PageSidebar
        title="药材统计"
        :stats="stats"
        hot-title="热门药材"
        :hot-items="hotPlants"
        @hot-click="showDetail"
      >
        <UpdateLogCard 
          :logs="allUpdateLogs" 
          title="更新日志" 
          :limit="5"
        />
      </PageSidebar>
    </div>

    <PlantDetailDialog
      v-model:visible="detailVisible"
      :plant="currentPlant"
      :is-favorited="isFavorited"
      @toggle-favorite="toggleFavorite"
    />

  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import request from '@/utils/request';
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Location, Plus, Star, StarFilled, View } from "@element-plus/icons-vue";
import CardGrid from "@/components/business/display/CardGrid.vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import PlantDetailDialog from "@/components/business/dialogs/PlantDetailDialog.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import SkeletonGridImage from "@/components/common/SkeletonGridImage.vue";
import { extractPageData, logFetchError } from "@/utils";
import { useUpdateLog } from "@/composables/useUpdateLog";
import { useDebounceFn } from "@/composables/useDebounce";
import { useFavorite } from "@/composables/useFavorite";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 12,
  LARGE: 24,
  SMALL: 8
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();
const router = useRouter();

const { items: favItems, isFavorited: isItemFavorited, loadFavorites, toggleFavorite: doToggleFavorite } = useFavorite('plant');
const pageLoading = ref(false);
const keyword = ref("");
const allPlants = ref([]);
const hotPlants = ref([]);
const detailVisible = ref(false);
const currentPlant = ref(null);
const catFilter = ref("");
const useFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);
const totalItems = ref(0);
const statsData = ref({ total: 0, categoryCount: 0, totalFavorites: 0, totalViews: 0 });

const filterConfig = ref([
  { key: "category", label: "类型", options: [{ label: "全部", value: "" }] },
  { key: "usage", label: "用法", type: "success", options: [{ label: "全部", value: "" }] }
]);

const stats = computed(() => [
  { value: statsData.value.total, label: "药材总数" },
  { value: statsData.value.categoryCount, label: "分类数" },
  { value: statsData.value.totalFavorites, label: "收藏量" },
  { value: statsData.value.totalViews, label: "浏览量" }
]);

const { parseUpdateLog } = useUpdateLog();

const allUpdateLogs = computed(() => {
  const logs = [];
  allPlants.value.forEach(item => {
    const itemLogs = parseUpdateLog(item.updateLog);
    itemLogs.forEach(log => {
      logs.push({ ...log, plantId: item.id, plantName: item.nameCn });
    });
  });
  return logs.sort((a, b) => new Date(b.time) - new Date(a.time)).slice(0, 10);
});

const filteredList = computed(() => allPlants.value);

const paginatedList = computed(() => allPlants.value);

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1;
  loadPlantsData();
}, DEBOUNCE_DELAY);

const handleSearch = () => {
  debouncedSearch();
};

const handleFilter = (filters) => { 
  catFilter.value = filters.category || ""; 
  useFilter.value = filters.usage || ""; 
  currentPage.value = 1;
  loadPlantsData();
};

const isFavorited = computed(() => currentPlant.value && isItemFavorited(currentPlant.value.id));

const showDetail = async (plant) => {
  currentPlant.value = plant;
  detailVisible.value = true;
  try {
    await request.post(`/plants/${plant.id}/view`);
    const idx = allPlants.value.findIndex(p => p.id === plant.id);
    if (idx > -1) {
      allPlants.value[idx].viewCount = (allPlants.value[idx].viewCount || 0) + 1;
      statsData.value.totalViews = (statsData.value.totalViews || 0) + 1;
    }
  } catch (e) {
    console.debug('浏览量更新失败:', e);
  }
};

const toggleFavorite = () => { if (currentPlant.value) doToggleFavorite(currentPlant.value.id, isFavorited.value); };
const toggleFavoriteCard = (item) => { doToggleFavorite(item.id, isItemFavorited(item.id)); };

const loadPlantsData = async () => {
  pageLoading.value = true;
  try {
    const [pageRes, statsRes] = await Promise.all([
      request.get("/plants/list", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          keyword: keyword.value,
          category: catFilter.value,
          usageWay: useFilter.value,
          _t: Date.now()
        }
      }),
      request.get("/stats/plants")
    ]);
    
    const { records, total } = extractPageData(pageRes);
    allPlants.value = records;
    favItems.value = allPlants.value;
    totalItems.value = total;
    hotPlants.value = [...allPlants.value].sort((a, b) => (b.popularity || 0) - (a.popularity || 0)).slice(0, 5);
    
    const sd = statsRes.data || statsRes || {};
    statsData.value = { total: sd.total || 0, categoryCount: sd.categoryCount || 0, totalFavorites: sd.totalFavorites || 0, totalViews: sd.totalViews || 0 };
    
    await loadFavorites();
  } catch (e) {
    logFetchError('植物数据', e);
    ElMessage.error("加载失败");
  }
  finally { pageLoading.value = false; }
};

const loadMetadata = async () => {
  try {
    const res = await request.get("/metadata/filters");
    const data = res.data || res || {};
    const plantsFilters = data.plants || {};
    const categories = plantsFilters.category || [];
    const usageWays = plantsFilters.usageWay || [];
    filterConfig.value = [
      { key: "category", label: "类型", options: [{ label: "全部", value: "" }, ...categories.map(c => ({ label: c, value: c }))] },
      { key: "usage", label: "用法", type: "success", options: [{ label: "全部", value: "" }, ...usageWays.map(u => ({ label: u, value: u }))] }
    ];
  } catch {}
};

onMounted(() => { loadMetadata(); loadPlantsData(); });

watch(() => route.path, (newPath) => {
  if (newPath === '/plants') loadPlantsData();
});

watch(() => route.query.id, async (id) => {
  if (!id) return
  const numId = Number(id)
  await nextTick()
  const item = allPlants.value.find(p => p.id === numId)
  if (item) {
    showDetail(item)
  } else {
    try {
      const res = await request.get(`/plants/${numId}`)
      if (res.data) showDetail(res.data)
    } catch {}
  }
}, { immediate: true })
</script>

<style scoped>
.plants-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--space-xl);
}

.habitat {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

@media (max-width: 1024px) {
  .plants-container {
    grid-template-columns: 1fr;
  }
}

.compare-btn {
  margin-top: var(--space-xs);
  width: 100%;
}

.floating-compare-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-md) var(--space-xl);
  background: var(--text-inverse);
  border-radius: 48px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-light);
}

.bar-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.bar-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  white-space: nowrap;
}

.bar-label strong {
  color: var(--dong-blue);
  font-size: var(--font-size-md);
}

.bar-thumbs {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.bar-thumb-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--bg-rice);
  border-radius: 20px;
  font-size: var(--font-size-xs);
  color: var(--text-primary);
}

.bar-remove {
  cursor: pointer;
  font-size: 12px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.bar-remove:hover {
  color: var(--color-danger);
}

.bar-right {
  flex-shrink: 0;
}

.float-bar-fade-enter-active,
.float-bar-fade-leave-active {
  transition: all 0.3s ease;
}

.float-bar-fade-enter-from,
.float-bar-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}

@media (max-width: 768px) {
  .plants-container {
    gap: var(--space-lg);
  }

  .habitat {
    display: none;
  }

  .favorite-btn {
    position: absolute;
    top: var(--space-sm);
    right: var(--space-sm);
  }

  .floating-compare-bar {
    bottom: 12px;
    left: 12px;
    right: 12px;
    transform: none;
    flex-direction: column;
    gap: var(--space-md);
    padding: var(--space-md);
    border-radius: var(--radius-lg);
  }

  .bar-left {
    flex-direction: column;
    gap: var(--space-sm);
    width: 100%;
  }

  .bar-thumbs {
    justify-content: center;
  }

  .bar-right {
    width: 100%;
  }

  .bar-right .el-button {
    width: 100%;
  }
}
</style>
