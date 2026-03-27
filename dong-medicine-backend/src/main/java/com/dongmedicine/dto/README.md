# 数据传输对象目录说明

## 文件夹结构

本目录包含项目的所有数据传输对象（DTO），用于前后端数据交互。

```
dto/
├── LoginDTO.java             # 登录请求DTO
├── RegisterDTO.java          # 注册请求DTO
├── UserUpdateDTO.java        # 用户更新DTO
├── PlantDTO.java             # 药材DTO
├── KnowledgeDTO.java         # 知识DTO
├── InheritorDTO.java         # 传承人DTO
├── FeedbackDTO.java          # 反馈DTO
├── FeedbackReplyDTO.java     # 反馈回复DTO
├── CommentDTO.java           # 评论DTO
├── CommentAddDTO.java        # 添加评论DTO
├── QuizQuestionDTO.java      # 答题题目DTO
├── QuizSubmitDTO.java        # 答题提交DTO
├── PlantGameSubmitDTO.java   # 植物游戏提交DTO
├── AnswerDTO.java            # 答案DTO
├── ChatRequest.java          # AI对话请求DTO
├── FileUploadResult.java     # 文件上传结果DTO
└── README.md                 # 说明文档
```

## 详细说明

### 1. LoginDTO.java - 登录请求DTO

**用途**：用户登录时的请求数据。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| username | String | @NotBlank, @Size(3-20) | 用户名 |
| password | String | @NotBlank, @Size(min=6) | 密码 |

**使用场景**：`POST /api/user/login`

### 2. RegisterDTO.java - 注册请求DTO

**用途**：用户注册时的请求数据。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| username | String | @NotBlank, @Size(3-20), @Pattern | 用户名 |
| password | String | @NotBlank, @Size(6-50) | 密码 |
| confirmPassword | String | @NotBlank | 确认密码 |

**用户名规则**：只能包含字母、数字和下划线

**使用场景**：`POST /api/user/register`

### 3. UserUpdateDTO.java - 用户更新DTO

**用途**：用户更新个人信息时的请求数据。

**使用场景**：`PUT /api/user/info`

### 4. PlantDTO.java - 药材DTO

**用途**：药材数据的传输对象。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| id | Integer | - | ID |
| nameCn | String | @NotBlank, @Size(max=100) | 中文名 |
| nameDong | String | @Size(max=100) | 侗语名 |
| category | String | @Size(max=50) | 分类 |
| usageWay | String | @Size(max=50) | 用法 |
| efficacy | String | @Size(max=2000) | 功效 |
| story | String | @Size(max=5000) | 故事 |
| images | String | - | 图片 |
| videos | String | - | 视频 |
| documents | String | - | 文档 |
| viewCount | Integer | - | 浏览次数 |
| favoriteCount | Integer | - | 收藏次数 |
| createdAt | LocalDateTime | - | 创建时间 |

**静态方法**：
- `fromEntity(Plant plant)`：从实体转换为DTO

### 5. KnowledgeDTO.java - 知识DTO

**用途**：知识数据的传输对象。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| id | Integer | - | ID |
| title | String | @NotBlank, @Size(max=200) | 标题 |
| type | String | @Size(max=50) | 类型 |
| therapyCategory | String | @Size(max=50) | 疗法分类 |
| diseaseCategory | String | @Size(max=50) | 疾病分类 |
| content | String | @Size(max=10000) | 内容 |
| images | String | - | 图片 |
| videos | String | - | 视频 |
| viewCount | Integer | - | 浏览次数 |
| favoriteCount | Integer | - | 收藏次数 |
| createdAt | LocalDateTime | - | 创建时间 |

**静态方法**：
- `fromEntity(Knowledge knowledge)`：从实体转换为DTO

### 6. InheritorDTO.java - 传承人DTO

**用途**：传承人数据的传输对象。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| id | Integer | - | ID |
| name | String | @NotBlank, @Size(max=50) | 姓名 |
| level | String | @NotBlank, @Pattern | 级别 |
| bio | String | @Size(max=5000) | 个人简介 |
| images | String | - | 图片 |
| videos | String | - | 视频 |
| viewCount | Integer | - | 浏览次数 |
| favoriteCount | Integer | - | 收藏次数 |

**级别规则**：国家级/省级/市级/县级/自治区级

**静态方法**：
- `fromEntity(Inheritor inheritor)`：从实体转换为DTO

### 7. FeedbackDTO.java - 反馈DTO

**用途**：用户提交反馈时的请求数据。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| type | String | @NotBlank, @Size(max=20) | 反馈类型 |
| title | String | @NotBlank, @Size(max=200) | 标题 |
| content | String | @NotBlank, @Size(max=2000) | 内容 |
| contact | String | @Size(max=100) | 联系方式 |

**使用场景**：`POST /api/feedback`

### 8. FeedbackReplyDTO.java - 反馈回复DTO

**用途**：管理员回复反馈时的请求数据。

**使用场景**：`PUT /api/admin/feedback/{id}/reply`

### 9. CommentDTO.java - 评论DTO

**用途**：评论数据的传输对象。

**字段**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | ID |
| userId | Integer | 用户ID |
| username | String | 用户名 |
| targetType | String | 目标类型 |
| targetId | Integer | 目标ID |
| content | String | 评论内容 |
| replyToId | Integer | 回复的评论ID |
| replyToUserId | Integer | 回复的用户ID |
| replyToUsername | String | 回复的用户名 |
| likes | Integer | 点赞数 |
| replyCount | Integer | 回复数 |
| status | String | 状态 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

### 10. CommentAddDTO.java - 添加评论DTO

**用途**：用户添加评论时的请求数据。

**使用场景**：`POST /api/comments`

### 11. QuizQuestionDTO.java - 答题题目DTO

**用途**：答题题目数据的传输对象。

**字段**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | ID |
| q | String | 题目 |
| options | List<String> | 选项列表 |

**使用场景**：`GET /api/quiz/questions`

### 12. QuizSubmitDTO.java - 答题提交DTO

**用途**：用户提交答题答案时的请求数据。

**使用场景**：`POST /api/quiz/submit`

### 13. PlantGameSubmitDTO.java - 植物游戏提交DTO

**用途**：用户提交植物游戏答案时的请求数据。

**使用场景**：`POST /api/plant-game/submit`

### 14. AnswerDTO.java - 答案DTO

**用途**：答案数据的传输对象。

### 15. ChatRequest.java - AI对话请求DTO

**用途**：用户发送AI对话时的请求数据。

**字段**：
| 字段名 | 类型 | 验证规则 | 说明 |
|-------|------|---------|------|
| message | String | @NotBlank | 消息内容 |
| history | List<Message> | - | 历史消息 |

**内部类 Message**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| role | String | 角色 |
| content | String | 内容 |

**使用场景**：`POST /api/chat`

### 16. FileUploadResult.java - 文件上传结果DTO

**用途**：文件上传后的返回结果。

**字段**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| success | boolean | 是否成功 |
| fileName | String | 文件名 |
| originalFileName | String | 原始文件名 |
| filePath | String | 文件路径 |
| fileUrl | String | 文件URL |
| fileType | String | 文件类型 |
| fileSize | Long | 文件大小 |
| message | String | 提示信息 |

**静态方法**：
- `success(...)`：创建成功结果
- `fail(String message)`：创建失败结果

**使用场景**：`POST /api/upload/*`

## DTO统计

| DTO类 | 主要用途 | 验证注解 |
|-------|---------|---------|
| LoginDTO | 用户登录 | @NotBlank, @Size |
| RegisterDTO | 用户注册 | @NotBlank, @Size, @Pattern |
| UserUpdateDTO | 用户更新 | - |
| PlantDTO | 药材数据 | @NotBlank, @Size |
| KnowledgeDTO | 知识数据 | @NotBlank, @Size |
| InheritorDTO | 传承人数据 | @NotBlank, @Size, @Pattern |
| FeedbackDTO | 意见反馈 | @NotBlank, @Size |
| FeedbackReplyDTO | 反馈回复 | - |
| CommentDTO | 评论数据 | - |
| CommentAddDTO | 添加评论 | - |
| QuizQuestionDTO | 答题题目 | - |
| QuizSubmitDTO | 答题提交 | - |
| PlantGameSubmitDTO | 游戏提交 | - |
| AnswerDTO | 答案数据 | - |
| ChatRequest | AI对话 | @NotBlank |
| FileUploadResult | 上传结果 | - |
| **总计** | **16个** | - |

## 开发规范

1. **命名规范**：
   - 类名使用大驼峰命名法，以DTO结尾
   - 字段名使用小驼峰命名法

2. **验证注解**：
   - `@NotBlank`：字符串不能为空
   - `@Size`：限制字符串长度
   - `@Pattern`：正则表达式验证
   - `@Min/@Max`：数值范围验证

3. **转换方法**：
   - 提供`fromEntity`静态方法用于实体转DTO
   - DTO只包含需要传输的字段，不包含敏感信息

4. **Lombok注解**：
   - `@Data`：自动生成getter/setter/toString等方法
   - `@Builder`：支持建造者模式
   - `@NoArgsConstructor`：无参构造方法
   - `@AllArgsConstructor`：全参构造方法

## 注意事项

- DTO与Entity的区别：
  - Entity与数据库表对应，包含所有字段
  - DTO用于数据传输，只包含必要字段
  - DTO可以包含验证注解
  - DTO可以隐藏敏感字段（如密码）

- 前后端数据交互流程：
  1. 前端发送DTO → Controller
  2. Controller验证DTO
  3. Service将DTO转换为Entity
  4. Service处理业务逻辑
  5. Service返回Entity或DTO
  6. Controller返回DTO给前端

---

**最后更新时间**：2026年3月27日
