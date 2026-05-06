package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.common.constant.TargetType;
import com.dongmedicine.entity.*;
import com.dongmedicine.mapper.*;
import com.dongmedicine.service.BrowseHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService {

    private final PlantMapper plantMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final InheritorMapper inheritorMapper;
    private final ResourceMapper resourceMapper;
    private final QaMapper qaMapper;

    private static final int MAX_HISTORY_COUNT = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Integer userId, String targetType, Integer targetId) {
        deleteExpiredHistory(userId);
        
        BrowseHistory existing = getOne(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getTargetType, targetType)
                .eq(BrowseHistory::getTargetId, targetId));
        if (existing != null) {
            update(new LambdaUpdateWrapper<BrowseHistory>()
                    .eq(BrowseHistory::getId, existing.getId())
                    .set(BrowseHistory::getCreatedAt, LocalDateTime.now()));
        } else {
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setTargetType(targetType);
            history.setTargetId(targetId);
            save(history);
        }
        
        trimHistoryToMaxCount(userId);
    }

    private void deleteExpiredHistory(Integer userId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        remove(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .lt(BrowseHistory::getCreatedAt, oneMonthAgo));
    }

    private void trimHistoryToMaxCount(Integer userId) {
        List<BrowseHistory> all = list(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .orderByDesc(BrowseHistory::getCreatedAt));
        
        if (all.size() > MAX_HISTORY_COUNT) {
            List<Long> idsToDelete = all.stream()
                    .skip(MAX_HISTORY_COUNT)
                    .map(BrowseHistory::getId)
                    .collect(Collectors.toList());
            removeByIds(idsToDelete);
        }
    }

    @Override
    public List<Map<String, Object>> getMyHistory(Integer userId, int limit) {
        try {
            deleteExpiredHistory(userId);
            
            List<BrowseHistory> all = list(new LambdaQueryWrapper<BrowseHistory>()
                    .eq(BrowseHistory::getUserId, userId)
                    .orderByDesc(BrowseHistory::getCreatedAt));

            Map<String, BrowseHistory> deduped = new LinkedHashMap<>();
            for (BrowseHistory h : all) {
                String key = h.getTargetType() + ":" + h.getTargetId();
                deduped.putIfAbsent(key, h);
            }

            int effectiveLimit = Math.min(limit, MAX_HISTORY_COUNT);
            
            return deduped.values().stream()
                    .limit(effectiveLimit)
                    .map(h -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", h.getId());
                        m.put("userId", h.getUserId());
                        m.put("targetType", h.getTargetType());
                        m.put("targetId", h.getTargetId());
                        m.put("browseTime", h.getCreatedAt());
                        
                        Map<String, String> titleDesc = getTitleAndDescription(h.getTargetType(), h.getTargetId());
                        m.put("title", titleDesc.get("title"));
                        m.put("description", titleDesc.get("description"));
                        
                        return m;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询浏览历史失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, String> getTitleAndDescription(String targetType, Integer targetId) {
        Map<String, String> result = new HashMap<>();
        result.put("title", null);
        result.put("description", null);
        
        if (targetType == null || targetId == null) {
            return result;
        }
        
        try {
            switch (targetType) {
                case TargetType.Constants.PLANT:
                    Plant plant = plantMapper.selectById(targetId);
                    if (plant != null) {
                        result.put("title", plant.getNameCn());
                        result.put("description", plant.getEfficacy());
                    }
                    break;
                case TargetType.Constants.KNOWLEDGE:
                    Knowledge knowledge = knowledgeMapper.selectById(targetId);
                    if (knowledge != null) {
                        result.put("title", knowledge.getTitle());
                        result.put("description", knowledge.getContent());
                    }
                    break;
                case TargetType.Constants.INHERITOR:
                    Inheritor inheritor = inheritorMapper.selectById(targetId);
                    if (inheritor != null) {
                        result.put("title", inheritor.getName());
                        result.put("description", inheritor.getBio());
                    }
                    break;
                case TargetType.Constants.RESOURCE:
                    Resource resource = resourceMapper.selectById(targetId);
                    if (resource != null) {
                        result.put("title", resource.getTitle());
                        result.put("description", resource.getDescription());
                    }
                    break;
                case TargetType.Constants.QA:
                    Qa qa = qaMapper.selectById(targetId);
                    if (qa != null) {
                        result.put("title", qa.getQuestion());
                        result.put("description", qa.getAnswer());
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("获取{}详情失败: targetId={}, error={}", targetType, targetId, e.getMessage());
        }
        
        return result;
    }
}
