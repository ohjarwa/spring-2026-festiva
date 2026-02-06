package org.example.newyear.entity.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskResultStatus {
    
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    TIMEOUT(4, "超时"),
    CANCELLED(5, "已取消");
    
    private final int code;
    private final String description;
}