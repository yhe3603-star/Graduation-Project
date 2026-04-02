# 配置文件目录 (config)

本目录存放应用的配置文件，用于管理各种可配置的数据和参数。

## 📁 文件列表

| 文件名 | 功能说明 |
|--------|----------|
| `homeConfig.js` | 首页配置文件 |

## 📦 详细说明

### homeConfig.js - 首页配置

首页的动态配置数据，包括导航入口、核心模块、扩展模块等。

**主要配置项:**

```javascript
export default {
  // 导航入口配置
  navEntries: [
    {
      id: 'plants',
      title: '药用植物',
      icon: '🌿',
      path: '/plants',
      color: '#28B463'
    },
    // ... 其他入口
  ],
  
  // 核心模块配置
  coreModules: [
    {
      id: 'knowledge',
      title: '知识库',
      icon: '📚',
      path: '/knowledge',
      description: '侗族医药知识体系'
    },
    // ... 其他模块
  ],
  
  // 扩展模块配置
  extendModules: [
    {
      id: 'interact',
      title: '文化互动',
      icon: '🎮',
      path: '/interact',
      description: '趣味游戏互动'
    },
    // ... 其他模块
  ],
  
  // 统计数据配置
  stats: {
    plants: { label: '药用植物', icon: '🌱' },
    formulas: { label: '经典药方', icon: '📜' },
    therapies: { label: '核心疗法', icon: '💊' },
    inheritors: { label: '传承人', icon: '👨‍⚕️' }
  }
}
```

**使用示例:**
```vue
<script setup>
import homeConfig from '@/config/homeConfig'

const { navEntries, coreModules, stats } = homeConfig
</script>

<template>
  <div class="nav-grid">
    <div
      v-for="entry in navEntries"
      :key="entry.id"
      class="nav-item"
      @click="$router.push(entry.path)"
    >
      <span class="icon">{{ entry.icon }}</span>
      <span class="title">{{ entry.title }}</span>
    </div>
  </div>
</template>
```

## 🎯 配置规范

### 配置文件结构
```javascript
export default {
  // 配置项名称要有意义
  configName: {
    // 每个配置项包含必要的属性
    id: 'unique-id',      // 唯一标识
    title: '显示标题',     // 显示文本
    icon: '🌟',           // 图标
    path: '/route-path',  // 路由路径
    description: '描述'   // 说明文字
  }
}
```

### 配置项命名规范
- **小驼峰命名**: 如 `navEntries`, `coreModules`
- **语义化**: 名称要能体现配置项的用途
- **一致性**: 同类配置使用相同的属性名

### 最佳实践
1. **集中管理**: 将可变数据提取到配置文件
2. **类型定义**: 使用JSDoc定义配置类型
3. **默认值**: 为可选配置提供默认值
4. **注释完善**: 添加配置说明注释

## 📚 扩展阅读

- [JavaScript 模块化](https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Guide/Modules)
- [配置设计模式](https://refactoring.guru/design-patterns)
