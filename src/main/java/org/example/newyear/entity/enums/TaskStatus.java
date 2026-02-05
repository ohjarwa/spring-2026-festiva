package org.example.newyear.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    
    QUEUED(1, "QUEUED", "任务提交"),
    STARTED(2, "STARTED", "任务开始执行"),
    EXECUTE_SUCCESS(3, "EXECUTE_SUCCESS", "任务执行成功"),
    EXECUTE_FAILED(4, "EXECUTE_FAILED", "任务执行失败"),
    CANCELLED(5, "CANCELLED", "任务取消");
    
    private final int code;
    private final String name;
    private final String description;
    
    public static TaskStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}