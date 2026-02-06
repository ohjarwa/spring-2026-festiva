package org.example.newyear.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.util.TaskRedisKey;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTaskResultStore implements TaskResultStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(String taskId, AlgorithmEnum algorithm, TaskResult result) {
        save(taskId, algorithm, result, Duration.ofSeconds(TaskRedisKey.DEFAULT_EXPIRE_SECONDS));
    }

    @Override
    public void save(String taskId, AlgorithmEnum algorithm, TaskResult result, Duration timeout) {
        String key = TaskRedisKey.resultKey(taskId, algorithm);
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, json, timeout);
            log.debug("保存任务结果, key={}, status={}", key, result.getStatus());
        } catch (JsonProcessingException e) {
            log.error("序列化任务结果失败, taskId={}, algorithm={}", taskId, algorithm.getName(), e);
            throw new RuntimeException("序列化任务结果失败", e);
        }
    }

    @Override
    public Optional<TaskResult> get(String taskId, AlgorithmEnum algorithm) {
        String key = TaskRedisKey.resultKey(taskId, algorithm);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }

        try {
            TaskResult result = objectMapper.readValue(json, TaskResult.class);
            return Optional.of(result);
        } catch (JsonProcessingException e) {
            log.error("反序列化任务结果失败, key={}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String taskId, AlgorithmEnum algorithm) {
        String key = TaskRedisKey.resultKey(taskId, algorithm);
        redisTemplate.delete(key);
        log.debug("删除任务结果, key={}", key);
    }

    @Override
    public boolean exists(String taskId, AlgorithmEnum algorithm) {
        String key = TaskRedisKey.resultKey(taskId, algorithm);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void updateStatus(String taskId, AlgorithmEnum algorithm, TaskResultStatus status) {
        get(taskId, algorithm).ifPresent(result -> {
            result.setStatus(status);
            save(taskId, algorithm, result);
        });
    }

    @Override
    public void saveProgress(String taskId, AlgorithmEnum algorithm, String progress) {
        String key = TaskRedisKey.progressKey(taskId, algorithm);
        redisTemplate.opsForValue().set(key, progress,
                Duration.ofSeconds(TaskRedisKey.DEFAULT_EXPIRE_SECONDS));
    }

    @Override
    public Optional<String> getProgress(String taskId, AlgorithmEnum algorithm) {
        String key = TaskRedisKey.progressKey(taskId, algorithm);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }
}