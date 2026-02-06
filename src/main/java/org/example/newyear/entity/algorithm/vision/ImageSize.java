package org.example.newyear.entity.algorithm.vision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageSize {
    
    SIZE_512(512, 512),
    SIZE_768(768, 768),
    SIZE_1024(1024, 1024),
    SIZE_1024_768(1024, 768),   // 4:3 横向
    SIZE_768_1024(768, 1024),   // 3:4 纵向
    SIZE_1280_720(1280, 720),   // 16:9 横向
    SIZE_720_1280(720, 1280),   // 9:16 纵向
    SIZE_1440(1440, 1440);      // 最大尺寸
    
    private final int width;
    private final int height;
    
    /**
     * 校验尺寸是否合法（不超过 1440*1440）
     */
    public static boolean isValidSize(int width, int height) {
        return width > 0 && height > 0 && width <= 1440 && height <= 1440;
    }
}