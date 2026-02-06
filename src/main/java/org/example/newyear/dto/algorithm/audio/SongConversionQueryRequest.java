package org.example.newyear.dto.algorithm.audio;

import lombok.Builder;
import lombok.Data;

/**
 * 歌曲转换结果轮询请求
 */
@Data
@Builder
public class SongConversionQueryRequest {
    
    /**
     * 任务ID
     */
    private String businessTaskId;
    
    /**
     * 业务编号
     */
    private String source;
}