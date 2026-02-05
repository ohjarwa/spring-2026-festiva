package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

/**
 * Lipsync 校验参数
 * 传什么校验什么
 */
@Data
@Builder
public class CheckParams {
    
    /**
     * 视频宽最大值
     * 错误码 90001
     */
    private Integer videoWidthMax;
    
    /**
     * 视频宽最小值
     * 错误码 90002
     */
    private Integer videoWidthMin;
    
    /**
     * 视频高最大值
     * 错误码 90003
     */
    private Integer videoHeightMax;
    
    /**
     * 视频高最小值
     * 错误码 90004
     */
    private Integer videoHeightMin;
    
    /**
     * 视频码率最大值
     * 错误码 90005
     */
    private Integer videoFrameRateMax;
    
    /**
     * 视频时长最大值（秒）
     * 错误码 90006
     */
    private Integer videoLengthInSecondMax;
}