package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("search_history")
public class SearchHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private String keyword;
    private LocalDateTime createdAt;
}
