package org.example.newyear.entity.algorithm.vision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageRatio {
    
    RATIO_1_1("1:1", 1024, 1024),
    RATIO_4_3("4:3", 1024, 768),
    RATIO_3_4("3:4", 768, 1024),
    RATIO_16_9("16:9", 1024, 576),
    RATIO_9_16("9:16", 576, 1024);
    
    private final String code;
    private final int defaultWidth;
    private final int defaultHeight;
    
    public static ImageRatio of(String code) {
        for (ImageRatio ratio : values()) {
            if (ratio.code.equals(code)) {
                return ratio;
            }
        }
        return RATIO_4_3; // 默认 4:3
    }
}