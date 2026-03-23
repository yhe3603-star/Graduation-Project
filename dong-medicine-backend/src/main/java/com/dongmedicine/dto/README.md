# 数据传输对象目录说明

## 文件夹结构

本目录包含项目中使用的数据传输对象（DTO），用于在不同层之间传递数据。

```
dto/
├── AnswerDTO.java           # 回答DTO
├── ChangePasswordDTO.java   # 修改密码DTO
├── ChatRequest.java         # 聊天请求DTO
├── ChatResponse.java        # 聊天响应DTO
├── CommentAddDTO.java       # 添加评论DTO
├── CommentDTO.java          # 评论DTO
├── FeedbackDTO.java         # 意见反馈DTO
├── FeedbackReplyDTO.java    # 意见反馈回复DTO
├── FileUploadResult.java    # 文件上传结果DTO
├── InheritorDTO.java        # 传承人DTO
├── KnowledgeDTO.java        # 知识DTO
├── LoginDTO.java            # 登录DTO
├── PlantDTO.java            # 药材DTO
├── PlantGameSubmitDTO.java  # 植物游戏提交DTO
├── QuizQuestionDTO.java     # 答题题目DTO
├── QuizSubmitDTO.java       # 答题提交DTO
├── RegisterDTO.java         # 注册DTO
└── UserUpdateDTO.java       # 用户更新DTO
```

## 详细说明

### 1. AnswerDTO.java

**功能**：回答数据传输对象，用于传递回答相关的数据。

**主要字段**：
- `id`：回答ID
- `questionId`：问题ID
- `content`：回答内容
- `userId`：用户ID
- `userName`：用户名
- `createdAt`：创建时间
- `likes`：点赞数

### 2. ChangePasswordDTO.java

**功能**：修改密码数据传输对象，用于传递修改密码相关的数据。

**主要字段**：
- `oldPassword`：旧密码
- `newPassword`：新密码
- `confirmPassword`：确认新密码

### 3. ChatRequest.java

**功能**：聊天请求数据传输对象，用于传递聊天相关的数据。

**主要字段**：
- `message`：聊天消息
- `history`：聊天历史
- `userId`：用户ID

### 4. ChatResponse.java

**功能**：聊天响应数据传输对象，用于传递聊天响应相关的数据。

**主要字段**：
- `message`：聊天消息
- `status`：状态
- `responseTime`：响应时间

### 5. CommentAddDTO.java

**功能**：添加评论数据传输对象，用于传递添加评论相关的数据。

**主要字段**：
- `targetType`：目标类型
- `targetId`：目标ID
- `content`：评论内容
- `replyToId`：回复ID

### 6. CommentDTO.java

**功能**：评论数据传输对象，用于传递评论相关的数据。

**主要字段**：
- `id`：评论ID
- `targetType`：目标类型
- `targetId`：目标ID
- `content`：评论内容
- `userId`：用户ID
- `userName`：用户名
- `replyToId`：回复ID
- `replyToName`：回复用户名
- `status`：状态
- `createdAt`：创建时间

### 7. FeedbackDTO.java

**功能**：意见反馈数据传输对象，用于传递意见反馈相关的数据。

**主要字段**：
- `id`：反馈ID
- `userId`：用户ID
- `userName`：用户名
- `type`：反馈类型
- `content`：反馈内容
- `contact`：联系方式
- `status`：状态
- `createdAt`：创建时间
- `processedBy`：处理人
- `processedAt`：处理时间

### 8. FeedbackReplyDTO.java

**功能**：意见反馈回复数据传输对象，用于传递意见反馈回复相关的数据。

**主要字段**：
- `feedbackId`：反馈ID
- `replyContent`：回复内容
- `status`：状态

### 9. FileUploadResult.java

**功能**：文件上传结果数据传输对象，用于传递文件上传结果相关的数据。

**主要字段**：
- `fileId`：文件ID
- `fileName`：文件名
- `filePath`：文件路径
- `fileUrl`：文件URL
- `fileSize`：文件大小
- `fileType`：文件类型
- `uploadTime`：上传时间

### 10. InheritorDTO.java

**功能**：传承人数据传输对象，用于传递传承人相关的数据。

**主要字段**：
- `id`：传承人ID
- `name`：姓名
- `level`：级别
- `specialties`：技艺特色
- `experienceYears`：从业年限
- `bio`：个人简介
- `representativeCases`：代表案例
- `honors`：荣誉资质
- `createdAt`：创建时间

### 11. KnowledgeDTO.java

**功能**：知识数据传输对象，用于传递知识相关的数据。

**主要字段**：
- `id`：知识ID
- `title`：标题
- `content`：内容
- `type`：类型
- `therapyCategory`：疗法分类
- `diseaseCategory`：疾病分类
- `viewCount`：浏览量
- `createdAt`：创建时间

### 12. LoginDTO.java

**功能**：登录数据传输对象，用于传递登录相关的数据。

**主要字段**：
- `token`：认证令牌
- `user`：用户信息
- `expireTime`：过期时间

### 13. PlantDTO.java

**功能**：药材数据传输对象，用于传递药材相关的数据。

**主要字段**：
- `id`：药材ID
- `name`：名称
- `latinName`：拉丁名
- `category`：分类
- `usageWay`：用法
- `effect`：功效
- `distribution`：分布
- `description`：描述
- `imageUrl`：图片URL
- `viewCount`：浏览量
- `favoriteCount`：收藏量
- `createdAt`：创建时间

### 14. PlantGameSubmitDTO.java

**功能**：植物游戏提交数据传输对象，用于传递植物游戏提交相关的数据。

**主要字段**：
- `questionId`：题目ID
- `answer`：答案
- `difficulty`：难度

### 15. QuizQuestionDTO.java

**功能**：答题题目数据传输对象，用于传递答题题目相关的数据。

**主要字段**：
- `id`：题目ID
- `content`：题目内容
- `options`：选项
- `correctAnswer`：正确答案
- `category`：分类
- `difficulty`：难度

### 16. QuizSubmitDTO.java

**功能**：答题提交数据传输对象，用于传递答题提交相关的数据。

**主要字段**：
- `questionId`：题目ID
- `answer`：答案

### 17. RegisterDTO.java

**功能**：注册数据传输对象，用于传递注册相关的数据。

**主要字段**：
- `username`：用户名
- `password`：密码
- `confirmPassword`：确认密码
- `email`：邮箱
- `phone`：手机号

### 18. UserUpdateDTO.java

**功能**：用户更新数据传输对象，用于传递用户更新相关的数据。

**主要字段**：
- `username`：用户名
- `email`：邮箱
- `phone`：手机号
- `nickname`：昵称
- `avatar`：头像

## DTO使用指南

### 1. 请求DTO

**用于接收前端传递的数据**：

```java
@PostMapping("/login")
public R<LoginDTO> login(@RequestBody @Valid LoginRequest request) {
    // 处理登录逻辑
}
```

### 2. 响应DTO

**用于返回给前端的数据**：

```java
@GetMapping("/plants")
public R<List<PlantDTO>> getPlants() {
    List<Plant> plants = plantService.list();
    List<PlantDTO> plantDTOs = plants.stream()
        .map(plant -> new PlantDTO(plant))
        .collect(Collectors.toList());
    return R.ok(plantDTOs);
}
```

### 3. 数据转换

**实体类转DTO**：

```java
// 在DTO类中添加构造方法
public PlantDTO(Plant plant) {
    this.id = plant.getId();
    this.name = plant.getName();
    this.latinName = plant.getLatinName();
    // 其他字段...
}

// 或者使用BeanUtils
PlantDTO plantDTO = new PlantDTO();
BeanUtils.copyProperties(plant, plantDTO);
```

**DTO转实体类**：

```java
// 在实体类中添加构造方法
public Plant(PlantDTO plantDTO) {
    this.id = plantDTO.getId();
    this.name = plantDTO.getName();
    this.latinName = plantDTO.getLatinName();
    // 其他字段...
}

// 或者使用BeanUtils
Plant plant = new Plant();
BeanUtils.copyProperties(plantDTO, plant);
```

## 开发规范

1. **命名规范**：DTO类名使用大驼峰命名法，以`DTO`结尾
2. **字段命名**：字段名使用小驼峰命名法
3. **数据验证**：使用`@NotNull`、`@Size`等注解进行数据验证
4. **序列化**：使用`@JsonProperty`注解指定JSON字段名
5. **文档说明**：为每个DTO类和字段添加注释说明

## 注意事项

- DTO应该只包含需要在不同层之间传递的数据
- 避免在DTO中包含业务逻辑
- 合理使用DTO可以减少数据传输量，提高性能
- 对于复杂的DTO，可以考虑使用MapStruct等工具进行数据转换

---

**最后更新时间**：2026年3月23日