package org.example.newyear.service.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.newyear.dto.algorithm.audio.SongConversionCallbackResponse;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Component
@RequiredArgsConstructor
public class SongConversionCallbackConverter
        implements CallbackResultConverter<SongConversionCallbackResponse> {

    private final ObjectMapper objectMapper;

    @Override
    public AlgorithmEnum getAlgorithm(SongConversionCallbackResponse callback) {
        return AlgorithmEnum.SONG_CONVERSION;
    }

    @Override
    public TaskResult convert(SongConversionCallbackResponse callback) {
        TaskResultStatus status = callback.isSuccess()
                ? TaskResultStatus.SUCCESS
                : TaskResultStatus.FAILED;

        String taskId = null;

        if (callback.getData() != null) {
            taskId = callback.getData().getTaskId();
        }

        String rawCallback = serializeCallback(callback);

        return TaskResult.builder()
                .taskId(taskId)
                .algorithm(AlgorithmEnum.SONG_CONVERSION)
                .status(status)
                .errorCode(callback.getCode())
                .errorMessage(callback.getMsg())
                .data(objectMapper.valueToTree(callback.getData()))
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