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

        <SkeletonGrid
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
              :type="isFavoritedCard(item.id) ? 'warning' : 'default'"
              size="small"
              class="favorite-btn"
              :class="{ favorited: isFavoritedCard(item.id) }"
              @click.stop="toggleFavoriteCard(item)"
            >
              <el-icon><component :is="isFavoritedCard(item.id) ? 'StarFilled' : 'Star'" /></el-icon>
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
import { computed, inject, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Location, Star, StarFilled, View } from "@element-plus/icons-vue";
import CardGrid from "@/components/business/display/CardGrid.vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import PlantDetailDialog from "@/components/business/dialogs/PlantDetailDialog.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import SkeletonGrid from "@/components/common/SkeletonGrid.vue";
import { extractPageData, extractData, logFetchError } from "@/utils";
import { useUpdateLog } from "@/composables/useUpdateLog";
import { useDebounceFn } from "@/composables/useDebounce";
import { useUserStore } from "@/stores/user";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 12,
  LARGE: 24,
  SMALL: 8
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();
const request = inject("request");
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const pageLoading = ref(false);
const keyword = ref("");
const allPlants = ref([]);
const allPlantsForStats = ref([]);
const hotPlants = ref([]);
const detailVisible = ref(false);
const currentPlant = ref(null);
const favorites = ref([]);
const catFilter = ref("");
const useFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);
const totalItems = ref(0);

const filterConfig = [
  { key: "category", label: "类型", options: [{ label: "全部", value: "" }, { label: "草本", value: "草本" }, { label: "木本", value: "木本" }] },
  { key: "usage", label: "用法", type: "success", options: [{ label: "全部", value: "" }, { label: "内服", value: "内服" }, { label: "外用", value: "外用" }, { label: "药浴", value: "药浴" }] }
];

const stats = computed(() => {
  const data = allPlantsForStats.value.length > 0 ? allPlantsForStats.value : allPlants.value;
  const totalViews = data.reduce((sum, p) => sum + (p.viewCount || 0), 0);
  const totalFavorites = data.reduce((sum, p) => sum + (p.favoriteCount || 0), 0);
  return [
    { value: data.length, label: "药材总数" },
    { value: new Set(data.map(p => p.category)).size, label: "分类数" },
    { value: totalFavorites, label: "收藏量" },
    { value: totalViews, label: "浏览量" }
  ];
});

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

const isFavorited = computed(() => currentPlant.value && favorites.value.some(f => f.targetId === currentPlant.value.id && f.type === "plant"));
const isFavoritedCard = (id) => favorites.value.some(f => f.targetId === id && f.type === "plant");

const showDetail = async (plant) => {
  currentPlant.value = plant;
  detailVisible.value = true;
  try {
    await request.post(`/plants/${plant.id}/view`);
    const idx = allPlants.value.findIndex(p => p.id === plant.id);
    if (idx > -1) allPlants.value[idx].viewCount = (allPlants.value[idx].viewCount || 0) + 1;
    const statsIdx = allPlantsForStats.value.findIndex(p => p.id === plant.id);
    if (statsIdx > -1) allPlantsForStats.value[statsIdx].viewCount = (allPlantsForStats.value[statsIdx].viewCount || 0) + 1;
  } catch (e) {
    console.debug('浏览量更新失败:', e);
  }
};

const updateFavorite = (id, delta) => {
  const idx = allPlants.value.findIndex(p => p.id === id);
  if (idx > -1) allPlants.value[idx].favoriteCount = Math.max(0, (allPlants.value[idx].favoriteCount || 0) + delta);
};

const doToggleFavorite = async (id, isFav) => {
  if (!isLoggedIn.value) { ElMessage.warning("请先登录"); return false; }
  try {
    if (isFav) {
      await request.delete(`/favorites/plant/${id}`);
      favorites.value = favorites.value.filter(f => !(f.targetId === id && f.type === "plant"));
      updateFavorite(id, -1);
      ElMessage.success("已取消收藏");
    } else {
      await request.post(`/favorites/plant/${id}`);
      favorites.value.push({ targetId: id, type: "plant" });
      updateFavorite(id, 1);
      ElMessage.success("收藏成功");
    }
    return true;
  } catch (e) {
    logFetchError('收藏操作', e);
    ElMessage.error("操作失败"); return false;
  }
};

const toggleFavorite = () => { if (currentPlant.value) doToggleFavorite(currentPlant.value.id, isFavorited.value); };
const toggleFavoriteCard = (item) => { doToggleFavorite(item.id, isFavoritedCard(item.id)); };

const loadPlantsData = async () => {
  pageLoading.value = true;
  try {
    // 并行请求：获取分页数据和所有数据用于统计
    const [pageRes, allRes] = await Promise.all([
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
      request.get("/plants/list", {
        params: {
          page: 1,
          size: 9999, // 获取足够多的数据以确保包含所有记录
          _t: Date.now()
        }
      })
    ]);
    
    const { records, total } = extractPageData(pageRes);
    allPlants.value = records;
    totalItems.value = total;
    hotPlants.value = [...allPlants.value].sort((a, b) => (b.popularity || 0) - (a.popularity || 0)).slice(0, 5);
    
    // 加载所有数据用于统计
    const allData = extractPageData(allRes);
    allPlantsForStats.value = allData.records;
    
    if (isLoggedIn.value) {
      try { favorites.value = extractData(await request.get("/favorites/my")); } catch (e) { console.debug('加载收藏失败:', e); }
    }
  } catch (e) {
    logFetchError('植物数据', e);
    ElMessage.error("加载失败");
  }
  finally { pageLoading.value = false; }
};

onMounted(loadPlantsData);

watch(() => route.path, (newPath) => {
  if (newPath === '/plants') loadPlantsData();
});
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
}
</style>
