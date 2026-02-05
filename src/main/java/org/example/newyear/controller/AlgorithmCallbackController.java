package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.callback.*;
import org.example.newyear.service.VideoProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 算法服务回调控制器
 * 为每个算法服务提供独立的回调接口
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@RestController
@RequestMapping("/api/callback")
@RequiredArgsConstructor
public class AlgorithmCallbackController {

    private final VideoProcessingService videoProcessingService;

    /**
     * 声音克隆回调接口
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/voice-clone")
    public Map<String, Object> handleVoiceCloneCallback(@RequestBody VoiceCloneCallbackDTO request) {
        log.info("收到声音克隆回调: callbackId={}, taskId={}, status={}, voiceId={}",
                request.getCallbackId(), request.getTaskId(), request.getStatus(), request.getVoiceId());

        try {
            videoProcessingService.notifyVoiceCloneCallback(request);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理声音克隆回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 声音合成TTS回调接口
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/voice-tts")
    public Map<String, Object> handleVoiceTtsCallback(@RequestBody VoiceTtsCallbackDTO request) {
        log.info("收到声音合成回调: callbackId={}, taskId={}, status={}, audioUrl={}",
                request.getCallbackId(), request.getTaskId(), request.getStatus(), request.getAudioUrl());

        try {
            videoProcessingService.notifyVoiceTtsCallback(request);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理声音合成回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 歌曲特征提取回调接口
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/song-feature-extract")
    public Map<String, Object> handleSongFeatureExtractCallback(@RequestBody SongFeatureExtractCallbackDTO request) {
        log.info("收到歌曲特征提取回调: callbackId={}, taskId={}, status={}, featureSize={}",
                request.getCallbackId(), request.getTaskId(), request.getStatus(),
                request.getFeature() != null ? request.getFeature().size() : 0);

        try {
            videoProcessingService.notifySongFeatureExtractCallback(request);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理歌曲特征提取回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 人脸替换回调接口
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/face-swap")
    public Map<String, Object> handleFaceSwapCallback(@RequestBody VideoProcessCallbackDTO request) {
        log.info("收到人脸替换回调: callbackId={}, taskId={}, status={}, videoUrl={}",
                request.getCallbackId(), request.getTaskId(), request.getStatus(), request.getVideoUrl());

        try {
            videoProcessingService.notifyVideoProcessCallback("face_swap", request);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理人脸替换回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 唇形同步回调接口
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/lip-sync")
    public Map<String, Object> handleLipSyncCallback(@RequestBody VideoProcessCallbackDTO request) {
        log.info("收到唇形同步回调: callbackId={}, taskId={}, status={}, videoUrl={}",
                request.getCallbackId(), request.getTaskId(), request.getStatus(), request.getVideoUrl());

        try {
            videoProcessingService.notifyVideoProcessCallback("lip_sync", request);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理唇形同步回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccessResponse() {
        return Map.of(
                "code", 0,
                "message", "success"
        );
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(String errorMsg) {
        return Map.of(
                "code", -1,
                "message", "处理失败: " + errorMsg
        );
    }
}
