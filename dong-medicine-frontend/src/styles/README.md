# 样式目录 (styles/)

项目的全局样式系统，基于 **CSS 自定义属性（CSS Variables）** 构建完整的设计标记体系，覆盖 Element Plus 默认主题并融入侗族文化视觉风格。

## 文件清单

| 文件 | 职责 | 说明 |
|------|------|------|
| `index.css` | 样式入口 | 按层级顺序 `@import` 所有样式文件 |
| `variables.css` | CSS 变量定义 | 颜色、字体、间距、阴影、圆角、动画等设计标记 |
| `theme-override.css` | Element Plus 主题覆盖 | 将 Element Plus 默认蓝色替换为侗族品牌色 |
| `dong-patterns.css` | 侗族文化纹样 | 侗锦纹样背景、装饰图案 |
| `base.css` | 基础样式 | 全局 reset、排版、链接、图片、滚动条 |
| `common.css` | 通用工具类 | 布局、文字、间距、Flex 等 utility class |
| `components.css` | 组件样式 | 卡片、按钮、表单、弹窗等跨组件复用样式 |
| `pages.css` | 页面样式 | 各页面特定布局样式 |
| `home.css` | 首页样式 | 首页专用（Hero 区、导航卡片、精选区等） |
| `dialog-common.css` | 弹窗通用样式 | 详情弹窗、表单弹窗的共享样式 |
| `media-common.css` | 媒体通用样式 | 媒体组件（图片轮播、视频播放器）通用样式 |
| `Visual.css` | 可视化页样式 | 数据可视化页面专用布局 |
| `scss/_variables.scss` | SCSS 变量 | 全局注入的 SCSS 变量（颜色、断点等） |
| `scss/_mixins.scss` | SCSS 混合宏 | 全局注入的 SCSS mixins（flex-center、text-ellipsis 等） |

---

## 样式层级结构

`styles/index.css` 按以下顺序导入：

```
1. variables.css        -- 基础：CSS 设计标记
2. theme-override.css   -- 覆盖：Element Plus 主题
3. dong-patterns.css    -- 装饰：侗族纹样
4. base.css             -- 重置：全局 reset + 排版
5. components.css       -- 组件：复用组件样式
6. pages.css            -- 页面：页面级样式
7. media-common.css     -- 媒体：媒体组件样式
```

---

## variables.css -- CSS 设计标记

基于侗族传统文化色彩构建的设计系统：

### 品牌色系

```css
:root {
  /* 核心品牌色 */
  --dong-indigo: #1A5276;       /* 靛蓝 - 侗族传统服饰主色调 */
  --dong-indigo-dark: #0d3d5c;
  --dong-indigo-light: #2e7d9a;
  --dong-jade: #28B463;         /* 青绿 - 侗乡山水与草药 */
  --dong-jade-dark: #1e8e4a;
  --dong-jade-light: #58d68d;
  --dong-gold: #c9a227;         /* 金铜 - 非遗荣誉 */
  --dong-gold-light: #f5a623;
  --dong-copper: #b87333;

  /* 背景色系（传统纸张质感） */
  --bg-rice: #f8f5f0;           /* 暖米色全局背景 */
  --bg-rice-dark: #f0ebe3;
  --bg-paper: #faf8f5;
  --bg-cream: #fffdf9;

  /* 功能色 */
  --color-primary: var(--dong-indigo);
  --color-success: var(--dong-jade);
  --color-warning: var(--dong-gold-light);
  --color-danger: #e74c3c;
  --color-info: #3498db;
}
```

### 文字色

| 变量 | 值 | 用途 |
|------|-----|------|
| `--text-primary` | `#1a1a1a` | 正文 |
| `--text-secondary` | `#555` | 次要文字 |
| `--text-muted` | `#888` | 弱化文字 |
| `--text-light` | `#aaa` | 极浅文字 |
| `--text-inverse` | `#fff` | 反白文字（深色背景上） |

### 阴影系统（基于靛蓝色调）

| 变量 | 值 | 用途 |
|------|-----|------|
| `--shadow-xs` | `0 1px 4px rgba(26,82,118,0.04)` | 微阴影 |
| `--shadow-sm` | `0 2px 8px rgba(26,82,118,0.06)` | 小阴影 |
| `--shadow-md` | `0 4px 16px rgba(26,82,118,0.08)` | 中阴影 |
| `--shadow-lg` | `0 8px 32px rgba(26,82,118,0.12)` | 大阴影 |
| `--shadow-xl` | `0 16px 48px rgba(26,82,118,0.16)` | 超大阴影 |
| `--shadow-glow` | `0 0 40px rgba(40,180,99,0.15)` | 青绿发光 |

### 圆角系统

```css
--radius-xs: 4px; --radius-sm: 8px; --radius-md: 12px;
--radius-lg: 16px; --radius-xl: 20px; --radius-2xl: 24px; --radius-full: 9999px;
```

### 字体系统

```css
--font-display: "Noto Serif SC", "Source Han Serif SC", "宋体", serif;   /* 标题用衬线体 */
--font-body: "Noto Sans SC", "Source Han Sans SC", "Microsoft YaHei", sans-serif; /* 正文用无衬线 */
--font-mono: "JetBrains Mono", "Fira Code", monospace;                     /* 代码用等宽 */

--font-size-xs: 11px; --font-size-sm: 13px; --font-size-base: 14px;
--font-size-md: 16px; --font-size-lg: 18px; --font-size-xl: 20px;
--font-size-2xl: 24px; --font-size-3xl: 32px; --font-size-4xl: 42px;
```

### 动画系统

| 变量 | 值 | 用途 |
|------|-----|------|
| `--transition-fast` | `0.15s ease` | 微交互 |
| `--transition-normal` | `0.25s ease` | 常规过渡 |
| `--transition-slow` | `0.4s ease` | 慢速过渡 |
| `--transition-bounce` | `0.4s cubic-bezier(0.34, 1.56, 0.64, 1)` | 弹性过渡 |

### 布局系统

```css
--container-max: 1400px;    /* 内容最大宽度 */
--sidebar-width: 300px;     /* 侧边栏宽度 */
--header-height: 64px;      /* 顶部导航高度 */
```

### Z-Index 层级

```css
--z-dropdown: 100; --z-sticky: 200; --z-fixed: 300;
--z-modal-backdrop: 400; --z-modal: 500;
--z-popover: 600; --z-tooltip: 700;
```

---

## theme-override.css -- Element Plus 主题覆盖

将 Element Plus 组件库的默认蓝色系替换为侗族品牌色：

```css
:root {
  --el-color-primary: var(--dong-indigo);
  --el-color-success: var(--dong-jade);
  --el-color-warning: var(--dong-gold-light);
  --el-color-danger: #e74c3c;
  --el-color-info: #5a7d8a;

  --el-border-radius-base: var(--radius-sm);
  --el-font-family: var(--font-body);
}
```

还包括按钮（primary/success/warning/danger 的 hover/active 状态色）、标签、输入框、表格、分页等具体组件的样式微调覆盖。

---

## dong-patterns.css -- 侗族文化纹样

定义侗族传统织锦纹样作为 CSS 背景装饰图案：

- **菱形纹**（`--pattern-diamond`）：侗锦中最常见的菱形几何纹
- **波纹纹**（`--pattern-wave`）：象征山水
- **编织纹**（`--pattern-weave`）：十字交错编织纹理
- **太阳纹**（`--pattern-sun`）：侗族铜鼓上的太阳纹

通过 `background` `repeating-linear-gradient` 技术实现，无需加载外部图片资源。

---

## base.css -- 基础样式

- **全局重置**：`box-sizing: border-box`，margin/padding 清零
- **HTML/Body**：16px 字号，平滑滚动，抗锯齿渲染，全局背景使用 CSS 径向渐变（青绿 + 靛蓝微光 + 暖米底色）
- **标题排版**：使用 `--font-display` 衬线字体（`h1` 42px 到 `h6` 16px）
- **链接**：靛蓝色，hover 时变浅
- **响应式图片**：`max-width: 100%; height: auto`
- **自定义滚动条样式**：细窄滚动条（6px 宽），靛蓝色滑块
- **选中文本**：靛蓝色背景 + 白色文字
- **通用布局类**：`.module-page`（列表页容器）、`.module-header`（页面标题区，含背景装饰和底部渐变线）

---

## common.css -- 通用工具类

提供常用的 utility class：

- **布局**：`.flex-center`、`.flex-between`、`.grid-2col` 等
- **文字**：`.text-center`、`.text-muted`、`.text-ellipsis`（单行和多行）、`.text-inverse` 等
- **间距**：`.mt-{size}`、`.mb-{size}`、`.p-{size}` 系列（映射到 CSS 变量间距值）
- **显示**：`.hide-mobile`、`.show-mobile` 等响应式显隐

---

## components.css -- 组件样式

跨组件复用的样式：

- **卡片样式**：`.card-base`（基础卡片）、`.card-hover`（hover 上浮 translateY(-4px) + 阴影增强）
- **按钮变体**：`.btn-jade`（青绿色按钮）、`.btn-gold`（金色按钮）、`.btn-outline-indigo`（靛蓝描边按钮）等
- **标签变体**：`.tag-level-province`（省级传承人标签）等
- **空状态**：`.empty-state` 统一样式
- **统计卡片**：`.stat-card-base` 统计数值展示
- **加载状态**：`.loading-overlay` 遮罩层

---

## home.css -- 首页专用样式

首页特有的视觉样式：

- **Hero 区域**：靛蓝/青绿渐变背景 + 光晕效果（`hero-glow`），标题文字渐变色，统计数据卡片毛玻璃效果（`backdrop-filter: blur`）
- **快捷导航**：7 个模块卡片 staggered 入场动画（CSS `--delay` 自定义属性 + `@keyframes fadeInUp`）
- **每周精选**：大卡片 + 毛玻璃图片遮罩 + 悬浮信息层
- **最新更新**：列表条目 hover 左侧边框高亮效果

---

## pages.css -- 页面布局样式

各页面级别的布局样式：

- 模块页通用布局（`.module-page` 带 max-width 居中、`.module-header` 标题区块背景 + 渐变底线）
- 各页面特定容器（`.plants-container`、`.knowledge-container`、`.inheritor-grid` 等）
- 搜索区域样式（全局搜索大搜索框、各列表页搜索 + 筛选标签行）
- 详情弹窗内容区排版
- 管理后台侧边栏 + 内容区布局

---

## dialog-common.css -- 弹窗通用样式

详情弹窗和表单弹窗的共享样式：

- 详情内容排版（属性键值对、描述文本段落）
- 媒体区域（图片预览容器、视频容器）
- 底部操作栏（固定定位 + 背景渐变遮罩）
- 弹窗最大宽度 + 响应式适配（移动端全屏弹窗）

---

## media-common.css -- 媒体通用样式

- 图片轮播容器和缩略图导航（底部小圆点导航）
- 视频播放器控制栏（播放/暂停/进度条）
- 文档列表项展示（文件图标 + 文件名 + 操作按钮行）
- 文件类型图标统一样式

---

## Visual.css -- 数据可视化页样式

`Visual.vue` 页面专用：

- 统计卡片行布局（`display: grid; grid-template-columns: repeat(5, 1fr)`）
- 图表卡片网格布局（2 列、全宽）
- 图表切换控件位置（右上角绝对定位）
- 响应式断点处理（移动端单列布局）

---

## scss/ -- SCSS 辅助文件

### _variables.scss

SCSS 变量定义，与 `variables.css` 中的 CSS 变量对应，额外定义响应式断点：

```scss
// 品牌色 SCSS 变量
$dong-indigo: #1A5276;
$dong-jade: #28B463;
$dong-gold: #c9a227;

// 响应式断点
$breakpoint-sm: 576px;
$breakpoint-md: 768px;
$breakpoint-lg: 992px;
$breakpoint-xl: 1200px;
$breakpoint-xxl: 1400px;
```

### _mixins.scss

SCSS 混合宏，通过 Vite 配置**全局自动注入**到每个 `<style lang="scss">` 块：

```scss
@mixin flex-center { display: flex; align-items: center; justify-content: center; }
@mixin flex-between { display: flex; align-items: center; justify-content: space-between; }
@mixin text-ellipsis($lines: 1) { overflow: hidden; text-overflow: ellipsis; /* 单行 + 多行支持 */ }
@mixin card-hover { transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  &:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); } }
@mixin respond-to($breakpoint) { @media (max-width: $breakpoint) { @content; } }
```

**Vite 全局注入配置（`vite.config.js`）：**

```js
css: {
  preprocessorOptions: {
    scss: {
      additionalData: `@use "@/styles/scss/variables" as *;\n@use "@/styles/scss/mixins" as *;\n`,
      api: "modern-compiler",
    },
  },
},
```

这意味着在任何 `.vue` 文件的 `<style lang="scss">` 中都可以**直接使用** SCSS 变量和 mixins，无需手动 `@import`。

---

## 样式使用指南

### 引用 CSS 设计标记

```css
.my-card {
  background: var(--bg-paper);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  color: var(--text-primary);
  font-family: var(--font-body);
  transition: box-shadow var(--transition-normal);
}
.my-card:hover {
  box-shadow: var(--shadow-md);
}
```

### 使用 SCSS 变量和 mixins

```scss
<style lang="scss" scoped>
// _variables.scss 和 _mixins.scss 已全局注入，无需手动 import
.my-section {
  @include flex-center;
  padding: $space-xl;

  @include respond-to($breakpoint-md) {
    padding: $space-md;
  }
}

.my-title {
  color: $dong-indigo;
  @include text-ellipsis(2);
}
</style>
```

### 品牌色使用建议

| 场景 | 推荐变量 |
|------|---------|
| 主色/标题/链接 | `--dong-indigo` (#1A5276) |
| 成功/通过/药用植物 | `--dong-jade` (#28B463) |
| 强调/荣誉/特殊标记 | `--dong-gold` (#c9a227) |
| 页面背景 | `--bg-rice` (#f8f5f0) |
| 卡片背景 | `--bg-paper` (#faf8f5) |
| 正文 | `--text-primary` (#1a1a1a) |
| 辅助文字 | `--text-secondary` (#555) |
