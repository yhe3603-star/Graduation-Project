import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useCountdown } from './useInteraction'
import { logOperationWarn, extractData } from '@/utils'

function fisherYatesShuffle(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

export const usePlantGame = (request, isLoggedIn) => {
  const difficulty = ref('easy')
  const plantsForGame = ref([])
  const currentPlant = ref(null)
  const options = ref([])
  const answered = ref(false)
  const selectedAnswer = ref('')
  const gameScore = ref(0)
  const streak = ref(0)
  const totalQuestions = ref(0)
  const correctAnswers = ref(0)
  const gameFinished = ref(false)
  const gameStarted = ref(false)
  const submittingGame = ref(false)
  const gameRecords = ref([])
  const plantsLoaded = ref(false)
  let nextPlantTimer = null

  const { formattedTime, isRunning, isExpired, isLowTime, start: startTimer, stop: stopTimer, reset: resetTimer } = useCountdown(3)

  watch(isExpired, (expired) => {
    if (expired && currentPlant.value && !gameFinished.value) {
      autoEndGame()
    }
  })

  const autoEndGame = async () => {
    ElMessage.warning('时间到！游戏结束')
    await submitGameScore()
  }

  const SCORE_CONFIG = {
    easy: 10,
    medium: 15,
    hard: 20
  }

  const OPTION_COUNT = {
    easy: 3,
    medium: 4,
    hard: 5
  }

  const loadPlants = async () => {
    try {
      const res = await request.get('/plants/random?limit=50')
      const raw = res?.data?.data || res?.data || []
      plantsForGame.value = Array.isArray(raw) ? raw : []
      plantsLoaded.value = plantsForGame.value.length > 0
    } catch {
      ElMessage.error('加载植物数据失败')
      plantsLoaded.value = false
    }
  }

  const selectDifficulty = (level) => {
    difficulty.value = level
  }

  const startGame = () => {
    if (plantsForGame.value.length === 0) {
      ElMessage.warning('暂无植物数据，请稍后再试')
      return
    }
    gameStarted.value = true
    gameScore.value = 0
    streak.value = 0
    totalQuestions.value = 0
    correctAnswers.value = 0
    gameFinished.value = false
    answered.value = false
    selectedAnswer.value = ''
    resetTimer(3)
    startTimer()
    showNextPlant()
  }

  const showNextPlant = () => {
    if (plantsForGame.value.length === 0) return
    const randomIndex = Math.floor(Math.random() * plantsForGame.value.length)
    currentPlant.value = plantsForGame.value[randomIndex]
    generateOptions()
    answered.value = false
    selectedAnswer.value = ''
  }

  const generateOptions = () => {
    if (!currentPlant.value) return
    const correctName = currentPlant.value.nameCn
    const otherPlants = plantsForGame.value.filter(p => p.nameCn !== correctName)
    const optionCount = OPTION_COUNT[difficulty.value] || 3
    const shuffled = [...otherPlants].sort(() => Math.random() - 0.5).slice(0, optionCount - 1)
    const allOptions = [correctName, ...shuffled.map(p => p.nameCn)]
    options.value = fisherYatesShuffle(allOptions)
  }

  const checkAnswer = (answer) => {
    if (answered.value) return
    answered.value = true
    selectedAnswer.value = answer
    totalQuestions.value++
    const isCorrect = answer === currentPlant.value.nameCn
    if (isCorrect) {
      correctAnswers.value++
      streak.value++
      const baseScore = SCORE_CONFIG[difficulty.value] || 10
      const bonus = Math.min(streak.value - 1, 5) * 2
      gameScore.value += baseScore + bonus
      nextPlantTimer = setTimeout(showNextPlant, 1000)
    } else {
      streak.value = 0
      nextPlantTimer = setTimeout(showNextPlant, 1500)
    }
  }

  const submitGameScore = async () => {
    stopTimer()
    submittingGame.value = true
    gameFinished.value = true
    currentPlant.value = null
    try {
      if (isLoggedIn.value && gameScore.value > 0) {
        await request.post('/plant-game/submit', {
          score: gameScore.value,
          correctCount: correctAnswers.value,
          totalCount: totalQuestions.value,
          difficulty: difficulty.value
        })
        await loadGameRecords()
      }
    } catch {
      logOperationWarn('保存游戏记录')
    } finally {
      submittingGame.value = false
    }
  }

  const resetGame = () => {
    stopTimer()
    if (nextPlantTimer) {
      clearTimeout(nextPlantTimer)
      nextPlantTimer = null
    }
    difficulty.value = 'easy'
    currentPlant.value = null
    options.value = []
    gameScore.value = 0
    streak.value = 0
    totalQuestions.value = 0
    correctAnswers.value = 0
    gameFinished.value = false
    gameStarted.value = false
    answered.value = false
    selectedAnswer.value = ''
  }

  const favoriteCurrentPlant = async () => {
    if (!currentPlant.value || !isLoggedIn.value) return
    try {
      await request.post('/favorites', { targetType: 'plant', targetId: currentPlant.value.id })
      ElMessage.success('收藏成功')
    } catch {
      ElMessage.error('收藏失败')
    }
  }

  const loadGameRecords = async () => {
    if (!isLoggedIn.value) {
      gameRecords.value = []
      return
    }
    try {
      const res = await request.get('/plant-game/records')
      gameRecords.value = extractData(res)
    } catch (e) {
      console.debug('加载游戏记录失败:', e)
    }
  }

  const totalGameScore = computed(() => {
    const historical = Array.isArray(gameRecords.value) ? gameRecords.value.reduce((sum, r) => sum + (r.score || 0), 0) : 0
    return historical + (gameFinished.value ? gameScore.value : 0)
  })

  const gameCount = computed(() => {
    const historical = Array.isArray(gameRecords.value) ? gameRecords.value.length : 0
    return historical + (gameFinished.value ? 1 : 0)
  })

  onUnmounted(() => {
    stopTimer()
    if (nextPlantTimer) {
      clearTimeout(nextPlantTimer)
      nextPlantTimer = null
    }
  })

  return {
    difficulty, currentPlant, options, answered, selectedAnswer, gameScore, streak, totalQuestions, correctAnswers, gameFinished, gameStarted, submittingGame, gameRecords, plantsLoaded,
    formattedTime, isRunning, isExpired, isLowTime,
    loadPlants, selectDifficulty, startGame, checkAnswer, resetGame, submitGameScore, favoriteCurrentPlant, loadGameRecords, totalGameScore, gameCount,
  }
}
