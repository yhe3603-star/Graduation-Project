# 路由配置目录说明

## 文件夹结构

本目录包含项目的路由配置。

```
router/
├── index.js         # 路由配置
└── README.md        # 说明文档
```

## 详细说明

### index.js - 路由配置

**功能**：定义应用的路由规则和导航守卫。

**路由列表**：
| 路径 | 名称 | 组件 | 权限 | 说明 |
|------|------|------|------|------|
| / | Home | Home.vue | 公开 | 首页 |
| /knowledge | Knowledge | Knowledge.vue | 公开 | 知识库 |
| /inheritors | Inheritors | Inheritors.vue | 公开 | 传承人 |
| /plants | Plants | Plants.vue | 公开 | 药材 |
| /qa | Qa | Qa.vue | 公开 | 问答 |
| /interact | Interact | Interact.vue | 公开 | 互动体验 |
| /resources | Resources | Resources.vue | 公开 | 学习资源 |
| /visual | Visual | Visual.vue | 公开 | 可视化 |
| /personal | Personal | PersonalCenter.vue | 需认证 | 个人中心 |
| /admin | Admin | Admin.vue | 需认证+管理员 | 管理后台 |
| /about | About | About.vue | 公开 | 关于我们 |
| /feedback | Feedback | Feedback.vue | 公开 | 意见反馈 |
| /search | Search | GlobalSearch.vue | 公开 | 全局搜索 |
| /:pathMatch(.*)* | NotFound | NotFound.vue | 公开 | 404页面 |

**路由元信息（meta）**：
| 属性 | 类型 | 说明 |
|------|------|------|
| requiresAuth | Boolean | 是否需要登录 |
| requiresAdmin | Boolean | 是否需要管理员权限 |

**导航守卫功能**：
1. **认证检查**：
   - 检查Token是否存在
   - 从sessionStorage恢复状态
   - 验证Token有效性

2. **权限检查**：
   - 需要登录的页面检查Token
   - 需要管理员的页面检查角色
   - 未授权时重定向到首页

3. **Token验证优化**：
   - 60秒内缓存验证结果
   - 避免重复验证
   - Token切换时重新验证

**Token验证流程**：
```javascript
async function validateToken() {
  const userStore = useUserStore()
  if (!userStore.token) return false

  // 缓存检查（60秒内）
  const now = Date.now()
  if (sameToken && now - lastValidationTime < 60000) {
    return tokenValidationPromise
  }

  // 服务端验证
  return userStore.validateToken()
}
```

**路由守卫逻辑**：
```javascript
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 恢复状态
  if (!userStore.token && sessionStorage.getItem('token')) {
    userStore.initializeFromStorage()
  }

  // 认证检查
  if (to.meta.requiresAuth) {
    if (!userStore.token) {
    return next({ path: '/', query: { redirect: to.fullPath, needLogin: 'true' } })
    }

    const isValid = await validateToken()
    if (!isValid) {
    return next({ path: '/', query: { redirect: to.fullPath, needLogin: 'true' } })
    }

    // 管理员检查
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next({ path: '/', query: { noPermission: 'true' } })
    }
  }

  next()
})
```

**重定向参数**：
| 参数 | 说明 |
|------|------|
| redirect | 登录后跳转的目标路径 |
| needLogin | 提示需要登录 |
| noPermission | 提示没有权限 |

**懒加载配置**：
- 所有路由组件使用动态导入（`import()`）
- 支持代码分割和按需加载

---

**最后更新时间**：2026年3月25日
