<template>
  <div class="dashboard-section">
    <div class="stats-grid">
      <div
        v-for="(stat, i) in stats"
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

    <el-card
      shadow="never"
      class="dashboard-card dashboard-row"
    >
      <template #header>
        <span>数据趋势</span>
      </template>
      <el-tabs v-model="activeChartTab">
        <el-tab-pane
          label="用户增长"
          name="userGrowth"
        >
          <div
            ref="userGrowthChartRef"
            style="height: 320px;"
          />
        </el-tab-pane>
        <el-tab-pane
          label="内容浏览"
          name="contentViews"
        >
          <div
            ref="contentViewsChartRef"
            style="height: 320px;"
          />
        </el-tab-pane>
        <el-tab-pane
          label="搜索热词"
          name="searchKeywords"
        >
          <div
            ref="searchKeywordsChartRef"
            style="height: 320px;"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-row
      :gutter="24"
      class="dashboard-row"
    >
      <el-col :span="16">
        <el-card
          shadow="never"
          class="dashboard-card"
        >
          <template #header>
            <div class="card-header">
              <span>最新反馈</span>
              <el-button
                type="primary"
                size="small"
                style="color: var(--text-inverse);"
                @click="$emit('view-feedback')"
              >
                查看全部
              </el-button>
            </div>
          </template>
          <div class="feedback-list">
            <div
              v-for="(item, i) in paginatedFeedback"
              :key="i"
              class="feedback-item"
            >
              <el-avatar :size="36">
                {{ item.userName?.charAt(0) || '游' }}
              </el-avatar>
              <div class="feedback-content">
                <div class="feedback-header">
                  <span class="user-name">{{ item.userName || '游客' }}</span>
                  <span class="feedback-time">{{ formatTime(item.createTime || item.createdAt) }}</span>
                </div>
                <p class="feedback-text">
                  {{ item.content }}
                </p>
              </div>
              <el-tag
                :type="item.status === 'resolved' ? 'success' : item.status === 'processing' ? 'primary' : 'warning'"
                size="small"
              >
                {{ item.status === 'resolved' ? '已处理' : item.status === 'processing' ? '处理中' : '待处理' }}
              </el-tag>
            </div>
            <el-empty
              v-if="!feedbackList.length"
              description="暂无反馈"
            />
          </div>
          <div
            v-if="feedbackList.length > pageSize"
            class="pagination-wrapper"
          >
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20]"
              :total="feedbackList.length"
              layout="total, sizes, prev, pager, next"
              small
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card
          shadow="never"
          class="dashboard-card"
        >
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button
              v-for="action in quickActions"
              :key="action.menu"
              @click="$emit('navigate', action.menu)"
            >
              <el-icon><component :is="action.icon" /></el-icon>
              {{ action.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from "vue";
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])
import { logPermissionWarn } from '@/utils/logger'
import { DataLine, User, Document, Avatar, Picture, ChatDotRound, Folder, EditPen, Tickets, List, Message, Comment } from "@element-plus/icons-vue";
import request from "@/utils/request";

const props = defineProps({
  stats: { type: Object, default: null },
  users: { type: Array, default: () => [] },
  knowledge: { type: Array, default: () => [] },
  inheritors: { type: Array, default: () => [] },
  plants: { type: Array, default: () => [] },
  qa: { type: Array, default: () => [] },
  resources: { type: Array, default: () => [] },
  feedback: { type: Array, default: () => [] },
  quiz: { type: Array, default: () => [] },
  comments: { type: Array, default: () => [] }
});

defineEmits(["view-feedback", "navigate"]);

const currentPage = ref(1);
const pageSize = ref(5);
const logCount = ref(0);

const activeChartTab = ref('userGrowth');
const userGrowthChartRef = ref(null);
const contentViewsChartRef = ref(null);
const searchKeywordsChartRef = ref(null);
const chartInstances = [];

const formatTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : '无';

const loadLogStats = async () => {
  try {
    const res = await request.get('/admin/logs/stats');
    const data = res.data || res;
    logCount.value = data.total || 0;
  } catch {
    logPermissionWarn('日志统计')
    logCount.value = 0;
  }
};

const initChart = (el) => {
  const chart = echarts.init(el);
  chartInstances.push(chart);
  return chart;
};

const loadUserGrowthChart = async () => {
  if (!userGrowthChartRef.value) return;
  try {
    const res = await request.get('/admin/stats/user-growth');
    const data = res.data || res;
    const chart = initChart(userGrowthChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { top: 20, right: 20, bottom: 30, left: 50 },
      xAxis: { type: 'category', data: data.dates || [], boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        data: data.counts || [],
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#667eea' },
        areaStyle: {
          color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{ offset: 0, color: 'rgba(102,126,234,0.3)' }, { offset: 1, color: 'rgba(102,126,234,0.02)' }]
          }
        }
      }]
    });
  } catch (e) {
    console.error('用户增长图表加载失败:', e);
  }
};

const loadContentViewsChart = async () => {
  if (!contentViewsChartRef.value) return;
  try {
    const res = await request.get('/admin/stats/content-views');
    const data = (res.data || res || []).reverse();
    const chart = initChart(contentViewsChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { top: 10, right: 20, bottom: 30, left: 120 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: data.map(d => (d.name || '').substring(0, 12)) },
      series: [{
        data: data.map(d => d.value),
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [{ offset: 0, color: '#3498db' }, { offset: 1, color: '#2ecc71' }]
          },
          borderRadius: [0, 4, 4, 0]
        }
      }]
    });
  } catch (e) {
    console.error('内容浏览图表加载失败:', e);
  }
};

const loadSearchKeywordsChart = async () => {
  if (!searchKeywordsChartRef.value) return;
  try {
    const res = await request.get('/admin/stats/search-keywords');
    const data = (res.data || res || []).reverse();
    const chart = initChart(searchKeywordsChartRef.value);
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { top: 10, right: 20, bottom: 30, left: 80 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: data.map(d => d.name) },
      series: [{
        data: data.map(d => d.value),
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [{ offset: 0, color: '#e74c3c' }, { offset: 1, color: '#f39c12' }]
          },
          borderRadius: [0, 4, 4, 0]
        }
      }]
    });
  } catch (e) {
    console.error('搜索热词图表加载失败:', e);
  }
};

const chartLoaders = { userGrowth: loadUserGrowthChart, contentViews: loadContentViewsChart, searchKeywords: loadSearchKeywordsChart };
const loadedTabs = new Set();

const loadActiveChart = () => {
  const tab = activeChartTab.value;
  if (!loadedTabs.has(tab)) {
    loadedTabs.add(tab);
    nextTick(() => chartLoaders[tab]?.());
  }
};

watch(activeChartTab, loadActiveChart);

onMounted(() => {
  loadLogStats();
  nextTick(loadActiveChart);
});

onUnmounted(() => {
  chartInstances.forEach(chart => chart?.dispose());
  chartInstances.length = 0;
});

const n = (key, fallbackArr) => {
  const s = props.stats
  if (s && typeof s[key] === 'number') return s[key]
  return (fallbackArr || []).length
}

const stats = computed(() => [
  { icon: User, label: "用户总数", value: n('users', props.users), color: "linear-gradient(135deg, #667eea, #764ba2)" },
  { icon: Document, label: "知识条目", value: n('knowledge', props.knowledge), color: "linear-gradient(135deg, var(--dong-gold-light), #e8930c)" },
  { icon: Avatar, label: "传承人", value: n('inheritors', props.inheritors), color: "linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark))" },
  { icon: Picture, label: "药用植物", value: n('plants', props.plants), color: "linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark))" },
  { icon: ChatDotRound, label: "问答数据", value: n('qa', props.qa), color: "linear-gradient(135deg, #e74c3c, #c0392b)" },
  { icon: Folder, label: "资源文件", value: n('resources', props.resources), color: "linear-gradient(135deg, #3498db, #2980b9)" },
  { icon: DataLine, label: "答题题目", value: n('quiz', props.quiz), color: "linear-gradient(135deg, #9b59b6, #8e44ad)" },
  { icon: Message, label: "评论数量", value: n('comments', props.comments), color: "linear-gradient(135deg, #1abc9c, #16a085)" },
  { icon: List, label: "系统日志", value: logCount.value, color: "linear-gradient(135deg, #34495e, #2c3e50)" },
  { icon: EditPen, label: "反馈总数", value: n('feedback', props.feedback), color: "linear-gradient(135deg, #f39c12, #e74c3c)" }
]);

const feedbackList = computed(() => {
  const fb = props.feedback;
  if (!fb || !Array.isArray(fb)) return [];
  return [...fb].sort((a, b) => new Date(b.createTime || b.createdAt || 0) - new Date(a.createTime || a.createdAt || 0));
});

const paginatedFeedback = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return feedbackList.value.slice(start, start + pageSize.value);
});

const quickActions = [
  { menu: "users", icon: User, label: "用户管理" },
  { menu: "quiz", icon: DataLine, label: "答题管理" },
  { menu: "comments", icon: Comment, label: "评论管理" },
  { menu: "logs", icon: List, label: "日志管理" },
  { menu: "knowledge", icon: Document, label: "管理知识" },
  { menu: "inheritors", icon: Avatar, label: "管理传承人" },
  { menu: "plants", icon: Picture, label: "管理植物" },
  { menu: "qa", icon: ChatDotRound, label: "管理问答" },
  { menu: "resources", icon: Folder, label: "管理资源" },
  { menu: "feedback", icon: Tickets, label: "反馈管理" }
];
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: var(--text-inverse); border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); transition: transform 0.3s, box-shadow 0.3s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: var(--text-inverse); flex-shrink: 0; }
.stat-info { display: flex; flex-direction: column; min-width: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); white-space: nowrap; }
.dashboard-row { margin-top: 24px; }
.dashboard-card { border-radius: 12px; height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.feedback-list { display: flex; flex-direction: column; gap: 12px; max-height: 400px; overflow-y: auto; }
.feedback-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px; background: #f8fafb; border-radius: 8px; }
.feedback-content { flex: 1; }
.feedback-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.user-name { font-size: 14px; font-weight: 500; }
.feedback-time { font-size: 12px; color: #999; }
.feedback-text { font-size: 13px; color: #666; margin: 0; }
.quick-actions { display: flex; flex-direction: column; gap: 12px; }
.quick-actions .el-button { justify-content: flex-start; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

@media (max-width: 1400px) {
  .stats-grid { grid-template-columns: repeat(5, 1fr); gap: 12px; }
  .stat-card { padding: 16px; }
  .stat-icon { width: 42px; height: 42px; }
  .stat-value { font-size: 20px; }
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(5, 1fr); gap: 10px; }
  .stat-card { padding: 14px; gap: 12px; }
  .stat-icon { width: 38px; height: 38px; }
  .stat-value { font-size: 18px; }
  .stat-label { font-size: 12px; }
}

@media (max-width: 992px) {
  .stats-grid { grid-template-columns: repeat(5, 1fr); gap: 8px; }
  .stat-card { padding: 12px; gap: 10px; flex-direction: column; text-align: center; }
  .stat-icon { width: 36px; height: 36px; }
  .stat-value { font-size: 16px; }
  .stat-label { font-size: 11px; }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
