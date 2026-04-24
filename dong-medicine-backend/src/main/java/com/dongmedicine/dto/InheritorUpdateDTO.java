package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 传承人更新DTO - 包含id字段，仅允许管理员修改的字段
 */
@Data
public class InheritorUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Integer id;

    @NotBlank(message = "传承人姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50字符")
    private String name;

    @Size(max = 20, message = "级别长度不能超过20字符")
    private String level;

    @Size(max = 5000, message = "个人简介长度不能超过5000字符")
    private String bio;

    @Size(max = 500, message = "擅长领域长度不能超过500字符")
    private String specialties;

    @NotNull(message = "从业年限不能为空")
    private Integer experienceYears;

    /** 视频列表JSON字符串 */
    @Size(max = 5000, message = "视频数据长度不能超过5000字符")
    private String videos;

    /** 图片列表JSON字符串 */
    @Size(max = 5000, message = "图片数据长度不能超过5000字符")
    private String images;

    /** 文档列表JSON字符串 */
    @Size(max = 5000, message = "文档数据长度不能超过5000字符")
    private String documents;

    /** 代表案例JSON字符串 */
    @Size(max = 5000, message = "代表案例数据长度不能超过5000字符")
    private String representativeCases;

    /** 荣誉资质JSON字符串 */
    @Size(max = 5000, message = "荣誉资质数据长度不能超过5000字符")
    private String honors;
}
