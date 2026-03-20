# 样式文件目录

## 职责范围

统一管理项目所有样式文件，建立清晰的样式层级结构：

1. **变量集中管理**：颜色、字体、间距等变量统一定义
2. **样式分层**：按层级组织样式，避免冲突
3. **命名规范**：遵循BEM命名规范
4. **模块化**：样式按功能模块组织

## 目录结构

```
styles/
├── variables.css    # CSS变量定义
├── base.css         # 基础样式
├── components.css   # 组件样式
├── pages.css        # 页面样式
├── media-common.css # 媒体通用样式
├── common.css       # 原有通用样式
└── index.css        # 样式入口
```

## 样式层级

样式按以下顺序导入，确保优先级正确：

| 层级 | 文件 | 说明 |
|------|------|------|
| 1 | variables.css | CSS变量，最低优先级 |
| 2 | base.css | 基础样式，重置和通用布局 |
| 3 | components.css | 组件样式，通用组件外观 |
| 4 | pages.css | 页面样式，特定页面样式 |
| 5 | media-common.css | 媒体样式，媒体相关组件 |

## CSS变量使用

### 颜色变量

```css
/* 主色调 */
--dong-blue: #1A5276;
--dong-green: #28B463;
--dong-light: #f8f5f0;

/* 功能色 */
--dong-gold: #c9a227;
--dong-silver: #c0c0c0;

/* 文本颜色 */
--text-primary: #1a1a1a;
--text-secondary: #666;
--text-muted: #888;
```

### 布局变量

```css
/* 阴影 */
--shadow-sm: 0 2px 12px rgba(0, 0, 0, 0.06);
--shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
--shadow-lg: 0 8px 24px rgba(26, 82, 118, 0.12);

/* 圆角 */
--radius-sm: 8px;
--radius-md: 12px;
--radius-lg: 16px;

/* 间距 */
--gap-sm: 12px;
--gap-md: 16px;
--gap-lg: 24px;
```

### 动画变量

```css
--transition-fast: 0.2s ease;
--transition-normal: 0.3s ease;
--transition-slow: 0.4s ease;
```

## BEM命名规范

采用Block（块）、Element（元素）、Modifier（修饰符）命名方式：

```css
/* Block */
.card { }
.nav-card { }
.feature-card { }

/* Element */
.card__header { }
.card__title { }
.card__content { }

/* Modifier */
.card--featured { }
.card--compact { }
.btn--primary { }
.btn--secondary { }
```

## 使用规范

### 在组件中使用

```vue
<template>
  <div class="card">
    <div class="card__header">
      <h3 class="card__title">标题</h3>
    </div>
    <div class="card__content">
      内容
    </div>
  </div>
</template>

<style scoped>
.card {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: var(--gap-lg);
}
</style>
```

### 导入样式

```javascript
// 在main.js中统一导入
import './styles/index.css'

// 或在组件中按需导入
import '@/styles/variables.css'
```

## 新增样式流程

1. 确定样式类型（变量/基础/组件/页面）
2. 在对应文件中添加样式
3. 如需新变量，添加到variables.css
4. 更新index.css导入顺序（如需要）
