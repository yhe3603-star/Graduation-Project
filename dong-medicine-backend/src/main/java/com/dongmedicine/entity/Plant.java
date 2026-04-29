package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plants")
public class Plant extends BaseEntity {
    
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
    private String updateLog;
    private Integer viewCount, favoriteCount, popularity;
}
