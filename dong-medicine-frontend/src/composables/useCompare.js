import { ref, watch } from 'vue'

const STORAGE_KEY = 'dong_compare_plants'

const compareList = ref(loadFromStorage())

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) return parsed.slice(0, 3)
    }
  } catch {}
  return []
}

function saveToStorage() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(compareList.value))
  } catch {}
}

watch(compareList, saveToStorage, { deep: true })

export function useCompare() {
  const MAX_COMPARE = 3

  const isInCompare = (id) => compareList.value.some(p => p.id === id)

  const addToCompare = (plant) => {
    if (!plant || !plant.id) return false
    if (isInCompare(plant.id)) return false
    if (compareList.value.length >= MAX_COMPARE) return false
    compareList.value.push({ ...plant })
    saveToStorage()
    return true
  }

  const removeFromCompare = (id) => {
    compareList.value = compareList.value.filter(p => p.id !== id)
    saveToStorage()
  }

  const clearCompare = () => {
    compareList.value = []
    saveToStorage()
  }

  return {
    compareList,
    isInCompare,
    addToCompare,
    removeFromCompare,
    clearCompare,
    MAX_COMPARE
  }
}
