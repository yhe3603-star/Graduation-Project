import { createRouter, createWebHistory } from "vue-router"
import { useUserStore } from "@/stores/user"

const routes = [
  { path: "/", name: "Home", component: () => import("@/views/Home.vue") },
  { path: "/knowledge", name: "Knowledge", component: () => import("@/views/Knowledge.vue") },
  { path: "/inheritors", name: "Inheritors", component: () => import("@/views/Inheritors.vue") },
  { path: "/plants", name: "Plants", component: () => import("@/views/Plants.vue") },
  { path: "/qa", name: "Qa", component: () => import("@/views/Qa.vue") },
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

let tokenValidationPromise = null
let lastValidationTime = 0
/** 必须与当前 token 一致才复用校验结果，避免登录换 token 后仍沿用旧 Promise 导致误判 */
let lastValidatedTokenSnapshot = ''
const VALIDATION_INTERVAL = 60 * 1000

async function validateToken() {
  const userStore = useUserStore()
  if (!userStore.token) return false

  const now = Date.now()
  const sameToken =
    lastValidatedTokenSnapshot === userStore.token
  if (
    sameToken &&
    now - lastValidationTime < VALIDATION_INTERVAL &&
    tokenValidationPromise
  ) {
    return tokenValidationPromise
  }

  lastValidationTime = now
  lastValidatedTokenSnapshot = userStore.token
  tokenValidationPromise = userStore
    .validateToken()
    .then((isValid) => {
      if (!isValid) {
        userStore.clearAuth()
      }
      return isValid
    })

  return tokenValidationPromise
}

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (!userStore.token && sessionStorage.getItem('token')) {
    userStore.initializeFromStorage()
  }

  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
    }

    const isValid = await validateToken()
    if (!isValid) {
      return next({ path: "/", query: { redirect: to.fullPath, needLogin: "true" } })
    }

    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      return next({ path: "/", query: { noPermission: "true" } })
    }
  }

  next()
})

export default router
