package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDTO {
    private Integer id;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;
    
    @Size(max = 50, message = "类型长度不能超过50字符")
    private String type;
    
    @Size(max = 50, message = "疗法分类长度不能超过50字符")
    private String therapyCategory;
    
    @Size(max = 50, message = "疾病分类长度不能超过50字符")
    private String diseaseCategory;
    
    @Size(max = 10000, message = "内容长度不能超过10000字符")
    private String content;
    
    private String images;
    private String videos;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;

    public static KnowledgeDTO fromEntity(com.dongmedicine.entity.Knowledge knowledge) {
        if (knowledge == null) return null;
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setId(knowledge.getId());
        dto.setTitle(knowledge.getTitle());
        dto.setType(knowledge.getType());
        dto.setTherapyCategory(knowledge.getTherapyCategory());
        dto.setDiseaseCategory(knowledge.getDiseaseCategory());
        dto.setContent(knowledge.getContent());
        dto.setImages(knowledge.getImages());
        dto.setVideos(knowledge.getVideos());
        dto.setViewCount(knowledge.getViewCount());
        dto.setFavoriteCount(knowledge.getFavoriteCount());
        dto.setCreatedAt(knowledge.getCreatedAt());
        return dto;
    }
}
