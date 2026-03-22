<template>
  <div
    v-loading="pageLoading"
    class="inheritors-page module-page"
  >
    <div class="module-header">
      <h1>传承人风采</h1>
      <p class="subtitle">
        非遗传承谱系 · 技艺特色 · 代表案例
      </p>
    </div>

    <div class="inheritors-container">
      <div class="inheritors-main">
        <SearchFilter
          v-model="keyword"
          placeholder="搜索传承人姓名、技艺..."
          :filters="filterConfig"
          @search="handleSearch"
          @filter="handleFilter"
        />

        <div class="inheritor-grid">
          <div
            v-for="item in paginatedList"
            :key="item.id"
            class="inheritor-card"
            @click="showDetail(item)"
          >
            <div class="inheritor-avatar">
              {{ (item.name || '传').charAt(0) }}
            </div>
            <div class="inheritor-info">
              <h3>{{ item.name }}</h3>
              <el-tag
                size="small"
                :type="getLevelType(item.level)"
                effect="dark"
              >
                {{ item.level }}
              </el-tag>
              <p class="inheritor-specialty">
                {{ item.specialties }}
              </p>
            </div>
            <div
              v-if="item.level === '自治区级'"
              class="inheritor-badge"
            >
              <el-icon><Medal /></el-icon>
            </div>
            <div class="card-stats">
              <span class="stat-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
              <span class="stat-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
            </div>
            <div
              class="card-actions"
              @click.stop
            >
              <el-button
                :type="isFavoritedCard(item.id) ? 'warning' : 'default'"
                size="small"
                class="favorite-btn"
                :class="{ favorited: isFavoritedCard(item.id) }"
                @click.stop="toggleFavoriteCard(item)"
              >
                <el-icon><component :is="isFavoritedCard(item.id) ? 'StarFilled' : 'Star'" /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!filteredList.length && !pageLoading"
          description="暂无传承人数据"
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
        title="传承人统计"
        :stats="stats"
        hot-title="代表传承人"
        :hot-items="featuredInheritors"
        @hot-click="showDetail"
      >
        <UpdateLogCard 
          :logs="allUpdateLogs" 
          title="更新日志" 
          :limit="5"
        />
      </PageSidebar>
    </div>

    <InheritorDetailDialog 
      v-model:visible="detailVisible" 
      :inheritor="currentInheritor" 
      :is-favorited="isFavorited"
      @toggle-favorite="toggleFavorite"
    />
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Medal, Star, StarFilled, View } from "@element-plus/icons-vue";
import InheritorDetailDialog from "@/components/business/dialogs/InheritorDetailDialog.vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import { extractData } from "@/utils";
import { useUpdateLog } from "@/composables/useUpdateLog";
import { useDebounceFn } from "@/composables/useDebounce";
import { useUserStore } from "@/stores/user";

const PAGE_SIZE_OPTIONS = {
  DEFAULT: 24,
  LARGE: 48,
  SMALL: 12
};

const DEBOUNCE_DELAY = 300;

const route = useRoute();
const request = inject("request");
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const pageLoading = ref(false);
const keyword = ref("");
const allInheritors = ref([]);
const featuredInheritors = ref([]);
const detailVisible = ref(false);
const currentInheritor = ref(null);
const favorites = ref([]);
const levelFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);

const filterConfig = [
  { key: "level", label: "级别", options: [{ label: "全部", value: "" }, { label: "自治区级", value: "自治区级" }, { label: "市级", value: "市级" }, { label: "县级", value: "县级" }] }
];

const { parseUpdateLog } = useUpdateLog();

const allUpdateLogs = computed(() => {
  const logs = [];
  allInheritors.value.forEach(item => {
    const itemLogs = parseUpdateLog(item.updateLog);
    itemLogs.forEach(log => {
      logs.push({
        ...log,
        inheritorId: item.id,
        inheritorName: item.name
      });
    });
  });
  return logs.sort((a, b) => new Date(b.time) - new Date(a.time)).slice(0, 10);
});

const stats = computed(() => {
  const total = allInheritors.value.length;
  const totalViews = allInheritors.value.reduce((sum, i) => sum + (i.viewCount || 0), 0);
  const totalFavorites = allInheritors.value.reduce((sum, i) => sum + (i.favoriteCount || 0), 0);
  return [
    { value: total, label: "传承人总数" },
    { value: allInheritors.value.filter(i => i.level === "自治区级").length, label: "自治区级" },
    { value: allInheritors.value.filter(i => i.level === "市级").length, label: "市级" },
    { value: allInheritors.value.filter(i => i.level === "县级").length, label: "县级" },
    { value: totalFavorites, label: "收藏量" },
    { value: totalViews, label: "浏览量" }
  ];
});

const filteredList = computed(() => {
  let result = allInheritors.value;
  const kw = keyword.value.toLowerCase();
  if (kw) {
    result = result.filter(i => 
      i.name?.toLowerCase().includes(kw) || 
      i.specialties?.toLowerCase().includes(kw)
    );
  }
  if (levelFilter.value) result = result.filter(i => i.level === levelFilter.value);
  return result;
});

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

const getLevelType = (level) => ({ "自治区级": "success", "市级": "primary", "县级": "info" }[level] || "info");

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1;
}, DEBOUNCE_DELAY);

const handleSearch = () => {
  debouncedSearch();
};

const handleFilter = (filters) => { levelFilter.value = filters.level || ""; currentPage.value = 1; };

const isFavorited = computed(() => currentInheritor.value && favorites.value.some(f => f.targetId === currentInheritor.value.id && f.type === "inheritor"));
const isFavoritedCard = (id) => favorites.value.some(f => f.targetId === id && f.type === "inheritor");

const showDetail = async (item) => {
  currentInheritor.value = item;
  detailVisible.value = true;
  try {
    await request.post(`/inheritors/${item.id}/view`);
    const idx = allInheritors.value.findIndex(i => i.id === item.id);
    if (idx > -1) allInheritors.value[idx].viewCount = (allInheritors.value[idx].viewCount || 0) + 1;
  } catch {}
};

const updateFavorite = (id, delta) => {
  const idx = allInheritors.value.findIndex(i => i.id === id);
  if (idx > -1) allInheritors.value[idx].favoriteCount = Math.max(0, (allInheritors.value[idx].favoriteCount || 0) + delta);
};

const doToggleFavorite = async (id, isFav) => {
  if (!isLoggedIn.value) { ElMessage.warning("请先登录"); return false; }
  try {
    if (isFav) {
      await request.delete(`/favorites/inheritor/${id}`);
      favorites.value = favorites.value.filter(f => !(f.targetId === id && f.type === "inheritor"));
      updateFavorite(id, -1);
      ElMessage.success("已取消收藏");
    } else {
      await request.post(`/favorites/inheritor/${id}`);
      favorites.value.push({ targetId: id, type: "inheritor" });
      updateFavorite(id, 1);
      ElMessage.success("收藏成功");
    }
    return true;
  } catch { ElMessage.error("操作失败"); return false; }
};

const toggleFavorite = () => { if (currentInheritor.value) doToggleFavorite(currentInheritor.value.id, isFavorited.value); };
const toggleFavoriteCard = (item) => { doToggleFavorite(item.id, isFavoritedCard(item.id)); };

const loadInheritorsData = async () => {
  pageLoading.value = true;
  try {
    const res = await request.get("/inheritors/list", { params: { _t: Date.now() } });
    allInheritors.value = extractData(res);
    featuredInheritors.value = [...allInheritors.value].sort((a, b) => (b.popularity || 0) - (a.popularity || 0)).slice(0, 5);
    if (isLoggedIn.value) {
      try { favorites.value = extractData(await request.get("/favorites/my")); } catch {}
    }
  } catch { ElMessage.error("加载失败"); }
  finally { pageLoading.value = false; }
};

onMounted(loadInheritorsData);

watch(() => route.path, (newPath) => {
  if (newPath === '/inheritors') loadInheritorsData();
});
</script>

<style scoped>
.inheritors-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--space-xl);
}

.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-xl);
}

.inheritor-card {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.inheritor-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.inheritor-avatar {
  width: 72px;
  height: 72px;
  margin: 0 auto var(--space-lg);
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
}

.inheritor-info {
  text-align: center;
}

.inheritor-info h3 {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-lg);
}

.inheritor-specialty {
  margin: var(--space-sm) 0 0 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.inheritor-badge {
  position: absolute;
  top: var(--space-md);
  right: var(--space-md);
  color: var(--dong-gold-light);
}

.card-stats {
  margin-top: var(--space-md);
  justify-content: center;
}

.card-actions {
  position: absolute;
  bottom: var(--space-md);
  right: var(--space-md);
}

@media (max-width: 1024px) {
  .inheritors-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .inheritor-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-md);
  }
  
  .inheritor-card {
    padding: var(--space-lg);
  }
  
  .inheritor-avatar {
    width: 56px;
    height: 56px;
    font-size: var(--font-size-xl);
    margin-bottom: var(--space-md);
  }
  
  .inheritor-info h3 {
    font-size: var(--font-size-base);
  }
  
  .inheritor-specialty {
    font-size: var(--font-size-xs);
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .card-stats {
    gap: var(--space-sm);
  }
  
  .card-actions {
    position: static;
    margin-top: var(--space-sm);
  }
}

@media (max-width: 480px) {
  .inheritor-grid {
    grid-template-columns: 1fr;
  }
  
  .inheritor-card {
    display: flex;
    flex-direction: row;
    align-items: center;
    text-align: left;
    gap: var(--space-lg);
  }
  
  .inheritor-avatar {
    margin: 0;
    flex-shrink: 0;
  }
  
  .inheritor-info {
    flex: 1;
    text-align: left;
  }
}
</style>
