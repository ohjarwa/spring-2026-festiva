package org.example.newyear.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * 任务结果轮询器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskResultPoller {

    private final TaskResultStore taskResultStore;

    private static final long DEFAULT_POLL_INTERVAL_MS = 500;
    private static final long DEFAULT_TIMEOUT_SECONDS = 1800;

    /**
     * 等待任务完成（阻塞方式）
     */
    public TaskResult waitForCompletion(String taskId, AlgorithmEnum algorithm, Duration timeout)
            throws TimeoutException {

        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout.toMillis();

        log.debug("开始等待任务完成, taskId={}, algorithm={}, timeout={}ms",
                taskId, algorithm.getName(), timeoutMs);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Optional<TaskResult> result = taskResultStore.get(taskId, algorithm);

            if (result.isPresent() && result.get().isCompleted()) {
                log.debug("任务完成, taskId={}, algorithm={}, status={}",
                        taskId, algorithm.getName(), result.get().getStatus());
                return result.get();
            }

            try {
                Thread.sleep(DEFAULT_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待任务结果被中断", e);
            }
        }

        throw new TimeoutException(String.format(
                "等待任务结果超时, taskId=%s, algorithm=%s", taskId, algorithm.getName()));
    }

    /**
     * 等待任务完成（默认超时5分钟）
     */
    public TaskResult waitForCompletion(String taskId, AlgorithmEnum algorithm)
            throws TimeoutException {
        return waitForCompletion(taskId, algorithm, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
    }

    /**
     * 异步等待任务完成
     */
    public CompletableFuture<TaskResult> waitForCompletionAsync(
            String taskId, AlgorithmEnum algorithm, Duration timeout) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return waitForCompletion(taskId, algorithm, timeout);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 获取任务结果（非阻塞）
     */
    public Optional<TaskResult> poll(String taskId, AlgorithmEnum algorithm) {
        return taskResultStore.get(taskId, algorithm);
    }

    /**
     * 检查任务是否完成
     */
    public boolean isCompleted(String taskId, AlgorithmEnum algorithm) {
        return taskResultStore.get(taskId, algorithm)
                .map(TaskResult::isCompleted)
                .orElse(false);
    }

    /**
     * 检查任务是否成功
     */
    public boolean isSuccess(String taskId, AlgorithmEnum algorithm) {
        return taskResultStore.get(taskId, algorithm)
                .map(TaskResult::isSuccess)
                .orElse(false);
    }

    /**
     * 带自定义间隔的等待
     */
    public TaskResult waitWithInterval(String taskId, AlgorithmEnum algorithm,
                                       Duration timeout, Duration pollInterval) throws TimeoutException {

        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout.toMillis();
        long intervalMs = pollInterval.toMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Optional<TaskResult> result = taskResultStore.get(taskId, algorithm);

            if (result.isPresent() && result.get().isCompleted()) {
                return result.get();
            }

            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待任务结果被中断", e);
            }
        }

        throw new TimeoutException(String.format(
                "等待任务结果超时, taskId=%s, algorithm=%s", taskId, algorithm.getName()));
    }
}
