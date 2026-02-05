package org.example.newyear.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 回调产物管理器
 * 管理算法回调产物的存储和获取
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackResultManager {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CALLBACK_CACHE_PREFIX = "callback:";
    private static final int CACHE_TTL = 3600; // 1小时

    /**
     * 保存回调产物
     *
     * @param recordId  记录ID
     * @param stepName  步骤名称（face_swap, voice_clone等）
     * @param result    回调结果Map，包含success标志和产物数据
     */
    public void saveResult(String recordId, String stepName, Map<String, Object> result) {
        String cacheKey = CALLBACK_CACHE_PREFIX + recordId + ":" + stepName;

        log.info("保存回调产物: recordId={}, stepName={}, resultKeys={}",
                recordId, stepName, result.keySet());

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
    }

    /**
     * 获取回调产物
     *
     * @param recordId 记录ID
     * @param stepName 步骤名称
     * @return 回调结果Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResult(String recordId, String stepName) {
        String cacheKey = CALLBACK_CACHE_PREFIX + recordId + ":" + stepName;
        Object result = redisTemplate.opsForValue().get(cacheKey);

        if (result == null) {
            throw new RuntimeException("未找到回调产物: recordId=" + recordId + ", stepName=" + stepName);
        }

        return (Map<String, Object>) result;
    }

    /**
     * 获取回调产物中的具体字段
     *
     * @param recordId 记录ID
     * @param stepName 步骤名称
     * @param key      字段名
     * @return 字段值
     */
    public Object getResultField(String recordId, String stepName, String key) {
        Map<String, Object> result = getResult(recordId, stepName);
        return result.get(key);
    }

    /**
     * 检查回调是否成功
     */
    public boolean isSuccess(String recordId, String stepName) {
        try {
            Map<String, Object> result = getResult(recordId, stepName);
            Boolean success = (Boolean) result.get("success");
            return success != null && success;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取产物URL（通用）
     */
    public String getResultUrl(String recordId, String stepName) {
        Map<String, Object> result = getResult(recordId, stepName);

        // 尝试不同的字段名
        Object url = result.get("targetVideoUrl");
        if (url == null) url = result.get("videoUrl");
        if (url == null) url = result.get("audioUrl");
        if (url == null) url = result.get("resultUrl");

        return (String) url;
    }

    /**
     * 获取产物ID（voice_id等）
     */
    public String getResultId(String recordId, String stepName) {
        Map<String, Object> result = getResult(recordId, stepName);

        // 尝试不同的字段名
        Object id = result.get("voiceId");
        if (id == null) id = result.get("taskId");

        return id != null ? id.toString() : null;
    }

    /**
     * 删除回调产物
     */
    public void deleteResult(String recordId, String stepName) {
        String cacheKey = CALLBACK_CACHE_PREFIX + recordId + ":" + stepName;
        redisTemplate.delete(cacheKey);
        log.debug("删除回调产物: recordId={}, stepName={}", recordId, stepName);
    }
}
