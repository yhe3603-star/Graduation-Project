# Styles 样式文件目录

本目录包含项目的全局样式文件。

## 目录结构

```
styles/
├── index.css             # 样式入口文件
├── variables.css         # CSS 变量定义
├── base.css              # 基础样式
├── components.css        # 组件样式
├── pages.css             # 页面样式
├── common.css            # 通用样式
├── home.css              # 首页专用样式
├── media-common.css      # 媒体相关通用样式
├── dialog-common.css     # 对话框通用样式
└── README.md             # 说明文档
```

## 样式文件说明

### variables.css - CSS 变量定义

**用途**: 定义全局 CSS 变量，构建设计系统

**品牌色（侗族文化特色）**:
```css
:root {
  --dong-indigo: #1A5276;        /* 靛蓝 - 主色调 */
  --dong-jade: #28B463;          /* 青绿 - 辅助色 */
  --dong-gold: #c9a227;          /* 金铜 - 强调色 */
}
```

**功能色**:
```css
:root {
  --color-primary: #1A5276;      /* 主色 */
  --color-success: #28B463;      /* 成功 */
  --color-warning: #f5a623;      /* 警告 */
  --color-danger: #e74c3c;       /* 危险 */
  --color-info: #3498db;         /* 信息 */
}
```

**间距系统**:
```css
:root {
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
  --space-2xl: 32px;
}
```

**圆角系统**:
```css
:root {
  --radius-xs: 4px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-full: 9999px;
}
```

**阴影系统**:
```css
:root {
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 8px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 16px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 12px 24px rgba(0, 0, 0, 0.15);
}
```

---

### base.css - 基础样式

**用途**: 重置样式、排版、通用布局

**包含内容**:
- CSS Reset
- 字体设置
- 全局盒模型
- 滚动条样式
- 选中文本样式

---

### components.css - 组件样式

**用途**: 组件通用样式

**包含组件**:
- 卡片样式
- 按钮样式
- 表单样式
- 标签样式
- 分页样式

---

### pages.css - 页面样式

**用途**: 各页面通用样式

**包含内容**:
- 页面容器
- 页面标题
- 页面布局
- 响应式适配

---

### common.css - 通用样式

**用途**: 通用工具类样式

**包含内容**:
- 文本工具类
- 间距工具类
- 显示工具类
- 动画工具类

---

### home.css - 首页专用样式

**用途**: 首页特定样式

**包含内容**:
- 首页布局
- 统计卡片
- 导航卡片
- 轮播样式

---

### media-common.css - 媒体相关通用样式

**用途**: 媒体组件通用样式

**包含内容**:
- 图片容器
- 视频播放器
- 文档预览
- 媒体列表

---

### dialog-common.css - 对话框通用样式

**用途**: 对话框组件通用样式

**包含内容**:
- 对话框容器
- 对话框头部
- 对话框内容
- 对话框底部
- 对话框动画

---

## 开发规范

1. **命名规范**: 使用小写字母，多个单词用连字符连接
2. **变量使用**: 优先使用 CSS 变量
3. **响应式**: 使用媒体查询适配不同屏幕
4. **命名空间**: 组件样式使用 `.dm-` 前缀

---

## 已知限制

| 限制 | 影响 |
|------|------|
| 不支持CSS Modules | 样式隔离依赖scoped |
| 不支持暗色主题 | 无深色模式切换 |
| 不支持RTL | 不适配从右到左语言 |
| 无响应式断点变量 | 媒体查询硬编码 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **主题系统**
   - 添加暗色主题
   - 实现主题切换
   - 添加主题持久化

2. **响应式优化**
   - 定义断点变量
   - 优化移动端适配

### 中期改进 (1-2月)

1. **CSS架构**
   - 采用CSS Modules
   - 实现原子化CSS
   - 添加样式lint

2. **国际化支持**
   - 支持RTL布局
   - 多语言字体适配

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| PostCSS | 8.x | CSS处理 |
| Autoprefixer | 10.x | 自动前缀 |

---

## 常见问题

### 1. 如何使用CSS变量？

```css
/* 定义变量 */
:root {
  --my-color: #1A5276;
}

/* 使用变量 */
.my-element {
  color: var(--my-color);
  background: var(--color-primary, #1A5276);  /* 带默认值 */
}
```

### 2. 如何覆盖Element Plus样式？

```css
/* 使用:deep()穿透 */
:deep(.el-button) {
  --el-button-bg-color: var(--dong-indigo);
}

/* 或使用全局样式 */
.el-button--primary {
  background-color: var(--dong-indigo);
}
```

### 3. 如何实现响应式布局？

```css
/* 使用媒体查询 */
.container {
  padding: var(--space-md);
}

@media (max-width: 768px) {
  .container {
    padding: var(--space-sm);
  }
}

/* 使用CSS Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-lg);
}
```

### 4. 如何添加动画？

```css
/* 定义动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 使用动画 */
.fade-enter {
  animation: fadeIn 0.3s ease;
}
```

---

**最后更新时间**：2026年3月28日
