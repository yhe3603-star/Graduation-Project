# 服务实现类目录 (service/impl)

本目录存放服务接口的实现类，包含具体的业务逻辑代码。

## 目录

- [什么是服务实现类？](#什么是服务实现类)
- [目录结构](#目录结构)
- [实现类列表](#实现类列表)
- [开发规范](#开发规范)

---

## 什么是服务实现类？

### 服务实现类的概念

**服务实现类**是服务接口的具体实现，包含真正的业务逻辑代码。它就像"厨房"——服务员（Controller）把点单（请求）送来，厨房（ServiceImpl）根据菜谱（业务规则）准备菜品（数据）。

### 接口与实现的关系

```
┌─────────────────────────────────────────────────────────────────┐
│                     接口与实现的关系                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  UserService.java（接口）                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  // 只定义方法签名，不包含实现                            │   │
│  │  User login(LoginDTO dto);                              │   │
│  │  void register(RegisterDTO dto);                        │   │
│  │  void changePassword(Integer userId, String password);  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            ↑                                    │
│                            │ 实现                               │
│                            │                                    │
│  UserServiceImpl.java（实现类）                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  // 包含具体的业务逻辑                                    │   │
│  │  @Override                                               │   │
│  │  public User login(LoginDTO dto) {                       │   │
│  │    // 1. 验证验证码                                       │   │
│  │    // 2. 查询用户                                         │   │
│  │    // 3. 验证密码                                         │   │
│  │    // 4. 生成Token                                        │   │
│  │    return user;                                          │   │
│  │  }                                                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 目录结构

```
service/impl/
│
├── AiChatServiceImpl.java             # AI聊天服务实现
├── CommentServiceImpl.java            # 评论服务实现
├── FavoriteServiceImpl.java           # 收藏服务实现
├── FeedbackServiceImpl.java           # 反馈服务实现
├── FileUploadServiceImpl.java         # 文件上传服务实现
├── InheritorServiceImpl.java          # 传承人服务实现
├── KnowledgeServiceImpl.java          # 知识库服务实现
├── OperationLogServiceImpl.java       # 操作日志服务实现
├── PlantGameServiceImpl.java          # 植物游戏服务实现
├── PlantServiceImpl.java              # 植物服务实现
├── QaServiceImpl.java                 # 问答服务实现
├── QuizServiceImpl.java               # 测验服务实现
├── ResourceServiceImpl.java           # 资源服务实现
├── TokenBlacklistServiceImpl.java     # Token黑名单服务实现
└── UserServiceImpl.java               # 用户服务实现
```

---

## 实现类列表

| 实现类 | 对应接口 | 功能描述 |
|--------|----------|----------|
| UserServiceImpl | UserService | 用户注册、登录、信息管理 |
| PlantServiceImpl | PlantService | 药用植物增删改查、搜索 |
| KnowledgeServiceImpl | KnowledgeService | 知识库管理 |
| InheritorServiceImpl | InheritorService | 传承人管理 |
| ResourceServiceImpl | ResourceService | 学习资源管理 |
| QaServiceImpl | QaService | 问答管理 |
| CommentServiceImpl | CommentService | 评论管理 |
| FavoriteServiceImpl | FavoriteService | 收藏管理 |
| FeedbackServiceImpl | FeedbackService | 反馈管理 |
| QuizServiceImpl | QuizService | 测验功能 |
| PlantGameServiceImpl | PlantGameService | 植物游戏 |
| AiChatServiceImpl | AiChatService | AI智能问答 |
| FileUploadServiceImpl | FileUploadService | 文件上传 |
| TokenBlacklistServiceImpl | TokenBlacklistService | Token黑名单 |
| OperationLogServiceImpl | OperationLogService | 操作日志 |

---

## 开发规范

### 1. 实现类基本结构

```java
/**
 * 用户服务实现类
 */
@Service                           // 标记为服务类
@RequiredArgsConstructor            // Lombok注解，生成构造函数
@Slf4j                             // Lombok注解，生成日志对象
public class UserServiceImpl implements UserService {
    
    // 依赖注入（使用final + @RequiredArgsConstructor）
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 业务逻辑...
    }
    
    @Override
    @Transactional  // 需要事务的方法
    public void register(RegisterDTO dto) {
        // 业务逻辑...
    }
}
```

### 2. 方法实现规范

```java
@Override
public LoginVO login(LoginDTO dto) {
    // 1. 参数验证（如果DTO注解验证不够）
    if (dto.getUsername() == null) {
        throw new BusinessException("用户名不能为空");
    }
    
    // 2. 业务逻辑处理
    User user = userMapper.findByUsername(dto.getUsername());
    if (user == null) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    
    // 3. 数据操作
    if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
        throw new BusinessException(ErrorCode.PASSWORD_ERROR);
    }
    
    // 4. 返回结果
    String token = jwtUtil.generateToken(user);
    return LoginVO.builder()
        .token(token)
        .userId(user.getId())
        .username(user.getUsername())
        .build();
}
```

### 3. 事务管理

```java
@Service
@RequiredArgsConstructor
public class PlantServiceImpl implements PlantService {
    
    private final PlantMapper plantMapper;
    private final FavoriteMapper favoriteMapper;
    
    // 需要事务的方法（涉及多表操作）
    @Transactional
    public void deletePlant(Integer id) {
        // 1. 删除收藏记录
        favoriteMapper.deleteByTargetTypeAndId("plant", id);
        
        // 2. 删除植物
        plantMapper.deleteById(id);
        
        // 如果任何一步失败，都会回滚
    }
    
    // 只读操作，不需要事务
    public Plant getById(Integer id) {
        return plantMapper.selectById(id);
    }
}
```

### 4. 日志记录

```java
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Override
    public void register(RegisterDTO dto) {
        log.info("用户注册开始: {}", dto.getUsername());
        
        try {
            // 注册逻辑...
            log.info("用户注册成功: {}", user.getId());
        } catch (Exception e) {
            log.error("用户注册失败: {}", dto.getUsername(), e);
            throw e;
        }
    }
}
```

---

## 常用实现类详解

### UserServiceImpl - 用户服务实现

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.findByUsername(dto.getUsername());
        
        // 2. 验证用户存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        // 4. 检查状态
        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }
        
        // 5. 生成Token
        String token = jwtUtil.generateToken(user);
        
        log.info("用户登录成功: {}", user.getUsername());
        
        return LoginVO.builder()
            .token(token)
            .userId(user.getId())
            .username(user.getUsername())
            .role(user.getRole())
            .build();
    }
    
    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        // 1. 验证两次密码
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        
        // 2. 检查用户名是否存在
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        
        // 3. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleConstants.USER);
        user.setStatus(0);
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
    }
    
    @Override
    public void logout(String token) {
        blacklistService.addToBlacklist(token);
        log.info("用户退出登录");
    }
}
```

### PlantServiceImpl - 植物服务实现

```java
@Service
@RequiredArgsConstructor
public class PlantServiceImpl implements PlantService {
    
    private final PlantMapper plantMapper;
    
    @Override
    public Page<Plant> list(int page, int size, String category, String usageWay) {
        Page<Plant> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), Plant::getCategory, category);
        wrapper.eq(StringUtils.hasText(usageWay), Plant::getUsageWay, usageWay);
        wrapper.orderByDesc(Plant::getViewCount);
        
        return plantMapper.selectPage(pageObj, wrapper);
    }
    
    @Override
    public List<Plant> search(String keyword) {
        // XSS过滤
        if (XssUtils.containsXss(keyword)) {
            throw new BusinessException("搜索词包含非法字符");
        }
        
        // LIKE转义
        String escaped = PageUtils.escapeLike(keyword);
        
        return plantMapper.searchByKeyword(escaped);
    }
    
    @Override
    public void incrementViewCount(Integer id) {
        plantMapper.incrementViewCount(id);
    }
}
```

---

## 最佳实践

### 1. 单一职责

```java
// ✅ 好的做法：每个方法只做一件事
public User login(LoginDTO dto) {
    User user = validateAndFindUser(dto);
    String token = generateToken(user);
    return buildLoginResponse(user, token);
}

// ❌ 不好的做法：一个方法做太多事
public User login(LoginDTO dto) {
    // 验证、查询、生成Token、发送邮件、记录日志...
    // 太多职责
}
```

### 2. 异常处理

```java
// ✅ 好的做法：使用业务异常
if (user == null) {
    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
}

// ❌ 不好的做法：使用运行时异常
if (user == null) {
    throw new RuntimeException("用户不存在");
}
```

### 3. 事务边界

```java
// ✅ 好的做法：只在需要的方法上加事务
@Transactional
public void deletePlant(Integer id) {
    // 多表操作
}

// 不需要事务
public Plant getById(Integer id) {
    return plantMapper.selectById(id);
}
```

---

**最后更新时间**：2026年4月3日
