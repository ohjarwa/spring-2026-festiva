package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

/**
 * 多人场景参数
 */
@Data
@Builder
public class MultiPersonParam {
    
    /**
     * 人物ID
     */
    private Integer id;
    
    /**
     * 区域 [x, y, width, height]
     */
    private int[] box;
    
    /**
     * 该人物的音频URL
     */
    private String audioUrl;
    
    /**
     * 人脸照片URL
     * 非必填
     */
    private String facePhotoUrl;
}
