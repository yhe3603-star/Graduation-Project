package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Resource;
import java.util.List;
import java.util.Map;

public interface ResourceService extends IService<Resource> {
    void incrementDownload(Integer id);
    void incrementViewCount(Integer id);
    List<Resource> listByCategoryAndKeyword(String category, String keyword);
    List<Resource> listByCategoryAndKeywordAndType(String category, String keyword, String fileType);
    Page<Resource> pageByCategoryAndKeywordAndType(String category, String keyword, String fileType, Integer page, Integer size);
    List<Resource> getHotResources();
    List<String> getAllCategories();
    void clearCache();
    void deleteWithFiles(Integer id);

    /**
     * 获取资源统计数据（使用SQL聚合查询+仅查询files字段，避免全表加载）
     *
     * @return 统计结果Map，包含total、videoCount、documentCount、imageCount、totalFavorites、totalViews、totalDownloads、totalSize
     */
    Map<String, Object> getStats();

    /**
     * 获取资源筛选选项（使用SQL DISTINCT查询，避免全表加载）
     *
     * @return 筛选选项Map，包含category
     */
    Map<String, List<String>> getFilterOptions();
}
