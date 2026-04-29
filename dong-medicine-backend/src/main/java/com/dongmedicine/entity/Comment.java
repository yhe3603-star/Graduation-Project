package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comments")
public class Comment extends BaseEntity {
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
}
