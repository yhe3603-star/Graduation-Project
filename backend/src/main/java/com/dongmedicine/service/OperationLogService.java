package com.dongmedicine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dongmedicine.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    void clearAll();
}
