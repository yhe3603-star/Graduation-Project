package com.dongmedicine.service;

import java.util.List;
import java.util.Map;

public interface SearchHistoryService {

    void recordSearch(Integer userId, String keyword);

    List<Map<String, Object>> topKeywords(int limit);
}
