package org.example.newyear.entity.algorithm.vision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VisionAbility {
    
    WAN_ANIMATE("Dreamface-WanAnimate-Image2Video-V1", "人物替换"),
    FLUX2_IMAGE_GEN("Dreamface-Flux2-ImageGen-V1", "多图生图"),
    WAN_VIDEO_FLF("AIGC-WanVideo-ImageToVideo-FLF", "首尾帧"),
    LIPSYNC("ImageProcess-TalkingFace-Render-V1", "口型同步");
    
    private final String code;
    private final String description;
}