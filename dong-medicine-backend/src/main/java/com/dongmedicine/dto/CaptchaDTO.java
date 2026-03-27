package com.dongmedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应DTO
 * 包含验证码key和Base64编码的图片
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {

    /**
     * 验证码唯一标识key
     * 用于后续验证时提交
     */
    private String captchaKey;

    /**
     * Base64编码的验证码图片
     * 格式: data:image/png;base64,xxxxx
     * 可直接在img标签的src属性中使用
     */
    private String captchaImage;
}
