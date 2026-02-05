package org.example.newyear.dto.algorithm.audio;

/**
 * 歌曲转换请求构建器
 */
public class SongConversionRequestBuilder {
    
    private String audioUrl;
    private String bgmUrl;
    private String businessTaskId;
    private String modelCode;
    private String voiceUrl;
    private Integer pitch;
    private String boardType;
    
    private SongConversionRequestBuilder() {}
    
    public static SongConversionRequestBuilder builder() {
        return new SongConversionRequestBuilder();
    }
    
    /**
     * 设置人声下载地址（必填）
     */
    public SongConversionRequestBuilder audioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
        return this;
    }
    
    /**
     * 设置背景音下载地址（必填）
     */
    public SongConversionRequestBuilder bgmUrl(String bgmUrl) {
        this.bgmUrl = bgmUrl;
        return this;
    }
    
    /**
     * 设置任务ID（必填）
     */
    public SongConversionRequestBuilder businessTaskId(String businessTaskId) {
        this.businessTaskId = businessTaskId;
        return this;
    }
    
    /**
     * 设置模型编号（必填）
     */
    public SongConversionRequestBuilder modelCode(String modelCode) {
        this.modelCode = modelCode;
        return this;
    }
    
    /**
     * 设置提取特征的原音频文件地址（必填）
     */
    public SongConversionRequestBuilder voiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
        return this;
    }
    
    /**
     * 设置变调值（-12~12，0不变调，-99自适应变调）
     */
    public SongConversionRequestBuilder pitch(Integer pitch) {
        if (pitch != null && pitch != -99 && (pitch < -12 || pitch > 12)) {
            throw new IllegalArgumentException("pitch 范围为 -12~12，或 -99（自适应变调）");
        }
        this.pitch = pitch;
        return this;
    }
    
    /**
     * 不变调
     */
    public SongConversionRequestBuilder noPitchShift() {
        this.pitch = 0;
        return this;
    }
    
    /**
     * 自适应变调
     */
    public SongConversionRequestBuilder adaptivePitch() {
        this.pitch = -99;
        return this;
    }
    
    /**
     * 升调
     */
    public SongConversionRequestBuilder pitchUp(int semitones) {
        return pitch(Math.min(semitones, 12));
    }
    
    /**
     * 降调
     */
    public SongConversionRequestBuilder pitchDown(int semitones) {
        return pitch(Math.max(-semitones, -12));
    }
    
    /**
     * 设置后处理方案
     */
    public SongConversionRequestBuilder boardType(String boardType) {
        this.boardType = boardType;
        return this;
    }
    
    /**
     * 仅变调后处理
     */
    public SongConversionRequestBuilder boardTypeBase() {
        this.boardType = "base";
        return this;
    }
    
    /**
     * 全量操作后处理
     */
    public SongConversionRequestBuilder boardTypeGeneral() {
        this.boardType = "general";
        return this;
    }
    
    public SongConversionRequest build(String callbackUrl, String source) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            throw new IllegalArgumentException("audioUrl 不能为空");
        }
        if (bgmUrl == null || bgmUrl.isEmpty()) {
            throw new IllegalArgumentException("bgmUrl 不能为空");
        }
        if (businessTaskId == null || businessTaskId.isEmpty()) {
            throw new IllegalArgumentException("businessTaskId 不能为空");
        }
        if (modelCode == null || modelCode.isEmpty()) {
            throw new IllegalArgumentException("modelCode 不能为空");
        }
        if (voiceUrl == null || voiceUrl.isEmpty()) {
            throw new IllegalArgumentException("voiceUrl 不能为空");
        }
        
        return SongConversionRequest.builder()
            .audioUrl(audioUrl)
            .bgmUrl(bgmUrl)
            .callbackUrl(callbackUrl)
            .businessTaskId(businessTaskId)
            .modelCode(modelCode)
            .voiceUrl(voiceUrl)
            .source(source)
            .pitch(pitch)
            .boardType(boardType)
            .build();
    }
}