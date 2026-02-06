package org.example.newyear.service.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.newyear.dto.algorithm.vision.VisionCallbackResponse;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.enums.VisionTaskStatus;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class VisionCallbackConverter
        implements CallbackResultConverter<VisionCallbackResponse<JsonNode>> {

    private final ObjectMapper objectMapper;

    @Override
    public AlgorithmEnum getAlgorithm(VisionCallbackResponse<JsonNode> callback) {
        return AlgorithmEnum.fromVisionAbility(callback.getAbility());
    }

    @Override
    public TaskResult convert(VisionCallbackResponse<JsonNode> callback) {
        AlgorithmEnum algorithm = getAlgorithm(callback);
        TaskResultStatus status = convertStatus(callback.getTaskStatus());

        Map<String, Object> data = new HashMap<>();
        Integer algorithmCode = null;
        String algorithmMessage = null;

        JsonNode dataNode = callback.getData();
        if (dataNode != null) {
            if (dataNode.has("code")) {
                algorithmCode = dataNode.get("code").asInt();
            }
            if (dataNode.has("message")) {
                algorithmMessage = dataNode.get("message").asText();
            }

            // 提取各类URL
            extractField(dataNode, "videoUrl", data);
            extractField(dataNode, "targetVideoUrl", data);
            extractField(dataNode, "targetImageUrl", data);
            extractField(dataNode, "alphaVideoUrl", data);
            extractField(dataNode, "resultInfo", data);
        }

        // 框架层成功但算法层失败
        if (status == TaskResultStatus.SUCCESS && algorithmCode != null
                && (algorithmCode < 200 || algorithmCode >= 400)) {
            status = TaskResultStatus.FAILED;
        }

        String rawCallback = serializeCallback(callback);

        return TaskResult.builder()
                .taskId(callback.getBusinessTaskId())
                .algorithm(algorithm)
                .status(status)
                .errorCode(algorithmCode != null ? algorithmCode : callback.getCode())
                .errorMessage(algorithmMessage != null ? algorithmMessage : callback.getMessage())
                .data(data)
                .businessMessage(callback.getBusinessMessage())
                .callbackTime(LocalDateTime.now())
                .rawCallback(rawCallback)
                .build();
    }

    private TaskResultStatus convertStatus(Integer taskStatus) {
        if (taskStatus == null) return TaskResultStatus.PENDING;

        VisionTaskStatus status = VisionTaskStatus.of(taskStatus);
        if (status == null) return TaskResultStatus.PENDING;

        switch (status) {
            case QUEUED:
            case STARTED:
                return TaskResultStatus.PROCESSING;
            case EXECUTE_SUCCESS:
                return TaskResultStatus.SUCCESS;
            case EXECUTE_FAILED:
                return TaskResultStatus.FAILED;
            case CANCELLED:
                return TaskResultStatus.CANCELLED;
            default:
                return TaskResultStatus.PENDING;
        }
    }

    private void extractField(JsonNode node, String field, Map<String, Object> data) {
        if (node.has(field) && !node.get(field).isNull()) {
            if (node.get(field).isObject()) {
                data.put(field, node.get(field));
            } else {
                data.put(field, node.get(field).asText());
            }
        }
    }

    private String serializeCallback(Object callback) {
        try {
            return objectMapper.writeValueAsString(callback);
        } catch (Exception e) {
            return null;
        }
    }
}