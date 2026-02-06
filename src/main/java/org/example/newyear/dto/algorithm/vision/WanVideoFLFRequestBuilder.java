package org.example.newyear.dto.algorithm.vision;

import java.util.HashMap;
import java.util.Map;

/**
 * WanVideo FLF 请求构建辅助类
 * 
 * 使用示例：
 * WanVideoFLFRequestBuilder.builder()
 *     .firstImage("http://first.png")
 *     .lastImage("http://last.png")
 *     .resolution(WanVideoResolution.P720)
 *     .build()
 */
public class WanVideoFLFRequestBuilder {
    
    private String prompt = "";  // 默认空字符串
    private String firstImage;
    private String lastImage;
    private WanVideoResolution resolution = WanVideoResolution.P480;  // 默认 480P
    private Integer seed = -1;   // 默认 -1
    private Map<String, Object> extParams;
    private String businessMessage;
    
    private WanVideoFLFRequestBuilder() {}
    
    public static WanVideoFLFRequestBuilder builder() {
        return new WanVideoFLFRequestBuilder();
    }
    
    /**
     * 设置提示词（可选，默认空字符串）
     */
    public WanVideoFLFRequestBuilder prompt(String prompt) {
        this.prompt = prompt != null ? prompt : "";
        return this;
    }
    
    /**
     * 设置首帧图URL（必填）
     */
    public WanVideoFLFRequestBuilder firstImage(String firstImage) {
        this.firstImage = firstImage;
        return this;
    }
    
    /**
     * 设置尾帧图URL（必填）
     */
    public WanVideoFLFRequestBuilder lastImage(String lastImage) {
        this.lastImage = lastImage;
        return this;
    }
    
    /**
     * 同时设置首尾帧图URL
     */
    public WanVideoFLFRequestBuilder images(String firstImage, String lastImage) {
        this.firstImage = firstImage;
        this.lastImage = lastImage;
        return this;
    }
    
    /**
     * 设置分辨率（默认 480P）
     */
    public WanVideoFLFRequestBuilder resolution(WanVideoResolution resolution) {
        this.resolution = resolution;
        return this;
    }
    
    /**
     * 使用 480P 分辨率
     */
    public WanVideoFLFRequestBuilder use480P() {
        this.resolution = WanVideoResolution.P480;
        return this;
    }
    
    /**
     * 使用 720P 分辨率
     */
    public WanVideoFLFRequestBuilder use720P() {
        this.resolution = WanVideoResolution.P720;
        return this;
    }
    
    /**
     * 设置随机种子（默认 -1）
     */
    public WanVideoFLFRequestBuilder seed(Integer seed) {
        this.seed = seed;
        return this;
    }
    
    /**
     * 设置扩展参数
     */
    public WanVideoFLFRequestBuilder extParams(Map<String, Object> extParams) {
        this.extParams = extParams;
        return this;
    }
    
    /**
     * 添加单个扩展参数
     */
    public WanVideoFLFRequestBuilder addExtParam(String key, Object value) {
        if (this.extParams == null) {
            this.extParams = new HashMap<>();
        }
        this.extParams.put(key, value);
        return this;
    }
    
    /**
     * 设置业务透传数据
     */
    public WanVideoFLFRequestBuilder businessMessage(String businessMessage) {
        this.businessMessage = businessMessage;
        return this;
    }
    
    public WanVideoFLFRequest build() {
        if (firstImage == null || firstImage.isEmpty()) {
            throw new IllegalArgumentException("firstImage 不能为空");
        }
        if (lastImage == null || lastImage.isEmpty()) {
            throw new IllegalArgumentException("lastImage 不能为空");
        }
        
        return WanVideoFLFRequest.builder()
            .prompt(prompt)
            .firstImage(firstImage)
            .lastImage(lastImage)
            .width(resolution.getWidth())
            .height(resolution.getHeight())
            .seed(seed)
            .extParams(extParams)
            .businessMessage(businessMessage)
            .build();
    }
}