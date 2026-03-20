package com.dongmedicine.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
