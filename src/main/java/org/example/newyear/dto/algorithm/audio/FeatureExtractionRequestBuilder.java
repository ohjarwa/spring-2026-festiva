package org.example.newyear.dto.algorithm.audio;

/**
 * 特征提取请求构建器
 */
public class FeatureExtractionRequestBuilder {
    
    private String businessTaskId;
    private String videoUrl;
    private String featureName;
    private Integer demusic;
    
    private FeatureExtractionRequestBuilder() {}
    
    public static FeatureExtractionRequestBuilder builder() {
        return new FeatureExtractionRequestBuilder();
    }
    
    /**
     * 设置任务ID（必填）
     */
    public FeatureExtractionRequestBuilder businessTaskId(String businessTaskId) {
        this.businessTaskId = businessTaskId;
        return this;
    }
    
    /**
     * 设置音频源地址（必填）
     */
    public FeatureExtractionRequestBuilder videoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
        return this;
    }
    
    /**
     * 设置特征ID名称（慎用）
     */
    public FeatureExtractionRequestBuilder featureName(String featureName) {
        this.featureName = featureName;
        return this;
    }
    
    /**
     * 设置是否过音乐分离
     */
    public FeatureExtractionRequestBuilder demusic(Integer demusic) {
        this.demusic = demusic;
        return this;
    }
    
    /**
     * 启用音乐分离（默认）
     */
    public FeatureExtractionRequestBuilder enableDemusic() {
        this.demusic = 1;
        return this;
    }
    
    /**
     * 禁用音乐分离
     */
    public FeatureExtractionRequestBuilder disableDemusic() {
        this.demusic = 0;
        return this;
    }
    
    public FeatureExtractionRequest build(String callbackUrl, String source) {
        if (businessTaskId == null || businessTaskId.isEmpty()) {
            throw new IllegalArgumentException("businessTaskId 不能为空");
        }
        if (videoUrl == null || videoUrl.isEmpty()) {
            throw new IllegalArgumentException("videoUrl 不能为空");
        }
        
        return FeatureExtractionRequest.builder()
            .businessTaskId(businessTaskId)
            .callbackUrl(callbackUrl)
            .videoUrl(videoUrl)
            .source(source)
            .featureName(featureName)
            .demusic(demusic)
            .build();
    }
}