package com.dongmedicine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dongmedicine.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE resources SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    void incrementViewCount(Integer id);

    @Update("UPDATE resources SET favorite_count = IFNULL(favorite_count, 0) + #{delta} WHERE id = #{id}")
    void incrementFavoriteCount(Integer id, int delta);

    @Update("UPDATE resources SET download_count = IFNULL(download_count, 0) + 1 WHERE id = #{id}")
    void incrementDownloadCount(Integer id);

    @Update("UPDATE resources SET view_count = IFNULL(view_count, 0) + 1, popularity = IFNULL(popularity, 0) + 3 WHERE id = #{id}")
    void incrementViewCount3AndPopularity(Integer id);

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM resources")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM resources")
    long sumFavoriteCount();

    @Select("SELECT IFNULL(SUM(download_count), 0) FROM resources")
    long sumDownloadCount();

    @Select("SELECT files FROM resources WHERE files IS NOT NULL AND files != ''")
    List<String> selectAllFiles();

    @Select("SELECT DISTINCT category FROM resources WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategory();

    @Select("SELECT COUNT(*) FROM resources WHERE files IS NOT NULL AND JSON_SEARCH(files, 'one', #{mimeType}, NULL, '$[*].type') IS NOT NULL")
    long countByFileType(String mimeType);

    @Select("SELECT IFNULL(SUM(CAST(JSON_EXTRACT(item, '$.size') AS UNSIGNED)), 0) " +
            "FROM resources, JSON_TABLE(files, '$[*]' COLUMNS (item JSON PATH '$')) AS jt " +
            "WHERE files IS NOT NULL AND files != ''")
    long sumFileSize();
}
