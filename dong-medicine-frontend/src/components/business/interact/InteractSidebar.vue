<template>
  <div class="interact-sidebar">
    <el-card
      class="stats-card"
      shadow="hover"
    >
      <template #header>
        <div class="card-header-title">
          <el-icon><Trophy /></el-icon>
          <span>我的成绩</span>
        </div>
      </template>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-value">{{ quizCount }}</span>
          <span class="stat-label">答题次数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ bestScore }}</span>
          <span class="stat-label">最高分</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ gameCount }}</span>
          <span class="stat-label">游戏次数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ totalGameScore }}</span>
          <span class="stat-label">游戏总分</span>
        </div>
      </div>
    </el-card>

    <el-card
      class="rank-card"
      shadow="hover"
    >
      <template #header>
        <div class="card-header-title">
          <el-icon><Medal /></el-icon>
          <span>成绩排行榜</span>
        </div>
      </template>
      <div class="sort-buttons">
        <el-button
          :type="sortBy === 'total' ? 'primary' : 'default'"
          size="small"
          @click="changeSort('total')"
        >
          综合
        </el-button>
        <el-button
          :type="sortBy === 'quiz' ? 'primary' : 'default'"
          size="small"
          @click="changeSort('quiz')"
        >
          答题
        </el-button>
        <el-button
          :type="sortBy === 'game' ? 'primary' : 'default'"
          size="small"
          @click="changeSort('game')"
        >
          游戏
        </el-button>
      </div>
      <div
        v-loading="loading"
        class="rank-list"
      >
        <div
          v-for="(r, i) in displayRank"
          :key="i"
          class="rank-item"
        >
          <span
            class="rank-num"
            :class="getRankClass(i)"
          >{{ r.rank }}</span>
          <span class="rank-name">{{ r.username }}</span>
          <span class="rank-scores">
            <span
              class="score-badge"
              :class="sortBy"
            >{{ r.score ?? r.totalScore }}分</span>
          </span>
        </div>
        <div
          v-if="displayRank.length === 0 && !loading"
          class="empty-rank"
        >
          暂无排行数据
        </div>
      </div>
      <div
        v-if="sortBy === 'total'"
        class="rank-legend"
      >
        <span class="legend-item">综合 = 答题最高分 + 游戏最高分</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from "vue";
import { logFetchError } from '@/utils/logger'
import { Trophy, Medal } from "@element-plus/icons-vue";

const request = inject("request");

const props = defineProps({
  quizCount: { type: Number, default: 0 },
  bestScore: { type: Number, default: 0 },
  gameCount: { type: Number, default: 0 },
  totalGameScore: { type: Number, default: 0 }
});

const sortBy = ref("total");
const loading = ref(false);
const rankData = ref({ total: [], quiz: [], game: [] });

const displayRank = computed(() => rankData.value[sortBy.value]);

const changeSort = (type) => {
  sortBy.value = type;
  loadLeaderboard();
};

const loadLeaderboard = async () => {
  const key = sortBy.value;
  if (rankData.value[key].length > 0) return;
  
  loading.value = true;
  try {
    const urls = { total: "/leaderboard/combined?sortBy=total", quiz: "/leaderboard/quiz", game: "/leaderboard/game" };
    const res = await request.get(urls[key]);
    const payload = res?.data ?? res;
    rankData.value[key] = Array.isArray(payload) ? payload : [];
  } catch (error) {
    console.error("加载排行榜失败:", error);
  } finally {
    loading.value = false;
  }
};

const refreshLeaderboard = () => {
  rankData.value = { total: [], quiz: [], game: [] };
  loadLeaderboard();
};

const getRankClass = (index) => {
  const rank = displayRank.value[index]?.rank || index + 1;
  return rank === 1 ? "gold" : rank === 2 ? "silver" : rank === 3 ? "bronze" : "";
};

defineExpose({ refreshLeaderboard });

onMounted(() => loadLeaderboard());
</script>

<style scoped>
.interact-sidebar { display: flex; flex-direction: column; gap: 20px; }
.stats-card, .rank-card { border-radius: 16px; border: none; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); }
.card-header-title { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--dong-blue); }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.stat-item { text-align: center; padding: 12px 8px; background: #f8fafb; border-radius: 10px; }
.stat-value { display: block; font-size: 22px; font-weight: 700; color: var(--dong-blue); }
.stat-label { font-size: 12px; color: var(--text-muted); }
.sort-buttons { display: flex; gap: 8px; margin-bottom: 12px; }
.sort-buttons .el-button { flex: 1; }
.rank-list { display: flex; flex-direction: column; gap: 8px; min-height: 200px; }
.rank-item { display: flex; align-items: center; gap: 10px; padding: 10px; background: #f8fafb; border-radius: 8px; }
.rank-num { width: 24px; height: 24px; line-height: 24px; text-align: center; background: #e8f4f8; color: var(--dong-blue); border-radius: 6px; font-size: 12px; font-weight: 600; }
.rank-num.gold { background: linear-gradient(135deg, var(--dong-gold-light), #e8930c); color: var(--text-inverse); }
.rank-num.silver { background: linear-gradient(135deg, #bdc3c7, #95a5a6); color: var(--text-inverse); }
.rank-num.bronze { background: linear-gradient(135deg, #cd7f32, var(--dong-copper)); color: var(--text-inverse); }
.rank-name { flex: 1; font-size: 14px; color: #333; }
.rank-scores { display: flex; gap: 4px; }
.score-badge { padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.score-badge.total { background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); color: var(--text-inverse); }
.score-badge.quiz { background: linear-gradient(135deg, var(--dong-green), var(--dong-jade-dark)); color: var(--text-inverse); }
.score-badge.game { background: linear-gradient(135deg, #e74c3c, #c0392b); color: var(--text-inverse); }
.empty-rank { text-align: center; padding: 40px 0; color: #999; font-size: 14px; }
.rank-legend { margin-top: 12px; padding-top: 12px; border-top: 1px dashed #e0e0e0; }
.legend-item { font-size: 11px; color: #999; }
</style>
