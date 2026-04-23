package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comments")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String username;
    private String targetType;
    private Integer targetId;
    private String content;
    private Integer replyToId;
    private Integer replyToUserId;
    private String replyToUsername;
    private Integer likes;
    private Integer replyCount;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
