import { ref, watch, onUnmounted } from 'vue'

export function useDebounceFn(fn, delay = 300) {
  let timeoutId = null
  
  const debouncedFn = (...args) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      fn(...args)
      timeoutId = null
    }, delay)
  }
  
  debouncedFn.cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }
  
  onUnmounted(() => {
    debouncedFn.cancel()
  })
  
  return debouncedFn
}

export function useDebounce(value, delay = 300) {
  const debouncedValue = ref(value.value)
  let timeoutId = null
  
  const stop = watch(value, (newValue) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      debouncedValue.value = newValue
      timeoutId = null
    }, delay)
  })
  
  onUnmounted(() => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    stop()
  })
  
  return debouncedValue
}
