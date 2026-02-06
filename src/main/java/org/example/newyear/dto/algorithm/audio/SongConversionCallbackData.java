package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

/**
 * 歌曲转换回调数据
 */
@Data
public class SongConversionCallbackData {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 结果下载地址（6天过期）
     */
    private String result;
}