package org.example.newyear.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.vision.VisionCallbackResponse;
import org.example.newyear.entity.enums.TaskStatus;
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

    private void handleSuccess(VisionCallbackResponse<JsonNode> callback) {
        JsonNode data = callback.getData();

        // 检查算法内部状态
        if (data != null && data.has("code")) {
            int algorithmCode = data.get("code").asInt();
            String algorithmMessage = data.has("message") ? data.get("message").asText() : "";

            if (algorithmCode < 200 || algorithmCode >= 400) {
                log.warn("算法内部错误, taskId={}, algorithmCode={}, algorithmMessage={}",
                        callback.getBusinessTaskId(), algorithmCode, algorithmMessage);
                // TODO: 处理算法内部失败
                return;
            }
        }

        log.info("任务执行成功, taskId={}, ability={}",
                callback.getBusinessTaskId(), callback.getAbility());

        // TODO: 根据 ability 分发到不同处理逻辑
        // 提取 videoUrl / targetImageUrl 等
    }

    private void handleFailure(VisionCallbackResponse<JsonNode> callback) {
        log.warn("任务执行失败, taskId={}, code={}, message={}",
                callback.getBusinessTaskId(), callback.getCode(), callback.getMessage());
        // TODO: 处理失败逻辑
    }

    private void handleCancelled(VisionCallbackResponse<JsonNode> callback) {
        log.info("任务已取消, taskId={}", callback.getBusinessTaskId());
        // TODO: 处理取消逻辑
    }
}