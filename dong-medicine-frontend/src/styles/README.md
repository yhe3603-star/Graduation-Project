# 样式文件目录 (styles)

本目录存放全局样式文件，包括CSS变量、基础样式、组件样式等。

## 📁 文件列表

| 文件名 | 功能说明 |
|--------|----------|
| `index.css` | 样式入口文件，统一导入所有样式 |
| `variables.css` | CSS变量定义文件 |
| `base.css` | 基础样式文件 |
| `common.css` | 通用样式文件 |
| `components.css` | 组件样式文件 |
| `pages.css` | 页面样式文件 |
| `home.css` | 首页样式文件 |
| `Visual.css` | 数据可视化页面样式 |
| `dialog-common.css` | 对话框通用样式 |
| `media-common.css` | 媒体组件通用样式 |

## 📦 详细说明

### 1. variables.css - CSS变量定义

定义全局CSS变量，包括颜色、字体、间距等。

**主要变量:**
```css
:root {
  /* 主题色 */
  --primary-color: #1A5276;
  --secondary-color: #28B463;
  
  /* 文字颜色 */
  --text-primary: #333333;
  --text-secondary: #666666;
  --text-muted: #999999;
  
  /* 背景色 */
  --bg-primary: #ffffff;
  --bg-secondary: #f5f5f5;
  
  /* 边框 */
  --border-color: #e0e0e0;
  --border-radius: 8px;
  
  /* 间距 */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  
  /* 字体 */
  --font-family: 'Microsoft YaHei', sans-serif;
  --font-size-sm: 12px;
  --font-size-base: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 18px;
}
```

### 2. base.css - 基础样式

定义HTML元素的基础样式。

**主要内容:**
- 重置样式 (Reset CSS)
- 盒模型设置
- 字体设置
- 链接样式
- 滚动条样式

### 3. common.css - 通用样式

定义通用的CSS类。

**主要类名:**
```css
/* 布局 */
.flex { display: flex; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }

/* 文字 */
.text-center { text-align: center; }
.text-ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 间距 */
.mt-sm { margin-top: var(--space-sm); }
.mb-md { margin-bottom: var(--space-md); }
.p-lg { padding: var(--space-lg); }
```

### 4. components.css - 组件样式

定义全局组件的样式。

**主要组件:**
- 卡片样式
- 按钮样式
- 表单样式
- 标签样式

### 5. pages.css - 页面样式

定义页面级别的通用样式。

**主要样式:**
- 页面容器
- 模块头部
- 内容区域

### 6. home.css - 首页样式

首页专用样式。

**主要内容:**
- Hero区域
- 统计卡片
- 导航入口
- 特色模块

### 7. Visual.css - 数据可视化样式

数据可视化页面专用样式。

**主要内容:**
- 图表容器
- 统计卡片
- 数据表格

### 8. dialog-common.css - 对话框样式

对话框组件通用样式。

**主要内容:**
- 对话框容器
- 标题区域
- 内容区域
- 底部按钮

### 9. media-common.css - 媒体样式

媒体组件通用样式。

**主要内容:**
- 图片容器
- 视频播放器
- 文档预览

## 🎯 使用规范

### 导入顺序
```css
/* 在 index.css 中按顺序导入 */
@import './variables.css';  /* 1. 变量定义 */
@import './base.css';       /* 2. 基础样式 */
@import './common.css';     /* 3. 通用样式 */
@import './components.css'; /* 4. 组件样式 */
@import './pages.css';      /* 5. 页面样式 */
```

### 使用CSS变量
```css
.my-component {
  color: var(--text-primary);
  background: var(--bg-primary);
  padding: var(--space-md);
  border-radius: var(--border-radius);
}
```

### 命名规范
- **类名**: 使用小写字母和连字符，如 `.card-container`
- **变量**: 使用小写字母和连字符，如 `--primary-color`
- **BEM命名**: 块__元素--修饰符，如 `.card__title--large`

### 最佳实践
1. **使用变量**: 尽量使用CSS变量，便于主题切换
2. **避免嵌套**: CSS选择器嵌套不超过3层
3. **模块化**: 每个页面/组件的样式独立管理
4. **响应式**: 使用媒体查询适配不同屏幕

## 📚 扩展阅读

- [CSS 变量](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Using_CSS_custom_properties)
- [BEM 命名规范](https://getbem.com/)
- [CSS 最佳实践](https://developer.mozilla.org/zh-CN/docs/Web/Guide/CSS/Writing_efficient_CSS)
