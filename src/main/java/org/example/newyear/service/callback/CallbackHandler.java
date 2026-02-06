package org.example.newyear.service.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.service.task.TaskResultStore;
import org.springframework.stereotype.Service;

/**
 * 统一回调处理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackHandler {

    private final TaskResultStore taskResultStore;

    /**
     * 处理回调并存储结果
     */
    public <T> TaskResult handleCallback(T callback, CallbackResultConverter<T> converter) {
        TaskResult result = converter.convert(callback);
        AlgorithmEnum algorithm = converter.getAlgorithm(callback);

        if (result.getTaskId() == null || result.getTaskId().isEmpty()) {
            log.warn("回调数据缺少 taskId, 无法存储, algorithm={}",
                    algorithm != null ? algorithm.getName() : "unknown");
            return result;
        }

        if (algorithm == null) {
            log.warn("无法识别算法类型, taskId={}", result.getTaskId());
            return result;
        }

        taskResultStore.save(result.getTaskId(), algorithm, result);

        log.info("回调处理完成, taskId={}, algorithm={}, status={}",
                result.getTaskId(), algorithm.getName(), result.getStatus());

        return result;
    }

    /**
     * 处理进度回调
     */
    public void handleProgress(String taskId, AlgorithmEnum algorithm, String progress) {
        taskResultStore.saveProgress(taskId, algorithm, progress);
        log.debug("进度更新, taskId={}, algorithm={}, progress={}",
                taskId, algorithm.getName(), progress);
    }
}