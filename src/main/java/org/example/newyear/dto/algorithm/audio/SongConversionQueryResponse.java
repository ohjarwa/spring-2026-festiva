package org.example.newyear.dto.algorithm.audio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

/**
 * 歌曲转换结果轮询响应
 */
@Data
public class SongConversionQueryResponse {
    
    /**
     * 返回码，1表示成功
     */
    private Integer code;
    
    /**
     * 详情
     */
    private String msg;
    
    /**
     * 结果数据（JSON字符串）
     */
    private String data;
    
    /**
     * 是否请求成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
    
    /**
     * 解析结果数据
     */
    public SongConversionQueryData parseData(ObjectMapper objectMapper) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(data, SongConversionQueryData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析轮询结果失败", e);
        }
    }
}