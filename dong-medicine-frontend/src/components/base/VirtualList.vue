<template>
  <div ref="containerRef" class="virtual-list" @scroll="handleScroll">
    <div 
      class="virtual-list-phantom" 
      :style="{ height: `${totalHeight}px` }"
    />
    <div 
      class="virtual-list-content" 
      :style="{ transform: `translateY(${offset}px)` }"
    >
      <div 
        v-for="item in visibleItems" 
        :key="getKey(item)" 
        class="virtual-list-item"
        :style="{ height: `${itemSize}px` }"
      >
        <slot :item="item" :index="item._index" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  itemSize: {
    type: Number,
    default: 50
  },
  buffer: {
    type: Number,
    default: 5
  },
  keyField: {
    type: String,
    default: 'id'
  }
})

const emit = defineEmits(['scroll', 'visible-change'])

const containerRef = ref(null)
const scrollTop = ref(0)
const containerHeight = ref(0)

const totalHeight = computed(() => props.items.length * props.itemSize)

const visibleCount = computed(() => {
  return Math.ceil(containerHeight.value / props.itemSize) + props.buffer * 2
})

const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemSize) - props.buffer
  return Math.max(0, index)
})

const endIndex = computed(() => {
  return Math.min(props.items.length, startIndex.value + visibleCount.value)
})

const offset = computed(() => {
  return startIndex.value * props.itemSize
})

const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value).map((item, i) => ({
    ...item,
    _index: startIndex.value + i
  }))
})

const getKey = (item) => {
  return item[props.keyField] || item._index
}

const handleScroll = (e) => {
  scrollTop.value = e.target.scrollTop
  emit('scroll', {
    scrollTop: scrollTop.value,
    startIndex: startIndex.value,
    endIndex: endIndex.value
  })
}

const updateContainerHeight = () => {
  if (containerRef.value) {
    containerHeight.value = containerRef.value.clientHeight
  }
}

const scrollToIndex = (index) => {
  if (containerRef.value) {
    containerRef.value.scrollTop = index * props.itemSize
  }
}

const scrollToTop = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
}

watch([startIndex, endIndex], ([start, end]) => {
  emit('visible-change', { startIndex: start, endIndex: end })
})

onMounted(() => {
  updateContainerHeight()
  window.addEventListener('resize', updateContainerHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateContainerHeight)
})

defineExpose({
  scrollToIndex,
  scrollToTop,
  updateContainerHeight
})
</script>

<style scoped>
.virtual-list {
  position: relative;
  overflow-y: auto;
  height: 100%;
}

.virtual-list-phantom {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  z-index: -1;
}

.virtual-list-content {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
}

.virtual-list-item {
  box-sizing: border-box;
}
</style>
