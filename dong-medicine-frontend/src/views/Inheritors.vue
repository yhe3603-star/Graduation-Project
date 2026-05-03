<template>
  <div class="inheritors-page module-page">
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

        <SkeletonGridCard
          v-if="pageLoading"
          :count="12"
        />

        <div
          v-else
          class="inheritor-grid"
        >
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
                :type="isItemFavorited(item.id) ? 'warning' : 'default'"
                size="small"
                class="favorite-btn"
                :class="{ favorited: isItemFavorited(item.id) }"
                @click.stop="doToggleFavorite(item.id, isItemFavorited(item.id))"
              >
                <el-icon><component :is="isItemFavorited(item.id) ? StarFilled : Star" /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!filteredList.length && !pageLoading"
          description="暂无传承人数据"
        />
        <Pagination
          v-if="totalItems > 0"
          :page="currentPage"
          :size="pageSize"
          :total="totalItems"
          @update:page="currentPage = $event; loadInheritorsData();"
          @update:size="pageSize = $event; currentPage = 1; loadInheritorsData();"
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
import { computed, nextTick, onMounted, ref, watch } from "vue";
import request from '@/utils/request';
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Medal, Star, StarFilled, View } from "@element-plus/icons-vue";
import InheritorDetailDialog from "@/components/business/dialogs/InheritorDetailDialog.vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import SkeletonGridCard from "@/components/common/SkeletonGridCard.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import { extractPageData } from "@/utils";
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

const { isFavorited: isItemFavorited, loadFavorites, toggleFavorite: doToggleFavorite, items: favItems } = useFavorite('inheritor');

const pageLoading = ref(false);
const keyword = ref("");
const allInheritors = ref([]);
const featuredInheritors = ref([]);
const detailVisible = ref(false);
const currentInheritor = ref(null);
const levelFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(PAGE_SIZE_OPTIONS.DEFAULT);
const totalItems = ref(0);
const statsData = ref({ total: 0, regionLevelCount: 0, cityLevelCount: 0, countyLevelCount: 0, totalFavorites: 0, totalViews: 0 });

const filterConfig = ref([
  { key: "level", label: "级别", options: [{ label: "全部", value: "" }] }
]);

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

const stats = computed(() => [
  { value: statsData.value.total, label: "传承人总数" },
  { value: statsData.value.regionLevelCount, label: "自治区级" },
  { value: statsData.value.cityLevelCount, label: "市级" },
  { value: statsData.value.countyLevelCount, label: "县级" },
  { value: statsData.value.totalFavorites, label: "收藏量" },
  { value: statsData.value.totalViews, label: "浏览量" }
]);

const filteredList = computed(() => allInheritors.value);

const paginatedList = computed(() => allInheritors.value);

const getLevelType = (level) => ({ "国家级": "danger", "自治区级": "success", "市级": "primary", "县级": "info" }[level] || "info");

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1;
  loadInheritorsData();
}, DEBOUNCE_DELAY);

const handleSearch = () => {
  debouncedSearch();
};

const handleFilter = (filters) => { 
  levelFilter.value = filters.level || ""; 
  currentPage.value = 1;
  loadInheritorsData();
};

const isFavorited = computed(() => currentInheritor.value && isItemFavorited(currentInheritor.value.id));

const showDetail = (item) => {
  currentInheritor.value = item;
  detailVisible.value = true;
};

const toggleFavorite = () => { if (currentInheritor.value) doToggleFavorite(currentInheritor.value.id, isItemFavorited(currentInheritor.value.id)); };

const loadInheritorsData = async () => {
  pageLoading.value = true;
  try {
    const [pageRes, statsRes] = await Promise.all([
      request.get("/inheritors/list", {
        params: {
          page: currentPage.value,
          size: pageSize.value,
          level: levelFilter.value || undefined,
          keyword: keyword.value,
          _t: Date.now()
        }
      }),
      request.get("/stats/inheritors")
    ]);
    
    const { records, total } = extractPageData(pageRes);
    allInheritors.value = records;
    favItems.value = allInheritors.value;
    totalItems.value = total;
    featuredInheritors.value = [...allInheritors.value].sort((a, b) => (b.popularity || 0) - (a.popularity || 0)).slice(0, 5);
    
    const sd = statsRes.data || statsRes || {};
    statsData.value = { total: sd.total || 0, regionLevelCount: sd.regionLevelCount || 0, cityLevelCount: sd.cityLevelCount || 0, countyLevelCount: sd.countyLevelCount || 0, totalFavorites: sd.totalFavorites || 0, totalViews: sd.totalViews || 0 };
    
    await loadFavorites();
  } catch (e) {
    console.error('加载传承人数据失败:', e);
    ElMessage.error("加载失败");
  }
  finally { pageLoading.value = false; }
};

const loadMetadata = async () => {
  try {
    const res = await request.get("/metadata/filters");
    const data = res.data || res || {};
    const inheritorsFilters = data.inheritors || {};
    const levels = inheritorsFilters.level || [];
    filterConfig.value = [
      { key: "level", label: "级别", options: [{ label: "全部", value: "" }, ...levels.map(l => ({ label: l, value: l }))] }
    ];
  } catch {}
};

onMounted(() => { loadMetadata(); loadInheritorsData(); });

watch(() => route.path, (newPath) => {
  if (newPath === '/inheritors') loadInheritorsData();
});

watch(() => route.query.id, async (id) => {
  if (!id) return
  const numId = Number(id)
  await nextTick()
  const item = allInheritors.value.find(i => i.id === numId)
  if (item) {
    showDetail(item)
  } else {
    try {
      const res = await request.get(`/inheritors/${numId}`)
      if (res.data) showDetail(res.data)
    } catch {}
  }
}, { immediate: true })
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
