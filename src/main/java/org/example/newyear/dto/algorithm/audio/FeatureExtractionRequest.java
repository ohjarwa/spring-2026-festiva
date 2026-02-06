package org.example.newyear.dto.algorithm.audio;

import lombok.Builder;
import lombok.Data;

/**
 * 特征提取请求
 * 接口: http://ip:port/hapi/v1/svc_feature/extraction
 * Content-Type: application/x-www-form-urlencoded
 */
@Data
@Builder
public class FeatureExtractionRequest {
    
    /**
     * 任务ID，用于唯一标识一次特征提取任务
     * 必填
     */
    private String businessTaskId;
    
    /**
     * 回调地址
     * 必填
     */
    private String callbackUrl;
    
    /**
     * 音频源地址
     * 必填
     */
    private String videoUrl;
    
    /**
     * 业务编号
     * 非必填
     */
    private String source;
    
    /**
     * 指定目标特征提取ID名称（慎用）
     * 非必填
     */
    private String featureName;
    
    /**
     * 是否过音乐分离
     * 0: 不过
     * 1: 过（默认）
     * 非必填
     */
    private Integer demusic;
}