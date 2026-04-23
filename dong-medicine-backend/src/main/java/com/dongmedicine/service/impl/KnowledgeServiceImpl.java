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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeMapper, Knowledge> implements KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeServiceImpl.class);

    @Autowired
    private KnowledgeMapper knowledgeMapper;
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private FileCleanupHelper fileCleanupHelper;

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
    @Cacheable(value = "knowledges", key = "'search:' + #keyword + ':' + #therapy + ':' + #disease + ':' + #herb + ':' + #sortBy")
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
            incrementPopularityAsync(id);
        }
        return knowledge;
    }

    @Async("popularityExecutor")
    public void incrementPopularityAsync(Integer id) {
        try {
            knowledgeMapper.incrementPopularity(id);
            log.debug("Knowledge popularity incremented async for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to increment popularity for knowledge id: {}", id, e);
        }
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
            knowledgeMapper.incrementViewCount(id);
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
    @CacheEvict(value = "knowledges", allEntries = true)
    public void deleteWithFiles(Integer id) {
        Knowledge knowledge = getById(id);
        if (knowledge == null) {
            return;
        }
        fileCleanupHelper.deleteFilesFromJson(knowledge.getVideos());
        fileCleanupHelper.deleteFilesFromJson(knowledge.getDocuments());
        removeById(id);
        log.info("Deleted knowledge {} with associated files", id);
    }
}
