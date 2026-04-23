# 常量目录 (common/constant/)

> 类比：常量就像药铺里贴在墙上的**规章制度** -- "营业时间 8:00-18:00"、"贵重药品需签字"，这些规定不会变，所有人都要遵守。常量就是代码中不会变的值，统一放在这里管理。

## 目录结构

```
constant/
├── RoleConstants.java   # 角色常量
└── README.md
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
- JWT Token 解析时，角色值规范化后再做权限判断

### 在项目中的使用

#### 1. 在 JwtAuthenticationFilter 中验证角色一致性

```java
// 从Token中获取角色
String tokenRole = tokenInfo.getRole();
// 从数据库中获取当前角色
String currentRole = user.getRole();

// 比较时先规范化，避免大小写不一致导致误判
if (!RoleConstants.normalize(currentRole).equals(RoleConstants.normalize(tokenRole))) {
    // 角色已变更，需要重新登录
    response.getWriter().write("{\"code\":401,\"msg\":\"用户角色已变更，请重新登录\"}");
}
```

**为什么需要规范化？** 数据库中存的是 `"admin"`，但 Token 中可能是 `"ADMIN"` 或 `" Admin "`。如果不规范化，字符串比较会不相等，导致用户被误判为角色变更。

#### 2. 在 CustomUserDetails 中设置权限

```java
public CustomUserDetails(String username, Integer userId, String role) {
    super(username, "",
        Collections.singletonList(
            new SimpleGrantedAuthority(
                role != null && role.startsWith("ROLE_")
                    ? role : "ROLE_" + (role != null ? role.toUpperCase() : "USER")
            )
        ));
    this.userId = userId;
}
```

**注意**：Spring Security 内部使用 `"ROLE_ADMIN"` 格式（带 `ROLE_` 前缀），但我们的数据库和 JWT 中存的是 `"admin"`（不带前缀）。CustomUserDetails 在构造时会自动加上前缀。

#### 3. 在 SecurityUtils 中获取角色

```java
public static String getCurrentUserRole() {
    CustomUserDetails user = getCurrentUser();
    if (user == null) {
        return null;
    }
    return user.getAuthorities().stream()
            .findFirst()
            .map(auth -> auth.getAuthority().replace("ROLE_", ""))  // 去掉 ROLE_ 前缀
            .orElse(RoleConstants.ROLE_USER);  // 默认为普通用户
}
```

#### 4. 在 SecurityConfig 中配置权限

```java
// 只有 admin 角色才能访问管理接口
.requestMatchers("/api/admin/**").hasRole("ADMIN")
// hasRole("ADMIN") 会自动匹配 "ROLE_ADMIN" 权限
// 所以数据库中存 "admin"，CustomUserDetails 转为 "ROLE_ADMIN"，这里用 "ADMIN"
```

### 角色值流转图

```
数据库存储: "admin" (小写，无前缀)
     |
     v
JWT Token: role="admin" (小写，无前缀)
     |
     v
CustomUserDetails: "ROLE_ADMIN" (大写，有前缀)  <-- Spring Security 格式
     |
     v
SecurityConfig: hasRole("ADMIN") (大写，无前缀)  <-- Spring Security 自动加 ROLE_
     |
     v
SecurityUtils: "admin" (小写，无前缀)  <-- 去掉 ROLE_ 前缀后返回
```

---

## 常见错误

**Q: 为什么数据库存 "admin" 而 Spring Security 用 "ROLE_ADMIN"？**
A: 这是 Spring Security 的约定。`hasRole("ADMIN")` 实际上检查的是 `ROLE_ADMIN` 权限。我们的数据库设计更简洁，只存 `"admin"`，在 CustomUserDetails 中转换。

**Q: 角色比较时直接用 `==` 而不是 `equals()`？**
A: 字符串比较必须用 `equals()`。`==` 比较的是对象引用，即使内容相同也可能返回 false。RoleConstants 中的 `isValid()` 方法已经正确使用了 `equals()`。

**Q: 想新增一个角色（如 "moderator"）怎么办？**
A: 需要修改三个地方：
1. `RoleConstants` 中新增常量 `ROLE_MODERATOR = "moderator"`
2. `isValid()` 方法中添加对新角色的判断
3. `SecurityConfig` 中配置新角色的权限规则

**Q: 为什么 RoleConstants 的构造函数是私有的？**
A: 这是**工具类的标准写法**。常量类只提供静态常量和静态方法，不需要创建对象。私有构造函数防止别人 `new RoleConstants()`，这是良好的编码习惯。
