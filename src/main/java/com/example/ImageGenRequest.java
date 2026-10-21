package com.example;

/**
 * 图片生成请求入参，承载「生成 HelloWord 图片」所需的全部可配置项。
 * <p>
 * 全部字段均可为空，空值在生成时由 {@link ImageGenerator} 自动填充默认值。
 * </p>
 *
 * @author AiWork
 * @date 2026/10/21
 */
public class ImageGenRequest {

    /** 图片文字内容，默认 "Hello World" */
    private String text;

    /** 图片宽度（像素），默认 800 */
    private Integer width;

    /** 图片高度（像素），默认 400 */
    private Integer height;

    /** 背景色（#RRGGBB），默认 #FFFFFF */
    private String backgroundColor;

    /** 文字色（#RRGGBB），默认 #000000 */
    private String textColor;

    /** 输出文件路径，默认 hello-world.png */
    private String outputPath;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String toString() {
        return "ImageGenRequest{"
            + "text='" + text + '\''
            + ", width=" + width
            + ", height=" + height
            + ", backgroundColor='" + backgroundColor + '\''
            + ", textColor='" + textColor + '\''
            + ", outputPath='" + outputPath + '\''
            + '}';
    }
}