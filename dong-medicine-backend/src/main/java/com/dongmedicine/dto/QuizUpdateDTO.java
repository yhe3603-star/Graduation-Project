package com.dongmedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 测验题目更新DTO - 包含id字段，仅允许管理员修改的字段
 */
@Data
public class QuizUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Integer id;

    @NotBlank(message = "题目不能为空")
    @Size(max = 500, message = "题目长度不能超过500字符")
    private String question;

    /** 选项列表，将转换为JSON字符串存储 */
    private List<String> options;

    @NotBlank(message = "答案不能为空")
    @Size(max = 10, message = "答案长度不能超过10字符")
    private String answer;

    @Size(max = 50, message = "分类长度不能超过50字符")
    private String category;

    @Size(max = 10, message = "正确答案长度不能超过10字符")
    private String correctAnswer;

    @Size(max = 1000, message = "解析长度不能超过1000字符")
    private String explanation;
}
