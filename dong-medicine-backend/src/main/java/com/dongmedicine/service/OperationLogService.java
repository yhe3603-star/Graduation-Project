package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.OperationLog;

import java.util.List;
import java.util.Map;

public interface OperationLogService extends IService<OperationLog> {
    void clearAll();
    List<Map<String, Object>> getTrendLast7Days();
}
