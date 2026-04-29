package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String userName;
    private String type;
    private String title;
    private String content;
    private String contact;
    private String status;
    private String reply;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
