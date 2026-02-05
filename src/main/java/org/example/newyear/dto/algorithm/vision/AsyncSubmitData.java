package org.example.newyear.dto.algorithm.vision;

import lombok.Data;

@Data
public class AsyncSubmitData {
    /**
     * 当前任务在队列中的位置
     */
    private Integer queuePosition;

    /**
     * 预计等待时间（毫秒）
     */
    private Long estimatedDurationMs;
}