package org.example.newyear.entity.enums;

public enum AlgorithmEnum {

    WAN_ANIMATE("wan_animate", "万能动画"),
    FLUX2_IMAGE_GEN("flux2_image_gen", "Flux2图片生成"),
    LIPS_SYNC("lips_sync", "人声同步"),
    WAN_VIDEO_FLF("wan_video_flf", "万能视频-首尾帧"),

    SONG_CONVERSION("song_conversion", "歌曲转换"),
    VOICE_CONVERSION("voice_conversion", "人声转换");

    private final String name;
    private final String description;

    AlgorithmEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static AlgorithmEnum of(String name) {
        for (AlgorithmEnum value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据视觉平台 Ability 获取枚举
     */
    public static AlgorithmEnum fromVisionAbility(String ability) {
        if (ability == null) return null;
        switch (ability) {
            case "Dreamface-WanAnimate-Image2Video-V1":
                return WAN_ANIMATE;
            case "Dreamface-Flux2-ImageGen-V1":
                return FLUX2_IMAGE_GEN;
            case "ImageProcess-TalkingFace-Render-V1":
                return LIPS_SYNC;
            case "AIGC-WanVideo-ImageToVideo-FLF":
                return WAN_VIDEO_FLF;
            default:
                return null;
        }
    }
}
