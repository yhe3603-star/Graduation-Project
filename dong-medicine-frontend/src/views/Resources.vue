<template>
  <div class="resources-page module-page">
    <div class="module-header">
      <h1>学习资源</h1>
      <p class="subtitle">侗医药学习资料库</p>
    </div>

    <div class="resources-container">
      <div class="resources-main">
        <SearchFilter
          v-model="keyword"
          placeholder="搜索资源名称..."
          :filters="filterConfig"
          @search="handleSearch"
          @filter="handleFilter"
        />

        <SkeletonListResource
          v-if="pageLoading"
          :count="6"
        />

        <div
          v-else
          class="resource-list"
        >
          <div
            v-for="item in paginatedList"
            :key="item.id"
            class="resource-card"
            :class="{ selected: selectedIds.has(item.id) }"
          >
            <div class="resource-checkbox" @click.stop>
              <el-checkbox
                :model-value="selectedIds.has(item.id)"
                @change="toggleSelect(item)"
              />
            </div>
            <div class="resource-card-body" @click="openResource(item)">
              <div class="resource-icon" :class="getTypeClass(getFileInfo(item).type)">
                <el-icon :size="28"><component :is="getTypeIcon(getFileInfo(item).type)" /></el-icon>
              </div>
              <div class="resource-info">
                <h3>{{ item.title }}</h3>
                <p class="resource-desc">{{ (item.description || '').substring(0, 60) }}...</p>
                <div class="resource-meta">
                  <el-tag size="small" effect="light">{{ item.category }}</el-tag>
                  <span class="resource-type">{{ getTypeName(getFileInfo(item).type) }}</span>
                  <span v-if="getFileExt(getFileInfo(item).url)" class="resource-ext">{{ getFileExt(getFileInfo(item).url).toUpperCase() }}</span>
                  <span v-if="getFileInfo(item).size" class="resource-size">{{ formatSize(getFileInfo(item).size) }}</span>
                  <div class="card-stats">
                    <span class="stat-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                    <span class="stat-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
                    <span class="stat-item"><el-icon><Download /></el-icon>{{ item.downloadCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="resource-actions" @click.stop>
              <el-button :type="isFavorited(item.id) ? 'warning' : 'default'" size="small" @click="toggleFavorite(item)">
                <el-icon><Star /></el-icon>{{ isFavorited(item.id) ? '已收藏' : '收藏' }}
              </el-button>
              <el-button type="primary" size="small" style="color: var(--text-inverse);" @click="openResource(item)">
                <el-icon><View /></el-icon>预览
              </el-button>
              <el-button type="success" size="small" style="color: var(--text-inverse);" @click="downloadResource(item)">
                <el-icon><Download /></el-icon>下载
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!filteredResources.length && !pageLoading" description="暂无资源" />
        <Pagination
          v-if="totalItems > 0"
          :page="currentPage"
          :size="pageSize"
          :total="totalItems"
          @update:page="currentPage = $event; loadResourcesData();"
          @update:size="pageSize = $event; currentPage = 1; loadResourcesData();"
        />
      </div>

      <PageSidebar title="资源统计" :stats="stats" hot-title="热门资源" :hot-items="hotResources" @hot-click="openResource">
        <UpdateLogCard :logs="allUpdateLogs" title="更新日志" :limit="5" />
      </PageSidebar>
    </div>

    <!-- Floating Batch Action Bar -->
    <transition name="batch-bar-fade">
      <div v-if="selectedIds.size > 0" class="batch-action-bar">
        <div class="batch-action-content">
          <span class="batch-count">已选 <strong>{{ selectedIds.size }}</strong> 项</span>
          <div class="batch-action-buttons">
            <el-button type="primary" :loading="batchDownloading" @click="batchDownload">
              <el-icon><Download /></el-icon>批量下载
            </el-button>
            <el-button @click="clearSelection">取消</el-button>
          </div>
        </div>
      </div>
    </transition>

    <ResourceDetailDialog
      v-model:visible="previewVisible"
      :resource="currentResource"
      :is-favorited="isCurrentFavorited"
      @toggle-favorite="toggleFavoriteDetail"
      @download="downloadCurrentResource"
    />
  </div>
</template>

<script setup>
import { computed, inject, nextTick, onMounted, ref, watch } from "vue";
import request from '@/utils/request';
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Document, Download, Picture, Star, VideoPlay, View } from "@element-plus/icons-vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import ResourceDetailDialog from "@/components/business/dialogs/ResourceDetailDialog.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import SkeletonListResource from "@/components/common/SkeletonListResource.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import { extractPageData } from "@/utils";
import { parseMediaList } from "@/utils/media";
import { useUpdateLog } from "@/composables/useUpdateLog";
import { useDebounceFn } from "@/composables/useDebounce";
import { useFavorite } from "@/composables/useFavorite";
import { useUserStore } from "@/stores/user";

const FILE_TYPE_MAP = {
  video: { extensions: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'], icon: VideoPlay, name: '视频' },
  document: { extensions: ['docx', 'doc', 'pdf', 'pptx', 'ppt', 'xlsx', 'xls', 'txt'], icon: Document, name: '文档' },
  image: { extensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'], icon: Picture, name: '图片' }
};

const filterConfig = ref([
  { key: "category", label: "难度", options: [{ label: "全部", value: "" }] },
  { key: "type", label: "类型", type: "success", options: [{ label: "全部", value: "" }] }
]);

const route = useRoute();
const showLoginDialog = inject("showLoginDialog");
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const { items: favItems, isFavorited, loadFavorites, toggleFavorite: doToggleFavorite } = useFavorite('resource');

const pageLoading = ref(false);
const keyword = ref("");
const allResources = ref([]);
const hotResources = ref([]);
const previewVisible = ref(false);
const currentResource = ref(null);
const categoryFilter = ref("");
const typeFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(9);
const totalItems = ref(0);
const statsData = ref({ total: 0, videoCount: 0, documentCount: 0, imageCount: 0, totalFavorites: 0, totalViews: 0, totalDownloads: 0, totalSize: 0 });

// Batch download state
const selectedIds = ref(new Set());
const batchDownloading = ref(false);

const { parseUpdateLog } = useUpdateLog();

const allUpdateLogs = computed(() => {
  const logs = [];
  allResources.value.forEach(item => {
    parseUpdateLog(item.updateLog).forEach(log => {
      logs.push({ ...log, resourceId: item.id, resourceName: item.title });
    });
  });
  return logs.sort((a, b) => new Date(b.time) - new Date(a.time)).slice(0, 10);
});

const filteredResources = computed(() => allResources.value);
const paginatedList = computed(() => allResources.value);

const stats = computed(() => [
  { value: statsData.value.total, label: "资源总数" },
  { value: statsData.value.videoCount, label: "视频数量" },
  { value: statsData.value.documentCount, label: "文档数量" },
  { value: statsData.value.imageCount, label: "图片数量" },
  { value: statsData.value.totalFavorites, label: "收藏量" },
  { value: statsData.value.totalViews, label: "浏览量" },
  { value: statsData.value.totalDownloads, label: "下载量" },
  { value: formatSize(statsData.value.totalSize), label: "总大小" }
]);

const getFileInfo = (item) => {
  if (!item.files) return { url: '', type: 'document', size: 0 };
  try {
    const files = parseMediaList(item.files);
    if (files.length > 0) {
      const firstFile = files[0];
      return { url: firstFile.url || firstFile.path || '', type: firstFile.type || 'document', size: firstFile.size || 0 };
    }
  } catch {}
  return { url: '', type: 'document', size: 0 };
};

const getFileExt = (fileUrl) => {
  if (!fileUrl) return '';
  const parts = fileUrl.split('.');
  return parts.length > 1 ? parts.pop().toLowerCase() : '';
};

const detectFileType = (fileUrl, fileType) => {
  if (fileType && ['video', 'document', 'image'].includes(fileType)) return fileType;
  const ext = getFileExt(fileUrl);
  for (const [type, config] of Object.entries(FILE_TYPE_MAP)) {
    if (config.extensions.includes(ext)) return type;
  }
  return 'document';
};

const getTypeIcon = (type) => FILE_TYPE_MAP[type]?.icon || Document;
const getTypeClass = (type) => ({ video: "type-video", document: "type-doc", image: "type-img" }[type] || "type-doc");
const getTypeName = (type) => FILE_TYPE_MAP[type]?.name || "其他";

const formatSize = (bytes) => {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  let size = bytes;
  let unitIndex = 0;
  while (size >= 1024 && unitIndex < units.length - 1) { size /= 1024; unitIndex++; }
  return `${size.toFixed(1)} ${units[unitIndex]}`;
};

// ========== Selection ==========
const toggleSelect = (item) => {
  const newSet = new Set(selectedIds.value);
  if (newSet.has(item.id)) {
    newSet.delete(item.id);
  } else {
    newSet.add(item.id);
  }
  selectedIds.value = newSet;
};

const clearSelection = () => {
  selectedIds.value = new Set();
};

// ========== Batch Download ==========
const batchDownload = async () => {
  if (selectedIds.value.size === 0) {
    ElMessage.warning('请先选择要下载的资源');
    return;
  }

  if (!isLoggedIn.value) {
    try {
      await ElMessageBox.confirm("批量下载资源需要登录，是否前往登录？", "提示", { confirmButtonText: "去登录", cancelButtonText: "取消", type: "info" });
      showLoginDialog();
    } catch {}
    return;
  }

  batchDownloading.value = true;
  try {
    const ids = [...selectedIds.value];
    const response = await request.post('/resources/batch-download', ids, {
      responseType: 'blob'
    });

    const blob = new Blob([response]);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'resources_batch_' + Date.now() + '.zip';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    // Update download counts for selected resources
    allResources.value.forEach(item => {
      if (selectedIds.value.has(item.id)) {
        item.downloadCount = (item.downloadCount || 0) + 1;
      }
    });

    ElMessage.success(`成功下载 ${ids.length} 个资源`);
    clearSelection();
  } catch (e) {
    console.error('批量下载失败:', e);
    ElMessage.error('批量下载失败，请重试');
  } finally {
    batchDownloading.value = false;
  }
};

// ========== Search & Filter ==========
const debouncedSearch = useDebounceFn(() => { currentPage.value = 1; loadResourcesData(); }, 300);
const handleSearch = () => debouncedSearch();
const handleFilter = (filters) => {
  typeFilter.value = filters.type || "";
  categoryFilter.value = filters.category || "";
  currentPage.value = 1;
  loadResourcesData();
};

// ========== Resource Actions ==========
const openResource = async (item) => {
  currentResource.value = item;
  previewVisible.value = true;
  try {
    await request.post(`/resources/${item.id}/view`);
    const idx = allResources.value.findIndex(r => r.id === item.id);
    if (idx > -1) {
      allResources.value[idx].viewCount = (allResources.value[idx].viewCount || 0) + 1;
      statsData.value.totalViews = (statsData.value.totalViews || 0) + 1;
    }
  } catch {}
};

const isCurrentFavorited = computed(() => currentResource.value && isFavorited(currentResource.value.id));

const toggleFavorite = (item) => {
  if (item) doToggleFavorite(item.id, isFavorited(item.id));
};

const toggleFavoriteDetail = () => { if (currentResource.value) toggleFavorite(currentResource.value); };

const downloadResource = async (item) => {
  if (!item) return;
  if (!isLoggedIn.value) {
    try {
      await ElMessageBox.confirm("下载资源需要登录，是否前往登录？", "提示", { confirmButtonText: "去登录", cancelButtonText: "取消", type: "info" });
      showLoginDialog();
    } catch {}
    return;
  }

  try {
    const response = await request.get(`/resources/download/${item.id}`, {
      responseType: 'blob'
    });

    const blob = new Blob([response]);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = item.title + '.' + getFileExt(getFileInfo(item).url);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    const idx = allResources.value.findIndex(r => r.id === item.id);
    if (idx > -1) allResources.value[idx].downloadCount = (allResources.value[idx].downloadCount || 0) + 1;
    ElMessage.success("下载成功");
  } catch (e) {
    console.error('下载失败:', e);
    ElMessage.error("下载失败，请重试");
  }
};

const downloadCurrentResource = () => { if (currentResource.value) downloadResource(currentResource.value); };

// ========== Data Loading ==========
const loadResourcesData = async () => {
  pageLoading.value = true;
  try {
    const [pageRes, statsRes] = await Promise.all([
      request.get("/resources/list", { params: { page: currentPage.value, size: pageSize.value, keyword: keyword.value, fileType: typeFilter.value, category: categoryFilter.value, _t: Date.now() } }),
      request.get("/stats/resources")
    ]);

    const { records, total } = extractPageData(pageRes);
    allResources.value = records.map(r => ({ ...r, fileType: detectFileType(getFileInfo(r).url, getFileInfo(r).type) }));
    favItems.value = allResources.value;
    totalItems.value = total;
    hotResources.value = allResources.value.slice(0, 5);

    const sd = statsRes.data || statsRes || {};
    statsData.value = { total: sd.total || 0, videoCount: sd.videoCount || 0, documentCount: sd.documentCount || 0, imageCount: sd.imageCount || 0, totalFavorites: sd.totalFavorites || 0, totalViews: sd.totalViews || 0, totalDownloads: sd.totalDownloads || 0, totalSize: sd.totalSize || 0 };

    await loadFavorites();
    // Clear selection on page load
    selectedIds.value = new Set();
  } catch { ElMessage.error("加载失败"); }
  finally { pageLoading.value = false; }
};

const loadMetadata = async () => {
  try {
    const res = await request.get("/metadata/filters");
    const data = res.data || res || {};
    const resourcesFilters = data.resources || {};
    const categories = resourcesFilters.category || [];
    const types = resourcesFilters.type || [];
    filterConfig.value = [
      { key: "category", label: "难度", options: [{ label: "全部", value: "" }, ...categories.map(c => ({ label: c, value: c }))] },
      { key: "type", label: "类型", type: "success", options: [{ label: "全部", value: "" }, ...types.map(t => ({ label: t, value: t }))] }
    ];
  } catch {}
};

onMounted(() => { loadMetadata(); loadResourcesData(); });

watch(() => route.path, (newPath) => {
  if (newPath === '/resources') loadResourcesData();
});

watch(() => route.query.id, async (id) => {
  if (!id) return
  const numId = Number(id)
  await nextTick()
  const item = allResources.value.find(r => r.id === numId)
  if (item) {
    openResource(item)
  } else {
    try {
      const res = await request.get(`/resources/${numId}`)
      if (res.data) openResource(res.data)
    } catch {}
  }
}, { immediate: true })
</script>

<style scoped>
.resources-container { display: grid; grid-template-columns: 1fr var(--sidebar-width); gap: var(--space-xl); }
.resource-list { display: flex; flex-direction: column; gap: var(--space-xl); }

.resource-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xl);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.resource-card.selected {
  border: 2px solid var(--dong-indigo);
  background: linear-gradient(135deg, rgba(26, 82, 118, 0.03), rgba(26, 82, 118, 0.06));
}

.resource-checkbox {
  flex-shrink: 0;
}

.resource-card-body {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  flex: 1;
  min-width: 0;
}

.resource-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.type-video { background: linear-gradient(135deg, #e74c3c, #c0392b); }
.type-doc { background: linear-gradient(135deg, #3498db, #2980b9); }
.type-img { background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark)); }

.resource-info { flex: 1; min-width: 0; }
.resource-info h3 { margin: 0 0 var(--space-sm) 0; font-size: var(--font-size-md); }
.resource-desc { margin: 0 0 var(--space-sm) 0; font-size: var(--font-size-sm); color: var(--text-muted); }
.resource-meta { display: flex; align-items: center; gap: var(--space-md); flex-wrap: wrap; }
.resource-type { font-size: var(--font-size-sm); color: var(--text-secondary); }
.resource-ext { font-size: var(--font-size-xs); color: var(--text-light); background: var(--bg-rice-dark); padding: var(--space-xs) var(--space-sm); border-radius: var(--radius-xs); }
.resource-size { font-size: var(--font-size-sm); color: var(--text-light); }
.resource-actions { display: flex; gap: var(--space-sm); flex-shrink: 0; }

.card-stats {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

/* Floating Batch Action Bar */
.batch-action-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: var(--z-sticky);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-md) var(--space-xl);
  border: 1px solid var(--border-light);
}

.batch-action-content {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.batch-count {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  white-space: nowrap;
}

.batch-count strong {
  color: var(--dong-indigo);
  font-size: var(--font-size-lg);
}

.batch-action-buttons {
  display: flex;
  gap: var(--space-sm);
}

/* Batch bar transition */
.batch-bar-fade-enter-active,
.batch-bar-fade-leave-active {
  transition: all var(--transition-normal);
}

.batch-bar-fade-enter-from,
.batch-bar-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(20px);
}

/* Responsive */
@media (max-width: 1024px) {
  .resources-container { grid-template-columns: 1fr; }
  .resource-card { flex-wrap: wrap; }
  .resource-actions { width: 100%; justify-content: flex-end; margin-top: var(--space-md); }
}

@media (max-width: 768px) {
  .resource-card {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--space-lg);
    gap: var(--space-md);
  }

  .resource-checkbox {
    position: absolute;
    top: var(--space-md);
    left: var(--space-md);
    z-index: 2;
  }

  .resource-card-body {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
    padding-left: var(--space-lg);
  }

  .resource-icon { width: 48px; height: 48px; }
  .resource-info { width: 100%; }
  .resource-info h3 { font-size: var(--font-size-base); }
  .resource-desc { font-size: var(--font-size-xs); -webkit-line-clamp: 2; display: -webkit-box; -webkit-box-orient: vertical; overflow: hidden; }
  .resource-meta { flex-wrap: wrap; gap: var(--space-sm); }
  .resource-actions { flex-wrap: wrap; width: 100%; }
  .resource-actions .el-button { flex: 1; min-width: 80px; }

  .batch-action-bar {
    left: var(--space-md);
    right: var(--space-md);
    transform: none;
    border-radius: var(--radius-md);
  }

  .batch-action-content {
    flex-direction: column;
    gap: var(--space-md);
    text-align: center;
  }

  .batch-action-buttons {
    width: 100%;
  }

  .batch-action-buttons .el-button {
    flex: 1;
  }

  .batch-bar-fade-enter-from,
  .batch-bar-fade-leave-to {
    opacity: 0;
    transform: translateY(20px);
  }
}

@media (max-width: 480px) {
  .resource-actions .el-button { font-size: var(--font-size-xs); padding: var(--space-sm) var(--space-md); }
  .batch-action-bar { padding: var(--space-md); left: var(--space-sm); right: var(--space-sm); bottom: 16px; }
}
</style>
