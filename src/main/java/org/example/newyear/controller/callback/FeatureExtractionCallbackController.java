package org.example.newyear.controller.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionCallbackResponse;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.service.callback.CallbackHandler;
import org.example.newyear.service.callback.FeatureExtractionCallbackConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/audio/callback")
@RequiredArgsConstructor
public class FeatureExtractionCallbackController {
    
    private final CallbackHandler callbackHandler;
    private final FeatureExtractionCallbackConverter converter;
    
    /**
     * 特征提取结果回调
     */
    @PostMapping("/feature-extraction")
    public ResponseEntity<String> handleCallback(
            @RequestBody FeatureExtractionCallbackResponse callback) {
        
        log.info("收到特征提取回调, code={}, msg={}", callback.getCode(), callback.getMsg());
        
        try {
            TaskResult result = callbackHandler.handleCallback(callback, converter);
            
            if (result.isSuccess()) {
                String featureId = result.getData("featureId", String.class);
                log.info("特征提取成功, taskId={}, featureId={}", 
                    result.getTaskId(), featureId);
            } else {
                log.warn("特征提取失败, taskId={}, errorCode={}, errorMessage={}", 
                    result.getTaskId(), result.getErrorCode(), result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("处理回调异常", e);
        }
        
        return ResponseEntity.ok("success");
    }
}