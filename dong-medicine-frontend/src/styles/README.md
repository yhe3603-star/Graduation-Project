# 样式文件目录说明

## 文件夹结构

本目录包含项目的样式文件，使用CSS变量和模块化的样式管理。

```
styles/
├── base.css          # 基础样式
├── common.css        # 通用样式
├── components.css    # 组件样式
├── dialog-common.css # 对话框通用样式
├── home.css          # 首页样式（新增）
├── index.css         # 样式入口
├── media-common.css  # 媒体相关样式
├── pages.css         # 页面样式
├── variables.css     # CSS变量定义
└── README.md         # 样式说明文档
```

## 详细说明

### 1. variables.css

**功能**：定义项目中使用的CSS变量，包括颜色、字体、间距等。

**主要变量分类**：

**颜色变量**：
- `--dong-indigo` / `--dong-indigo-dark`：侗乡靛蓝主题色
- `--dong-jade` / `--dong-jade-dark`：侗乡玉绿强调色
- `--dong-gold` / `--dong-gold-light`：侗乡金色点缀
- `--text-primary` / `--text-secondary` / `--text-muted`：文字颜色
- `--text-inverse`：反色文字
- `--bg-rice` / `--bg-rice-dark`：米色背景

**字体变量**：
- `--font-display`：展示字体
- `--font-size-xs` ~ `--font-size-4xl`：字体大小
- `--font-weight-normal` / `--font-weight-medium` / `--font-weight-semibold` / `--font-weight-bold`：字重

**间距变量**：
- `--space-xs` ~ `--space-4xl`：间距大小

**圆角变量**：
- `--radius-xs` ~ `--radius-2xl` / `--radius-full`：圆角大小

**阴影变量**：
- `--shadow-sm` / `--shadow-md` / `--shadow-lg` / `--shadow-xl`：阴影
- `--shadow-glow`：发光效果

**过渡变量**：
- `--transition-fast` / `--transition-normal` / `--transition-slow`：过渡时间

**布局变量**：
- `--container-max`：最大容器宽度
- `--sidebar-width`：侧边栏宽度

### 2. base.css

**功能**：基础样式重置和全局样式设置。

**主要内容**：
- 浏览器样式重置
- 全局字体设置
- 基础元素样式
- 滚动条样式

### 3. common.css

**功能**：通用样式类，可在多个组件中复用。

**主要样式类**：
- `.module-page`：模块页面容器
- `.module-header`：模块页面头部
- `.card-stats`：卡片统计信息
- `.stat-item`：统计项

### 4. components.css

**功能**：组件通用样式。

**主要内容**：
- Element Plus组件样式覆盖
- 自定义组件样式

### 5. dialog-common.css

**功能**：对话框通用样式。

**主要内容**：
- 对话框基础样式
- 对话框内容布局
- 详情描述列表样式

### 6. home.css（新增）

**功能**：首页独立样式文件。

**主要内容**：
- Hero区域样式（背景、动画、统计卡片）
- 快捷导航卡片样式
- 功能模块卡片样式
- 传承人卡片样式
- 拓展功能卡片样式
- CTA区域样式
- 响应式媒体查询

**代码行数**：约885行

### 7. media-common.css

**功能**：媒体相关样式。

**主要内容**：
- 图片样式
- 视频样式
- 文档样式

### 8. pages.css

**功能**：页面特定样式。

**主要内容**：
- 各页面的特定样式

### 9. index.css

**功能**：样式文件入口，导入所有样式文件。

**导入顺序**：
1. variables.css（变量定义）
2. base.css（基础样式）
3. common.css（通用样式）
4. components.css（组件样式）
5. dialog-common.css（对话框样式）
6. media-common.css（媒体样式）
7. pages.css（页面样式）

## 样式文件统计

| 文件 | 行数 | 主要用途 |
|------|------|---------|
| variables.css | ~150 | CSS变量定义 |
| base.css | ~100 | 基础样式重置 |
| common.css | ~200 | 通用样式类 |
| components.css | ~150 | 组件样式 |
| dialog-common.css | ~100 | 对话框样式 |
| media-common.css | ~80 | 媒体样式 |
| pages.css | ~300 | 页面样式 |
| home.css | ~885 | 首页样式 |
| index.css | ~20 | 样式入口 |

## CSS变量使用示例

```css
/* 使用主题色 */
.button {
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  color: var(--text-inverse);
}

/* 使用间距和圆角 */
.card {
  padding: var(--space-xl);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

/* 使用过渡 */
.button:hover {
  transform: translateY(-2px);
  transition: all var(--transition-normal);
}
```

## 响应式断点

| 断点 | 描述 | 媒体查询 |
|------|------|----------|
| xs | 超小屏幕 | < 480px |
| sm | 小屏幕 | < 768px |
| md | 中等屏幕 | < 1024px |
| lg | 大屏幕 | < 1200px |

## 开发规范

1. **命名规范**：使用BEM（Block, Element, Modifier）命名规范
2. **模块化**：按功能和组件划分样式文件
3. **变量使用**：优先使用CSS变量，避免硬编码
4. **响应式**：使用媒体查询实现响应式设计
5. **性能优化**：避免使用复杂的选择器

## 注意事项

- 所有样式都应该使用CSS变量，便于主题定制
- 避免使用!important，优先通过选择器优先级解决样式冲突
- 对于重复使用的样式，应该提取为通用类
- 注意样式的继承和层叠，避免不必要的样式覆盖
- 定期清理未使用的样式，保持样式文件的简洁

---

**最后更新时间**：2026年3月25日
