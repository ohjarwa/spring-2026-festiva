package org.example.newyear.dto.algorithm.vision;

import org.example.newyear.entity.algorithm.vision.VideoResolution;
import org.example.newyear.entity.algorithm.vision.WanAnimateTaskMode;

import java.util.Arrays;
import java.util.List;

public class WanAnimateRequestBuilder {
    
    private WanAnimateTaskMode taskMode = WanAnimateTaskMode.REPLACE_BODY;
    private List<String> images;
    private String video;
    private String templateType = "user";
    private Integer seed = 42;
    private VideoResolution resolution = VideoResolution.P480;
    private String businessMessage;
    
    public static WanAnimateRequestBuilder builder() {
        return new WanAnimateRequestBuilder();
    }
    
    /**
     * 设置任务模式（默认 REPLACE_BODY）
     */
    public WanAnimateRequestBuilder taskMode(WanAnimateTaskMode taskMode) {
        this.taskMode = taskMode;
        return this;
    }
    
    /**
     * 设置图片URL列表
     */
    public WanAnimateRequestBuilder images(List<String> images) {
        this.images = images;
        return this;
    }
    
    /**
     * 设置单张图片
     */
    public WanAnimateRequestBuilder image(String imageUrl) {
        this.images = Arrays.asList(imageUrl);
        return this;
    }
    
    /**
     * 设置驱动视频URL
     */
    public WanAnimateRequestBuilder video(String video) {
        this.video = video;
        return this;
    }
    
    /**
     * 设置模板类型（默认 user）
     */
    public WanAnimateRequestBuilder templateType(String templateType) {
        this.templateType = templateType;
        return this;
    }
    
    /**
     * 使用平台模板
     */
    public WanAnimateRequestBuilder usePlatformTemplate() {
        this.templateType = "platform";
        return this;
    }
    
    /**
     * 设置种子值（默认 42）
     */
    public WanAnimateRequestBuilder seed(Integer seed) {
        this.seed = seed;
        return this;
    }
    
    /**
     * 设置分辨率（默认 480P）
     */
    public WanAnimateRequestBuilder resolution(VideoResolution resolution) {
        this.resolution = resolution;
        return this;
    }
    
    /**
     * 设置业务透传数据
     */
    public WanAnimateRequestBuilder businessMessage(String businessMessage) {
        this.businessMessage = businessMessage;
        return this;
    }
    
    public WanAnimateRequest build() {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("images 不能为空");
        }
        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException("video 不能为空");
        }
        
        return WanAnimateRequest.builder()
            .taskMode(taskMode.getCode())
            .images(images)
            .video(video)
            .templateType(templateType)
            .seed(seed)
            .resolution(resolution.getCode())
            .businessMessage(businessMessage)
            .build();
    }
}