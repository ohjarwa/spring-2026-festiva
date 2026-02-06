package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionCallbackData;
import org.example.newyear.dto.algorithm.audio.SongConversionCallbackData;
import org.example.newyear.dto.algorithm.vision.Flux2ImageGenResultData;
import org.example.newyear.dto.algorithm.vision.LipsyncResultData;
import org.example.newyear.dto.algorithm.vision.WanAnimateResultData;
import org.example.newyear.dto.algorithm.vision.WanVideoFLFResultData;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.service.task.TaskOrchestrator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoWorkflowService {
    
    private final VisionFacade visionFacade;
    private final SongConversionFacade songConversionFacade;
    private final TaskOrchestrator taskOrchestrator;
    
    /**
     * 示例1：口型同步任务
     */
    public String createLipsyncVideo(String videoUrl, String audioUrl) throws TimeoutException {
        String taskId = visionFacade.generateTaskId();
        
        // 初始化任务（指定算法类型）
        taskOrchestrator.initTask(taskId, AlgorithmEnum.LIPS_SYNC);
        
        // 提交任务
        visionFacade.submitLipsync(videoUrl, audioUrl, taskId);
        
        // 等待完成（指定算法类型）
        TaskResult result = taskOrchestrator.awaitTask(taskId, AlgorithmEnum.LIPS_SYNC,
            Duration.ofMinutes(5));
        
        if (result.isSuccess()) {
            return result.getVideoUrl();
        } else {
            throw new RuntimeException("口型同步失败: " + result.getErrorMessage());
        }
    }
    
    /**
     * 示例2：多步骤工作流
     */
    public void createMusicVideo(String videoUrl, String vocalUrl, String bgmUrl, 
            String modelCode) throws TimeoutException {
        
        // 步骤1: 口型同步
        String lipsyncTaskId = visionFacade.generateTaskId();
        taskOrchestrator.initTask(lipsyncTaskId, AlgorithmEnum.LIPS_SYNC);
        visionFacade.submitLipsync(videoUrl, vocalUrl, lipsyncTaskId);
        
        TaskResult lipsyncResult = taskOrchestrator.awaitTask(
            lipsyncTaskId, AlgorithmEnum.LIPS_SYNC, Duration.ofMinutes(10));
        
        if (!lipsyncResult.isSuccess()) {
            throw new RuntimeException("口型同步失败");
        }
        
        // 步骤2: 歌曲转换
        String songTaskId = songConversionFacade.generateTaskId();
        taskOrchestrator.initTask(songTaskId, AlgorithmEnum.SONG_CONVERSION);
        songConversionFacade.submit(vocalUrl, bgmUrl, vocalUrl, modelCode, songTaskId);


        // 获取结果示例
        SongConversionCallbackData result = taskOrchestrator.awaitTask(songTaskId, AlgorithmEnum.SONG_CONVERSION, SongConversionCallbackData.class);
        WanAnimateResultData wanAnimateResultData = taskOrchestrator.awaitTask(songTaskId, AlgorithmEnum.WAN_ANIMATE, WanAnimateResultData.class);
        WanVideoFLFResultData wanVideoFLFResultData = taskOrchestrator.awaitTask(songTaskId, AlgorithmEnum.WAN_VIDEO_FLF, WanVideoFLFResultData.class);
        Flux2ImageGenResultData flux2ImageGenResultData = taskOrchestrator.awaitTask(songTaskId, AlgorithmEnum.FLUX2_IMAGE_GEN, Flux2ImageGenResultData.class);
        LipsyncResultData lipsyncResultData = taskOrchestrator.awaitTask(songTaskId, AlgorithmEnum.LIPS_SYNC, LipsyncResultData.class);
        FeatureExtractionCallbackData featureExtractionCallbackData = taskOrchestrator.awaitTask(songTaskId, AlgorithmEnum.VOICE_CONVERSION, FeatureExtractionCallbackData.class);


        TaskResult songResult = taskOrchestrator.awaitTask(
            songTaskId, AlgorithmEnum.SONG_CONVERSION, Duration.ofMinutes(10));
        
        if (!songResult.isSuccess()) {
            throw new RuntimeException("歌曲转换失败");
        }
        
        log.info("工作流完成! 视频={}, 音频={}", 
            lipsyncResult.getVideoUrl(), songResult.getAudioUrl());
    }
    
    /**
     * 示例3：检查任务状态
     */
    public String checkStatus(String taskId, AlgorithmEnum algorithm) {
        TaskResult result = taskOrchestrator.checkTask(taskId, algorithm);
        
        if (result == null) {
            return "任务不存在";
        }
        return String.format("任务状态: %s", result.getStatus().getDescription());
    }
}