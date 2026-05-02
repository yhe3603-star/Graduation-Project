# 侗乡医药数字展示平台 - 前端项目

> **非遗视角下侗乡医药数字展示平台** -- 广西壮族自治区级非物质文化遗产数字保护与传播平台

## 项目概述

本项目是侗乡医药数字展示平台的前端工程，基于 **Vue 3** + **Element Plus** + **Vite** 构建的单页应用（SPA）。平台以"保护、传承、活态传播"为核心理念，通过数字化手段展示三江侗族自治县的传统医药文化，涵盖药用植物图鉴、非遗知识库、传承人风采、问答社区、趣味答题、植物识别游戏、二十四节气采药等丰富模块。

应用入口 HTML 文件 `index.html` 内置了自定义加载动画（带品牌色的旋转 logo、进度条、跳动圆点），在 Vue 应用完全挂载后平滑淡出消失，提升首屏体验。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | JavaScript (ES Module) | -- |
| 框架 | Vue 3 | ^3.4.0 |
| 构建工具 | Vite | ^5.0.0 |
| UI 组件库 | Element Plus | ^2.4.0 |
| 图标库 | @element-plus/icons-vue | ^2.3.1 |
| 状态管理 | Pinia | ^2.3.1 |
| 路由 | Vue Router | ^4.2.0 |
| HTTP 客户端 | Axios | ^1.6.0 |
| 数据可视化 | ECharts | ^5.4.0 |
| CSS 预处理器 | Sass | ^1.99.0 |
| Markdown 渲染 | marked | ^18.0.2 |
| XSS 防护 | dompurify | ^3.4.1 |
| 拖拽排序 | vuedraggable | ^4.1.0 |
| 工具库 | lodash-es | ^4.17.21 |

**开发依赖：**

| 类别 | 技术 | 用途 |
|------|------|------|
| 单元测试 | Vitest + @vue/test-utils + jsdom | 组件/函数单元测试 |
| E2E 测试 | @playwright/test | 端到端测试 |
| 代码规范 | ESLint + eslint-plugin-vue | 代码检查 |
| 覆盖率 | @vitest/coverage-v8 | 测试覆盖率报告 |

## 目录结构

```
dong-medicine-frontend/
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .env.ci                   # CI 环境变量
├── default.conf              # Nginx 部署配置
├── index.html                # HTML 入口（含自定义加载动画）
├── package.json              # 项目依赖与脚本
├── vite.config.js            # Vite 构建配置
├── Dockerfile                # Docker 构建文件
├── nginx.conf                # Nginx 核心配置
├── public/                   # 静态资源（不经编译处理）
│   └── vite.svg              # 网站 Favicon
├── e2e/                      # Playwright E2E 测试
│   └── tests/                # 测试用例
└── src/
    ├── main.js               # 应用入口，注册插件与全局配置
    ├── App.vue               # 根组件（登录/注册对话框、路由视图）
    ├── router/               # Vue Router 路由配置
    ├── stores/               # Pinia 状态管理
    ├── composables/          # 组合式函数（Composition API）
    ├── utils/                # 工具函数与常量
    ├── components/           # 组件库（base/common/business 三层）
    ├── views/                # 页面组件（16 个页面）
    ├── styles/               # 全局样式系统（CSS变量 + 主题覆盖 + SCSS）
    ├── config/               # 页面配置（首页数据等）
    ├── directives/           # 自定义指令（懒加载、防抖、节流等）
    └── __test__/             # 单元测试
```

## 安装与运行

### 环境配置

项目通过 `.env` 文件管理多环境变量，核心变量为 `VITE_API_BASE_URL`：

- **开发环境** (`.env.development`)：API 基础路径 `/api`，通过 Vite proxy 转发到 `localhost:8080`
- **生产环境** (`.env.production`)：API 基础路径 `/api`，通过 Nginx 反向代理
- **CI 环境** (`.env.ci`)：用于持续集成测试

### 安装依赖

```bash
cd dong-medicine-frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`，自动打开浏览器。Vite 配置了以下 **代理转发**：

| 前缀 | 目标 | 说明 |
|------|------|------|
| `/api` | `http://localhost:8080` | 后端 API 接口 |
| `/images` | `http://localhost:8080` | 图片资源 |
| `/videos` | `http://localhost:8080` | 视频资源 |
| `/documents` | `http://localhost:8080` | 文档资源 |
| `/public` | `http://localhost:8080` | 公共资源 |
| `/kkfileview` | `http://localhost:8012` | 文件预览服务 (KKFileView) |

### 构建生产版本

```bash
npm run build
```

构建产物输出到 `dist/` 目录。构建配置要点：

- **代码分割**：通过 `manualChunks` 将 `echarts`、`element-plus`、`vue` 核心库拆分为独立 vendor chunk
- **Source Map**：生产构建默认关闭 (`sourcemap: false`)，减小包体积
- **chunkSizeWarningLimit**：设置为 1500KB，避免大 chunk 告警
- **并行操作**：`maxParallelFileOps: 2`，防止 CI 环境内存溢出

### 运行测试

```bash
npm test              # 运行 Vitest 单元测试（watch 模式）
npm run test:run      # 单次运行所有测试
npm run test:coverage # 生成覆盖率报告
npm run test:e2e      # 运行 Playwright E2E 测试
npm run test:e2e:ui   # Playwright UI 模式运行测试
npm run lint          # ESLint 修复代码
```

## Vite 构建配置详解

`vite.config.js` 核心配置：

**路径别名：** `@` 映射到 `src/` 目录，方便跨层级引用。

**SCSS 全局注入：** 每个组件的 `<style lang="scss">` 自动注入以下内容，无需手动 import：

```scss
@use "@/styles/scss/variables" as *;
@use "@/styles/scss/mixins" as *;
```

**SCSS API：** 使用 `modern-compiler` 模式，适配 Sass 1.99+ 的新编译器 API。

**Rollup 手动分块策略：**

```js
manualChunks(id) {
  if (id.includes("node_modules")) {
    if (id.includes("echarts")) return "vendor-echarts";
    if (id.includes("element-plus")) return "vendor-element-plus";
    if (id.includes("vue") || id.includes("vue-router") || id.includes("pinia")) return "vendor-vue";
    return "vendor-misc";
  }
}
```

## 应用初始化流程

`main.js` 中的初始化步骤：

1. 创建 Vue 应用实例
2. 注册 Element Plus 组件库并设置中文语言包 (`zh-cn`)
3. 注册全局自定义指令（懒加载、防抖、节流、权限控制等，共 8 个指令）
4. 注册 Vue Router 路由
5. 注册 Pinia 状态管理
6. 通过 `provide` 和 `globalProperties` 全局注入 `request`（Axios 实例）
7. 批量注册所有 Element Plus 图标组件为全局组件
8. 挂载到 `#app` DOM 节点
9. 路由就绪后移除加载动画（`#app-loading` 淡出并移除）

## 组件架构

采用 **三层组件架构**：

1. **base/** -- 基础组件层：框架级通用组件，不含业务逻辑
   - `ErrorBoundary.vue`：Vue 错误边界，捕获子组件渲染错误并显示重试界面
   - `VirtualList.vue`：虚拟滚动列表，优化大数据量渲染

2. **common/** -- 通用组件层：跨页面复用的辅助 UI 组件
   - `PageLoading.vue`：页面级加载状态
   - 4 个 Skeleton 组件：不同场景的骨架屏（GridCard、GridImage、ListQa、ListResource）

3. **business/** -- 业务组件层：侗医药业务专属组件，按功能域分层
   - `layout/`：AppHeader、AppFooter
   - `display/`：SearchFilter、CardGrid、Pagination、ChartCard、StatCard、AiChatCard、KnowledgeGraph、UpdateLogCard、UpdateLogDialog、PageSidebar
   - `interact/`：CaptchaInput、CommentSection、QuizSection、InteractSidebar、PlantGame
   - `media/`：ImageCarousel、VideoPlayer、HerbAudio、MediaDisplay、DocumentList、DocumentPreview
   - `upload/`：ImageUploader、VideoUploader、DocumentUploader、FileUploader
   - `dialogs/`：PlantDetailDialog、KnowledgeDetailDialog、InheritorDetailDialog、QuizDetailDialog、ResourceDetailDialog
   - `admin/`：AdminSidebar、AdminDashboard、AdminDataTable 及对应的管理端 dialogs/forms

## 自定义指令

项目注册了 8 个全局自定义指令：

| 指令名 | 用途 |
|--------|------|
| `v-lazy` | 图片懒加载（IntersectionObserver，rootMargin 50px） |
| `v-lazy-background` | 背景图懒加载 |
| `v-debounce` | 事件防抖（默认 300ms） |
| `v-throttle` | 事件节流（默认 300ms） |
| `v-click-outside` | 点击元素外部时触发回调 |
| `v-focus` | 挂载后自动聚焦元素 |
| `v-permission` | 基于角色控制元素显隐（非指定角色则移除 DOM） |
| `v-loading` | 元素加载状态（追加 loading spinner） |

## 登录/注册流程

`App.vue` 作为根组件，内置了登录和注册对话框。流程特点：

- 使用 Element Plus 的 `el-dialog` + `el-form`
- 包含验证码组件 `CaptchaInput`（支持 `captchaKey` / `captchaCode` 双向绑定）
- 密码验证使用共享的 `createPasswordValidator()` 工厂函数
  - 密码需 8-50 位，包含字母和数字，不含空格
  - 确认密码校验支持动态引用表单密码字段
- 登录成功后调用 `userStore.setAuth()` 存储 token 和用户信息
- 注册成功后自动切换到登录对话框，预填用户名
- 未登录用户触发需要登录的操作时，可通过 `inject("showLoginDialog")` 弹出登录框
- 退出登录时调用 `userStore.logout()` 并跳转到首页

## 安全机制

- **JWT 认证**：token 存储在 localStorage，请求自动携带 `Authorization: Bearer <token>` 头
- **Token 自动刷新**：401 响应时自动尝试 `/user/refresh-token` 接口刷新 token；刷新失败触发 `auth-expired` 自定义事件清空认证状态
- **Token 本地解码**：客户端解码 JWT payload 检查 `exp` 过期时间，提前判断无需每次都请求服务端
- **XSS 防护**：请求数据在发送前经过 `sanitizeRequestData` 处理，检测并清除 `<script>`、`onerror` 等 28 种攻击模式
- **SQL 注入防护**：请求参数检测并移除 SQL 注入特征码（`--`、`;`、`union select`、`' OR 1=1` 等）
- **重复请求取消**：非 GET/HEAD/OPTIONS 的重复请求自动通过 Axios CancelToken 取消前一次请求，防止重复提交
- **请求重试**：GET/HEAD/OPTIONS 请求在网络错误或 408/429/5xx 状态码时自动重试（最多 3 次，指数退避 1s/2s/4s）
- **敏感信息脱敏**：日志输出前对敏感内容进行脱敏处理

## 缓存策略

`cache.js` 提供两层缓存：

- **内存缓存**（Map）：页面刷新后失效，最快访问速度
- **sessionStorage 缓存**：会话级别持久化，关闭标签页后失效

预置了各模块的缓存 TTL 配置：

| 模块 | TTL | 存储方式 |
|------|-----|---------|
| 植物/知识/传承人 | 10 分钟 | sessionStorage |
| 资源 | 5 分钟 | sessionStorage |
| 分类 | 30 分钟 | sessionStorage |
| 题目 | 5 分钟 | 内存 |
| 排行榜 | 2 分钟 | 内存 |
| 用户信息 | 30 分钟 | sessionStorage |

提供 `createCachedFetcher` 工厂函数将任意请求函数升级为带缓存的版本。后台每 60 秒自动清理过期缓存项。

## 设计系统

项目使用 CSS 变量构建了完整的设计系统（定义在 `styles/variables.css`），核心品牌色：

- **靛蓝** `#1A5276`（`--dong-indigo`）：侗族传统服饰主色调，象征智慧与传承
- **青绿** `#28B463`（`--dong-jade`）：代表侗乡山水与草药，寓意生机与希望
- **金铜** `#c9a227`（`--dong-gold`）：象征非遗荣誉与匠人精神
- **暖米** `#f8f5f0`（`--bg-rice`）：传统纸张质感，作为全局背景

Element Plus 的全局主题色被覆盖为项目品牌色（`styles/theme-override.css`），确保 UI 组件库与项目整体视觉风格一致。
