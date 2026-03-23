import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useCountdown } from './useInteraction'
import { logFetchError } from '@/utils'

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

  const { formattedTime, isRunning, isExpired, isLowTime, start: startTimer, stop: stopTimer, reset: resetTimer } = useCountdown(3)

  watch(isExpired, (expired) => {
    if (expired && isQuizStarted.value && !quizFinished.value) {
      autoSubmit()
    }
  })

  const autoSubmit = async () => {
    ElMessage.warning('时间到！系统自动提交答卷')
    await submitQuiz(true)
  }

  const startNewQuiz = async () => {
    quizLoading.value = true
    try {
      const res = await request.get('/quiz/questions')
      selectedQuestions.value = res?.data?.data || res?.data || []
      userAnswers.value = new Array(selectedQuestions.value.length).fill('')
      currentQuestion.value = 0
      isQuizStarted.value = true
      quizFinished.value = false
      resetTimer(3)
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
      const unanswered = userAnswers.value.filter(a => !a).length
      if (unanswered > 0) {
        ElMessage.warning(`还有 ${unanswered} 题未作答，未作答题目将按答错处理`)
      }
    }
    stopTimer()
    submitting.value = true
    try {
      const answers = selectedQuestions.value.map((q, i) => ({ questionId: q.id, answer: userAnswers.value[i] || '' }))
      const res = await request.post('/quiz/submit', { answers })
      const data = res?.data?.data || res?.data || {}
      finalScore.value = data.score || 0
      correctCount.value = data.correct || 0
      quizFinished.value = true
      isQuizStarted.value = false
      if (isLoggedIn.value) {
        try {
          const quizRes = await request.get('/quiz/records')
          quizRecords.value = quizRes?.data?.data || quizRes?.data || []
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
      quizRecords.value = res?.data?.data || res?.data || []
    } catch (e) {
      console.debug('加载答题记录失败:', e)
    }
  }

  const bestScore = computed(() => {
    const scores = quizRecords.value.map(r => r.score || 0)
    return scores.length ? Math.max(...scores) : 0
  })

  return {
    isQuizStarted, selectedQuestions, userAnswers, currentQuestion, quizFinished, finalScore, correctCount, quizLoading, submitting, quizRecords,
    formattedTime, isRunning, isExpired, isLowTime,
    startNewQuiz, resetQuiz, nextQuestion, prevQuestion, submitQuiz, shareQuizResult, loadQuizRecords, bestScore,
  }
}
