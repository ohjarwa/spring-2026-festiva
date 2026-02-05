package org.example.newyear.dto.algorithm.vision;

import lombok.Data;

@Data
public class VisionResponse<T> {
    /**
     * 错误码，0为正常
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 任务状态：1-提交 2-执行中 3-成功 4-失败 5-取消
     */
    private Integer taskStatus;
    
    /**
     * 任务状态描述
     */
    private String taskStatusDescription;
    
    /**
     * 业务任务ID
     */
    private String businessTaskId;
    
    /**
     * 业务透传数据
     */
    private String businessMessage;
    
    /**
     * 原子能力名
     */
    private String ability;
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 响应数据
     */
    private T data;
    
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}