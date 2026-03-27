package com.dongmedicine.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.dongmedicine.common.exception.BusinessException;

/**
 * 验证码服务
 * 生成4位数字验证码图片，存储到Redis中，有效期5分钟
 */
@Service
public class CaptchaService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 验证码图片宽度
    private static final int WIDTH = 120;
    // 验证码图片高度
    private static final int HEIGHT = 40;
    // 验证码字符数
    private static final int CODE_LENGTH = 4;
    // 验证码有效期（分钟）
    private static final int EXPIRE_MINUTES = 5;
    // Redis key前缀
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    // 干扰线数量
    private static final int INTERFERENCE_LINES = 8;
    // 噪点数量
    private static final int NOISE_DOTS = 50;

    private final Random random = new Random();

    /**
     * 生成验证码
     * @return 包含验证码key和Base64编码图片的数据
     */
    public CaptchaResult generateCaptcha() {
        // 生成唯一key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        
        // 生成4位数字验证码
        String captchaCode = generateCode();
        
        // 生成验证码图片
        BufferedImage image = generateImage(captchaCode);
        
        // 转换为Base64
        String base64Image = imageToBase64(image);
        
        // 存储到Redis，有效期5分钟
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        redisTemplate.opsForValue().set(redisKey, captchaCode, EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return new CaptchaResult(captchaKey, base64Image);
    }

    /**
     * 验证验证码
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @return 验证是否通过
     */
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        // 验证后立即删除，防止重复使用
        if (storedCode != null) {
            redisTemplate.delete(redisKey);
        }
        
        // 验证码不区分大小写（数字验证码无需区分）
        return storedCode != null && storedCode.equals(captchaCode);
    }

    /**
     * 验证验证码（带异常抛出）
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @throws BusinessException 验证失败时抛出异常
     */
    public void validateCaptchaOrThrow(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.isBlank()) {
            throw BusinessException.badRequest("验证码key不能为空");
        }
        if (captchaCode == null || captchaCode.isBlank()) {
            throw BusinessException.badRequest("请输入验证码");
        }
        if (!validateCaptcha(captchaKey, captchaCode)) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }
    }

    /**
     * 生成4位数字验证码
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片
     */
    private BufferedImage generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景色（浅色背景）
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制边框
        g.setColor(new Color(200, 200, 200));
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        
        // 绘制干扰线
        drawInterferenceLines(g);
        
        // 绘制噪点
        drawNoiseDots(g);
        
        // 绘制验证码字符
        drawCode(g, code);
        
        g.dispose();
        return image;
    }

    /**
     * 绘制干扰线
     */
    private void drawInterferenceLines(Graphics2D g) {
        for (int i = 0; i < INTERFERENCE_LINES; i++) {
            g.setColor(getRandomColor(100, 200));
            g.setStroke(new BasicStroke(1.5f));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制噪点
     */
    private void drawNoiseDots(Graphics2D g) {
        for (int i = 0; i < NOISE_DOTS; i++) {
            g.setColor(getRandomColor(100, 200));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
    }

    /**
     * 绘制验证码字符
     */
    private void drawCode(Graphics2D g, String code) {
        int charWidth = WIDTH / (CODE_LENGTH + 1);
        int baseY = HEIGHT * 3 / 4;
        
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色（深色）
            g.setColor(getRandomColor(20, 120));
            
            // 随机字体大小
            int fontSize = 20 + random.nextInt(8);
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g.setFont(font);
            
            // 计算字符位置
            int x = charWidth * (i + 1) - charWidth / 2 - fontSize / 3;
            
            // 随机倾斜角度
            double angle = (random.nextDouble() - 0.5) * 0.4;
            
            // 绘制字符
            g.rotate(angle, x + fontSize / 2, baseY);
            g.drawString(String.valueOf(code.charAt(i)), x, baseY);
            g.rotate(-angle, x + fontSize / 2, baseY);
        }
    }

    /**
     * 获取随机颜色
     */
    private Color getRandomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 图片转Base64
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    /**
     * 验证码生成结果
     */
    public static class CaptchaResult {
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
