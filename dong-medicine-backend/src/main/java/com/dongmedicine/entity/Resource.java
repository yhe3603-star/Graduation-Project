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
@TableName("resources")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @NotBlank(message = "资源标题不能为空")
    private String title;
    private String category;
    private String files;
    private String description;
    private String updateLog;
    private Integer viewCount, downloadCount, favoriteCount, popularity;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
