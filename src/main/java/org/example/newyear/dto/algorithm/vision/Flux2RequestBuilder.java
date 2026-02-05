package org.example.newyear.dto.algorithm.vision;


import org.example.newyear.entity.algorithm.vision.ImageRatio;
import org.example.newyear.entity.algorithm.vision.ImageSize;

import java.util.Arrays;
import java.util.List;

/**
 * Flux2 请求构建辅助类
 * 
 * 使用示例：
 * 1. 文生图：Flux2RequestBuilder.textToImage("prompt").size(1024, 1024).build()
 * 2. 图生图：Flux2RequestBuilder.imageToImage("prompt", imageUrl).ratio(ImageRatio.RATIO_4_3).build()
 */
public class Flux2RequestBuilder {
    
    private static final int MAX_SIZE = 1440;
    
    private String prompt;
    private List<String> images;
    private String ratio;
    private Integer width;
    private Integer height;
    private Integer num;
    private String businessMessage;
    
    private Flux2RequestBuilder() {}
    
    /**
     * 创建文生图请求构建器
     * @param prompt 正向提示词
     */
    public static Flux2RequestBuilder textToImage(String prompt) {
        Flux2RequestBuilder builder = new Flux2RequestBuilder();
        builder.prompt = prompt;
        builder.num = 4; // 文生图默认生成 4 张
        return builder;
    }
    
    /**
     * 创建图生图请求构建器（单图）
     * @param prompt 正向提示词
     * @param imageUrl 参考图片URL
     */
    public static Flux2RequestBuilder imageToImage(String prompt, String imageUrl) {
        Flux2RequestBuilder builder = new Flux2RequestBuilder();
        builder.prompt = prompt;
        builder.images = Arrays.asList(imageUrl);
        builder.num = 1; // 图生图默认生成 1 张
        return builder;
    }
    
    /**
     * 创建图生图请求构建器（多图融合）
     * @param prompt 正向提示词
     * @param imageUrls 参考图片URL列表
     */
    public static Flux2RequestBuilder imageToImage(String prompt, List<String> imageUrls) {
        Flux2RequestBuilder builder = new Flux2RequestBuilder();
        builder.prompt = prompt;
        builder.images = imageUrls;
        builder.num = 1;
        return builder;
    }
    
    /**
     * 设置生成图片比例（与 size 二选一）
     * 使用 ratio 时，生成图像的像素面积值为 1024*1024
     */
    public Flux2RequestBuilder ratio(ImageRatio ratio) {
        this.ratio = ratio.getCode();
        this.width = null;
        this.height = null;
        return this;
    }
    
    /**
     * 设置生成图片比例（字符串形式）
     */
    public Flux2RequestBuilder ratio(String ratio) {
        this.ratio = ratio;
        this.width = null;
        this.height = null;
        return this;
    }
    
    /**
     * 设置生成图片尺寸（与 ratio 二选一）
     */
    public Flux2RequestBuilder size(int width, int height) {
        if (width > MAX_SIZE || height > MAX_SIZE) {
            throw new IllegalArgumentException("尺寸不能超过 " + MAX_SIZE);
        }
        this.width = width;
        this.height = height;
        this.ratio = null;
        return this;
    }
    
    /**
     * 设置生成图片尺寸（使用预设枚举）
     */
    public Flux2RequestBuilder size(ImageSize size) {
        return size(size.getWidth(), size.getHeight());
    }
    
    /**
     * 设置生成图片数量
     */
    public Flux2RequestBuilder num(int num) {
        if (num < 1) {
            throw new IllegalArgumentException("生成数量至少为 1");
        }
        this.num = num;
        return this;
    }
    
    /**
     * 设置业务透传数据
     */
    public Flux2RequestBuilder businessMessage(String businessMessage) {
        this.businessMessage = businessMessage;
        return this;
    }
    
    public Flux2ImageGenRequest build() {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("prompt 不能为空");
        }
        
        // 文生图模式必须指定尺寸
        boolean isTextToImage = (images == null || images.isEmpty());
        if (isTextToImage && width == null && height == null) {
            // 默认使用 1024x1024
            width = 1024;
            height = 1024;
        }
        
        return Flux2ImageGenRequest.builder()
            .prompt(prompt)
            .images(images)
            .ratio(ratio)
            .width(width)
            .height(height)
            .num(num)
            .businessMessage(businessMessage)
            .build();
    }
}