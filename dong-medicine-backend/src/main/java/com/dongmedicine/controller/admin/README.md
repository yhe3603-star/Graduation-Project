# Admin Controller -- 管理后台控制器

> 本目录存放管理后台的4个专职Controller，统一受 `@SaCheckRole("admin")` 保护，仅管理员可访问。
> 类比：药铺的"后堂" -- 只有掌柜（admin角色）才能进入，负责盘点库存、审核药方、管理伙计。

---

## 一、目录结构

```
controller/admin/
├── AdminContentController.java      # 内容管理 -- 五大模块CRUD
├── AdminInteractionController.java  # 互动管理 -- 反馈回复 + 评论审核
├── AdminStatsController.java        # 数据统计 -- 仪表盘/增长/排行/关键词
└── AdminUserController.java         # 用户管理 -- 列表/删除/角色/封禁
```

---

## 二、架构定位

Admin Controller 是前端管理后台的"数据枢纽"，所有 `/api/admin/*` 请求在此处理：

```
┌──────────────────────────────────────────────────────────────────────┐
│                     前端管理后台（Vue Admin Panel）                    │
│   仪表盘 │ 内容管理 │ 互动管理 │ 用户管理 │ 日志管理 │ 数据导出        │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     Admin Controller 层                               │
│                                                                      │
│  AdminStatsController     AdminContentController                     │
│  ├─ GET /stats            ├─ CRUD /inheritors                        │
│  ├─ GET /stats/user-growth├─ CRUD /knowledge                        │
│  ├─ GET /stats/content-   ├─ CRUD /plants                            │
│  │   views                ├─ CRUD /qa                                │
│  └─ GET /stats/search-    └─ CRUD /resources                        │
│     keywords                                                         │
│                                                                      │
│  AdminInteractionController   AdminUserController                    │
│  ├─ 反馈: list/reply/delete   ├─ GET /users                          │
│  └─ 评论: list/approve/       ├─ DELETE /users/{id}                  │
│         reject/delete          ├─ PUT /users/{id}/role               │
│                                ├─ PUT /users/{id}/ban                │
│                                └─ PUT /users/{id}/unban              │
└────────┬───────────┬────────────┬────────────┬──────────────────────┘
         │           │            │            │
         ▼           ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                          Service 层                                   │
│  UserService │ PlantService │ KnowledgeService │ FeedbackService ... │
└──────────────────────────────────────────────────────────────────────┘
```

> **注意**：`ExportController` 和 `OperationLogController` 虽然也属于管理后台，但它们位于 `controller/` 根目录而非 `admin/` 子目录。

---

## 三、公共特征

4个Admin Controller共享以下编程规范：

```java
@Tag(name = "后台管理-xxx", description = "xxx")   // Swagger文档分组
@RestController
@RequestMapping("/api/admin")                       // 统一路径前缀
@Validated                                          // 启用参数校验
@SaCheckRole("admin")                               // 类级admin角色校验
@RequiredArgsConstructor                            // 构造器注入
public class AdminXxxController {
    // ...
}
```

| 特征 | 说明 |
|------|------|
| 路径前缀 | 统一 `/api/admin` |
| 权限控制 | 类级 `@SaCheckRole("admin")`，所有接口仅admin可访问 |
| 参数校验 | `@Validated` + `@Valid` + `@NotNull` / `@NotBlank` |
| 分页工具 | `PageUtils.getPage()` + `PageUtils.toMap()` |
| 依赖注入 | `@RequiredArgsConstructor` + `private final` 字段 |

---

## 四、各文件详解

### 4.1 AdminStatsController -- 数据统计

> 类比：药铺的"账房先生" -- 每天盘算进账、客流、热销药材，为掌柜提供决策依据。

**核心职责**：为管理后台仪表盘提供4类统计数据。

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `userService` | UserService | 用户总数统计 |
| `knowledgeService` | KnowledgeService | 知识库总数 + 浏览排行 |
| `inheritorService` | InheritorService | 传承人总数 + 浏览排行 |
| `plantService` | PlantService | 植物总数 + 浏览排行 |
| `qaService` | QaService | 问答总数 |
| `resourceService` | ResourceService | 资源总数 |
| `quizService` | QuizService | 测验题目数 |
| `commentService` | CommentService | 评论总数 |
| `feedbackService` | FeedbackService | 反馈总数 |
| `userMapper` | UserMapper | 近7天用户增长（自定义SQL） |
| `searchHistoryMapper` | SearchHistoryMapper | 热门搜索关键词（自定义SQL） |

**接口清单**：

| 方法 | 路径 | 核心逻辑 |
|------|------|---------|
| GET | `/api/admin/stats` | 聚合9个Service的 `count()` 结果，返回 `Map<String, Long>` |
| GET | `/api/admin/stats/user-growth` | 调用 `userMapper.countByDateLast7Days()` 自定义SQL，拆分为 dates + counts 两个数组 |
| GET | `/api/admin/stats/content-views` | 分别取植物/知识/传承人各Top10浏览量，合并后按浏览量降序取前10 |
| GET | `/api/admin/stats/search-keywords` | 调用 `searchHistoryMapper.topKeywords(10)` 自定义SQL |

**关键算法 -- 内容浏览排行混合排序**：

```java
// 从三个模块各取Top10，合并后全局排序取Top10
List<Map<String, Object>> all = new ArrayList<>();
all.addAll(plantService.topByViewCount(10));      // 植物Top10
all.addAll(knowledgeService.topByViewCount(10));   // 知识Top10
all.addAll(inheritorService.topByViewCount(10));   // 传承人Top10

// 按浏览量降序排序
all.sort((a, b) -> Long.compare(
    ((Number) b.get("value")).longValue(),
    ((Number) a.get("value")).longValue()));

// 取全局前10
return R.ok(all.subList(0, Math.min(10, all.size())));
```

**设计思路**：`stats()` 接口一次性聚合9个模块的计数，前端仪表盘只需一次请求即可获取全部概览数据，减少网络开销。

---

### 4.2 AdminInteractionController -- 互动管理

> 类比：药铺的"坐堂问诊台" -- 管理员在此查看顾客反馈、审核留言，决定哪些可以公开展示。

**核心职责**：管理用户反馈（回复/删除）和评论（审核/删除）。

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `feedbackService` | FeedbackService | 反馈列表查询、回复、删除 |
| `commentService` | CommentService | 评论列表查询、审核、删除 |

**接口清单**：

#### 反馈管理

| 方法 | 路径 | 核心逻辑 |
|------|------|---------|
| GET | `/api/admin/feedback` | LambdaQueryWrapper按status筛选，默认按创建时间降序，支持分页 |
| PUT | `/api/admin/feedback/{id}/reply` | 调用 `feedbackService.replyFeedback(id, reply)` 回复反馈 |
| DELETE | `/api/admin/feedback/{id}` | 直接调用 `feedbackService.removeById(id)` 删除 |

**反馈列表筛选逻辑**：

```java
LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
    .orderByDesc(Feedback::getCreatedAt);   // 默认按时间降序

if (!"all".equalsIgnoreCase(status)) {
    wrapper.eq(Feedback::getStatus, status);  // 按状态筛选：pending/resolved
}
```

#### 评论管理

| 方法 | 路径 | 核心逻辑 |
|------|------|---------|
| GET | `/api/admin/comments` | 调用 `commentService.pageAllDTO(status, page, size)` 返回DTO分页 |
| PUT | `/api/admin/comments/{id}/approve` | 调用 `commentService.approveComment(id)` 审核通过 |
| PUT | `/api/admin/comments/{id}/reject` | 调用 `commentService.rejectComment(id)` 审核拒绝 |
| DELETE | `/api/admin/comments/{id}` | 直接调用 `commentService.removeById(id)` 删除 |

**设计思路**：评论审核采用"先审后发"模式，新评论默认 `status=pending`，管理员可 `approve`（通过）或 `reject`（拒绝），通过后前端才展示。

---

### 4.3 AdminContentController -- 内容管理

> 类比：药铺的"药材仓库" -- 管理员在此入库新药材、修改药方信息、下架过期品种，五大类药材分门别类管理。

**核心职责**：对5大内容模块（传承人、知识库、药用植物、问答、学习资源）执行完整的CRUD操作。

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `inheritorService` | InheritorService | 传承人CRUD + 缓存清理 + 关联文件删除 |
| `knowledgeService` | KnowledgeService | 知识CRUD + 缓存清理 + 关联文件删除 |
| `plantService` | PlantService | 植物CRUD + 缓存清理 + 关联文件删除 |
| `qaService` | QaService | 问答CRUD（无缓存清理，无关联文件） |
| `resourceService` | ResourceService | 资源CRUD + 缓存清理 + 关联文件删除 |

**接口清单**（每个模块4个接口，共20个）：

| 模块 | GET列表 | POST新增 | PUT更新 | DELETE删除 |
|------|---------|---------|---------|-----------|
| 传承人 | `/api/admin/inheritors` | `/api/admin/inheritors` | `/api/admin/inheritors/{id}` | `/api/admin/inheritors/{id}` |
| 知识库 | `/api/admin/knowledge` | `/api/admin/knowledge` | `/api/admin/knowledge/{id}` | `/api/admin/knowledge/{id}` |
| 药用植物 | `/api/admin/plants` | `/api/admin/plants` | `/api/admin/plants/{id}` | `/api/admin/plants/{id}` |
| 问答 | `/api/admin/qa` | `/api/admin/qa` | `/api/admin/qa/{id}` | `/api/admin/qa/{id}` |
| 学习资源 | `/api/admin/resources` | `/api/admin/resources` | `/api/admin/resources/{id}` | `/api/admin/resources/{id}` |

**CRUD模式统一实现**：

```java
// 新增（以传承人为例）
@PostMapping("/inheritors")
public R<String> createInheritor(@RequestBody @Valid InheritorCreateDTO dto) {
    Inheritor inheritor = new Inheritor();
    BeanUtils.copyProperties(dto, inheritor);   // DTO → Entity 属性拷贝
    inheritorService.save(inheritor);            // MyBatis-Plus save
    inheritorService.clearCache();               // 清除Redis缓存
    return R.ok("新增传承人成功");
}

// 更新
@PutMapping("/inheritors/{id}")
public R<String> updateInheritor(@PathVariable @NotNull Integer id,
                                  @RequestBody @Valid InheritorUpdateDTO dto) {
    Inheritor inheritor = new Inheritor();
    BeanUtils.copyProperties(dto, inheritor);
    inheritor.setId(id);                         // 手动设置ID
    inheritorService.updateById(inheritor);       // MyBatis-Plus updateById
    inheritorService.clearCache();               // 清除Redis缓存
    return R.ok("更新传承人成功");
}

// 删除
@DeleteMapping("/inheritors/{id}")
public R<String> deleteInheritor(@PathVariable @NotNull Integer id) {
    inheritorService.deleteWithFiles(id);         // 删除记录 + 关联文件
    return R.ok("删除传承人成功");
}
```

**关键设计 -- 缓存清理与文件删除**：

| 操作 | 缓存清理 | 关联文件删除 | 说明 |
|------|---------|------------|------|
| 新增(C) | `clearCache()` | 不需要 | 新数据写入后需清除列表缓存 |
| 更新(U) | `clearCache()` | 不需要 | 修改数据后需清除详情/列表缓存 |
| 删除(D) | 不需要 | `deleteWithFiles()` | 删除记录时同步删除images/videos/documents中的文件 |

> **问答模块特殊**：QaService没有 `clearCache()` 和 `deleteWithFiles()` 方法，删除时直接调用 `removeById()`。因为问答数据不涉及文件关联，且缓存策略与其他模块不同。

---

### 4.4 AdminUserController -- 用户管理

> 类比：药铺的"人事管理" -- 掌柜查看伙计名册、调整岗位、处分违规者。

**核心职责**：管理平台用户的列表查看、删除、角色变更、封禁/解封。

**依赖注入**：

| 依赖 | 类型 | 用途 |
|------|------|------|
| `userService` | UserService | 用户列表、删除、角色更新、封禁/解封 |

**接口清单**：

| 方法 | 路径 | 核心逻辑 |
|------|------|---------|
| GET | `/api/admin/users` | 分页查询，**密码字段置null**防止泄露 |
| DELETE | `/api/admin/users/{id}` | 调用 `userService.deleteUser(id)` 删除用户 |
| PUT | `/api/admin/users/{id}/role` | 调用 `userService.updateUserRole(id, role)` 修改角色 |
| PUT | `/api/admin/users/{id}/ban` | 调用 `userService.banUser(id, reason)` 封禁用户 |
| PUT | `/api/admin/users/{id}/unban` | 调用 `userService.unbanUser(id)` 解封用户 |

**安全设计 -- 密码字段脱敏**：

```java
@GetMapping("/users")
public R<Map<String, Object>> listUsers(...) {
    Page<User> pageResult = userService.page(PageUtils.getPage(page, size));
    // 关键：将密码哈希置null，防止通过API泄露
    pageResult.getRecords().forEach(u -> u.setPasswordHash(null));
    return R.ok(PageUtils.toMap(pageResult));
}
```

**封禁机制**：

```
用户状态流转：

  active ────── banUser(id, reason) ──────► banned
     ▲                                        │
     └──────────── unbanUser(id) ─────────────┘
```

封禁时可附加原因（`reason`参数可选），解封时无需原因。

---

## 五、文件间依赖关系

```
AdminStatsController ──依赖──► UserService / KnowledgeService / InheritorService
                     ──依赖──► PlantService / QaService / ResourceService
                     ──依赖──► QuizService / CommentService / FeedbackService
                     ──依赖──► UserMapper / SearchHistoryMapper（自定义SQL）

AdminInteractionController ──依赖──► FeedbackService / CommentService

AdminContentController ──依赖──► InheritorService / KnowledgeService
                        ──依赖──► PlantService / QaService / ResourceService

AdminUserController ──依赖──► UserService
```

4个Controller之间**无直接依赖**，各自独立处理不同业务领域，符合单一职责原则。

---

## 六、与前端管理后台的交互流程

```
┌─────────────────────────────────────────────────────────────────┐
│  前端管理后台                                                     │
│                                                                  │
│  1. 管理员登录 → 获取Sa-Token（含admin角色）                       │
│  2. 请求 /api/admin/* → Header携带 Authorization: Bearer xxx    │
│  3. Sa-Token校验 → @SaCheckRole("admin") 通过                    │
│  4. Controller处理 → 返回 R<T> 统一响应                           │
└─────────────────────────────────────────────────────────────────┘
```

**请求示例**：

```bash
# 获取仪表盘统计
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/admin/stats

# 回复反馈
curl -X PUT -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"reply":"感谢您的反馈，我们已修复该问题"}' \
     http://localhost:8080/api/admin/feedback/1/reply

# 新增药用植物
curl -X POST -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"nameCn":"钩藤","nameDong":"gens naeml","category":"根茎类",...}' \
     http://localhost:8080/api/admin/plants

# 封禁用户
curl -X PUT -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/admin/users/3/ban?reason=违规发言"
```
