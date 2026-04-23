package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
