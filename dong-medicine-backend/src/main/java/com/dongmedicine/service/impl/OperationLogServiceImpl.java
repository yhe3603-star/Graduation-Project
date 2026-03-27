package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mapper.OperationLogMapper;
import com.dongmedicine.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    
    private static final int MAX_LOG_COUNT = 500;
    
    @Override
    public boolean save(OperationLog log) {
        boolean result = super.save(log);
        if (result) {
            cleanOldLogs();
        }
        return result;
    }
    
    private void cleanOldLogs() {
        long count = count();
        if (count > MAX_LOG_COUNT) {
            QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
            wrapper.orderByAsc("id");
            wrapper.last("LIMIT " + (count - MAX_LOG_COUNT));
            List<OperationLog> oldLogs = list(wrapper);
            if (!oldLogs.isEmpty()) {
                removeByIds(oldLogs.stream().map(OperationLog::getId).toList());
            }
        }
    }
    
    @Override
    public void clearAll() {
        remove(new QueryWrapper<>());
    }
}
