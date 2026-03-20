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
      ref="chartRef"
      class="chart-container"
      :style="{ height: height + 'px' }"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue"
import * as echarts from "echarts"

const props = defineProps({
  title: String,
  height: { type: Number, default: 300 },
  option: { type: Object, required: true }
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
.chart-card { border-radius: 16px; border: none; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06); }
.chart-header { display: flex; justify-content: space-between; align-items: center; }
.chart-title { font-size: 16px; font-weight: 600; color: var(--dong-blue); }
.chart-container { width: 100%; }
</style>
