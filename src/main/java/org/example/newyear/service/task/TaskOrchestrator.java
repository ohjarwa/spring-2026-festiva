package org.example.newyear.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * 任务编排辅助工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOrchestrator {
    
    private final TaskResultStore taskResultStore;
    private final TaskResultPoller taskResultPoller;
    
    /**
     * 初始化任务状态（提交任务后调用）
     */
    public void initTask(String taskId, AlgorithmEnum algorithm) {
        TaskResult pending = TaskResult.builder()
            .taskId(taskId)
            .algorithm(algorithm)
            .status(TaskResultStatus.PENDING)
            .build();
        taskResultStore.save(taskId, algorithm, pending);
        log.debug("初始化任务状态, taskId={}, algorithm={}", taskId, algorithm.getName());
    }
    
    /**
     * 等待单个任务完成
     */
    public TaskResult awaitTask(String taskId, AlgorithmEnum algorithm, Duration timeout)
            throws TimeoutException {
        return taskResultPoller.waitForCompletion(taskId, algorithm, timeout);
    }
    
    /**
     * 等待单个任务完成（默认5分钟超时）
     */
    public TaskResult awaitTask(String taskId, AlgorithmEnum algorithm) throws TimeoutException {
        return taskResultPoller.waitForCompletion(taskId, algorithm);
    }
    
    /**
     * 等待任务完成并执行回调
     */
    public void awaitTaskWithCallback(String taskId, AlgorithmEnum algorithm, Duration timeout,
                                      Consumer<TaskResult> onSuccess, Consumer<TaskResult> onFailure) {
        
        try {
            TaskResult result = awaitTask(taskId, algorithm, timeout);
            
            if (result.isSuccess()) {
                onSuccess.accept(result);
            } else {
                onFailure.accept(result);
            }
        } catch (TimeoutException e) {
            TaskResult timeoutResult = TaskResult.builder()
                .taskId(taskId)
                .algorithm(algorithm)
                .status(TaskResultStatus.TIMEOUT)
                .errorMessage("任务执行超时")
                .build();
            onFailure.accept(timeoutResult);
        }
    }
    
    /**
     * 非阻塞检查任务状态
     */
    public TaskResult checkTask(String taskId, AlgorithmEnum algorithm) {
        return taskResultPoller.poll(taskId, algorithm).orElse(null);
    }
    
    /**
     * 检查任务是否完成
     */
    public boolean isTaskCompleted(String taskId, AlgorithmEnum algorithm) {
        return taskResultPoller.isCompleted(taskId, algorithm);
    }
    
    /**
     * 检查任务是否成功
     */
    public boolean isTaskSuccess(String taskId, AlgorithmEnum algorithm) {
        return taskResultPoller.isSuccess(taskId, algorithm);
    }
    
    /**
     * 清理任务结果
     */
    public void cleanupTask(String taskId, AlgorithmEnum algorithm) {
        taskResultStore.delete(taskId, algorithm);
    }
}