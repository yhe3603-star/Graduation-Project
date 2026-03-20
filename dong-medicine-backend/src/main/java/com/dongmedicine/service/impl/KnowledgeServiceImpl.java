package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.util.FileCleanupHelper;
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
    @Cacheable(value = "knowledges", key = "'search:' + #keyword + ':' + #therapy + ':' + #disease + ':' + #sortBy")
    public List<Knowledge> advancedSearch(String keyword, String therapy, String disease, String sortBy) {
        LambdaQueryWrapper<Knowledge> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(wrapper -> wrapper.like(Knowledge::getTitle, keyword).or().like(Knowledge::getContent, keyword));
        }
        if (StringUtils.hasText(therapy)) {
            qw.eq(Knowledge::getTherapyCategory, therapy);
        }
        if (StringUtils.hasText(disease)) {
            qw.eq(Knowledge::getDiseaseCategory, disease);
        }
        if ("popularity".equals(sortBy)) {
            qw.orderByDesc(Knowledge::getPopularity);
        } else {
            qw.orderByDesc(Knowledge::getCreatedAt);
        }
        return list(qw);
    }

    @Override
    @Cacheable(value = "knowledges", key = "'detail:' + #id")
    public Knowledge getDetailWithRelated(Integer id) {
        Knowledge knowledge = getById(id);
        if (knowledge != null) {
            knowledge.setPopularity(knowledge.getPopularity() != null ? knowledge.getPopularity() + 1 : 1);
            updateById(knowledge);
        }
        return knowledge;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledges", allEntries = true)
    public void addFavorite(Integer userId, Integer knowledgeId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (knowledgeId == null) {
            throw new RuntimeException("知识ID不能为空");
        }
        if (getById(knowledgeId) == null) {
            throw new RuntimeException("知识不存在");
        }
        favoriteService.addFavorite(userId, "knowledge", knowledgeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(Integer knowledgeId, String feedback) {
        if (!StringUtils.hasText(feedback)) {
            throw new RuntimeException("反馈内容不能为空");
        }
        if (feedback.length() > 1000) {
            throw new RuntimeException("反馈内容长度不能超过1000个字符");
        }
        if (knowledgeId == null) {
            throw new RuntimeException("知识ID不能为空");
        }
        Knowledge knowledge = getById(knowledgeId);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }

        Feedback feedbackEntity = new Feedback();
        feedbackEntity.setType("知识纠错");
        feedbackEntity.setTitle("知识反馈：" + knowledge.getTitle());
        feedbackEntity.setContent("【知识ID：" + knowledgeId + "】" + feedback);
        feedbackEntity.setStatus("pending");
        feedbackEntity.setCreateTime(LocalDateTime.now());
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
