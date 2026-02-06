package org.example.newyear.service.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionCallbackData;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionCallbackResponse;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FeatureExtractionCallbackConverter 
        implements CallbackResultConverter<FeatureExtractionCallbackResponse> {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public AlgorithmEnum getAlgorithm(FeatureExtractionCallbackResponse callback) {
        return AlgorithmEnum.VOICE_CONVERSION;
    }
    
    @Override
    public TaskResult convert(FeatureExtractionCallbackResponse callback) {
        TaskResultStatus status = callback.isSuccess()
            ? TaskResultStatus.SUCCESS 
            : TaskResultStatus.FAILED;

        String taskId = null;

        FeatureExtractionCallbackData data = callback.getData();

        String rawCallback = serializeCallback(callback);
        
        return TaskResult.builder()
            .taskId(taskId)
            .algorithm(AlgorithmEnum.VOICE_CONVERSION)
            .status(status)
            .errorCode(callback.getCode())
            .errorMessage(callback.getMsg())
            .data(objectMapper.valueToTree(data))
            .callbackTime(LocalDateTime.now())
            .rawCallback(rawCallback)
            .build();
    }
    
    private String serializeCallback(Object callback) {
        try {
            return objectMapper.writeValueAsString(callback);
        } catch (Exception e) {
            return null;
        }
    }
}