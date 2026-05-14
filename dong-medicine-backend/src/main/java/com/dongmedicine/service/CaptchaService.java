package com.dongmedicine.service;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    CaptchaResult generateCaptcha();

    boolean validateCaptcha(String captchaKey, String captchaCode);

    void validateCaptchaOrThrow(String captchaKey, String captchaCode);

    /**
     * 验证码生成结果
     */
    class CaptchaResult {
        private final String captchaKey;
        private final String captchaImage;

        public CaptchaResult(String captchaKey, String captchaImage) {
            this.captchaKey = captchaKey;
            this.captchaImage = captchaImage;
        }

        public String getCaptchaKey() {
            return captchaKey;
        }

        public String getCaptchaImage() {
            return captchaImage;
        }
    }
}
