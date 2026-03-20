package com.dongmedicine.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dongmedicine.entity.OperationLog;
import com.dongmedicine.mapper.OperationLogMapper;
import com.dongmedicine.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    
    @Override
    public void clearAll() {
        remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }
}
