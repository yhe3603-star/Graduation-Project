package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 药用植物创建DTO - 仅包含管理员可提交的字段，防止字段篡改
 */
@Data
public class PlantCreateDTO {

    @NotBlank(message = "中文名称不能为空")
    @Size(max = 100, message = "中文名称长度不能超过100字符")
    private String nameCn;

    @Size(max = 100, message = "侗语名长度不能超过100字符")
    private String nameDong;

    @Size(max = 200, message = "学名长度不能超过200字符")
    private String scientificName;

    @Size(max = 50, message = "分类长度不能超过50字符")
    private String category;

    @Size(max = 50, message = "用法长度不能超过50字符")
    private String usageWay;

    @Size(max = 500, message = "生长环境长度不能超过500字符")
    private String habitat;

    @Size(max = 2000, message = "功效描述长度不能超过2000字符")
    private String efficacy;

    @Size(max = 5000, message = "故事长度不能超过5000字符")
    private String story;

    /** 图片列表JSON字符串 */
    @Size(max = 5000, message = "图片数据长度不能超过5000字符")
    private String images;

    /** 视频列表JSON字符串 */
    @Size(max = 5000, message = "视频数据长度不能超过5000字符")
    private String videos;

    /** 文档列表JSON字符串 */
    @Size(max = 5000, message = "文档数据长度不能超过5000字符")
    private String documents;

    @Size(max = 20, message = "难度长度不能超过20字符")
    private String difficulty;
}
