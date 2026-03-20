<template>
  <div
    v-loading="pageLoading"
    class="interact-page module-page"
    element-loading-text="加载中..."
  >
    <div class="module-header">
      <h1>文化互动专区</h1>
      <p class="subtitle">
        趣味答题 · 植物识别 · 评论交流
      </p>
    </div>

    <div class="interact-container">
      <InteractSidebar 
        ref="sidebarRef"
        :quiz-count="quizRecords.length"
        :best-score="bestScore"
        :game-count="gameRecords.length"
        :total-game-score="totalGameScore"
      />

      <div class="interact-main">
        <el-tabs
          v-model="activeTab"
          class="interact-tabs"
        >
          <el-tab-pane name="quiz">
            <template #label>
              <span class="tab-label"><el-icon><EditPen /></el-icon>趣味答题</span>
            </template>
            <QuizSection 
              :is-started="isQuizStarted"
              :finished="quizFinished"
              :loading="quizLoading"
              :submitting="submitting"
              :questions="selectedQuestions"
              :current="currentQuestion"
              :answers="userAnswers"
              :score="finalScore"
              :correct="correctCount"
              :formatted-time="quizFormattedTime"
              :is-low-time="quizIsLowTime"
              @start="startNewQuiz"
              @prev="prevQuestion"
              @next="nextQuestion"
              @submit="handleQuizSubmit"
              @retry="resetQuiz"
              @share="shareQuizResult"
            />
          </el-tab-pane>

          <el-tab-pane name="game">
            <template #label>
              <span class="tab-label"><el-icon><Picture /></el-icon>植物识别</span>
            </template>
            <PlantGame 
              :current-plant="currentPlant"
              :options="options"
              :score="gameScore"
              :streak="streak"
              :answered="answered"
              :selected-answer="selectedAnswer"
              :finished="gameFinished"
              :submitting="submittingGame"
              :correct="correctAnswers"
              :total="totalQuestions"
              :is-logged-in="isLoggedIn"
              :selected-difficulty="difficulty"
              :formatted-time="gameFormattedTime"
              :is-low-time="gameIsLowTime"
              @select-difficulty="setDifficulty"
              @answer="checkAnswer"
              @end-game="handleGameSubmit"
              @favorite="favoriteCurrentPlant"
              @restart="resetGame"
            />
          </el-tab-pane>

          <el-tab-pane name="comment">
            <template #label>
              <span class="tab-label"><el-icon><ChatDotRound /></el-icon>评论交流</span>
            </template>
            <CommentSection 
              :comments="comments"
              :is-logged-in="isLoggedIn"
              :user-name="userName"
              :loading="commentLoading"
              @post="handleCommentPost"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { ChatDotRound, EditPen, Picture } from "@element-plus/icons-vue";
import CommentSection from "@/components/business/interact/CommentSection.vue";
import InteractSidebar from "@/components/business/interact/InteractSidebar.vue";
import PlantGame from "@/components/business/interact/PlantGame.vue";
import QuizSection from "@/components/business/interact/QuizSection.vue";
import { useComments } from "@/composables/useInteraction";
import { usePlantGame } from "@/composables/usePlantGame";
import { useQuiz } from "@/composables/useQuiz";

const request = inject("request");
const isLoggedIn = inject("isLoggedIn");
const userName = inject("userName");

const pageLoading = ref(false);
const activeTab = ref("quiz");
const sidebarRef = ref(null);

const {
  isQuizStarted, selectedQuestions, userAnswers, currentQuestion, quizFinished, finalScore, correctCount, quizLoading, submitting, quizRecords,
  formattedTime: quizFormattedTime, isLowTime: quizIsLowTime,
  startNewQuiz, resetQuiz, nextQuestion, prevQuestion, submitQuiz, shareQuizResult, loadQuizRecords, bestScore,
} = useQuiz(request, isLoggedIn);

const {
  difficulty, currentPlant, options, answered, selectedAnswer, gameScore, streak, totalQuestions, correctAnswers, gameFinished, submittingGame, gameRecords,
  formattedTime: gameFormattedTime, isLowTime: gameIsLowTime,
  setDifficulty, checkAnswer, resetGame, submitGameScore, favoriteCurrentPlant, loadGameRecords, loadPlants, totalGameScore,
} = usePlantGame(request, isLoggedIn);

const { comments, commentLoading, loadComments, handleCommentPost } = useComments(request);

const handleQuizSubmit = async () => {
  await submitQuiz();
  sidebarRef.value?.refreshLeaderboard();
};

const handleGameSubmit = async () => {
  await submitGameScore();
  sidebarRef.value?.refreshLeaderboard();
};

onMounted(async () => {
  pageLoading.value = true;
  try {
    await loadPlants();
    if (isLoggedIn.value) {
      await loadQuizRecords();
      await loadGameRecords();
    }
    await loadComments();
  } catch {
    ElMessage.error("数据加载失败");
  } finally {
    pageLoading.value = false;
  }
});
</script>

<style scoped>
.interact-container {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: var(--space-xl);
}

.interact-main {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

@media (max-width: 1024px) {
  .interact-container {
    grid-template-columns: 1fr;
  }
  .interact-sidebar {
    order: -1;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--space-xl);
  }
}
</style>
