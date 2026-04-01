<template>
  <div class="quiz-section">
    <div
      v-if="!isStarted && !finished"
      class="quiz-intro"
    >
      <div class="intro-icon">
        📝
      </div>
      <h2>侗医药知识趣味答题</h2>
      <p>选择难度，测试你对侗医药的了解程度</p>
      <div class="intro-rules">
        <div class="rule-item">
          <el-icon><Clock /></el-icon>
          <span>限时作答</span>
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
            {{ d.count }}题
          </div>
          <div class="diff-score">
            {{ d.score }}分/题
          </div>
        </div>
      </div>
      <el-button
        type="primary"
        size="large"
        class="start-btn"
        :loading="loading"
        @click="$emit('start')"
      >
        <el-icon><VideoPlay /></el-icon>
        开始答题
      </el-button>
    </div>

    <div
      v-if="isStarted"
      class="quiz-content"
    >
      <div class="quiz-header">
        <div class="header-top">
          <div class="progress-text">
            第 {{ current + 1 }} / {{ questions.length }} 题
          </div>
          <div
            class="countdown-timer"
            :class="{ 'low-time': isLowTime, 'flashing': isLowTime }"
          >
            <el-icon><Clock /></el-icon>
            <span>{{ formattedTime }}</span>
          </div>
        </div>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{ width: ((current + 1) / questions.length * 100) + '%' }"
          />
        </div>
      </div>

      <el-card
        class="quiz-card"
        shadow="never"
      >
        <h3 class="quiz-question">
          {{ questions[current]?.q }}
        </h3>
        <el-radio-group
          v-model="answers[current]"
          class="quiz-options"
        >
          <el-radio
            v-for="(opt, i) in (questions[current]?.options || [])"
            :key="i"
            :label="opt"
            border
            class="option-item"
          >
            <span class="option-letter">{{ String.fromCharCode(65 + i) }}</span>
            <span class="option-text">{{ opt }}</span>
          </el-radio>
        </el-radio-group>
      </el-card>

      <div class="quiz-actions">
        <el-button
          :disabled="current === 0"
          size="large"
          @click="$emit('prev')"
        >
          <el-icon><ArrowLeft /></el-icon>
          上一题
        </el-button>
        <el-button
          v-if="current < questions.length - 1"
          type="primary"
          size="large"
          @click="$emit('next')"
        >
          下一题
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button
          v-else
          type="success"
          :loading="submitting"
          size="large"
          @click="$emit('submit')"
        >
          <el-icon><Check /></el-icon>
          提交答卷
        </el-button>
      </div>
    </div>

    <div
      v-if="finished"
      class="quiz-result"
    >
      <div
        class="result-icon"
        :class="scoreLevel"
      >
        {{ scoreEmoji }}
      </div>
      <h2>{{ scoreText }}</h2>
      <div class="score-circle">
        <span class="score-num">{{ score }}</span>
        <span class="score-unit">分</span>
      </div>
      <p class="score-detail">
        正确 {{ correct }} / {{ questions.length }} 题
      </p>
      <p class="difficulty-info">
        难度：{{ difficultyText }}
      </p>
      <div class="result-actions">
        <el-button
          type="primary"
          size="large"
          @click="$emit('retry')"
        >
          <el-icon><RefreshRight /></el-icon>
          重新选择难度
        </el-button>
        <el-button
          size="large"
          @click="$emit('share')"
        >
          <el-icon><Share /></el-icon>
          分享成绩
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { Clock, Star, VideoPlay, ArrowLeft, ArrowRight, Check, RefreshRight, Share } from "@element-plus/icons-vue";

const props = defineProps({
  isStarted: Boolean,
  finished: Boolean,
  loading: Boolean,
  submitting: Boolean,
  questions: { type: Array, default: () => [] },
  current: { type: Number, default: 0 },
  answers: { type: Array, default: () => [] },
  score: { type: Number, default: 0 },
  correct: { type: Number, default: 0 },
  formattedTime: { type: String, default: "03:00" },
  isLowTime: Boolean,
  selectedDifficulty: { type: String, default: 'easy' }
});

const emit = defineEmits(["start", "prev", "next", "submit", "retry", "share", "selectDifficulty"]);

const difficulties = [
  { value: 'easy', label: '初级', icon: '📝', count: 10, score: 10 },
  { value: 'medium', label: '中级', icon: '💡', count: 20, score: 15 },
  { value: 'hard', label: '高级', icon: '🏆', count: 30, score: 20 },
];

const getScoreLevel = (score) => score >= 90 ? 0 : score >= 70 ? 1 : score >= 60 ? 2 : 3;

const scoreLevel = computed(() => ["excellent", "good", "pass", "fail"][getScoreLevel(props.score)]);

const scoreEmoji = computed(() => ["🏆", "😊", "🙂", "💪"][getScoreLevel(props.score)]);

const scoreText = computed(() => [
  "太棒了！你是侗医药达人！",
  "不错哦！继续加油！",
  "及格啦！再接再厉！",
  "还需努力，多学习侗医药知识吧！"
][getScoreLevel(props.score)]);

const difficultyText = computed(() => {
  const d = difficulties.find(item => item.value === props.selectedDifficulty);
  return d ? d.label : '初级';
});

const selectDifficulty = (level) => emit("selectDifficulty", level);
</script>

<style scoped>
.quiz-intro, .quiz-result { text-align: center; padding: 40px 20px; }
.intro-icon { font-size: 64px; margin-bottom: 20px; }
.quiz-intro h2, .quiz-result h2 { font-size: 24px; color: var(--dong-blue); margin: 0 0 12px 0; }
.quiz-intro p { font-size: 14px; color: #666; margin: 0 0 24px 0; }
.intro-rules { display: flex; justify-content: center; gap: 32px; margin-bottom: 24px; }
.rule-item { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #666; }
.difficulty-select { display: flex; justify-content: center; gap: 16px; margin-bottom: 32px; }
.difficulty-card { width: 140px; padding: 20px; background: #f8fafb; border: 2px solid #e8f4f8; border-radius: 12px; cursor: pointer; transition: all 0.3s; }
.difficulty-card:hover { border-color: var(--dong-blue); }
.difficulty-card.active { background: linear-gradient(135deg, rgba(26, 82, 118, 0.1), rgba(40, 180, 99, 0.1)); border-color: var(--dong-blue); }
.diff-icon { font-size: 32px; margin-bottom: 8px; }
.diff-name { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 4px; }
.diff-desc { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }
.diff-score { font-size: 12px; color: var(--dong-green); font-weight: 600; }
.start-btn { padding: 14px 40px; font-size: 16px; border-radius: 12px; }
.quiz-content { max-width: 640px; margin: 0 auto; }
.quiz-header { margin-bottom: 20px; }
.header-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.progress-bar { height: 6px; background: #e8f4f8; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, var(--dong-green), var(--dong-jade-dark)); border-radius: 3px; transition: width 0.3s; }
.progress-text { font-size: 14px; color: var(--dong-green); font-weight: 600; }
.countdown-timer { display: flex; align-items: center; gap: 6px; padding: 6px 14px; background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); color: var(--text-inverse); border-radius: 20px; font-size: 16px; font-weight: 700; font-family: 'Consolas', monospace; }
.countdown-timer.low-time { background: linear-gradient(135deg, #e74c3c, #c0392b); }
.countdown-timer.flashing { animation: flash 0.5s ease-in-out infinite; }
@keyframes flash { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.quiz-card { border-radius: 16px; padding: 28px; margin-bottom: 20px; }
.quiz-question { font-size: 18px; color: var(--text-primary); margin: 0 0 24px 0; line-height: 1.6; }
.quiz-options { display: flex; flex-direction: column; gap: 12px; width: 100%; }
.option-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 12px; margin: 0; }
.option-item :deep(.el-radio__input) { display: none; }
.option-item :deep(.el-radio__label) { display: flex; align-items: center; gap: 12px; padding-left: 0; }
.option-letter { width: 28px; height: 28px; line-height: 28px; text-align: center; background: #e8f4f8; color: var(--dong-blue); border-radius: 8px; font-weight: 600; font-size: 14px; }
.option-text { font-size: 15px; color: #333; }
.option-item.is-checked .option-letter { background: var(--dong-blue); color: var(--text-inverse); }
.quiz-actions { display: flex; justify-content: center; gap: 12px; }
.result-icon { font-size: 64px; margin-bottom: 16px; }
.quiz-result h2 { font-size: 20px; color: #333; margin: 0 0 24px 0; }
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
