<template>
  <div
    v-loading="pageLoading"
    class="visual-page module-page"
  >
    <div class="module-header">
      <h1>数据可视化</h1>
      <p class="subtitle">
        地域分布可视化 · 使用频次统计 · 传承人分布
      </p>
      <el-button
        type="primary"
        size="small"
        :loading="refreshing"
        style="margin-left: auto;"
        @click="refreshData"
      >
        <el-icon><Refresh /></el-icon>刷新数据
      </el-button>
    </div>

    <div class="visual-dashboard">
      <div class="stats-row">
        <div
          v-for="(stat, i) in statsCards"
          :key="i"
          class="stat-card"
        >
          <div
            class="stat-icon"
            :style="{ background: stat.color }"
          >
            <el-icon :size="24">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </div>

      <div class="charts-row">
        <ChartCard
          title="药方使用频次统计"
          :option="freqChartOption"
          :height="280"
        >
          <template #actions>
            <el-radio-group
              v-model="freqChartType"
              size="small"
            >
              <el-radio-button value="bar">
                柱状图
              </el-radio-button>
              <el-radio-button value="line">
                折线图
              </el-radio-button>
            </el-radio-group>
          </template>
        </ChartCard>
        <ChartCard
          title="疗法分类占比"
          :option="pieChartOption"
          :height="280"
        />
      </div>

      <div class="charts-row">
        <ChartCard
          title="传承人等级分布"
          :option="inheritorChartOption"
          :height="280"
        />
        <ChartCard
          title="药用植物分类统计"
          :option="plantChartOption"
          :height="280"
        />
      </div>

      <div class="charts-row full-width">
        <ChartCard
          title="药用植物地域分布"
          :option="regionChartOption"
          :height="320"
        >
          <template #actions>
            <el-select
              v-model="selectedRegion"
              placeholder="选择地区"
              size="small"
              style="width: 120px;"
            >
              <el-option
                label="全部地区"
                value=""
              />
              <el-option
                v-for="r in regions"
                :key="r"
                :label="r"
                :value="r"
              />
            </el-select>
          </template>
        </ChartCard>
      </div>

      <div class="charts-row">
        <ChartCard
          title="问答分类热度"
          :option="qaChartOption"
          :height="280"
        />
        <ChartCard
          title="用户活跃趋势"
          :option="userChartOption"
          :height="280"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref } from "vue";
import { logFetchError } from '@/utils/logger'
import { ElMessage } from "element-plus";
import { ChatDotRound, DataAnalysis, Document, Picture, Refresh, User } from "@element-plus/icons-vue";
import ChartCard from "@/components/business/display/ChartCard.vue";
import { extractData } from "@/utils";

const request = inject("request");

const pageLoading = ref(false);
const refreshing = ref(false);
const stats = ref({ plants: 0, knowledge: 0, inheritors: 0, qa: 0, resources: 0 });
const freqChartType = ref("bar");
const selectedRegion = ref("");
const regions = ref(["贵州", "湖南", "广西", "云南"]);

const chartData = ref({ therapyCategories: [], inheritorLevels: [], plantCategories: [], qaCategories: [] });

const statsCards = computed(() => [
  { icon: Picture, label: "药用植物", value: stats.value.plants, color: "linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark))" },
  { icon: Document, label: "知识条目", value: stats.value.knowledge, color: "linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark))" },
  { icon: User, label: "传承人", value: stats.value.inheritors, color: "linear-gradient(135deg, var(--dong-gold-light), #e8930c)" },
  { icon: ChatDotRound, label: "问答数据", value: stats.value.qa, color: "linear-gradient(135deg, #667eea, #764ba2)" },
  { icon: DataAnalysis, label: "学习资源", value: stats.value.resources, color: "linear-gradient(135deg, #e74c3c, #c0392b)" }
]);

const freqChartOption = computed(() => ({
  tooltip: { trigger: "axis" },
  xAxis: { type: "category", data: ["鼻炎方", "药浴方", "跌打方", "妇科方", "儿科方", "风湿方"] },
  yAxis: { type: "value" },
  series: [{ type: freqChartType.value, data: [120, 95, 88, 75, 62, 55], itemStyle: { color: "#1A5276" }, areaStyle: freqChartType.value === "line" ? { color: "rgba(26, 82, 118, 0.2)" } : undefined }]
}));

const pieChartOption = computed(() => {
  const data = chartData.value.therapyCategories.length > 0 ? chartData.value.therapyCategories : [
    { value: 35, name: "药浴疗法", itemStyle: { color: "#1A5276" } },
    { value: 25, name: "艾灸疗法", itemStyle: { color: "#28B463" } },
    { value: 20, name: "推拿疗法", itemStyle: { color: "var(--dong-gold-light)" } },
    { value: 12, name: "熏蒸疗法", itemStyle: { color: "#667eea" } },
    { value: 8, name: "其他", itemStyle: { color: "#e74c3c" } }
  ];
  return { tooltip: { trigger: "item" }, legend: { bottom: 10 }, series: [{ type: "pie", radius: ["40%", "70%"], data }] };
});

const inheritorChartOption = computed(() => {
  const data = chartData.value.inheritorLevels.length > 0 ? chartData.value.inheritorLevels : [2, 4, 8, 12];
  return {
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: ["国家级", "省级", "市级", "县级"] },
    yAxis: { type: "value" },
    series: [{ type: "bar", data, itemStyle: { color: (params) => ["var(--dong-gold-light)", "#28B463", "#3498db", "#9b59b6"][params.dataIndex] } }]
  };
});

const plantChartOption = computed(() => {
  const data = chartData.value.plantCategories.length > 0 ? chartData.value.plantCategories : [18, 15, 10, 8, 6];
  return {
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: ["根茎类", "全草类", "花叶类", "果实类", "皮类"], axisLabel: { rotate: 30 } },
    yAxis: { type: "value" },
    series: [{ type: "bar", data, itemStyle: { color: "#28B463" } }]
  };
});

const regionChartOption = computed(() => ({
  tooltip: { trigger: "axis" },
  xAxis: { type: "category", data: regions.value },
  yAxis: { type: "value" },
  series: [{ type: "bar", data: [25, 18, 15, 10], itemStyle: { color: "#1A5276" } }]
}));

const qaChartOption = computed(() => {
  const data = chartData.value.qaCategories.length > 0 ? chartData.value.qaCategories : [85, 72, 68, 55];
  return {
    tooltip: { trigger: "axis" },
    radar: { indicator: [{ name: "侗药常识", max: 100 }, { name: "侗医疗法", max: 100 }, { name: "文化背景", max: 100 }, { name: "用法用量", max: 100 }] },
    series: [{ type: "radar", data: [{ value: data, name: "热度", areaStyle: { color: "rgba(26, 82, 118, 0.3)" } }] }]
  };
});

const userChartOption = computed(() => ({
  tooltip: { trigger: "axis" },
  xAxis: { type: "category", data: ["周一", "周二", "周三", "周四", "周五", "周六", "周日"] },
  yAxis: { type: "value" },
  series: [{ type: "line", data: [120, 132, 101, 134, 90, 230, 210], smooth: true, itemStyle: { color: "#28B463" }, areaStyle: { color: "rgba(40, 180, 99, 0.2)" } }]
}));

const fetchData = async () => {
  try {
    const [pRes, kRes, iRes, qRes, rRes] = await Promise.all([
      request.get("/plants/list").catch(() => ({})),
      request.get("/knowledge/list").catch(() => ({})),
      request.get("/inheritors/list").catch(() => ({})),
      request.get("/qa/list").catch(() => ({})),
      request.get("/resources/list").catch(() => ({}))
    ]);
    
    const plants = extractData(pRes);
    const knowledge = extractData(kRes);
    const inheritors = extractData(iRes);
    const qa = extractData(qRes);
    const resources = extractData(rRes);
    
    stats.value = { plants: plants.length, knowledge: knowledge.length, inheritors: inheritors.length, qa: qa.length, resources: resources.length };

    const therapyMap = {};
    knowledge.forEach(k => { const cat = k.therapyCategory || '其他'; therapyMap[cat] = (therapyMap[cat] || 0) + 1; });
    const colors = ["#1A5276", "#28B463", "var(--dong-gold-light)", "#667eea", "#e74c3c"];
    chartData.value.therapyCategories = Object.entries(therapyMap).map(([name, value], i) => ({ value, name, itemStyle: { color: colors[i % colors.length] } }));

    const levelMap = { '国家级': 0, '省级': 0, '市级': 0, '县级': 0 };
    inheritors.forEach(i => { if (levelMap[i.level] !== undefined) levelMap[i.level]++; });
    chartData.value.inheritorLevels = Object.values(levelMap);

    const categoryMap = {};
    plants.forEach(p => { const cat = p.category || '其他'; categoryMap[cat] = (categoryMap[cat] || 0) + 1; });
    chartData.value.plantCategories = Object.values(categoryMap);

    const qaCatMap = {};
    qa.forEach(q => { const cat = q.category || '其他'; qaCatMap[cat] = (qaCatMap[cat] || 0) + 1; });
    chartData.value.qaCategories = Object.values(qaCatMap);
  } catch (e) {
    console.error('Failed to fetch data:', e);
  }
};

const refreshData = async () => {
  refreshing.value = true;
  try {
    await fetchData();
    ElMessage.success("数据已刷新");
  } catch {
    ElMessage.error("刷新失败");
  } finally {
    refreshing.value = false;
  }
};

onMounted(async () => {
  pageLoading.value = true;
  try { await fetchData(); } finally { pageLoading.value = false; }
});
</script>

<style scoped>
.visual-dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-lg);
}

.stat-card {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  box-shadow: var(--shadow-xs);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-xl);
}

.charts-row.full-width {
  grid-template-columns: 1fr;
}

@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
