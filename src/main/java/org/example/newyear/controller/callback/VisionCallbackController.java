package org.example.newyear.controller.callback;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.vision.VisionCallbackResponse;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.service.callback.CallbackHandler;
import org.example.newyear.service.callback.VisionCallbackConverter;
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

    private final CallbackHandler callbackHandler;
    private final VisionCallbackConverter converter;

    @PostMapping("/result")
    public ResponseEntity<String> handleResultCallback(
            @RequestBody VisionCallbackResponse<JsonNode> callback) {

        AlgorithmEnum algorithm = converter.getAlgorithm(callback);

        log.info("收到视觉任务回调, taskId={}, algorithm={}, taskStatus={}",
                callback.getBusinessTaskId(),
                algorithm != null ? algorithm.getName() : callback.getAbility(),
                callback.getTaskStatusDescription());

        try {
            TaskResult result = callbackHandler.handleCallback(callback, converter);

            if (!result.isSuccess()) {
                log.warn("任务执行失败, taskId={}, algorithm={}, errorCode={}, errorMessage={}",
                        result.getTaskId(),
                        algorithm != null ? algorithm.getName() : "unknown",
                        result.getErrorCode(),
                        result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("处理回调异常, taskId={}", callback.getBusinessTaskId(), e);
        }

        return ResponseEntity.ok("success");
    }

    @PostMapping("/progress")
    public ResponseEntity<String> handleProgressCallback(
            @RequestBody VisionCallbackResponse<JsonNode> callback) {

        AlgorithmEnum algorithm = converter.getAlgorithm(callback);

        log.info("收到视觉任务进度回调, taskId={}, algorithm={}, status={}",
                callback.getBusinessTaskId(),
                algorithm != null ? algorithm.getName() : callback.getAbility(),
                callback.getTaskStatusDescription());

        try {
            if (algorithm != null) {
                callbackHandler.handleProgress(
                        callback.getBusinessTaskId(),
                        algorithm,
                        callback.getTaskStatusDescription()
                );
            }
        } catch (Exception e) {
            log.error("处理进度回调异常, taskId={}", callback.getBusinessTaskId(), e);
        }

        return ResponseEntity.ok("success");
    }
}