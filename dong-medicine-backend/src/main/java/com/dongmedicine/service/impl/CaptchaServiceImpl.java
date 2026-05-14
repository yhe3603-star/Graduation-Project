package com.dongmedicine.service.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.dongmedicine.common.exception.BusinessException;
import com.dongmedicine.service.CaptchaService;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 5;
    private static final String CHAR_POOL = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int EXPIRE_MINUTES = 5;
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final int INTERFERENCE_LINES = 8;
    private static final int NOISE_DOTS = 50;

    private final SecureRandom random = new SecureRandom();

    @Override
    public CaptchaResult generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        String captchaCode = generateCode();
        BufferedImage image = generateImage(captchaCode);
        String base64Image = imageToBase64(image);
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        redisTemplate.opsForValue().set(redisKey, captchaCode, EXPIRE_MINUTES, TimeUnit.MINUTES);
        return new CaptchaResult(captchaKey, base64Image);
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        if (storedCode != null) {
            redisTemplate.delete(redisKey);
        }
        return storedCode != null && storedCode.equalsIgnoreCase(captchaCode);
    }

    @Override
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

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return code.toString();
    }

    private BufferedImage generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        drawInterferenceLines(g);
        drawNoiseDots(g);
        drawCode(g, code);
        g.dispose();
        return image;
    }

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

    private void drawNoiseDots(Graphics2D g) {
        for (int i = 0; i < NOISE_DOTS; i++) {
            g.setColor(getRandomColor(100, 200));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
    }

    private void drawCode(Graphics2D g, String code) {
        int charWidth = WIDTH / (CODE_LENGTH + 1);
        int baseY = HEIGHT * 3 / 4;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor(20, 120));
            int fontSize = 20 + random.nextInt(8);
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g.setFont(font);
            int x = charWidth * (i + 1) - charWidth / 2 - fontSize / 3;
            double angle = (random.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, x + fontSize / 2, baseY);
            g.drawString(String.valueOf(code.charAt(i)), x, baseY);
            g.rotate(-angle, x + fontSize / 2, baseY);
        }
    }

    private Color getRandomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }
}
