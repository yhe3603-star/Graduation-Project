# 路由配置目录说明

## 文件夹结构

本目录包含项目的路由配置文件。

```
router/
└── index.js  # 路由配置文件
```

## 详细说明

### index.js

**功能**：配置项目的路由系统，定义所有页面的路由规则。

**主要内容**：
- 导入Vue Router
- 导入所有页面组件
- 定义路由规则
- 配置路由守卫
- 导出路由实例

**路由配置结构**：

```javascript
const routes = [
  {
    path: '/',           // 路由路径
    name: 'Home',        // 路由名称
    component: Home,     // 对应组件
    meta: {              // 路由元信息
      title: '首页'      // 页面标题
    }
  },
  // 其他路由...
]
```

**主要路由**：

| 路径 | 名称 | 组件 | 描述 |
|------|------|------|------|
| `/` | Home | Home.vue | 首页 |
| `/plants` | Plants | Plants.vue | 药材页面 |
| `/inheritors` | Inheritors | Inheritors.vue | 传承人页面 |
| `/knowledge` | Knowledge | Knowledge.vue | 知识库页面 |
| `/resources` | Resources | Resources.vue | 资源页面 |
| `/qa` | Qa | Qa.vue | 问答页面 |
| `/interact` | Interact | Interact.vue | 互动中心 |
| `/about` | About | About.vue | 关于我们 |
| `/feedback` | Feedback | Feedback.vue | 意见反馈 |
| `/search` | GlobalSearch | GlobalSearch.vue | 全局搜索 |
| `/personal` | PersonalCenter | PersonalCenter.vue | 个人中心 |
| `/admin` | Admin | Admin.vue | 管理后台 |
| `/:pathMatch(.*)*` | NotFound | NotFound.vue | 404页面 |

**路由守卫**：

- **全局前置守卫**：在路由跳转前执行，可用于权限验证、页面标题设置等
- **全局后置守卫**：在路由跳转后执行，可用于页面统计等

**权限控制**：

管理后台路由需要管理员权限，通过路由元信息和全局守卫实现权限控制。

**使用方法**：

在 `main.js` 中导入并使用路由：

```javascript
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
```

**路由跳转**：

```javascript
// 方法1：使用router-link组件
<router-link to="/plants">药材</router-link>

// 方法2：使用router.push
router.push('/plants')

// 方法3：使用命名路由
router.push({ name: 'Plants' })

// 方法4：带参数的路由
router.push({ 
  path: '/plants', 
  query: { category: 'herb' } 
})
```

**路由参数**：

- **动态路由参数**：如 `/plants/:id`
- **查询参数**：如 `/plants?category=herb`
- **命名路由**：通过name跳转，如 `{ name: 'PlantDetail', params: { id: 1 } }`

**路由懒加载**：

为了提高页面加载性能，对大型组件使用懒加载：

```javascript
const Admin = () => import('../views/Admin.vue')
```

**导航守卫**：

```javascript
// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '侗乡医药数字展示平台'
  
  // 权限验证
  if (to.path === '/admin' && !isAdmin) {
    next('/')
  } else {
    next()
  }
})
```

## 开发规范

1. **路由命名**：使用PascalCase命名路由名称
2. **路径规范**：使用小写字母，单词间用连字符分隔
3. **元信息**：为每个路由添加title等元信息
4. **懒加载**：对大型组件使用路由懒加载
5. **权限控制**：对需要权限的路由添加权限验证

## 注意事项

- 路由配置应该清晰、简洁
- 避免深层嵌套路由，保持路由结构扁平
- 使用路由守卫时要注意避免无限循环
- 对敏感路由（如管理后台）要进行权限验证

---

**最后更新时间**：2026年3月23日