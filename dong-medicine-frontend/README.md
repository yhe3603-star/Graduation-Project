# 侗乡医药数字展示平台 - 前端应用

> 基于Vue 3.4 + Vite 5的侗族医药文化遗产数字化展示平台前端应用

---

## 一、先搞懂这些概念（写给第一次学前端的同学）

在动手之前，你需要理解本项目用到的核心技术。别怕，我们用生活中的例子来解释！

### 1.1 什么是 Vue.js？

**类比：搭积木的框架**

想象你在搭一座乐高城堡。如果每次都要从零开始捏砖头，那太累了。Vue.js 就是一套"积木框架"——它帮你把网页拆成一个个**可复用的组件**（就像不同形状的积木块），你只需要拼装它们，就能搭出完整的页面。

```
传统写网页：  一个大HTML文件，几千行代码混在一起，改一处牵动全身
Vue写网页：   页面 = 组件A + 组件B + 组件C，每个组件独立维护
```

**核心思想：数据驱动视图。** 你只需要关心"数据是什么"，Vue 会自动帮你更新页面显示。就像 Excel 表格——你改了单元格的数字，图表自动跟着变。

### 1.2 什么是 SPA（单页应用）？

**类比：单页笔记本，翻页不刷新**

传统网站像一本"多页书"：点一个链接，浏览器就重新加载一个全新的页面，白屏一闪。

SPA 像"单页笔记本"：整个应用只加载一次，之后"翻页"（切换页面）时，只更新变化的部分，不会整页刷新。体验更流畅，像手机 App 一样。

```
传统网站：  首页 → [白屏刷新] → 植物页 → [白屏刷新] → 知识页
SPA应用：  首页 → [丝滑切换] → 植物页 → [丝滑切换] → 知识页
```

本项目就是 SPA，你点击导航栏时页面不会白屏闪烁。

### 1.3 什么是组件化开发？

**类比：乐高积木**

组件化就是把页面拆成独立的小块，每块有自己的模板（长什么样）、逻辑（做什么事）、样式（怎么装饰）。

```
+--------------------------------------------------+
|                    页面 (Plants.vue)              |
|  +------------+  +-------------+  +-----------+  |
|  | 搜索筛选   |  | 卡片网格    |  | 分页器    |  |
|  | SearchFilter|  | CardGrid    |  | Pagination|  |
|  +------------+  +-------------+  +-----------+  |
+--------------------------------------------------+
```

好处：
- **复用**：同一个分页器组件，在植物页、知识页、资源页都能用
- **独立**：改搜索筛选不会影响分页器
- **协作**：不同人负责不同组件，互不干扰

### 1.4 什么是 Vite？

**类比：高速加工厂**

Vite 是一个**构建工具**，负责把你的源代码"加工"成浏览器能运行的文件。

为什么选 Vite 而不是老牌的 Webpack？因为 Vite 快——

| 场景 | Webpack（传统工厂） | Vite（高速工厂） |
|------|-------------------|-----------------|
| 启动开发服务器 | 等几十秒，先打包所有文件 | 秒开，按需加载 |
| 修改代码后刷新 | 全部重新打包 | 只更新改动的部分（HMR） |
| 生产构建 | 较慢 | 使用 Rollup，更快更小 |

### 1.5 什么是 Element Plus？

**类比：装修材料包**

自己写按钮、表格、弹窗太费时间了。Element Plus 是一套现成的 UI 组件库，就像装修时直接买现成的门窗、橱柜，不用自己从木头开始做。

```vue
<!-- 不用 Element Plus：自己写一个按钮 -->
<button class="my-btn" @click="handleClick">提交</button>
<style>
.my-btn { background: #409eff; color: white; border: none; padding: 8px 16px; ... }
</style>

<!-- 用 Element Plus：一行搞定，样式、交互、无障碍都帮你做好了 -->
<el-button type="primary" @click="handleClick">提交</el-button>
```

本项目使用的 Element Plus 组件包括：按钮、表格、表单、弹窗、分页、消息提示等几十种。

### 1.6 什么是 Pinia？

**类比：全局公告板**

想象一个学校公告板——任何人都可以去看公告，也可以更新公告，所有人看到的都是同一份信息。

Pinia 就是 Vue 应用的"全局公告板"（状态管理工具）。当多个组件需要共享同一份数据时（比如用户登录状态），不用组件之间传来传去，统一放在 Pinia 里，谁需要谁去取。

```
没有 Pinia：  组件A → 传给 → 组件B → 传给 → 组件C（层层传递，很麻烦）
有了 Pinia：  组件A → 存入Pinia ← 组件B ← 组件C（各取所需，互不干扰）
```

本项目的 `stores/user.js` 就是 Pinia Store，管理用户的登录状态、角色信息等。

### 1.7 什么是 Vue Router？

**类比：导航地图**

一个 SPA 有很多"页面"，但 URL 只有一个怎么行？Vue Router 就是"导航地图"——它把不同的 URL 路径映射到不同的页面组件。

```
/            →  Home.vue（首页）
/plants      →  Plants.vue（药用植物）
/knowledge   →  Knowledge.vue（知识库）
/admin       →  Admin.vue（管理后台，需要登录+管理员权限）
```

它还能做"守卫"——比如你没登录就想访问个人中心，路由守卫会拦住你，跳转到首页让你先登录。

### 1.8 什么是 Axios？

**类比：服务员传菜单**

前端需要从后端获取数据（比如植物列表），怎么拿？通过 HTTP 请求。Axios 就是一个"服务员"——你告诉他"我要点这道菜"（发请求），他跑去"厨房"（后端服务器），把"菜"（数据）端回来给你。

```javascript
// 告诉服务员：我要获取药用植物列表
const response = await request.get('/plants/list', { params: { page: 1, size: 10 } })
// 服务员端回来了：这是你要的数据
console.log(response.data) // 植物列表数据
```

本项目的 `utils/request.js` 在 Axios 基础上做了很多增强（后面会详细讲）。

### 1.9 什么是 ECharts？

**类比：画图工具**

ECharts 是百度开源的数据可视化库，就像一个超级画图工具——你给它数据，它帮你画出漂亮的柱状图、折线图、饼图、地图等。

本项目在"数据可视化"页面使用 ECharts 展示侗医药的统计数据，比如药用植物分类分布、传承人级别统计等。

---

## 二、项目结构详解

```
dong-medicine-frontend/
│
├── public/                  # 公共静态资源（不会被Vite处理，原样复制）
│   └── vite.svg             #   网站图标
│
├── src/                     # 源代码目录（你的主战场，详见 src/README.md）
│   ├── assets/              #   静态资源（会被Vite优化处理）
│   ├── components/          #   Vue组件（base/common/business三层）
│   ├── composables/         #   组合式函数（可复用的业务逻辑）
│   ├── config/              #   页面配置
│   ├── directives/          #   自定义指令
│   ├── router/              #   路由配置
│   ├── stores/              #   Pinia状态管理
│   ├── styles/              #   全局样式与设计系统
│   ├── utils/               #   工具函数
│   ├── views/               #   页面组件
│   ├── App.vue              #   根组件
│   └── main.js              #   应用入口
│
├── .dockerignore             # Docker构建时忽略的文件
├── .env.example              # 环境变量示例（复制为.env使用）
├── .gitignore                # Git忽略的文件
├── Dockerfile                # Docker镜像构建脚本
├── default.conf              # Nginx站点配置（Docker部署用）
├── nginx.conf                # Nginx主配置（Docker部署用）
├── index.html                # HTML入口（Vite注入JS的模板）
├── package.json              # 项目依赖和脚本命令
├── vite.config.js            # Vite构建配置
├── vitest.config.js          # 测试配置
└── eslint.config.js          # 代码规范配置
```

### 关键文件说明

| 文件 | 作用 | 你需要改吗？ |
|------|------|------------|
| `package.json` | 定义项目依赖和npm脚本 | 添加新依赖时改 |
| `vite.config.js` | Vite构建配置（路径别名、代理、打包优化） | 需要改代理或别名时改 |
| `index.html` | 单页应用的唯一HTML文件 | 一般不用改 |
| `.env.example` | 环境变量模板 | 复制为.env后填入实际值 |

---

## 三、核心功能模块

| 功能模块 | 说明 | 涉及的关键组件 |
|---------|------|--------------|
| 首页 | 数据概览、快速导航、传承人风采 | ECharts统计卡片、组件化布局 |
| 药用植物 | 植物图鉴、分类筛选、全文搜索、收藏 | CardGrid + SearchFilter + Pagination |
| 传承人 | 传承人档案、级别筛选、代表案例 | 级别标签映射、ngram搜索 |
| 知识库 | 知识条目、疗法/疾病/药材分类、相关推荐 | 多维度筛选、关联推荐 |
| 问答社区 | 常见问题、AI智能回答 | AiChatCard + DeepSeek API |
| 学习资源 | 视频/文档/图片资源库、在线预览 | KKFileView文档预览、MediaDisplay |
| 互动专区 | 趣味答题、植物识别游戏、评论交流 | QuizSection + PlantGame + CommentSection |
| 数据可视化 | ECharts统计图表 | useVisualData + chartConfig |
| 个人中心 | 收藏管理、答题记录、账号设置 | usePersonalCenter |
| 管理后台 | 内容CRUD、用户管理、评论审核 | AdminDataTable + AdminSidebar |

---

## 四、技术栈一览

| 技术 | 版本 | 用途 | 一句话理解 |
|------|------|------|----------|
| Vue.js | 3.4 | 渐进式JavaScript框架 | 搭积木的框架 |
| Vite | 5.0 | 构建工具与开发服务器 | 高速加工厂 |
| Element Plus | 2.4 | UI组件库（中文locale） | 装修材料包 |
| Pinia | 2.3 | 状态管理 | 全局公告板 |
| Vue Router | 4.2 | 前端路由 | 导航地图 |
| Axios | 1.6 | HTTP客户端 | 服务员传菜单 |
| ECharts | 5.4 | 数据可视化 | 画图工具 |
| lodash-es | 4.17 | 工具函数（debounce/throttle） | 工具箱 |
| vuedraggable | 4.1 | 拖拽排序 | 拖拽插件 |
| Vitest | 1.0 | 单元测试 | 代码体检 |
| ESLint | 9.22 | 代码规范 | 语法检查器 |

---

## 五、关键架构特性（新手重点理解）

### 5.1 HTTP客户端 (utils/request.js)

这个文件是整个前端与后端通信的"总管"，在 Axios 基础上增加了四大能力：

#### 能力一：请求去重

**问题**：用户快速点击两次"查询"按钮，发了两个一模一样的请求，浪费资源。

**解决**：用 CancelToken 机制，如果发现相同的请求已经在进行中，就取消前一个，只保留最新的。

```javascript
// 生成请求的唯一标识（方法+地址+参数）
function generateRequestKey(config) {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

// 如果发现重复请求，取消前一个
if (pendingRequests.has(key)) {
  const cancel = pendingRequests.get(key)
  cancel(`请求被取消: ${config.url}`)
}
```

#### 能力二：Token自动刷新

**问题**：JWT Token 过期了，用户正在操作，突然被踢下线，体验很差。

**解决**：当后端返回 401（未授权）时，自动用旧 Token 去换新 Token，换好之后重新发送之前失败的请求，用户完全无感知。

```javascript
// 单例Promise模式：多个请求同时401时，只刷新一次Token
async function getOrRefreshToken() {
  if (refreshPromise) {
    return refreshPromise  // 已经在刷新了，等结果就行
  }
  refreshPromise = refreshToken()  // 开始刷新
  try {
    return await refreshPromise
  } finally {
    refreshPromise = null  // 刷新完毕，清除状态
  }
}
```

#### 能力三：自动重试

**问题**：网络偶尔抖动，请求失败一次就报错，太脆弱了。

**解决**：对 GET 请求自动重试最多3次，间隔时间指数递增（1秒、2秒、4秒），只重试"可恢复"的错误（超时、服务器错误等）。

```javascript
const RETRY_CONFIG = {
  maxRetries: 3,                          // 最多重试3次
  retryDelay: 1000,                       // 基础延迟1秒
  retryableStatuses: [408, 429, 500, 502, 503, 504],  // 这些状态码可以重试
  retryableMethods: ['get', 'head', 'options']         // 只有GET类请求重试（POST不重试，避免重复操作）
}
```

#### 能力四：安全过滤

**问题**：恶意用户在输入框里填入 `<script>alert('hack')</script>` 或 `DROP TABLE users`。

**解决**：请求发送前，递归检查所有参数，发现 XSS 或 SQL 注入就自动清理。

```javascript
// 递归检查并清理请求数据
if (containsXss(value) || containsSqlInjection(value)) {
  sanitized[key] = sanitize(value)  // 清理危险内容
}
```

### 5.2 双层缓存 (utils/cache.js)

**问题**：每次打开植物列表都要请求后端，加载慢又浪费带宽。

**解决**：双层缓存机制，像电脑的内存和硬盘一样配合工作。

```
+-------------------+     没有？     +-------------------+
|   内存缓存 (Map)  | <-----------> | SessionStorage缓存 |
|   速度：极快      |               | 速度：快           |
|   生命周期：页面  |               | 生命周期：浏览器会话|
|   刷新后丢失      |               | 关闭标签页后丢失    |
+-------------------+               +-------------------+
         |                                  |
         | 都没有？                          |
         v                                  v
    请求后端API，拿到数据后同时写入两层缓存
```

每种数据有不同的缓存时间（TTL）：

| 数据类型 | 缓存时间 | 存储位置 | 为什么这样设？ |
|---------|---------|---------|-------------|
| 植物/知识/传承人 | 10分钟 | SessionStorage | 数据不常变，可以缓存久一点 |
| 学习资源 | 5分钟 | SessionStorage | 可能经常上传新资源 |
| 分类信息 | 30分钟 | SessionStorage | 很少变化 |
| 测验题目 | 5分钟 | 内存 | 每次答题应该拿新题 |
| 排行榜 | 2分钟 | 内存 | 实时性要求高 |
| 用户信息 | 30分钟 | SessionStorage | 很少变化 |

还有一个 `createCachedFetcher` 高阶函数，可以把任何异步请求自动包装成带缓存的版本：

```javascript
// 创建一个带缓存的获取植物数据的函数
const fetchPlants = createCachedFetcher(
  (page, size) => request.get('/plants/list', { params: { page, size } }),
  'plants'  // 使用plants的缓存配置
)
// 第一次调用：请求后端，结果存入缓存
// 第二次调用（10分钟内）：直接从缓存返回，不请求后端
```

### 5.3 JWT生命周期

JWT（JSON Web Token）是用户登录后的"通行证"。本项目的 JWT 管理非常完善：

```
用户登录
   |
   v
后端返回Token + 用户信息
   |
   v
存入sessionStorage + Pinia Store
   |
   v
每次请求自动带上Token（请求拦截器）
   |
   v
+-- 本地过期检查（5分钟缓冲）--- 过期？---> 清除登录状态
|                                          |
|  没过期                                  |
|  v                                       |
+-- 路由守卫：服务端验证（60秒缓存）--- 无效？---> 跳转首页
|                                          |
|  有效                                    |
|  v                                       |
+-- 正常访问页面
   |
   v
Token快过期时（401响应）---> 自动刷新 ---> 继续操作（用户无感知）
   |
   刷新失败？
   v
清除登录状态 + 触发auth-expired事件 + 提示重新登录
```

**5分钟缓冲**是什么意思？如果 Token 还剩5分钟就过期了，系统认为它"实际上已过期"，提前刷新，避免用户操作到一半突然失效。

### 5.4 设计系统 (styles/variables.css)

这不是随便选的颜色！每个颜色都来自侗族传统文化：

```
+----------------------------------------------------------+
|                  侗乡医药设计系统                          |
+----------------------------------------------------------+
|                                                          |
|  靛蓝 #1A5276  —— 侗族传统服饰主色调，象征智慧与传承      |
|  ████████                                                |
|                                                          |
|  翡翠绿 #28B463 —— 侗乡山水与草药，寓意生机与希望         |
|  ████████                                                |
|                                                          |
|  古金 #c9a227  —— 象征非遗荣誉与匠人精神                  |
|  ████████                                                |
|                                                          |
|  铜色 #b87333  —— 传统铜器质感，体现历史厚重感            |
|  ████████                                                |
|                                                          |
|  暖米 #f8f5f0  —— 传统纸张质感，体现文化底蕴              |
|  ████████                                                |
+----------------------------------------------------------+
```

除了颜色，还定义了完整的间距、字体、阴影、圆角、动画等变量，确保整个应用视觉统一。还预留了暗色模式支持。

---

## 六、如何运行项目（手把手教学）

### 6.1 环境准备

**第一步：安装 Node.js**

去 [Node.js官网](https://nodejs.org/) 下载 LTS（长期支持）版本，安装时一路下一步即可。

安装完成后打开终端验证：

```bash
node -v    # 应该显示 v18.x.x 或更高
npm -v     # 应该显示 9.x.x 或更高
```

**第二步：安装编辑器**

推荐使用 VS Code，安装以下插件：
- Vue - Official（Vue语法高亮和智能提示）
- ESLint（代码规范检查）

### 6.2 安装与运行

```bash
# 1. 进入项目目录
cd dong-medicine-frontend

# 2. 安装依赖（只需要执行一次，会根据package.json下载所有依赖包）
npm install
# 注意：如果下载慢，可以先设置国内镜像
# npm config set registry https://registry.npmmirror.com

# 3. 启动开发服务器（会自动打开浏览器访问 http://localhost:5173）
npm run dev

# 4. 构建生产版本（把源代码编译成可部署的静态文件，输出到dist/目录）
npm run build

# 5. 运行单元测试
npm run test:run

# 6. 代码规范检查（自动修复可修复的问题）
npm run lint
```

### 6.3 环境变量配置

复制 `.env.example` 为 `.env`，根据实际情况修改：

```env
# 后端API地址（开发时Vite会自动代理，生产环境需要改成实际地址）
VITE_API_BASE_URL=/api

# KKFileView文档预览服务地址（Docker部署时通过Nginx代理访问）
VITE_KKFILEVIEW_URL=/kkfileview

# KKFileView获取文件时使用的后端地址（Docker内部网络地址，仅Docker部署时使用）
VITE_KKFILEVIEW_FILE_HOST=http://backend:8080
```

> ⚠️ **Docker部署注意**：`VITE_KKFILEVIEW_FILE_HOST` 必须设置为 `http://backend:8080`（Docker内部网络地址），
> 而不是 `http://localhost:3000`，因为KKFileView容器在Docker内部无法访问宿主机的localhost。
> 开发环境下此变量可留空，会自动使用 `window.location.origin`。

### 6.4 开发代理说明

开发时，Vite 会自动把以下请求转发到后端服务器（localhost:8080），你不需要处理跨域问题：

| 前端请求路径 | 转发到后端 | 用途 |
|------------|----------|------|
| `/api` | `http://localhost:8080` | API接口 |
| `/images` | `http://localhost:8080` | 图片资源 |
| `/videos` | `http://localhost:8080` | 视频资源 |
| `/documents` | `http://localhost:8080` | 文档资源 |
| `/kkfileview` | `http://localhost:8012` | 文档预览服务 |

---

## 七、Docker部署

### 7.1 什么是Docker？（给新手的超简版）

**类比：集装箱**

你写好的代码在你的电脑上能跑，在别人电脑上可能就跑不了（Node版本不同、缺少依赖等）。Docker 就像一个"集装箱"——把你的代码和它需要的所有环境一起打包，放到任何服务器上都能正常运行。

### 7.2 构建和运行

```bash
# 构建Docker镜像（把前端代码打包成可部署的镜像）
docker build -t dong-medicine-frontend .

# 运行容器（启动镜像，把容器的80端口映射到主机的80端口）
docker run -p 80:80 dong-medicine-frontend
```

### 7.3 Docker构建过程解析

Dockerfile 使用了"多阶段构建"——先用 Node.js 环境编译代码，再把编译结果放进 Nginx 服务器：

```
阶段一：构建（Node.js环境）          阶段二：运行（Nginx环境）
+---------------------------+       +---------------------------+
| 1. 安装npm依赖            |       | 1. 复制Nginx配置文件       |
| 2. 运行npm run build      |  -->  | 2. 从阶段一复制dist/目录   |
| 3. 生成dist/目录          |       | 3. 启动Nginx服务          |
+---------------------------+       +---------------------------+
   最终镜像只有Nginx + 静态文件，非常小（~30MB）
```

### 7.4 Nginx配置要点

生产环境用 Nginx 作为 Web 服务器，关键配置：

| 配置项 | 值 | 作用 |
|-------|-----|------|
| 静态资源缓存 | 1年 | JS/CSS/图片等不常变，浏览器缓存后不用重复下载 |
| API代理 | 60秒超时 | 转发API请求到后端 |
| KKFileView代理 | sub_filter+proxy_redirect | 代理文档预览服务，替换内部地址为代理地址 |
| 视频代理 | Range请求支持 | 支持视频分片加载和拖动进度条 |
| SPA回退 | try_files $uri /index.html | 刷新页面时不会404 |
| 安全头 | X-Frame-Options等 | 防止点击劫持、XSS攻击 |
| Gzip压缩 | level 6 | 减小传输体积，加快加载速度 |
| 上传限制 | 100MB | 支持大文件上传 |

---

## 八、常见问题与解决方案

### Q1：`npm install` 报错或很慢

**原因**：npm默认从国外服务器下载，网络可能不稳定。

**解决**：
```bash
# 切换到国内镜像源
npm config set registry https://registry.npmmirror.com

# 然后重新安装
npm install
```

### Q2：`npm run dev` 后页面空白

**可能原因**：
1. 后端服务没启动——检查 `http://localhost:8080` 是否可访问
2. 端口被占用——Vite 会自动尝试下一个端口（5174、5175...），看终端输出

### Q3：修改代码后页面没更新

**解决**：
- Vite 的热更新（HMR）通常会自动刷新，如果没有，手动按 `Ctrl+S` 保存
- 如果还是不行，重启开发服务器：在终端按 `Ctrl+C` 停止，再 `npm run dev`

### Q4：登录后刷新页面就退出登录了

**原因**：本项目使用 `sessionStorage` 存储登录信息，关闭浏览器标签页就会丢失。这是设计选择，不是Bug——对于展示类平台，这样更安全。

### Q5：`npm run build` 后怎么预览？

```bash
# 方法一：安装serve工具预览dist目录
npx serve dist

# 方法二：用Vite的预览命令
npx vite preview
```

### Q6：Docker构建失败

**常见原因**：
1. 网络问题——Dockerfile已配置国内镜像源，如果还慢检查Docker网络设置
2. 内存不足——Node.js构建需要至少2GB内存，Docker Desktop默认可能不够

### Q7：ESLint报一堆红色波浪线

**解决**：
```bash
# 自动修复可修复的问题
npm run lint
```
如果还有无法自动修复的，看错误提示手动修改。

### Q8：组件导入路径中的 `@` 是什么？

`@` 是路径别名，在 `vite.config.js` 中配置的，指向 `src/` 目录：

```javascript
// vite.config.js
resolve: {
  alias: {
    "@": resolve(__dirname, "src"),
  },
}

// 所以这两行是等价的：
import AppHeader from "@/components/business/layout/AppHeader.vue"
import AppHeader from "../../../components/business/layout/AppHeader.vue"  // 相对路径，容易写错
```

---

## 九、项目限制与改进方向

### 当前限制

- AI聊天为同步请求，等待响应时用户体验不佳
- 前端缓存基于sessionStorage，浏览器关闭后丢失
- 部分页面组件较大，可进一步拆分
- 缺少端到端测试

### 未来改进方向

- [ ] AI聊天改为WebSocket流式输出
- [ ] 增加PWA离线支持
- [ ] 增加端到端测试（Playwright/Cypress）
- [ ] 增加国际化（i18n）
- [ ] 组件进一步拆分，提升可维护性
- [ ] 增加Storybook组件文档

---

## 代码审查与改进建议

- [安全] ESLint安全规则过于宽松：no-console:'off'允许生产环境泄露敏感信息，no-undef:'off'关闭未定义变量检查，vue/no-v-html应为'error'
- [安全] AiChatCard.vue使用v-html渲染AI返回内容，存在XSS风险
- [✅已修复] 状态管理：移除了provide/inject模式，统一使用直接import from '@/utils/request'，状态来源一致
- [性能] AdminDashboard.vue和ChartCard.vue全量导入echarts约1MB+，应使用按需导入
- [性能] useMedia.js中computed解构导致响应性丢失
- [✅已修复] 代码重复：三个上传组件(ImageUploader/VideoUploader/DocumentUploader)的重复逻辑已抽取为useFileUpload composable
- [代码重复] PlantGame.vue和QuizSection.vue大量重复UI结构和逻辑
- [✅已修复] 代码重复：密码校验规则已抽取为utils/validators.js共享模块
- [✅已修复] 错误处理：Feedback.vue中logFetchError未导入的ReferenceError已修复
- [错误处理] useAdminData.js中所有API错误被.catch(() => ({}))静默吞没
- [✅已修复] 浏览量统计：各页面showDetail方法中statsData.totalViews未同步更新的问题已修复
- [可访问性] 多处使用div+@click而非语义化标签，键盘用户无法访问
