package org.example.newyear.dto.algorithm.vision;

import java.util.ArrayList;
import java.util.List;

/**
 * Lipsync 请求构建辅助类
 * 
 * 使用示例：
 * LipsyncRequestBuilder.builder()
 *     .srcVideoUrl("http://video.mp4")
 *     .audioUrl("http://audio.mp3")
 *     .build()
 */
public class LipsyncRequestBuilder {
    
    private String srcVideoUrl;
    private String audioUrl;
    private VideoParams videoParams;
    private String faceJsonUrl;
    private String vocalAudioUrl;
    private List<MultiPersonParam> multiPersonParams;
    private CheckParams checkParams;
    private String businessMessage;
    
    private LipsyncRequestBuilder() {
        this.videoParams = VideoParams.defaultParams();
    }
    
    public static LipsyncRequestBuilder builder() {
        return new LipsyncRequestBuilder();
    }
    
    /**
     * 设置源视频URL（必填）
     */
    public LipsyncRequestBuilder srcVideoUrl(String srcVideoUrl) {
        this.srcVideoUrl = srcVideoUrl;
        return this;
    }
    
    /**
     * 设置音频URL（必填）
     */
    public LipsyncRequestBuilder audioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
        return this;
    }
    
    /**
     * 设置视频参数
     */
    public LipsyncRequestBuilder videoParams(VideoParams videoParams) {
        this.videoParams = videoParams;
        return this;
    }
    
    /**
     * 启用超分（高清模式）
     */
    public LipsyncRequestBuilder enableEnhance() {
        this.videoParams = VideoParams.hdParams();
        return this;
    }
    
    /**
     * 设置输出分辨率
     */
    public LipsyncRequestBuilder outputResolution(int width, int height) {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setVideoWidth(width);
        this.videoParams.setVideoHeight(height);
        return this;
    }
    
    /**
     * 设置输出码率（单位K）
     */
    public LipsyncRequestBuilder outputBitrate(int bitrateK) {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setVideoBitrate(bitrateK);
        return this;
    }
    
    /**
     * 保持原视频帧率
     */
    public LipsyncRequestBuilder keepOriginalFps() {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setKeepFps(1);
        return this;
    }
    
    /**
     * 保持原视频码率
     */
    public LipsyncRequestBuilder keepOriginalBitrate() {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setKeepBitrate(1);
        return this;
    }
    
    /**
     * 设置说话幅度系数（推荐1.0~2.0）
     */
    public LipsyncRequestBuilder amplifier(double amplifier) {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setAmplifier(amplifier);
        return this;
    }
    
    /**
     * 启用侧脸过滤
     */
    public LipsyncRequestBuilder filterSideFace() {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setFilterHeadPose(1);
        return this;
    }
    
    /**
     * 启用首帧静音驱动（宠物场景优化）
     */
    public LipsyncRequestBuilder enableShutupFirst() {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setShutupFirst(1);
        return this;
    }
    
    /**
     * 设置人脸检测区域
     */
    public LipsyncRequestBuilder faceBox(int x, int y, int width, int height) {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setFaceBox(new int[]{x, y, width, height});
        return this;
    }
    
    /**
     * 设置人脸检测阈值（默认0.5）
     */
    public LipsyncRequestBuilder faceDetThreshold(double threshold) {
        if (this.videoParams == null) {
            this.videoParams = VideoParams.defaultParams();
        }
        this.videoParams.setFaceDetThreshold(threshold);
        return this;
    }
    
    /**
     * 设置视频信息json链接
     */
    public LipsyncRequestBuilder faceJsonUrl(String faceJsonUrl) {
        this.faceJsonUrl = faceJsonUrl;
        return this;
    }
    
    /**
     * 设置人声音频链接（用于推理，需要与audioUrl长度一致）
     */
    public LipsyncRequestBuilder vocalAudioUrl(String vocalAudioUrl) {
        this.vocalAudioUrl = vocalAudioUrl;
        return this;
    }
    
    /**
     * 添加多人参数
     */
    public LipsyncRequestBuilder addPerson(int id, int[] box, String audioUrl) {
        if (this.multiPersonParams == null) {
            this.multiPersonParams = new ArrayList<>();
        }
        this.multiPersonParams.add(MultiPersonParam.builder()
            .id(id)
            .box(box)
            .audioUrl(audioUrl)
            .build());
        return this;
    }
    
    /**
     * 添加多人参数（带人脸照片）
     */
    public LipsyncRequestBuilder addPerson(int id, int[] box, String audioUrl, String facePhotoUrl) {
        if (this.multiPersonParams == null) {
            this.multiPersonParams = new ArrayList<>();
        }
        this.multiPersonParams.add(MultiPersonParam.builder()
            .id(id)
            .box(box)
            .audioUrl(audioUrl)
            .facePhotoUrl(facePhotoUrl)
            .build());
        return this;
    }
    
    /**
     * 设置校验参数
     */
    public LipsyncRequestBuilder checkParams(CheckParams checkParams) {
        this.checkParams = checkParams;
        return this;
    }
    
    /**
     * 设置视频时长限制（秒）
     */
    public LipsyncRequestBuilder maxVideoDuration(int seconds) {
        if (this.checkParams == null) {
            this.checkParams = CheckParams.builder().build();
        }
        this.checkParams.setVideoLengthInSecondMax(seconds);
        return this;
    }
    
    /**
     * 设置业务透传数据
     */
    public LipsyncRequestBuilder businessMessage(String businessMessage) {
        this.businessMessage = businessMessage;
        return this;
    }
    
    public LipsyncRequest build() {
        if (srcVideoUrl == null || srcVideoUrl.isEmpty()) {
            throw new IllegalArgumentException("srcVideoUrl 不能为空");
        }
        if (audioUrl == null || audioUrl.isEmpty()) {
            throw new IllegalArgumentException("audioUrl 不能为空");
        }
        if (videoParams == null) {
            videoParams = VideoParams.defaultParams();
        }
        
        return LipsyncRequest.builder()
            .srcVideoUrl(srcVideoUrl)
            .audioUrl(audioUrl)
            .videoParams(videoParams)
            .faceJsonUrl(faceJsonUrl)
            .vocalAudioUrl(vocalAudioUrl)
            .multiPersonParams(multiPersonParams)
            .checkParams(checkParams)
            .businessMessage(businessMessage)
            .build();
    }
}