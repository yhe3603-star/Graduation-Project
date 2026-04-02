# 服务层 (service)

本目录存放所有服务接口和实现类，负责处理业务逻辑。

## 目录

- [什么是服务层？](#什么是服务层)
- [目录结构](#目录结构)
- [服务接口与实现](#服务接口与实现)
- [服务列表](#服务列表)
- [服务开发规范](#服务开发规范)
- [常用服务详解](#常用服务详解)

---

## 什么是服务层？

### 服务层的概念

**服务层（Service）** 是后端应用中处理业务逻辑的层。它就像餐厅的"厨房"——服务员（Controller）把点单（请求）送来，厨房（Service）根据菜谱（业务规则）准备菜品（数据），然后服务员再把菜品端给顾客。

### 服务层的作用

```
┌─────────────────────────────────────────────────────────────────┐
│                        三层架构                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Controller 层                         │   │
│  │  职责：接收请求，调用Service，返回响应                     │   │
│  │  代码量：少，主要是转发                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     Service 层                          │   │
│  │  职责：处理业务逻辑                                       │   │
│  │  代码量：多，包含各种业务规则                              │   │
│  │  ┌─────────────────────────────────────────────────┐    │   │
│  │  │  - 数据验证                                       │    │   │
│  │  │  - 业务规则判断                                    │    │   │
│  │  │  - 调用Mapper操作数据库                            │    │   │
│  │  │  - 调用其他Service                                │    │   │
│  │  │  - 事务管理                                       │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     Mapper 层                           │   │
│  │  职责：与数据库交互                                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 为什么需要服务层？

1. **业务逻辑集中**：所有业务规则都在Service层，便于维护
2. **代码复用**：多个Controller可以调用同一个Service
3. **事务管理**：Service层可以方便地管理事务
4. **测试方便**：可以单独测试Service层的业务逻辑

---

## 目录结构

```
service/
│
├── impl/                              # 服务实现类
│   ├── UserServiceImpl.java           # 用户服务实现
│   ├── PlantServiceImpl.java          # 植物服务实现
│   ├── KnowledgeServiceImpl.java      # 知识服务实现
│   ├── InheritorServiceImpl.java      # 传承人服务实现
│   ├── ResourceServiceImpl.java       # 资源服务实现
│   ├── QaServiceImpl.java             # 问答服务实现
│   ├── CommentServiceImpl.java        # 评论服务实现
│   ├── FavoriteServiceImpl.java       # 收藏服务实现
│   ├── FeedbackServiceImpl.java       # 反馈服务实现
│   ├── QuizServiceImpl.java           # 测验服务实现
│   ├── PlantGameServiceImpl.java      # 植物游戏服务实现
│   ├── LeaderboardServiceImpl.java    # 排行榜服务实现
│   ├── AiChatServiceImpl.java         # AI聊天服务实现
│   ├── FileUploadServiceImpl.java     # 文件上传服务实现
│   └── AdminServiceImpl.java          # 管理后台服务实现
│
├── UserService.java                   # 用户服务接口
├── PlantService.java                  # 植物服务接口
├── KnowledgeService.java              # 知识服务接口
├── InheritorService.java              # 传承人服务接口
├── ResourceService.java               # 资源服务接口
├── QaService.java                     # 问答服务接口
├── CommentService.java                # 评论服务接口
├── FavoriteService.java               # 收藏服务接口
├── FeedbackService.java               # 反馈服务接口
├── QuizService.java                   # 测验服务接口
├── PlantGameService.java              # 植物游戏服务接口
├── LeaderboardService.java            # 排行榜服务接口
├── AiChatService.java                 # AI聊天服务接口
├── FileUploadService.java             # 文件上传服务接口
└── AdminService.java                  # 管理后台服务接口
```

---

## 服务接口与实现

### 服务接口

服务接口定义了服务提供的方法，是一种规范。

```java
/**
 * 用户服务接口
 * 定义用户相关的业务方法
 */
public interface UserService {
    
    /**
     * 用户登录
     * @param dto 登录信息
     * @return 登录结果（包含Token）
     */
    LoginVO login(LoginDTO dto);
    
    /**
     * 用户注册
     * @param dto 注册信息
     */
    void register(RegisterDTO dto);
    
    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Integer id);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param dto 密码信息
     */
    void changePassword(Integer userId, ChangePasswordDTO dto);
    
    /**
     * 退出登录
     * @param token Token
     */
    void logout(String token);
}
```

### 服务实现类

服务实现类实现了接口中定义的方法，包含具体的业务逻辑。

```java
/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Service                           // 标记为服务类
@RequiredArgsConstructor            // Lombok注解，生成构造函数
@Slf4j                             // Lombok注解，生成日志对象
public class UserServiceImpl implements UserService {
    
    // 依赖注入
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 验证验证码
        validateCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        // 2. 查询用户
        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        // 4. 检查用户状态
        if (user.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }
        
        // 5. 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 6. 返回登录结果
        return LoginVO.builder()
            .token(token)
            .userId(user.getId())
            .username(user.getUsername())
            .role(user.getRole())
            .build();
    }
    
    @Override
    @Transactional  // 开启事务
    public void register(RegisterDTO dto) {
        // 1. 验证验证码
        validateCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());
        
        // 2. 检查用户名是否存在
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }
        
        // 3. 验证密码
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        
        // 4. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RoleConstants.USER);
        user.setStatus(0);
        user.setCreatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
    }
    
    @Override
    public void logout(String token) {
        // 将Token加入黑名单
        tokenBlacklistService.addToBlacklist(token);
        log.info("用户退出登录");
    }
    
    // 私有方法：验证验证码
    private void validateCaptcha(String key, String code) {
        // 验证码验证逻辑...
    }
}
```

---

## 服务列表

| 服务 | 功能描述 |
|------|----------|
| UserService | 用户注册、登录、信息管理、Token刷新 |
| PlantService | 药用植物增删改查、搜索、浏览计数 |
| KnowledgeService | 知识库增删改查、搜索、分类过滤 |
| InheritorService | 传承人增删改查、级别筛选 |
| ResourceService | 学习资源管理、下载计数 |
| QaService | 问答管理 |
| CommentService | 评论发表、审核、嵌套回复 |
| FavoriteService | 收藏管理、收藏状态检查 |
| FeedbackService | 用户反馈提交、回复 |
| QuizService | 测验题目获取、答案提交、记录保存 |
| PlantGameService | 植物游戏逻辑、分数计算 |
| LeaderboardService | 排行榜数据统计 |
| AiChatService | AI智能问答（DeepSeek集成） |
| FileUploadService | 文件上传、类型验证、大小限制 |
| AdminService | 管理后台数据统计、用户管理 |

---

## 服务开发规范

### 1. 接口定义规范

```java
public interface ExampleService {
    
    /**
     * 方法说明
     * @param paramName 参数说明
     * @return 返回值说明
     * @throws BusinessException 异常说明
     */
    ReturnType methodName(ParamType paramName);
}
```

### 2. 实现类规范

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleServiceImpl implements ExampleService {
    
    // 1. 依赖注入（使用final + @RequiredArgsConstructor）
    private final ExampleMapper exampleMapper;
    private final OtherService otherService;
    
    // 2. 公有方法（实现接口）
    @Override
    @Transactional
    public ReturnType methodName(ParamType paramName) {
        // 业务逻辑
    }
    
    // 3. 私有方法（内部使用）
    private void privateMethod() {
        // 辅助逻辑
    }
}
```

### 3. 事务管理规范

```java
@Service
public class ExampleServiceImpl implements ExampleService {
    
    // 需要事务的方法
    @Transactional
    public void addWithDetails(ExampleDTO dto) {
        // 多个数据库操作，需要事务保证一致性
        exampleMapper.insert(example);
        detailMapper.insertBatch(details);
    }
    
    // 只读操作，不需要事务
    public Example getById(Integer id) {
        return exampleMapper.selectById(id);
    }
    
    // 指定事务属性
    @Transactional(
        readOnly = false,                    // 非只读
        timeout = 30,                        // 超时时间30秒
        rollbackFor = Exception.class        // 所有异常都回滚
    )
    public void importantOperation() {
        // 重要操作
    }
}
```

### 4. 异常处理规范

```java
@Service
public class ExampleServiceImpl implements ExampleService {
    
    public Example getById(Integer id) {
        Example example = exampleMapper.selectById(id);
        
        // 找不到数据时抛出业务异常
        if (example == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        return example;
    }
    
    public void update(ExampleDTO dto) {
        // 业务规则验证失败时抛出异常
        if (!isValid(dto)) {
            throw new BusinessException("数据验证失败：原因");
        }
        
        exampleMapper.updateById(convert(dto));
    }
}
```

---

## 常用服务详解

### UserService - 用户服务

处理用户注册、登录、信息管理等核心功能。

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
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
        
        return new LoginVO(token, user);
    }
}
```

### PlantService - 植物服务

处理药用植物的增删改查和搜索。

```java
@Service
@RequiredArgsConstructor
public class PlantServiceImpl implements PlantService {
    
    private final PlantMapper plantMapper;
    
    @Override
    public Page<Plant> list(int page, int size, String category, String usageWay) {
        Page<Plant> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
        
        // 条件筛选
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

### QuizService - 测验服务

处理测验题目获取和答案提交。

```java
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    
    private final QuizQuestionMapper questionMapper;
    private final QuizRecordMapper recordMapper;
    
    @Override
    public List<QuizQuestion> getRandomQuestions(int count, int scorePerQuestion) {
        // 随机获取问题
        return questionMapper.selectRandomQuestions(count);
    }
    
    @Override
    @Transactional
    public QuizResultVO submit(QuizSubmitDTO dto) {
        // 1. 计算得分
        int correctCount = 0;
        for (AnswerDTO answer : dto.getAnswers()) {
            QuizQuestion question = questionMapper.selectById(answer.getQuestionId());
            if (question.getCorrectAnswer().equals(answer.getAnswer())) {
                correctCount++;
            }
        }
        
        // 2. 计算总分
        int totalScore = correctCount * dto.getScorePerQuestion();
        
        // 3. 保存记录
        QuizRecord record = new QuizRecord();
        record.setUserId(SecurityUtils.getCurrentUserId());
        record.setTotalQuestions(dto.getAnswers().size());
        record.setCorrectCount(correctCount);
        record.setTotalScore(totalScore);
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);
        
        // 4. 返回结果
        return new QuizResultVO(correctCount, dto.getAnswers().size(), totalScore);
    }
}
```

---

## 最佳实践

### 1. 服务层职责

- **处理业务逻辑**：所有业务规则都在Service层
- **事务管理**：需要事务的方法加@Transactional
- **调用Mapper**：通过Mapper操作数据库
- **调用其他Service**：复杂业务可能需要多个Service协作

### 2. 不要在Service层做的事情

- 不要处理HTTP请求参数（Controller层做）
- 不要直接操作HttpServletRequest/Response
- 不要返回R<T>（Controller层封装）

### 3. 性能优化

```java
// 使用缓存
@Cacheable(value = "plants", key = "#id")
public Plant getById(Integer id) {
    return plantMapper.selectById(id);
}

// 批量操作
public void addBatch(List<Plant> plants) {
    plantMapper.insertBatch(plants);
}
```

---

**最后更新时间**：2026年4月3日
