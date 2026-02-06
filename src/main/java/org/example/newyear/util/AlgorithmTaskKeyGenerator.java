package org.example.newyear.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 算法任务Key生成器
 *
 * 用于生成算法任务的唯一标识key，格式：recordId:stepName:uuid
 *
 * @author Claude
 * @since 2026-02-06
 */
@Slf4j
@Component
public class AlgorithmTaskKeyGenerator {

    /**
     * 生成算法任务key
     *
     * @param recordId 记录ID
     * @param stepName 步骤名称（如：flux2_image_gen、wan_animate_face_swap_0、lipsync等）
     * @return 任务key，格式：recordId:stepName:uuid
     */
    public String generateKey(String recordId, String stepName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String key = recordId + ":" + stepName + ":" + uuid;
        log.debug("生成算法任务key: recordId={}, stepName={}, key={}", recordId, stepName, key);
        return key;
    }

    /**
     * 从key中提取recordId
     *
     * @param key 任务key
     * @return recordId
     */
    public String extractRecordId(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 1) {
            return parts[0];
        }
        throw new IllegalArgumentException("Invalid key format: " + key);
    }

    /**
     * 从key中提取stepName
     *
     * @param key 任务key
     * @return stepName
     */
    public String extractStepName(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new IllegalArgumentException("Invalid key format: " + key);
    }

    /**
     * 从key中提取uuid
     *
     * @param key 任务key
     * @return uuid
     */
    public String extractUuid(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 3) {
            return parts[2];
        }
        throw new IllegalArgumentException("Invalid key format: " + key);
    }
}
