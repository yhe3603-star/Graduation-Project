<template>
  <div class="video-player-container">
    <div
      v-if="videoList.length > 0"
      class="video-wrapper"
    >
      <div class="video-carousel">
        <button
          v-if="videoList.length > 1"
          class="carousel-btn prev"
          :disabled="currentIndex === 0"
          @click="prevVideo"
        >
          <el-icon><ArrowLeft /></el-icon>
        </button>
        
        <div class="video-track-wrapper">
          <video
            ref="videoRef"
            :key="videoList[currentIndex]"
            :src="videoList[currentIndex]"
            controls
            class="video-player"
            preload="metadata"
            @error="onVideoError"
            @loadeddata="onVideoLoaded"
          >
            您的浏览器不支持视频播放
          </video>
        </div>
        
        <button
          v-if="videoList.length > 1"
          class="carousel-btn next"
          :disabled="currentIndex === videoList.length - 1"
          @click="nextVideo"
        >
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>

      <div
        v-if="videoList.length > 1"
        class="carousel-indicators"
      >
        <span
          v-for="(_, index) in videoList"
          :key="index"
          class="indicator"
          :class="{ active: index === currentIndex }"
          @click="switchToVideo(index)"
        />
      </div>

      <div
        v-if="videoList.length > 1"
        class="carousel-counter"
      >
        {{ currentIndex + 1 }} / {{ videoList.length }}
      </div>
    </div>

    <div
      v-else
      class="empty-state"
    >
      <el-icon :size="48">
        <VideoPlay />
      </el-icon>
      <p>暂无视频</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { logAutoPlayWarn } from '@/utils/logger'
import { ArrowLeft, ArrowRight, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { parseMediaList } from '@/utils/media'

const props = defineProps({
  videos: { type: [Array, String], default: () => [] },
  height: { type: String, default: '400px' },
  autoPlay: { type: Boolean, default: true }
})

const emit = defineEmits(['video-play', 'video-pause', 'video-ended'])

const videoRef = ref(null)
const currentIndex = ref(0)

const videoList = computed(() => {
  const items = parseMediaList(props.videos)
  return items.map(item => typeof item === 'string' ? item : item.url)
})

const play = () => {
  if (videoRef.value) {
    videoRef.value.play().catch(e => console.warn('自动播放失败，可能需要用户交互:', e))
    emit('video-play', currentIndex.value)
  }
}

const pause = () => {
  if (videoRef.value) { videoRef.value.pause(); emit('video-pause', currentIndex.value) }
}

const handleVideoSwitch = (newIndex) => {
  pause()
  currentIndex.value = newIndex
  if (props.autoPlay) setTimeout(() => play(), 100)
}

const prevVideo = () => { if (currentIndex.value > 0) handleVideoSwitch(currentIndex.value - 1) }
const nextVideo = () => { if (currentIndex.value < videoList.value.length - 1) handleVideoSwitch(currentIndex.value + 1) }
const switchToVideo = (index) => { if (index !== currentIndex.value) handleVideoSwitch(index) }
const onVideoError = () => ElMessage.warning('视频加载失败，请检查视频链接是否有效')
const onVideoLoaded = () => { if (props.autoPlay) play() }

watch(videoList, (newList, oldList) => {
  if (JSON.stringify(newList) !== JSON.stringify(oldList || [])) {
    currentIndex.value = 0
    if (videoRef.value) videoRef.value.load()
  }
}, { immediate: true, deep: true })

watch(() => props.autoPlay, (newVal) => { if (videoRef.value) newVal ? play() : pause() })

onMounted(() => {
  if (videoRef.value && props.autoPlay && videoList.value.length > 0) videoRef.value.addEventListener('canplay', play, { once: true })
})

onUnmounted(pause)

defineExpose({ play, pause, prevVideo, nextVideo, switchToVideo, getCurrentIndex: () => currentIndex.value })
</script>

<style scoped>
.video-player-container { width: 100%; }
.video-wrapper { width: 100%; }
.video-carousel { display: flex; align-items: center; gap: 12px; }
.video-track-wrapper { flex: 1; overflow: hidden; border-radius: 12px; background: #000; }
.video-player { width: 100%; max-height: v-bind(height); display: block; background: #000; border-radius: 12px; }
.carousel-btn { width: 40px; height: 40px; border: none; border-radius: 50%; background: rgba(26, 82, 118, 0.9); color: var(--text-inverse); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; flex-shrink: 0; }
.carousel-btn:hover:not(:disabled) { background: rgba(26, 82, 118, 1); transform: scale(1.1); }
.carousel-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.carousel-indicators { display: flex; justify-content: center; gap: 8px; margin-top: 16px; }
.indicator { width: 10px; height: 10px; border-radius: 50%; background: #d0d0d0; cursor: pointer; transition: all 0.3s; }
.indicator:hover { background: #a0a0a0; }
.indicator.active { background: var(--dong-blue, #1A5276); width: 24px; border-radius: 5px; }
.carousel-counter { text-align: center; font-size: 13px; color: var(--text-muted); margin-top: 8px; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: #999; }
.empty-state p { margin: 12px 0 0; font-size: 14px; }
@media (max-width: 768px) { .carousel-btn { width: 32px; height: 32px; } .video-carousel { gap: 8px; } }
</style>
