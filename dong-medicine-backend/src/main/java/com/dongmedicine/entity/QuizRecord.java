package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz_record")
public class QuizRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
