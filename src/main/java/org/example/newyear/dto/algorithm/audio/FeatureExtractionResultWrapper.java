package org.example.newyear.dto.algorithm.audio;

import lombok.Builder;
import lombok.Data;
import org.example.newyear.entity.task.TaskResultStatus;

/**
 * 特征提取结果包装类
 */
@Data
@Builder
public class FeatureExtractionResultWrapper {
    
    /**
     * 任务ID
     */
    private String businessTaskId;
    
    /**
     * 状态
     */
    private TaskResultStatus status;
    
    /**
     * 特征ID（成功时有值）
     */
    private String featureId;
    
    /**
     * 原音频URL
     */
    private String originUrl;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status == TaskResultStatus.SUCCESS && featureId != null;
    }
}