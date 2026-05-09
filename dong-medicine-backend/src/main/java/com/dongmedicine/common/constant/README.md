# 常量目录 (common/constant/)

> 类比：常量就像药铺里贴在墙上的**规章制度** -- "营业时间 8:00-18:00"、"贵重药品需签字"，这些规定不会变，所有人都要遵守。常量就是代码中不会变的值，统一放在这里管理。

## 目录结构

```
constant/
├── ApiPaths.java           # API路径常量（按模块组织）
├── RabbitMQConstants.java  # RabbitMQ队列/交换机/路由键常量
├── RoleConstants.java      # 角色常量（ROLE_USER/ROLE_ADMIN）
└── TargetType.java         # 目标类型常量（评论/收藏/浏览历史的关联目标类型）
```

---

## RoleConstants -- 角色常量

> 类比：RoleConstants 就像药铺的**岗位铭牌** -- 只有"药师"和"学徒"两种岗位，每个人必须属于其中一种。

### 什么是角色常量？

在侗乡医药数字展示平台中，用户只有两种角色：

| 角色 | 常量值 | 权限 |
|------|--------|------|
| 普通用户 | `ROLE_USER = "user"` | 浏览内容、评论、收藏、测验 |
| 管理员 | `ROLE_ADMIN = "admin"` | 管理所有内容、上传文件、查看统计 |

### 源码解析

```java
public final class RoleConstants {

    // 私有构造函数 -- 防止别人创建实例
    // 常量类不需要创建对象，所有方法都是 static 的
    private RoleConstants() {
    }

    // 两个角色常量
    public static final String ROLE_USER = "user";    // 普通用户
    public static final String ROLE_ADMIN = "admin";  // 管理员

    // 判断角色是否合法
    public static boolean isValid(String role) {
        return ROLE_USER.equals(role) || ROLE_ADMIN.equals(role);
    }

    // 规范化角色值 -- 确保角色值始终是合法的
    public static String normalize(String role) {
        if (role == null || role.isEmpty()) {
            return ROLE_USER;  // 空值默认为普通用户
        }
        String normalized = role.toLowerCase().trim();  // 统一小写、去空格
        return isValid(normalized) ? normalized : ROLE_USER;  // 不合法也默认为普通用户
    }
}
```

### 核心方法详解

#### 1. isValid() -- 角色合法性检查

```java
// 检查角色值是否是合法的 "user" 或 "admin"
boolean valid1 = RoleConstants.isValid("user");    // true
boolean valid2 = RoleConstants.isValid("admin");   // true
boolean valid3 = RoleConstants.isValid("superadmin"); // false
boolean valid4 = RoleConstants.isValid(null);       // false
```

**使用场景**：在注册或修改角色时，验证用户输入的角色是否合法。

#### 2. normalize() -- 角色规范化

```java
// 将各种格式的角色值统一为标准格式
String r1 = RoleConstants.normalize("ADMIN");       // "admin"（大写转小写）
String r2 = RoleConstants.normalize(" User ");      // "user"（去空格+小写）
String r3 = RoleConstants.normalize(null);           // "user"（null默认为user）
String r4 = RoleConstants.normalize("");             // "user"（空串默认为user）
String r5 = RoleConstants.normalize("superadmin");   // "user"（不合法默认为user）
```

**使用场景**：
- 用户注册时，角色字段规范化后再存入数据库
- Sa-Token 会话中角色值规范化后再做权限判断

### 在项目中的使用

#### 1. 在 AdminDataInitializer 中创建管理员

```java
// 创建默认管理员用户
admin.setRole(RoleConstants.ROLE_ADMIN);
admin.setStatus(User.STATUS_ACTIVE);
userService.save(admin);
```

#### 2. 在 StpInterfaceImpl 中获取角色列表

```java
// Sa-Token 权限接口实现
@Override
public List<String> getRoleList(Object loginId, String loginType) {
    User user = userService.getUserInfo(Integer.parseInt(key));
    List<String> roleList = (user != null && user.getRole() != null)
        ? List.of(user.getRole()) : new ArrayList<>();
    return roleList;
}
```

Sa-Token 通过 `StpInterface` 接口获取用户角色列表，角色值直接使用 `"user"` 或 `"admin"`（与数据库存储一致），无需添加 `ROLE_` 前缀。

#### 3. 在 SecurityUtils 中获取角色

```java
public static String getCurrentUserRole() {
    if (!StpUtil.isLogin()) return "user";
    List<String> roles = StpInterfaceImpl.getRoleList(StpUtil.getLoginId(), ...);
    return roles.isEmpty() ? "user" : roles.get(0);
}

public static boolean isAdmin() {
    return ROLE_ADMIN.equals(getCurrentUserRole());
}
```

#### 4. 在 Controller 中使用 @SaCheckRole

```java
// 只有 admin 角色才能访问管理接口
@SaCheckRole("admin")
@DeleteMapping("/api/plants/{id}")
public R<Void> deletePlant(@PathVariable Integer id) { ... }
```

### 角色值流转图

```
数据库存储: "admin" (小写，无前缀)
     |
     v
StpInterfaceImpl.getRoleList(): ["admin"] (直接返回数据库值)
     |
     v
Sa-Token会话: StpUtil.checkRole("admin") / @SaCheckRole("admin")
     |
     v
SecurityUtils: "admin" (直接从角色列表获取)
```

**与Spring Security的区别**：本项目使用 Sa-Token 而非 Spring Security，角色值无需 `ROLE_` 前缀转换，数据库存储值与权限检查值完全一致。

---

## TargetType -- 目标类型常量

> 类比：TargetType 就像药铺的**药材分类标签** -- 每味药材属于"根茎类"、"花叶类"、"果实类"等分类，TargetType 定义了评论、收藏等可以关联的目标类型。

### 源码解析

```java
public final class TargetType {

    private TargetConstants() {}

    // 四种目标类型
    public static final String PLANT = "plant";           // 药用植物
    public static final String KNOWLEDGE = "knowledge";   // 知识库
    public static final String INHERITOR = "inheritor";   // 传承人
    public static final String RESOURCE = "resource";     // 学习资源

    // 验证目标类型是否合法
    public static boolean isValid(String type) {
        return PLANT.equals(type) || KNOWLEDGE.equals(type)
            || INHERITOR.equals(type) || RESOURCE.equals(type);
    }
}
```

### 使用场景

评论、收藏、浏览历史等模块通过 `targetType + targetId` 实现多态关联：

```java
// 评论可以关联不同类型的目标
Comment comment = new Comment();
comment.setTargetType(TargetType.PLANT);      // 评论的是药用植物
comment.setTargetId(plantId);                  // 植物ID

// 收藏也可以关联不同类型的目标
Favorite favorite = new Favorite();
favorite.setTargetType(TargetType.KNOWLEDGE);  // 收藏的是知识条目
favorite.setTargetId(knowledgeId);             // 知识ID
```

### 设计优势

- **统一接口**：不同模块的评论/收藏共用同一张表，通过 `targetType` 区分
- **类型安全**：`isValid()` 方法确保只接受合法的目标类型值
- **可扩展**：新增模块时只需添加新的常量即可

---

## ApiPaths -- API路径常量

> 类比：ApiPaths 就像药铺的**柜台编号** -- "1号窗口抓药"、"2号窗口问诊"，每个接口都有固定的路径。

### 源码解析

```java
public final class ApiPaths {

    private ApiPaths() {}

    // 按模块组织的API路径前缀
    public static final String USER = "/api/user";
    public static final String AUTH = "/api/auth";
    public static final String PLANT = "/api/plants";
    public static final String KNOWLEDGE = "/api/knowledge";
    public static final String INHERITOR = "/api/inheritors";
    public static final String QA = "/api/qa";
    public static final String QUIZ = "/api/quiz";
    public static final String PLANT_GAME = "/api/plant-game";
    public static final String RESOURCE = "/api/resources";
    public static final String COMMENT = "/api/comments";
    public static final String FAVORITE = "/api/favorites";
    public static final String FEEDBACK = "/api/feedback";
    public static final String VISUAL = "/api/visual";
    public static final String ADMIN = "/api/admin";
    public static final String FILE = "/api/files";
    public static final String CHAT = "/api/chat";
    public static final String LEADERBOARD = "/api/leaderboard";
    public static final String DOCUMENT = "/api/documents";
    public static final String METADATA = "/api/metadata";
    public static final String STATS = "/api/stats";
}
```

**当前状态**：Controller中尚未统一使用此常量类，路径仍以字符串硬编码为主。后续重构可逐步替换。

---

## RabbitMQConstants -- RabbitMQ常量

> 类比：RabbitMQConstants 就像药铺的**快递单号规则** -- 每种快递有固定的编号格式，RabbitMQ的队列名、交换机名、路由键也是固定不变的。

### 源码解析

```java
public class RabbitMQConstants {

    // Exchange 名称
    public static final String EXCHANGE_DIRECT = "dong.medicine.direct";
    public static final String EXCHANGE_TOPIC  = "dong.medicine.topic";
    public static final String EXCHANGE_DLX    = "dong.medicine.dlx";

    // Queue 名称
    public static final String QUEUE_OPERATION_LOG = "operation.log.queue";
    public static final String QUEUE_FEEDBACK      = "feedback.queue";
    public static final String QUEUE_FILE_PROCESS  = "file.process.queue";
    public static final String QUEUE_STATISTICS    = "statistics.queue";
    public static final String QUEUE_NOTIFICATION  = "notification.queue";

    // Routing Key
    public static final String ROUTING_KEY_OPERATION_LOG = "operation.log";
    public static final String ROUTING_KEY_FEEDBACK      = "feedback";
    public static final String ROUTING_KEY_FILE_PROCESS  = "file.process";
    public static final String ROUTING_KEY_STATISTICS    = "statistics";
    public static final String ROUTING_KEY_NOTIFICATION  = "notification";

    // Dead Letter Queue
    public static final String DLQ_OPERATION_LOG = "operation.log.dlq";
    public static final String DLQ_FEEDBACK      = "feedback.dlq";
    public static final String DLQ_FILE_PROCESS  = "file.process.dlq";
    public static final String DLQ_STATISTICS    = "statistics.dlq";
    public static final String DLQ_NOTIFICATION  = "notification.dlq";
}
```

**与RabbitMQConfig的关系**：`RabbitMQConfig` 中使用 `@Value` 定义了可配置的同名变量，两者可能存在不一致风险。建议后续统一使用常量类或统一使用配置文件。

---

## 常见错误

**Q: 为什么数据库存 "admin" 而 Sa-Token 用 "admin"？**
A: 本项目使用 Sa-Token，角色值在数据库和权限检查中完全一致，无需像 Spring Security 那样添加 `ROLE_` 前缀。

**Q: 角色比较时直接用 `==` 而不是 `equals()`？**
A: 字符串比较必须用 `equals()`。`==` 比较的是对象引用，即使内容相同也可能返回 false。RoleConstants 中的 `isValid()` 方法已经正确使用了 `equals()`。

**Q: 想新增一个角色（如 "moderator"）怎么办？**
A: 需要修改三个地方：
1. `RoleConstants` 中新增常量 `ROLE_MODERATOR = "moderator"`
2. `isValid()` 方法中添加对新角色的判断
3. `SaTokenConfig` 中配置新角色的权限规则

**Q: 为什么常量类的构造函数是私有的？**
A: 这是**工具类的标准写法**。常量类只提供静态常量和静态方法，不需要创建对象。私有构造函数防止别人 `new RoleConstants()`，这是良好的编码习惯。

**Q: TargetType 的值可以随意新增吗？**
A: 新增目标类型需要同步修改 `isValid()` 方法，并确保对应的Controller和Service已实现。否则评论/收藏关联到不存在的目标类型会导致数据孤立。

---

## 代码审查与改进建议

- **[结构] RabbitMQConstants中硬编码了队列名/交换机名**：而RabbitMQConfig中又用@Value定义了可配置的同名变量，两者可能不一致
- **[规范] ApiPaths未被Controller统一使用**：路径仍以字符串硬编码为主，建议后续重构逐步替换
