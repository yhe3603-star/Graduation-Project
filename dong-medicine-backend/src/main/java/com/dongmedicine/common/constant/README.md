# 常量目录 (constant)

本目录存放项目中使用的常量定义。

## 文件列表

| 文件 | 功能描述 |
|------|----------|
| RoleConstants.java | 用户角色常量定义 |

---

## RoleConstants.java - 角色常量

定义系统中的用户角色常量。

```java
/**
 * 角色常量
 * 定义系统中使用的用户角色
 */
public class RoleConstants {
    
    /**
     * 普通用户角色
     */
    public static final String USER = "USER";
    
    /**
     * 管理员角色
     */
    public static final String ADMIN = "ADMIN";
    
    /**
     * 角色名称映射
     */
    public static final Map<String, String> ROLE_NAMES = Map.of(
        USER, "普通用户",
        ADMIN, "管理员"
    );
}
```

---

## 使用示例

```java
// 在代码中使用常量
if (RoleConstants.ADMIN.equals(user.getRole())) {
    // 管理员逻辑
}

// 获取角色显示名称
String displayName = RoleConstants.ROLE_NAMES.get(user.getRole());
```

---

## 最佳实践

### 1. 使用常量而非魔法值

```java
// ✅ 好的做法：使用常量
if (RoleConstants.ADMIN.equals(role)) { ... }

// ❌ 不好的做法：使用魔法值
if ("ADMIN".equals(role)) { ... }
```

### 2. 常量命名规范

```java
// 常量使用大写字母和下划线
public static final String USER_ROLE = "USER";
public static final int MAX_RETRY_COUNT = 3;
```

---

**最后更新时间**：2026年4月3日
