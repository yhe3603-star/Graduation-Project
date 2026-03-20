<template>
  <div
    v-loading="pageLoading"
    class="resources-page module-page"
  >
    <div class="module-header">
      <h1>学习资源</h1>
      <p class="subtitle">
        侗医药学习资料库
      </p>
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

        <div class="resource-list">
          <div
            v-for="item in paginatedList"
            :key="item.id"
            class="resource-card"
            @click="openResource(item)"
          >
            <div
              class="resource-icon"
              :class="getTypeClass(getFileInfo(item).type)"
            >
              <el-icon :size="28">
                <component :is="getTypeIcon(getFileInfo(item).type)" />
              </el-icon>
            </div>
            <div class="resource-info">
              <h3>{{ item.title }}</h3>
              <p class="resource-desc">
                {{ (item.description || '').substring(0, 60) }}...
              </p>
              <div class="resource-meta">
                <el-tag
                  size="small"
                  effect="light"
                >
                  {{ item.category }}
                </el-tag>
                <span class="resource-type">{{ getTypeName(getFileInfo(item).type) }}</span>
                <span
                  v-if="getFileExt(getFileInfo(item).url)"
                  class="resource-ext"
                >{{ getFileExt(getFileInfo(item).url).toUpperCase() }}</span>
                <span
                  v-if="getFileInfo(item).size"
                  class="resource-size"
                >{{ formatSize(getFileInfo(item).size) }}</span>
                <div class="card-stats">
                  <span class="stat-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                  <span class="stat-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
                  <span class="stat-item"><el-icon><Download /></el-icon>{{ item.downloadCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div
              class="resource-actions"
              @click.stop
            >
              <el-button
                :type="isFavorited(item.id) ? 'warning' : 'default'"
                size="small"
                @click="toggleFavorite(item)"
              >
                <el-icon><Star /></el-icon>{{ isFavorited(item.id) ? '已收藏' : '收藏' }}
              </el-button>
              <el-button
                type="primary"
                size="small"
                style="color: var(--text-inverse);"
                @click="openResource(item)"
              >
                <el-icon><View /></el-icon>预览
              </el-button>
              <el-button
                type="success"
                size="small"
                style="color: var(--text-inverse);"
                @click="downloadResource(item)"
              >
                <el-icon><Download /></el-icon>下载
              </el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!filteredResources.length && !pageLoading"
          description="暂无资源"
        />
        <Pagination
          v-if="filteredResources.length"
          :page="currentPage"
          :size="pageSize"
          :total="filteredResources.length"
          @update:page="currentPage = $event"
          @update:size="pageSize = $event"
        />
      </div>

      <PageSidebar
        title="资源统计"
        :stats="stats"
        hot-title="热门资源"
        :hot-items="hotResources"
        @hot-click="openResource"
      >
        <UpdateLogCard 
          :logs="allUpdateLogs" 
          title="更新日志" 
          :limit="5"
        />
      </PageSidebar>
    </div>

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
import { computed, inject, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Document, Download, Picture, Star, VideoPlay, View } from "@element-plus/icons-vue";
import PageSidebar from "@/components/business/display/PageSidebar.vue";
import Pagination from "@/components/business/display/Pagination.vue";
import ResourceDetailDialog from "@/components/business/dialogs/ResourceDetailDialog.vue";
import SearchFilter from "@/components/business/display/SearchFilter.vue";
import UpdateLogCard from "@/components/business/display/UpdateLogCard.vue";
import { extractData } from "@/utils";
import { useUpdateLog } from "@/composables/useUpdateLog";

const request = inject("request");
const isLoggedIn = computed(() => !!localStorage.getItem("token"));

const pageLoading = ref(false);
const keyword = ref("");
const allResources = ref([]);
const hotResources = ref([]);
const previewVisible = ref(false);
const currentResource = ref(null);
const favorites = ref([]);
const typeFilter = ref("");
const currentPage = ref(1);
const pageSize = ref(9);

const FILE_TYPE_MAP = {
  video: { extensions: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'], icon: VideoPlay, name: '视频' },
  document: { extensions: ['docx', 'doc', 'pdf', 'pptx', 'ppt', 'xlsx', 'xls', 'txt'], icon: Document, name: '文档' },
  image: { extensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'], icon: Picture, name: '图片' }
};

const filterConfig = [
  { key: "type", label: "类型", type: "success", options: [{ label: "全部", value: "" }, { label: "视频", value: "video" }, { label: "文档", value: "document" }, { label: "图片", value: "image" }] }
];

const stats = computed(() => {
  const totalViews = allResources.value.reduce((sum, r) => sum + (r.viewCount || 0), 0);
  const totalDownloads = allResources.value.reduce((sum, r) => sum + (r.downloadCount || 0), 0);
  const totalFavorites = allResources.value.reduce((sum, r) => sum + (r.favoriteCount || 0), 0);
  const totalSize = allResources.value.reduce((sum, r) => {
    const fileInfo = getFileInfo(r);
    return sum + (fileInfo.size || 0);
  }, 0);
  
  const videoCount = allResources.value.filter(r => getFileInfo(r).type === 'video').length;
  const docCount = allResources.value.filter(r => getFileInfo(r).type === 'document').length;
  const imgCount = allResources.value.filter(r => getFileInfo(r).type === 'image').length;
  
  return [
    { value: allResources.value.length, label: "资源总数" },
    { value: videoCount, label: "视频数量" },
    { value: docCount, label: "文档数量" },
    { value: imgCount, label: "图片数量" },
    { value: totalFavorites, label: "收藏量" },
    { value: totalViews, label: "浏览量" },
    { value: totalDownloads, label: "下载量" },
    { value: formatSize(totalSize), label: "总大小" }
  ];
});

const { parseUpdateLog } = useUpdateLog();

const allUpdateLogs = computed(() => {
  const logs = [];
  allResources.value.forEach(item => {
    const itemLogs = parseUpdateLog(item.updateLog);
    itemLogs.forEach(log => {
      logs.push({
        ...log,
        resourceId: item.id,
        resourceName: item.title
      });
    });
  });
  return logs.sort((a, b) => new Date(b.time) - new Date(a.time)).slice(0, 10);
});

const filteredResources = computed(() => {
  let result = allResources.value;
  if (keyword.value) result = result.filter(r => r.title?.includes(keyword.value) || r.description?.includes(keyword.value));
  if (typeFilter.value) result = result.filter(r => r.fileType === typeFilter.value);
  return result;
});

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredResources.value.slice(start, start + pageSize.value);
});

const getFileInfo = (item) => {
  if (!item.files) return { url: '', type: 'document', size: 0 };
  try {
    const files = typeof item.files === 'string' ? JSON.parse(item.files) : item.files;
    if (Array.isArray(files) && files.length > 0) {
      const firstFile = files[0];
      return {
        url: firstFile.path || firstFile.url || '',
        type: firstFile.type || 'document',
        size: firstFile.size || 0
      };
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

const handleSearch = () => { currentPage.value = 1; };
const handleFilter = (filters) => { typeFilter.value = filters.type || ""; currentPage.value = 1; };

const openResource = async (item) => {
  currentResource.value = item;
  previewVisible.value = true;
  try {
    await request.post(`/resources/${item.id}/view`);
    const idx = allResources.value.findIndex(r => r.id === item.id);
    if (idx > -1) allResources.value[idx].viewCount = (allResources.value[idx].viewCount || 0) + 1;
  } catch {}
};

const isFavorited = (id) => favorites.value.some(f => f.targetId === id && f.type === "resource");
const isCurrentFavorited = computed(() => currentResource.value && isFavorited(currentResource.value.id));

const toggleFavorite = async (item) => {
  if (!item || !isLoggedIn.value) { if (!isLoggedIn.value) ElMessage.warning("请先登录后再收藏"); return; }
  try {
    const resIdx = allResources.value.findIndex(r => r.id === item.id);
    if (isFavorited(item.id)) {
      await request.delete(`/favorites/resource/${item.id}`);
      favorites.value = favorites.value.filter(f => !(f.targetId === item.id && f.type === "resource"));
      if (resIdx > -1) allResources.value[resIdx].favoriteCount = Math.max(0, (allResources.value[resIdx].favoriteCount || 1) - 1);
      ElMessage.success("已取消收藏");
    } else {
      await request.post(`/favorites/resource/${item.id}`);
      favorites.value.push({ targetId: item.id, type: "resource" });
      if (resIdx > -1) allResources.value[resIdx].favoriteCount = (allResources.value[resIdx].favoriteCount || 0) + 1;
      ElMessage.success("收藏成功");
    }
  } catch { ElMessage.error("操作失败"); }
};

const toggleFavoriteDetail = () => { if (currentResource.value) toggleFavorite(currentResource.value); };

const downloadResource = async (item) => {
  if (!item) return;
  if (!isLoggedIn.value) {
    try {
      await ElMessageBox.confirm("下载资源需要登录，是否前往登录？", "提示", { confirmButtonText: "去登录", cancelButtonText: "取消", type: "info" });
      window.location.href = "/";
    } catch {}
    return;
  }
  try {
    const link = document.createElement("a");
    link.href = `/api/resources/download/${item.id}`;
    link.download = item.title + '.' + getFileExt(getFileInfo(item).url);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    const idx = allResources.value.findIndex(r => r.id === item.id);
    if (idx > -1) allResources.value[idx].downloadCount = (allResources.value[idx].downloadCount || 0) + 1;
    ElMessage.success("开始下载");
  } catch { ElMessage.error("下载失败"); }
};

const downloadCurrentResource = () => { if (currentResource.value) downloadResource(currentResource.value); };

onMounted(async () => {
  pageLoading.value = true;
  try {
    const res = await request.get("/resources/list");
    allResources.value = extractData(res).map(r => ({ ...r, fileType: detectFileType(getFileInfo(r).url, getFileInfo(r).type) }));
    hotResources.value = allResources.value.slice(0, 5);
    if (isLoggedIn.value) {
      try { favorites.value = extractData(await request.get("/favorites/my")); } catch {}
    }
  } catch { ElMessage.error("加载失败"); }
  finally { pageLoading.value = false; }
});
</script>

<style scoped>
.resources-container {
  display: grid;
  grid-template-columns: 1fr var(--sidebar-width);
  gap: var(--space-xl);
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.resource-card {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-xl);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
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

.type-video {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}

.type-doc {
  background: linear-gradient(135deg, #3498db, #2980b9);
}

.type-img {
  background: linear-gradient(135deg, var(--dong-jade), var(--dong-jade-dark));
}

.resource-info {
  flex: 1;
}

.resource-info h3 {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-md);
}

.resource-desc {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.resource-type {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.resource-ext {
  font-size: var(--font-size-xs);
  color: var(--text-light);
  background: var(--bg-rice-dark);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-xs);
}

.resource-size {
  font-size: var(--font-size-sm);
  color: var(--text-light);
}

.resource-actions {
  display: flex;
  gap: var(--space-sm);
}

@media (max-width: 1024px) {
  .resources-container {
    grid-template-columns: 1fr;
  }
  
  .resource-card {
    flex-wrap: wrap;
  }
  
  .resource-actions {
    width: 100%;
    justify-content: flex-end;
    margin-top: var(--space-md);
  }
}

@media (max-width: 768px) {
  .resource-card {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--space-lg);
    gap: var(--space-md);
  }
  
  .resource-icon {
    width: 48px;
    height: 48px;
  }
  
  .resource-info {
    width: 100%;
  }
  
  .resource-info h3 {
    font-size: var(--font-size-base);
  }
  
  .resource-desc {
    font-size: var(--font-size-xs);
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .resource-meta {
    flex-wrap: wrap;
    gap: var(--space-sm);
  }
  
  .resource-actions {
    flex-wrap: wrap;
    width: 100%;
  }
  
  .resource-actions .el-button {
    flex: 1;
    min-width: 80px;
  }
}

@media (max-width: 480px) {
  .resource-actions .el-button {
    font-size: var(--font-size-xs);
    padding: var(--space-sm) var(--space-md);
  }
}
</style>
