package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学习资源创建DTO - 仅包含管理员可提交的字段，防止字段篡改
 */
@Data
public class ResourceCreateDTO {

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
