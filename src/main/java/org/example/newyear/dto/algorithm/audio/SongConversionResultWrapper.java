package org.example.newyear.dto.algorithm.audio;

import lombok.Builder;
import lombok.Data;

/**
 * 歌曲转换结果包装类（解析后的完整结果）
 */
@Data
@Builder
public class SongConversionResultWrapper {
    
    /**
     * 任务ID
     */
    private String businessTaskId;
    
    /**
     * 状态
     */
    private SongConversionStatus status;
    
    /**
     * 人声转换结果下载地址
     */
    private String vocalUrl;
    
    /**
     * 背景音转换结果下载地址
     */
    private String bgmUrl;
    
    /**
     * 完整音频下载地址
     */
    private String allUrl;
    
    /**
     * 原始响应
     */
    private SongConversionQueryResponse rawResponse;
    
    /**
     * 是否处理中
     */
    public boolean isProcessing() {
        return status == SongConversionStatus.PROCESSING;
    }
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status == SongConversionStatus.SUCCESS;
    }
    
    /**
     * 是否失败
     */
    public boolean isFailed() {
        return status == SongConversionStatus.FAILED;
    }
}