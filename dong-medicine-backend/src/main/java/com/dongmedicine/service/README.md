# 服务层目录说明

## 文件夹结构

本目录包含项目的服务层代码，分为接口和实现类两部分。

```
service/
├── impl/           # 服务实现类
│   ├── AiChatServiceImpl.java       # AI对话服务实现
│   ├── CommentServiceImpl.java      # 评论服务实现
│   ├── FavoriteServiceImpl.java     # 收藏服务实现
│   ├── FeedbackServiceImpl.java     # 意见反馈服务实现
│   ├── FileUploadServiceImpl.java   # 文件上传服务实现
│   ├── InheritorServiceImpl.java    # 传承人服务实现
│   ├── KnowledgeServiceImpl.java    # 知识库服务实现
│   ├── OperationLogServiceImpl.java # 操作日志服务实现
│   ├── PlantGameServiceImpl.java    # 植物游戏服务实现
│   ├── PlantServiceImpl.java        # 药材服务实现
│   ├── QaServiceImpl.java           # 问答服务实现
│   ├── QuizServiceImpl.java         # 答题服务实现
│   ├── ResourceServiceImpl.java     # 资源服务实现
│   ├── TokenBlacklistServiceImpl.java # 令牌黑名单服务实现
│   └── UserServiceImpl.java         # 用户服务实现
├── AiChatService.java       # AI对话服务接口
├── CommentService.java      # 评论服务接口
├── FavoriteService.java     # 收藏服务接口
├── FeedbackService.java     # 意见反馈服务接口
├── FileUploadService.java   # 文件上传服务接口
├── InheritorService.java    # 传承人服务接口
├── KnowledgeService.java    # 知识库服务接口
├── OperationLogService.java # 操作日志服务接口
├── PlantGameService.java    # 植物游戏服务接口
├── PlantService.java        # 药材服务接口
├── QaService.java           # 问答服务接口
├── QuizService.java         # 答题服务接口
├── ResourceService.java     # 资源服务接口
├── TokenBlacklistService.java # 令牌黑名单服务接口
└── UserService.java         # 用户服务接口
```

## 详细说明

### 1. 服务接口

#### 1.1 AiChatService.java

**功能**：AI对话服务接口。

**主要方法**：
- `chat`：与AI进行对话
- `getChatHistory`：获取聊天历史

#### 1.2 CommentService.java

**功能**：评论服务接口。

**主要方法**：
- `addComment`：添加评论
- `updateComment`：更新评论
- `deleteComment`：删除评论
- `getComments`：获取评论列表
- `getCommentsByTarget`：根据目标获取评论

#### 1.3 FavoriteService.java

**功能**：收藏服务接口。

**主要方法**：
- `addFavorite`：添加收藏
- `removeFavorite`：取消收藏
- `getFavorites`：获取收藏列表
- `isFavorited`：检查是否已收藏

#### 1.4 FeedbackService.java

**功能**：意见反馈服务接口。

**主要方法**：
- `addFeedback`：添加反馈
- `updateFeedback`：更新反馈
- `deleteFeedback`：删除反馈
- `getFeedbacks`：获取反馈列表
- `getFeedbackById`：根据ID获取反馈

#### 1.5 FileUploadService.java

**功能**：文件上传服务接口。

**主要方法**：
- `uploadImage`：上传图片
- `uploadVideo`：上传视频
- `uploadDocument`：上传文档
- `uploadFile`：上传通用文件
- `deleteFile`：删除文件

#### 1.6 InheritorService.java

**功能**：传承人服务接口。

**主要方法**：
- `addInheritor`：添加传承人
- `updateInheritor`：更新传承人
- `deleteInheritor`：删除传承人
- `getInheritors`：获取传承人列表
- `getInheritorById`：根据ID获取传承人
- `getInheritorsByLevel`：根据级别获取传承人

#### 1.7 KnowledgeService.java

**功能**：知识库服务接口。

**主要方法**：
- `addKnowledge`：添加知识
- `updateKnowledge`：更新知识
- `deleteKnowledge`：删除知识
- `getKnowledge`：获取知识列表
- `getKnowledgeById`：根据ID获取知识
- `getKnowledgeByType`：根据类型获取知识

#### 1.8 OperationLogService.java

**功能**：操作日志服务接口。

**主要方法**：
- `addLog`：添加操作日志
- `getLogs`：获取操作日志列表
- `getLogById`：根据ID获取操作日志
- `deleteLog`：删除操作日志

#### 1.9 PlantGameService.java

**功能**：植物游戏服务接口。

**主要方法**：
- `getGameQuestions`：获取游戏题目
- `submitGameAnswer`：提交游戏答案
- `getGameResult`：获取游戏结果
- `getGameHistory`：获取游戏历史
- `getLeaderboard`：获取排行榜

#### 1.10 PlantService.java

**功能**：药材服务接口。

**主要方法**：
- `addPlant`：添加药材
- `updatePlant`：更新药材
- `deletePlant`：删除药材
- `getPlants`：获取药材列表
- `getPlantById`：根据ID获取药材
- `getPlantsByCategory`：根据分类获取药材

#### 1.11 QaService.java

**功能**：问答服务接口。

**主要方法**：
- `addQa`：添加问答
- `updateQa`：更新问答
- `deleteQa`：删除问答
- `getQaList`：获取问答列表
- `getQaById`：根据ID获取问答
- `addAnswer`：添加回答

#### 1.12 QuizService.java

**功能**：答题服务接口。

**主要方法**：
- `getQuizQuestions`：获取答题题目
- `submitQuizAnswer`：提交答题答案
- `getQuizResult`：获取答题结果
- `getQuizHistory`：获取答题历史
- `getLeaderboard`：获取排行榜

#### 1.13 ResourceService.java

**功能**：资源服务接口。

**主要方法**：
- `addResource`：添加资源
- `updateResource`：更新资源
- `deleteResource`：删除资源
- `getResources`：获取资源列表
- `getResourceById`：根据ID获取资源
- `downloadResource`：下载资源

#### 1.14 TokenBlacklistService.java

**功能**：令牌黑名单服务接口。

**主要方法**：
- `addToken`：添加令牌到黑名单
- `containsToken`：检查令牌是否在黑名单中
- `removeToken`：从黑名单中移除令牌

#### 1.15 UserService.java

**功能**：用户服务接口。

**主要方法**：
- `login`：用户登录
- `register`：用户注册
- `logout`：用户登出
- `getUserById`：根据ID获取用户
- `updateUser`：更新用户信息
- `changePassword`：修改密码
- `getUserList`：获取用户列表

### 2. 服务实现类

服务实现类是服务接口的具体实现，包含了业务逻辑的核心代码。每个实现类都对应一个服务接口，实现了接口中定义的所有方法。

**主要特点**：
- 包含具体的业务逻辑
- 调用Mapper接口进行数据库操作
- 处理业务异常
- 实现事务管理
- 调用其他服务进行协作

## 服务层使用指南

### 1. 注入服务

```java
@Autowired
private PlantService plantService;
```

### 2. 调用服务方法

```java
// 添加药材
Plant plant = new Plant();
plant.setName("人参");
plant.setCategory("根茎类");
plantService.addPlant(plant);

// 获取药材列表
List<Plant> plants = plantService.getPlants();

// 根据ID获取药材
Plant plant = plantService.getPlantById(1L);

// 更新药材
plant.setName("西洋参");
plantService.updatePlant(plant);

// 删除药材
plantService.deletePlant(1L);
```

### 3. 事务管理

```java
@Service
@Transactional
public class PlantServiceImpl implements PlantService {
    // 方法实现...
}
```

### 4. 异常处理

```java
@Override
public Plant getPlantById(Long id) {
    Plant plant = plantMapper.selectById(id);
    if (plant == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND, "药材不存在");
    }
    return plant;
}
```

## 开发规范

1. **命名规范**：服务接口名使用大驼峰命名法，以`Service`结尾；实现类名使用大驼峰命名法，以`ServiceImpl`结尾
2. **方法命名**：方法名使用小驼峰命名法，动词+名词形式
3. **参数规范**：参数应该有明确的含义，使用适当的类型
4. **返回值规范**：返回值应该明确，避免使用`Object`类型
5. **事务管理**：使用`@Transactional`注解管理事务
6. **异常处理**：使用`BusinessException`处理业务异常
7. **日志记录**：使用`@OperationLog`注解记录操作日志
8. **注释规范**：为每个方法添加注释说明

## 注意事项

- 服务层应该只包含业务逻辑，不包含数据访问逻辑
- 服务方法应该简洁明了，避免复杂的业务逻辑
- 合理使用事务管理，确保数据一致性
- 注意异常处理，提供清晰的错误信息
- 定期检查和优化服务层代码

---

**最后更新时间**：2026年3月23日