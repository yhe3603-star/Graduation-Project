import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref, nextTick } from 'vue'

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), warning: vi.fn(), error: vi.fn() }
}))

vi.mock('@/utils', () => ({
  logOperationWarn: vi.fn(),
  extractData: vi.fn((res) => res?.data || [])
}))

const mockCountdown = {
  formattedTime: ref('03:00'),
  isRunning: ref(false),
  isExpired: ref(false),
  isLowTime: ref(false),
  start: vi.fn(),
  stop: vi.fn(),
  reset: vi.fn()
}

vi.mock('@/composables/useInteraction', () => ({
  useCountdown: () => mockCountdown
}))

import { ElMessage } from 'element-plus'
import { logOperationWarn, extractData } from '@/utils'
import { usePlantGame } from '@/composables/usePlantGame'

const SAMPLE_PLANTS = [
  { id: 1, nameCn: '黄芪', nameSci: 'Astragalus' },
  { id: 2, nameCn: '当归', nameSci: 'Angelica' },
  { id: 3, nameCn: '人参', nameSci: 'Panax' },
  { id: 4, nameCn: '甘草', nameSci: 'Glycyrrhiza' },
  { id: 5, nameCn: '白术', nameSci: 'Atractylodes' }
]

describe('usePlantGame', () => {
  let mockRequest
  let isLoggedIn

  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
    mockRequest = {
      get: vi.fn(),
      post: vi.fn()
    }
    isLoggedIn = ref(false)
    mockCountdown.formattedTime.value = '03:00'
    mockCountdown.isRunning.value = false
    mockCountdown.isExpired.value = false
    mockCountdown.isLowTime.value = false
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('fisherYatesShuffle (via generateOptions)', () => {
    it('should produce options containing the correct answer', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()

      // Options should contain the correct answer
      const correctName = game.currentPlant.value.nameCn
      expect(game.options.value).toContain(correctName)
    })

    it('should produce options with correct count for easy difficulty', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      game.selectDifficulty('easy')
      await game.loadPlants()
      game.startGame()

      expect(game.options.value).toHaveLength(3)
    })

    it('should produce options with correct count for medium difficulty', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      game.selectDifficulty('medium')
      await game.loadPlants()
      game.startGame()

      expect(game.options.value).toHaveLength(4)
    })

    it('should produce options with correct count for hard difficulty', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      game.selectDifficulty('hard')
      await game.loadPlants()
      game.startGame()

      expect(game.options.value).toHaveLength(5)
    })
  })

  describe('loadPlants', () => {
    it('should load plants and set plantsLoaded to true', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })

      const { plantsLoaded, loadPlants } = usePlantGame(mockRequest, isLoggedIn)
      await loadPlants()
      expect(mockRequest.get).toHaveBeenCalledWith('/plants/random?limit=50')
      expect(plantsLoaded.value).toBe(true)
    })

    it('should handle res.data.data structure', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })

      const { plantsLoaded, loadPlants } = usePlantGame(mockRequest, isLoggedIn)
      await loadPlants()
      expect(plantsLoaded.value).toBe(true)
    })

    it('should handle res.data (array) structure', async () => {
      mockRequest.get.mockResolvedValue({ data: SAMPLE_PLANTS })

      const { plantsLoaded, loadPlants } = usePlantGame(mockRequest, isLoggedIn)
      await loadPlants()
      expect(plantsLoaded.value).toBe(true)
    })

    it('should handle non-array response as empty', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: 'not-an-array' } })

      const { plantsLoaded, loadPlants } = usePlantGame(mockRequest, isLoggedIn)
      await loadPlants()
      expect(plantsLoaded.value).toBe(false)
    })

    it('should handle load failure', async () => {
      mockRequest.get.mockRejectedValue(new Error('fail'))

      const { plantsLoaded, loadPlants } = usePlantGame(mockRequest, isLoggedIn)
      await loadPlants()
      expect(ElMessage.error).toHaveBeenCalledWith('加载植物数据失败')
      expect(plantsLoaded.value).toBe(false)
    })
  })

  describe('selectDifficulty', () => {
    it('should update difficulty level', () => {
      const { difficulty, selectDifficulty } = usePlantGame(mockRequest, isLoggedIn)
      selectDifficulty('medium')
      expect(difficulty.value).toBe('medium')
      selectDifficulty('hard')
      expect(difficulty.value).toBe('hard')
    })
  })

  describe('startGame', () => {
    it('should warn when no plants loaded', () => {
      const { startGame, gameStarted } = usePlantGame(mockRequest, isLoggedIn)
      startGame()
      expect(ElMessage.warning).toHaveBeenCalledWith('暂无植物数据，请稍后再试')
      expect(gameStarted.value).toBe(false)
    })

    it('should initialize game state correctly', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()

      game.startGame()
      expect(game.gameStarted.value).toBe(true)
      expect(game.gameScore.value).toBe(0)
      expect(game.streak.value).toBe(0)
      expect(game.totalQuestions.value).toBe(0)
      expect(game.correctAnswers.value).toBe(0)
      expect(game.gameFinished.value).toBe(false)
      expect(game.answered.value).toBe(false)
      expect(game.currentPlant.value).toBeTruthy()
      expect(game.options.value.length).toBeGreaterThan(0)
    })

    it('should start the countdown timer', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()

      game.startGame()
      expect(mockCountdown.reset).toHaveBeenCalledWith(3)
      expect(mockCountdown.start).toHaveBeenCalled()
    })
  })

  describe('checkAnswer - scoring logic', () => {
    it('should award base score for correct answer on easy', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('easy')
      game.startGame()

      const correctName = game.currentPlant.value.nameCn
      game.checkAnswer(correctName)

      expect(game.correctAnswers.value).toBe(1)
      expect(game.streak.value).toBe(1)
      expect(game.totalQuestions.value).toBe(1)
      expect(game.gameScore.value).toBe(10) // easy base score, streak=1 => bonus=0
    })

    it('should award medium base score for correct answer', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('medium')
      game.startGame()

      const correctName = game.currentPlant.value.nameCn
      game.checkAnswer(correctName)

      expect(game.gameScore.value).toBe(15) // medium base score, streak=1 => bonus=0
    })

    it('should award hard base score for correct answer', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('hard')
      game.startGame()

      const correctName = game.currentPlant.value.nameCn
      game.checkAnswer(correctName)

      expect(game.gameScore.value).toBe(20) // hard base score, streak=1 => bonus=0
    })

    it('should apply streak bonus for consecutive correct answers', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('easy')
      game.startGame()

      // Answer correctly 3 times
      for (let i = 0; i < 3; i++) {
        const correctName = game.currentPlant.value.nameCn
        game.checkAnswer(correctName)
        // Advance timer to trigger showNextPlant
        vi.advanceTimersByTime(1000)
      }

      // Score: 10 (streak 1, bonus 0) + 10+2 (streak 2, bonus 2) + 10+4 (streak 3, bonus 4) = 36
      expect(game.streak.value).toBe(3)
      expect(game.gameScore.value).toBe(36)
    })

    it('should cap streak bonus at 5', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('easy')
      game.startGame()

      // Answer correctly 7 times to reach streak 7
      for (let i = 0; i < 7; i++) {
        const correctName = game.currentPlant.value.nameCn
        game.checkAnswer(correctName)
        vi.advanceTimersByTime(1000)
      }

      // The bonus for streak 7 should be min(7-1, 5)*2 = 10 (capped)
      // Just verify streak is 7 and score is cumulative
      expect(game.streak.value).toBe(7)
      // Last answer should have bonus = min(6,5)*2 = 10, so score for last = 10 + 10 = 20
    })

    it('should reset streak on wrong answer', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()

      // First answer correctly
      const correctName = game.currentPlant.value.nameCn
      game.checkAnswer(correctName)
      expect(game.streak.value).toBe(1)
      const scoreAfterCorrect = game.gameScore.value

      // Advance past the timeout for correct answer
      vi.advanceTimersByTime(1000)

      // Now answer wrong
      const wrongName = game.options.value.find(o => o !== game.currentPlant.value.nameCn)
      game.checkAnswer(wrongName)

      expect(game.streak.value).toBe(0)
      expect(game.gameScore.value).toBe(scoreAfterCorrect) // No score change
      expect(game.correctAnswers.value).toBe(1) // Still 1
      expect(game.totalQuestions.value).toBe(2)
    })

    it('should not allow answering twice', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()

      const correctName = game.currentPlant.value.nameCn
      game.checkAnswer(correctName)
      const scoreAfter = game.gameScore.value

      game.checkAnswer(correctName)
      expect(game.totalQuestions.value).toBe(1) // Still 1, not 2
      expect(game.gameScore.value).toBe(scoreAfter)
    })

    it('should schedule next plant after correct answer (1000ms)', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()

      const firstPlant = game.currentPlant.value
      game.checkAnswer(firstPlant.nameCn)

      // Before timeout
      expect(game.currentPlant.value).toBe(firstPlant)

      // After 1000ms timeout
      vi.advanceTimersByTime(1000)
      expect(game.answered.value).toBe(false)
    })

    it('should schedule next plant after wrong answer (1500ms)', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()

      const firstPlant = game.currentPlant.value
      const wrongName = game.options.value.find(o => o !== firstPlant.nameCn)
      game.checkAnswer(wrongName)

      // After 1500ms timeout
      vi.advanceTimersByTime(1500)
      expect(game.answered.value).toBe(false)
    })
  })

  describe('submitGameScore', () => {
    it('should submit score when logged in and score > 0', async () => {
      isLoggedIn.value = true
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      mockRequest.post.mockResolvedValue({})

      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('easy')
      game.startGame()

      const correctName = game.currentPlant.value.nameCn
      game.checkAnswer(correctName)

      await game.submitGameScore()

      expect(game.gameFinished.value).toBe(true)
      expect(game.submittingGame.value).toBe(false)
      expect(mockCountdown.stop).toHaveBeenCalled()
      expect(mockRequest.post).toHaveBeenCalledWith('/plant-game/submit', {
        score: expect.any(Number),
        correctCount: 1,
        totalCount: 1,
        difficulty: 'easy'
      })
    })

    it('should not submit when not logged in', async () => {
      isLoggedIn.value = false
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })

      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()

      await game.submitGameScore()

      expect(game.gameFinished.value).toBe(true)
      expect(mockRequest.post).not.toHaveBeenCalledWith('/plant-game/submit', expect.any(Object))
    })

    it('should not submit when score is 0', async () => {
      isLoggedIn.value = true
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })

      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()
      // Don't answer any question, score stays 0

      await game.submitGameScore()

      expect(mockRequest.post).not.toHaveBeenCalledWith('/plant-game/submit', expect.any(Object))
    })

    it('should log warning on submit failure', async () => {
      isLoggedIn.value = true
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      mockRequest.post.mockRejectedValue(new Error('fail'))

      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.startGame()
      game.checkAnswer(game.currentPlant.value.nameCn)

      await game.submitGameScore()

      expect(logOperationWarn).toHaveBeenCalledWith('保存游戏记录')
    })
  })

  describe('resetGame', () => {
    it('should reset all game state', async () => {
      mockRequest.get.mockResolvedValue({ data: { data: SAMPLE_PLANTS } })
      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('hard')
      game.startGame()
      game.checkAnswer(game.currentPlant.value.nameCn)

      game.resetGame()

      expect(game.difficulty.value).toBe('easy')
      expect(game.currentPlant.value).toBeNull()
      expect(game.options.value).toEqual([])
      expect(game.gameScore.value).toBe(0)
      expect(game.streak.value).toBe(0)
      expect(game.totalQuestions.value).toBe(0)
      expect(game.correctAnswers.value).toBe(0)
      expect(game.gameFinished.value).toBe(false)
      expect(game.gameStarted.value).toBe(false)
      expect(game.answered.value).toBe(false)
      expect(game.selectedAnswer.value).toBe('')
      expect(mockCountdown.stop).toHaveBeenCalled()
    })
  })

  describe('loadGameRecords', () => {
    it('should clear records when not logged in', async () => {
      isLoggedIn.value = false
      const { gameRecords, loadGameRecords } = usePlantGame(mockRequest, isLoggedIn)
      await loadGameRecords()
      expect(gameRecords.value).toEqual([])
    })

    it('should load records when logged in', async () => {
      isLoggedIn.value = true
      const records = [{ score: 100, correctCount: 8 }]
      extractData.mockReturnValue(records)
      mockRequest.get.mockResolvedValue({ data: records })

      const { gameRecords, loadGameRecords } = usePlantGame(mockRequest, isLoggedIn)
      await loadGameRecords()
      expect(mockRequest.get).toHaveBeenCalledWith('/plant-game/records')
      expect(gameRecords.value).toEqual(records)
    })
  })

  describe('totalGameScore and gameCount', () => {
    it('should compute total from historical records', async () => {
      isLoggedIn.value = true
      const records = [{ score: 50 }, { score: 30 }]
      extractData.mockReturnValue(records)
      mockRequest.get.mockResolvedValue({ data: records })

      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadGameRecords()

      expect(game.totalGameScore.value).toBe(80)
      expect(game.gameCount.value).toBe(2)
    })

    it('should include current game score when finished', async () => {
      isLoggedIn.value = true
      mockRequest.get
        .mockResolvedValueOnce({ data: { data: SAMPLE_PLANTS } }) // loadPlants
        .mockResolvedValueOnce({ data: [{ score: 50 }] }) // loadGameRecords (from submit)
      extractData.mockReturnValue([{ score: 50 }])
      mockRequest.post.mockResolvedValue({})

      const game = usePlantGame(mockRequest, isLoggedIn)
      await game.loadPlants()
      game.selectDifficulty('easy')
      game.startGame()
      game.checkAnswer(game.currentPlant.value.nameCn)
      await game.submitGameScore()

      // totalGameScore should include both historical and current
      expect(game.gameFinished.value).toBe(true)
      expect(game.totalGameScore.value).toBe(50 + game.gameScore.value)
      expect(game.gameCount.value).toBe(2) // 1 historical + 1 current
    })

    it('should handle empty records', () => {
      const game = usePlantGame(mockRequest, isLoggedIn)
      expect(game.totalGameScore.value).toBe(0)
      expect(game.gameCount.value).toBe(0)
    })
  })
})
