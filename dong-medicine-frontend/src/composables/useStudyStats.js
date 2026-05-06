import { ref, computed } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer
])

export function useStudyStats() {
  const studyStatsRaw = ref({
    totalQuizAttempts: 0,
    averageScore: 0,
    totalGameAttempts: 0,
    totalFavorites: 0,
    totalBrowseCount: 0
  })

  const studyStats = computed(() => [
    { value: studyStatsRaw.value.totalQuizAttempts, label: '总答题次数' },
    { value: Math.round(studyStatsRaw.value.averageScore), label: '平均得分' },
    { value: studyStatsRaw.value.totalGameAttempts, label: '植物游戏次' },
    { value: studyStatsRaw.value.totalFavorites, label: '收藏总数' },
    { value: studyStatsRaw.value.totalBrowseCount, label: '浏览总数' }
  ])

  function computeStudyStats(quizRecords, gameRecords, favorites, browseHistory) {
    const quizTotal = quizRecords.length
    const gameTotal = gameRecords.length
    const totalAttempts = quizTotal + gameTotal

    let totalScore = 0
    let scoreCount = 0
    quizRecords.forEach(r => {
      if (r.score !== undefined && r.score !== null) {
        totalScore += Number(r.score)
        scoreCount++
      }
    })
    gameRecords.forEach(r => {
      if (r.score !== undefined && r.score !== null) {
        totalScore += Number(r.score)
        scoreCount++
      }
    })
    const avgScore = scoreCount > 0 ? totalScore / scoreCount : 0

    studyStatsRaw.value = {
      totalQuizAttempts: totalAttempts,
      averageScore: avgScore,
      totalGameAttempts: gameTotal,
      totalFavorites: favorites.length,
      totalBrowseCount: browseHistory.length
    }
  }

  const scoreChartRef = ref(null)
  let scoreChartInstance = null
  let scoreResizeObserver = null
  const hasScoreData = ref(false)

  function buildScoreTrendData(quizRecords, gameRecords) {
    const quizPoints = quizRecords.map(r => ({
      date: (r.createdAt || r.createTime) ? new Date(r.createdAt || r.createTime).toISOString().slice(0, 10) : '',
      score: Number(r.score || 0),
      type: 'quiz'
    }))
    const gamePoints = gameRecords.map(r => ({
      date: (r.createdAt || r.createTime) ? new Date(r.createdAt || r.createTime).toISOString().slice(0, 10) : '',
      score: Number(r.score || 0),
      type: 'game'
    }))
    const allPoints = [...quizPoints, ...gamePoints].filter(p => p.date).sort((a, b) => a.date.localeCompare(b.date))

    if (allPoints.length === 0) {
      hasScoreData.value = false
      return { dates: [], quizScores: [], gameScores: [] }
    }

    hasScoreData.value = true
    const dateMap = new Map()
    allPoints.forEach(p => {
      if (!dateMap.has(p.date)) dateMap.set(p.date, { quizScores: [], gameScores: [] })
      const entry = dateMap.get(p.date)
      if (p.type === 'quiz') entry.quizScores.push(p.score)
      else entry.gameScores.push(p.score)
    })

    const dates = [...dateMap.keys()].sort()
    const quizScores = dates.map(d => {
      const arr = dateMap.get(d).quizScores
      return arr.length > 0 ? Math.round(arr.reduce((a, b) => a + b, 0) / arr.length) : null
    })
    const gameScores = dates.map(d => {
      const arr = dateMap.get(d).gameScores
      return arr.length > 0 ? Math.round(arr.reduce((a, b) => a + b, 0) / arr.length) : null
    })
    return { dates, quizScores, gameScores }
  }

  function initScoreChart(quizRecords, gameRecords) {
    if (!scoreChartRef.value) return
    if (scoreChartInstance) scoreChartInstance.dispose()

    scoreChartInstance = echarts.init(scoreChartRef.value)
    const { dates, quizScores, gameScores } = buildScoreTrendData(quizRecords, gameRecords)

    if (!dates.length) {
      scoreChartInstance = null
      return
    }

    scoreChartInstance.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          let result = `<b>${params[0].axisValue}</b><br/>`
          params.forEach(p => {
            if (p.value !== null) result += `${p.marker} ${p.seriesName}: ${p.value}分<br/>`
          })
          return result
        }
      },
      legend: {
        data: ['趣味答题', '植物识别'],
        bottom: 0,
        textStyle: { color: 'var(--text-secondary, #555)', fontSize: 12 }
      },
      grid: { left: '3%', right: '4%', top: '10%', bottom: '15%', containLabel: true },
      xAxis: {
        type: 'category', data: dates, boundaryGap: false,
        axisLabel: { color: 'var(--text-muted, #888)', fontSize: 10, rotate: 30 },
        axisLine: { lineStyle: { color: 'var(--border-light, #eee)' } }
      },
      yAxis: {
        type: 'value', name: '分数', min: 0, max: 100,
        axisLabel: { color: 'var(--text-muted, #888)' },
        splitLine: { lineStyle: { color: 'var(--border-light, #eee)', type: 'dashed' } }
      },
      series: [
        {
          name: '趣味答题', type: 'line', data: quizScores, smooth: true, connectNulls: true,
          symbol: 'circle', symbolSize: 6,
          lineStyle: { color: '#3498db', width: 2 }, itemStyle: { color: '#3498db' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(52, 152, 219, 0.3)' },
              { offset: 1, color: 'rgba(52, 152, 219, 0.02)' }
            ])
          }
        },
        {
          name: '植物识别', type: 'line', data: gameScores, smooth: true, connectNulls: true,
          symbol: 'diamond', symbolSize: 6,
          lineStyle: { color: '#28B463', width: 2 }, itemStyle: { color: '#28B463' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(40, 180, 99, 0.3)' },
              { offset: 1, color: 'rgba(40, 180, 99, 0.02)' }
            ])
          }
        }
      ]
    })

    if (scoreResizeObserver) scoreResizeObserver.disconnect()
    scoreResizeObserver = new ResizeObserver(() => {
      if (scoreChartInstance && !scoreChartInstance.isDisposed()) scoreChartInstance.resize()
    })
    scoreResizeObserver.observe(scoreChartRef.value)
  }

  function disposeChart() {
    if (scoreResizeObserver) { scoreResizeObserver.disconnect(); scoreResizeObserver = null }
    if (scoreChartInstance && !scoreChartInstance.isDisposed()) { scoreChartInstance.dispose(); scoreChartInstance = null }
  }

  return { studyStatsRaw, studyStats, hasScoreData, scoreChartRef, computeStudyStats, initScoreChart, disposeChart }
}
