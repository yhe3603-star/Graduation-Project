# Mapper 层 -- 数据访问层（15个Mapper）

> 每个Mapper继承 `BaseMapper<Entity>` 自动获得17+个CRUD方法。自定义SQL通过 `@Select` / `@Update` 注解实现。

---

## 一、Mapper 清单

| # | Mapper | 对应Entity | 表名 | 自定义方法说明 |
|---|--------|-----------|------|--------------|
| 1 | PlantMapper | Plant | plants | incrementViewCount, incrementFavoriteCount, selectRandomPlants, searchByFullText, searchByLike, countDistinctCategory, sumViewCount, sumFavoriteCount, selectDistinctCategory, selectDistinctUsageWay, countByCategory, countByDistribution |
| 2 | KnowledgeMapper | Knowledge | knowledge | incrementViewCount, incrementFavoriteCount, incrementPopularity, countDistinctTherapy/Disease/Type, sumViewCount, sumFavoriteCount, selectDistinctTherapy/Disease/HerbCategory, countByTherapyCategory, topByPopularity, topFormulaByViewCount, topByViewCount |
| 3 | InheritorMapper | Inheritor | inheritors | countByLevel |
| 4 | QaMapper | Qa | qa | topCategoryByPopularity |
| 5 | UserMapper | User | users | 无（纯BaseMapper CRUD） |
| 6 | ResourceMapper | Resource | resources | 无 |
| 7 | CommentMapper | Comment | comments | 无 |
| 8 | FavoriteMapper | Favorite | favorites | 无 |
| 9 | FeedbackMapper | Feedback | feedback | 无 |
| 10 | QuizQuestionMapper | QuizQuestion | quiz_questions | 无 |
| 11 | QuizRecordMapper | QuizRecord | quiz_record | 无 |
| 12 | PlantGameRecordMapper | PlantGameRecord | plant_game_record | 无 |
| 13 | OperationLogMapper | OperationLog | operation_log | selectTrendLast7Days |
| 14 | BrowseHistoryMapper | BrowseHistory | browse_history | 无 |
| 15 | ChatHistoryMapper | ChatHistory | chat_history | 无 |

---

## 二、BaseMapper 自动方法

所有Mapper继承 `BaseMapper<Entity>` 后自动拥有以下方法（无需写代码）：

```java
// 插入
int insert(T entity);

// 删除
int deleteById(Serializable id);
int deleteByMap(Map<String, Object> columnMap);
int delete(Wrapper<T> wrapper);
int deleteBatchIds(Collection<?> idList);

// 更新
int updateById(T entity);
int update(T entity, Wrapper<T> updateWrapper);

// 查询
T selectById(Serializable id);
List<T> selectBatchIds(Collection<?> idList);
List<T> selectByMap(Map<String, Object> columnMap);
List<T> selectList(Wrapper<T> queryWrapper);
Map<String, Object> selectMap(Wrapper<T> queryWrapper);
List<Map<String, Object>> selectMaps(Wrapper<T> queryWrapper);

// 分页
<E extends IPage<T>> E selectPage(E page, Wrapper<T> queryWrapper);

// 计数
Long selectCount(Wrapper<T> queryWrapper);
```

---

## 三、PlantMapper -- 自定义方法最多的Mapper

文件：`mapper/PlantMapper.java`

```java
@Mapper
public interface PlantMapper extends BaseMapper<Plant> {

    // === 计数更新 ===
    @Update("UPDATE plants SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE plants SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Update("UPDATE plants SET popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementPopularity(Integer id);

    // === 随机查询 ===
    @Select("SELECT * FROM plants WHERE id >= " +
            "(SELECT FLOOR(RAND() * (SELECT MAX(id) FROM plants)) + 1) LIMIT #{limit}")
    List<Plant> selectRandomPlants(@Param("limit") int limit);

    // === 全文搜索（利用MySQL FULLTEXT索引） ===
    @Select("SELECT * FROM plants WHERE MATCH(name_cn, name_dong, efficacy, story) " +
            "AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByFullText(@Param("keyword") String keyword, @Param("limit") int limit);

    // === LIKE搜索（全文索引不可用时的降级方案） ===
    @Select("SELECT * FROM plants WHERE " +
            "name_cn LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "name_dong LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "efficacy LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' OR " +
            "story LIKE CONCAT('%', #{keyword}, '%') ESCAPE '\\' " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<Plant> searchByLike(@Param("keyword") String keyword, @Param("limit") int limit);

    // === 统计查询 ===
    @Select("SELECT COUNT(DISTINCT category) FROM plants WHERE category IS NOT NULL AND category != ''")
    int countDistinctCategory();

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM plants")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM plants")
    long sumFavoriteCount();

    // === 去重查询（用于筛选器） ===
    @Select("SELECT DISTINCT category FROM plants WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategory();

    @Select("SELECT DISTINCT usage_way FROM plants WHERE usage_way IS NOT NULL AND usage_way != '' ORDER BY usage_way")
    List<String> selectDistinctUsageWay();

    // === 分组统计 ===
    @Select("SELECT category AS name, COUNT(*) AS value FROM plants WHERE category IS NOT NULL AND category != '' GROUP BY category ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByCategory(@Param("limit") int limit);

    @Select("SELECT distribution AS name, COUNT(*) AS value FROM plants WHERE distribution IS NOT NULL AND distribution != '' GROUP BY distribution ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByDistribution(@Param("limit") int limit);
}
```

**关键设计点**：
- `searchByFullText` 使用MySQL全文索引 `MATCH ... AGAINST`
- `searchByLike` 中 `ESCAPE '\\'` 配合 `PageUtils.escapeLike()` 防LIKE注入
- `selectRandomPlants` 使用子查询实现高效随机取样
- 统计查询均使用 `IFNULL` 避免NULL值问题

---

## 四、KnowledgeMapper -- 知识库Mapper

文件：`mapper/KnowledgeMapper.java`

```java
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    // 计数操作
    @Update("UPDATE knowledge SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Integer id);

    @Update("UPDATE knowledge SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(@Param("id") Integer id, @Param("delta") int delta);

    @Update("UPDATE knowledge SET popularity = IFNULL(popularity, 0) + 1 WHERE id = #{id}")
    void incrementPopularity(@Param("id") Integer id);

    // 统计
    @Select("SELECT COUNT(DISTINCT therapy_category) FROM knowledge WHERE therapy_category IS NOT NULL AND therapy_category != ''")
    int countDistinctTherapyCategory();

    @Select("SELECT COUNT(DISTINCT disease_category) FROM knowledge WHERE disease_category IS NOT NULL AND disease_category != ''")
    int countDistinctDiseaseCategory();

    @Select("SELECT COUNT(DISTINCT type) FROM knowledge WHERE type IS NOT NULL AND type != ''")
    int countDistinctType();

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM knowledge")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM knowledge")
    long sumFavoriteCount();

    // 去重查询（筛选器）
    @Select("SELECT DISTINCT therapy_category FROM knowledge WHERE therapy_category IS NOT NULL AND therapy_category != '' ORDER BY therapy_category")
    List<String> selectDistinctTherapyCategory();

    @Select("SELECT DISTINCT disease_category FROM knowledge WHERE disease_category IS NOT NULL AND disease_category != '' ORDER BY disease_category")
    List<String> selectDistinctDiseaseCategory();

    @Select("SELECT DISTINCT herb_category FROM knowledge WHERE herb_category IS NOT NULL AND herb_category != '' ORDER BY herb_category")
    List<String> selectDistinctHerbCategory();

    // 分组统计
    @Select("SELECT therapy_category AS name, COUNT(*) AS value FROM knowledge WHERE therapy_category IS NOT NULL AND therapy_category != '' GROUP BY therapy_category ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> countByTherapyCategory(@Param("limit") int limit);

    // 热门排行
    @Select("SELECT title AS name, popularity AS value FROM knowledge WHERE popularity > 0 ORDER BY popularity DESC LIMIT #{limit}")
    List<Map<String, Object>> topByPopularity(@Param("limit") int limit);

    @Select("SELECT title AS name, view_count AS value FROM knowledge WHERE (type = '药方' OR title LIKE '%方%') AND view_count > 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> topFormulaByViewCount(@Param("limit") int limit);

    @Select("SELECT title AS name, view_count AS value FROM knowledge WHERE view_count > 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> topByViewCount(@Param("limit") int limit);
}
```

---

## 五、LambdaQueryWrapper 查询构造

Service层大量使用 `LambdaQueryWrapper` 构建类型安全的查询条件：

```java
// PlantServiceImpl 中的典型用法
LambdaQueryWrapper<Plant> qw = new LambdaQueryWrapper<>();
qw.like(Plant::getNameCn, escapedKeyword)      // LIKE %keyword%
  .or().like(Plant::getNameDong, escapedKeyword)
  .eq(Plant::getCategory, "清热药")             // = '清热药'
  .orderByAsc(Plant::getNameCn)                 // ORDER BY name_cn ASC
  .last("LIMIT 10");                            // LIMIT 10 (谨慎使用)

List<Plant> result = plantMapper.selectList(qw);
```

### 常用LambdaQueryWrapper方法对照表

| 方法 | 等价SQL | 说明 |
|------|---------|------|
| `eq(字段, 值)` | `= 值` | 等于 |
| `ne(字段, 值)` | `!= 值` | 不等于 |
| `like(字段, 值)` | `LIKE '%值%'` | 模糊匹配 |
| `gt/ge/lt/le(字段, 值)` | `>/>=/</<=` | 数值比较 |
| `in(字段, 列表)` | `IN (v1, v2, ...)` | 在列表中 |
| `isNull/isNotNull(字段)` | `IS NULL / IS NOT NULL` | 空判断 |
| `orderByAsc/Desc(字段)` | `ORDER BY ... ASC/DESC` | 排序 |
| `between(字段, v1, v2)` | `BETWEEN v1 AND v2` | 区间 |
| `groupBy(字段)` | `GROUP BY ...` | 分组 |

---

## 六、MyBatis-Plus配置

文件：`config/MybatisPlusConfig.java`

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件（MySQL方言）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### application.yml配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 下划线自动转驼峰（name_cn → nameCn）
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl  # 生产环境不输出SQL
```

---

## 七、@Mapper 注解

每个Mapper接口必须标注 `@Mapper` 注解，MyBatis-Plus在启动时扫描并注册：

```java
@Mapper  // 必须加！告诉Spring这是一个Mapper接口
public interface PlantMapper extends BaseMapper<Plant> { ... }
```

忘记加 `@Mapper` 会导致运行时报错：
`required a bean of type 'xxxMapper' that could not be found`
