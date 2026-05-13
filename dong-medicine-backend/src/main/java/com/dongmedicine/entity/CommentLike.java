package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_likes")
public class CommentLike {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer commentId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
