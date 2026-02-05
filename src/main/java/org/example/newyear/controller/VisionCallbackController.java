package org.example.newyear.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.vision.VisionCallbackResponse;
import org.example.newyear.entity.enums.TaskStatus;
import org.example.newyear.service.VideoProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/vision/callback")
@RequiredArgsConstructor
public class VisionCallbackController {

    private final VideoProcessingService videoProcessingService;
    private final ObjectMapper objectMapper;

    /**
     * 任务结果回调
     */
    @PostMapping("/result")
    public ResponseEntity<String> handleResultCallback(
            @RequestBody VisionCallbackResponse<JsonNode> callback) {

        log.info("收到任务结果回调, taskId={}, ability={}, taskStatus={}",
                callback.getBusinessTaskId(),
                callback.getAbility(),
                callback.getTaskStatusDescription());

        // 1. 检查框架层状态
        if (!callback.isSuccess()) {
            log.error("框架层错误, taskId={}, code={}, message={}",
                    callback.getBusinessTaskId(), callback.getCode(), callback.getMessage());
            // 仍然需要处理失败，以唤醒等待线程
            handleFailure(callback);
            return ResponseEntity.ok("success");
        }

        // 2. 根据任务状态处理
        TaskStatus status = TaskStatus.of(callback.getTaskStatus());

        switch (status) {
            case EXECUTE_SUCCESS:
                handleSuccess(callback);
                break;
            case EXECUTE_FAILED:
                handleFailure(callback);
                break;
            case CANCELLED:
                handleCancelled(callback);
                break;
            default:
                log.warn("未知任务状态, taskId={}, status={}",
                        callback.getBusinessTaskId(), status);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 任务进度回调
     */
    @PostMapping("/progress")
    public ResponseEntity<String> handleProgressCallback(
            @RequestBody VisionCallbackResponse<JsonNode> callback) {

        log.info("收到任务进度回调, taskId={}, ability={}, status={}",
                callback.getBusinessTaskId(),
                callback.getAbility(),
                callback.getTaskStatusDescription());

        // TODO: 更新任务状态，通知前端等

        return ResponseEntity.ok("success");
    }

    /**
     * 处理成功回调
     */
    private void handleSuccess(VisionCallbackResponse<JsonNode> callback) {
        String ability = callback.getAbility();
        JsonNode data = callback.getData();

        // 检查算法内部状态
        if (data != null && data.has("code")) {
            int algorithmCode = data.get("code").asInt();
            String algorithmMessage = data.has("message") ? data.get("message").asText() : "";

            if (algorithmCode < 200 || algorithmCode >= 400) {
                log.warn("算法内部错误, taskId={}, algorithmCode={}, algorithmMessage={}",
                        callback.getBusinessTaskId(), algorithmCode, algorithmMessage);
                handleFailure(callback);
                return;
            }
        }

        log.info("任务执行成功, taskId={}, ability={}",
                callback.getBusinessTaskId(), ability);

        // 根据 ability 分发到不同处理逻辑
        dispatchCallback(callback);
    }

    /**
     * 根据能力类型分发回调
     */
    private void dispatchCallback(VisionCallbackResponse<JsonNode> callback) {
        String ability = callback.getAbility();
        String callbackId = callback.getBusinessTaskId();

        try {
            switch (ability) {
                case "Dreamface-WanAnimate-V1":
                    videoProcessingService.notifyWanAnimateCallback(callback, callbackId);
                    break;

                case "Dreamface-Flux2-ImageGen-V1":
                    videoProcessingService.notifyFlux2ImageGenCallback(callback, callbackId);
                    break;

                case "Dreamface-Lipsync-V1":
                    videoProcessingService.notifyLipsyncCallback(callback, callbackId);
                    break;

                case "Dreamface-WanVideo-FLF-V1":
                    videoProcessingService.notifyWanVideoFLFCallback(callback, callbackId);
                    break;

                default:
                    log.warn("未知的ability类型: {}", ability);
            }
        } catch (Exception e) {
            log.error("处理vision回调异常: ability={}, callbackId={}", ability, callbackId, e);
        }
    }

    /**
     * 处理失败回调
     */
    private void handleFailure(VisionCallbackResponse<JsonNode> callback) {
        log.warn("任务执行失败, taskId={}, code={}, message={}",
                callback.getBusinessTaskId(), callback.getCode(), callback.getMessage());

        String ability = callback.getAbility();
        String callbackId = callback.getBusinessTaskId();

        // 根据 ability 分发失败处理
        try {
            switch (ability) {
                case "Dreamface-WanAnimate-V1":
                    videoProcessingService.notifyWanAnimateCallback(callback, callbackId);
                    break;

                case "Dreamface-Flux2-ImageGen-V1":
                    videoProcessingService.notifyFlux2ImageGenCallback(callback, callbackId);
                    break;

                case "Dreamface-Lipsync-V1":
                    videoProcessingService.notifyLipsyncCallback(callback, callbackId);
                    break;

                case "Dreamface-WanVideo-FLF-V1":
                    videoProcessingService.notifyWanVideoFLFCallback(callback, callbackId);
                    break;

                default:
                    log.warn("未知的ability类型: {}", ability);
            }
        } catch (Exception e) {
            log.error("处理vision失败回调异常: ability={}, callbackId={}", ability, callbackId, e);
        }
    }

    /**
     * 处理取消回调
     */
    private void handleCancelled(VisionCallbackResponse<JsonNode> callback) {
        log.info("任务已取消, taskId={}", callback.getBusinessTaskId());
        // TODO: 处理取消逻辑
    }
}