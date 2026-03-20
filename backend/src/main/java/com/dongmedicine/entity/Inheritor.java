package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inheritors")
public class Inheritor {
    @TableId(type = IdType.AUTO)
    private Integer id;
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
    private LocalDateTime createdAt;
}
