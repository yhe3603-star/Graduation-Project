package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.common.util.FileCleanupHelper;
import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.entity.Feedback;
import com.dongmedicine.entity.Knowledge;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.service.FavoriteService;
import com.dongmedicine.service.FeedbackService;
import com.dongmedicine.service.KnowledgeService;
import com.dongmedicine.service.PopularityAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeMapper, Knowledge> implements KnowledgeService {

    private final FavoriteService favoriteService;
    private final FeedbackService feedbackService;
    private final FileCleanupHelper fileCleanupHelper;
    private final PopularityAsyncService popularityAsyncService;

    @Override
    public List<Knowledge> getAllTherapies() {
        return list(new LambdaQueryWrapper<Knowledge>()
                .eq(Knowledge::getType, "therapy")
                .orderByDesc(Knowledge::getPopularity));
    }

    @Override
    public List<Knowledge> getAllDiseases() {
        return list(new LambdaQueryWrapper<Knowledge>()
                .eq(Knowledge::getType, "disease")
                .orderByDesc(Knowledge::getPopularity));
    }

    @Override
    public List<Knowledge> getByType(String type) {
        if (!StringUtils.hasText(type)) {
            return list();
        }
        return list(new LambdaQueryWrapper<Knowledge>()
                .eq(Knowledge::getType, type)
                .orderByDesc(Knowledge::getPopularity));
    }

    @Override
    public List<Knowledge> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return list();
        }
        String escapedKeyword = PageUtils.escapeLike(keyword);
        return list(new LambdaQueryWrapper<Knowledge>()
                .like(Knowledge::getTitle, escapedKeyword)
                .or().like(Knowledge::getContent, escapedKeyword)
                .orderByDesc(Knowledge::getPopularity));
    }

    @Override
    public Knowledge getDetailById(Integer id) {
        return getById(id);
    }

    @Override
    @Cacheable(value = "searchResults", key = "'knowledges:' + #keyword + ':' + #therapy + ':' + #disease + ':' + #herb + ':' + #sortBy")
    public List<Knowledge> advancedSearch(String keyword, String therapy, String disease, String herb, String sortBy) {
        LambdaQueryWrapper<Knowledge> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(wrapper -> wrapper.like(Knowledge::getTitle, escapedKeyword).or().like(Knowledge::getContent, escapedKeyword));
        }
        if (StringUtils.hasText(therapy)) {
            qw.eq(Knowledge::getTherapyCategory, therapy);
        }
        if (StringUtils.hasText(disease)) {
            qw.eq(Knowledge::getDiseaseCategory, disease);
        }
        if (StringUtils.hasText(herb)) {
            qw.eq(Knowledge::getHerbCategory, herb);
        }
        if ("popularity".equals(sortBy)) {
            qw.orderByDesc(Knowledge::getPopularity);
        } else {
            qw.orderByDesc(Knowledge::getCreatedAt);
        }
        return list(qw);
    }

    @Override
    public Page<Knowledge> pageAll(Integer page, Integer size, String sortBy) {
        Page<Knowledge> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Knowledge> qw = new LambdaQueryWrapper<>();
        if ("popularity".equals(sortBy)) {
            qw.orderByDesc(Knowledge::getPopularity);
        } else {
            qw.orderByDesc(Knowledge::getCreatedAt);
        }
        return page(pageParam, qw);
    }

    @Override
    public Page<Knowledge> advancedSearchPaged(String keyword, String therapy, String disease, String herb, String sortBy, Integer page, Integer size) {
        Page<Knowledge> pageParam = PageUtils.getPage(page, size);
        LambdaQueryWrapper<Knowledge> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String escapedKeyword = PageUtils.escapeLike(keyword);
            qw.and(wrapper -> wrapper.like(Knowledge::getTitle, escapedKeyword).or().like(Knowledge::getContent, escapedKeyword));
        }
        if (StringUtils.hasText(therapy)) {
            qw.eq(Knowledge::getTherapyCategory, therapy);
        }
        if (StringUtils.hasText(disease)) {
            qw.eq(Knowledge::getDiseaseCategory, disease);
        }
        if (StringUtils.hasText(herb)) {
            qw.eq(Knowledge::getHerbCategory, herb);
        }
        if ("popularity".equals(sortBy)) {
            qw.orderByDesc(Knowledge::getPopularity);
        } else {
            qw.orderByDesc(Knowledge::getCreatedAt);
        }
        return page(pageParam, qw);
    }

    @Override
    @Cacheable(value = "knowledges", key = "'detail:' + #id")
    public Knowledge getDetailWithRelated(Integer id) {
        Knowledge knowledge = getById(id);
        if (knowledge != null) {
            popularityAsyncService.incrementKnowledgeViewAndPopularity(id);
        }
        return knowledge;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledges", allEntries = true)
    public void addFavorite(Integer userId, Integer knowledgeId) {
        if (userId == null) {
            throw BusinessException.unauthorized("用户未登录");
        }
        if (knowledgeId == null) {
            throw BusinessException.badRequest("知识ID不能为空");
        }
        if (getById(knowledgeId) == null) {
            throw BusinessException.notFound("知识不存在");
        }
        favoriteService.addFavorite(userId, "knowledge", knowledgeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(Integer knowledgeId, String feedback) {
        if (!StringUtils.hasText(feedback)) {
            throw BusinessException.badRequest("反馈内容不能为空");
        }
        if (feedback.length() > 1000) {
            throw BusinessException.badRequest("反馈内容长度不能超过1000个字符");
        }
        if (knowledgeId == null) {
            throw BusinessException.badRequest("知识ID不能为空");
        }
        Knowledge knowledge = getById(knowledgeId);
        if (knowledge == null) {
            throw BusinessException.notFound("知识不存在");
        }

        Feedback feedbackEntity = new Feedback();
        feedbackEntity.setType("知识纠错");
        feedbackEntity.setTitle("知识反馈：" + knowledge.getTitle());
        feedbackEntity.setContent("【知识ID：" + knowledgeId + "】" + feedback);
        feedbackEntity.setStatus("pending");
        feedbackService.save(feedbackEntity);
    }

    @Override
    public void incrementViewCount(Integer id) {
        try {
            baseMapper.incrementViewCount(id);
            log.debug("Knowledge view count incremented for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment view count for knowledge id: {}", id, e);
        }
    }

    @Override
    @CacheEvict(value = "knowledges", allEntries = true)
    public void clearCache() {
        log.info("Knowledge cache cleared");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledges", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Knowledge knowledge = getById(id);
        if (knowledge == null) {
            return;
        }
        removeById(id);
        fileCleanupHelper.deleteFilesFromJson(knowledge.getVideos());
        fileCleanupHelper.deleteFilesFromJson(knowledge.getDocuments());
        log.info("Deleted knowledge {} with associated files", id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", count());
        stats.put("therapyCategoryCount", baseMapper.countDistinctTherapyCategory());
        stats.put("diseaseCategoryCount", baseMapper.countDistinctDiseaseCategory());
        stats.put("typeCount", baseMapper.countDistinctType());
        stats.put("totalViews", baseMapper.sumViewCount());
        stats.put("totalFavorites", baseMapper.sumFavoriteCount());
        return stats;
    }

    @Override
    public Map<String, List<String>> getFilterOptions() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("therapyCategory", baseMapper.selectDistinctTherapyCategory());
        map.put("diseaseCategory", baseMapper.selectDistinctDiseaseCategory());
        map.put("herbCategory", baseMapper.selectDistinctHerbCategory());
        return map;
    }

    @Override
    public List<Map<String, Object>> topByViewCount(int limit) {
        return baseMapper.topByViewCount(limit);
    }
}
