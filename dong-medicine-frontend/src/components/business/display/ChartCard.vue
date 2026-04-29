<template>
  <el-card
    class="chart-card"
    shadow="never"
  >
    <template #header>
      <div class="chart-header">
        <span class="chart-title">{{ title }}</span>
        <div class="chart-actions">
          <slot name="actions" />
        </div>
      </div>
    </template>
    <div
      v-loading="loading"
      element-loading-text="加载中..."
      element-loading-background="rgba(255, 255, 255, 0.8)"
    >
      <div
        ref="chartRef"
        class="chart-container"
        :style="{ height: height + 'px' }"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue"
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart, RadarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  RadarComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  BarChart,
  LineChart,
  PieChart,
  RadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  RadarComponent,
  CanvasRenderer
])

const props = defineProps({
  title: String,
  height: { type: Number, default: 300 },
  option: { type: Object, required: true },
  loading: { type: Boolean, default: false }
})

const chartRef = ref(null)
let chart = null

const initChart = () => {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  chart.setOption(props.option)
}

const resizeChart = () => chart?.resize()

watch(() => props.option, (newOpt) => {
  if (chart) chart.setOption(newOpt, true)
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener("resize", resizeChart)
})

onUnmounted(() => {
  window.removeEventListener("resize", resizeChart)
  chart?.dispose()
})

defineExpose({ resize: resizeChart, chart })
</script>

<style scoped>
.chart-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dong-blue);
  position: relative;
  padding-left: 12px;
}

.chart-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, var(--dong-blue), var(--dong-indigo-dark));
  border-radius: 2px;
}

.chart-container {
  width: 100%;
}
</style>
