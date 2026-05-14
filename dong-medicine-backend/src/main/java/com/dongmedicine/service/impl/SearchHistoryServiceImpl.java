package com.dongmedicine.service.impl;

import com.dongmedicine.entity.SearchHistory;
import com.dongmedicine.mapper.SearchHistoryMapper;
import com.dongmedicine.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryMapper searchHistoryMapper;

    @Override
    public void recordSearch(Integer userId, String keyword) {
        try {
            SearchHistory sh = new SearchHistory();
            sh.setUserId(userId);
            sh.setKeyword(keyword);
            searchHistoryMapper.insert(sh);
        } catch (Exception e) {
            log.debug("记录搜索历史失败: {}", e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> topKeywords(int limit) {
        return searchHistoryMapper.topKeywords(limit);
    }
}
