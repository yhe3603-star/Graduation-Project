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
          v-if="filteredList.length"
          :page="currentPage"
          :size="pageSize"
          :total="filteredList.length"
          @update:page="currentPage = $event"
          @update:size="pageSize = $event"
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
import { extractData } from "@/utils";
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
const hotPlants = ref([]);
const detailVisible = ref(false);
const currentPlant = ref(null);
const favorites = ref([]);
const catFilter = ref("");
const useFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);

const filterConfig = [
  { key: "category", label: "类型", options: [{ label: "全部", value: "" }, { label: "根茎类", value: "根茎类" }, { label: "全草类", value: "全草类" }, { label: "花叶类", value: "花叶类" }] },
  { key: "usage", label: "用法", type: "success", options: [{ label: "全部", value: "" }, { label: "内服", value: "内服" }, { label: "外用", value: "外用" }, { label: "药浴", value: "药浴" }] }
];

const stats = computed(() => {
  const totalViews = allPlants.value.reduce((sum, p) => sum + (p.viewCount || 0), 0);
  const totalFavorites = allPlants.value.reduce((sum, p) => sum + (p.favoriteCount || 0), 0);
  return [
    { value: allPlants.value.length, label: "药材总数" },
    { value: new Set(allPlants.value.map(p => p.category)).size, label: "分类数" },
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

const filteredList = computed(() => {
  let result = allPlants.value;
  const kw = keyword.value.toLowerCase();
  if (kw) {
    result = result.filter(p => 
      p.nameCn?.toLowerCase().includes(kw) || 
      p.efficacy?.toLowerCase().includes(kw) ||
      p.nameDong?.toLowerCase().includes(kw)
    );
  }
  if (catFilter.value) result = result.filter(p => p.category === catFilter.value);
  if (useFilter.value) result = result.filter(p => p.usageWay?.includes(useFilter.value));
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

const handleFilter = (filters) => { 
  catFilter.value = filters.category || ""; 
  useFilter.value = filters.usage || ""; 
  currentPage.value = 1; 
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
  } catch {}
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
  } catch { ElMessage.error("操作失败"); return false; }
};

const toggleFavorite = () => { if (currentPlant.value) doToggleFavorite(currentPlant.value.id, isFavorited.value); };
const toggleFavoriteCard = (item) => { doToggleFavorite(item.id, isFavoritedCard(item.id)); };

const loadPlantsData = async () => {
  pageLoading.value = true;
  try {
    const res = await request.get("/plants/list", { params: { _t: Date.now() } });
    allPlants.value = extractData(res);
    hotPlants.value = [...allPlants.value].sort((a, b) => (b.popularity || 0) - (a.popularity || 0)).slice(0, 5);
    if (isLoggedIn.value) {
      try { favorites.value = extractData(await request.get("/favorites/my")); } catch {}
    }
  } catch { ElMessage.error("加载失败"); }
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
