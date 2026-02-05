package org.example.newyear.dto.algorithm.vision;

import lombok.Data;

@Data
public class VisionCallbackResponse<T> {
    
    /**
     * 框架层错误码，0为正常
     */
    private Integer code;
    
    /**
     * 框架层错误信息
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
     * 业务任务ID（来自业务方请求时传入的 Task-Id）
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
     * 算法特定的返回数据（内层）
     */
    private T data;
    
    /**
     * 框架层是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
    
    /**
     * 任务是否执行成功
     */
    public boolean isTaskSuccess() {
        return taskStatus != null && taskStatus == 3;
    }
    
    /**
     * 任务是否执行失败
     */
    public boolean isTaskFailed() {
        return taskStatus != null && taskStatus == 4;
    }
    
    /**
     * 任务是否已取消
     */
    public boolean isTaskCancelled() {
        return taskStatus != null && taskStatus == 5;
    }
}