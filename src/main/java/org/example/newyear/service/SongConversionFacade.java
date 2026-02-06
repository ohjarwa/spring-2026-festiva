package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.SongConversionConfig;
import org.example.newyear.dto.algorithm.audio.*;
import org.example.newyear.service.algorithm.SongConversionService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SongConversionFacade {
    
    private final SongConversionService songConversionService;
    private final SongConversionConfig config;
    
    /**
     * 提交歌曲转换任务（完整参数）
     *
     * @param request 请求参数（不含 callbackUrl 和 source，由配置提供）
     * @return 提交结果
     */
    public SongConversionSubmitResponse submit(SongConversionRequest request) {
        // 填充配置中的 callbackUrl 和 source
        request.setCallbackUrl(config.getCallbackUrl());
        request.setSource(config.getSource());
        
        log.info("提交歌曲转换任务, taskId={}, modelCode={}", 
            request.getBusinessTaskId(), request.getModelCode());
        
        return songConversionService.submitAsync(request);
    }
    
    /**
     * 提交歌曲转换任务（简化参数）
     *
     * @param audioUrl  人声下载地址
     * @param bgmUrl    背景音下载地址
     * @param voiceUrl  提取特征的原音频文件地址
     * @param modelCode 模型编号
     * @param taskId    任务ID
     * @return 提交结果
     */
    public SongConversionSubmitResponse submit(String audioUrl, String bgmUrl, 
            String voiceUrl, String modelCode, String taskId) {
        
        SongConversionRequest request = SongConversionRequestBuilder.builder()
            .audioUrl(audioUrl)
            .bgmUrl(bgmUrl)
            .voiceUrl(voiceUrl)
            .modelCode(modelCode)
            .businessTaskId(taskId)
            .build(config.getCallbackUrl(), config.getSource());
        
        return songConversionService.submitAsync(request);
    }
    
    /**
     * 提交歌曲转换任务（带变调）
     *
     * @param audioUrl  人声下载地址
     * @param bgmUrl    背景音下载地址
     * @param voiceUrl  提取特征的原音频文件地址
     * @param modelCode 模型编号
     * @param pitch     变调值（-12~12，0不变调，-99自适应）
     * @param taskId    任务ID
     * @return 提交结果
     */
    public SongConversionSubmitResponse submitWithPitch(String audioUrl, String bgmUrl, 
            String voiceUrl, String modelCode, int pitch, String taskId) {
        
        SongConversionRequest request = SongConversionRequestBuilder.builder()
            .audioUrl(audioUrl)
            .bgmUrl(bgmUrl)
            .voiceUrl(voiceUrl)
            .modelCode(modelCode)
            .businessTaskId(taskId)
            .pitch(pitch)
            .build(config.getCallbackUrl(), config.getSource());
        
        return songConversionService.submitAsync(request);
    }
    
    /**
     * 提交歌曲转换任务（自适应变调）
     */
    public SongConversionSubmitResponse submitWithAdaptivePitch(String audioUrl, String bgmUrl, 
            String voiceUrl, String modelCode, String taskId) {
        
        SongConversionRequest request = SongConversionRequestBuilder.builder()
            .audioUrl(audioUrl)
            .bgmUrl(bgmUrl)
            .voiceUrl(voiceUrl)
            .modelCode(modelCode)
            .businessTaskId(taskId)
            .adaptivePitch()
            .build(config.getCallbackUrl(), config.getSource());
        
        return songConversionService.submitAsync(request);
    }
    
    /**
     * 查询转换结果（原始响应）
     */
    public SongConversionQueryResponse queryResult(String businessTaskId) {
        log.info("查询歌曲转换结果, taskId={}", businessTaskId);
        return songConversionService.queryResult(businessTaskId);
    }
    
    /**
     * 查询转换结果（解析后）
     */
    public SongConversionResultWrapper queryParsedResult(String businessTaskId) {
        log.info("查询并解析歌曲转换结果, taskId={}", businessTaskId);
        return songConversionService.queryAndParseResult(businessTaskId);
    }
    
    /**
     * 生成任务ID
     */
    public String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}