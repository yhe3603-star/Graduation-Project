# 自定义指令目录 (directives/)

> 类比：给 HTML 元素贴一张"超能力标签"，贴上之后元素就自动拥有了某种特殊行为，不需要你手动写逻辑。

---

## 一、什么是指令？

在 Vue 中，**指令（Directive）** 是一种特殊的标记，写在 HTML 标签上，以 `v-` 开头。
当 Vue 看到 `v-` 开头的属性时，就知道"这不是普通属性，我要对它做特殊处理"。

```html
<!-- 普通属性：浏览器自己理解 -->
<img src="photo.jpg" alt="照片" />

<!-- 指令：Vue 会拦截并做特殊处理 -->
<img v-lazy="photoUrl" />
```

你可以把指令想象成**给元素贴标签**：
- 贴上 `v-lazy` 标签 -> 图片自动懒加载
- 贴上 `v-focus` 标签 -> 输入框自动获得焦点
- 贴上 `v-permission` 标签 -> 没权限就自动隐藏

你不需要在组件里写 `onMounted`、`watch` 之类的逻辑，指令帮你搞定了。

---

## 二、Vue 内置指令回顾

Vue 官方已经提供了一些常用指令，你应该已经见过它们了：

| 指令 | 作用 | 示例 |
|------|------|------|
| `v-if` | 条件渲染（真才渲染，假则销毁） | `<div v-if="isLoggedIn">欢迎</div>` |
| `v-show` | 条件显示（CSS 切换 display） | `<div v-show="isVisible">你好</div>` |
| `v-for` | 列表渲染 | `<li v-for="item in list">{{ item }}</li>` |
| `v-model` | 双向数据绑定 | `<input v-model="username" />` |
| `v-bind` | 动态绑定属性（缩写 `:`） | `<img :src="imgUrl" />` |
| `v-on` | 监听事件（缩写 `@`） | `<button @click="handleClick">` |

这些是 Vue 自带的，但有时候你需要**自己的指令**来做一些 Vue 没想到的事情。

---

## 三、为什么需要自定义指令？

当你的需求是**直接操作 DOM**，而且这个操作在多个组件里都要用到，自定义指令就是最佳选择。

### 什么时候用自定义指令？

| 场景 | 用什么 | 为什么 |
|------|--------|--------|
| 切换显示/隐藏 | `v-if` / `v-show` | Vue 内置就能搞定 |
| 绑定点击事件 | `@click` | Vue 内置就能搞定 |
| 图片进入视口才加载 | **自定义 `v-lazy`** | 需要操作 DOM + IntersectionObserver |
| 点击元素外部关闭弹窗 | **自定义 `v-click-outside`** | 需要监听 document 事件 |
| 按钮防抖/节流 | **自定义 `v-debounce` / `v-throttle`** | 需要包装事件处理函数 |

**简单判断标准**：如果逻辑是"给 DOM 元素附加某种行为"，而且多处复用 -> 写自定义指令。

---

## 四、本项目的 8 个自定义指令

所有指令都定义在 `index.js` 中，通过 Vue 插件方式全局注册。

### 4.1 v-lazy - 图片懒加载

> 类比：商场橱窗的灯，只有你走到橱窗前面，灯才会亮起来。图片也一样，只有滚动到可视区域，才真正加载。

**解决的问题**：页面有 50 张药用植物图片，如果一次性全部加载，浏览器要发 50 个请求，页面卡顿。用 `v-lazy` 后，只有用户滚动到图片位置时才加载。

```html
<!-- 使用方式：把图片地址传给 v-lazy -->
<img v-lazy="plant.imageUrl" alt="钩藤" />

<!-- 也可以用 data-src 属性 -->
<img v-lazy data-src="/images/gouteng.jpg" alt="钩藤" />
```

**实现原理**：

```javascript
export const vLazy = {
  // 元素被插入 DOM 时触发（类似 mounted 生命周期）
  mounted(el, binding) {
    // 1. 创建一个"观察者"，盯着这个元素
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          // 2. 当元素进入可视区域时
          if (entry.isIntersecting) {
            // 3. 把图片地址赋给 src，浏览器才开始加载图片
            const src = binding.value || el.dataset.src
            if (src) {
              el.src = src                           // 真正加载图片
              el.classList.remove('lazy-loading')     // 移除加载中样式
              el.classList.add('lazy-loaded')         // 添加加载完成样式
            }
            // 4. 加载完成后不再观察这个元素（节省性能）
            observer.unobserve(el)
          }
        })
      },
      {
        rootMargin: '50px',   // 提前 50px 就开始加载（用户感觉更流畅）
        threshold: 0.1        // 元素露出 10% 就触发
      }
    )

    el.classList.add('lazy-loading')  // 先显示加载中样式
    el._observer = observer           // 保存引用，方便后续清理
    observer.observe(el)              // 开始观察
  },

  // 元素从 DOM 移除时触发（类似 unmounted 生命周期）
  unmounted(el) {
    // 清理观察者，防止内存泄漏
    if (el._observer) {
      el._observer.disconnect()
    }
  }
}
```

**常见错误**：

```html
<!-- 错误：同时写了 src 和 v-lazy，图片会立即加载，懒加载失效 -->
<img src="/images/gouteng.jpg" v-lazy="/images/gouteng.jpg" />

<!-- 正确：只写 v-lazy，不要写 src -->
<img v-lazy="/images/gouteng.jpg" />
```

---

### 4.2 v-lazy-background - 背景图懒加载

和 `v-lazy` 原理完全一样，区别是它设置的是 `background-image` 而不是 `src`。

```html
<!-- 使用场景：卡片背景图、Banner 图等用 CSS 背景图的地方 -->
<div v-lazy-background="bgUrl" class="hero-banner"></div>

<!-- 也可以用 data-bg-src 属性 -->
<div v-lazy-background data-bg-src="/images/banner.jpg" class="card-bg"></div>
```

**实现关键差异**：

```javascript
// v-lazy 设置的是 el.src（img 标签专用）
el.src = src

// v-lazy-background 设置的是 el.style.backgroundImage（div 等通用）
el.style.backgroundImage = `url(${src})`
```

**什么时候用 v-lazy，什么时候用 v-lazy-background？**

| 标签 | 用哪个 | 原因 |
|------|--------|------|
| `<img>` | `v-lazy` | img 用 src 属性显示图片 |
| `<div>` / `<section>` | `v-lazy-background` | div 用 background-image 显示图片 |

---

### 4.3 v-debounce - 事件防抖

> 类比：电梯等人。有人按了电梯按钮，电梯不会立刻关门，而是等一小段时间。如果这段时间内又有人按，就重新计时。只有等了一段时间没人再按，电梯才关门。

**解决的问题**：用户在搜索框快速输入"钩藤"两个字，会触发 2 次搜索请求。用防抖后，只有用户停止输入 300ms 后才发请求。

```html
<!-- 基本用法：默认 300ms 防抖，默认监听 click 事件 -->
<button v-debounce="handleSave">保存</button>

<!-- 指定事件类型：监听 input 事件（搜索框常用） -->
<input v-debounce:input="handleSearch" />

<!-- 指定防抖时间：500ms -->
<button v-debounce="{ handler: handleSubmit, delay: 500 }">提交</button>

<!-- 直接传时间数字：500ms 防抖 -->
<button v-debounce="500">保存</button>
```

**实现原理**：

```javascript
import { debounce } from 'lodash-es'  // 使用 lodash 提供的防抖函数

export const vDebounce = {
  mounted(el, binding) {
    const { value, arg = 'click' } = binding  // arg 是 v-debounce:xxx 中的 xxx
    const delay = typeof value === 'number' ? value : 300
    const handler = typeof value === 'function' ? value : binding.value?.handler

    if (handler) {
      // 用 lodash 的 debounce 包装原始函数
      el._debounceHandler = debounce(handler, delay)
      el.addEventListener(arg, el._debounceHandler)
    }
  },

  unmounted(el, binding) {
    const { arg = 'click' } = binding
    if (el._debounceHandler) {
      el.removeEventListener(arg, el._debounceHandler)
      el._debounceHandler.cancel()  // 取消可能还在等待的调用
    }
  }
}
```

**常见错误**：

```html
<!-- 错误：在方法后面加了括号，每次渲染都创建新函数，防抖失效 -->
<button v-debounce="handleSave()">保存</button>

<!-- 正确：只传函数引用，不要加括号 -->
<button v-debounce="handleSave">保存</button>
```

---

### 4.4 v-throttle - 事件节流

> 类比：水龙头限流。不管你拧多大，水龙头每隔固定时间才放出一股水。节流也是，不管你点多少次，每隔固定时间才执行一次。

**解决的问题**：用户疯狂点击"提交"按钮，1 秒点了 10 次。用节流后，300ms 内最多执行 1 次。

```html
<!-- 基本用法：默认 300ms 节流，默认监听 click 事件 -->
<button v-throttle="handleSubmit">提交</button>

<!-- 指定事件类型：监听 scroll 事件 -->
<div v-throttle:scroll="handleScroll">滚动区域</div>

<!-- 指定节流时间：500ms -->
<button v-throttle="{ handler: handleSave, delay: 500 }">保存</button>
```

**防抖 vs 节流，到底用哪个？**

| 场景 | 用哪个 | 原因 |
|------|--------|------|
| 搜索框输入 | **防抖** | 用户打字过程中不需要搜索，停下来才搜 |
| 窗口滚动加载 | **节流** | 滚动过程中需要持续检测，但不能太频繁 |
| 按钮点击提交 | **防抖** | 防止重复提交，点一次就够了 |
| 拖拽元素位置 | **节流** | 拖动过程中需要持续更新位置 |

---

### 4.5 v-click-outside - 点击外部检测

> 类比：你走进一家店，店门自动打开；你走出店门，店门自动关闭。点击元素外部，就触发关闭操作。

**解决的问题**：弹出一个下拉菜单，用户点击菜单外部区域时，菜单应该自动关闭。

```html
<!-- 使用方式：传入一个回调函数，点击外部时自动调用 -->
<div v-click-outside="closeMenu" class="dropdown">
  <button @click="showMenu = !showMenu">打开菜单</button>
  <ul v-if="showMenu">
    <li>选项一</li>
    <li>选项二</li>
  </ul>
</div>
```

**实现原理**：

```javascript
export const vClickOutside = {
  mounted(el, binding) {
    // 监听整个 document 的点击事件
    el._clickOutsideHandler = (event) => {
      // 关键判断：点击的目标不在 el 元素内部
      if (!el.contains(event.target) && el !== event.target) {
        binding.value(event)  // 调用传入的回调函数
      }
    }
    document.addEventListener('click', el._clickOutsideHandler)
  },

  unmounted(el) {
    if (el._clickOutsideHandler) {
      document.removeEventListener('click', el._clickOutsideHandler)
    }
  }
}
```

**常见错误**：

```html
<!-- 错误：回调函数加了括号，变成立即执行，而不是事件触发时执行 -->
<div v-click-outside="closeMenu()">下拉菜单</div>

<!-- 正确：只传函数引用 -->
<div v-click-outside="closeMenu">下拉菜单</div>
```

---

### 4.6 v-focus - 自动聚焦

> 类比：你走进会议室，主持人自动把麦克风递给你。页面加载后，输入框自动获得焦点，用户可以直接打字。

```html
<!-- 使用方式：加在输入框上，页面渲染后自动聚焦 -->
<input v-focus placeholder="搜索药用植物..." />
```

**实现原理**（最简单的指令）：

```javascript
export const vFocus = {
  mounted(el) {
    el.focus()  // 就这一行，元素挂载后自动聚焦
  }
}
```

**使用场景**：登录页面的用户名输入框、搜索页面的搜索框、弹窗中的第一个表单项。

---

### 4.7 v-permission - 权限控制

> 类比：VIP 专属通道。只有持有 VIP 卡的人才能通过，其他人直接被拦住。没有权限的按钮，直接从页面上消失。

**解决的问题**：管理员的"删除"按钮，普通用户不应该看到。用 `v-permission` 可以根据用户角色自动隐藏元素。

```html
<!-- 只有 admin 角色才能看到这个按钮 -->
<button v-permission="'admin'">删除用户</button>

<!-- 多个角色都可以看到（数组形式） -->
<button v-permission="['admin', 'editor']">编辑内容</button>
```

**实现原理**：

```javascript
export const vPermission = {
  mounted(el, binding) {
    const { value } = binding
    // 从 sessionStorage 获取当前用户角色
    const role = sessionStorage.getItem('role') || 'user'
    // 支持单个角色或角色数组
    const requiredRoles = Array.isArray(value) ? value : [value]

    // 如果用户角色不在允许列表中，且不是 ADMIN，就移除元素
    if (value && !requiredRoles.includes(role) && role.toUpperCase() !== 'ADMIN') {
      el.parentNode?.removeChild(el)  // 直接从 DOM 中删除
    }
  }
}
```

**注意事项**：
- `v-permission` 只是**前端隐藏**，不是真正的安全措施。后端接口必须做权限校验。
- ADMIN 角色自动通过所有权限检查（`role.toUpperCase() !== 'ADMIN'`）。
- 依赖 `sessionStorage` 中的 `role` 字段，确保登录时正确存储。

---

### 4.8 v-loading - 加载遮罩

**解决的问题**：数据还在加载时，显示一个转圈圈的遮罩层，告诉用户"请稍等"。

```html
<!-- 基本用法：loading 为 true 时显示遮罩 -->
<div v-loading="isLoading">
  <p>这里是内容区域</p>
</div>

<!-- 在组件中使用 -->
<template>
  <div v-loading="dataLoading">
    <div v-for="plant in plants" :key="plant.id">{{ plant.name }}</div>
  </div>
</template>
```

**实现原理**：

```javascript
export const vLoading = {
  mounted(el, binding) {
    if (binding.value) {
      // 添加 loading 样式
      el.classList.add('loading')
      // 创建一个转圈圈的 div
      const loadingEl = document.createElement('div')
      loadingEl.className = 'loading-spinner'
      loadingEl.innerHTML = '<div class="spinner"></div>'
      el.appendChild(loadingEl)
      el._loadingEl = loadingEl  // 保存引用
    }
  },

  // 当 binding.value 变化时触发（响应式更新）
  updated(el, binding) {
    if (binding.value !== binding.oldValue) {
      if (binding.value) {
        // 变为 true：显示 loading
        el.classList.add('loading')
        if (!el._loadingEl) {
          const loadingEl = document.createElement('div')
          loadingEl.className = 'loading-spinner'
          loadingEl.innerHTML = '<div class="spinner"></div>'
          el.appendChild(loadingEl)
          el._loadingEl = loadingEl
        }
      } else {
        // 变为 false：隐藏 loading
        el.classList.remove('loading')
        if (el._loadingEl) {
          el._loadingEl.remove()
          el._loadingEl = null
        }
      }
    }
  }
}
```

---

## 五、指令速查表

| 指令 | 用途 | 核心技术 | 类比 |
|------|------|----------|------|
| `v-lazy` | 图片懒加载 | IntersectionObserver | 走到橱窗前才亮灯 |
| `v-lazy-background` | 背景图懒加载 | IntersectionObserver | 同上，但用在 div 上 |
| `v-debounce` | 事件防抖 | lodash-es debounce | 电梯等人，最后进来才关门 |
| `v-throttle` | 事件节流 | lodash-es throttle | 水龙头限流，定时放水 |
| `v-click-outside` | 点击外部检测 | document.addEventListener | 走出店门自动关门 |
| `v-focus` | 自动聚焦 | el.focus() | 自动把麦克风递给你 |
| `v-permission` | 权限控制 | sessionStorage + DOM 移除 | VIP 专属通道 |
| `v-loading` | 加载遮罩 | 动态创建 DOM 元素 | 遮住内容，显示转圈 |

---

## 六、如何注册和使用自定义指令

### 6.1 全局注册（推荐，本项目使用的方式）

在 `main.js` 中通过插件方式注册，所有组件都能使用：

```javascript
// main.js
import directives from './directives'

const app = createApp(App)
app.use(directives)  // 一行代码注册所有指令
```

插件内部是怎么注册的？看 `index.js` 的 `install` 方法：

```javascript
export const directives = {
  install(app) {
    app.directive('lazy', vLazy)              // 注册 v-lazy
    app.directive('lazy-background', vLazyBackground)  // 注册 v-lazy-background
    app.directive('debounce', vDebounce)      // 注册 v-debounce
    app.directive('throttle', vThrottle)      // 注册 v-throttle
    app.directive('click-outside', vClickOutside)  // 注册 v-click-outside
    app.directive('focus', vFocus)            // 注册 v-focus
    app.directive('permission', vPermission)  // 注册 v-permission
    app.directive('loading', vLoading)        // 注册 v-loading
  }
}
```

### 6.2 局部注册（只在某个组件内使用）

```javascript
// 在单个组件中引入
import { vLazy, vFocus } from '@/directives'

export default {
  directives: {
    lazy: vLazy,
    focus: vFocus
  }
}
```

---

## 七、如何创建一个新的自定义指令（手把手教学）

假设你想创建一个 `v-watermark` 指令，给元素添加水印文字。

### 第 1 步：在 `index.js` 中定义指令对象

```javascript
// 指令对象就是一个包含生命周期钩子的普通对象
export const vWatermark = {
  // mounted：元素被插入 DOM 时触发
  mounted(el, binding) {
    const text = binding.value || '侗乡医药'  // 默认水印文字
    el.style.position = 'relative'            // 确保伪元素定位正确

    // 创建水印层
    const watermark = document.createElement('div')
    watermark.style.cssText = `
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      pointer-events: none;                   /* 不影响点击 */
      background-repeat: repeat;
      opacity: 0.1;
      font-size: 16px;
      color: #000;
    `
    // 用 canvas 生成水印图片（更专业的做法）
    watermark.textContent = text
    el.appendChild(watermark)
    el._watermarkEl = watermark  // 保存引用，方便清理
  },

  // unmounted：元素从 DOM 移除时触发
  unmounted(el) {
    if (el._watermarkEl) {
      el._watermarkEl.remove()   // 清理水印元素
      el._watermarkEl = null
    }
  }
}
```

### 第 2 步：在 `install` 方法中注册

```javascript
export const directives = {
  install(app) {
    // ... 已有的注册代码 ...
    app.directive('watermark', vWatermark)  // 添加这一行
  }
}
```

### 第 3 步：在模板中使用

```html
<div v-watermark="'内部资料'">
  <p>这是需要加水印的内容</p>
</div>
```

### 指令生命周期钩子速查

| 钩子 | 触发时机 | 常用程度 |
|------|----------|----------|
| `created` | 元素创建后，属性/事件之前 | 少用 |
| `beforeMount` | 元素插入 DOM 之前 | 少用 |
| **`mounted`** | **元素插入 DOM 之后** | **最常用** |
| `beforeUpdate` | 元素更新之前 | 少用 |
| **`updated`** | **元素更新之后** | **较常用**（如 v-loading） |
| `beforeUnmount` | 元素卸载之前 | 少用 |
| **`unmounted`** | **元素卸载之后** | **必须用**（清理资源） |

### 指令钩子参数说明

```javascript
mounted(el, binding, vnode) {
  // el：指令绑定的 DOM 元素，可以直接操作
  // binding：一个对象，包含以下属性：
  //   binding.value    -> v-xxx="这里面的值"
  //   binding.oldValue -> 上一次的值（updated 中才有）
  //   binding.arg      -> v-xxx:arg 中的 arg
  //   binding.modifiers -> v-xxx.mod 中的修饰符对象
}
```

```html
<!-- 参数示例 -->
<button v-debounce:input.prevent="handleSearch" />

<!-- 对应的 binding 值 -->
// binding.value = handleSearch（函数）
// binding.arg = 'input'（事件类型）
// binding.modifiers = { prevent: true }（修饰符）
```

---

## 八、常见问题

### Q1：指令和组件有什么区别？

| 对比 | 组件 | 指令 |
|------|------|------|
| 本质 | 一个 Vue 文件，有模板/逻辑/样式 | 一个 JS 对象，只有钩子函数 |
| 用途 | 构建页面结构 | 给已有元素附加行为 |
| 示例 | `<PlantCard />` | `<img v-lazy="url" />` |
| 有模板吗 | 有 | 没有 |

### Q2：为什么 unmounted 里一定要清理资源？

不清理会导致**内存泄漏**。比如 `v-lazy` 创建了 IntersectionObserver，如果不 disconnect，即使元素从页面消失了，观察者还在后台运行，白白消耗内存。

### Q3：v-permission 安全吗？

不安全。这只是前端层面的隐藏，用户可以通过浏览器开发者工具修改 sessionStorage 来绕过。**真正的权限控制必须在后端实现**，前端只是提升用户体验。
