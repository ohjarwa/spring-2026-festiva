package org.example.newyear.dto.algorithm.vision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WanVideoResolution {
    
    /**
     * 480P - 832x480
     */
    P480(832, 480, "480P"),
    
    /**
     * 720P - 1280x720
     */
    P720(1280, 720, "720P");
    
    private final int width;
    private final int height;
    private final String description;
    
    /**
     * 根据描述获取分辨率
     */
    public static WanVideoResolution of(String description) {
        for (WanVideoResolution resolution : values()) {
            if (resolution.description.equalsIgnoreCase(description)) {
                return resolution;
            }
        }
        return P480; // 默认 480P
    }
    
    /**
     * 根据宽高判断分辨率
     */
    public static WanVideoResolution of(int width, int height) {
        if (width == 1280 && height == 720) {
            return P720;
        }
        return P480;
    }
}