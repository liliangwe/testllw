package com.example;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

/**
 * HelloWord 图片生成器。
 * <p>
 * 使用 JDK 内置的 {@link BufferedImage} / {@link Graphics2D} 渲染文字，
 * 并通过 {@link ImageIO} 编码输出 PNG 文件，不依赖任何第三方库。
 * 无共享可变状态，单次调用失败不影响后续调用。
 * </p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class ImageGenerator {

    /** 默认文字内容 */
    private static final String DEFAULT_TEXT = "Hello World";

    /** 默认图片宽度（像素） */
    private static final int DEFAULT_WIDTH = 800;

    /** 默认图片高度（像素） */
    private static final int DEFAULT_HEIGHT = 400;

    /** 默认背景色 */
    private static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFF";

    /** 默认文字色 */
    private static final String DEFAULT_TEXT_COLOR = "#000000";

    /** 默认输出文件路径 */
    private static final String DEFAULT_OUTPUT_PATH = "hello-world.png";

    /** 输出图片格式 */
    private static final String OUTPUT_FORMAT = "png";

    /** 输出图片类型（对应 PNG 的颜色模型） */
    private static final int OUTPUT_IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

    /** 渲染字体名称（系统默认无衬线字体，规避字体缺失） */
    private static final String FONT_FAMILY = Font.SANS_SERIF;

    /** 渲染字号 */
    private static final int DEFAULT_FONT_SIZE = 36;

    /** 生成 HelloWord 图片，缺省参数自动填充默认值。 */
    public void generateDefault() {
        generate(null);
    }

    /**
     * 依据入参渲染并输出一张 PNG 图片。
     *
     * @param request 生成入参，可为 null（使用全部默认值），空字段自动填充默认值
     */
    public void generate(ImageGenRequest request) {
        ImageGenRequest normalized = normalize(request);
        validate(normalized);

        BufferedImage image = render(normalized);
        write(image, normalized.getOutputPath());
    }

    private ImageGenRequest normalize(ImageGenRequest request) {
        ImageGenRequest source = request == null ? new ImageGenRequest() : request;
        ImageGenRequest normalized = new ImageGenRequest();
        normalized.setText(isBlank(source.getText()) ? DEFAULT_TEXT : source.getText());
        normalized.setWidth(source.getWidth() == null ? DEFAULT_WIDTH : source.getWidth());
        normalized.setHeight(source.getHeight() == null ? DEFAULT_HEIGHT : source.getHeight());
        normalized.setBackgroundColor(
            isBlank(source.getBackgroundColor()) ? DEFAULT_BACKGROUND_COLOR : source.getBackgroundColor());
        normalized.setTextColor(isBlank(source.getTextColor()) ? DEFAULT_TEXT_COLOR : source.getTextColor());
        normalized.setOutputPath(isBlank(source.getOutputPath()) ? DEFAULT_OUTPUT_PATH : source.getOutputPath());
        return normalized;
    }

    private void validate(ImageGenRequest request) {
        Integer width = request.getWidth();
        Integer height = request.getHeight();
        if (width == null || height == null || width <= 0 || height <= 0) {
            throw new ImageGenException(
                ImageGenErrorCodeEnum.INVALID_PARAMETER, "宽、高必须为正整数, width=" + width + ", height=" + height);
        }
    }

    private BufferedImage render(ImageGenRequest request) {
        Color backgroundColor = parseColor(request.getBackgroundColor());
        Color textColor = parseColor(request.getTextColor());

        BufferedImage image = new BufferedImage(request.getWidth(), request.getHeight(), OUTPUT_IMAGE_TYPE);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBackground(graphics, backgroundColor, request.getWidth(), request.getHeight());
            drawCenteredText(graphics, textColor, request.getText(), request.getWidth(), request.getHeight());
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void drawBackground(Graphics2D graphics, Color color, int width, int height) {
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
    }

    private void drawCenteredText(Graphics2D graphics, Color color, String text, int width, int height) {
        Font font = new Font(FONT_FAMILY, Font.PLAIN, DEFAULT_FONT_SIZE);
        FontMetrics metrics = graphics.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + metrics.getAscent();

        graphics.setFont(font);
        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }

    private Color parseColor(String colorHex) {
        try {
            return Color.decode(colorHex);
        } catch (NumberFormatException e) {
            throw new ImageGenException(
                ImageGenErrorCodeEnum.INVALID_PARAMETER, "颜色参数非法: " + colorHex, e);
        }
    }

    private void write(BufferedImage image, String outputPath) {
        Path target = Paths.get(outputPath).toAbsolutePath();
        Path parent = target.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new ImageGenException(
                    ImageGenErrorCodeEnum.OUTPUT_DIR_NOT_WRITABLE, "输出目录不可写: " + outputPath, e);
            }
        }

        File tmpFile = new File(target + ".tmp");
        try {
            boolean written = ImageIO.write(image, OUTPUT_FORMAT, tmpFile);
            if (!written) {
                throw new ImageGenException(
                    ImageGenErrorCodeEnum.ENCODE_WRITE_FAILED, "无可用编码器写入图片: " + outputPath);
            }
            Files.move(tmpFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(tmpFile);
            throw new ImageGenException(
                ImageGenErrorCodeEnum.ENCODE_WRITE_FAILED, "图片编码或写盘失败: " + outputPath, e);
        }
    }

    private void deleteQuietly(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}