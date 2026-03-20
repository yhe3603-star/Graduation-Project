package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("plant_game_record")
public class PlantGameRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String difficulty;
    private Integer score;
    private Integer correctCount;
    private Integer totalCount;
    @TableField("created_at")
    private LocalDateTime createTime;
}
