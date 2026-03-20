import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useCountdown } from './useInteraction'
import { logOperationWarn } from '@/utils'

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
  const submittingGame = ref(false)
  const gameRecords = ref([])

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

  const loadPlants = async () => {
    try {
      const res = await request.get('/plants/list')
      plantsForGame.value = res?.data?.data || res?.data || []
    } catch {
      ElMessage.error('加载植物数据失败')
    }
  }

  const loadPlantsByDifficulty = async (level) => {
    try {
      const res = await request.get(`/plants/random?difficulty=${level}&limit=20`)
      plantsForGame.value = res?.data?.data || res?.data || []
    } catch {
      await loadPlants()
    }
  }

  const setDifficulty = async (level) => {
    difficulty.value = level
    await loadPlantsByDifficulty(level)
    if (plantsForGame.value.length > 0) {
      startNewGame()
    } else {
      ElMessage.warning('该难度下暂无植物数据')
    }
  }

  const startNewGame = () => {
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
    const optionCount = difficulty.value === 'easy' ? 2 : difficulty.value === 'medium' ? 3 : 4
    const shuffled = otherPlants.sort(() => Math.random() - 0.5).slice(0, optionCount)
    const allOptions = [correctName, ...shuffled.map(p => p.nameCn)]
    options.value = allOptions.sort(() => Math.random() - 0.5)
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
      setTimeout(showNextPlant, 1000)
    } else {
      streak.value = 0
      setTimeout(showNextPlant, 1500)
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
    difficulty.value = 'easy'
    currentPlant.value = null
    options.value = []
    gameScore.value = 0
    streak.value = 0
    totalQuestions.value = 0
    correctAnswers.value = 0
    gameFinished.value = false
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
      gameRecords.value = res?.data?.data || res?.data || []
    } catch {}
  }

  const totalGameScore = computed(() => gameRecords.value.reduce((sum, r) => sum + (r.score || 0), 0))

  return {
    difficulty, currentPlant, options, answered, selectedAnswer, gameScore, streak, totalQuestions, correctAnswers, gameFinished, submittingGame, gameRecords,
    formattedTime, isRunning, isExpired, isLowTime,
    loadPlants, setDifficulty, checkAnswer, resetGame, submitGameScore, favoriteCurrentPlant, loadGameRecords, totalGameScore,
  }
}
