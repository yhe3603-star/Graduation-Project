package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qa")
public class Qa extends BaseEntity {
    private String category;
    
    @NotBlank(message = "问题不能为空")
    private String question;
    private String answer;
    private Integer popularity;
    private Integer viewCount;
    private Integer favoriteCount;
}
