<template>
  <div class="game-section">
    <div
      v-if="!currentPlant && !finished"
      class="game-intro"
    >
      <div class="intro-icon">
        🌿
      </div>
      <h2>药用植物识别游戏</h2>
      <p>根据图片识别药用植物，测试你的辨识能力</p>
      <div class="intro-rules">
        <div class="rule-item">
          <el-icon><Clock /></el-icon>
          <span>限时3分钟</span>
        </div>
        <div class="rule-item">
          <el-icon><Star /></el-icon>
          <span>初级10分/中级15分/高级20分</span>
        </div>
      </div>
      <div class="difficulty-select">
        <div 
          v-for="d in difficulties" 
          :key="d.value" 
          class="difficulty-card"
          :class="{ active: selectedDifficulty === d.value }"
          @click="selectDifficulty(d.value)"
        >
          <div class="diff-icon">
            {{ d.icon }}
          </div>
          <div class="diff-name">
            {{ d.label }}
          </div>
          <div class="diff-desc">
            {{ d.desc }}
          </div>
          <div class="diff-score">
            {{ d.score }}分/题
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="currentPlant && !finished"
      class="game-content"
    >
      <div class="game-header">
        <div class="game-score">
          <span class="score-label">当前得分</span>
          <span class="score-value">{{ score }}</span>
        </div>
        <div
          class="countdown-timer"
          :class="{ 'low-time': isLowTime, 'flashing': isLowTime }"
        >
          <el-icon><Clock /></el-icon>
          <span>{{ formattedTime }}</span>
        </div>
        <div
          v-if="streak > 0"
          class="game-streak"
        >
          <el-icon><TrendCharts /></el-icon>
          连击 {{ streak }}
        </div>
      </div>

      <el-card
        class="game-card"
        shadow="never"
      >
        <div class="game-img-wrap">
          <el-image
            :src="plantImage"
            fit="contain"
            class="game-img"
            lazy
          >
            <template #placeholder>
              <div class="img-placeholder">
                加载中...
              </div>
            </template>
            <template #error>
              <div class="img-error">
                植物图片
              </div>
            </template>
          </el-image>
        </div>
        <h3 class="game-question">
          这是什么植物？
        </h3>
        <div class="game-options">
          <el-button 
            v-for="(opt, i) in options" 
            :key="i" 
            :disabled="answered" 
            class="game-opt-btn"
            :class="{ 
              correct: answered && opt === currentPlant.nameCn,
              wrong: answered && opt === selectedAnswer && opt !== currentPlant.nameCn
            }"
            @click="checkAnswer(opt)"
          >
            {{ opt }}
          </el-button>
        </div>
      </el-card>

      <div class="game-actions">
        <el-button
          type="success"
          :loading="submitting"
          @click="$emit('endGame')"
        >
          <el-icon><Check /></el-icon>
          结束游戏
        </el-button>
        <el-button
          v-if="isLoggedIn"
          @click="$emit('favorite')"
        >
          <el-icon><Star /></el-icon>
          收藏植物
        </el-button>
      </div>
    </div>

    <div
      v-if="finished"
      class="game-result"
    >
      <div class="result-icon">
        🎉
      </div>
      <h2>游戏结束</h2>
      <div class="score-circle">
        <span class="score-num">{{ score }}</span>
        <span class="score-unit">分</span>
      </div>
      <p class="score-detail">
        正确 {{ correct }} / {{ total }} 题
      </p>
      <p class="difficulty-info">
        难度：{{ difficultyText }}
      </p>
      <div class="result-actions">
        <el-button
          type="primary"
          size="large"
          @click="$emit('restart')"
        >
          <el-icon><RefreshRight /></el-icon>
          重新选择难度
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { Clock, Star, TrendCharts, Check, RefreshRight } from "@element-plus/icons-vue";
import { getFirstImage } from "@/utils";

const props = defineProps({
  currentPlant: Object,
  options: { type: Array, default: () => [] },
  score: { type: Number, default: 0 },
  streak: { type: Number, default: 0 },
  answered: Boolean,
  selectedAnswer: String,
  finished: Boolean,
  submitting: Boolean,
  correct: { type: Number, default: 0 },
  total: { type: Number, default: 0 },
  isLoggedIn: Boolean,
  selectedDifficulty: String,
  formattedTime: { type: String, default: "03:00" },
  isLowTime: Boolean
});

const emit = defineEmits(["selectDifficulty", "answer", "endGame", "favorite", "restart"]);

const difficulties = [
  { value: 'easy', label: '初级', icon: '🌱', desc: '3个选项', score: 10 },
  { value: 'medium', label: '中级', icon: '🌿', desc: '4个选项', score: 15 },
  { value: 'hard', label: '高级', icon: '🌳', desc: '5个选项', score: 20 },
];

const PLACEHOLDER_IMG = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='300'%3E%3Crect fill='%23e8f4f8' width='400' height='300'/%3E%3Ctext x='50%25' y='50%25' fill='%231A5276' text-anchor='middle' dy='.3em' font-size='16'%3E植物图片%3C/text%3E%3C/svg%3E";

const plantImage = computed(() => {
  if (!props.currentPlant) return PLACEHOLDER_IMG;
  return getFirstImage(props.currentPlant.images);
});

const difficultyText = computed(() => {
  const d = difficulties.find(item => item.value === props.selectedDifficulty);
  return d ? d.label : '初级';
});

const selectDifficulty = (level) => emit("selectDifficulty", level);
const checkAnswer = (opt) => emit("answer", opt);
</script>

<style scoped>
.game-intro, .game-result { text-align: center; padding: 40px 20px; }
.intro-icon { font-size: 64px; margin-bottom: 20px; }
.game-intro h2, .game-result h2 { font-size: 24px; color: var(--dong-blue); margin: 0 0 12px 0; }
.game-intro p { font-size: 14px; color: #666; margin: 0 0 24px 0; }
.intro-rules { display: flex; justify-content: center; gap: 32px; margin-bottom: 24px; }
.rule-item { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #666; }
.difficulty-select { display: flex; justify-content: center; gap: 16px; margin-top: 24px; }
.difficulty-card { width: 140px; padding: 20px; background: #f8fafb; border: 2px solid #e8f4f8; border-radius: 12px; cursor: pointer; transition: all 0.3s; }
.difficulty-card:hover { border-color: var(--dong-blue); }
.difficulty-card.active { background: linear-gradient(135deg, rgba(26, 82, 118, 0.1), rgba(40, 180, 99, 0.1)); border-color: var(--dong-blue); }
.diff-icon { font-size: 32px; margin-bottom: 8px; }
.diff-name { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 4px; }
.diff-desc { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }
.diff-score { font-size: 12px; color: var(--dong-green); font-weight: 600; }
.game-content { max-width: 500px; margin: 0 auto; }
.game-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.game-score { display: flex; align-items: baseline; gap: 8px; }
.score-label { font-size: 14px; color: #666; }
.score-value { font-size: 28px; font-weight: 700; color: var(--dong-green); }
.countdown-timer { display: flex; align-items: center; gap: 6px; padding: 6px 14px; background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); color: var(--text-inverse); border-radius: 20px; font-size: 16px; font-weight: 700; font-family: 'Consolas', monospace; }
.countdown-timer.low-time { background: linear-gradient(135deg, #e74c3c, #c0392b); }
.countdown-timer.flashing { animation: flash 0.5s ease-in-out infinite; }
@keyframes flash { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.game-streak { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: linear-gradient(135deg, var(--dong-gold-light), #e8930c); color: var(--text-inverse); border-radius: 20px; font-size: 14px; font-weight: 600; }
.game-card { border-radius: 16px; padding: 24px; text-align: center; }
.game-img-wrap { height: 240px; margin-bottom: 20px; border-radius: 12px; overflow: hidden; background: #f5f5f5; }
.game-img { width: 100%; height: 100%; }
.img-placeholder, .img-error { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background: linear-gradient(135deg, #e8f4f8, #d4e8ed); color: var(--dong-blue); }
.game-question { font-size: 18px; margin: 0 0 20px 0; color: var(--dong-blue); }
.game-options { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.game-opt-btn { height: 48px; border-radius: 10px; font-size: 15px; }
.game-opt-btn.correct { background: var(--dong-green) !important; border-color: var(--dong-green) !important; color: var(--text-inverse) !important; }
.game-opt-btn.wrong { background: #e74c3c !important; border-color: #e74c3c !important; color: var(--text-inverse) !important; }
.game-actions { display: flex; justify-content: center; gap: 12px; margin-top: 20px; }
.result-icon { font-size: 64px; margin-bottom: 16px; }
.game-result h2 { font-size: 20px; color: #333; margin: 0 0 24px 0; }
.score-circle { display: inline-flex; align-items: baseline; justify-content: center; width: 120px; height: 120px; background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); border-radius: 50%; margin-bottom: 16px; }
.score-num { font-size: 42px; font-weight: 700; color: var(--text-inverse); }
.score-unit { font-size: 16px; color: rgba(255, 255, 255, 0.8); margin-left: 4px; }
.score-detail { font-size: 14px; color: #666; margin: 0 0 8px 0; }
.difficulty-info { font-size: 13px; color: var(--text-muted); margin: 0 0 24px 0; }
.result-actions { display: flex; justify-content: center; gap: 12px; }

@media (max-width: 640px) {
  .difficulty-select { flex-direction: column; align-items: center; }
  .difficulty-card { width: 100%; max-width: 200px; }
  .intro-rules { flex-direction: column; gap: 8px; }
}
</style>
