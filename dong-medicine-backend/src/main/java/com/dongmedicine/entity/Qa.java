package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa")
public class Qa {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String category;
    private String question;
    private String answer;
    private Integer popularity;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
}
