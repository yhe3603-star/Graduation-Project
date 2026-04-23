package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge")
public class Knowledge {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Integer viewCount, favoriteCount;
}
