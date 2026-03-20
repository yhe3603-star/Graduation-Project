/**
 * @file 应用入口文件
 * @description Vue应用初始化配置，包括插件注册、全局组件和样式导入
 * @author Dong Medicine Team
 * @version 1.0.0
 * 
 * @requires vue
 * @requires element-plus
 * @requires @element-plus/icons-vue
 * @requires ./App.vue
 * @requires ./router
 * @requires ./utils/request
 * @requires ./stores
 * @requires ./styles
 * 
 * 初始化流程：
 * 1. 创建Vue应用实例
 * 2. 注册Element Plus组件库（中文语言包）
 * 3. 注册Vue Router路由
 * 4. 注册Pinia状态管理
 * 5. 全局注入request服务
 * 6. 注册所有Element Plus图标组件
 * 7. 挂载应用到DOM
 */

import { createApp } from "vue"
import ElementPlus from "element-plus"
import zhCn from "element-plus/dist/locale/zh-cn.mjs"
import "element-plus/dist/index.css"
import * as Icons from "@element-plus/icons-vue"
import App from "./App.vue"
import router from "./router"
import request from "./utils/request"
import { pinia } from "./stores"
import "./styles/index.css"

const app = createApp(App)

app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.use(pinia)

app.provide("request", request)
app.config.globalProperties.$axios = request

Object.entries(Icons).forEach(([name, component]) => {
  app.component(name, component)
})

app.mount("#app")
