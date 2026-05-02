# CSS 优化：PurgeCSS + 拆分

**日期**: 2026-05-03
**方案**: 方案三 — PurgeCSS 清除死 CSS，再拆分剩余到组件

## 目标

| 指标 | 当前 | 目标 |
|------|------|------|
| 全局 CSS 总行数 | 3554 (5 文件) | <500 (仅基础设施) |
| components.css | 951 | 归入组件或删除 |
| home.css | 911 | 归入 Home.vue |
| pages.css | 807 | 归入各页面 View |
| common.css | 489 | 提取通用部分保留，其余归组件 |
| dialog-common.css | 396 | 归入各 Dialog 组件 |

## Part 1: PurgeCSS 清除死 CSS

### 安装
```bash
npm install -D @fullhuman/postcss-purgecss
```

### Vite 配置
```js
// vite.config.js
import purgecss from '@fullhuman/postcss-purgecss'

css: {
  postcss: {
    plugins: [
      purgecss({
        content: ['./index.html', './src/**/*.{vue,js}'],
        safelist: {
          standard: [/^el-/, /^hljs/, /^v-enter/, /^v-leave/, /^fade-/],
          deep: [/el-/, /is-/],
        },
        defaultExtractor: content => content.match(/[\w-/:]+(?<!:)/g) || [],
        keyframes: true,
        fontFace: true,
        variables: true,
      })
    ]
  }
}
```

### 验证
```bash
npm run build  # PurgeCSS 仅在 production build 执行
```

## Part 2: 剩余 CSS 归位

PurgeCSS 之后，剩余样式按归属迁移：

### 保留为全局 (~400行)
```
src/styles/
  ├── reset.css        浏览器重置
  ├── tokens.css       CSS 变量/设计 token
  ├── typography.css   字体排版
  └── utilities.css    工具类
```

### 迁移映射
| 源文件 | 迁入目标 |
|--------|---------|
| components.css 剩余 | 对应业务组件的 `<style scoped>` |
| home.css 剩余 | Home.vue + 子组件 |
| pages.css 剩余 | 各页面 View 组件 |
| common.css 通用部分 | tokens.css / utilities.css |
| common.css 组件部分 | 对应组件 |
| dialog-common.css | KnowledgeDetailDialog / InheritorDetailDialog / ResourceDetailDialog |

## 风险控制

- PurgeCSS 仅 production build 执行，开发不受影响
- 配置出问题直接删除 postcss 插件即可回滚
- 保留 `variables: true` 确保 CSS 变量不被清除
- Safelist 覆盖 Element Plus、highlight.js、Vue transition

## 验收标准

- `npm run build` 通过
- `npm run test:run` 293/294 通过
- 全局 CSS 文件 < 500 行
- 无明显视觉回归
