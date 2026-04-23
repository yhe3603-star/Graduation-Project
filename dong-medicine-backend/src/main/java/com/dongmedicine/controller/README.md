# Controller 层 -- 控制器目录

> Controller 是三层架构中的"服务员"层，负责接待顾客（前端请求），把订单传给厨房（Service），再把做好的菜端给顾客。

---

## 一、什么是 Controller？

### 生活类比：餐厅服务员

```
顾客（前端）走进餐厅
       |
       v
服务员（Controller）迎上去
       |
       +-- "您好，看菜单"        --> GET 请求（查询数据）
       +-- "好的，帮您下单"      --> POST 请求（创建数据）
       +-- "这道菜帮您换一下"    --> PUT 请求（更新数据）
       +-- "好的，帮您退掉"      --> DELETE 请求（删除数据）
       |
       v
服务员把订单交给厨房（Service）
       |
       v
厨房做好菜，服务员端给顾客
       |
       v
顾客收到菜（JSON 响应）
```

**Controller 的核心原则：只做"传话人"，不做"厨师"。**

- 该做的：接收请求、提取参数、校验参数、调用 Service、返回结果
- 不该做的：写业务逻辑、直接操作数据库、处理复杂计算

---

## 二、核心注解详解

### 2.1 @RestController -- "我是服务员"

```java
@RestController   // 告诉 Spring：这个类是接收 HTTP 请求的控制器
@RequestMapping("/api/plants")  // 这个控制器处理所有 /api/plants 开头的请求
public class PlantController {
    // ...
}
```

**@RestController = @Controller + @ResponseBody**

| 注解 | 作用 |
|------|------|
| `@Controller` | 标记这是一个控制器类 |
| `@ResponseBody` | 方法的返回值自动转成 JSON 返回给前端 |

> 如果只用 `@Controller` 不加 `@ResponseBody`，Spring 会把返回值当作页面名称去查找 HTML 模板。我们做的是前后端分离项目，只需要返回 JSON 数据，所以用 `@RestController`。

### 2.2 @RequestMapping -- "我的工位在哪"

```java
@RequestMapping("/api/plants")  // 所有请求的公共前缀
public class PlantController {

    @GetMapping("/list")       // 完整路径：GET /api/plants/list
    public R<Map<String, Object>> list(...) { ... }

    @GetMapping("/{id}")       // 完整路径：GET /api/plants/5
    public R<Plant> detail(...) { ... }

    @PostMapping("/{id}/view") // 完整路径：POST /api/plants/5/view
    public R<String> incrementView(...) { ... }
}
```

### 2.3 HTTP 方法注解 -- "顾客要做什么操作"

| 注解 | HTTP方法 | 用途 | 类比 |
|------|---------|------|------|
| `@GetMapping` | GET | 查询数据 | 看菜单 |
| `@PostMapping` | POST | 创建数据 | 下新订单 |
| `@PutMapping` | PUT | 更新数据 | 修改订单 |
| `@DeleteMapping` | DELETE | 删除数据 | 取消订单 |

**RESTful 风格的 URL 设计：**

```
GET    /api/plants/list      --> 查询植物列表
GET    /api/plants/5         --> 查询ID为5的植物
POST   /api/plants           --> 新增一个植物
PUT    /api/plants/5         --> 修改ID为5的植物
DELETE /api/plants/5         --> 删除ID为5的植物
```

---

## 三、请求参数的三种方式

### 3.1 @PathVariable -- 从 URL 路径中提取

```java
// 请求：GET /api/plants/5
//                              URL 中的 {id} 被提取为 id=5
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable @NotNull Integer id) {
    Plant plant = service.getDetailWithStory(id);
    return plant == null ? R.error("植物不存在") : R.ok(plant);
}
```

**适用场景：** 获取指定 ID 的资源详情、删除指定资源等。

### 3.2 @RequestParam -- 从 URL 查询字符串中提取

```java
// 请求：GET /api/plants/list?page=1&size=12&category=清热药&keyword=钩藤
@GetMapping("/list")
public R<Map<String, Object>> list(
        @RequestParam(defaultValue = "1") Integer page,        // 有默认值
        @RequestParam(defaultValue = "12") Integer size,       // 有默认值
        @RequestParam(required = false) String category,       // 可选参数
        @RequestParam(required = false) String keyword) {      // 可选参数
    // ...
}
```

| 属性 | 作用 | 示例 |
|------|------|------|
| `defaultValue` | 参数不存在时的默认值 | `defaultValue = "1"` |
| `required` | 是否必填（默认 true） | `required = false` |
| `name` | 指定参数名（字段名不同时） | `name = "page_num"` |

**适用场景：** 分页、筛选、搜索等查询条件。

### 3.3 @RequestBody -- 从请求体中提取（JSON）

```java
// 请求：POST /api/user/login
// 请求体：{"username": "admin", "password": "Admin123456"}
@PostMapping("/login")
@RateLimit(value = 5, key = "user_login")
public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
    // dto.getUsername() --> "admin"
    // dto.getPassword() --> "Admin123456"
    String token = service.login(dto.getUsername(), dto.getPassword());
    return R.ok(Map.of("token", token, ...));
}
```

**适用场景：** 提交表单、创建资源、登录注册等，数据量较大或包含敏感信息时。

> **三种参数方式对比：**
>
> | 方式 | 数据位置 | 适用场景 | 例子 |
> |------|---------|---------|------|
> | @PathVariable | URL路径 | 资源标识 | /api/plants/5 |
> | @RequestParam | URL查询串 | 筛选条件 | ?page=1&keyword=钩藤 |
> | @RequestBody | 请求体(JSON) | 提交数据 | {"name":"钩藤"} |

---

## 四、统一响应格式 R\<T\>

所有 Controller 方法都返回 `R<T>` 类型，保证前端收到的响应格式一致：

```java
public class R<T> {
    private int code;        // 状态码：200成功，400参数错误，401未登录，403无权限，500服务器错误
    private String msg;      // 提示信息
    private T data;          // 具体数据（泛型，可以是任何类型）
    private String requestId; // 请求追踪ID，方便排查问题
}
```

### 常用方法

```java
// 成功响应
R.ok()                        // {"code":200, "msg":"success", "data":null}
R.ok(plant)                   // {"code":200, "msg":"success", "data":{植物对象}}
R.ok("注册成功")               // {"code":200, "msg":"success", "data":"注册成功"}

// 失败响应
R.error("植物不存在")          // {"code":500, "msg":"植物不存在", "data":null}
R.badRequest("参数错误")       // {"code":400, "msg":"参数错误", "data":null}
R.unauthorized("请先登录")     // {"code":401, "msg":"请先登录", "data":null}
R.forbidden("权限不足")        // {"code":403, "msg":"权限不足", "data":null}
R.notFound("资源不存在")       // {"code":404, "msg":"资源不存在", "data":null}
```

### 为什么需要统一格式？

```
没有统一格式时，前端开发者崩溃了：
  接口A返回：{"status": "ok", "result": {...}}
  接口B返回：{"success": true, "data": {...}}
  接口C返回：{"code": 0, "info": {...}}
  接口D返回：直接报500错误，没有JSON

有了统一格式后，前端开发者很开心：
  所有接口都返回：{"code": 200, "msg": "success", "data": {...}, "requestId": "..."}
  只需判断 code === 200 就知道是否成功
```

---

## 五、速率限制 @RateLimit

### 什么是速率限制？

**生活类比：** 药铺门口放个叫号机，每分钟只叫5个号。防止有人恶意刷号（暴力破解密码、恶意注册等）。

### 怎么用？

```java
@PostMapping("/login")
@RateLimit(value = 5, key = "user_login")   // 每秒最多5次登录请求
public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
    // ...
}

@PostMapping("/register")
@RateLimit(value = 3, key = "user_register")  // 每秒最多3次注册请求
public R<String> register(@Valid @RequestBody RegisterDTO dto) {
    // ...
}
```

### @RateLimit 参数说明

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `value` | 10 | 每秒允许的最大请求数 |
| `key` | "" | 限流的唯一标识，用于区分不同接口 |

### 超过限制会怎样？

```json
{
  "code": 429,
  "msg": "操作过于频繁，请稍后再试",
  "data": null,
  "requestId": "x1y2z3"
}
```

### 实现原理

```
请求到达 Controller
       |
       v
RateLimitAspect 切面拦截（AOP）
       |
       +-- 用 Redis 计数器检查：这个 key 在过去1秒内被调用了几次？
       |       |
       |       +-- 未超限 --> 放行，计数器+1
       |       +-- 已超限 --> 抛出 BusinessException(OPERATION_TOO_FREQUENT)
       |
       +-- Redis 不可用？降级到本地令牌桶（LocalTokenBucket）
```

---

## 六、权限控制

### 三种权限级别

```
+-----------------------------------------------------------+
|                    所有接口                                  |
|                                                           |
|  +------------------+  +------------------+  +----------+ |
|  |   permitAll      |  |   authenticated  |  |  ADMIN   | |
|  |   (公开访问)      |  |   (需要登录)      |  | (需管理员)| |
|  |                  |  |                  |  |          | |
|  | 植物列表/详情     |  | 修改密码         |  | 用户管理  | |
|  | 知识列表/详情     |  | 发表评论         |  | 内容CRUD  | |
|  | 传承人列表/详情   |  | 我的收藏         |  | 评论审核  | |
|  | 登录/注册        |  | 退出登录         |  | 反馈回复  | |
|  | 验证码           |  | 我的测验记录     |  | 文件上传  | |
|  | AI聊天           |  |                  |  |          | |
|  +------------------+  +------------------+  +----------+ |
+-----------------------------------------------------------+
```

### 权限控制方式

本项目使用 **Sa-Token** 注解进行权限控制：

| 注解 | 作用 | 使用场景 |
|------|------|----------|
| `@SaCheckLogin` | 验证是否登录 | 需要登录才能访问的接口 |
| `@SaCheckRole("admin")` | 验证角色 | 仅管理员可访问的接口 |
| 无注解 | 公开访问 | 不需要登录的公开接口 |

**示例：**

```java
// 公开接口 -- 任何人都能访问
@GetMapping("/api/plants/list")
public R<Map<String, Object>> list() { ... }

// 需要登录 -- 加 @SaCheckLogin
@GetMapping("/api/user/me")
@SaCheckLogin
public R<User> me() { ... }

// 仅管理员 -- 加 @SaCheckRole("admin")
@RestController
@RequestMapping("/api/admin")
@SaCheckRole("admin")
public class AdminController { ... }
```

### 权限不足时的响应

| 情况 | HTTP状态码 | 响应 |
|------|-----------|------|
| 未登录（没带Token） | 401 | `{"code":401,"msg":"未登录或登录已过期"}` |
| 已登录但权限不够 | 403 | `{"code":403,"msg":"权限不足"}` |

---

## 七、完整示例：PlantController 逐行解析

```java
package com.dongmedicine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dongmedicine.common.R;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Plant;
import com.dongmedicine.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController                    // [1] 标记为REST控制器，返回值自动转JSON
@RequestMapping("/api/plants")     // [2] 所有接口的URL前缀
@Validated                         // [3] 开启参数校验（@NotNull等注解才生效）
@RequiredArgsConstructor           // [4] Lombok：自动生成构造函数，用于依赖注入
public class PlantController {

    private final PlantService service;  // [5] 注入Service（厨师），用final保证不可变

    // ============================================================
    // 接口1：获取植物列表（分页+筛选）
    // 请求：GET /api/plants/list?page=1&size=12&category=清热药&keyword=钩藤
    // ============================================================
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,      // 页码，默认第1页
            @RequestParam(defaultValue = "12") Integer size,     // 每页数量，默认12条
            @RequestParam(required = false) String category,     // 分类筛选（可选）
            @RequestParam(required = false) String usageWay,     // 用法筛选（可选）
            @RequestParam(required = false) String keyword) {    // 关键词搜索（可选）
        Page<Plant> pageResult = service.advancedSearchPaged(keyword, category, usageWay, page, size);
        return R.ok(PageUtils.toMap(pageResult));  // 分页结果转Map返回
    }

    // ============================================================
    // 接口2：搜索植物
    // 请求：GET /api/plants/search?keyword=钩藤&page=1&size=12
    // ============================================================
    @GetMapping("/search")
    public R<Map<String, Object>> search(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        Page<Plant> pageResult = service.searchPaged(keyword, page, size);
        return R.ok(PageUtils.toMap(pageResult));
    }

    // ============================================================
    // 接口3：获取植物详情
    // 请求：GET /api/plants/5
    // ============================================================
    @GetMapping("/{id}")
    public R<Plant> detail(@PathVariable @NotNull Integer id) {  // 从URL路径取id
        Plant plant = service.getDetailWithStory(id);
        return plant == null ? R.error("植物不存在") : R.ok(plant);  // 三元表达式处理空值
    }

    // ============================================================
    // 接口4：获取相似植物
    // 请求：GET /api/plants/5/similar
    // ============================================================
    @GetMapping("/{id}/similar")
    public R<List<Plant>> similar(@PathVariable @NotNull Integer id) {
        return R.ok(service.getSimilarPlants(id));
    }

    // ============================================================
    // 接口5：随机获取植物
    // 请求：GET /api/plants/random?limit=20
    // ============================================================
    @GetMapping("/random")
    public R<List<Plant>> random(
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "数量不能小于1")       // 最小值校验
            @Max(value = 100, message = "数量不能大于100")   // 最大值校验
            Integer limit) {
        return R.ok(service.getRandomPlants(limit));
    }

    // ============================================================
    // 接口6：增加浏览量
    // 请求：POST /api/plants/5/view
    // ============================================================
    @PostMapping("/{id}/view")
    public R<String> incrementView(@PathVariable Integer id) {
        service.incrementViewCount(id);
        return R.ok("ok");
    }
}
```

---

## 八、本项目的所有控制器

| 控制器 | 路径前缀 | 权限 | 说明 |
|--------|---------|------|------|
| PlantController | /api/plants | 公开+认证 | 药用植物列表、详情、搜索、随机 |
| KnowledgeController | /api/knowledge | 公开+认证 | 知识库列表、详情、搜索 |
| InheritorController | /api/inheritors | 公开+认证 | 传承人列表、详情、搜索 |
| QaController | /api/qa | 公开 | 问答列表、搜索 |
| ResourceController | /api/resources | 公开+认证 | 资源列表、详情、下载 |
| UserController | /api/user | 混合 | 注册、登录、登出、改密 |
| CaptchaController | /api/captcha | 公开 | 验证码生成 |
| AdminController | /api/admin | ADMIN | 统一管理接口 |
| CommentController | /api/comments | 公开+认证 | 评论发表、列表 |
| FavoriteController | /api/favorites | 认证 | 收藏管理 |
| FeedbackController | /api/feedback | 混合 | 反馈提交、查看 |
| QuizController | /api/quiz | 混合 | 测验题目、提交 |
| PlantGameController | /api/plant-game | 混合 | 植物识别游戏 |
| ChatController | /api/chat | 混合 | AI聊天 |
| LeaderboardController | /api/leaderboard | 公开 | 排行榜 |
| StatisticsController | /api/stats | 公开 | 统计数据 |
| FileUploadController | /api/upload | ADMIN | 文件上传 |
| OperationLogController | /api/admin/logs | ADMIN | 操作日志 |

---

## 九、常见错误与避免方法

### 错误1：忘记加 @Validated 或 @Valid

```java
// 错误！@NotBlank 注解不会生效，空关键词也能通过
@GetMapping("/search")
public R<Map<String, Object>> search(@RequestParam @NotBlank String keyword) { ... }

// 正确！类上加了 @Validated，方法参数上的校验注解才会生效
@Validated   // <-- 必须在类上添加这个注解
public class PlantController {
    @GetMapping("/search")
    public R<Map<String, Object>> search(@RequestParam @NotBlank String keyword) { ... }
}

// 对于 @RequestBody 的校验，需要在参数前加 @Valid
@PostMapping("/login")
public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) { ... }
```

### 错误2：Controller 里写业务逻辑

```java
// 错误！Controller 不应该包含业务逻辑
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable Integer id) {
    Plant plant = plantMapper.selectById(id);  // 直接调 Mapper
    if (plant.getViewCount() > 1000) {          // 业务判断写在 Controller
        plant.setStory("热门植物：" + plant.getStory());
    }
    return R.ok(plant);
}

// 正确！业务逻辑放在 Service 里
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable Integer id) {
    Plant plant = service.getDetailWithStory(id);  // 只调 Service
    return plant == null ? R.error("植物不存在") : R.ok(plant);
}
```

### 错误3：用 @Autowired 字段注入（不推荐）

```java
// 不推荐！字段注入，不利于测试，隐藏了依赖关系
@Autowired
private PlantService plantService;

// 推荐！构造器注入，依赖关系明确，方便测试
@RequiredArgsConstructor   // Lombok 自动生成构造函数
public class PlantController {
    private final PlantService service;  // final 保证不可变
}
```

### 错误4：返回类型不统一

```java
// 错误！直接返回对象，前端不知道请求是否成功
@GetMapping("/{id}")
public Plant detail(@PathVariable Integer id) {
    return service.getById(id);
}

// 正确！用 R<T> 统一封装
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable Integer id) {
    Plant plant = service.getDetailWithStory(id);
    return plant == null ? R.error("植物不存在") : R.ok(plant);
}
```

### 错误5：忘记在 SecurityConfig 中配置新接口的权限

```java
// 新增了 /api/recipes 接口，但忘记在 SecurityConfig 中配置
// 结果：所有请求都被拦截，返回 401 或 403

// 解决：在 SecurityConfig.filterChain() 中添加权限规则
.requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()
.requestMatchers("/api/admin/recipes/**").hasRole("ADMIN")
```
