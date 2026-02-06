package org.example.newyear.dto.algorithm.vision;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoParams {
    
    // ========== 输出控制参数 ==========
    
    /**
     * 比特率（单位K）
     * 默认0表示采用ffmpeg自动压缩码率
     * 如果想保持视频清晰度，推荐自己指定码率
     */
    @JsonProperty("video_bitrate")
    private Integer videoBitrate;
    
    /**
     * 输出视频宽度
     * 默认0表示不指定
     */
    @JsonProperty("video_width")
    private Integer videoWidth;
    
    /**
     * 输出视频高度
     * 默认0表示不指定
     */
    @JsonProperty("video_height")
    private Integer videoHeight;
    
    /**
     * 是否超分
     * 0-否 1-是，默认0
     */
    @JsonProperty("video_enhance")
    private Integer videoEnhance;
    
    /**
     * 视频帧率
     * 默认0表示统一处理为25fps
     * 置1时保持原视频帧率（最大60fps）
     */
    @JsonProperty("keep_fps")
    private Integer keepFps;
    
    /**
     * 是否保持原视频码率
     * 默认0表示ffmpeg自动压缩码率
     * 置1时保持原视频码率，优先级低于 video_bitrate
     */
    @JsonProperty("keep_bitrate")
    private Integer keepBitrate;
    
    /**
     * 码率最大限制
     * 默认0表示不限制，但兜底值为50000（50M）
     */
    @JsonProperty("video_max_bitrate")
    private Integer videoMaxBitrate;
    
    /**
     * 首帧静音驱动
     * 用于优化宠物did效果
     * 默认0不启用，1启用
     */
    @JsonProperty("shutup_first")
    private Integer shutupFirst;
    
    // ========== 输入控制参数 ==========
    
    /**
     * 预处理的图像区域 [x, y, width, height]
     * 默认 [0, 0, 0, 0]，当width或height为0时失效
     */
    @JsonProperty("face_box")
    private int[] faceBox;
    
    /**
     * 人脸检测阈值
     * 默认0.5
     */
    @JsonProperty("face_det_threshold")
    private Double faceDetThreshold;
    
    /**
     * 侧脸检测
     * 默认0不启用，1表示过滤侧脸帧
     */
    @JsonProperty("filter_head_pose")
    private Integer filterHeadPose;
    
    /**
     * 说话幅度系数
     * 默认1.0，推荐范围1.0~2.0
     * 注意该值过大会导致嘴部形变
     */
    private Double amplifier;
    
    // ========== 资源限制参数 ==========
    
    /**
     * 限制底板视频和合成视频的长边
     * 避免视频分辨率过大打爆内存
     */
    @JsonProperty("video_max_side")
    private Integer videoMaxSide;
    
    /**
     * 限制输入音频的最大时长（单位秒）
     * 默认不限制
     */
    @JsonProperty("audio_max_time")
    private Integer audioMaxTime;
    
    /**
     * 创建默认参数（不指定任何输出限制）
     */
    public static VideoParams defaultParams() {
        return VideoParams.builder()
            .videoBitrate(0)
            .videoWidth(0)
            .videoHeight(0)
            .videoEnhance(0)
            .build();
    }
    
    /**
     * 创建高清参数（启用超分）
     */
    public static VideoParams hdParams() {
        return VideoParams.builder()
            .videoBitrate(0)
            .videoWidth(0)
            .videoHeight(0)
            .videoEnhance(1)
            .keepBitrate(1)
            .build();
    }
    
    /**
     * 创建指定分辨率的参数
     */
    public static VideoParams withResolution(int width, int height) {
        return VideoParams.builder()
            .videoBitrate(0)
            .videoWidth(width)
            .videoHeight(height)
            .videoEnhance(0)
            .build();
    }
}