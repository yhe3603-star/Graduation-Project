# Java 源代码根目录 (com/dongmedicine/)

> 这是整个后端 Java 代码的"大本营"，所有业务代码都在这个包下面。

---

## 一、什么是 Java 包（Package）？

### 生活类比：文件夹分类

你电脑里的文件夹是怎么组织的？

```
D:\侗族药铺\
  +-- 药材\          <-- 按类型分类
  |   +-- 清热药\
  |   +-- 祛风药\
  +-- 账本\          <-- 按用途分类
  |   +-- 进货记录\
  |   +-- 销售记录\
  +-- 人员\          <-- 按角色分类
      +-- 员工档案\
      +-- 客户档案\
```

Java 包也是一样的道理 -- **按功能分类组织代码，避免混乱**。

```
com.dongmedicine/          <-- 我们的项目根包
  +-- controller/          <-- 所有"服务员"放这里
  +-- service/             <-- 所有"厨师"放这里
  +-- mapper/              <-- 所有"仓库管理员"放这里
  +-- entity/              <-- 所有"货物清单"放这里
  +-- dto/                 <-- 所有"订单表格"放这里
  +-- config/              <-- 所有"规章制度"放这里
  +-- common/              <-- 所有"公共工具"放这里
```

> 为什么要用 `com.dongmedicine` 这么长的名字？这是 Java 的命名规范 -- 用域名的反写作为包名前缀，保证全球唯一。就像你的身份证号一样，不会和别人重复。

---

## 二、三层架构详解

### 完整架构图

```
                        前端发送 HTTP 请求
                              |
                              v
+---------------------------------------------------------------+
|                     Controller 层（服务员）                     |
|                                                               |
|  @RestController  @GetMapping  @PostMapping  @DeleteMapping   |
|                                                               |
|  职责：                                                        |
|  1. 接收请求（从URL、请求体中获取参数）                          |
|  2. 参数校验（@Valid, @NotNull, @NotBlank）                    |
|  3. 调用 Service 处理业务                                      |
|  4. 用 R<T> 封装响应结果返回给前端                              |
|                                                               |
|  不做的事：不写业务逻辑、不直接操作数据库                        |
+-------------------------------+-------------------------------+
                                |
                                v
+---------------------------------------------------------------+
|                      Service 层（厨师）                         |
|                                                               |
|  @Service  @Transactional  @Cacheable  @CacheEvict            |
|                                                               |
|  职责：                                                        |
|  1. 处理业务逻辑（搜索、过滤、计算等）                          |
|  2. 管理缓存（@Cacheable 读缓存, @CacheEvict 清缓存）          |
|  3. 控制事务（@Transactional 保证数据一致性）                   |
|  4. 调用 Mapper 存取数据                                       |
|                                                               |
|  不做的事：不接收HTTP请求、不返回HTTP响应                        |
+-------------------------------+-------------------------------+
                                |
                                v
+---------------------------------------------------------------+
|                    Mapper 层（仓库管理员）                      |
|                                                               |
|  @Mapper  extends BaseMapper<T>                               |
|                                                               |
|  职责：                                                        |
|  1. 继承 BaseMapper 自动获得增删改查方法                        |
|  2. 用 @Select @Update 写自定义 SQL                            |
|  3. 只负责和数据库交互                                         |
|                                                               |
|  不做的事：不写业务逻辑、不知道业务规则                          |
+-------------------------------+-------------------------------+
                                |
                                v
                        +-----------+
                        |  MySQL    |
                        |  数据库    |
                        +-----------+
```

### 辅助层说明

| 层 | 包 | 类比 | 职责 |
|----|-----|------|------|
| **Entity** | `entity/` | 货物清单 | 一个类对应数据库一张表，一个字段对应一列 |
| **DTO** | `dto/` | 订单表格 | 前端和后端之间传递数据的"表格"，可以过滤敏感字段 |
| **Config** | `config/` | 规章制度 | 配置 Spring Boot 的各种行为（安全、缓存、限流等） |
| **Common** | `common/` | 公共工具 | 统一响应格式、异常处理、工具方法 |

---

## 三、各层代码详解

### 3.1 Entity 层 -- 数据库表的 Java 映射

**什么是 Entity？** 一个 Entity 类 = 数据库中的一张表，一个字段 = 表中的一列。

```java
package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data                    // Lombok：自动生成 getter/setter/toString/equals/hashCode
@TableName("plants")    // 告诉 MyBatis-Plus：这个类对应数据库的 plants 表
public class Plant {

    @TableId(type = IdType.AUTO)   // 主键，自增长
    private Integer id;

    // 下面每个字段对应 plants 表的一列
    // MyBatis-Plus 自动把 name_cn 映射为 nameCn（下划线转驼峰）
    private String nameCn;          // 中文名
    private String nameDong;        // 侗语名
    private String scientificName;  // 学名
    private String category;        // 分类
    private String usageWay;        // 用法
    private String habitat;         // 生长环境
    private String efficacy;        // 功效
    private String story;           // 民间故事
    private String images;          // 图片（JSON字符串）
    private String videos;          // 视频（JSON字符串）
    private String documents;       // 文档（JSON字符串）
    private LocalDateTime createdAt; // 创建时间
    private Integer viewCount;       // 浏览次数
    private Integer favoriteCount;   // 收藏次数
}
```

**关键注解说明：**

| 注解 | 作用 | 类比 |
|------|------|------|
| `@Data` | 自动生成 getter/setter 等方法 | 自动写手，帮你写重复代码 |
| `@TableName("plants")` | 指定对应的数据库表名 | 货物标签：这个箱子对应3号货架 |
| `@TableId(type = IdType.AUTO)` | 标记主键，自增长 | 身份证号，每人唯一 |
| `@TableField("password_hash")` | 指定字段对应的列名（不按默认规则时） | 别名标签 |

> **常见错误：** 忘记加 `@Data`，导致字段无法被读取或设置，运行时报 NullPointerException。

### 3.2 DTO 层 -- 数据传输对象

**什么是 DTO？** DTO (Data Transfer Object) 是前端和后端之间传递数据的"表格"。为什么不直接用 Entity？因为 Entity 包含了数据库的所有字段（包括密码等敏感信息），而 DTO 可以只暴露需要的字段。

```java
package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlantDTO {

    private Integer id;

    @NotBlank(message = "中文名不能为空")       // 校验：不能为空
    @Size(max = 100, message = "中文名长度不能超过100字符")  // 校验：最大长度
    private String nameCn;

    @Size(max = 100, message = "侗语名长度不能超过100字符")
    private String nameDong;

    private String category;
    private String efficacy;
    private String story;
    // ... 其他字段

    // Entity 转 DTO 的静态方法
    public static PlantDTO fromEntity(Plant plant) {
        if (plant == null) return null;
        PlantDTO dto = new PlantDTO();
        dto.setId(plant.getId());
        dto.setNameCn(plant.getNameCn());
        // ... 复制需要的字段
        return dto;
    }
}
```

**Entity vs DTO 的区别：**

| 对比项 | Entity | DTO |
|--------|--------|-----|
| 用途 | 和数据库交互 | 和前端交互 |
| 包含密码？ | 可能包含 | 绝对不包含 |
| 校验注解 | 少量 | 丰富（@NotBlank, @Size等） |
| 对应关系 | 一张表 | 可能来自多张表的组合 |

### 3.3 Controller 层 -- 接收请求

```java
package com.dongmedicine.controller;

@RestController          // 告诉 Spring：这是一个接收 HTTP 请求的控制器
@RequestMapping("/api/plants")  // 所有接口的 URL 前缀
@Validated               // 开启参数校验
@RequiredArgsConstructor // Lombok：自动生成构造函数注入
public class PlantController {

    private final PlantService service;  // 注入 Service（厨师）

    // GET /api/plants/list?page=1&size=12&category=清热药
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,      // URL查询参数
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String category,     // 可选参数
            @RequestParam(required = false) String keyword) {
        Page<Plant> pageResult = service.advancedSearchPaged(keyword, category, usageWay, page, size);
        return R.ok(PageUtils.toMap(pageResult));  // 统一返回格式
    }

    // GET /api/plants/5
    @GetMapping("/{id}")
    public R<Plant> detail(@PathVariable @NotNull Integer id) {  // URL路径参数
        Plant plant = service.getDetailWithStory(id);
        return plant == null ? R.error("植物不存在") : R.ok(plant);
    }
}
```

### 3.4 Service 层 -- 业务逻辑

```java
package com.dongmedicine.service;

// 接口：定义"厨师会做什么菜"
public interface PlantService extends IService<Plant> {
    List<Plant> advancedSearch(String keyword, String category, String usageWay);
    Page<Plant> advancedSearchPaged(String keyword, String category, String usageWay, Integer page, Integer size);
    Plant getDetailWithStory(Integer id);
    void incrementViewCount(Integer id);
    void clearCache();
}
```

```java
package com.dongmedicine.service.impl;

@Service   // 告诉 Spring：这是一个业务逻辑类，请管理它的生命周期
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    @Autowired
    private PlantMapper plantMapper;

    @Override
    @Cacheable(value = "plants", key = "'list:' + (#keyword ?: 'all')")  // 缓存结果
    public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
        LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Plant::getNameCn, keyword)
                         .or().like(Plant::getNameDong, keyword)
                         .or().like(Plant::getEfficacy, keyword));
        }
        if (StringUtils.hasText(category)) {
            qw.eq(Plant::getCategory, category);
        }
        return list(qw);  // 调用 Mapper 查询
    }

    @Override
    @CacheEvict(value = "plants", allEntries = true)  // 清除缓存
    public void clearCache() {
        log.info("Plant cache cleared");
    }
}
```

### 3.5 Mapper 层 -- 数据库操作

```java
package com.dongmedicine.mapper;

@Mapper   // 告诉 MyBatis：这是一个数据库映射接口
public interface PlantMapper extends BaseMapper<Plant> {
    // 继承 BaseMapper<Plant> 后，自动拥有以下方法：
    // insert(Plant)          -- 插入
    // deleteById(id)         -- 按ID删除
    // updateById(Plant)      -- 按ID更新
    // selectById(id)         -- 按ID查询
    // selectList(wrapper)    -- 条件查询
    // selectPage(page, wrapper) -- 分页查询
    // ... 等等

    // 自定义SQL：增加浏览量
    @Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    // 自定义SQL：随机获取植物
    @Select("SELECT * FROM plants WHERE id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants)) + 1) LIMIT #{limit}")
    List<Plant> selectRandomPlants(@Param("limit") int limit);
}
```

---

## 四、一个请求的完整旅程

以"获取钩藤详情"为例，看看请求是怎么从浏览器走到数据库再回来的：

```
浏览器输入: GET /api/plants/1
    |
    v
[1] Spring Boot 接收请求，路由到 PlantController.detail()
    |
    v
[2] Controller: @PathVariable 提取 id=1，调用 service.getDetailWithStory(1)
    |
    v
[3] Service: 先查缓存 @Cacheable("plants")
    |
    +-- 缓存命中 --> 直接返回，不走数据库
    |
    +-- 缓存未命中 --> 调用 getById(1)
        |
        v
[4] Mapper: 执行 SQL "SELECT * FROM plants WHERE id = 1"
    |
    v
[5] MySQL 返回数据，MyBatis-Plus 自动映射为 Plant 对象
    |
    v
[6] Service 把结果存入缓存，返回给 Controller
    |
    v
[7] Controller 用 R.ok(plant) 封装成统一格式
    |
    v
[8] Spring Boot 把 R<Plant> 序列化为 JSON 返回给浏览器

浏览器收到:
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "nameCn": "钩藤",
    "nameDong": "jis xenc",
    ...
  },
  "requestId": "a1b2c3d4"
}
```

### 对应的代码追踪

```java
// [1][2] Controller 接收请求
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable @NotNull Integer id) {
    Plant plant = service.getDetailWithStory(id);  // 调用 Service
    return plant == null ? R.error("植物不存在") : R.ok(plant);
}

// [3][6] Service 处理业务
@Override
public Plant getDetailWithStory(Integer id) {
    return getById(id);  // 调用 Mapper（ServiceImpl 提供的方法）
}

// [4][5] Mapper 执行 SQL（BaseMapper 自动提供的 selectById 方法）
// 实际执行: SELECT * FROM plants WHERE id = 1

// [7][8] R.ok() 封装统一响应
public static <T> R<T> ok(T data) {
    return new R<>(200, "success", data, getRequestId());
}
```

---

## 五、如何添加一个新功能模块

假设要添加一个"侗族药方"（Recipe）模块，按以下6步操作：

### 第1步：创建 Entity（货物清单）

```java
// entity/Recipe.java
@Data
@TableName("recipes")
public class Recipe {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String nameCn;         // 方名
    private String nameDong;       // 侗语方名
    private String ingredients;    // 药材组成（JSON）
    private String usage;          // 用法用量
    private String indications;    // 主治
    private String precautions;    // 注意事项
    private LocalDateTime createdAt;
}
```

### 第2步：创建 Mapper（仓库管理员）

```java
// mapper/RecipeMapper.java
@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
    // 继承 BaseMapper 就有了基础增删改查
    // 需要自定义SQL时再添加方法
}
```

### 第3步：创建 Service 接口（厨师菜单）

```java
// service/RecipeService.java
public interface RecipeService extends IService<Recipe> {
    Page<Recipe> searchPaged(String keyword, Integer page, Integer size);
    void clearCache();
}
```

### 第4步：创建 Service 实现类（厨师做菜）

```java
// service/impl/RecipeServiceImpl.java
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements RecipeService {

    @Override
    @Cacheable(value = "recipes", key = "'search:' + (#keyword ?: 'all') + ':' + #page")
    public Page<Recipe> searchPaged(String keyword, Integer page, Integer size) {
        Page<Recipe> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Recipe> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Recipe::getNameCn, keyword)
              .or().like(Recipe::getIndications, keyword);
        }
        return page(pageParam, qw);
    }

    @Override
    @CacheEvict(value = "recipes", allEntries = true)
    public void clearCache() {}
}
```

### 第5步：创建 Controller（服务员）

```java
// controller/RecipeController.java
@RestController
@RequestMapping("/api/recipes")
@Validated
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService service;

    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String keyword) {
        return R.ok(PageUtils.toMap(service.searchPaged(keyword, page, size)));
    }

    @GetMapping("/{id}")
    public R<Recipe> detail(@PathVariable @NotNull Integer id) {
        Recipe recipe = service.getById(id);
        return recipe == null ? R.error("药方不存在") : R.ok(recipe);
    }
}
```

### 第6步：配置安全权限

在 `SecurityConfig.java` 中添加权限规则：

```java
// 在 authorizeHttpRequests 中添加：
.requestMatchers(HttpMethod.GET, "/api/recipes/**").permitAll()     // 公开可读
.requestMatchers("/api/admin/recipes/**").hasRole("ADMIN")           // 管理需要ADMIN
```

### 完整流程图

```
创建 Entity  -->  创建 Mapper  -->  创建 Service 接口
                                            |
                                            v
配置权限  <--  创建 Controller  <--  创建 Service 实现
```

---

## 六、本项目的所有模块

### Entity（13个实体）

| 实体类 | 对应表 | 说明 |
|--------|--------|------|
| Plant | plants | 药用植物 |
| Knowledge | knowledges | 侗医药知识 |
| Inheritor | inheritors | 传承人 |
| Qa | qa | 问答 |
| Resource | resources | 学习资源 |
| User | users | 用户 |
| Comment | comments | 评论 |
| Favorite | favorites | 收藏 |
| Feedback | feedback | 反馈 |
| QuizQuestion | quiz_questions | 测验题目 |
| QuizRecord | quiz_records | 测验记录 |
| PlantGameRecord | plant_game_records | 植物识别游戏记录 |
| OperationLog | operation_logs | 操作日志 |

### Controller（17个控制器）

| 控制器 | 路径前缀 | 说明 |
|--------|---------|------|
| PlantController | /api/plants | 药用植物 |
| KnowledgeController | /api/knowledge | 侗医药知识 |
| InheritorController | /api/inheritors | 传承人 |
| QaController | /api/qa | 问答 |
| ResourceController | /api/resources | 学习资源 |
| UserController | /api/user | 用户认证 |
| AdminController | /api/admin | 后台管理 |
| CommentController | /api/comments | 评论 |
| FavoriteController | /api/favorites | 收藏 |
| FeedbackController | /api/feedback | 反馈 |
| QuizController | /api/quiz | 测验 |
| PlantGameController | /api/plant-game | 植物识别游戏 |
| ChatController | /api/chat | AI聊天 |
| LeaderboardController | /api/leaderboard | 排行榜 |
| StatisticsController | /api/stats | 统计数据 |
| FileUploadController | /api/upload | 文件上传 |
| CaptchaController | /api/captcha | 验证码 |
| OperationLogController | /api/operation-logs | 操作日志 |

---

## 七、常见错误与避免方法

### 错误1：忘记加 @Service 或 @Mapper 注解

```java
// 错误！Spring 不知道这是一个 Bean，注入时会报 NullPointerException
public class PlantServiceImpl { ... }

// 正确！@Service 告诉 Spring 来管理这个类
@Service
public class PlantServiceImpl { ... }
```

### 错误2：Controller 里直接写 SQL

```java
// 错误！违反三层架构，代码无法复用
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable Integer id) {
    Plant plant = plantMapper.selectById(id);  // Controller 不应该直接调用 Mapper
    return R.ok(plant);
}

// 正确！通过 Service 调用
@GetMapping("/{id}")
public R<Plant> detail(@PathVariable Integer id) {
    Plant plant = service.getDetailWithStory(id);  // 通过 Service
    return R.ok(plant);
}
```

### 错误3：Entity 字段名和数据库列名不匹配

```java
// 数据库列名是 password_hash，Java 字段名是 passwordHash
// MyBatis-Plus 默认会把 password_hash 自动映射为 passwordHash（下划线转驼峰）
// 但如果列名和字段名不能自动对应，需要手动指定：
@TableField("password_hash")
private String passwordHash;
```

### 错误4：忘记在 Service 实现类上加缓存注解

```java
// 没有 @Cacheable，每次查询都走数据库，性能差
@Override
public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
    return list(qw);
}

// 加上 @Cacheable，第一次查询走数据库，后续从缓存读取
@Override
@Cacheable(value = "plants", key = "'list:' + (#keyword ?: 'all')")
public List<Plant> advancedSearch(String keyword, String category, String usageWay) {
    return list(qw);
}
```

---

## 八、代码审查与改进建议

- [结构] 时间字段命名不一致：部分Entity使用createdAt，部分使用createTime，但数据库列名都是created_at
- [结构] 依赖注入方式不一致：部分类用@Autowired字段注入，部分用@RequiredArgsConstructor构造器注入
- [安全] SaTokenConfig中大量写操作API路径绕过认证
- [安全] XssFilter对管理员路径完全跳过XSS过滤
- [性能] LoggingAspect和OperationLogAspect功能重叠，每个请求被AOP处理两次
