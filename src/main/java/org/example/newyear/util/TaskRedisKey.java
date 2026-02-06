package org.example.newyear.util;

import org.example.newyear.entity.enums.AlgorithmEnum;

/**
 * Redis Key 常量
 */
public class TaskRedisKey {
    
    /**
     * 任务结果前缀
     * 完整 key: task:result:{taskId}
     */
    public static final String TASK_RESULT_PREFIX = "activity2026:result:";
    
    /**
     * 任务进度前缀
     * 完整 key: task:progress:{taskId}
     */
    public static final String TASK_PROGRESS_PREFIX = "activity2026:progress:";
    
    /**
     * 默认过期时间（秒）- 24小时
     */
    public static final long DEFAULT_EXPIRE_SECONDS = 24 * 60 * 60;

    /**
     * 获取任务结果 Key
     */
    public static String resultKey(String taskId, AlgorithmEnum algorithm) {
        return TASK_RESULT_PREFIX + algorithm.getName() + ":" + taskId;
    }

    /**
     * 获取任务结果 Key（字符串形式）
     */
    public static String resultKey(String taskId, String algorithmName) {
        return TASK_RESULT_PREFIX + algorithmName + ":" + taskId;
    }

    /**
     * 获取任务进度 Key
     */
    public static String progressKey(String taskId, AlgorithmEnum algorithm) {
        return TASK_PROGRESS_PREFIX + algorithm.getName() + ":" + taskId;
    }

    /**
     * 获取任务进度 Key（字符串形式）
     */
    public static String progressKey(String taskId, String algorithmName) {
        return TASK_PROGRESS_PREFIX + algorithmName + ":" + taskId;
    }
}