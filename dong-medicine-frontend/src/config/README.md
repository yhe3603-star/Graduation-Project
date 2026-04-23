# 页面配置目录 (config/)

> 类比：餐厅的菜单本。菜单本决定了展示哪些菜品、怎么分类、每道菜叫什么名字。配置文件也一样，它决定了页面展示哪些模块、怎么排列、每个模块叫什么。

---

## 一、什么是配置？

在开发中，**配置（Config）** 就是把"可能会变的东西"从代码里抽出来，放到单独的文件中管理。

这样做的好处：

| 不用配置 | 用配置 |
|----------|--------|
| 首页要加一个模块？去 Home.vue 里翻代码改 | 首页要加一个模块？在配置文件加一行 |
| 改个标题要找半天在哪个组件里 | 改个标题直接在配置文件里改 |
| 不同页面的数据散落在各处 | 所有页面数据集中在 config 目录 |

**一句话总结**：配置 = 把"数据"和"展示"分开。组件只负责"怎么展示"，配置负责"展示什么"。

---

## 二、文件列表

目前 config 目录只有一个文件：

| 文件 | 职责 |
|------|------|
| `homeConfig.js` | 首页的导航入口、模块卡片、统计数据等配置 |

---

## 三、homeConfig.js 详解

这个文件是首页的"菜单本"，决定了首页上展示哪些内容。我们来逐个拆解。

### 3.1 quickEntries - 快速导航卡片（4 个）

首页顶部的 4 个彩色小卡片，用户点击可以快速跳转到对应页面。

```javascript
import { Document, User, Picture, ChatDotRound } from '@element-plus/icons-vue'

export const quickEntries = [
  { title: '知识库', desc: '理论知识体系', path: '/knowledge',
    color: 'linear-gradient(135deg, #1A5276, var(--dong-indigo-dark))', icon: Document },
  { title: '传承人', desc: '非遗传承档案', path: '/inheritors',
    color: 'linear-gradient(135deg, #28B463, var(--dong-jade-dark))', icon: User },
  { title: '药用图鉴', desc: '道地药材详解', path: '/plants',
    color: 'linear-gradient(135deg, var(--dong-gold-light), #e8930c)', icon: Picture },
  { title: '问答社区', desc: '互动答疑解惑', path: '/qa',
    color: 'linear-gradient(135deg, #667eea, #764ba2)', icon: ChatDotRound }
]
```

**每个字段的作用**：

| 字段 | 类型 | 作用 | 示例 |
|------|------|------|------|
| `title` | String | 卡片标题 | `'知识库'` |
| `desc` | String | 卡片描述 | `'理论知识体系'` |
| `path` | String | 点击跳转的路由路径 | `'/knowledge'` |
| `color` | String | 卡片背景渐变色 | `'linear-gradient(135deg, #1A5276, ...)'` |
| `icon` | Component | Element Plus 图标组件 | `Document` |

**在组件中使用**：

```html
<template>
  <div class="quick-entries">
    <!-- 遍历配置数组，渲染每个导航卡片 -->
    <router-link
      v-for="entry in quickEntries"
      :key="entry.title"
      :to="entry.path"
      class="entry-card"
      :style="{ background: entry.color }"
    >
      <el-icon><component :is="entry.icon" /></el-icon>
      <h3>{{ entry.title }}</h3>
      <p>{{ entry.desc }}</p>
    </router-link>
  </div>
</template>

<script setup>
import { quickEntries } from '@/config/homeConfig'
</script>
```

---

### 3.2 coreModules - 核心模块卡片（4 个）

首页中部的 4 个大卡片，比 quickEntries 多了描述文字和数据计数。

```javascript
export const coreModules = [
  { title: '知识库',
    desc: '系统整理侗医药理论知识，包含病因病机、诊断方法、治疗原则等内容',
    path: '/knowledge', count: '50+ 条目',
    bgColor: 'linear-gradient(135deg, #1A5276, var(--dong-indigo-dark))', icon: Document },
  // ... 其他 3 个模块
]
```

**和 quickEntries 的区别**：

| 对比 | quickEntries | coreModules |
|------|-------------|-------------|
| 位置 | 页面顶部 | 页面中部 |
| 大小 | 小卡片 | 大卡片 |
| 额外字段 | 无 | `desc`（详细描述）、`count`（数据量） |
| 字段名差异 | `color` | `bgColor` |

**为什么字段名不一样？** 因为这是两个不同的设计师/开发者写的，后来合并到了一起。实际项目中这种情况很常见，不用纠结。

---

### 3.3 extendModules - 扩展模块卡片（3 个）

首页底部的 3 个模块，是核心功能的补充。

```javascript
export const extendModules = [
  { title: '文化互动', desc: '趣味答题、植物识别游戏，寓教于乐',
    path: '/interact', icon: Aim },
  { title: '学习资源', desc: '视频教程、文档资料，深入学习',
    path: '/resources', icon: Folder },
  { title: '数据可视化', desc: '统计分析，直观展示平台数据',
    path: '/visual', icon: DataLine }
]
```

**和 coreModules 的区别**：没有 `count` 和 `bgColor` 字段，样式更简洁。

---

### 3.4 levelClassMap - 传承人级别 CSS 类映射

把传承人的级别文字映射成 CSS 类名，方便在模板中动态设置样式。

```javascript
export const levelClassMap = {
  '省级': 'level-gold',       // 金色标签
  '自治区级': 'level-green',   // 绿色标签
  '州级': 'level-purple'      // 紫色标签
}

// 工具函数：获取级别对应的 CSS 类名，找不到就用默认的 level-blue
export const getLevelClass = (level) => levelClassMap[level] || 'level-blue'
```

**使用示例**：

```html
<template>
  <!-- 传承人卡片上的级别标签 -->
  <el-tag :class="getLevelClass(inheritor.level)">
    {{ inheritor.level }}
  </el-tag>
</template>

<script setup>
import { getLevelClass } from '@/config/homeConfig'

const inheritor = { name: '吴老师', level: '省级' }
// getLevelClass('省级') 返回 'level-gold'
// getLevelClass('国家级') 返回 'level-blue'（默认值）
</script>
```

**为什么用映射而不是 if-else？**

```javascript
// 不好的写法：每加一个级别就要改代码
if (level === '省级') return 'level-gold'
else if (level === '自治区级') return 'level-green'
else if (level === '州级') return 'level-purple'
else return 'level-blue'

// 好的写法：加一个级别只需要在对象里加一行
const levelClassMap = {
  '省级': 'level-gold',
  '自治区级': 'level-green',
  '州级': 'level-purple',
  '国家级': 'level-red'  // 新增一行即可
}
```

---

### 3.5 createHeroStats - 统计数据工厂函数

首页顶部的大数字统计（如"50 种药用植物"、"10 名传承人"），用工厂函数生成配置。

```javascript
import { Picture, Document, User, Medal } from '@element-plus/icons-vue'

export const createHeroStats = (stats) => [
  { icon: Picture, value: stats.plants,     label: '种药用植物' },
  { icon: Document, value: stats.formulas,  label: '项经典药方' },
  { icon: User, value: stats.inheritors,    label: '名传承人' },
  { icon: Medal, value: stats.therapies,    label: '类核心疗法' }
]
```

**为什么用函数而不是直接写数组？**

因为统计数字是**动态的**，需要从后端获取。函数接收后端返回的数据，生成对应的配置。

```javascript
// 在组件中使用
import { createHeroStats } from '@/config/homeConfig'

// 假设从后端获取到了这些数据
const apiData = { plants: 52, formulas: 38, inheritors: 12, therapies: 6 }

// 调用工厂函数，生成统计卡片配置
const heroStats = createHeroStats(apiData)
// 结果：
// [
//   { icon: Picture, value: 52, label: '种药用植物' },
//   { icon: Document, value: 38, label: '项经典药方' },
//   { icon: User, value: 12, label: '名传承人' },
//   { icon: Medal, value: 6, label: '类核心疗法' }
// ]
```

**"工厂函数"是什么意思？**

类比：工厂函数就像一个模具。你把原材料（参数）倒进去，它自动生产出标准化的产品（返回值）。你不需要关心产品是怎么做出来的，只需要传入正确的原材料。

```javascript
// 工厂函数的特点：
// 1. 接收参数（原材料）
// 2. 返回新对象（产品）
// 3. 每次调用都返回新的结果

createHeroStats({ plants: 10 })  // 生产一组配置
createHeroStats({ plants: 100 }) // 生产另一组配置
```

---

## 四、配置数据流转全图

```
homeConfig.js（配置文件）
    |
    |  import { quickEntries } from '@/config/homeConfig'
    v
Home.vue（首页组件）
    |
    |  <router-link v-for="entry in quickEntries" :to="entry.path">
    v
浏览器渲染出 4 个导航卡片
```

配置文件只管"数据是什么"，组件只管"数据怎么展示"，各司其职。

---

## 五、如何修改配置

### 5.1 修改已有模块的信息

直接改配置文件中的对应字段即可，不需要动组件代码。

```javascript
// 需求：把"知识库"改名为"侗医百科"
export const quickEntries = [
  { title: '侗医百科', desc: '理论知识体系', path: '/knowledge',  // 改 title
    color: 'linear-gradient(135deg, #1A5276, var(--dong-indigo-dark))', icon: Document },
  // ... 其他不变
]
```

### 5.2 添加一个新的快速导航

在数组末尾加一个对象：

```javascript
import { VideoPlay } from '@element-plus/icons-vue'  // 先导入图标

export const quickEntries = [
  // ... 已有的 4 个
  // 新增：视频教程入口
  { title: '视频教程', desc: '侗医药影像资料', path: '/resources',
    color: 'linear-gradient(135deg, #e74c3c, #c0392b)', icon: VideoPlay }
]
```

### 5.3 添加新的传承人级别

在 `levelClassMap` 中加一行：

```javascript
export const levelClassMap = {
  '省级': 'level-gold',
  '自治区级': 'level-green',
  '州级': 'level-purple',
  '国家级': 'level-red'     // 新增：国家级用红色标签
}
```

---

## 六、如何添加新的配置文件

假设你要给"关于我们"页面也做一个配置文件。

### 第 1 步：创建配置文件

在 `config/` 目录下新建 `aboutConfig.js`：

```javascript
// config/aboutConfig.js

// 团队成员配置
export const teamMembers = [
  { name: '张同学', role: '前端开发', avatar: '/avatars/zhang.jpg', desc: '负责页面开发' },
  { name: '李同学', role: '后端开发', avatar: '/avatars/li.jpg', desc: '负责接口开发' },
  { name: '王同学', role: 'UI 设计', avatar: '/avatars/wang.jpg', desc: '负责界面设计' }
]

// 项目里程碑
export const milestones = [
  { date: '2025-01', event: '项目启动' },
  { date: '2025-03', event: '完成核心功能' },
  { date: '2025-05', event: '项目答辩' }
]
```

### 第 2 步：在组件中引入使用

```html
<!-- About.vue -->
<template>
  <div class="team-section">
    <h2>项目团队</h2>
    <div v-for="member in teamMembers" :key="member.name" class="member-card">
      <img :src="member.avatar" :alt="member.name" />
      <h3>{{ member.name }}</h3>
      <span>{{ member.role }}</span>
      <p>{{ member.desc }}</p>
    </div>
  </div>
</template>

<script setup>
import { teamMembers, milestones } from '@/config/aboutConfig'
</script>
```

---

## 七、常见问题

### Q1：配置写死在前端，后端改了数据怎么办？

目前配置是**静态的**，修改后需要重新构建部署。如果需要动态更新，有两种方案：

1. **简单方案**：把配置改成从后端 API 获取
2. **进阶方案**：保留前端默认配置，后端返回的数据覆盖默认值

```javascript
// 进阶方案的思路
const defaultEntries = [...quickEntries]  // 前端默认配置
const apiEntries = await fetch('/api/config/entries')  // 后端配置
const finalEntries = apiEntries.length ? apiEntries : defaultEntries  // 优先用后端的
```

### Q2：quickEntries 和 coreModules 内容重复，能不能合并？

它们虽然展示的是同样的 4 个模块，但**样式和字段不同**（一个简洁、一个详细）。合并后反而会让代码更复杂，因为需要用条件判断来区分两种展示方式。保持分开是合理的。

### Q3：为什么图标用组件而不是字符串？

```javascript
// 方式一：用组件（本项目采用）
icon: Document

// 方式二：用字符串
icon: 'Document'
```

用组件的好处是**编译时就能发现错误**。如果你写了一个不存在的图标名，构建时就会报错。用字符串的话，只有运行时才会发现图标显示不出来。
