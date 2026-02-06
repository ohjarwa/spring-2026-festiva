package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

/**
 * 特征提取回调数据
 */
@Data
public class FeatureExtractionCallbackData {
    
    /**
     * 任务ID（businessTaskId）
     */
    private String taskId;
    
    /**
     * 原音频下载地址
     */
    private String originUrl;
    
    /**
     * 特征ID（用于后续歌曲转换）
     */
    private String featureId;
}