<template>
  <div class="search-page module-page">
    <div class="module-header">
      <h1>全局搜索</h1>
      <p class="subtitle">
        搜索知识、植物、传承人、问答
      </p>
    </div>

    <div class="search-main">
      <div class="search-box-large">
        <el-input
          v-model="keyword"
          placeholder="输入关键词搜索..."
          size="large"
          clearable
          @keyup.enter="doSearch"
          @clear="onClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="doSearch"
        >
          搜索
        </el-button>
      </div>

      <div
        v-if="!searched"
        class="hot-keywords"
      >
        <span class="hot-label">热门搜索：</span>
        <el-tag
          v-for="k in hotKeywords"
          :key="k"
          class="hot-tag"
          effect="plain"
          @click="keyword = k; doSearch()"
        >
          {{ k }}
        </el-tag>
      </div>

      <div
        v-if="total > 0"
        class="search-stats"
      >
        <span>找到 <strong>{{ total }}</strong> 条结果</span>
        <span
          v-if="searched"
          class="search-keyword"
        >关键词：{{ lastKeyword }}</span>
      </div>

      <el-tabs
        v-if="total > 0"
        v-model="activeTab"
        class="result-tabs"
        @tab-change="onTabChange"
      >
        <el-tab-pane
          label="全部"
          name="all"
        >
          <div class="result-grid">
            <div
              v-for="item in paginatedResults"
              :key="item.id + item.type"
              class="result-card"
              @click="goToDetail(item)"
            >
              <div class="result-icon">
                <el-icon><component :is="getTypeIcon(item.type)" /></el-icon>
              </div>
              <div class="result-info">
                <h4>{{ item.title || item.nameCn || item.name || item.question }}</h4>
                <p>{{ (item.description || item.bio || item.efficacy || item.answer || '').substring(0, 60) }}...</p>
              </div>
              <el-tag
                size="small"
                :type="getTypeTag(item.type)"
              >
                {{ getTypeName(item.type) }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane
          v-for="type in typeList"
          :key="type"
          :label="getTypeName(type) + ' (' + typeCounts[type] + ')'"
          :name="type"
        >
          <div class="result-list">
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="result-item"
              @click="goToDetail(item)"
            >
              <span class="result-title">{{ item.title || item.nameCn || item.name || item.question }}</span>
              <span class="result-desc">{{ (item.description || item.bio || item.efficacy || item.answer || '').substring(0, 40) }}...</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div
        v-if="total > pageSize"
        class="pagination-wrap"
      >
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onPageSizeChange"
          @current-change="onPageChange"
        />
      </div>

      <el-empty
        v-if="searched && total === 0"
        description="未找到相关结果"
      />
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ChatDotRound, Document, Picture, Search, User } from "@element-plus/icons-vue";

const router = useRouter();
const route = useRoute();
const request = inject("request");

const keyword = ref("");
const lastKeyword = ref("");
const loading = ref(false);
const searched = ref(false);
const activeTab = ref("all");
const allResults = ref([]);
const searchResults = ref([]);
const total = ref(0);
const typeCounts = ref({ knowledge: 0, plant: 0, inheritor: 0, qa: 0 });

const currentPage = ref(1);
const pageSize = ref(12);

const hotKeywords = ["侗医药", "药浴", "传承人", "艾灸", "鼻炎", "风湿"];
const typeList = ["knowledge", "plant", "inheritor", "qa"];

const getTypeIcon = (type) => ({ knowledge: Document, plant: Picture, inheritor: User, qa: ChatDotRound }[type] || Document);
const getTypeTag = (type) => ({ knowledge: "primary", plant: "success", inheritor: "warning", qa: "info" }[type] || "info");
const getTypeName = (type) => ({ knowledge: "知识", plant: "植物", inheritor: "传承人", qa: "问答" }[type] || "其他");

const paginatedResults = computed(() => {
  if (activeTab.value !== "all") return searchResults.value;
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return allResults.value.slice(start, end);
});

const onTabChange = () => {
  currentPage.value = 1;
  if (activeTab.value === "all") {
    searchResults.value = [];
    total.value = allResults.value.length;
  } else {
    loadSingleType();
  }
};

const onPageChange = () => {
  window.scrollTo({ top: 300, behavior: 'smooth' });
  if (activeTab.value !== "all") {
    loadSingleType();
  }
};

const onPageSizeChange = () => {
  currentPage.value = 1;
  if (activeTab.value !== "all") {
    loadSingleType();
  }
};

const onClear = () => {
  keyword.value = "";
  lastKeyword.value = "";
  searched.value = false;
  activeTab.value = "all";
  currentPage.value = 1;
  loadAllData();
};

const getData = (res) => {
  const d = res?.data;
  if (Array.isArray(d)) return { records: d, total: d.length };
  if (d?.records && Array.isArray(d.records)) return { records: d.records, total: d.total || d.records.length };
  return { records: [], total: 0 };
};

const loadAllData = async () => {
  loading.value = true;
  try {
    const [kRes, pRes, iRes, qRes] = await Promise.all([
      request.get(`/knowledge/list?page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/plants/list?page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/inheritors/list?page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/qa/list?page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } }))
    ]);

    const kData = getData(kRes);
    const pData = getData(pRes);
    const iData = getData(iRes);
    const qData = getData(qRes);

    allResults.value = [
      ...(kData.records.map(r => ({ ...r, type: "knowledge" }))),
      ...(pData.records.map(r => ({ ...r, type: "plant" }))),
      ...(iData.records.map(r => ({ ...r, type: "inheritor" }))),
      ...(qData.records.map(r => ({ ...r, type: "qa" })))
    ];

    total.value = kData.total + pData.total + iData.total + qData.total;
    typeCounts.value = {
      knowledge: kData.total,
      plant: pData.total,
      inheritor: iData.total,
      qa: qData.total
    };

    searchResults.value = [];
  } finally {
    loading.value = false;
  }
};

const loadSingleType = async () => {
  loading.value = true;
  try {
    const tab = activeTab.value;
    const page = currentPage.value;
    const size = pageSize.value;

    const typeUrls = {
      knowledge: `/knowledge/list?page=${page}&size=${size}`,
      plant: `/plants/list?page=${page}&size=${size}`,
      inheritor: `/inheritors/list?page=${page}&size=${size}`,
      qa: `/qa/list?page=${page}&size=${size}`
    };

    const res = await request.get(typeUrls[tab]).catch(() => ({ data: { records: [], total: 0 } }));
    const data = getData(res);
    searchResults.value = data.records.map(r => ({ ...r, type: tab }));
    total.value = data.total;
  } finally {
    loading.value = false;
  }
};

const doSearch = async () => {
  if (!keyword.value.trim()) {
    onClear();
    return;
  }
  loading.value = true;
  searched.value = true;
  lastKeyword.value = keyword.value.trim();
  try {
    const kw = keyword.value.trim();

    const [kRes, pRes, iRes, qRes] = await Promise.all([
      request.get(`/knowledge/search?keyword=${kw}&page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/plants/search?keyword=${kw}&page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/inheritors/search?keyword=${kw}&page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/qa/search?keyword=${kw}&page=1&size=1000`).catch(() => ({ data: { records: [], total: 0 } }))
    ]);

    const kData = getData(kRes);
    const pData = getData(pRes);
    const iData = getData(iRes);
    const qData = getData(qRes);

    allResults.value = [
      ...(kData.records.map(r => ({ ...r, type: "knowledge" }))),
      ...(pData.records.map(r => ({ ...r, type: "plant" }))),
      ...(iData.records.map(r => ({ ...r, type: "inheritor" }))),
      ...(qData.records.map(r => ({ ...r, type: "qa" })))
    ];

    typeCounts.value = {
      knowledge: kData.total,
      plant: pData.total,
      inheritor: iData.total,
      qa: qData.total
    };

    if (activeTab.value === "all") {
      total.value = allResults.value.length;
      searchResults.value = [];
    } else {
      await loadSingleTypeSearch();
    }
  } finally {
    loading.value = false;
  }
};

const loadSingleTypeSearch = async () => {
  const tab = activeTab.value;
  const page = currentPage.value;
  const size = pageSize.value;
  const kw = keyword.value.trim();

  const typeUrls = {
    knowledge: `/knowledge/search?keyword=${kw}&page=${page}&size=${size}`,
    plant: `/plants/search?keyword=${kw}&page=${page}&size=${size}`,
    inheritor: `/inheritors/search?keyword=${kw}&page=${page}&size=${size}`,
    qa: `/qa/search?keyword=${kw}&page=${page}&size=${size}`
  };

  const res = await request.get(typeUrls[tab]).catch(() => ({ data: { records: [], total: 0 } }));
  const data = getData(res);
  searchResults.value = data.records.map(r => ({ ...r, type: tab }));
  total.value = data.total;
};

const goToDetail = (item) => {
  const routes = { knowledge: "/knowledge", plant: "/plants", inheritor: "/inheritors", qa: "/qa" };
  router.push(routes[item.type] || "/");
};

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q;
    doSearch();
  } else {
    loadAllData();
  }
});
</script>

<style scoped>
.search-main {
  max-width: 900px;
  margin: 0 auto;
}

.search-box-large {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
}

.search-box-large .el-input {
  flex: 1;
}

.hot-keywords {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
  margin-bottom: var(--space-xl);
}

.hot-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.hot-tag {
  cursor: pointer;
}

.search-stats {
  margin-bottom: var(--space-lg);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-keyword {
  color: var(--dong-indigo);
  font-weight: 500;
}

.result-tabs {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.result-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.result-card:hover {
  background: rgba(26, 82, 118, 0.08);
}

.result-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.result-info {
  flex: 1;
}

.result-info h4 {
  margin: 0 0 var(--space-xs) 0;
  font-size: var(--font-size-sm);
}

.result-info p {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.result-item {
  padding: var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.result-item:hover {
  background: rgba(26, 82, 118, 0.08);
}

.result-title {
  display: block;
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-xs);
}

.result-desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-xl);
  padding: var(--space-lg);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
</style>
