<template>
  <div class="herb-audio">
    <el-tooltip
      :content="tooltipText"
      placement="top"
    >
      <button
        class="audio-btn"
        :class="{ playing: isPlaying, unsupported: !supported }"
        :disabled="!supported"
        @click="togglePlay"
      >
        <span class="audio-icon">
          <el-icon :size="iconSize">
            <component :is="isPlaying ? VideoPause : VideoPlay" />
          </el-icon>
        </span>
        <span v-if="showLabel" class="audio-label">
          {{ isPlaying ? '播放中...' : buttonLabel }}
        </span>
      </button>
    </el-tooltip>
    <span v-if="!supported" class="unsupported-hint">
      您的浏览器不支持语音合成
    </span>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { VideoPause, VideoPlay } from '@element-plus/icons-vue'

const props = defineProps({
  plantName: { type: String, default: '' },
  plantId: { type: [Number, String], default: null },
  showLabel: { type: Boolean, default: true },
  iconSize: { type: [Number, String], default: 18 },
  buttonLabel: { type: String, default: '侗语发音' }
})

const isPlaying = ref(false)
const supported = ref(typeof window !== 'undefined' && 'speechSynthesis' in window)

const tooltipText = computed(() => {
  if (!supported.value) return '您的浏览器不支持语音合成'
  return isPlaying.value ? '点击停止' : '点击播放侗语发音'
})

let utterance = null

function getChineseVoice() {
  if (!supported.value) return null
  const voices = window.speechSynthesis.getVoices()
  const preferred = voices.find(v => v.lang === 'zh-CN' && v.name.includes('Tingting')) ||
                    voices.find(v => v.lang === 'zh-CN' && v.name.includes('Xiaoxiao')) ||
                    voices.find(v => v.lang === 'zh-CN') ||
                    voices.find(v => v.lang === 'zh') ||
                    voices.find(v => v.lang === 'zh-HK') ||
                    voices.find(v => v.lang.startsWith('zh'))
  return preferred || voices[0]
}

function preloadVoices() {
  if (!supported.value) return
  const voices = window.speechSynthesis.getVoices()
  if (voices.length === 0) {
    window.speechSynthesis.onvoiceschanged = () => {
      window.speechSynthesis.onvoiceschanged = null
    }
    window.speechSynthesis.getVoices()
  }
}

function togglePlay() {
  if (!supported.value) return
  if (isPlaying.value) {
    stopPlay()
    return
  }
  startPlay()
}

function startPlay() {
  if (!supported.value) return

  window.speechSynthesis.cancel()

  const text = props.plantName || '暂无侗语名称'
  if (!text || text === '暂无侗语名称' || text === '暂无') {
    return
  }

  utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.85
  utterance.pitch = 1.0
  utterance.volume = 1.0

  const voice = getChineseVoice()
  if (voice) {
    utterance.voice = voice
  }

  utterance.onstart = () => {
    isPlaying.value = true
  }

  utterance.onend = () => {
    isPlaying.value = false
    utterance = null
  }

  utterance.onerror = (e) => {
    if (e.error !== 'interrupted') {
      console.warn('语音合成播放错误:', e.error)
    }
    isPlaying.value = false
    utterance = null
  }

  try {
    window.speechSynthesis.speak(utterance)
  } catch (e) {
    console.warn('语音合成启动失败:', e)
    isPlaying.value = false
    utterance = null
  }
}

function stopPlay() {
  if (supported.value) {
    window.speechSynthesis.cancel()
  }
  isPlaying.value = false
  utterance = null
}

preloadVoices()

onUnmounted(() => {
  stopPlay()
})
</script>

<style scoped>
.herb-audio {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
}

.audio-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 2px solid var(--dong-green);
  border-radius: 24px;
  background: rgba(40, 180, 99, 0.08);
  color: var(--dong-green);
  cursor: pointer;
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
  outline: none;
  font-family: inherit;
}

.audio-btn:hover {
  background: rgba(40, 180, 99, 0.15);
  transform: scale(1.05);
}

.audio-btn.playing {
  background: var(--dong-green);
  color: var(--text-inverse);
  border-color: var(--dong-green);
  animation: audio-pulse 1.2s ease-in-out infinite;
}

.audio-btn.unsupported {
  border-color: var(--text-muted);
  color: var(--text-muted);
  background: var(--bg-rice);
  cursor: not-allowed;
}

.audio-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.audio-label {
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}

.unsupported-hint {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-style: italic;
}

@keyframes audio-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(40, 180, 99, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 8px rgba(40, 180, 99, 0);
  }
}

@media (max-width: 480px) {
  .audio-btn {
    padding: 4px 10px;
    font-size: var(--font-size-xs);
  }

  .audio-label {
    font-size: var(--font-size-xs);
  }
}
</style>
