# 侗乡医药数字展示平台后端

> 基于 Spring Boot 3.1 的侗族医药文化遗产数字化展示平台后端服务

## 目录

- [项目概述](#项目概述)
- [新手入门指南](#新手入门指南)
- [Spring Boot 基础概念](#spring-boot-基础概念)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [API 接口](#api-接口)
- [实体类](#实体类)
- [DTO 数据传输对象](#dto-数据传输对象)
- [配置说明](#配置说明)
- [安全机制](#安全机制)
- [性能优化](#性能优化)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)
- [常见问题](#常见问题)

---

## 项目概述

本项目是侗乡医药数字展示平台的后端服务，提供用户认证、内容管理、互动功能、AI问答等核心功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 用户系统 | 注册、登录、权限管理、Token 刷新、用户状态检查 |
| 验证码 | 图形验证码生成与验证 |
| 内容管理 | 植物、知识、传承人、资源、问答管理 |
| 互动功能 | 评论、收藏、反馈 |
| 游戏化学习 | 测验、植物识别游戏、排行榜 |
| AI问答 | DeepSeek 智能问答，支持消息长度限制 |
| 后台管理 | 数据统计、内容审核、日志管理 |
| 文件上传 | 图片、视频、文档上传管理 |

---

## 新手入门指南

### 我应该从哪里开始？

如果你是第一次接触后端开发，建议按照以下顺序学习：

```
第1步：学习 Java 基础 → 了解面向对象编程
第2步：学习 Spring Boot 基础 → 阅读"Spring Boot 基础概念"
第3步：理解项目架构 → 阅读"项目结构"
第4步：搭建开发环境 → 按照"快速开始"操作
第5步：运行项目 → 启动后端服务
第6步：阅读代码 → 从简单模块开始，逐步深入
```

### 学习路线图

```
┌─────────────────────────────────────────────────────────────────┐
│                        后端学习路线图                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  基础阶段:                                                       │
│  Java基础 → 面向对象 → 集合框架 → 异常处理                         │
│                                                                 │
│  框架阶段:                                                       │
│  Spring Boot基础 → Spring Security → MyBatis Plus                │
│                                                                 │
│  数据库阶段:                                                     │
│  SQL基础 → MySQL使用 → 数据库设计 → 索引优化                       │
│                                                                 │
│  进阶阶段:                                                       │
│  Redis缓存 → JWT认证 → RESTful API → 项目实战                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 推荐学习资源

| 学习内容 | 推荐资源 | 链接 |
|---------|---------|------|
| Java 基础 | 菜鸟教程 | https://www.runoob.com/java/ |
| Spring Boot | Spring 官方指南 | https://spring.io/guides |
| MyBatis Plus | MyBatis Plus 官方文档 | https://baomidou.com/ |
| MySQL | MySQL 教程 | https://www.runoob.com/mysql/mysql-tutorial.html |

---

## Spring Boot 基础概念

### 什么是 Spring Boot？

Spring Boot 是一个用于简化 Spring 应用开发的框架。它提供了：
- **自动配置**：根据依赖自动配置 Spring 应用
- **起步依赖**：一个依赖包含多个相关依赖
- **内嵌服务器**：内置 Tomcat，无需部署 WAR 文件
- **生产就绪**：提供健康检查、指标监控等功能

**通俗理解**：Spring Boot 就像是一个"预制好的房子框架"，它已经帮你搭好了地基、墙壁、屋顶，你只需要往里面放家具（业务代码）就行了。

### 项目结构层次

本项目采用经典的三层架构：

```
┌─────────────────────────────────────────────────────────────────┐
│                        三层架构示意图                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Controller 层                         │   │
│  │                    （控制器层）                           │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  职责：接收HTTP请求，调用Service，返回响应        │    │   │
│  │  │  注解：@RestController, @GetMapping, @PostMapping│    │   │
│  │  │  示例：UserController, PlantController           │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     Service 层                          │   │
│  │                    （服务层）                            │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  职责：处理业务逻辑，调用Mapper                    │    │   │
│  │  │  注解：@Service                                  │    │   │
│  │  │  示例：UserService, PlantService                 │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     Mapper 层                           │   │
│  │                   （数据访问层）                          │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  职责：与数据库交互，执行SQL语句                   │    │   │
│  │  │  注解：@Mapper                                   │    │   │
│  │  │  示例：UserMapper, PlantMapper                   │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    数据库 (MySQL)                        │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 常用注解说明

#### 1. 控制器注解

```java
// @RestController - 标记这是一个控制器类
// 相当于 @Controller + @ResponseBody，方法返回值自动转为JSON
@RestController
@RequestMapping("/api/users")  // 设置这个控制器的基础路径
public class UserController {
    
    // @GetMapping - 处理GET请求
    // 访问路径：GET /api/users/list
    @GetMapping("/list")
    public R<List<User>> list() {
        return R.ok(userService.list());
    }
    
    // @PostMapping - 处理POST请求
    // 访问路径：POST /api/users/add
    @PostMapping("/add")
    public R<String> add(@RequestBody UserDTO dto) {
        userService.add(dto);
        return R.ok("添加成功");
    }
    
    // @PutMapping - 处理PUT请求（更新）
    @PutMapping("/update")
    public R<String> update(@RequestBody UserDTO dto) {
        userService.update(dto);
        return R.ok("更新成功");
    }
    
    // @DeleteMapping - 处理DELETE请求
    // {id} 是路径变量
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Integer id) {
        userService.delete(id);
        return R.ok("删除成功");
    }
}
```

#### 2. 参数注解

```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    // @PathVariable - 获取URL路径中的变量
    // 访问：GET /api/example/user/123
    @GetMapping("/user/{id}")
    public R<User> getById(@PathVariable Integer id) {
        // id = 123
        return R.ok(userService.getById(id));
    }
    
    // @RequestParam - 获取URL查询参数
    // 访问：GET /api/example/search?keyword=张三&page=1
    @GetMapping("/search")
    public R<List<User>> search(
        @RequestParam String keyword,           // 必需参数
        @RequestParam(defaultValue = "1") int page,  // 有默认值
        @RequestParam(required = false) Integer size  // 可选参数
    ) {
        return R.ok(userService.search(keyword, page, size));
    }
    
    // @RequestBody - 获取请求体中的JSON数据
    // 前端发送：POST /api/example/add
    // Body: {"name": "张三", "age": 25}
    @PostMapping("/add")
    public R<String> add(@RequestBody UserDTO dto) {
        // dto.getName() = "张三"
        // dto.getAge() = 25
        return R.ok("添加成功");
    }
}
```

#### 3. 服务层注解

```java
// @Service - 标记这是一个服务类
@Service
// @RequiredArgsConstructor - Lombok注解，生成构造函数
// 用于依赖注入，替代@Autowired
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    // 通过构造函数注入Mapper
    private final UserMapper userMapper;
    
    // @Transactional - 开启事务
    // 方法执行成功自动提交，抛出异常自动回滚
    @Transactional
    public void addUser(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        userMapper.insert(user);
        // 如果这里抛出异常，上面的insert会回滚
    }
}
```

#### 4. 数据访问注解

```java
// @Mapper - 标记这是一个MyBatis Mapper接口
@Mapper
public interface UserMapper {
    
    // MyBatis Plus 提供的基础方法，无需写SQL：
    // - insert(entity) 插入
    // - deleteById(id) 根据ID删除
    // - updateById(entity) 根据ID更新
    // - selectById(id) 根据ID查询
    // - selectList(wrapper) 条件查询列表
    // - selectPage(page, wrapper) 分页查询
    
    // 自定义SQL查询
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    // 自定义更新
    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
}
```

### 实体类 (Entity)

实体类对应数据库中的表：

```java
// @Data - Lombok注解，自动生成getter/setter/toString等方法
@Data
// @TableName - 指定对应的数据库表名
@TableName("users")
public class User {
    
    // @TableId - 标记主键
    // type = IdType.AUTO 表示自增
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    // 普通字段，对应数据库的username列
    private String username;
    
    // @TableField - 字段映射配置
    // fill = FieldFill.INSERT 表示插入时自动填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // @TableLogic - 逻辑删除字段
    // 删除时不是真删除，而是把这个字段设为1
    @TableLogic
    private Integer deleted;
    
    // @Version - 乐观锁版本号
    @Version
    private Integer version;
}
```

### DTO (数据传输对象)

DTO用于接收前端传递的数据：

```java
@Data
public class UserDTO {
    
    // @NotBlank - 字符串不能为空
    @NotBlank(message = "用户名不能为空")
    // @Size - 限制长度
    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    private String username;
    
    // @Pattern - 正则表达式验证
    @Pattern(regexp = "^[a-zA-Z0-9]{8,20}$", message = "密码必须是8-20位字母数字")
    private String password;
    
    // @Email - 邮箱格式验证
    @Email(message = "邮箱格式不正确")
    private String email;
    
    // @Min, @Max - 数字范围验证
    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;
}
```

### 统一响应格式

所有API返回统一的JSON格式：

```java
// R.java - 统一响应封装类
@Data
public class R<T> {
    private int code;        // 状态码：200成功，其他失败
    private String msg;      // 消息
    private T data;          // 数据
    private String requestId; // 请求追踪ID
    
    // 成功响应
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }
    
    // 失败响应
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}
```

**使用示例：**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }
        return R.ok(user);
    }
}
```

**返回结果：**

```json
// 成功
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1,
        "username": "张三"
    },
    "requestId": "abc123"
}

// 失败
{
    "code": 500,
    "msg": "用户不存在",
    "data": null,
    "requestId": "abc123"
}
```

### 异常处理

使用全局异常处理器统一处理异常：

```java
// @RestControllerAdvice - 全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException e) {
        return R.fail(e.getMessage());
    }
    
    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> handleValidException(MethodArgumentNotValidException e) {
        // 获取第一个错误信息
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return R.fail(message);
    }
    
    // 处理其他异常
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail("系统繁忙，请稍后重试");
    }
}
```

### 配置文件

Spring Boot 使用 `application.yml` 作为配置文件：

```yaml
# application.yml - 主配置文件

# 服务器配置
server:
  port: 8080  # 服务端口

# Spring配置
spring:
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password: 
  
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 100MB      # 单个文件最大大小
      max-request-size: 100MB   # 总请求最大大小

# MyBatis Plus配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印SQL日志
  global-config:
    db-config:
      logic-delete-field: deleted  # 逻辑删除字段

# 日志配置
logging:
  level:
    com.dongmedicine: DEBUG  # 项目日志级别
```

### MyBatis Plus 查询

MyBatis Plus 提供了强大的查询构造器：

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    
    // 条件查询
    public List<User> search(String keyword, Integer status) {
        // LambdaQueryWrapper - 使用Lambda表达式构造查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // like - 模糊查询
        wrapper.like(StringUtils.hasText(keyword), User::getUsername, keyword);
        // eq - 等于
        wrapper.eq(status != null, User::getStatus, status);
        // orderByDesc - 降序排序
        wrapper.orderByDesc(User::getCreatedAt);
        
        return userMapper.selectList(wrapper);
    }
    
    // 分页查询
    public Page<User> page(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), User::getUsername, keyword);
        
        return userMapper.selectPage(page, wrapper);
    }
}
```

---

## 技术栈

| 技术 | 版本 | 用途 | 通俗解释 |
|------|------|------|----------|
| Spring Boot | 3.1.12 | 基础框架 | 快速构建后端应用 |
| Spring Security | - | 安全框架 | 处理用户登录权限 |
| MyBatis Plus | 3.5.9 | ORM 框架 | 操作数据库 |
| MySQL | 8.0+ | 数据库 | 存储数据 |
| Redis | 7.0+ | 缓存数据库 | 快速存取临时数据 |
| Caffeine | 3.1+ | 本地缓存 | 内存中的快速缓存 |
| JWT (jjwt) | 0.11.5 | Token 认证 | 用户身份认证 |
| SpringDoc | 2.2.0 | API 文档 | 自动生成API文档 |
| Lombok | 1.18.38 | 简化代码 | 自动生成getter/setter |
| Docker | - | 容器化 | 打包部署 |

---

## 项目结构

```
dong-medicine-backend/
│
├── src/main/java/com/dongmedicine/
│   │
│   ├── common/                          # 公共模块
│   │   ├── constant/                    # 常量定义
│   │   │   └── RoleConstants.java       # 角色常量 (USER, ADMIN)
│   │   │
│   │   ├── exception/                   # 异常处理
│   │   │   ├── BusinessException.java      # 业务异常类
│   │   │   ├── ErrorCode.java              # 错误码定义
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │   │
│   │   ├── util/                        # 工具类
│   │   │   ├── FileCleanupHelper.java      # 文件清理助手
│   │   │   ├── FileTypeUtils.java          # 文件类型工具
│   │   │   ├── PageUtils.java              # 分页工具
│   │   │   ├── PasswordValidator.java      # 密码验证器
│   │   │   ├── SensitiveDataUtils.java     # 敏感信息脱敏
│   │   │   └── XssUtils.java               # XSS防护工具
│   │   │
│   │   ├── R.java                       # 统一响应封装
│   │   └── SecurityUtils.java           # 安全工具类
│   │
│   ├── config/                          # 配置模块
│   │   ├── health/                      # 健康检查
│   │   ├── logging/                     # 日志配置
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   ├── JwtUtil.java                 # JWT 工具类
│   │   ├── CacheConfig.java             # 缓存配置
│   │   └── ...                          # 其他配置类
│   │
│   ├── controller/                      # 控制器层 (17个)
│   │   ├── UserController.java          # 用户接口
│   │   ├── PlantController.java         # 植物接口
│   │   └── ...                          # 其他控制器
│   │
│   ├── dto/                             # 数据传输对象 (18个)
│   ├── entity/                          # 实体类 (13个)
│   ├── mapper/                          # 数据访问层 (13个)
│   ├── service/                         # 服务层
│   │   ├── impl/                        # 服务实现 (15个)
│   │   └── *Service.java                # 服务接口
│   └── DongMedicineBackendApplication.java  # 应用入口
│
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   ├── application-dev.yml              # 开发环境配置
│   ├── application-prod.yml             # 生产环境配置
│   └── logback-spring.xml               # 日志配置
│
├── sql/                                 # 数据库脚本
│   ├── dong_medicine.sql                # 完整数据库脚本
│   ├── fulltext_index.sql               # 全文索引脚本
│   └── optimize_indexes.sql             # 索引优化脚本
│
├── public/                              # 静态资源目录
│   ├── images/                          # 图片资源
│   │   ├── plants/                      # 植物图片 (70+张)
│   │   ├── inheritors/                  # 传承人图片
│   │   ├── knowledge/                   # 知识图片
│   │   └── common/                      # 公共图片
│   └── documents/                       # 文档资源
│
├── test/                                # 测试目录
├── Dockerfile                           # Docker 构建文件
└── pom.xml                              # Maven 配置
```

### 目录职责说明

| 目录 | 职责 | 通俗解释 |
|------|------|----------|
| `controller/` | 控制器层 | 接收HTTP请求，返回响应 |
| `service/` | 服务层 | 处理业务逻辑 |
| `mapper/` | 数据访问层 | 操作数据库 |
| `entity/` | 实体类 | 对应数据库表 |
| `dto/` | 数据传输对象 | 接收前端数据 |
| `config/` | 配置类 | 各种配置 |
| `common/` | 公共模块 | 工具类、常量、异常处理 |

---

## API 接口

### 用户模块 `/api/user`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/login` | 用户登录 | 公开 |
| POST | `/register` | 用户注册 | 公开 |
| GET | `/me` | 获取当前用户信息 | 需登录 |
| POST | `/change-password` | 修改密码 | 需登录 |
| POST | `/logout` | 退出登录 | 需登录 |
| GET | `/validate` | 验证Token | 公开 |
| POST | `/refresh-token` | 刷新Token | 需登录 |

### 验证码模块 `/api/captcha`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/image` | 获取图形验证码 | 公开 |

### 药用植物模块 `/api/plants`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 植物列表(支持分类/用法过滤) | 公开 |
| GET | `/search` | 搜索植物 | 公开 |
| GET | `/{id}` | 植物详情 | 公开 |
| GET | `/{id}/similar` | 相似植物 | 公开 |
| GET | `/random` | 按难度随机获取植物 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |

### 知识库模块 `/api/knowledge`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 知识列表 | 公开 |
| GET | `/search` | 高级搜索(支持疗法/疾病分类) | 公开 |
| GET | `/{id}` | 知识详情 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |

### 传承人模块 `/api/inheritors`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 传承人列表(支持级别过滤) | 公开 |
| GET | `/search` | 搜索传承人 | 公开 |
| GET | `/{id}` | 传承人详情 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |

### 学习资源模块 `/api/resources`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 资源列表(支持分类/类型过滤) | 公开 |
| GET | `/hot` | 热门资源 | 公开 |
| GET | `/{id}` | 资源详情 | 公开 |
| GET | `/download/{id}` | 下载资源文件 | 公开 |
| POST | `/{id}/view` | 增加浏览次数 | 公开 |
| POST | `/{id}/download` | 增加下载次数 | 公开 |

### 评论模块 `/api/comments`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/` | 发表评论 | 需登录 |
| GET | `/list/{targetType}/{targetId}` | 目标评论列表 | 公开 |
| GET | `/list/all` | 所有已审核评论 | 公开 |
| GET | `/my` | 我的评论 | 需登录 |

### 收藏模块 `/api/favorites`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/{targetType}/{targetId}` | 添加收藏 | 需登录 |
| DELETE | `/{targetType}/{targetId}` | 取消收藏 | 需登录 |
| GET | `/my` | 我的收藏列表 | 需登录 |

### 测验模块 `/api/quiz`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/questions` | 获取随机问题 | 公开 |
| POST | `/submit` | 提交答案 | 公开 |
| GET | `/records` | 答题记录 | 需登录 |
| GET | `/list` | 所有问题 | 公开 |
| POST | `/add` | 添加问题 | ADMIN |
| PUT | `/update` | 更新问题 | ADMIN |
| DELETE | `/{id}` | 删除问题 | ADMIN |

### 植物游戏模块 `/api/plant-game`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/submit` | 提交游戏结果 | 公开 |
| GET | `/records` | 游戏记录 | 需登录 |

### AI聊天模块 `/api/chat`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| POST | `/` | 发送聊天消息(DeepSeek AI) | 公开 |
| GET | `/stats` | 聊天统计 | ADMIN |

**请求限制**：
- 消息长度最大 2000 字符
- 历史消息最多 20 条
- 每秒最多 10 次请求

### 排行榜模块 `/api/leaderboard`

| 方法 | 端点 | 说明 | 权限 |
|------|------|------|------|
| GET | `/combined` | 综合排行榜 | 公开 |
| GET | `/quiz` | 问答排行榜 | 公开 |
| GET | `/game` | 游戏排行榜 | 公开 |

### 文件上传模块 `/api/upload` (需要 ADMIN 角色)

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/image` | 上传单个图片 |
| POST | `/images` | 批量上传图片 |
| POST | `/video` | 上传单个视频 |
| POST | `/videos` | 批量上传视频 |
| POST | `/document` | 上传单个文档 |
| POST | `/documents` | 批量上传文档 |
| POST | `/file` | 通用文件上传 |
| DELETE | `/` | 删除文件 |

### 管理后台 `/api/admin` (需要 ADMIN 角色)

| 模块 | 端点 | 功能 |
|------|------|------|
| 仪表盘 | `GET /dashboard` | 数据统计概览 |
| 用户管理 | `GET/DELETE/PUT /users/*` | 用户列表、删除、角色更新、封禁 |
| 内容管理 | `CRUD /inheritors/*` | 传承人管理 |
| | `CRUD /knowledge/*` | 知识管理 |
| | `CRUD /plants/*` | 植物管理 |
| | `CRUD /qa/*` | 问答管理 |
| | `CRUD /resources/*` | 资源管理 |
| 互动管理 | `GET/PUT/DELETE /feedback/*` | 反馈管理 |
| | `GET/PUT/DELETE /comments/*` | 评论审核 |
| 系统管理 | `GET/DELETE /logs/*` | 操作日志 |

---

## 实体类

### User - 用户

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键（自增） |
| username | String | 用户名 |
| passwordHash | String | 密码哈希(BCrypt) |
| role | String | 角色(USER/ADMIN) |
| status | Integer | 状态(0正常/1封禁) |
| createdAt | LocalDateTime | 创建时间 |

### Plant - 药用植物

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| nameCn | String | 中文名称 |
| nameDong | String | 侗语名称 |
| scientificName | String | 学名 |
| category | String | 分类 |
| usageWay | String | 用法方式 |
| habitat | String | 生长环境 |
| efficacy | String | 功效 |
| story | String | 相关故事 |
| images | String | 图片(JSON) |
| videos | String | 视频(JSON) |
| documents | String | 文档(JSON) |
| distribution | String | 分布地区 |
| difficulty | String | 难度级别 |
| viewCount | Integer | 浏览次数 |
| favoriteCount | Integer | 收藏次数 |

### Knowledge - 知识库

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| title | String | 标题 |
| type | String | 类型 |
| therapyCategory | String | 疗法分类 |
| diseaseCategory | String | 疾病分类 |
| content | String | 内容 |
| formula | String | 配方 |
| usageMethod | String | 使用方法 |
| steps | String | 步骤(JSON) |
| images/videos/documents | String | 媒体资源(JSON) |
| relatedPlants | String | 相关植物 |
| viewCount | Integer | 浏览次数 |

### Inheritor - 传承人

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| name | String | 姓名 |
| level | String | 级别(国家级/省级/市级/县级) |
| bio | String | 简介 |
| specialties | String | 专长(JSON) |
| experienceYears | Integer | 从业年限 |
| honors | String | 荣誉(JSON) |
| representativeCases | String | 代表案例 |
| viewCount | Integer | 浏览次数 |

### Resource - 学习资源

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| title | String | 标题 |
| category | String | 分类 |
| files | String | 文件(JSON) |
| description | String | 描述 |
| viewCount | Integer | 浏览次数 |
| downloadCount | Integer | 下载次数 |

### 其他实体

| 实体 | 说明 |
|------|------|
| Comment | 评论，支持嵌套回复 |
| Favorite | 收藏，支持多种资源类型 |
| Feedback | 用户反馈 |
| QuizQuestion | 问答题 |
| QuizRecord | 答题记录 |
| PlantGameRecord | 植物游戏记录 |
| Qa | 常见问答 |
| OperationLog | 操作日志 |

---

## DTO 数据传输对象

### 登录/注册相关

| DTO | 用途 | 主要字段 |
|-----|------|----------|
| LoginDTO | 登录请求 | username, password, captchaKey, captchaCode |
| RegisterDTO | 注册请求 | username, password, confirmPassword, captchaKey, captchaCode |
| CaptchaDTO | 验证码响应 | captchaKey, captchaImage |
| ChangePasswordDTO | 修改密码 | currentPassword, newPassword, confirmPassword, captchaCode |

### 内容相关

| DTO | 用途 |
|-----|------|
| PlantDTO | 植物数据传输 |
| KnowledgeDTO | 知识数据传输 |
| InheritorDTO | 传承人数据传输 |

### 互动相关

| DTO | 用途 |
|-----|------|
| CommentDTO | 评论数据 |
| CommentAddDTO | 添加评论请求 |
| FeedbackDTO | 反馈数据 |
| FeedbackReplyDTO | 反馈回复 |

### 游戏相关

| DTO | 用途 |
|-----|------|
| QuizQuestionDTO | 测验问题 |
| QuizSubmitDTO | 提交答案 |
| AnswerDTO | 答案数据 |
| PlantGameSubmitDTO | 植物游戏提交 |

### AI相关

| DTO | 用途 |
|-----|------|
| ChatRequest | AI聊天请求(含消息长度限制) |
| ChatResponse | AI聊天响应 |

---

## 配置说明

### application.yml - 主配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

app:
  security:
    jwt-secret: ${JWT_SECRET:...}
    jwt-expiration: ${JWT_EXPIRATION:86400000}
    cors-allowed-origins:
      - ${CORS_ORIGIN_1:http://localhost:5173}
  cache:
    enabled: true
    max-size: 1000
    expire-minutes: 60
  request:
    max-body-size: ${MAX_BODY_SIZE:10485760}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
```

### application-dev.yml - 开发环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dong_medicine
    username: root
    password: 123456

  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.dongmedicine: DEBUG

file:
  upload:
    base-path: ${user.dir}/public
    image-max-size: 10485760      # 10MB
    video-max-size: 104857600     # 100MB
    document-max-size: 52428800   # 50MB

deepseek:
  api-key: ${DEEPSEEK_API_KEY:}
  base-url: https://api.deepseek.com
  model: deepseek-chat
```

### application-prod.yml - 生产环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:dong_medicine}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}

jwt:
  secret: ${JWT_SECRET}

file:
  upload:
    base-path: ${FILE_UPLOAD_PATH:/opt/dong-medicine/backend/public}

logging:
  level:
    com.dongmedicine: INFO
```

---

## 安全机制

### 1. 认证与授权

```java
// JWT认证过滤器
// - Token验证
// - 用户状态检查（封禁用户无法访问）
// - Token黑名单检查

// JwtAuthenticationFilter.java
if (user.isBanned()) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write("{\"code\":403,\"msg\":\"账号已被封禁\"}");
    return;
}
```

### 2. Token黑名单

```java
// TokenBlacklistServiceImpl.java
// - Redis主存储
// - Caffeine本地缓存降级
// - 自动过期（2小时）

private final Cache<String, Boolean> localBlacklist = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(2, TimeUnit.HOURS)
    .build();
```

### 3. 密码安全

```java
// PasswordValidator.java
// - 长度 8-50 位
// - 必须包含字母和数字
// - 不能包含空格
// - BCrypt 加密存储
```

### 4. XSS 防护

```java
// XssUtils.java
// 覆盖 30+ 危险模式：
// - script标签
// - javascript/vbscript协议
// - 事件处理器 (onclick, onerror等)
// - HTML实体编码
// - eval/expression函数
// - 危险标签 (iframe, object, embed等)
```

### 5. SQL 注入防护

```java
// PageUtils.java
public static String escapeLike(String keyword) {
    return keyword
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
}

// 所有LIKE查询前调用
String escapedKeyword = PageUtils.escapeLike(keyword);
wrapper.like(Entity::getField, escapedKeyword);
```

### 6. 请求限流

```java
// RateLimitAspect.java
// - Redis计数器限流
// - 本地令牌桶降级

@RateLimit(value = 5, key = "login")  // 每分钟最多5次
public Result<LoginVO> login(LoginDTO dto) { ... }

// 本地令牌桶降级
private static class LocalTokenBucket {
    public synchronized boolean tryAcquire() { ... }
}
```

---

## 性能优化

### 1. 多级缓存

```
请求 → Caffeine本地缓存 → Redis分布式缓存 → MySQL数据库
         (毫秒级)            (毫秒级)          (秒级)
```

### 2. 缓存配置

```yaml
app:
  cache:
    max-size: 1000           # 最大缓存数量
    expire-minutes: 60       # 过期时间
```

### 3. 数据库优化

- 全文索引支持中文分词
- 组合索引优化常用查询
- HikariCP 高性能连接池

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+

### 启动步骤

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE dong_medicine DEFAULT CHARACTER SET utf8mb4;"

# 2. 导入数据
mysql -u root -p dong_medicine < sql/dong_medicine.sql

# 3. 创建全文索引（可选）
mysql -u root -p dong_medicine < sql/fulltext_index.sql

# 4. 启动 Redis
redis-server

# 5. 启动后端服务
./mvnw spring-boot:run

# 或指定环境
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 访问地址

- 服务地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui/index.html
- 健康检查: http://localhost:8080/actuator/health

---

## Docker 部署

### 构建镜像

```bash
docker build -t dong-medicine-backend .
```

### 运行容器

```bash
docker run -d \
  --name dong-medicine-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e DB_USERNAME=dongmedicine \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  dong-medicine-backend
```

### 使用 Docker Compose

```bash
# 在项目根目录执行
docker-compose up -d --build
```

---

## 常见问题

### 1. 后端启动失败 - 数据库连接错误

**错误信息**：`Communications link failure`

**解决方案**：
- 检查MySQL是否启动
- 检查数据库连接配置是否正确
- 检查数据库是否存在

```bash
# 检查MySQL状态
mysql -u root -p -e "SHOW DATABASES;"
```

### 2. Redis 连接失败

**错误信息**：`Unable to connect to Redis`

**解决方案**：
- 检查Redis是否启动
- 检查Redis配置是否正确

```bash
# 检查Redis状态
redis-cli ping
# 应该返回 PONG
```

### 3. JWT 密钥验证失败

**原因**：生产环境 JWT 密钥要求较高

**要求**：
- 至少 64 个字符
- 包含大写字母、小写字母、数字、特殊字符
- 不能包含 "secret" 等常见单词

### 4. 端口被占用

**错误信息**：`Port 8080 already in use`

**解决方案**：

```bash
# Windows - 查找占用端口的进程
netstat -ano | findstr :8080

# 结束进程
taskkill /PID <进程ID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### 5. 文件上传失败

**可能原因**：
1. 文件大小超过限制
2. 文件类型不支持
3. 磁盘空间不足

**解决方案**：
- 图片限制：10MB
- 视频限制：100MB
- 文档限制：50MB

---

**最后更新时间**：2026年4月3日
