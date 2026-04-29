package com.dongmedicine.dto;

import lombok.Data;

import java.util.List;

/**
 * 植物游戏提交DTO
 * 支持两种提交方式：
 * 1. 提交答案列表（answers），由服务端验证并计算得分（推荐）
 * 2. 直接提交 correctCount/totalCount（兼容旧版前端，服务端会做合理性校验）
 */
@Data
public class PlantGameSubmitDTO {
    /**
     * 难度等级
     */
    private String difficulty;

    /**
     * 玩家提交的答案列表，用于服务端验证
     */
    private List<PlantGameAnswerDTO> answers;

    /**
     * 正确答题数（兼容旧版前端，服务端优先使用 answers 计算）
     */
    private Integer correctCount;

    /**
     * 总答题数（兼容旧版前端，服务端优先使用 answers 计算）
     */
    private Integer totalCount;
}
