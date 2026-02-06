package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.FeatureExtractionConfig;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionRequest;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionRequestBuilder;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionSubmitResponse;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.service.algorithm.FeatureExtractionService;
import org.example.newyear.service.task.TaskOrchestrator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureExtractionFacade {
    
    private final FeatureExtractionService featureExtractionService;
    private final FeatureExtractionConfig config;
    private final TaskOrchestrator taskOrchestrator;
    
    /**
     * 提交特征提取任务（完整参数）
     */
    public FeatureExtractionSubmitResponse submit(FeatureExtractionRequest request) {
        // 填充配置
        request.setCallbackUrl(config.getCallbackUrl());
        request.setSource(config.getSource());
        
        log.info("提交特征提取任务, taskId={}, videoUrl={}", 
            request.getBusinessTaskId(), request.getVideoUrl());
        
        // 初始化任务状态
        taskOrchestrator.initTask(request.getBusinessTaskId(), AlgorithmEnum.VOICE_CONVERSION);
        
        return featureExtractionService.submitAsync(request);
    }
    
    /**
     * 提交特征提取任务（简化参数）
     *
     * @param audioUrl 音频源地址
     * @param taskId   任务ID
     * @return 提交结果
     */
    public FeatureExtractionSubmitResponse submit(String audioUrl, String taskId) {
        FeatureExtractionRequest request = FeatureExtractionRequestBuilder.builder()
            .businessTaskId(taskId)
            .videoUrl(audioUrl)
            .enableDemusic()  // 默认启用音乐分离
            .build(config.getCallbackUrl(), config.getSource());
        
        return submit(request);
    }
    
    /**
     * 提交特征提取任务（禁用音乐分离）
     */
    public FeatureExtractionSubmitResponse submitWithoutDemusic(String audioUrl, String taskId) {
        FeatureExtractionRequest request = FeatureExtractionRequestBuilder.builder()
            .businessTaskId(taskId)
            .videoUrl(audioUrl)
            .disableDemusic()
            .build(config.getCallbackUrl(), config.getSource());
        
        return submit(request);
    }
    
    /**
     * 提交特征提取任务（指定特征名称）
     */
    public FeatureExtractionSubmitResponse submitWithFeatureName(
            String audioUrl, String featureName, String taskId) {
        
        FeatureExtractionRequest request = FeatureExtractionRequestBuilder.builder()
            .businessTaskId(taskId)
            .videoUrl(audioUrl)
            .featureName(featureName)
            .enableDemusic()
            .build(config.getCallbackUrl(), config.getSource());
        
        return submit(request);
    }
}