package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.Knowledge;
import java.util.List;

public interface KnowledgeService extends IService<Knowledge> {
    List<Knowledge> advancedSearch(String keyword, String therapy, String disease, String sortBy);
    Knowledge getDetailWithRelated(Integer id);
    void addFavorite(Integer userId, Integer knowledgeId);
    void submitFeedback(Integer knowledgeId, String feedback);
    void incrementViewCount(Integer id);
    void clearCache();
    void deleteWithFiles(Integer id);
}
