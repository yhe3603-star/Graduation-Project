import { describe, it, expect } from 'vitest'
import {
  GRADIENT_COLORS, COLOR_PALETTE, baseTooltip, baseGrid, baseXAxis, baseYAxis,
  createLinearGradient, createBarSeries, createMultiColorBarSeries,
  createLineSeries, createPieSeries, createRadarSeries
} from '@/utils/chartConfig'

describe('constants', () => {
  it('GRADIENT_COLORS should have 7 entries', () => {
    expect(Object.keys(GRADIENT_COLORS)).toHaveLength(7)
  })

  it('COLOR_PALETTE should have 8 colors', () => {
    expect(COLOR_PALETTE).toHaveLength(8)
  })

  it('baseTooltip should be an object', () => {
    expect(baseTooltip).toHaveProperty('backgroundColor')
    expect(baseTooltip).toHaveProperty('borderColor')
    expect(baseTooltip).toHaveProperty('textStyle')
  })

  it('baseGrid should have containLabel', () => {
    expect(baseGrid.containLabel).toBe(true)
  })

  it('baseXAxis should be category type', () => {
    expect(baseXAxis.type).toBe('category')
    expect(baseXAxis.axisTick.show).toBe(false)
  })

  it('baseYAxis should be value type with dashed split line', () => {
    expect(baseYAxis.type).toBe('value')
    expect(baseYAxis.splitLine.lineStyle.type).toBe('dashed')
  })
})

describe('createLinearGradient', () => {
  it('should return gradient config', () => {
    const g = createLinearGradient('#111', '#222')
    expect(g.type).toBe('linear')
    expect(g.x).toBe(0)
    expect(g.y).toBe(0)
    expect(g.x2).toBe(0)
    expect(g.y2).toBe(1)
    expect(g.colorStops).toHaveLength(2)
    expect(g.colorStops[0].color).toBe('#111')
    expect(g.colorStops[1].color).toBe('#222')
  })
})

describe('createBarSeries', () => {
  it('should create bar series config', () => {
    const s = createBarSeries([10, 20], GRADIENT_COLORS.blue)
    expect(s.type).toBe('bar')
    expect(s.barWidth).toBe('45%')
    expect(s.data).toHaveLength(2)
    expect(s.data[0].itemStyle.borderRadius).toEqual([6, 6, 0, 0])
  })
})

describe('createMultiColorBarSeries', () => {
  it('should use color palette', () => {
    const s = createMultiColorBarSeries([{ value: 10 }, { value: 20 }])
    expect(s.type).toBe('bar')
    expect(s.data).toHaveLength(2)
  })

  it('should handle plain numbers', () => {
    const s = createMultiColorBarSeries([5, 15, 25])
    expect(s.data).toHaveLength(3)
    expect(s.data[0].value).toBe(5)
  })
})

describe('createLineSeries', () => {
  it('should create line series with area', () => {
    const s = createLineSeries([1, 2, 3], GRADIENT_COLORS.blue)
    expect(s.type).toBe('line')
    expect(s.smooth).toBe(true)
    expect(s.areaStyle).toBeDefined()
    expect(s.lineStyle.color).toBe(GRADIENT_COLORS.blue.start)
  })

  it('should create line series without area', () => {
    const s = createLineSeries([1, 2, 3], GRADIENT_COLORS.green, false)
    expect(s.areaStyle).toBeUndefined()
  })
})

describe('createPieSeries', () => {
  it('should create pie config', () => {
    const data = [{ name: 'A', value: 10 }, { name: 'B', value: 20 }]
    const s = createPieSeries(data)
    expect(s.type).toBe('pie')
    expect(s.radius).toEqual(['45%', '72%'])
    expect(s.data).toEqual(data)
    expect(s.animationType).toBe('scale')
  })
})

describe('createRadarSeries', () => {
  it('should create radar config', () => {
    const s = createRadarSeries([80, 70, 90], GRADIENT_COLORS.green)
    expect(s.type).toBe('radar')
    expect(s.data[0].value).toEqual([80, 70, 90])
    expect(s.data[0].name).toBe('热度')
    expect(s.data[0].lineStyle.color).toBe(GRADIENT_COLORS.green.start)
  })
})
