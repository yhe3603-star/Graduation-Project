package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("plants")
public class Plant {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @NotBlank(message = "中文名称不能为空")
    private String nameCn;
    private String nameDong;
    private String scientificName;
    private String category;
    private String usageWay;
    private String habitat;
    private String efficacy;
    private String story;
    private String images;
    private String videos;
    private String documents;
    private String distribution;
    private String difficulty;
    private String updateLog;
    private LocalDateTime createdAt;
    private Integer viewCount, favoriteCount, popularity;
}
