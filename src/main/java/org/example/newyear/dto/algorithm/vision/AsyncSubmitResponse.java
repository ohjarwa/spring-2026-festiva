package org.example.newyear.dto.algorithm.vision;

import lombok.Data;

@Data
public class AsyncSubmitResponse {
    /**
     * 错误码，0为正常
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private AsyncSubmitData data;
    
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}