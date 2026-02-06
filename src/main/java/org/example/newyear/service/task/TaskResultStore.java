package org.example.newyear.service.task;

import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;

import java.time.Duration;
import java.util.Optional;

public interface TaskResultStore {

    /**
     * 保存任务结果
     */
    void save(String taskId, AlgorithmEnum algorithm, TaskResult result);

    /**
     * 保存任务结果（指定过期时间）
     */
    void save(String taskId, AlgorithmEnum algorithm, TaskResult result, Duration timeout);

    /**
     * 获取任务结果
     */
    Optional<TaskResult> get(String taskId, AlgorithmEnum algorithm);

    /**
     * 删除任务结果
     */
    void delete(String taskId, AlgorithmEnum algorithm);

    /**
     * 判断任务结果是否存在
     */
    boolean exists(String taskId, AlgorithmEnum algorithm);

    /**
     * 更新任务状态
     */
    void updateStatus(String taskId, AlgorithmEnum algorithm, TaskResultStatus status);

    /**
     * 保存任务进度
     */
    void saveProgress(String taskId, AlgorithmEnum algorithm, String progress);

    /**
     * 获取任务进度
     */
    Optional<String> getProgress(String taskId, AlgorithmEnum algorithm);
}