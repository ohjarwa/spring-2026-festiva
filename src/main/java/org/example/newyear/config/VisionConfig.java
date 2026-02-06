package org.example.newyear.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vision")
public class VisionConfig {

    /**
     * 图像平台分配的 App-Id
     */
    private String appId;

    /**
     * 图像平台分配的 App-Secret
     */
    private String appSecret;

    /**
     * 结果回调地址
     */
    private String callbackUrl;

    /**
     * 进度回调地址
     */
    private String progressCallbackUrl;

    /**
     * 任务标签，本次活动统一为 activity2026
     */
    private String tags = "activity2026";
}