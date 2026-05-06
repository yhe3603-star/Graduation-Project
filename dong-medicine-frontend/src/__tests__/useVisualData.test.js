import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils', () => ({
  logFetchError: vi.fn()
}))

vi.mock('@/utils/chartConfig', () => ({
  COLOR_PALETTE: ['#1A5276', '#28B463', '#D4AC0D', '#8E44AD', '#E74C3C', '#16A085', '#2980B9', '#CA6F1E']
}))

import { logFetchError } from '@/utils'
import { useVisualData } from '@/composables/useVisualData'

describe('useVisualData', () => {
  let mockRequest

  beforeEach(() => {
    vi.clearAllMocks()
    mockRequest = { get: vi.fn() }
  })

  describe('fetchData - loading state', () => {
    it('should set loading to true during fetch and false after', async () => {
      let resolveRequest
      mockRequest.get.mockReturnValue(new Promise(r => { resolveRequest = r }))

      const { loading, fetchData } = useVisualData(mockRequest)
      expect(loading.value).toBe(false)

      const promise = fetchData()
      expect(loading.value).toBe(true)

      resolveRequest({ data: {} })
      await promise
      expect(loading.value).toBe(false)
    })

    it('should set loading to false even on error', async () => {
      mockRequest.get.mockRejectedValue(new Error('network'))

      const { loading, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(loading.value).toBe(false)
    })
  })

  describe('fetchData - stats', () => {
    it('should populate stats from data.counts', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          counts: { plants: 10, knowledge: 20, inheritors: 5, qa: 15, resources: 8 }
        }
      })

      const { stats, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(stats.value).toEqual({
        plants: 10, knowledge: 20, inheritors: 5, qa: 15, resources: 8
      })
    })

    it('should default missing count fields to 0', async () => {
      mockRequest.get.mockResolvedValue({
        data: { counts: { plants: 10 } }
      })

      const { stats, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(stats.value).toEqual({
        plants: 10, knowledge: 0, inheritors: 0, qa: 0, resources: 0
      })
    })

    it('should keep default stats when counts is absent', async () => {
      mockRequest.get.mockResolvedValue({ data: {} })

      const { stats, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(stats.value).toEqual({
        plants: 0, knowledge: 0, inheritors: 0, qa: 0, resources: 0
      })
    })
  })

  describe('fetchData - therapyCategories', () => {
    it('should map therapyCategories with COLOR_PALETTE colors', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          therapyCategories: [
            { name: '针灸', value: 30 },
            { name: '推拿', value: 20 },
            { name: '药浴', value: 10 }
          ]
        }
      })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.therapyCategories).toHaveLength(3)
      expect(chartData.value.therapyCategories[0]).toEqual({
        name: '针灸',
        value: 30,
        itemStyle: { color: '#1A5276' }
      })
      expect(chartData.value.therapyCategories[1]).toEqual({
        name: '推拿',
        value: 20,
        itemStyle: { color: '#28B463' }
      })
      expect(chartData.value.therapyCategories[2]).toEqual({
        name: '药浴',
        value: 10,
        itemStyle: { color: '#D4AC0D' }
      })
    })
  })

  describe('fetchData - inheritorLevels', () => {
    it('should set inheritorLevels directly', async () => {
      const levels = [{ name: '国家级', value: 3 }, { name: '省级', value: 5 }]
      mockRequest.get.mockResolvedValue({ data: { inheritorLevels: levels } })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.inheritorLevels).toEqual(levels)
    })
  })

  describe('fetchData - plantCategories and plantCategoryNames', () => {
    it('should set plantCategories and plantCategoryNames', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          plantCategories: [10, 20, 30],
          plantCategoryNames: ['草药', '木本', '菌类']
        }
      })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.plantCategories).toEqual([10, 20, 30])
      expect(chartData.value.plantCategoryNames).toEqual(['草药', '木本', '菌类'])
    })
  })

  describe('fetchData - qaCategories and qaCategoryNames', () => {
    it('should set qaCategories and qaCategoryNames', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          qaCategories: [5, 10],
          qaCategoryNames: ['治疗', '养生']
        }
      })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.qaCategories).toEqual([5, 10])
      expect(chartData.value.qaCategoryNames).toEqual(['治疗', '养生'])
    })
  })

  describe('fetchData - plantDistribution', () => {
    it('should populate plantDistribution and extract unique region names', async () => {
      const distribution = [
        { name: '贵州', value: 10 },
        { name: '湖南', value: 8 },
        { name: '贵州', value: 5 },
        { name: '广西', value: 3 }
      ]
      mockRequest.get.mockResolvedValue({ data: { plantDistribution: distribution } })

      const { chartData, regionList, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.plantDistribution).toEqual(distribution)
      expect(regionList.value).toEqual(['贵州', '湖南', '广西'])
    })
  })

  describe('fetchData - knowledgePopularity', () => {
    it('should truncate long names to 12 chars with ellipsis', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          knowledgePopularity: [
            { name: '这是一个超过十二个字符的很长很长的知识标题', value: 100 },
            { name: '短标题', value: 50 }
          ]
        }
      })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.knowledgePopularity[0].name).toBe('这是一个超过十二个字符的...')
      expect(chartData.value.knowledgePopularity[0].value).toBe(100)
      expect(chartData.value.knowledgePopularity[1].name).toBe('短标题')
      expect(chartData.value.knowledgePopularity[1].value).toBe(50)
    })

    it('should not truncate names with exactly 12 chars', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          knowledgePopularity: [{ name: '一二三四五六七八九十十一', value: 10 }]
        }
      })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.knowledgePopularity[0].name).toBe('一二三四五六七八九十十一')
    })
  })

  describe('fetchData - formulaFreq and formulaNames', () => {
    it('should set formulaFreq and formulaNames', async () => {
      mockRequest.get.mockResolvedValue({
        data: {
          formulaFreq: [3, 5, 2],
          formulaNames: ['方剂A', '方剂B', '方剂C']
        }
      })

      const { chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(chartData.value.formulaFreq).toEqual([3, 5, 2])
      expect(chartData.value.formulaNames).toEqual(['方剂A', '方剂B', '方剂C'])
    })
  })

  describe('fetchData - error handling', () => {
    it('should log fetch error on failure', async () => {
      const error = new Error('server error')
      mockRequest.get.mockRejectedValue(error)

      const { fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(logFetchError).toHaveBeenCalledWith('可视化数据', error)
    })
  })

  describe('fetchData - response structure variations', () => {
    it('should handle res.data being absent (use res directly)', async () => {
      const chartResponse = { counts: { plants: 5, knowledge: 0, inheritors: 0, qa: 0, resources: 0 } }
      mockRequest.get.mockResolvedValue(chartResponse)

      const { stats, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(stats.value.plants).toBe(5)
    })

    it('should handle empty response', async () => {
      mockRequest.get.mockResolvedValue({})

      const { stats, chartData, fetchData } = useVisualData(mockRequest)
      await fetchData()
      expect(stats.value).toEqual({ plants: 0, knowledge: 0, inheritors: 0, qa: 0, resources: 0 })
      expect(chartData.value.therapyCategories).toEqual([])
    })
  })
})
