import { createRouter, createWebHistory } from "vue-router"
import { useUserStore } from "@/stores/user"

const routes = [
  { path: "/", name: "Home", component: () => import("@/views/Home.vue") },
  { path: "/knowledge", name: "Knowledge", component: () => import("@/views/Knowledge.vue") },
  { path: "/inheritors", name: "Inheritors", component: () => import("@/views/Inheritors.vue") },
  { path: "/plants", name: "Plants", component: () => import("@/views/Plants.vue") },
  { path: "/qa", name: "Qa", component: () => import("@/views/Qa.vue"), meta: { keepAlive: true } },
  { path: "/interact", name: "Interact", component: () => import("@/views/Interact.vue") },
  { path: "/resources", name: "Resources", component: () => import("@/views/Resources.vue") },
  { path: "/visual", name: "Visual", component: () => import("@/views/Visual.vue") },
  { path: "/personal", name: "Personal", component: () => import("@/views/PersonalCenter.vue"), meta: { requiresAuth: true } },
  { path: "/admin", name: "Admin", component: () => import("@/views/Admin.vue"), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: "/about", name: "About", component: () => import("@/views/About.vue") },
  { path: "/feedback", name: "Feedback", component: () => import("@/views/Feedback.vue") },
  { path: "/search", name: "Search", component: () => import("@/views/GlobalSearch.vue") },
  { path: "/:pathMatch(.*)*", name: "NotFound", component: () => import("@/views/NotFound.vue") }
]

const router = createRouter({ history: createWebHistory(), routes })

const VALIDATION_CACHE_TTL = 60 * 1000
const validationCache = {
  promise: null,
  token: null,
  timestamp: 0
}

function isTokenLocallyExpired(userStore) {
  const token = userStore.token
  if (!token) return true
  
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return false
    const payload = JSON.parse(atob(parts[1]))
    if (!payload || !payload.exp) return false
    return Date.now() >= payload.exp * 1000
  } catch {
    return false
  }
}

async function validateTokenWithCache(userStore) {
  const now = Date.now()
  const token = userStore.token
  
  if (!token) return false
  
  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return false
  }
  
  if (validationCache.token === token && 
      validationCache.promise && 
      now - validationCache.timestamp < VALIDATION_CACHE_TTL) {
    return validationCache.promise
  }
  
  validationCache.token = token
  validationCache.timestamp = now
  validationCache.promise = userStore.validateToken().then(isValid => {
    if (!isValid) userStore.clearAuth()
    return isValid
  })
  
  return validationCache.promise
}

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (!userStore.token && sessionStorage.getItem('token')) {
    userStore.initializeFromStorage()
  }

  if (!to.meta.requiresAuth) {
    return next()
  }

  if (!userStore.token) {
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  if (isTokenLocallyExpired(userStore)) {
    userStore.clearAuth()
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  const isValid = await validateTokenWithCache(userStore)
  if (!isValid) {
    return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ path: "/", query: { noPermission: "true" } })
  }

  next()
})

export default router
