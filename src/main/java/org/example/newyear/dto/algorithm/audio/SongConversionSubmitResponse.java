package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

/**
 * 歌曲转换异步提交响应
 */
@Data
public class SongConversionSubmitResponse {
    
    /**
     * 返回码，1表示成功，其他表示异常
     */
    private Integer code;
    
    /**
     * 详情
     */
    private String msg;
    
    /**
     * 预留字段
     */
    private Object data;
    
    /**
     * 是否提交成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
}