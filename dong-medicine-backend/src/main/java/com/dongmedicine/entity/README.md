# 实体类目录说明

## 文件夹结构

本目录包含项目的所有实体类，与数据库表一一对应。

```
entity/
├── User.java              # 用户实体
├── Plant.java             # 药材实体
├── Knowledge.java         # 知识实体
├── Inheritor.java         # 传承人实体
├── Resource.java          # 资源实体
├── Qa.java                # 问答实体
├── QuizQuestion.java      # 答题题目实体
├── QuizRecord.java        # 答题记录实体
├── PlantGameRecord.java   # 植物游戏记录实体
├── Comment.java           # 评论实体
├── Favorite.java          # 收藏实体
├── Feedback.java          # 反馈实体
├── OperationLog.java      # 操作日志实体
└── README.md              # 说明文档
```

## 详细说明

### 1. User.java - 用户实体

**对应表**：`users`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| username | String | 用户名 |
| passwordHash | String | 密码哈希 |
| role | String | 角色（admin/user） |
| status | String | 状态（active/banned） |
| createdAt | LocalDateTime | 创建时间 |

**常量定义**：
- `STATUS_ACTIVE`：激活状态
- `STATUS_BANNED`：封禁状态

**方法**：
- `isBanned()`：判断用户是否被封禁

### 2. Plant.java - 药材实体

**对应表**：`plants`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| nameCn | String | 中文名称（必填） |
| nameDong | String | 侗语名称 |
| scientificName | String | 学名 |
| category | String | 分类 |
| usageWay | String | 用法 |
| habitat | String | 生长环境 |
| efficacy | String | 功效 |
| story | String | 相关故事 |
| images | String | 图片（JSON数组） |
| videos | String | 视频（JSON数组） |
| documents | String | 文档（JSON数组） |
| distribution | String | 分布地区 |
| difficulty | String | 难度等级 |
| updateLog | String | 更新日志 |
| viewCount | Integer | 浏览次数 |
| favoriteCount | Integer | 收藏次数 |
| popularity | Integer | 热度 |
| createdAt | LocalDateTime | 创建时间 |

### 3. Knowledge.java - 知识实体

**对应表**：`knowledge`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| title | String | 标题（必填） |
| type | String | 类型 |
| therapyCategory | String | 疗法分类 |
| diseaseCategory | String | 疾病分类 |
| herbCategory | String | 药材分类 |
| content | String | 内容（必填） |
| formula | String | 配方 |
| usageMethod | String | 使用方法 |
| steps | String | 步骤 |
| images | String | 图片（JSON数组） |
| videos | String | 视频（JSON数组） |
| documents | String | 文档（JSON数组） |
| relatedPlants | String | 相关药材 |
| updateLog | String | 更新日志 |
| viewCount | Integer | 浏览次数 |
| favoriteCount | Integer | 收藏次数 |
| popularity | Integer | 热度 |
| createdAt | LocalDateTime | 创建时间 |

### 4. Inheritor.java - 传承人实体

**对应表**：`inheritors`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| name | String | 姓名（必填） |
| level | String | 级别（国家级/省级/市级等） |
| bio | String | 个人简介 |
| specialties | String | 技艺特色 |
| experienceYears | Integer | 从业年限 |
| videos | String | 视频（JSON数组） |
| images | String | 图片（JSON数组） |
| documents | String | 文档（JSON数组） |
| representativeCases | String | 代表案例 |
| honors | String | 荣誉 |
| updateLog | String | 更新日志 |
| viewCount | Integer | 浏览次数 |
| favoriteCount | Integer | 收藏次数 |
| popularity | Integer | 热度 |
| createdAt | LocalDateTime | 创建时间 |

### 5. Resource.java - 资源实体

**对应表**：`resources`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| title | String | 标题（必填） |
| category | String | 分类 |
| files | String | 文件列表（JSON数组） |
| description | String | 描述 |
| updateLog | String | 更新日志 |
| viewCount | Integer | 浏览次数 |
| downloadCount | Integer | 下载次数 |
| favoriteCount | Integer | 收藏次数 |
| popularity | Integer | 热度 |
| createdAt | LocalDateTime | 创建时间 |

### 6. Qa.java - 问答实体

**对应表**：`qa`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| category | String | 分类 |
| question | String | 问题（必填） |
| answer | String | 答案 |
| viewCount | Integer | 浏览次数 |
| favoriteCount | Integer | 收藏次数 |
| popularity | Integer | 热度 |
| createdAt | LocalDateTime | 创建时间 |

### 7. QuizQuestion.java - 答题题目实体

**对应表**：`quiz_questions`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键 |
| question | String | 题目 |
| options | String | 选项（JSON数组，不序列化） |
| answer | String | 答案 |
| category | String | 分类 |
| difficulty | String | 难度 |
| correctAnswer | String | 正确答案 |
| explanation | String | 解析 |

**方法**：
- `getOptionList()`：获取选项列表（从JSON解析）
- `setOptionList(List<String>)`：设置选项列表（转换为JSON）

### 8. QuizRecord.java - 答题记录实体

**对应表**：`quiz_record`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| userId | Integer | 用户ID |
| score | Integer | 得分 |
| totalQuestions | Integer | 总题数 |
| correctAnswers | Integer | 正确数 |
| createTime | LocalDateTime | 创建时间 |

### 9. PlantGameRecord.java - 植物游戏记录实体

**对应表**：`plant_game_record`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| userId | Integer | 用户ID |
| difficulty | String | 难度 |
| score | Integer | 得分 |
| correctCount | Integer | 正确数 |
| totalCount | Integer | 总题数 |
| createTime | LocalDateTime | 创建时间 |

### 10. Comment.java - 评论实体

**对应表**：`comments`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
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
| status | String | 状态（pending/approved/rejected） |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 11. Favorite.java - 收藏实体

**对应表**：`favorites`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| userId | Integer | 用户ID |
| targetType | String | 目标类型 |
| targetId | Integer | 目标ID |
| createdAt | LocalDateTime | 创建时间 |

### 12. Feedback.java - 反馈实体

**对应表**：`feedback`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| userId | Integer | 用户ID |
| userName | String | 用户名 |
| type | String | 反馈类型 |
| title | String | 标题 |
| content | String | 内容 |
| contact | String | 联系方式 |
| status | String | 状态（pending/resolved） |
| reply | String | 回复 |
| createTime | LocalDateTime | 创建时间 |

### 13. OperationLog.java - 操作日志实体

**对应表**：`operation_log`

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Integer | 主键，自增 |
| userId | Integer | 用户ID |
| username | String | 用户名 |
| module | String | 模块 |
| operation | String | 操作 |
| type | String | 类型（CREATE/UPDATE/DELETE/QUERY） |
| method | String | 请求方法 |
| params | String | 请求参数 |
| ip | String | 客户端IP |
| duration | Integer | 执行时长（毫秒） |
| success | Boolean | 是否成功 |
| errorMsg | String | 错误信息 |
| createdAt | LocalDateTime | 创建时间 |

## 实体统计

| 实体类 | 对应表 | 主要用途 |
|-------|-------|---------|
| User | users | 用户管理 |
| Plant | plants | 药材信息 |
| Knowledge | knowledge | 知识库 |
| Inheritor | inheritors | 传承人信息 |
| Resource | resources | 学习资源 |
| Qa | qa | 问答管理 |
| QuizQuestion | quiz_questions | 答题题目 |
| QuizRecord | quiz_record | 答题记录 |
| PlantGameRecord | plant_game_record | 游戏记录 |
| Comment | comments | 评论管理 |
| Favorite | favorites | 收藏管理 |
| Feedback | feedback | 意见反馈 |
| OperationLog | operation_log | 操作日志 |
| **总计** | **13个表** | - |

## 开发规范

1. **注解使用**：
   - `@Data`：Lombok注解，自动生成getter/setter/toString等方法
   - `@TableName`：MyBatis-Plus注解，指定表名
   - `@TableId`：指定主键，支持自增
   - `@TableField`：指定字段映射
   - `@NotBlank`：参数验证注解

2. **命名规范**：
   - 类名使用大驼峰命名法
   - 字段名使用小驼峰命名法
   - 表名使用下划线命名法

3. **字段类型**：
   - 主键使用`Integer`类型
   - 时间使用`LocalDateTime`类型
   - 布尔值使用`Boolean`类型
   - JSON数据使用`String`类型存储

4. **JSON字段处理**：
   - 使用`@JsonIgnore`注解隐藏不需要序列化的字段
   - 提供getXxxList/setXxxList方法进行JSON转换

---

## 已知限制

| 实体 | 限制 | 影响 |
|------|------|------|
| User | 不支持OAuth2 | 第三方登录需扩展 |
| Plant | 图片字段为JSON字符串 | 需要手动解析 |
| QuizQuestion | 选项字段为JSON字符串 | 需要手动解析 |
| 所有实体 | 不支持软删除 | 删除数据会物理删除 |
| 所有实体 | 不支持多租户 | SaaS场景需扩展 |

---

## 未来改进建议

### 短期改进 (1-2周)

1. **字段优化**
   - 添加逻辑删除字段
   - 添加审计字段（创建人、更新人）
   - 实现JSON字段自动转换

2. **验证增强**
   - 添加字段验证注解
   - 实现字段默认值

### 中期改进 (1-2月)

1. **功能增强**
   - 添加乐观锁支持
   - 实现数据权限过滤
   - 添加数据加密字段

2. **性能优化**
   - 添加字段懒加载
   - 实现字段缓存

---

## 依赖要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| Lombok | 1.18+ | 自动生成代码 |
| MyBatis-Plus | 3.5+ | ORM框架 |
| Jackson | 2.15+ | JSON序列化 |
| Jakarta Validation | 3.0+ | 字段验证 |

---

## 常见问题

### 1. 如何添加新实体？

```java
@Data
@TableName("new_table")
public class NewEntity {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @NotBlank
    @TableField("name")
    private String name;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableLogic
    private Boolean deleted;
}
```

### 2. 如何处理JSON字段？

```java
@Data
public class Plant {
    
    @TableField("images")
    @JsonIgnore
    private String imagesJson;
    
    @TableField(exist = false)
    private List<String> images;
    
    public List<String> getImages() {
        if (images == null && imagesJson != null) {
            images = JSON.parseArray(imagesJson, String.class);
        }
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
        this.imagesJson = images != null ? JSON.toJSONString(images) : null;
    }
}
```

### 3. 如何实现软删除？

```java
@Data
@TableName("plants")
public class Plant {
    
    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;
    
    @TableField(value = "deleted_at", fill = FieldFill.UPDATE)
    private LocalDateTime deletedAt;
}

// 配置MyBatis-Plus
@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public ISqlInjector sqlInjector() {
        return new LogicSqlInjector();
    }
}
```

### 4. 如何添加审计字段？

```java
@Data
public class BaseEntity {
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT)
    private Integer createdBy;
    
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    
    @TableField(fill = FieldFill.UPDATE)
    private Integer updatedBy;
}

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdBy", Integer.class, getCurrentUserId());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Integer.class, getCurrentUserId());
    }
}
```

---

**最后更新时间**：2026年3月30日
