package org.example.newyear.dto.algorithm.audio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

/**
 * 歌曲转换轮询结果数据
 */
@Data
public class SongConversionQueryData {
    
    /**
     * 任务ID
     */
    private String businessTaskId;
    
    /**
     * URL数据（JSON字符串）
     */
    private String url;
    
    /**
     * 状态：1-处理中，2-成功，3-失败
     */
    private Integer status;
    
    /**
     * 是否处理中
     */
    public boolean isProcessing() {
        return status != null && status == 1;
    }
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status != null && status == 2;
    }
    
    /**
     * 是否失败
     */
    public boolean isFailed() {
        return status != null && status == 3;
    }
    
    /**
     * 解析URL数据
     */
    public SongConversionUrlData parseUrl(ObjectMapper objectMapper) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(url, SongConversionUrlData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析URL数据失败", e);
        }
    }
}