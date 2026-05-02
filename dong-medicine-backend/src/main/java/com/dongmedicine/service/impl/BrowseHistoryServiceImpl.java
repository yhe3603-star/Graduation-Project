package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.BrowseHistory;
import com.dongmedicine.mapper.BrowseHistoryMapper;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Integer userId, String targetType, Integer targetId) {
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
    }

    @Override
    public List<Map<String, Object>> getMyHistory(Integer userId, int limit) {
        try {
            List<BrowseHistory> all = list(new LambdaQueryWrapper<BrowseHistory>()
                    .eq(BrowseHistory::getUserId, userId)
                    .orderByDesc(BrowseHistory::getCreatedAt));

            Map<String, BrowseHistory> deduped = new LinkedHashMap<>();
            for (BrowseHistory h : all) {
                String key = h.getTargetType() + ":" + h.getTargetId();
                deduped.putIfAbsent(key, h);
            }

            return deduped.values().stream()
                    .limit(limit)
                    .map(h -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", h.getId());
                        m.put("userId", h.getUserId());
                        m.put("targetType", h.getTargetType());
                        m.put("targetId", h.getTargetId());
                        m.put("browseTime", h.getCreatedAt());
                        return m;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询浏览历史失败: userId={}, error={}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
