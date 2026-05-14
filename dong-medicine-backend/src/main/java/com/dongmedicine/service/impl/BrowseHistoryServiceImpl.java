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
            List<BrowseHistory> limited = new ArrayList<>(deduped.values()).subList(0, Math.min(effectiveLimit, deduped.size()));

            Map<String, Map<Integer, Map<String, String>>> dataCache = batchFetchTitleAndDescription(limited);

            return limited.stream()
                    .map(h -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", h.getId());
                        m.put("userId", h.getUserId());
                        m.put("targetType", h.getTargetType());
                        m.put("targetId", h.getTargetId());
                        m.put("browseTime", h.getCreatedAt());

                        Map<Integer, Map<String, String>> idMap = dataCache.getOrDefault(h.getTargetType(), Collections.emptyMap());
                        Map<String, String> titleDesc = idMap.get(h.getTargetId());
                        m.put("title", titleDesc != null ? titleDesc.get("title") : null);
                        m.put("description", titleDesc != null ? titleDesc.get("description") : null);

                        return m;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询浏览历史失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, Map<Integer, Map<String, String>>> batchFetchTitleAndDescription(List<BrowseHistory> items) {
        Map<String, List<Integer>> groupedIds = items.stream()
                .filter(h -> h.getTargetType() != null && h.getTargetId() != null)
                .collect(Collectors.groupingBy(
                        BrowseHistory::getTargetType,
                        Collectors.mapping(BrowseHistory::getTargetId, Collectors.toList())
                ));

        Map<String, Map<Integer, Map<String, String>>> result = new HashMap<>();

        groupedIds.forEach((type, ids) -> {
            Map<Integer, Map<String, String>> typeMap = new HashMap<>();
            try {
                switch (type) {
                    case TargetType.Constants.PLANT -> {
                        List<Plant> plants = plantMapper.selectBatchIds(ids);
                        for (Plant p : plants) {
                            typeMap.put(p.getId(), createTitleDesc(p.getNameCn(), p.getEfficacy()));
                        }
                    }
                    case TargetType.Constants.KNOWLEDGE -> {
                        List<Knowledge> list = knowledgeMapper.selectBatchIds(ids);
                        for (Knowledge k : list) {
                            typeMap.put(k.getId(), createTitleDesc(k.getTitle(), k.getContent()));
                        }
                    }
                    case TargetType.Constants.INHERITOR -> {
                        List<Inheritor> list = inheritorMapper.selectBatchIds(ids);
                        for (Inheritor i : list) {
                            typeMap.put(i.getId(), createTitleDesc(i.getName(), i.getBio()));
                        }
                    }
                    case TargetType.Constants.RESOURCE -> {
                        List<Resource> list = resourceMapper.selectBatchIds(ids);
                        for (Resource r : list) {
                            typeMap.put(r.getId(), createTitleDesc(r.getTitle(), r.getDescription()));
                        }
                    }
                    case TargetType.Constants.QA -> {
                        List<Qa> list = qaMapper.selectBatchIds(ids);
                        for (Qa q : list) {
                            typeMap.put(q.getId(), createTitleDesc(q.getQuestion(), q.getAnswer()));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("批量获取{}详情失败: ids={}, error={}", type, ids, e.getMessage());
            }
            result.put(type, typeMap);
        });

        return result;
    }

    private Map<String, String> createTitleDesc(String title, String description) {
        Map<String, String> m = new HashMap<>();
        m.put("title", title);
        m.put("description", description);
        return m;
    }
}
