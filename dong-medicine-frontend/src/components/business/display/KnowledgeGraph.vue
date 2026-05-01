<template>
  <div ref="chartContainer" class="knowledge-graph" :style="containerStyle"></div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  plantName: {
    type: String,
    default: ''
  },
  relatedPlants: {
    type: Array,
    default: () => []
  },
  knowledgeTitle: {
    type: String,
    default: '侗医药知识'
  },
  height: {
    type: [String, Number],
    default: '500px'
  },
  width: {
    type: [String, Number],
    default: '100%'
  }
})

const chartContainer = ref(null)
let chartInstance = null
let resizeObserver = null

const containerStyle = computed(() => ({
  height: typeof props.height === 'number' ? props.height + 'px' : props.height,
  width: typeof props.width === 'number' ? props.width + 'px' : props.width
}))

function buildGraphData() {
  const nodes = []
  const links = []
  const categories = [{ name: '知识' }, { name: '药材' }, { name: '功效分类' }]

  // Central knowledge node
  nodes.push({
    id: 'center',
    name: props.knowledgeTitle || '侗医药知识',
    category: 0,
    symbolSize: 50,
    itemStyle: { color: '#1A5276' },
    label: { show: true, fontSize: 14, fontWeight: 'bold' }
  })

  // Track efficacy categories to avoid duplicates
  const efficacySet = new Map()

  // Related plants and efficacy categories
  if (props.relatedPlants && props.relatedPlants.length > 0) {
    props.relatedPlants.forEach((plant, index) => {
      const plantName = plant.nameCn || plant.name || `药材${index + 1}`
      const nodeId = `plant_${index}`

      nodes.push({
        id: nodeId,
        name: plantName,
        category: 1,
        symbolSize: 36,
        itemStyle: { color: '#28B463' },
        label: { show: true, fontSize: 12 }
      })

      links.push({
        source: 'center',
        target: nodeId,
        lineStyle: { color: '#999', width: 1.5, curveness: 0.2 }
      })

      // Connect to efficacy categories
      const categories = [
        plant.efficacy,
        plant.category,
        plant.diseaseCategory,
        plant.herbCategory,
        plant.therapyCategory
      ].filter(Boolean)

      const uniqueCategories = [...new Set(categories)]
      uniqueCategories.forEach((cat) => {
        const catKey = String(cat).trim()
        if (!catKey) return
        if (!efficacySet.has(catKey)) {
          const catNodeId = `efficacy_${efficacySet.size}`
          efficacySet.set(catKey, catNodeId)
          nodes.push({
            id: catNodeId,
            name: catKey,
            category: 2,
            symbolSize: 28,
            itemStyle: { color: '#3498db' },
            label: { show: true, fontSize: 10 }
          })
        }
        links.push({
          source: nodeId,
          target: efficacySet.get(catKey),
          lineStyle: { color: '#b0c4de', width: 1, curveness: 0.2 }
        })
      })
    })
  }

  return { nodes, links, categories }
}

function getOption() {
  const { nodes, links, categories } = buildGraphData()

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `<b>${params.name}</b><br/>类型: ${categories[params.data.category]?.name || '未知'}`
        }
        return `${params.data.source} → ${params.data.target}`
      }
    },
    legend: [{
      data: categories.map(c => c.name),
      orient: 'horizontal',
      bottom: 10,
      left: 'center',
      textStyle: { color: 'var(--text-secondary, #555)', fontSize: 12 }
    }],
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      categories: categories,
      roam: true,
      draggable: true,
      force: {
        repulsion: 300,
        gravity: 0.08,
        edgeLength: [100, 200],
        layoutAnimation: true,
        friction: 0.6
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 3
        }
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{b}',
        color: 'var(--text-primary, #1a1a1a)',
        fontSize: 11
      },
      lineStyle: {
        color: '#bbb',
        opacity: 0.6,
        width: 1.5,
        curveness: 0.2
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 2,
        shadowBlur: 8,
        shadowColor: 'rgba(0, 0, 0, 0.15)'
      }
    }]
  }
}

function initChart() {
  if (!chartContainer.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartContainer.value)

  const option = getOption()
  chartInstance.setOption(option)

  // Resize handling
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
  resizeObserver = new ResizeObserver(() => {
    if (chartInstance && !chartInstance.isDisposed()) {
      chartInstance.resize()
    }
  })
  resizeObserver.observe(chartContainer.value)
}

function updateChart() {
  if (!chartInstance || chartInstance.isDisposed()) {
    initChart()
    return
  }
  const option = getOption()
  chartInstance.setOption(option, true)
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance && !chartInstance.isDisposed()) {
    chartInstance.dispose()
    chartInstance = null
  }
})

watch(
  () => [props.relatedPlants, props.knowledgeTitle, props.plantName],
  () => {
    nextTick(() => {
      updateChart()
    })
  },
  { deep: true }
)
</script>

<style scoped>
.knowledge-graph {
  border-radius: var(--radius-lg);
  background: var(--text-inverse);
  box-shadow: var(--shadow-sm);
  min-height: 300px;
  overflow: hidden;
}

@media (max-width: 768px) {
  .knowledge-graph {
    min-height: 250px;
  }
}

@media (max-width: 480px) {
  .knowledge-graph {
    min-height: 220px;
    border-radius: var(--radius-md);
  }
}
</style>
