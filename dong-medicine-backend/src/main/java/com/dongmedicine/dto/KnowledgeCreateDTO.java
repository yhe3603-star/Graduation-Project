package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识条目创建DTO - 仅包含管理员可提交的字段，防止字段篡改
 */
@Data
public class KnowledgeCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @Size(max = 50, message = "类型长度不能超过50字符")
    private String type;

    @Size(max = 50, message = "疗法分类长度不能超过50字符")
    private String therapyCategory;

    @Size(max = 50, message = "疾病分类长度不能超过50字符")
    private String diseaseCategory;

    @Size(max = 50, message = "药材分类长度不能超过50字符")
    private String herbCategory;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000字符")
    private String content;

    @Size(max = 2000, message = "药方长度不能超过2000字符")
    private String formula;

    @Size(max = 500, message = "用法长度不能超过500字符")
    private String usageMethod;

    /** 步骤JSON字符串 */
    @Size(max = 5000, message = "步骤数据长度不能超过5000字符")
    private String steps;

    /** 图片列表JSON字符串 */
    @Size(max = 5000, message = "图片数据长度不能超过5000字符")
    private String images;

    /** 视频列表JSON字符串 */
    @Size(max = 5000, message = "视频数据长度不能超过5000字符")
    private String videos;

    /** 文档列表JSON字符串 */
    @Size(max = 5000, message = "文档数据长度不能超过5000字符")
    private String documents;

    /** 关联植物JSON字符串 */
    @Size(max = 2000, message = "关联植物数据长度不能超过2000字符")
    private String relatedPlants;
}
