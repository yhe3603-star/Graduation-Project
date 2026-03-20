package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge")
public class Knowledge {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String type;
    private String therapyCategory;
    private String diseaseCategory;
    private String content;
    private String formula;
    private String usageMethod;
    private String steps;
    private String images;
    private String videos;
    private String documents;
    private String relatedPlants;
    private String updateLog;
    private Integer popularity;
    private LocalDateTime createdAt;
    private Integer viewCount, favoriteCount;
}
