<template>
  <div class="search-page module-page">
    <div class="module-header">
      <h1>全局搜索</h1>
      <p class="subtitle">
        搜索知识、植物、传承人、问答、资源
      </p>
    </div>

    <div class="search-main">
      <div class="search-box-area">
        <div class="search-box-large">
          <el-autocomplete
            v-model="keyword"
            :fetch-suggestions="fetchSuggestions"
            :trigger-on-focus="false"
            placeholder="输入关键词搜索..."
            clearable
            class="search-autocomplete"
            @select="handleSuggestionSelect"
            @keyup.enter="doSearch"
            @clear="onClear"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-autocomplete>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="doSearch"
          >
            搜索
          </el-button>
        </div>

        <div v-if="searchHistory.length > 0 && !searched" class="search-history">
          <span class="history-label">
            <el-icon><Clock /></el-icon>搜索历史：
          </span>
          <el-tag
            v-for="(h, i) in searchHistory"
            :key="i"
            class="history-chip"
            closable
            size="small"
            effect="plain"
            @click="keyword = h; doSearch()"
            @close="removeHistory(i)"
          >
            {{ h }}
          </el-tag>
          <el-button
            v-if="searchHistory.length > 0"
            text
            size="small"
            type="info"
            @click="clearHistory"
          >
            清除历史
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
      </div>

      <div
        v-if="searched && total > 0"
        class="search-stats"
      >
        <span>找到 <strong>{{ total }}</strong> 条结果</span>
        <span class="search-keyword">关键词：{{ lastKeyword }}</span>
      </div>

      <el-tabs
        v-if="searched || total > 0"
        v-model="activeTab"
        class="result-tabs"
        @tab-change="onTabChange"
      >
        <el-tab-pane
          label="全部"
          name="all"
        >
          <div
            v-if="allResults.length > 0"
            class="result-grid"
          >
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
                <h4 v-html="highlightText(item.title || item.nameCn || item.name || item.question)" />
                <p v-html="highlightText((item.description || item.bio || item.efficacy || item.answer || '').substring(0, 60)) + '...'" />
              </div>
              <el-tag
                size="small"
                :type="getTypeTag(item.type)"
              >
                {{ getTypeName(item.type) }}
              </el-tag>
            </div>
          </div>
          <div v-else>
            <el-empty description="未找到相关结果" />
            <div class="recommend-section">
              <p class="recommend-title">无结果？试试以下推荐</p>
              <div class="recommend-tags">
                <el-tag
                  v-for="r in recommendedItems"
                  :key="r"
                  class="recommend-tag"
                  effect="plain"
                  @click="keyword = r; doSearch()"
                >
                  {{ r }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane
          v-for="type in typeList"
          :key="type"
          :label="getTypeName(type) + ' (' + typeCounts[type] + ')'"
          :name="type"
        >
          <div
            v-if="searchResults.length > 0"
            class="result-list"
          >
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="result-item"
              @click="goToDetail(item)"
            >
              <span class="result-title" v-html="highlightText(item.title || item.nameCn || item.name || item.question)" />
              <span class="result-desc" v-html="highlightText((item.description || item.bio || item.efficacy || item.answer || '').substring(0, 40)) + '...'" />
            </div>
          </div>
          <div v-else>
            <el-empty description="该类型暂无搜索结果" />
            <div class="recommend-section">
              <p class="recommend-title">无结果？试试以下推荐</p>
              <div class="recommend-tags">
                <el-tag
                  v-for="r in recommendedItems"
                  :key="r"
                  class="recommend-tag"
                  effect="plain"
                  @click="keyword = r; doSearch()"
                >
                  {{ r }}
                </el-tag>
              </div>
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
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from "vue";
import request from '@/utils/request';
import { useRoute, useRouter } from "vue-router";
import { ChatDotRound, Clock, Document, FolderOpened, Picture, Search, User } from "@element-plus/icons-vue";

const router = useRouter();
const route = useRoute();

const keyword = ref("");
const lastKeyword = ref("");
const loading = ref(false);
const searched = ref(false);
const activeTab = ref("all");
const allResults = ref([]);
const searchResults = ref([]);
const total = ref(0);
const typeCounts = ref({ knowledge: 0, plant: 0, inheritor: 0, qa: 0, resource: 0 });
const suggestions = ref([]);

const currentPage = ref(1);
const pageSize = ref(12);

const hotKeywords = ["侗医药", "药浴", "传承人", "艾灸", "鼻炎", "风湿"];
const typeList = ["knowledge", "plant", "inheritor", "qa", "resource"];

const HISTORY_KEY = 'dong_search_history';
const MAX_HISTORY = 10;

const popularRecommendItems = [
  "侗医药理论", "药浴疗法", "艾灸", "风湿", "鼻炎", "清热解毒",
  "活血化瘀", "侗族草药", "金银花", "慢性支气管炎", "穴位按摩", "青钱柳"
];

const recommendedItems = computed(() => {
  const shuffled = [...popularRecommendItems].sort(() => Math.random() - 0.5);
  return shuffled.slice(0, 6);
});

const searchHistory = ref(loadHistory());

function loadHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveHistory() {
  try {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(searchHistory.value));
  } catch {}
}

function addToHistory(kw) {
  if (!kw || !kw.trim()) return;
  const term = kw.trim();
  searchHistory.value = [term, ...searchHistory.value.filter(h => h !== term)].slice(0, MAX_HISTORY);
  saveHistory();
}

function removeHistory(index) {
  searchHistory.value.splice(index, 1);
  saveHistory();
}

function clearHistory() {
  searchHistory.value = [];
  saveHistory();
}

const getTypeIcon = (type) => ({ knowledge: Document, plant: Picture, inheritor: User, qa: ChatDotRound, resource: FolderOpened }[type] || Document);
const getTypeTag = (type) => ({ knowledge: "primary", plant: "success", inheritor: "warning", qa: "info", resource: "danger" }[type] || "info");
const getTypeName = (type) => ({ knowledge: "知识", plant: "植物", inheritor: "传承人", qa: "问答", resource: "资源" }[type] || "其他");

function escapeRegex(str) {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function highlightText(text) {
  if (!text) return '';
  const kw = lastKeyword.value || keyword.value;
  if (!kw || !kw.trim()) return text;
  const escaped = escapeRegex(kw.trim());
  const regex = new RegExp(`(${escaped})`, 'gi');
  return String(text).replace(regex, '<em class="highlight">$1</em>');
}

const paginatedResults = computed(() => {
  if (activeTab.value !== "all") return searchResults.value;
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return allResults.value.slice(start, end);
});

const fetchSuggestions = async (queryString, cb) => {
  if (!queryString || queryString.trim().length < 1) {
    cb([]);
    return;
  }
  try {
    const res = await request.get('/search/suggest', { params: { keyword: queryString.trim() } });
    const data = res?.data || res || [];
    const items = Array.isArray(data) ? data : (data.suggestions || data.data || []);
    const formatted = items.map(item => ({
      value: typeof item === 'string' ? item : (item.value || item.name || item.title || item.keyword || ''),
      ...(typeof item === 'object' ? item : {})
    }));
    cb(formatted);
  } catch {
    cb([]);
  }
};

const handleSuggestionSelect = (item) => {
  keyword.value = item.value || item;
  doSearch();
};

const onTabChange = () => {
  currentPage.value = 1;
  if (activeTab.value === "all") {
    searchResults.value = [];
    total.value = allResults.value.length;
  } else {
    if (searched.value && keyword.value.trim()) {
      loadSingleTypeSearch();
    } else {
      loadSingleType();
    }
  }
};

const onPageChange = () => {
  window.scrollTo({ top: 300, behavior: 'smooth' });
  if (activeTab.value !== "all") {
    if (searched.value && keyword.value.trim()) {
      loadSingleTypeSearch();
    } else {
      loadSingleType();
    }
  }
};

const onPageSizeChange = () => {
  currentPage.value = 1;
  if (activeTab.value !== "all") {
    if (searched.value && keyword.value.trim()) {
      loadSingleTypeSearch();
    } else {
      loadSingleType();
    }
  }
};

const onClear = () => {
  keyword.value = "";
  lastKeyword.value = "";
  searched.value = false;
  activeTab.value = "all";
  currentPage.value = 1;
  allResults.value = [];
  searchResults.value = [];
  total.value = 0;
  typeCounts.value = { knowledge: 0, plant: 0, inheritor: 0, qa: 0, resource: 0 };
};

const getData = (res) => {
  const d = res?.data;
  if (Array.isArray(d)) return { records: d, total: d.length };
  if (d?.records && Array.isArray(d.records)) return { records: d.records, total: d.total || d.records.length };
  return { records: [], total: 0 };
};

const doSearch = async () => {
  if (!keyword.value.trim()) {
    onClear();
    return;
  }
  loading.value = true;
  searched.value = true;
  lastKeyword.value = keyword.value.trim();
  addToHistory(lastKeyword.value);
  try {
    const kw = keyword.value.trim();

    const [kRes, pRes, iRes, qRes, rRes] = await Promise.all([
      request.get(`/knowledge/search?keyword=${kw}&page=1&size=100`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/plants/search?keyword=${kw}&page=1&size=100`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/inheritors/search?keyword=${kw}&page=1&size=100`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/qa/search?keyword=${kw}&page=1&size=100`).catch(() => ({ data: { records: [], total: 0 } })),
      request.get(`/resources/search?keyword=${kw}&page=1&size=100`).catch(() => ({ data: { records: [], total: 0 } }))
    ]);

    const kData = getData(kRes);
    const pData = getData(pRes);
    const iData = getData(iRes);
    const qData = getData(qRes);
    const rData = getData(rRes);

    allResults.value = [
      ...(kData.records.map(r => ({ ...r, type: "knowledge" }))),
      ...(pData.records.map(r => ({ ...r, type: "plant" }))),
      ...(iData.records.map(r => ({ ...r, type: "inheritor" }))),
      ...(qData.records.map(r => ({ ...r, type: "qa" }))),
      ...(rData.records.map(r => ({ ...r, type: "resource" })))
    ];

    typeCounts.value = {
      knowledge: kData.total,
      plant: pData.total,
      inheritor: iData.total,
      qa: qData.total,
      resource: rData.total
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
    qa: `/qa/search?keyword=${kw}&page=${page}&size=${size}`,
    resource: `/resources/search?keyword=${kw}&page=${page}&size=${size}`
  };

  const res = await request.get(typeUrls[tab]).catch(() => ({ data: { records: [], total: 0 } }));
  const data = getData(res);
  searchResults.value = data.records.map(r => ({ ...r, type: tab }));
  total.value = data.total;
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
      qa: `/qa/list?page=${page}&size=${size}`,
      resource: `/resources/list?page=${page}&size=${size}`
    };

    const res = await request.get(typeUrls[tab]).catch(() => ({ data: { records: [], total: 0 } }));
    const data = getData(res);
    searchResults.value = data.records.map(r => ({ ...r, type: tab }));
    total.value = data.total;
  } finally {
    loading.value = false;
  }
};

const goToDetail = (item) => {
  const routes = { knowledge: "/knowledge", plant: "/plants", inheritor: "/inheritors", qa: "/qa", resource: "/resources" };
  router.push(routes[item.type] || "/");
};

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q;
    doSearch();
  }
});
</script>

<style scoped>
.search-main {
  max-width: 900px;
  margin: 0 auto;
}

.search-box-area {
  margin-bottom: var(--space-xl);
}

.search-box-large {
  display: flex;
  gap: var(--space-md);
}

.search-autocomplete {
  flex: 1;
}

.search-history {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
  margin-top: var(--space-md);
  padding: var(--space-sm) 0;
}

.history-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  white-space: nowrap;
}

.history-chip {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.history-chip:hover {
  border-color: var(--dong-blue);
  color: var(--dong-blue);
}

.hot-keywords {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
  margin-top: var(--space-md);
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

.result-info h4 :deep(.highlight) {
  background: #fff3cd;
  color: #856404;
  padding: 1px 3px;
  border-radius: 2px;
  font-weight: 600;
  font-style: normal;
}

.result-info p {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.result-info p :deep(.highlight) {
  background: #fff3cd;
  color: #856404;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
  font-style: normal;
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

.result-title :deep(.highlight) {
  background: #fff3cd;
  color: #856404;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
}

.result-desc {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.result-desc :deep(.highlight) {
  background: #fff3cd;
  color: #856404;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
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

.recommend-section {
  margin-top: var(--space-lg);
  text-align: center;
}

.recommend-title {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-bottom: var(--space-md);
}

.recommend-tags {
  display: flex;
  gap: var(--space-sm);
  justify-content: center;
  flex-wrap: wrap;
}

.recommend-tag {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.recommend-tag:hover {
  border-color: var(--dong-blue);
  color: var(--dong-blue);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .search-box-large {
    flex-direction: column;
  }

  .result-grid {
    grid-template-columns: 1fr;
  }

  .search-history {
    gap: var(--space-xs);
  }

  .hot-keywords {
    gap: var(--space-xs);
  }
}

@media (max-width: 480px) {
  .search-main {
    padding: 0 var(--space-sm);
  }

  .result-tabs {
    padding: var(--space-md);
    border-radius: var(--radius-md);
  }
}
</style>
