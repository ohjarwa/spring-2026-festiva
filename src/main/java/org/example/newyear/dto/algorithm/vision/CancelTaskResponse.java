package org.example.newyear.dto.algorithm.vision;

import lombok.Data;

/**
 * 取消任务响应
 */
@Data
public class CancelTaskResponse {
    
    /**
     * 错误码，0为成功
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 数据（通常为 null）
     */
    private Object data;
    
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}