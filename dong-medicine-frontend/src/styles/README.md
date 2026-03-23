# 样式文件目录说明

## 文件夹结构

本目录包含项目的样式文件，使用CSS变量和模块化的样式管理。

```
styles/
├── base.css          # 基础样式
├── common.css        # 通用样式
├── components.css    # 组件样式
├── dialog-common.css # 对话框通用样式
├── index.css         # 样式入口
├── media-common.css  # 媒体相关样式
├── pages.css         # 页面样式
├── variables.css     # CSS变量定义
└── README.md         # 样式说明文档
```

## 详细说明

### 1. variables.css

**功能**：定义项目中使用的CSS变量，包括颜色、字体、间距等。

**主要变量**：
- **颜色变量**：主题色、文字颜色、背景颜色等
- **字体变量**：字体大小、字重、行高等
- **间距变量**：内边距、外边距、间距等
- **边框变量**：边框宽度、圆角等
- **动画变量**：动画时长、缓动函数等

**使用方法**：

```css
/* 在其他样式文件中使用 */
.button {
  background-color: var(--primary-color);
  color: var(--white);
  padding: var(--padding-sm) var(--padding-md);
  border-radius: var(--border-radius);
  font-size: var(--font-size-md);
}
```

### 2. base.css

**功能**：基础样式重置和全局样式设置。

**主要内容**：
- 浏览器样式重置
- 全局字体设置
- 基础元素样式
- 通用选择器样式

### 3. common.css

**功能**：通用样式类，可在多个组件中复用。

**主要样式类**：
- 布局类：flex布局、grid布局等
- 文本类：文本对齐、文本颜色等
- 间距类：margin、padding等
- 显示类：display、visibility等

### 4. components.css

**功能**：组件通用样式。

**主要内容**：
- 按钮样式
- 表单元素样式
- 卡片样式
- 列表样式
- 其他通用组件样式

### 5. dialog-common.css

**功能**：对话框通用样式。

**主要内容**：
- 对话框基础样式
- 对话框标题样式
- 对话框内容样式
- 对话框按钮样式

### 6. media-common.css

**功能**：媒体相关样式。

**主要内容**：
- 图片样式
- 视频样式
- 文档样式
- 媒体容器样式

### 7. pages.css

**功能**：页面特定样式。

**主要内容**：
- 首页样式
- 药材页面样式
- 传承人页面样式
- 知识库页面样式
- 其他页面样式

### 8. index.css

**功能**：样式文件入口，导入所有样式文件。

**导入顺序**：
1. variables.css（变量定义）
2. base.css（基础样式）
3. common.css（通用样式）
4. components.css（组件样式）
5. dialog-common.css（对话框样式）
6. media-common.css（媒体样式）
7. pages.css（页面样式）

**使用方法**：

在 `main.js` 中导入：

```javascript
import './styles/index.css'
```

## CSS变量参考

### 颜色变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| --primary-color | 主题色 | #1890ff |
| --secondary-color | 次要色 | #52c41a |
| --danger-color | 危险色 | #ff4d4f |
| --warning-color | 警告色 | #faad14 |
| --info-color | 信息色 | #1890ff |
| --success-color | 成功色 | #52c41a |
| --text-primary | 主要文字颜色 | #333333 |
| --text-secondary | 次要文字颜色 | #666666 |
| --text-tertiary | 第三级文字颜色 | #999999 |
| --bg-primary | 主要背景色 | #ffffff |
| --bg-secondary | 次要背景色 | #f5f5f5 |
| --border-color | 边框颜色 | #e8e8e8 |

### 字体变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| --font-family | 字体家族 | 'PingFang SC', 'Helvetica Neue', Arial, sans-serif |
| --font-size-xs | 极小字体 | 12px |
| --font-size-sm | 小字体 | 14px |
| --font-size-md | 中等字体 | 16px |
| --font-size-lg | 大字体 | 18px |
| --font-size-xl | 超大字体 | 20px |
| --font-size-xxl | 特大字体 | 24px |
| --font-weight-normal | 正常字重 | 400 |
| --font-weight-medium | 中等字重 | 500 |
| --font-weight-bold | 粗体 | 600 |

### 间距变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| --margin-xs | 极小间距 | 4px |
| --margin-sm | 小间距 | 8px |
| --margin-md | 中等间距 | 16px |
| --margin-lg | 大间距 | 24px |
| --margin-xl | 超大间距 | 32px |
| --padding-xs | 极小内边距 | 4px |
| --padding-sm | 小内边距 | 8px |
| --padding-md | 中等内边距 | 16px |
| --padding-lg | 大内边距 | 24px |
| --padding-xl | 超大内边距 | 32px |

### 边框变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| --border-width | 边框宽度 | 1px |
| --border-radius-sm | 小圆角 | 4px |
| --border-radius | 中等圆角 | 8px |
| --border-radius-lg | 大圆角 | 12px |

### 动画变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| --transition-fast | 快速过渡 | 0.2s |
| --transition-normal | 正常过渡 | 0.3s |
| --transition-slow | 慢速过渡 | 0.5s |
| --ease-in-out | 缓入缓出 | cubic-bezier(0.4, 0, 0.2, 1) |

## 样式开发规范

1. **命名规范**：使用BEM（Block, Element, Modifier）命名规范
2. **模块化**：按功能和组件划分样式文件
3. **变量使用**：优先使用CSS变量，避免硬编码
4. **响应式**：使用媒体查询实现响应式设计
5. **性能优化**：避免使用复杂的选择器，减少样式计算

## 响应式断点

| 断点 | 描述 | 媒体查询 |
|------|------|----------|
| xs | 超小屏幕 | < 576px |
| sm | 小屏幕 | ≥ 576px |
| md | 中等屏幕 | ≥ 768px |
| lg | 大屏幕 | ≥ 992px |
| xl | 超大屏幕 | ≥ 1200px |
| xxl | 特大屏幕 | ≥ 1600px |

**使用示例**：

```css
/* 响应式样式 */
@media (max-width: 768px) {
  .container {
    padding: var(--padding-sm);
  }
  
  .title {
    font-size: var(--font-size-lg);
  }
}
```

## 注意事项

- 所有样式都应该使用CSS变量，便于主题定制
- 避免使用!important，优先通过选择器优先级解决样式冲突
- 对于重复使用的样式，应该提取为通用类
- 注意样式的继承和层叠，避免不必要的样式覆盖
- 定期清理未使用的样式，保持样式文件的简洁

---

**最后更新时间**：2026年3月23日