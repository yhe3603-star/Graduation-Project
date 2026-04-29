package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {
    private Integer userId;
    private String username;
    private String module;
    private String operation;
    private String type;
    private String method;
    private String params;
    private String ip;
    private Integer duration;
    private Boolean success;
    private String errorMsg;
}
