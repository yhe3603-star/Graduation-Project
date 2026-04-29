package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inheritors")
public class Inheritor extends BaseEntity {
    
    @NotBlank(message = "传承人姓名不能为空")
    private String name;
    private String level;
    private String bio;
    private String specialties;
    private Integer experienceYears;
    private String videos, images, documents;
    private String representativeCases;
    private String honors;
    private String updateLog;
    private Integer viewCount, favoriteCount, popularity;
}
