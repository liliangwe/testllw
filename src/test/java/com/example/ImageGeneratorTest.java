package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link ImageGenerator} 单元测试。
 * <p>
 * 覆盖正常路径、默认参数填充、无参入口与参数非法（IMG_003）等分支。
 * </p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
class ImageGeneratorTest {

    private final ImageGenerator imageGenerator = new ImageGenerator();

    @Test
    void should_generatePng_when_validRequest(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path output = tempDir.resolve("nested/dir/hello-world.png");
        ImageGenRequest request = buildRequest("Hello World", 320, 160, output.toString());

        // Act
        imageGenerator.generate(request);

        // Assert
        assertThat(output).exists();
        assertThat(Files.size(output)).isPositive();
        BufferedImage image = ImageIO.read(output.toFile());
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(320);
        assertThat(image.getHeight()).isEqualTo(160);
    }

    @Test
    void should_applyDefaultValues_when_fieldsAreBlank(@TempDir Path tempDir) throws IOException {
        // Arrange
        ImageGenRequest request = new ImageGenRequest();
        request.setOutputPath(tempDir.resolve("default.png").toString());

        // Act
        imageGenerator.generate(request);

        // Assert
        BufferedImage image = ImageIO.read(tempDir.resolve("default.png").toFile());
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(800);
        assertThat(image.getHeight()).isEqualTo(400);
        assertThat(image.getRGB(0, 0)).isEqualTo(Color.WHITE.getRGB());
    }

    @Test
    void should_writeDefaultFile_when_generateDefault() throws IOException {
        // Arrange
        File defaultFile = new File("hello-world.png");

        // Act & Assert
        try {
            imageGenerator.generateDefault();
            assertThat(defaultFile).exists();
            assertThat(ImageIO.read(defaultFile)).isNotNull();
        } finally {
            Files.deleteIfExists(defaultFile.toPath());
        }
    }

    @Test
    void should_throwInvalidParameter_when_widthIsZero(@TempDir Path tempDir) {
        // Arrange
        ImageGenRequest request = buildRequest("Hello", 0, 100, tempDir.resolve("zero.png").toString());

        // Act & Assert
        assertThatThrownBy(() -> imageGenerator.generate(request))
            .isInstanceOfSatisfying(ImageGenException.class,
                e -> assertThat(e.getCode()).isEqualTo("IMG_003"));
    }

    @Test
    void should_throwInvalidParameter_when_heightIsNegative(@TempDir Path tempDir) {
        // Arrange
        ImageGenRequest request = buildRequest("Hello", 100, -1, tempDir.resolve("neg.png").toString());

        // Act & Assert
        assertThatThrownBy(() -> imageGenerator.generate(request))
            .isInstanceOfSatisfying(ImageGenException.class,
                e -> assertThat(e.getCode()).isEqualTo("IMG_003"));
    }

    @Test
    void should_throwInvalidParameter_when_colorIsInvalid(@TempDir Path tempDir) {
        // Arrange
        ImageGenRequest request = buildRequest("Hello", 100, 100, tempDir.resolve("bad.png").toString());
        request.setBackgroundColor("#GGGGGG");

        // Act & Assert
        assertThatThrownBy(() -> imageGenerator.generate(request))
            .isInstanceOfSatisfying(ImageGenException.class,
                e -> assertThat(e.getCode()).isEqualTo("IMG_003"));
    }

    private ImageGenRequest buildRequest(String text, int width, int height, String outputPath) {
        ImageGenRequest request = new ImageGenRequest();
        request.setText(text);
        request.setWidth(width);
        request.setHeight(height);
        request.setBackgroundColor("#FFFFFF");
        request.setTextColor("#000000");
        request.setOutputPath(outputPath);
        return request;
    }
}