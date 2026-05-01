package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.BrowseHistory;

import java.util.List;
import java.util.Map;

public interface BrowseHistoryService extends IService<BrowseHistory> {

    void record(Integer userId, String targetType, Integer targetId);

    List<Map<String, Object>> getMyHistory(Integer userId, int limit);
}
