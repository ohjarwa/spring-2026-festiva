package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

/**
 * 歌曲转换回调响应
 */
@Data
public class SongConversionCallbackResponse {
    
    /**
     * 返回码，1表示成功，其他表示异常
     */
    private Integer code;
    
    /**
     * 详情
     */
    private String msg;
    
    /**
     * 转换结果
     */
    private SongConversionCallbackData data;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
}