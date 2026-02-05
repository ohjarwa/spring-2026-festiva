package org.example.newyear.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.callback.*;
import org.example.newyear.service.VideoProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 视频算法回调控制器
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@RestController
@RequestMapping("/api/callback/video")
@RequiredArgsConstructor
public class VideoAlgorithmCallbackController {

    private final VideoProcessingService videoProcessingService;
    private final ObjectMapper objectMapper;

    /**
     * 人脸替换回调接口
     *
     * @param response 回调响应
     * @return 处理结果
     */
    @PostMapping("/face-swap")
    public Map<String, Object> handleFaceSwapCallback(@RequestBody VideoAlgorithmCallbackResponse response) {
        log.info("收到人脸替换回调: code={}, message={}", response.getCode(), response.getMessage());

        try {
            // 解析data为FaceSwapCallbackData
            FaceSwapCallbackData data = objectMapper.convertValue(response.getData(), FaceSwapCallbackData.class);
            log.info("人脸替换结果: targetVideoUrl={}", data.getTargetVideoUrl());

            videoProcessingService.notifyFaceSwapCallback(response, data);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理人脸替换回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 多图生图回调接口
     *
     * @param response 回调响应
     * @return 处理结果
     */
    @PostMapping("/multi-image-generate")
    public Map<String, Object> handleMultiImageGenerateCallback(@RequestBody VideoAlgorithmCallbackResponse response) {
        log.info("收到多图生图回调: code={}, message={}", response.getCode(), response.getMessage());

        try {
            // 解析data为MultiImageGenerateCallbackData
            MultiImageGenerateCallbackData data = objectMapper.convertValue(
                    response.getData(), MultiImageGenerateCallbackData.class);
            log.info("多图生图结果: fileUrls count={}",
                    data.getFileUrls() != null ? data.getFileUrls().size() : 0);

            videoProcessingService.notifyMultiImageGenerateCallback(response, data);
            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理多图生图回调失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 唇形同步回调接口
     *
     * @param response 回调响应
     * @return 处理结果
     */
    @PostMapping("/lip-sync")
    public Map<String, Object> handleLipSyncCallback(@RequestBody VideoAlgorithmCallbackResponse response) {
        log.info("收到唇形同步回调: code={}, message={}", response.getCode(), response.getMessage());

        try {
            // 解析data为LipSyncCallbackData
            LipSyncCallbackData data = objectMapper.convertValue(response.getData(), LipSyncCallbackData.class);
            log.info("唇形同步结果: videoUrl={}, code={}, message={}",
                    data.getVideoUrl(), data.getCode(), data.getMessage());

            videoProcessingService.notifyLipSyncCallback(response, data);
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
