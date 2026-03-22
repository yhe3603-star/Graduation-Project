package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InheritorDTO {
    private Integer id;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50字符")
    private String name;
    
    @NotBlank(message = "级别不能为空")
    @Pattern(regexp = "国家级|省级|市级|县级|自治区级", message = "级别格式不正确")
    private String level;
    
    @Size(max = 5000, message = "个人简介长度不能超过5000字符")
    private String bio;
    
    private String images;
    private String videos;
    private Integer viewCount;
    private Integer favoriteCount;

    public static InheritorDTO fromEntity(com.dongmedicine.entity.Inheritor inheritor) {
        if (inheritor == null) return null;
        InheritorDTO dto = new InheritorDTO();
        dto.setId(inheritor.getId());
        dto.setName(inheritor.getName());
        dto.setLevel(inheritor.getLevel());
        dto.setBio(inheritor.getBio());
        dto.setImages(inheritor.getImages());
        dto.setVideos(inheritor.getVideos());
        dto.setViewCount(inheritor.getViewCount());
        dto.setFavoriteCount(inheritor.getFavoriteCount());
        return dto;
    }
}
