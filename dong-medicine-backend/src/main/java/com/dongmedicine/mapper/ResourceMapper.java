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

    // ===== 统计查询方法 =====

    @Select("SELECT IFNULL(SUM(view_count), 0) FROM resources")
    long sumViewCount();

    @Select("SELECT IFNULL(SUM(favorite_count), 0) FROM resources")
    long sumFavoriteCount();

    @Select("SELECT IFNULL(SUM(download_count), 0) FROM resources")
    long sumDownloadCount();

    @Select("SELECT files FROM resources WHERE files IS NOT NULL AND files != ''")
    List<String> selectAllFiles();

    // ===== 去重查询方法（用于筛选器） =====

    @Select("SELECT DISTINCT category FROM resources WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategory();
}
