import { createApp } from "vue"
import App from "./App.vue"
import router from "./router"
import request from "./utils/request"
import { pinia } from "./stores"
import { directives } from './directives'
import "./styles/index.css"

const app = createApp(App)

app.use(directives)
app.use(router)
app.use(pinia)

app.provide("request", request)
app.config.globalProperties.$axios = request

app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Error]', err, info)
}

app.mount("#app")

router.isReady().then(() => {
  const loading = document.getElementById('app-loading')
  if (loading) {
    loading.classList.add('fade-out')
    setTimeout(() => loading.remove(), 300)
  }
})
