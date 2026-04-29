package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge")
public class Knowledge extends BaseEntity {
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String type;
    private String therapyCategory;
    private String diseaseCategory;
    private String herbCategory;
    
    @NotBlank(message = "内容不能为空")
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
    private Integer viewCount, favoriteCount;
}
