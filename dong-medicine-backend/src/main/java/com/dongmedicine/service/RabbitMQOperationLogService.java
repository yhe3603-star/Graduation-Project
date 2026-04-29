package com.dongmedicine.service;

import com.dongmedicine.entity.OperationLog;

public interface RabbitMQOperationLogService {
    
    void saveLogAsync(OperationLog log);
}