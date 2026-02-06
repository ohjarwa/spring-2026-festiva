package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VisionRequestHeaders {
    private String appId;
    private String appSecret;
    private String ability;      // 区分不同算法
    private String taskId;
    private String callbackUrl;
    private String progressCallbackUrl;
    private String traceId;
    private String tags;         // 如 "activity2026"
    private String group;
    private Integer position;
}