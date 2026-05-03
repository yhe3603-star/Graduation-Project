<template>
  <div class="visual-page module-page">
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
        <StatCard
          v-for="(stat, i) in statsCards"
          :key="i"
          :icon="stat.icon"
          :label="stat.label"
          :value="stat.value"
          :color="stat.color"
        />
      </div>

      <div class="charts-row">
        <ChartCard
          title="药方使用频次统计"
          :option="freqChartOption"
          :height="300"
          :loading="chartLoading"
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
          :height="300"
          :loading="chartLoading"
        />
      </div>

      <div class="charts-row">
        <ChartCard
          title="传承人等级分布"
          :option="inheritorChartOption"
          :height="300"
          :loading="chartLoading"
        />
        <ChartCard
          title="药用植物分类统计"
          :option="plantChartOption"
          :height="300"
          :loading="chartLoading"
        />
      </div>

      <div class="charts-row">
        <ChartCard
          title="药用植物地域分布"
          :option="regionChartOption"
          :height="300"
          :loading="chartLoading"
        >
          <template #actions>
            <el-select
              v-model="selectedRegion"
              placeholder="选择地区"
              size="small"
              style="width: 120px;"
              clearable
            >
              <el-option
                label="全部地区"
                value=""
              />
              <el-option
                v-for="r in regionList"
                :key="r"
                :label="r"
                :value="r"
              />
            </el-select>
          </template>
        </ChartCard>
        <ChartCard
          title="药用植物分布占比"
          :option="plantPieChartOption"
          :height="300"
          :loading="chartLoading"
        />
      </div>

      <div class="charts-row full-width">
        <ChartCard
          title="药方/疗法热度排行"
          :option="popularityChartOption"
          :height="340"
          :loading="chartLoading"
        />
      </div>

      <div class="charts-row">
        <ChartCard
          title="问答分类热度"
          :option="qaChartOption"
          :height="300"
          :loading="chartLoading"
        />
        <ChartCard
          title="平台访问趋势"
          :option="trendChartOption"
          :height="300"
          :loading="chartLoading"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import request from '@/utils/request';
import { ElMessage } from "element-plus";
import { ChatDotRound, DataAnalysis, Document, Picture, Refresh, User } from "@element-plus/icons-vue";
import ChartCard from "@/components/business/display/ChartCard.vue";
import StatCard from "@/components/business/display/StatCard.vue";
import { useVisualData } from "@/composables/useVisualData";
import {
  GRADIENT_COLORS, baseTooltip, baseGrid, baseXAxis, baseYAxis,
  createBarSeries, createMultiColorBarSeries, createLineSeries, createPieSeries, createRadarSeries
} from "@/utils/chartConfig";
import "@/styles/Visual.css";

const { loading: chartLoading, stats, chartData, regionList, fetchData } = useVisualData(request);

const pageLoading = ref(false);
const refreshing = ref(false);
const freqChartType = ref("bar");
const selectedRegion = ref("");

const statsCards = computed(() => [
  { icon: Picture, label: "药用植物", value: stats.value.plants, color: "linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark))" },
  { icon: Document, label: "知识条目", value: stats.value.knowledge, color: "linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark))" },
  { icon: User, label: "传承人", value: stats.value.inheritors, color: "linear-gradient(135deg, var(--dong-gold-light), #e8930c)" },
  { icon: ChatDotRound, label: "问答数据", value: stats.value.qa, color: "linear-gradient(135deg, #667eea, #764ba2)" },
  { icon: DataAnalysis, label: "学习资源", value: stats.value.resources, color: "linear-gradient(135deg, #e74c3c, #c0392b)" }
]);

const createBarChart = (names, data, gradient, bottom = '8%', rotate = 0) => ({
  tooltip: { ...baseTooltip, trigger: "axis", axisPointer: { type: 'shadow' } },
  grid: { ...baseGrid, bottom },
  xAxis: { ...baseXAxis, data: names, axisLabel: { ...baseXAxis.axisLabel, rotate } },
  yAxis: baseYAxis,
  series: [createBarSeries(data, gradient)]
});

const freqChartOption = computed(() => {
  const names = chartData.value.formulaNames.length ? chartData.value.formulaNames : ['暂无数据'];
  const data = chartData.value.formulaFreq.length ? chartData.value.formulaFreq : [0];
  const isBar = freqChartType.value === 'bar';
  return {
    tooltip: { ...baseTooltip, trigger: "axis" },
    grid: { ...baseGrid, bottom: '12%' },
    xAxis: { ...baseXAxis, data: names, axisLabel: { color: '#666', fontSize: 11, rotate: 30 } },
    yAxis: baseYAxis,
    series: [{
      type: freqChartType.value,
      data,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: GRADIENT_COLORS.blue.start }, { offset: 1, color: GRADIENT_COLORS.blue.end }] },
        borderRadius: isBar ? [6, 6, 0, 0] : 0
      },
      areaStyle: isBar ? undefined : { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(26, 82, 118, 0.4)' }, { offset: 1, color: 'rgba(26, 82, 118, 0.05)' }] } },
      animationDuration: 1500,
      animationEasing: 'cubicOut'
    }]
  };
});

const pieChartOption = computed(() => ({
  tooltip: { ...baseTooltip, trigger: "item", formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 10, itemWidth: 12, itemHeight: 12, textStyle: { color: '#666' } },
  series: [createPieSeries(chartData.value.therapyCategories.length ? chartData.value.therapyCategories : [{ value: 1, name: '暂无数据', itemStyle: { color: '#e0e0e0' } }])]
}));

const inheritorChartOption = computed(() => {
  const data = chartData.value.inheritorLevels.length ? chartData.value.inheritorLevels : [0, 0, 0, 0];
  const categories = ["国家级", "省级", "市级", "县级"];
  const colors = [GRADIENT_COLORS.gold, GRADIENT_COLORS.green, GRADIENT_COLORS.blue, GRADIENT_COLORS.purple];
  return {
    tooltip: { ...baseTooltip, trigger: "axis", axisPointer: { type: 'shadow' } },
    grid: baseGrid,
    xAxis: { ...baseXAxis, data: categories },
    yAxis: baseYAxis,
    series: [{
      type: "bar",
      data: data.map((v, i) => ({ value: v, itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: colors[i].start }, { offset: 1, color: colors[i].end }] }, borderRadius: [6, 6, 0, 0] } })),
      barWidth: '50%',
      animationDuration: 1500,
      animationEasing: 'cubicOut'
    }]
  };
});

const plantChartOption = computed(() => createBarChart(
  chartData.value.plantCategoryNames.length ? chartData.value.plantCategoryNames : ['暂无数据'],
  chartData.value.plantCategories.length ? chartData.value.plantCategories : [0],
  GRADIENT_COLORS.green, '12%', 30
));

const regionChartOption = computed(() => {
  const filtered = selectedRegion.value ? chartData.value.plantDistribution.filter(d => d.name === selectedRegion.value) : chartData.value.plantDistribution;
  const names = filtered.length ? filtered.map(d => d.name) : ['暂无数据'];
  const data = filtered.length ? filtered.map(d => d.value) : [0];
  return {
    tooltip: { ...baseTooltip, trigger: "axis", axisPointer: { type: 'shadow' } },
    grid: { ...baseGrid, bottom: '10%' },
    xAxis: { ...baseXAxis, data: names, axisLabel: { color: '#666', fontSize: 11, interval: 0, rotate: names.length > 6 ? 30 : 0 } },
    yAxis: baseYAxis,
    series: [createBarSeries(data, GRADIENT_COLORS.teal, '40%')]
  };
});

const plantPieChartOption = computed(() => {
  const names = chartData.value.plantCategoryNames.length ? chartData.value.plantCategoryNames : ['暂无数据'];
  const values = chartData.value.plantCategories.length ? chartData.value.plantCategories : [1];
  const pieData = names.map((n, i) => ({ name: n, value: values[i] || 0 }));
  return {
    tooltip: { ...baseTooltip, trigger: "item", formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 10, itemWidth: 12, itemHeight: 12, textStyle: { color: '#666' } },
    series: [createPieSeries(pieData.length ? pieData : [{ value: 1, name: '暂无数据', itemStyle: { color: '#e0e0e0' } }])]
  };
});

const popularityChartOption = computed(() => {
  const data = chartData.value.knowledgePopularity.length ? chartData.value.knowledgePopularity : [{ name: '暂无数据', value: 0 }];
  return {
    tooltip: { ...baseTooltip, trigger: "axis", axisPointer: { type: 'shadow' } },
    grid: { ...baseGrid, bottom: '15%' },
    xAxis: { ...baseXAxis, data: data.map(d => d.name), axisLabel: { rotate: 30, color: '#666', fontSize: 10, interval: 0 } },
    yAxis: baseYAxis,
    series: [createMultiColorBarSeries(data)]
  };
});

const qaChartOption = computed(() => {
  const values = chartData.value.qaCategories.length ? chartData.value.qaCategories : [0, 0, 0, 0];
  const names = chartData.value.qaCategoryNames.length ? chartData.value.qaCategoryNames : ['暂无数据', '暂无数据', '暂无数据', '暂无数据'];
  const maxVal = Math.max(...values, 100);
  return {
    tooltip: baseTooltip,
    radar: {
      indicator: names.map(name => ({ name, max: maxVal })),
      center: ['50%', '50%'],
      radius: '65%',
      axisName: { color: '#666', fontSize: 11 },
      splitArea: { areaStyle: { color: ['rgba(26, 82, 118, 0.02)', 'rgba(26, 82, 118, 0.05)', 'rgba(26, 82, 118, 0.08)', 'rgba(26, 82, 118, 0.11)'] } },
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      splitLine: { lineStyle: { color: '#e0e0e0' } }
    },
    series: [createRadarSeries(values, GRADIENT_COLORS.blue)]
  };
});

const trendChartOption = computed(() => {
  const dates = chartData.value.userTrendDates.length ? chartData.value.userTrendDates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const data = chartData.value.userTrend.length ? chartData.value.userTrend : [0, 0, 0, 0, 0, 0, 0];
  return {
    tooltip: { ...baseTooltip, trigger: "axis" },
    grid: baseGrid,
    xAxis: { ...baseXAxis, data: dates, boundaryGap: false },
    yAxis: baseYAxis,
    series: [createLineSeries(data, GRADIENT_COLORS.green)]
  };
});

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
