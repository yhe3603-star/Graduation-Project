package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlantDTO {
    private Integer id;
    
    @NotBlank(message = "中文名不能为空")
    @Size(max = 100, message = "中文名长度不能超过100字符")
    private String nameCn;
    
    @Size(max = 100, message = "侗语名长度不能超过100字符")
    private String nameDong;
    
    @Size(max = 50, message = "分类长度不能超过50字符")
    private String category;
    
    @Size(max = 50, message = "用法长度不能超过50字符")
    private String usageWay;
    
    @Size(max = 2000, message = "功效描述长度不能超过2000字符")
    private String efficacy;
    
    @Size(max = 5000, message = "故事长度不能超过5000字符")
    private String story;
    
    private String images;
    private String videos;
    private String documents;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;

    public static PlantDTO fromEntity(com.dongmedicine.entity.Plant plant) {
        if (plant == null) return null;
        PlantDTO dto = new PlantDTO();
        dto.setId(plant.getId());
        dto.setNameCn(plant.getNameCn());
        dto.setNameDong(plant.getNameDong());
        dto.setCategory(plant.getCategory());
        dto.setUsageWay(plant.getUsageWay());
        dto.setEfficacy(plant.getEfficacy());
        dto.setStory(plant.getStory());
        dto.setImages(plant.getImages());
        dto.setVideos(plant.getVideos());
        dto.setDocuments(plant.getDocuments());
        dto.setViewCount(plant.getViewCount());
        dto.setFavoriteCount(plant.getFavoriteCount());
        dto.setCreatedAt(plant.getCreatedAt());
        return dto;
    }
}
