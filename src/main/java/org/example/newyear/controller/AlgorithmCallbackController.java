package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.dto.AlgorithmCallbackRequest;
import org.example.newyear.service.VideoProcessingService;
import org.springframework.web.bind.annotation.*;

/**
 * 算法回调控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class AlgorithmCallbackController {

    private final VideoProcessingService videoProcessingService;

    /**
     * 视频处理回调接口
     * 接收人脸替换、唇形同步等视频处理算法的异步回调
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/video")
    public Result<Void> handleVideoCallback(@RequestBody AlgorithmCallbackRequest request) {
        log.info("收到视频处理回调: taskId={}, stepName={}, status={}",
                request.getTaskId(), request.getStepName(), request.getStatus());

        try {
            videoProcessingService.handleVideoCallback(request);
            return Result.success();
        } catch (Exception e) {
            log.error("处理视频回调失败", e);
            return Result.error(50000, "处理回调失败");
        }
    }

    /**
     * 音频处理回调接口
     * 接收声音克隆、声音合成等音频处理算法的异步回调
     *
     * @param request 回调请求
     * @return 处理结果
     */
    @PostMapping("/audio")
    public Result<Void> handleAudioCallback(@RequestBody AlgorithmCallbackRequest request) {
        log.info("收到音频处理回调: taskId={}, stepName={}, status={}",
                request.getTaskId(), request.getStepName(), request.getStatus());

        try {
            videoProcessingService.handleAudioCallback(request);
            return Result.success();
        } catch (Exception e) {
            log.error("处理音频回调失败", e);
            return Result.error(50000, "处理回调失败");
        }
    }
}