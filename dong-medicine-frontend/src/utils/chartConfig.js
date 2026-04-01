export const GRADIENT_COLORS = {
  blue: { start: '#1A5276', end: '#5DADE2' },
  green: { start: '#1E8449', end: '#58D68D' },
  gold: { start: '#B7950B', end: '#F4D03F' },
  purple: { start: '#6C3483', end: '#BB8FCE' },
  red: { start: '#922B21', end: '#EC7063' },
  teal: { start: '#0E6655', end: '#48C9B0' },
  orange: { start: '#CA6F1E', end: '#F5B041' }
};

export const COLOR_PALETTE = ['#1A5276', '#28B463', '#D4AC0D', '#8E44AD', '#E74C3C', '#16A085', '#2980B9', '#CA6F1E'];

export const baseTooltip = {
  backgroundColor: 'rgba(255, 255, 255, 0.95)',
  borderColor: '#e0e0e0',
  borderWidth: 1,
  textStyle: { color: '#333' }
};

export const baseGrid = { left: '3%', right: '4%', bottom: '8%', top: '8%', containLabel: true };

export const baseXAxis = {
  type: 'category',
  axisLabel: { color: '#666' },
  axisLine: { lineStyle: { color: '#ddd' } },
  axisTick: { show: false }
};

export const baseYAxis = {
  type: 'value',
  axisLabel: { color: '#999' },
  axisLine: { show: false },
  splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
};

export const createLinearGradient = (startColor, endColor) => ({
  type: 'linear',
  x: 0, y: 0, x2: 0, y2: 1,
  colorStops: [
    { offset: 0, color: startColor },
    { offset: 1, color: endColor }
  ]
});

export const createBarSeries = (data, gradient, barWidth = '45%') => ({
  type: 'bar',
  data: data.map(v => ({
    value: v,
    itemStyle: {
      color: createLinearGradient(gradient.start, gradient.end),
      borderRadius: [6, 6, 0, 0]
    }
  })),
  barWidth,
  animationDuration: 1500,
  animationEasing: 'cubicOut'
});

export const createMultiColorBarSeries = (data, barWidth = '50%') => ({
  type: 'bar',
  data: data.map((d, i) => ({
    value: d.value ?? d,
    itemStyle: {
      color: createLinearGradient(COLOR_PALETTE[i % COLOR_PALETTE.length], COLOR_PALETTE[i % COLOR_PALETTE.length] + '99'),
      borderRadius: [6, 6, 0, 0]
    }
  })),
  barWidth,
  animationDuration: 1500,
  animationEasing: 'cubicOut'
});

export const createLineSeries = (data, gradient, withArea = true) => ({
  type: 'line',
  data,
  smooth: true,
  symbol: 'circle',
  symbolSize: 8,
  lineStyle: { color: gradient.start, width: 3 },
  itemStyle: { color: gradient.start, borderWidth: 2, borderColor: '#fff' },
  areaStyle: withArea ? {
    color: createLinearGradient(gradient.start + '66', gradient.start + '05')
  } : undefined,
  animationDuration: 2000,
  animationEasing: 'cubicOut'
});

export const createPieSeries = (data) => ({
  type: 'pie',
  radius: ['45%', '72%'],
  center: ['50%', '45%'],
  avoidLabelOverlap: true,
  itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
  label: { show: true, position: 'outside', formatter: '{b}\n{d}%', fontSize: 11, color: '#666' },
  labelLine: { length: 15, length2: 10 },
  emphasis: {
    label: { show: true, fontWeight: 'bold' },
    itemStyle: { shadowBlur: 20, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.2)' }
  },
  data,
  animationType: 'scale',
  animationEasing: 'elasticOut',
  animationDuration: 1500
});

export const createRadarSeries = (values, gradient) => ({
  type: 'radar',
  data: [{
    value: values,
    name: '热度',
    symbol: 'circle',
    symbolSize: 6,
    lineStyle: { color: gradient.start, width: 2 },
    areaStyle: {
      color: {
        type: 'radial',
        x: 0.5, y: 0.5, r: 0.5,
        colorStops: [
          { offset: 0, color: gradient.start + '99' },
          { offset: 1, color: gradient.start + '1a' }
        ]
      }
    },
    itemStyle: { color: gradient.start }
  }],
  animationDuration: 1500
});
