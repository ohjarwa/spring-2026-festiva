package org.example.newyear.entity.algorithm.vision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VideoResolution {
    
    /**
     * 480P - 视频时长不超过60s
     */
    P480("480P", 60),
    
    /**
     * 720P - 视频时长不超过15s
     */
    P720("720P", 15);
    
    private final String code;
    private final int maxDurationSeconds;
}