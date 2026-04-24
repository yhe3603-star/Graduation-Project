package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学习资源更新DTO - 包含id字段，仅允许管理员修改的字段
 */
@Data
public class ResourceUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Integer id;

    @NotBlank(message = "资源标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @Size(max = 50, message = "分类长度不能超过50字符")
    private String category;

    /** 文件列表JSON字符串 */
    @Size(max = 5000, message = "文件数据长度不能超过5000字符")
    private String files;

    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;
}
