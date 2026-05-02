import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Star, EditPen, Cherry } from '@element-plus/icons-vue'
import request from '@/utils/request'

const historyTypeIcons = {
  plant: Cherry,
  knowledge: Document,
  inheritor: User,
  resource: Star,
  qa: EditPen
}

const typeIconMap = {
  plant: Cherry,
  knowledge: Document,
  inheritor: User,
  resource: Star,
  qa: EditPen
}

export function useBrowseHistory() {
  const router = useRouter()
  const browseHistory = ref([])
  const historyLoading = ref(false)
  const historyPage = ref(1)
  const historyPageSize = ref(10)

  const paginatedHistory = computed(() => {
    const start = (historyPage.value - 1) * historyPageSize.value
    return browseHistory.value.slice(start, start + historyPageSize.value)
  })

  function getHistoryIcon(type) {
    return historyTypeIcons[type] || typeIconMap.knowledge
  }

  function goToHistoryItem(item) {
    const pathMap = {
      plant: '/plants',
      knowledge: '/knowledge',
      inheritor: '/inheritors',
      resource: '/resources',
      qa: '/qa'
    }
    const basePath = pathMap[item.targetType]
    if (basePath) {
      router.push(`${basePath}?id=${item.targetId || item.id}`)
    }
  }

  async function loadBrowseHistory() {
    historyLoading.value = true
    try {
      const res = await request.get('/browse-history/my')
      browseHistory.value = res.data || res || []
    } catch (e) {
      console.error('加载浏览历史失败:', e)
      browseHistory.value = []
    } finally {
      historyLoading.value = false
    }
  }

  return {
    browseHistory, historyLoading, historyPage, historyPageSize,
    paginatedHistory, getHistoryIcon, goToHistoryItem, loadBrowseHistory
  }
}
