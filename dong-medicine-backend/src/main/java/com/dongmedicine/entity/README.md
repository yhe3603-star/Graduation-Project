# Entity 层 -- 实体类（数据库表的 Java 翻译模板）

> 本目录存放所有实体类（Entity），每一个实体类对应数据库中的一张表。

---

## 一、什么是 Entity？

**类比：数据库表和 Java 类之间的"翻译模板"**

想象一下，数据库里有一张 `plants` 表，里面存着钩藤、透骨草等各种药用植物的数据。
但 Java 代码不能直接操作数据库表，它只认识"对象"。

Entity 就是那个"翻译模板"：它告诉 Java，数据库里的每一行数据，应该被翻译成什么样的 Java 对象。

```
+-------------------+          Entity           +-------------------+
|   数据库 plants 表  |    <----翻译模板---->     |   Java Plant 对象   |
+-------------------+                           +-------------------+
| id = 1            |                           | id = 1            |
| name_cn = "钩藤"   |    一一对应，自动翻译       | nameCn = "钩藤"    |
| efficacy = "清热.." |                          | efficacy = "清热.." |
+-------------------+                           +-------------------+
```

简单说：**一张表 = 一个 Entity 类 = 一行数据 = 一个 Java 对象**

---

## 二、什么是 ORM？

**类比：翻译官**

ORM 全称是 **Object-Relational Mapping**（对象-关系映射），就像一个翻译官：

- Java 世界说"对象语言"（`plant.getNameCn()`）
- 数据库世界说"SQL 语言"（`SELECT name_cn FROM plants`）
- ORM 翻译官自动帮你把两种语言互相翻译

```
  Java 世界                         数据库世界
+-----------+     ORM 翻译官     +-----------+
| 对象.属性  |  ===============>  | 表.列     |
| plant.id  |  <===============  | plants.id |
+-----------+                    +-----------+
```

我们项目用的 ORM 框架是 **MyBatis-Plus**，它是 MyBatis 的增强版，能自动完成大部分翻译工作。

---

## 三、核心注解详解

### 3.1 @TableName -- 告诉翻译官"我对应哪张表"

```java
@TableName("plants")
public class Plant { ... }
```

**含义**：这个 Plant 类对应数据库里的 `plants` 表。

如果不加这个注解，MyBatis-Plus 会默认按类名找表（Plant -> plant），但我们的表名是 `plants`（复数），所以必须显式指定。

**常见错误**：忘记加 @TableName，导致报错 "Table 'dong_medicine.plant' doesn't exist"。

---

### 3.2 @TableId -- 告诉翻译官"哪个字段是主键"

```java
@TableId(type = IdType.AUTO)
private Integer id;
```

**含义**：
- `id` 是主键
- `IdType.AUTO` 表示主键由数据库自动递增（AUTO_INCREMENT）

```
数据库：id INT NOT NULL AUTO_INCREMENT
                    ^^^^^^^^^^^^^^^
Java：  @TableId(type = IdType.AUTO)
                    ^^^^^^^^^^^^^^^^
        两者必须对应！
```

---

### 3.3 @TableField -- 字段映射 + 自动填充

> 类比：@TableField 就像**标签贴纸**，告诉翻译官"这个Java字段对应数据库的哪个列"，以及"这个字段需要自动填充"。

当 Java 字段名和数据库列名不一致时，需要用 @TableField 手动指定；也可以用它配置自动填充策略：

```java
// 情况1：Java字段名和数据库列名不一致时，手动指定映射
@TableField("password_hash")      // Java: passwordHash → 数据库: password_hash
private String passwordHash;

// 情况2：自动填充 -- 插入时自动设置当前时间
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createdAt;

// 情况3：自动填充 -- 插入和更新时都自动设置当前时间
@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updatedAt;

// 情况4：自动填充 + 列名映射（字段名和列名不一致时）
@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createTime;
```

**什么时候需要用 @TableField？**

| 情况 | 是否需要 @TableField | 示例 |
|------|---------------------|------|
| Java 名和数据库列名能自动匹配 | 不需要 | `nameCn` -> `name_cn`（自动转换） |
| Java 名和数据库列名不能自动匹配 | **需要** | `passwordHash` -> `password_hash`（需手动指定） |
| `createTime` -> `created_at` | **需要** | 两者差异太大，自动匹配不上 |
| 需要自动填充时间等字段 | **需要** | `createdAt` 用 `fill = FieldFill.INSERT` |

**自动填充策略说明：**

| 策略 | 说明 | 使用场景 |
|------|------|----------|
| `FieldFill.INSERT` | 仅插入时填充 | `createdAt` 创建时间 |
| `FieldFill.UPDATE` | 仅更新时填充 | 单独的更新时间字段 |
| `FieldFill.INSERT_UPDATE` | 插入和更新时都填充 | `updatedAt` 更新时间 |
| `FieldFill.DEFAULT` | 不自动填充 | 默认值，可省略 |

**注意**：自动填充需要在 `MyMetaObjectHandler.java` 中配合实现，详见 config 目录 README。

---

### 3.4 @Data -- Lombok 魔法注解

```java
@Data
public class Plant { ... }
```

**含义**：Lombok 自动帮你生成以下代码：

```java
// 以下代码你不用手写，@Data 全帮你生成了！
public Integer getId() { return id; }
public void setId(Integer id) { this.id = id; }
public String getNameCn() { return nameCn; }
public void setNameCn(String nameCn) { this.nameCn = nameCn; }
// ... 每个字段都有 getter 和 setter ...

public boolean equals(Object o) { ... }
public int hashCode() { ... }
public String toString() { ... }
```

**为什么用 @Data？** 一个 Plant 有十几个字段，手写 getter/setter 要写上百行代码，用 @Data 一行搞定！

---

## 四、字段命名映射规则（camelCase -> snake_case）

MyBatis-Plus 默认开启驼峰命名转换：

```
Java 字段名（驼峰）          数据库列名（下划线）
-------------------         -------------------
nameCn            -->       name_cn
scientificName    -->       scientific_name
createdAt         -->       created_at
viewCount         -->       view_count
passwordHash      -->       password_hash
```

**规则很简单**：
- Java 用驼峰：第一个单词小写，后面每个单词首字母大写（如 `nameCn`）
- 数据库用下划线：所有字母小写，单词之间用下划线连接（如 `name_cn`）
- MyBatis-Plus 自动帮你转换！

---

## 五、完整示例：Plant 实体类逐行解读

```java
package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;      // 主键类型枚举
import com.baomidou.mybatisplus.annotation.TableId;      // 主键注解
import com.baomidou.mybatisplus.annotation.TableName;    // 表名注解
import jakarta.validation.constraints.NotBlank;          // 校验注解：不能为空
import lombok.Data;                                      // Lombok 注解

import java.time.LocalDateTime;                          // 日期时间类型

@Data                           // [1] Lombok：自动生成 getter/setter/toString/equals/hashCode
@TableName("plants")            // [2] 对应数据库的 plants 表
public class Plant {

    @TableId(type = IdType.AUTO) // [3] 主键，数据库自动递增
    private Integer id;

    @NotBlank(message = "中文名称不能为空")  // [4] 校验：保存时 nameCn 不能为空
    private String nameCn;       // [5] 映射到数据库的 name_cn 列
    private String nameDong;     //      映射到 name_dong
    private String scientificName; //    映射到 scientific_name
    private String category;     //      映射到 category
    private String usageWay;     //      映射到 usage_way
    private String habitat;      //      映射到 habitat
    private String efficacy;     //      映射到 efficacy
    private String story;        //      映射到 story
    private String images;       //      映射到 images（JSON 字符串）
    private String videos;       //      映射到 videos（JSON 字符串）
    private String documents;    //      映射到 documents（JSON 字符串）
    private String distribution; //      映射到 distribution
    private String updateLog;    //      映射到 update_log
    private LocalDateTime createdAt; //  映射到 created_at（日期时间）
    private Integer viewCount, favoriteCount, popularity; // 统计字段
}
```

**关键点说明**：

| 行号 | 注解/代码 | 作用 |
|------|----------|------|
| [1] | `@Data` | 自动生成 getter/setter，省去大量样板代码 |
| [2] | `@TableName("plants")` | 指定对应的数据库表名 |
| [3] | `@TableId(type = IdType.AUTO)` | 声明主键，且由数据库自增 |
| [4] | `@NotBlank` | 保存时校验，如果 nameCn 为空则报错 |
| [5] | `private String nameCn` | 自动映射到 `name_cn` 列 |

---

## 六、本项目全部 13 个实体类一览

| 序号 | 实体类 | 数据库表名 | 主要字段 | 用途说明 |
|------|--------|-----------|---------|---------|
| 1 | `Plant` | `plants` | nameCn, nameDong, efficacy, story | 药用植物 |
| 2 | `Knowledge` | `knowledge` | title, type, content, formula | 知识库（药方/疗法） |
| 3 | `Inheritor` | `inheritors` | name, level, bio, specialties | 传承人 |
| 4 | `Qa` | `qa` | category, question, answer | 问答知识 |
| 5 | `Resource` | `resources` | title, category, files, description | 学习资源 |
| 6 | `User` | `users` | username, passwordHash, role, status | 用户 |
| 7 | `Comment` | `comments` | targetType, targetId, content, likes | 评论 |
| 8 | `Favorite` | `favorites` | userId, targetType, targetId | 收藏 |
| 9 | `Feedback` | `feedback` | type, title, content, status, reply | 反馈 |
| 10 | `QuizQuestion` | `quiz_questions` | question, options, answer, explanation | 测验题目 |
| 11 | `QuizRecord` | `quiz_record` | userId, score, totalQuestions | 测验记录 |
| 12 | `PlantGameRecord` | `plant_game_record` | userId, difficulty, score, correctCount | 植物游戏记录 |
| 13 | `OperationLog` | `operation_log` | userId, module, operation, type | 操作日志 |

---

## 七、特殊实体类说明

### 7.1 User -- 使用了 @TableField

```java
@Data
@TableName("users")
public class User {
    public static final String STATUS_ACTIVE = "active";   // 状态常量
    public static final String STATUS_BANNED = "banned";

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    @TableField("password_hash")    // <-- 手动映射，因为自动匹配不上
    private String passwordHash;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public boolean isBanned() {     // 业务方法：判断用户是否被封禁
        return STATUS_BANNED.equals(this.status);
    }
}
```

**为什么 passwordHash 需要 @TableField？**
- Java 字段名：`passwordHash`
- 自动转换结果：`password_hash`（看起来能匹配上？）
- 实际上 MyBatis-Plus 的驼峰转换会把 `passwordHash` 转为 `password_hash`，这里是可以匹配的
- 但为了**明确语义、避免歧义**，项目选择显式声明

### 7.2 QuizQuestion -- 包含 JSON 字段处理

```java
@Data
@TableName("quiz_questions")
public class QuizQuestion {
    @TableId
    private Integer id;
    private String question;
    @JsonIgnore                    // 返回给前端时不显示原始 options 字符串
    private String options;        // 数据库存的是 JSON 字符串：["A","B","C","D"]
    private String answer;
    private String category;
    private String correctAnswer;
    private String explanation;

    // 手动写方法：把 JSON 字符串转成 List<String>
    public List<String> getOptionList() { ... }
    // 手动写方法：把 List<String> 转成 JSON 字符串
    public void setOptionList(List<String> optionList) { ... }
}
```

**为什么 options 用 String 存 JSON？**
数据库没有"数组"类型，所以把 `["选项A","选项B","选项C","选项D"]` 作为字符串存入数据库，
在 Java 代码中通过 `getOptionList()` 方法解析成 `List<String>` 使用。

### 7.3 QuizRecord / PlantGameRecord -- 字段名不匹配的例子

```java
@Data
@TableName("quiz_record")
public class QuizRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    @TableField("created_at")      // <-- createTime 和 created_at 差异大，需手动映射
    private LocalDateTime createTime;
}
```

---

## 八、常见错误与解决方案

### 错误 1：忘记加 @TableName

```
报错：Table 'dong_medicine.plant' doesn't exist
原因：MyBatis-Plus 默认用类名 Plant 找表 plant，但实际表名是 plants
解决：加 @TableName("plants")
```

### 错误 2：字段名和数据库列名不匹配

```
报错：Column 'create_time' not found
原因：Java 字段名 createTime，数据库列名 created_at，自动匹配不上
解决：加 @TableField("created_at")
```

### 错误 3：忘记加 @Data

```
报错：Cannot find getter for property 'nameCn'
原因：没有 @Data，没有 getter/setter 方法
解决：加 @Data 注解
```

### 错误 4：日期类型用错

```
错误写法：private Date createdAt;      // 过时的 java.util.Date
正确写法：private LocalDateTime createdAt;  // 推荐的 java.time.LocalDateTime
```

### 错误 5：JSON 字段用错类型

```
错误写法：private List<String> images;    // MyBatis-Plus 不知道怎么存 List
正确写法：private String images;           // 用 String 存 JSON，前端负责解析
```

---

## 九、速记口诀

```
一张表，一个类，@TableName 来配对
主键加 @TableId，AUTO 递增最常见
字段名驼峰写，自动映射下划线
@Data 一加全都有，getter/setter 不用愁
名字对不上别慌张，@TableField 来帮忙
日期就用 LocalDateTime，JSON 字段用 String 存
```

---

## 代码审查与改进建议

- **[结构] @NotBlank注解在Entity层无效**：Plant、Knowledge、Inheritor、Resource、Qa五个Entity在字段上使用了`@NotBlank`注解，但Bean Validation注解在MyBatis-Plus操作时不会生效，应放在DTO层
- **[结构] 时间字段命名不一致**：User/Plant/Knowledge等Entity使用`createdAt`，而Feedback/QuizRecord/PlantGameRecord使用`createTime`，但数据库列名都是`created_at`
- **[结构] 多字段声明在同一行**：Plant、Knowledge、Inheritor、Resource中存在`private Integer viewCount, favoriteCount, popularity;`这样的多字段单行声明
- **[结构] JSON字段处理方式不统一**：QuizQuestion提供了`getOptionList()`/`setOptionList()`转换方法，但其他Entity的images/videos/documents字段都是裸String
- **[结构] 缺少updatedAt字段**：大部分Entity只有createdAt没有updatedAt
- **[结构] id使用Integer而非Long**：所有Entity的id都使用Integer类型，对于数据增长较快的表可能不足
- **[结构] 缺少通用基类**：多个Entity有共同字段(id, createdAt, viewCount, favoriteCount, popularity)，可通过抽取BaseEntity减少重复
- **[安全] Comment冗余存储username**：评论表同时存储userId和username，用户名修改时数据不一致
- **[安全] QuizQuestion答案字段未加@JsonIgnore**：answer字段暴露给前端破坏测验公平性
- **[安全] OperationLog的params字段可能记录敏感数据**
