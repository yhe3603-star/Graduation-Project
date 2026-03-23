package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Resource;
import java.util.List;

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
}
