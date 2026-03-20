<template>
  <div class="image-carousel-container">
    <div class="carousel-wrapper">
      <button
        v-if="imageList.length > 1"
        class="carousel-btn prev"
        :disabled="currentIndex === 0"
        @click="prevSlide"
      >
        <el-icon><ArrowLeft /></el-icon>
      </button>
      
      <div class="carousel-track-wrapper">
        <transition-group
          name="slide-fade"
          tag="div"
          class="carousel-track"
        >
          <div
            v-for="(img, index) in imageList"
            v-show="index === currentIndex"
            :key="index"
            class="carousel-slide"
          >
            <el-image
              :src="img"
              fit="contain"
              class="carousel-image"
              :preview-src-list="imageList"
              :initial-index="index"
            >
              <template #placeholder>
                <div class="image-placeholder">
                  <el-icon class="loading-icon">
                    <Loading />
                  </el-icon><span>加载中...</span>
                </div>
              </template>
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon><span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </transition-group>
      </div>
      
      <button
        v-if="imageList.length > 1"
        class="carousel-btn next"
        :disabled="currentIndex === imageList.length - 1"
        @click="nextSlide"
      >
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <div
      v-if="imageList.length > 1"
      class="carousel-indicators"
    >
      <span
        v-for="(_, index) in imageList"
        :key="index"
        class="indicator"
        :class="{ active: index === currentIndex }"
        @click="currentIndex = index"
      />
    </div>

    <div
      v-if="imageList.length > 1"
      class="carousel-counter"
    >
      {{ currentIndex + 1 }} / {{ imageList.length }}
    </div>

    <div
      v-if="imageList.length === 0"
      class="empty-state"
    >
      <el-icon :size="48">
        <Picture />
      </el-icon>
      <p>暂无图片</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { ArrowLeft, ArrowRight, Picture, Loading } from '@element-plus/icons-vue'
import { parseMediaList } from '@/utils'

const props = defineProps({
  images: { type: [Array, String], default: () => [] },
  autoPlay: { type: Boolean, default: true },
  autoPlayInterval: { type: Number, default: 4000 },
  height: { type: String, default: '320px' }
})

const currentIndex = ref(0)
let autoPlayTimer = null

const imageList = computed(() => {
  const items = parseMediaList(props.images)
  return items.map(item => typeof item === 'string' ? item : item.url)
})

const prevSlide = () => {
  if (currentIndex.value > 0) currentIndex.value--
  else if (imageList.value.length > 1) currentIndex.value = imageList.value.length - 1
}

const nextSlide = () => {
  currentIndex.value = currentIndex.value < imageList.value.length - 1 ? currentIndex.value + 1 : 0
}

const startAutoPlay = () => {
  stopAutoPlay()
  if (props.autoPlay && imageList.value.length > 1) autoPlayTimer = setInterval(nextSlide, props.autoPlayInterval)
}

const stopAutoPlay = () => {
  if (autoPlayTimer) { clearInterval(autoPlayTimer); autoPlayTimer = null }
}

watch(imageList, () => {
  currentIndex.value = 0
  if (props.autoPlay) startAutoPlay()
}, { immediate: true })

onUnmounted(stopAutoPlay)

defineExpose({ prevSlide, nextSlide, startAutoPlay, stopAutoPlay })
</script>

<style scoped>
.image-carousel-container { width: 100%; }
.carousel-wrapper { display: flex; align-items: center; gap: 12px; }
.carousel-track-wrapper { flex: 1; overflow: hidden; border-radius: 12px; background: #f8f9fa; }
.carousel-track { position: relative; width: 100%; height: v-bind(height); }
.carousel-slide { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
.carousel-image { width: 100%; height: 100%; }
.carousel-btn { width: 40px; height: 40px; border: none; border-radius: 50%; background: rgba(26, 82, 118, 0.9); color: var(--text-inverse); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; flex-shrink: 0; }
.carousel-btn:hover:not(:disabled) { background: rgba(26, 82, 118, 1); transform: scale(1.1); }
.carousel-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.carousel-indicators { display: flex; justify-content: center; gap: 8px; margin-top: 16px; }
.indicator { width: 10px; height: 10px; border-radius: 50%; background: #d0d0d0; cursor: pointer; transition: all 0.3s; }
.indicator:hover { background: #a0a0a0; }
.indicator.active { background: var(--dong-blue, #1A5276); width: 24px; border-radius: 5px; }
.carousel-counter { text-align: center; font-size: 13px; color: var(--text-muted); margin-top: 8px; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 200px; color: #999; }
.empty-state p { margin: 12px 0 0; font-size: 14px; }
.image-placeholder, .image-error { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; background: linear-gradient(135deg, #e8f4f8, #d4e8ed); color: var(--dong-blue, #1A5276); }
.loading-icon { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.4s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateX(20px); }
.slide-fade-leave-to { opacity: 0; transform: translateX(-20px); }
@media (max-width: 768px) { .carousel-btn { width: 32px; height: 32px; } .carousel-wrapper { gap: 8px; } }
</style>
