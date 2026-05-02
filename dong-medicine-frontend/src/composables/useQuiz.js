import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useCountdown } from './useInteraction'
import { logFetchError, extractData } from '@/utils'

export const useQuiz = (request, isLoggedIn) => {
  const isQuizStarted = ref(false)
  const selectedQuestions = ref([])
  const userAnswers = ref([])
  const currentQuestion = ref(0)
  const quizFinished = ref(false)
  const finalScore = ref(0)
  const correctCount = ref(0)
  const quizLoading = ref(false)
  const submitting = ref(false)
  const quizRecords = ref([])
  const selectedDifficulty = ref('easy')

  const difficultyConfig = {
    easy: { label: '初级', icon: '📝', count: 10, scorePerQuestion: 10, time: 5 },
    medium: { label: '中级', icon: '💡', count: 20, scorePerQuestion: 15, time: 5 },
    hard: { label: '高级', icon: '🏆', count: 30, scorePerQuestion: 20, time: 5 }
  }

  const { formattedTime, isRunning, isExpired, isLowTime, start: startTimer, stop: stopTimer, reset: resetTimer } = useCountdown(5)

  watch(isExpired, (expired) => {
    if (expired && isQuizStarted.value && !quizFinished.value) {
      autoSubmit()
    }
  })

  const autoSubmit = async () => {
    ElMessage.warning('时间到！系统自动提交答卷')
    await submitQuiz(true)
  }

  const setDifficulty = (level) => {
    selectedDifficulty.value = level
  }

  const startNewQuiz = async () => {
    quizLoading.value = true
    try {
      const config = difficultyConfig[selectedDifficulty.value]
      const res = await request.get('/quiz/questions', { 
        params: { 
          count: config.count,
          scorePerQuestion: config.scorePerQuestion
        } 
      })
      const raw = res?.data?.data || res?.data || []
      selectedQuestions.value = Array.isArray(raw) ? raw : []
      if (selectedQuestions.value.length === 0) {
        ElMessage.warning('题库为空，暂无可用题目')
        return
      }
      userAnswers.value = new Array(selectedQuestions.value.length).fill('')
      currentQuestion.value = 0
      isQuizStarted.value = true
      quizFinished.value = false
      resetTimer(config.time)
      startTimer()
    } catch (e) {
      logFetchError('答题题目', e)
      ElMessage.error('加载题目失败')
    } finally {
      quizLoading.value = false
    }
  }

  const resetQuiz = () => {
    stopTimer()
    isQuizStarted.value = false
    quizFinished.value = false
    finalScore.value = 0
    correctCount.value = 0
    userAnswers.value = []
    currentQuestion.value = 0
  }

  const nextQuestion = () => { if (currentQuestion.value < selectedQuestions.value.length - 1) currentQuestion.value++ }
  const prevQuestion = () => { if (currentQuestion.value > 0) currentQuestion.value-- }

  const submitQuiz = async (isAutoSubmit = false) => {
    if (!isAutoSubmit) {
      const answered = userAnswers.value.filter(a => a).length
      const total = selectedQuestions.value.length
      if (answered === 0) {
        ElMessage.warning('你还没有作答任何题目')
        return
      }
    }
    stopTimer()
    submitting.value = true
    try {
      const config = difficultyConfig[selectedDifficulty.value]
      const answers = selectedQuestions.value.map((q, i) => ({ questionId: q.id, answer: userAnswers.value[i] || '' }))
      const res = await request.post('/quiz/submit', { answers }, { 
        params: { 
          scorePerQuestion: config.scorePerQuestion
        } 
      })
      const data = res?.data?.data || res?.data || {}
      finalScore.value = data.score || 0
      correctCount.value = data.correct || 0
      quizFinished.value = true
      isQuizStarted.value = false
      if (isLoggedIn.value) {
        try {
          const quizRes = await request.get('/quiz/records')
          quizRecords.value = extractData(quizRes)
        } catch (e) {
          console.debug('加载答题记录失败:', e)
        }
      }
    } catch (e) {
      logFetchError('提交答卷', e)
      ElMessage.error('提交失败，请重试')
    } finally {
      submitting.value = false
    }
  }

  const shareQuizResult = () => {
    const text = `我在侗乡医药答题中获得了 ${finalScore.value} 分！正确 ${correctCount.value}/${selectedQuestions.value.length} 题`
    if (navigator.share) {
      navigator.share({ title: '侗医药答题成绩', text })
    } else {
      navigator.clipboard.writeText(text)
      ElMessage.success('成绩已复制到剪贴板')
    }
  }

  const loadQuizRecords = async () => {
    try {
      const res = await request.get('/quiz/records')
      quizRecords.value = extractData(res)
    } catch (e) {
      console.debug('加载答题记录失败:', e)
    }
  }

  const bestScore = computed(() => {
    const historicalScores = Array.isArray(quizRecords.value) ? quizRecords.value.map(r => r.score || 0) : []
    const currentScore = finalScore.value || 0
    const allScores = [...historicalScores, currentScore]
    return allScores.length ? Math.max(...allScores) : 0
  })

  const quizCount = computed(() => {
    const historical = Array.isArray(quizRecords.value) ? quizRecords.value.length : 0
    return historical + (quizFinished.value ? 1 : 0)
  })

  return {
    isQuizStarted, selectedQuestions, userAnswers, currentQuestion, quizFinished, finalScore, correctCount, quizLoading, submitting, quizRecords, selectedDifficulty, difficultyConfig,
    formattedTime, isRunning, isExpired, isLowTime,
    setDifficulty, startNewQuiz, resetQuiz, nextQuestion, prevQuestion, submitQuiz, shareQuizResult, loadQuizRecords, bestScore, quizCount,
  }
}
