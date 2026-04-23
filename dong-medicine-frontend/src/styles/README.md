# 样式目录 (styles/)

> 类比：想象你在装修一家侗族文化餐厅。你需要决定墙壁刷什么颜色、桌椅用什么材质、灯光明暗如何、走道多宽。**CSS 就是装修方案**，而 styles 目录就是存放所有装修图纸的文件夹。

## 什么是 CSS？

CSS（层叠样式表，Cascading Style Sheets）是用来控制网页**外观和布局**的语言。HTML 决定页面"有什么内容"，CSS 决定内容"长什么样"。

```css
/* 选择器 { 属性: 值; } */
.card {
  background: white;        /* 背景色：白色 */
  border-radius: 16px;      /* 圆角：16像素 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);  /* 阴影 */
  padding: 24px;            /* 内边距：24像素 */
}
```

## 什么是 CSS 变量？

> 类比：想象一个调色板，上面贴着标签——"主色"贴在靛蓝色上、"成功色"贴在青绿色上。当你想换主色时，只需要把"主色"标签换到另一个颜色上，所有用了"主色"的地方都会自动更新。**CSS 变量就是这个标签**。

```css
/* 定义变量（在调色板上贴标签） */
:root {
  --dong-indigo: #1A5276;    /* 侗族靛蓝：主色 */
  --dong-jade: #28B463;      /* 侗族青绿：成功色 */
}

/* 使用变量（根据标签取颜色） */
.card-title {
  color: var(--dong-indigo);  /* 用标签引用，而不是写死 #1A5276 */
}

.btn-success {
  background: var(--dong-jade);  /* 如果将来青绿色要换，只改一处 */
}
```

**为什么不用直接写颜色值？** 因为项目中有上百个地方用了主色，如果某天要换主色调，没有变量的话你要改上百处，有了变量只需改 1 处。

---

## 设计系统 -- variables.css

这是整个项目样式的"根基"，定义了所有 CSS 变量。其他样式文件都依赖它。

### 侗族文化色彩体系

本项目的配色灵感来自侗族传统文化：

| 变量名 | 颜色值 | 文化含义 | 用途 |
|--------|--------|---------|------|
| `--dong-indigo` | `#1A5276` | 侗族传统服饰主色调，象征智慧与传承 | 主色、标题、导航 |
| `--dong-jade` | `#28B463` | 侗乡山水与草药，寓意生机与希望 | 成功提示、按钮 |
| `--dong-gold` | `#c9a227` | 非遗荣誉与匠人精神 | 警告提示、收藏按钮 |
| `--dong-copper` | `#b87333` | 铜器质感，体现古朴韵味 | 装饰元素 |

```css
:root {
  /* 核心品牌色 */
  --dong-indigo: #1A5276;
  --dong-indigo-dark: #0d3d5c;
  --dong-indigo-light: #2e7d9a;
  --dong-jade: #28B463;
  --dong-jade-dark: #1e8e4a;
  --dong-jade-light: #58d68d;
  --dong-gold: #c9a227;
  --dong-gold-light: #f5a623;
  --dong-copper: #b87333;
}
```

### 背景色系 -- 传统纸张质感

| 变量名 | 颜色值 | 用途 |
|--------|--------|------|
| `--bg-rice` | `#f8f5f0` | 页面主背景（米白色，像宣纸） |
| `--bg-rice-dark` | `#f0ebe3` | 滚动条轨道、悬停背景 |
| `--bg-paper` | `#faf8f5` | 内容区背景 |
| `--bg-cream` | `#fffdf9` | 卡片内背景 |

### 间距系统

```css
:root {
  --space-xs: 4px;    /* 极小间距：图标和文字之间 */
  --space-sm: 8px;    /* 小间距：同组元素之间 */
  --space-md: 12px;   /* 中间距：表单项之间 */
  --space-lg: 16px;   /* 大间距：卡片内边距 */
  --space-xl: 24px;   /* 超大间距：区块之间 */
  --space-2xl: 32px;  /* 区块外边距 */
  --space-3xl: 48px;  /* 页面级间距 */
  --space-4xl: 64px;  /* 最大间距 */
}
```

**为什么用 4px 倍数？** 这是设计界的黄金法则，4 的倍数让视觉节奏和谐统一，不会出现"差一点对不齐"的问题。

### 字体系统

```css
:root {
  /* 字体族 */
  --font-display: "Noto Serif SC", "宋体", serif;     /* 标题用衬线体，文化感 */
  --font-body: "Noto Sans SC", "Microsoft YaHei", sans-serif;  /* 正文用无衬线体，易读 */
  --font-mono: "JetBrains Mono", "Fira Code", monospace;  /* 代码用等宽字体 */

  /* 字号梯度 */
  --font-size-xs: 11px;    /* 辅助信息 */
  --font-size-sm: 13px;    /* 次要文字 */
  --font-size-base: 14px;  /* 正文（默认） */
  --font-size-md: 16px;    /* 小标题 */
  --font-size-lg: 18px;    /* 区块标题 */
  --font-size-xl: 20px;    /* 页面标题 */
  --font-size-2xl: 24px;   /* 大标题 */
  --font-size-3xl: 32px;   /* 超大标题 */
  --font-size-4xl: 42px;   /* 首页主标题 */
}
```

### 阴影系统

```css
:root {
  --shadow-xs: 0 1px 4px rgba(26, 82, 118, 0.04);    /* 几乎看不到，微妙的层次感 */
  --shadow-sm: 0 2px 8px rgba(26, 82, 118, 0.06);    /* 卡片默认阴影 */
  --shadow-md: 0 4px 16px rgba(26, 82, 118, 0.08);   /* 悬停时阴影 */
  --shadow-lg: 0 8px 32px rgba(26, 82, 118, 0.12);   /* 弹出层阴影 */
  --shadow-xl: 0 16px 48px rgba(26, 82, 118, 0.16);  /* 模态框阴影 */
  --shadow-glow: 0 0 40px rgba(40, 180, 99, 0.15);   /* 青绿发光效果 */
}
```

注意阴影颜色用的是 `rgba(26, 82, 118, ...)` -- 这是侗族靛蓝色的半透明版，而不是纯黑色，让阴影更柔和、更有品牌感。

### 圆角系统

```css
:root {
  --radius-xs: 4px;     /* 小标签 */
  --radius-sm: 8px;     /* 输入框 */
  --radius-md: 12px;    /* 按钮 */
  --radius-lg: 16px;    /* 卡片 */
  --radius-xl: 20px;    /* 大卡片 */
  --radius-2xl: 24px;   /* 弹窗 */
  --radius-full: 9999px; /* 完全圆形（头像、标签） */
}
```

### 动画系统

```css
:root {
  --transition-fast: 0.15s ease;           /* 快速反馈：按钮悬停 */
  --transition-normal: 0.25s ease;         /* 标准过渡：卡片悬停 */
  --transition-slow: 0.4s ease;            /* 慢速过渡：页面切换 */
  --transition-bounce: 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);  /* 弹跳效果 */
}
```

### Z-Index 层级

```css
:root {
  --z-dropdown: 100;        /* 下拉菜单 */
  --z-sticky: 200;          /* 粘性定位 */
  --z-fixed: 300;           /* 固定定位 */
  --z-modal-backdrop: 400;  /* 遮罩层 */
  --z-modal: 500;           /* 弹窗 */
  --z-popover: 600;         /* 气泡提示 */
  --z-tooltip: 700;         /* 工具提示（最上层） */
}
```

**为什么从 100 开始？** 避免和 Element Plus 等第三方库的 z-index 冲突，留出空间。

---

## 如何在组件中使用 CSS 变量

### 在 `<style>` 中使用

```vue
<template>
  <div class="plant-card">
    <h3 class="plant-name">钩藤</h3>
    <p class="plant-desc">清热平肝，息风定惊</p>
  </div>
</template>

<style scoped>
.plant-card {
  /* 使用变量而不是硬编码 */
  background: var(--bg-cream);              /* 米白背景 */
  border-radius: var(--radius-lg);          /* 大圆角 */
  padding: var(--space-xl);                 /* 大内边距 */
  box-shadow: var(--shadow-sm);             /* 小阴影 */
  transition: all var(--transition-normal); /* 标准过渡 */
}

.plant-card:hover {
  box-shadow: var(--shadow-lg);             /* 悬停时大阴影 */
  transform: translateY(-4px);              /* 上浮效果 */
}

.plant-name {
  color: var(--dong-indigo);                /* 侗族靛蓝标题 */
  font-family: var(--font-display);         /* 衬线字体 */
  font-size: var(--font-size-lg);           /* 大字号 */
  margin-bottom: var(--space-sm);           /* 小下边距 */
}

.plant-desc {
  color: var(--text-muted);                 /* 灰色描述文字 */
  font-size: var(--font-size-sm);           /* 小字号 */
  line-height: var(--line-height-normal);   /* 标准行高 */
}
</style>
```

### 在 JavaScript 中使用

```javascript
// 读取 CSS 变量的值
const rootStyles = getComputedStyle(document.documentElement)
const primaryColor = rootStyles.getPropertyValue('--dong-indigo')  // "#1A5276"

// 动态修改 CSS 变量（比如切换主题色）
document.documentElement.style.setProperty('--dong-indigo', '#2c3e50')
```

---

## 文件清单与职责

| 文件 | 加载顺序 | 职责 | 说明 |
|------|---------|------|------|
| `variables.css` | 第 1 个 | CSS 变量定义 | 颜色、字体、间距、阴影等所有设计令牌 |
| `base.css` | 第 2 个 | 基础样式 | 全局重置、排版、布局工具类、动画关键帧 |
| `components.css` | 第 3 个 | 组件样式 | 卡片、按钮、标签、表单等通用组件样式 |
| `pages.css` | 第 4 个 | 页面样式 | 各页面特定的布局和样式 |
| `media-common.css` | 第 5 个 | 媒体样式 | 图片、视频、文档等媒体相关样式 |
| `common.css` | 额外 | 通用补充 | 模块页面布局、卡片、徽章等补充样式 |
| `dialog-common.css` | 额外 | 弹窗样式 | 对话框通用样式 |
| `home.css` | 额外 | 首页样式 | 首页专属样式 |
| `Visual.css` | 额外 | 可视化样式 | 数据可视化页面专属样式 |
| `index.css` | 入口 | 统一导入 | 按顺序导入上述核心文件 |

### 加载顺序为什么重要？

CSS 的规则是"后写的覆盖先写的"，所以加载顺序决定了优先级：

```
variables.css  -->  base.css  -->  components.css  -->  pages.css
   (变量定义)      (基础重置)     (组件样式)         (页面样式，优先级最高)
```

这就像装修的顺序：先确定材料（variables），再打地基（base），然后装家具（components），最后挂画（pages）。

---

## 如何添加新样式

### 原则 1：优先使用 CSS 变量

```css
/* 不好：硬编码颜色值 */
.my-card {
  background: #f8f5f0;
  border-radius: 16px;
  color: #1A5276;
}

/* 好：使用 CSS 变量 */
.my-card {
  background: var(--bg-rice);
  border-radius: var(--radius-lg);
  color: var(--dong-indigo);
}
```

### 原则 2：组件样式用 scoped

```vue
<!-- 好：scoped 限制样式只作用于当前组件 -->
<style scoped>
.plant-card { ... }
</style>

<!-- 不好：全局样式可能污染其他组件 -->
<style>
.plant-card { ... }
</style>
```

### 原则 3：通用样式放 styles/，组件专属放组件内

```
通用样式（多个组件都用）  -->  styles/components.css
组件专属样式（只有自己用） -->  组件内的 <style scoped>
页面专属样式（只有某页面用） -->  styles/pages.css 或页面组件内
```

### 原则 4：新增 CSS 变量时，加在 variables.css

```css
/* 在 variables.css 的 :root 中添加新变量 */
:root {
  /* ... 已有变量 ... */

  /* 新增：药浴主题色 */
  --dong-herbal: #8B7355;
  --dong-herbal-light: #C4A87C;
}
```

---

## 常见错误

### 错误 1：不使用 CSS 变量，到处写死颜色

```css
/* 错误：如果将来要换主色，要改几十个地方 */
.card-title { color: #1A5276; }
.btn-primary { background: #1A5276; }
.nav-link { color: #1A5276; }

/* 正确：只改 variables.css 一处即可 */
.card-title { color: var(--dong-indigo); }
.btn-primary { background: var(--dong-indigo); }
.nav-link { color: var(--dong-indigo); }
```

### 错误 2：用 !important 强制覆盖

```css
/* 错误：!important 会破坏 CSS 优先级规则，后期难以维护 */
.my-button { color: white !important; }

/* 正确：通过提高选择器特异性来覆盖 */
.card .my-button { color: white; }
/* 或者检查为什么被覆盖，从根本上解决 */
```

### 错误 3：在全局样式中写太具体的选择器

```css
/* 错误：全局样式太具体，可能影响其他页面 */
.views .plants .card .title { font-size: 20px; }

/* 正确：使用通用的 CSS 类名 */
.card-title { font-size: var(--font-size-xl); }
```

### 错误 4：忘记考虑响应式

```css
/* 错误：只考虑了桌面端，手机上可能溢出 */
.sidebar {
  width: 300px;
  float: right;
}

/* 正确：加上响应式断点 */
.sidebar {
  width: 300px;
}

@media (max-width: 1024px) {
  .sidebar {
    width: 100%;  /* 手机上占满宽度 */
  }
}
```
