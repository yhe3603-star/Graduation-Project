import { inject } from 'vue'
import { ElMessage } from 'element-plus'

export function useLoginPrompt() {
  let showLoginDialog = null
  try {
    showLoginDialog = inject('showLoginDialog', null)
  } catch {
    // inject() outside component setup context (e.g. in tests) — silently ignore
  }

  function requireLogin(message = '请先登录') {
    ElMessage.warning(message)
    if (typeof showLoginDialog === 'function') {
      showLoginDialog()
    }
    return false
  }

  return { requireLogin, showLoginDialog }
}
