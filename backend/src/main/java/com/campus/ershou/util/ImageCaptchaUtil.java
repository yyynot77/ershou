package com.campus.ershou.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 纯 JDK 图片验证码，不依赖 Nashorn（JDK15+ 已移除），兼容 Java 17/21。
 */
public final class ImageCaptchaUtil {
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ImageCaptchaUtil() {}

    public static CaptchaResult generate(int width, int height, int length) {
        String code = randomCode(length);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(240, 253, 250));
            g.fillRect(0, 0, width, height);
            drawNoise(g, width, height);
            drawCode(g, code, width, height);
        } finally {
            g.dispose();
        }
        return new CaptchaResult(code, toDataUri(image));
    }

    private static String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private static void drawNoise(Graphics2D g, int width, int height) {
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(100 + RANDOM.nextInt(100), 150 + RANDOM.nextInt(80), 140 + RANDOM.nextInt(60), 80));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private static void drawCode(Graphics2D g, String code, int width, int height) {
        int charWidth = width / (code.length() + 1);
        for (int i = 0; i < code.length(); i++) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28 + RANDOM.nextInt(6)));
            g.setColor(new Color(13, 120, 110));
            int x = charWidth * (i + 1) - 8;
            int y = height / 2 + 10 + RANDOM.nextInt(6) - 3;
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }
    }

    private static String toDataUri(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    public record CaptchaResult(String code, String imageBase64) {}
}
