package org.example.newyear.dto.algorithm.audio;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongConversionRequest {
    
    /**
     * 人声下载地址
     * 必填
     */
    private String audioUrl;
    
    /**
     * 背景音下载地址
     * 必填
     */
    private String bgmUrl;
    
    /**
     * 回调地址
     * 必填
     */
    private String callbackUrl;
    
    /**
     * 任务ID，用于唯一确定一条转换请求
     * 必填
     */
    private String businessTaskId;
    
    /**
     * 模型编号（或特征ID）
     * 必填
     */
    private String modelCode;
    
    /**
     * 提取特征的原音频文件下载地址
     * 必填
     */
    private String voiceUrl;
    
    /**
     * 业务编号（可使用 appId 或协商获取）
     * 必填
     */
    private String source;
    
    /**
     * 变调值
     * 范围 -12~12，0为不变调，-99为自适应变调
     * 非必填，不传时引擎将不变调
     */
    private Integer pitch;
    
    /**
     * 后处理方案
     * base: 仅变调
     * general: 全量操作
     * 非必填
     */
    private String boardType;
}