package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resources")
public class Resource extends BaseEntity {
    
    @NotBlank(message = "资源标题不能为空")
    private String title;
    private String category;
    private String files;
    private String description;
    private String updateLog;
    private Integer viewCount, downloadCount, favoriteCount, popularity;
}
