package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

/**
 * 特征提取异步提交响应
 */
@Data
public class FeatureExtractionSubmitResponse {
    
    /**
     * 返回码，1表示成功，其他表示异常
     * 11000: 风控校验失败
     */
    private Integer code;
    
    /**
     * 详情
     */
    private String msg;
    
    /**
     * 预留字段，目前为调用时的 businessTaskId
     */
    private String data;
    
    /**
     * 是否提交成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
    
    /**
     * 是否风控校验失败
     */
    public boolean isRiskControlFailed() {
        return code != null && code == 11000;
    }
}