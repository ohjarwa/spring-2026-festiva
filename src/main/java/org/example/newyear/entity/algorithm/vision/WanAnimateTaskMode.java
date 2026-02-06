package org.example.newyear.entity.algorithm.vision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WanAnimateTaskMode {
    
    /**
     * 人物替换模式
     */
    REPLACE_BODY("replace_body", "人物替换"),
    
    /**
     * 姿态驱动模式
     */
    POSE2V("pose2v", "姿态驱动");
    
    private final String code;

    private final String description;
}