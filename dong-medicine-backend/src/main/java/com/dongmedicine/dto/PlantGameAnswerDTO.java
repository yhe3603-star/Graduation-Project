package com.dongmedicine.dto;

import lombok.Data;

/**
 * 植物游戏单题答案DTO
 * 用于服务端验证玩家提交的每一道题的答案
 */
@Data
public class PlantGameAnswerDTO {
    /**
     * 药用植物ID
     */
    private Integer plantId;

    /**
     * 玩家提交的答案（植物中文名称）
     */
    private String userAnswer;
}
