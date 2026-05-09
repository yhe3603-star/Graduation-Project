# Mapper 层 -- 数据访问（16个Mapper）

> Mapper 是三层架构的"数据访问层"，每个Mapper = 一张数据库表 + 若干自定义SQL方法。
> 所有Mapper继承 `BaseMapper<T>`，自动获得CRUD能力。自定义方法使用 `@Select`/`@Update` 注解直接编写SQL。
> 无需XML映射文件，所有SQL以注解方式内联在接口中。

---

## 一、Mapper 清单

| # | Mapper | 对应表 | 自定义方法数 | 自定义方法概要 |
|---|--------|--------|------------|-------------|
| 1 | **PlantMapper** | plants | 13 | 浏览量/收藏量递增、全文搜索、LIKE搜索、随机植物、统计聚合、去重筛选、分类/分布/浏览排行 |
| 2 | **KnowledgeMapper** | knowledge | 14 | 浏览量/收藏量递增、统计聚合(疗法/疾病/类型)、去重筛选(疗法/疾病/药材)、疗法分类/人气/药方/浏览排行 |
| 3 | **InheritorMapper** | inheritors | 8 | 浏览量/收藏量递增、级别计数、统计聚合、去重级别筛选、浏览排行 |
| 4 | **UserMapper** | users | 1 | 最近7天用户增长趋势 |
| 5 | **CommentMapper** | comments | 0 | 无（纯BaseMapper） |
| 6 | **FavoriteMapper** | favorites | 0 | 无（纯BaseMapper） |
| 7 | **FeedbackMapper** | feedback | 0 | 无（纯BaseMapper） |
| 8 | **QuizQuestionMapper** | quiz_questions | 1 | 随机获取题目（ORDER BY RAND()） |
| 9 | **QuizRecordMapper** | quiz_records | 0 | 无（纯BaseMapper） |
| 10 | **ResourceMapper** | resources | 10 | 浏览量/收藏量/下载量递增、统计聚合、去重分类、文件类型计数、文件大小汇总、全量文件路径 |
| 11 | **QaMapper** | qa | 7 | 浏览量/收藏量递增、统计聚合、去重分类筛选、分类人气排行 |
| 12 | **OperationLogMapper** | operation_log | 1 | 最近7天操作趋势 |
| 13 | **PlantGameRecordMapper** | plant_game_records | 0 | 无（纯BaseMapper） |
| 14 | **BrowseHistoryMapper** | browse_history | 0 | 无（纯BaseMapper） |
| 15 | **ChatHistoryMapper** | chat_history | 0 | 无（纯BaseMapper） |
| 16 | **SearchHistoryMapper** | search_history | 1 | 热门搜索关键词排行 |

---

## 二、自定义方法详解

### 2.1 PlantMapper（13个自定义方法）

#### 递增操作（3个）

```java
// 浏览量 +1
@Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
void incrementViewCount(Integer id);

// 收藏量 +delta，人气值 +delta*10
@Update("UPDATE plants SET favorite_count = IFNULL(favorite_count, 0) + #{delta}, popularity = IFNULL(popularity, 0) + #{delta} * 10 WHERE id = #{id}")
void incrementFavoriteCount(Integer id, int delta);

// 浏览量 +1，人气值 +3（PopularityAsyncService调用）
@Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1, popularity = IFNULL(popularity, 0) + 3 WHERE id = #{id}")
void incrementViewCount3AndPopularity(Integer id);
```

#### 搜索方法（3个）

```java
// 随机植物（基于ID范围随机，比ORDER BY RAND()高效）
@Select("SELECT * FROM plants WHERE id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants)) + 1) LIMIT #{limit}")
List<Plant> selectRandomPlants(int limit);

// 全文搜索（MySQL FULLTEXT索引，NATURAL LANGUAGE MODE）
@Select("SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY id DESC LIMIT #{limit}")
List<Plant> searchByFullText(String keyword, int limit);

// LIKE搜索（降级方案，ESCAPE '\\'防注入）
@Select("SELECT * FROM plants WHERE name_cn LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR ... LIMIT #{limit}")
List<Plant> searchByLike(String keyword, int limit);
```

#### 统计聚合（3个）

```java
int countDistinctCategory();   // SELECT COUNT(DISTINCT category)
long sumViewCount();           // SELECT IFNULL(SUM(view_count), 0)
long sumFavoriteCount();       // SELECT IFNULL(SUM(favorite_count), 0)
```

#### 去重筛选（2个）

```java
List<String> selectDistinctCategory();   // 植物分类选项
List<String> selectDistinctUsageWay();   // 用法选项
```

#### 排行统计（2个）

```java
List<Map<String, Object>> countByCategory(int limit);     // 分类分布统计
List<Map<String, Object>> countByDistribution(int limit); // 产地分布统计
List<Map<String, Object>> topByViewCount(int limit);      // 浏览量排行
```

### 2.2 KnowledgeMapper（14个自定义方法）

#### 递增操作（3个）

与PlantMapper模式相同：`incrementViewCount`、`incrementFavoriteCount`、`incrementViewCount3AndPopularity`

#### 统计聚合（5个）

```java
int countDistinctTherapyCategory();   // 疗法分类数
int countDistinctDiseaseCategory();   // 疾病分类数
int countDistinctType();              // 类型数
long sumViewCount();                  // 总浏览量
long sumFavoriteCount();              // 总收藏量
```

#### 去重筛选（3个）

```java
List<String> selectDistinctTherapyCategory();  // 疗法分类选项
List<String> selectDistinctDiseaseCategory();  // 疾病分类选项
List<String> selectDistinctHerbCategory();     // 药材分类选项
```

#### 排行统计（4个）

```java
List<Map<String, Object>> countByTherapyCategory(int limit);  // 疗法分类分布
List<Map<String, Object>> topByPopularity(int limit);         // 人气排行
List<Map<String, Object>> topFormulaByViewCount(int limit);   // 药方浏览排行（type='药方' OR title LIKE '%方%'）
List<Map<String, Object>> topByViewCount(int limit);          // 总浏览排行
```

### 2.3 InheritorMapper（8个自定义方法）

#### 递增操作（3个）

与PlantMapper模式相同：`incrementViewCount`、`incrementFavoriteCount`、`incrementViewCount3AndPopularity`

#### 统计聚合（3个）

```java
int countByLevel(String level);       // 按级别计数
long sumViewCount();                  // 总浏览量
long sumFavoriteCount();              // 总收藏量
```

#### 去重筛选（1个）

```java
List<String> selectDistinctLevel();   // 级别选项
```

#### 排行统计（1个）

```java
List<Map<String, Object>> topByViewCount(int limit);  // 浏览量排行
```

### 2.4 UserMapper（1个自定义方法）

```java
// 最近7天用户注册趋势（按日期分组）
@Select("SELECT DATE(created_at) AS date, COUNT(*) AS count FROM users " +
        "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
        "GROUP BY DATE(created_at) ORDER BY date")
List<Map<String, Object>> countByDateLast7Days();
```

### 2.5 QuizQuestionMapper（1个自定义方法）

```java
// 随机获取题目（MySQL ORDER BY RAND()）
@Select("SELECT * FROM quiz_questions ORDER BY RAND() LIMIT #{limit}")
List<QuizQuestion> selectRandomQuestions(int limit);
```

### 2.6 ResourceMapper（10个自定义方法）

#### 递增操作（4个）

```java
void incrementViewCount(Integer id);                    // 浏览量 +1
void incrementFavoriteCount(Integer id, int delta);     // 收藏量 +delta, 人气值 +delta*10
void incrementDownloadCount(Integer id);                // 下载量 +1
void incrementViewCount3AndPopularity(Integer id);      // 浏览量 +1, 人气值 +3
```

#### 统计聚合（4个）

```java
long sumViewCount();           // 总浏览量
long sumFavoriteCount();       // 总收藏量
long sumDownloadCount();       // 总下载量
long sumFileSize();            // 总文件大小（JSON_EXTRACT解析files字段）
```

#### 去重与查询（2个）

```java
List<String> selectDistinctCategory();    // 分类选项
long countByFileType(String mimeType);    // 按MIME类型计数（JSON_SEARCH查询files字段）
List<String> selectAllFiles();            // 全量文件路径（用于文件清理校验）
```

### 2.7 QaMapper（7个自定义方法）

#### 递增操作（3个）

与PlantMapper模式相同：`incrementViewCount`、`incrementFavoriteCount`、`incrementViewCount3AndPopularity`

#### 统计聚合（3个）

```java
int countDistinctCategory();     // 分类数
long sumViewCount();             // 总浏览量
long sumFavoriteCount();         // 总收藏量
```

#### 去重筛选（1个）

```java
List<String> selectDistinctCategory();   // 分类选项
```

#### 排行统计（1个）

```java
List<Map<String, Object>> topCategoryByPopularity(int limit);  // 分类人气排行
```

### 2.8 OperationLogMapper（1个自定义方法）

```java
// 最近7天操作趋势（按日期分组）
@Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM operation_log " +
        "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
        "GROUP BY DATE(created_at) ORDER BY date")
List<Map<String, Object>> selectTrendLast7Days();
```

### 2.9 SearchHistoryMapper（1个自定义方法）

```java
// 热门搜索关键词排行（最近7天，按频次降序）
@Select("SELECT keyword AS name, COUNT(*) AS value FROM search_history " +
        "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
        "GROUP BY keyword ORDER BY value DESC LIMIT #{limit}")
List<Map<String, Object>> topKeywords(int limit);
```

---

## 三、设计模式与规范

### 3.1 递增操作统一模式

所有内容实体（Plant、Knowledge、Inheritor、Qa、Resource）的Mapper都遵循相同的递增方法模式：

| 方法 | SQL模式 | 用途 |
|------|---------|------|
| `incrementViewCount(id)` | `SET view_count = IFNULL(view_count, 0) + 1` | 单独增加浏览量 |
| `incrementFavoriteCount(id, delta)` | `SET favorite_count += delta, popularity += delta * 10` | 收藏变化时同步更新人气值 |
| `incrementViewCount3AndPopularity(id)` | `SET view_count += 1, popularity += 3` | 浏览时同步更新人气值（异步调用） |

**IFNULL防护**：所有递增操作使用 `IFNULL(field, 0)` 防止NULL值导致运算结果为NULL。

### 3.2 人气值权重设计

```
人气值(popularity) = 浏览量贡献(3/次) + 收藏量贡献(10/次)
```

- 浏览1次：popularity += 3
- 收藏1次：popularity += 10（delta=1时）
- 取消收藏：popularity -= 10（delta=-1时）

收藏的权重远高于浏览，体现"收藏=深度认可"的产品理念。

### 3.3 统计聚合方法模式

所有统计方法使用 `IFNULL(SUM(field), 0)` 确保空表也返回0而非NULL：

```java
@Select("SELECT IFNULL(SUM(view_count), 0) FROM plants")
long sumViewCount();
```

去重筛选使用 `WHERE field IS NOT NULL AND field != ''` 过滤空值：

```java
@Select("SELECT DISTINCT category FROM plants WHERE category IS NOT NULL AND category != '' ORDER BY category")
List<String> selectDistinctCategory();
```

### 3.4 排行统计返回格式

所有排行方法统一返回 `List<Map<String, Object>>`，每个Map包含 `name` 和 `value` 两个key：

```java
@Select("SELECT category AS name, COUNT(*) AS value FROM plants ... GROUP BY category ORDER BY value DESC LIMIT #{limit}")
List<Map<String, Object>> countByCategory(int limit);
```

### 3.5 全文搜索 vs LIKE搜索

```java
// 全文搜索（需要MySQL FULLTEXT索引）
// 优点：利用索引，速度快；支持相关度排序
// 缺点：需要预先建索引；短词可能搜不到
searchByFullText(keyword, limit)

// LIKE搜索（降级方案）
// 优点：无需额外索引；总是可用
// 缺点：全表扫描，数据量大时慢
searchByLike(escapedKeyword, limit)
```

LIKE搜索使用 `ESCAPE '\\'` 转义，配合Service层的 `PageUtils.escapeLike()` 防注入。

---

## 四、Mapper编程模板

### 纯BaseMapper（无自定义方法）

```java
@Mapper
public interface XxxMapper extends BaseMapper<Xxx> {}
```

### 带自定义方法的Mapper

```java
@Mapper
public interface XxxMapper extends BaseMapper<Xxx> {

    // 递增操作
    @Update("UPDATE xxx SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    // 统计聚合
    @Select("SELECT IFNULL(SUM(view_count), 0) FROM xxx")
    long sumViewCount();

    // 去重筛选
    @Select("SELECT DISTINCT category FROM xxx WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategory();

    // 排行统计
    @Select("SELECT category AS name, COUNT(*) AS value FROM xxx WHERE category IS NOT NULL AND category != '' GROUP BY category ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByCategory(@Param("limit") int limit);
}
```

---

## 五、依赖关系

```
Mapper接口
  ↓ 被继承
ServiceImpl（通过baseMapper字段访问）
  ↓ 被注入
Controller -> Service -> Mapper -> MySQL
```

每个Mapper对应一张数据库表，ServiceImpl通过 `baseMapper` 字段（ServiceImpl提供）访问Mapper的自定义方法。
