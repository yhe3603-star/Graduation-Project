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
        v-if="!keyword"
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
        v-if="searchResults.length"
        class="search-stats"
      >
        <span>找到 <strong>{{ searchResults.length }}</strong> 条结果</span>
      </div>

      <el-tabs
        v-if="searchResults.length"
        v-model="activeTab"
        class="result-tabs"
      >
        <el-tab-pane
          label="全部"
          name="all"
        >
          <div class="result-grid">
            <div
              v-for="item in searchResults"
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
          :label="getTypeName(type)"
          :name="type"
        >
          <div class="result-list">
            <div
              v-for="item in getResultsByType(type)"
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

      <el-empty
        v-if="searched && !searchResults.length"
        description="未找到相关结果"
      />
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ChatDotRound, Document, Picture, Search, User } from "@element-plus/icons-vue";

const router = useRouter();
const route = useRoute();
const request = inject("request");

const keyword = ref("");
const loading = ref(false);
const searched = ref(false);
const activeTab = ref("all");
const searchResults = ref([]);

const hotKeywords = ["侗医药", "药浴", "传承人", "艾灸", "鼻炎", "风湿"];
const typeList = ["knowledge", "plant", "inheritor", "qa"];

const getTypeIcon = (type) => ({ knowledge: Document, plant: Picture, inheritor: User, qa: ChatDotRound }[type] || Document);
const getTypeTag = (type) => ({ knowledge: "primary", plant: "success", inheritor: "warning", qa: "info" }[type] || "info");
const getTypeName = (type) => ({ knowledge: "知识", plant: "植物", inheritor: "传承人", qa: "问答" }[type] || "其他");

const getResultsByType = (type) => searchResults.value.filter(r => r.type === type);

const doSearch = async () => {
  if (!keyword.value.trim()) return;
  loading.value = true;
  searched.value = true;
  try {
    const [kRes, pRes, iRes, qRes] = await Promise.all([
      request.get(`/knowledge/search?keyword=${keyword.value}`).catch(() => ({ data: [] })),
      request.get(`/plants/search?keyword=${keyword.value}`).catch(() => ({ data: [] })),
      request.get(`/inheritors/search?keyword=${keyword.value}`).catch(() => ({ data: [] })),
      request.get(`/qa/search?keyword=${keyword.value}`).catch(() => ({ data: [] }))
    ]);
    searchResults.value = [
      ...((kRes?.data ?? []).map(r => ({ ...r, type: "knowledge" }))),
      ...((pRes?.data ?? []).map(r => ({ ...r, type: "plant" }))),
      ...((iRes?.data ?? []).map(r => ({ ...r, type: "inheritor" }))),
      ...((qRes?.data ?? []).map(r => ({ ...r, type: "qa" })))
    ];
  } finally {
    loading.value = false;
  }
};

const goToDetail = (item) => {
  const routes = { knowledge: "/knowledge", plant: "/plants", inheritor: "/inheritors", qa: "/qa" };
  router.push(routes[item.type] || "/");
};

onMounted(() => {
  if (route.query.q) { keyword.value = route.query.q; doSearch(); }
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
</style>
