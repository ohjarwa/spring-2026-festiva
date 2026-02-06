package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

/**
 * 特征提取回调响应
 */
@Data
public class FeatureExtractionCallbackResponse {
    
    /**
     * 返回码，1表示成功
     */
    private Integer code;
    
    /**
     * 详情
     */
    private String msg;
    
    /**
     * 回调数据
     */
    private FeatureExtractionCallbackData data;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
}