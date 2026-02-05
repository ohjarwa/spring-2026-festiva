package org.example.newyear.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.callback.*;
import org.example.newyear.service.VideoProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    private final ObjectMapper objectMapper;

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
     * @param response 回调响应（最外层）
     * @param callbackId 回调ID（包含recordId）
     * @return 处理结果
     */
    @PostMapping("/face-swap")
    public Map<String, Object> handleFaceSwapCallback(
            @RequestBody VideoAlgorithmCallbackResponse response,
            @RequestParam String callbackId) {

        log.info("收到人脸替换回调: callbackId={}, code={}, message={}",
                callbackId, response.getCode(), response.getMessage());

        try {
            // 解析data为FaceSwapCallbackData
            FaceSwapCallbackData data = parseCallbackData(response.getData(), FaceSwapCallbackData.class);
            log.info("人脸替换结果: targetVideoUrl={}", data.getTargetVideoUrl());

            videoProcessingService.notifyFaceSwapCallback(response, data, callbackId);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理人脸替换回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 唇形同步回调接口
     *
     * @param response 回调响应（最外层）
     * @param callbackId 回调ID（包含recordId）
     * @return 处理结果
     */
    @PostMapping("/lip-sync")
    public Map<String, Object> handleLipSyncCallback(
            @RequestBody VideoAlgorithmCallbackResponse response,
            @RequestParam String callbackId) {

        log.info("收到唇形同步回调: callbackId={}, code={}, message={}",
                callbackId, response.getCode(), response.getMessage());

        try {
            // 解析data为LipSyncCallbackData
            LipSyncCallbackData data = parseCallbackData(response.getData(), LipSyncCallbackData.class);
            log.info("唇形同步结果: videoUrl={}, code={}, message={}",
                    data.getVideoUrl(), data.getCode(), data.getMessage());

            videoProcessingService.notifyLipSyncCallback(response, data, callbackId);
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
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        return response;
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(String errorMsg) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", -1);
        response.put("message", "处理失败: " + errorMsg);
        return response;
    }

    /**
     * 解析回调数据
     *
     * @param data 回调data对象
     * @param clazz 目标类型
     * @return 解析后的对象
     */
    private <T> T parseCallbackData(Object data, Class<T> clazz) {
        try {
            return objectMapper.convertValue(data, clazz);
        } catch (Exception e) {
            log.error("解析回调数据失败: data={}, targetClass={}", data, clazz.getSimpleName(), e);
            throw new RuntimeException("解析回调数据失败", e);
        }
    }
}
