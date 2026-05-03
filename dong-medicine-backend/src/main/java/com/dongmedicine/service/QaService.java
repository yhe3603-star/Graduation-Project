package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Qa;
import java.util.List;
import java.util.Map;

public interface QaService extends IService<Qa> {
    List<Qa> listByCategory(String category);
    Page<Qa> pageByCategory(String category, Integer page, Integer size);

    Page<Qa> advancedSearchPaged(String keyword, String category, Integer page, Integer size);
    List<Qa> search(String keyword);
    Page<Qa> searchPaged(String keyword, Integer page, Integer size);
    void incrementViewCount(Integer id);
    Qa getDetail(Integer id);

    /**
     * 获取问答统计数据（使用SQL聚合查询，避免全表加载）
     *
     * @return 统计结果Map，包含total、categoryCount、totalViews、totalFavorites
     */
    Map<String, Object> getStats();

    /**
     * 获取问答筛选选项（使用SQL DISTINCT查询，避免全表加载）
     *
     * @return 筛选选项Map，包含category
     */
    Map<String, List<String>> getFilterOptions();
}
