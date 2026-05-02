# 前端源代码目录 (src/)

`src/` 是整个前端项目的核心目录，包含所有源代码：Vue 组件、JavaScript 逻辑、CSS 样式、路由配置、状态管理等。只有 `src/` 里的文件才会被 Vite 处理和打包。

## 目录结构

```
src/
├── main.js                # 应用入口文件
├── App.vue                # 根组件
├── router/                # Vue Router 路由配置
│   └── index.js           # 路由定义 + 导航守卫
├── stores/                # Pinia 状态管理
│   ├── index.js           # Pinia 实例创建
│   └── user.js            # 用户认证状态（token、登录/退出）
├── composables/           # 组合式函数（Composition API Hooks）
│   ├── index.js           # 统一导出
│   ├── useAdminData.js    # 管理后台数据获取
│   ├── useDebounce.js     # 防抖函数
│   ├── useErrorHandler.js # 全局错误处理
│   ├── useFavorite.js     # 收藏功能（增删查）
│   ├── useFileUpload.js   # 文件上传通用逻辑
│   ├── useFormDialog.js   # 表单对话框（CRUD + 更新日志）
│   ├── useInteraction.js  # 交互功能（倒计时、评论、分页、过滤、统计）
│   ├── useMedia.js        # 媒体显示（文档预览、媒体Tabs、文件信息）
│   ├── usePersonalCenter.js # 个人中心全部功能
│   ├── usePlantGame.js    # 植物识别游戏
│   ├── useQuiz.js         # 趣味答题功能
│   ├── useUpdateLog.js    # 更新日志管理
│   └── useVisualData.js   # 数据可视化数据获取
├── utils/                 # 工具函数与常量
│   ├── index.js           # 统一导出 + 通用工具函数
│   ├── request.js         # Axios 封装（拦截器、认证、重试、XSS防护）
│   ├── cache.js           # 数据缓存（内存 + sessionStorage 两层）
│   ├── xss.js             # XSS/SQL注入检测与清理
│   ├── media.js           # 媒体文件处理（类型判断、URL规范化、列表解析）
│   ├── chartConfig.js     # ECharts 图表配置生成器
│   ├── logger.js          # 日志输出（开发环境打印，生产静默）
│   ├── adminUtils.js      # 管理后台工具（表格列配置、标签映射）
│   └── validators.js      # 表单验证规则工厂（密码等）
├── components/            # 组件库（三层架构）
│   ├── base/              # 基础组件（ErrorBoundary、VirtualList）
│   ├── common/            # 通用组件（骨架屏、PageLoading）
│   └── business/          # 业务组件
│       ├── layout/        # AppHeader、AppFooter
│       ├── display/       # SearchFilter、CardGrid、ChartCard 等
│       ├── interact/      # CommentSection、QuizSection、PlantGame 等
│       ├── media/         # ImageCarousel、VideoPlayer 等
│       ├── upload/        # ImageUploader、FileUploader 等
│       ├── dialogs/       # 前台详情弹窗
│       └── admin/         # 管理后台组件
├── views/                 # 页面组件（15 个页面，与路由一一对应）
│   ├── Home.vue           # 首页
│   ├── Plants.vue         # 药用资源图鉴
│   ├── Knowledge.vue      # 非遗医药知识库
│   ├── Inheritors.vue     # 传承人风采
│   ├── Qa.vue             # 问答社区
│   ├── Resources.vue      # 学习资源
│   ├── Interact.vue       # 文化互动专区
│   ├── SolarTerms.vue     # 节气采药
│   ├── About.vue          # 关于平台
│   ├── Feedback.vue       # 意见反馈
│   ├── Admin.vue          # 管理后台
│   ├── PersonalCenter.vue # 个人中心
│   ├── GlobalSearch.vue   # 全局搜索
│   ├── Visual.vue         # 数据可视化
│   └── NotFound.vue       # 404 页面
├── styles/                # 全局样式系统
│   ├── index.css          # 样式入口（统一导入）
│   ├── variables.css      # CSS 变量（设计标记：颜色、字号、间距、阴影等）
│   ├── theme-override.css # Element Plus 主题色覆盖
│   ├── dong-patterns.css  # 侗族文化纹样与装饰
│   ├── base.css           # 基础样式（reset、标题、链接、图片）
│   ├── common.css         # 通用样式工具类
│   ├── components.css     # 组件样式（卡片、按钮、表单等）
│   ├── home.css           # 首页专用样式
│   ├── pages.css          # 各页面布局样式
│   ├── dialog-common.css  # 弹窗通用样式
│   ├── media-common.css   # 媒体组件通用样式
│   ├── Visual.css         # 数据可视化页样式
│   └── scss/              # SCSS 变量和 mixins（全局注入）
│       ├── _variables.scss
│       └── _mixins.scss
├── config/                # 页面级静态配置
│   └── homeConfig.js      # 首页的统计数据、快捷入口等配置
├── directives/            # 全局自定义指令
│   └── index.js           # 8 个指令（lazy、debounce、throttle、permission 等）
└── __test__/              # 单元测试
    └── ...
```

## 技术栈总览

| 技术 | 用途 | 核心 API / 文件 |
|------|------|----------------|
| Vue 3.4 | SFC 组件（`<script setup>`） | 所有 .vue 文件 |
| Pinia 2.3 | 全局状态管理 | `stores/user.js`（用户认证） |
| Vue Router 4.2 | 客户端路由 + 导航守卫 | `router/index.js`（15 条路由） |
| Axios 1.6 | HTTP 请求 | `utils/request.js`（自动 token、重试、XSS防护） |
| Element Plus 2.4 | UI 组件（表单、表格、弹窗、标签等） | 全局注册 |
| ECharts 5.4 | 数据可视化（柱状图、饼图、折线图、雷达图） | `utils/chartConfig.js` |
| Sass 1.99 | CSS 预处理（全局变量/mixins 自动注入） | `styles/scss/` |
| marked 18 | Markdown 转 HTML | AI 对话消息渲染 |
| DOMPurify 3 | HTML 安全净化 | 配合 marked 使用防止 XSS |

## 路径别名

Vite 配置了 `@` 别名指向 `src/` 目录：

```js
// vite.config.js
resolve: { alias: { "@": resolve(__dirname, "src") } }
```

在所有组件中可使用：

```js
import request from '@/utils/request'
import AppHeader from '@/components/business/layout/AppHeader.vue'
```

## SCSS 全局变量

Vite 配置了 SCSS 全局注入，每个 `<style lang="scss">` 块自动可用 SCSS 变量和 mixins，无需手动 import：

```scss
// 自动注入：@/styles/scss/_variables.scss 和 @/styles/scss/_mixins.scss
.my-component {
  color: $dong-indigo;  // SCSS 变量直接可用
  @include flex-center; // SCSS mixin 直接可用
}
```

## 开发约定

### 组件命名

- **页面组件**：PascalCase，放在 `views/` 目录，如 `GlobalSearch.vue`
- **基础/通用组件**：PascalCase，放在 `components/base/` 或 `components/common/`
- **业务组件**：PascalCase，按功能域放在 `components/business/<domain>/`

### Composable 命名

- 所有组合式函数以 `use` 开头：`useQuiz()`、`useFavorite()`、`usePersonalCenter()`
- 文件名与导出函数名一致
- 导出统一通过 `composables/index.js` 的 `export * from`

### 工具函数

- 纯函数无副作用，放在 `utils/` 目录
- 统一通过 `utils/index.js` 导出
- 日志输出通过 `utils/logger.js` 的专用函数（开发环境打印，生产环境静默）

### 样式组织

- CSS 变量集中定义在 `styles/variables.css`（设计标记层）
- Element Plus 主题覆盖在 `styles/theme-override.css`
- 全局样式按功能拆分为独立文件，通过 `styles/index.css` 统一 `@import`
- 组件内部样式使用 `scoped` 避免污染

### 状态管理

- 仅跨组件共享的全局状态放入 Pinia store
- 组件私有状态使用 `ref`/`reactive` 在 `<script setup>` 中定义
- 可复用的有状态逻辑封装为 composable

### HTTP 请求

- 统一使用 `request` 实例（`utils/request.js` 导出的 Axios 实例）
- GET/POST/PUT/DELETE/PATCH 方法均为 async，返回 `{ code, msg, data }` 格式
- 通过 `request.get(url, config)` / `request.post(url, data, config)` 调用
- 需要跳过认证刷新的请求传 `{ skipAuthRefresh: true }`
