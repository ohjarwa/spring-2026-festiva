package org.example.newyear.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "audio.feature-extraction")
public class FeatureExtractionConfig {
    
    /**
     * 服务地址（如 http://ip:port）
     */
    private String baseUrl;
    
    /**
     * 业务编号（source）
     */
    private String source;
    
    /**
     * 回调地址
     */
    private String callbackUrl;
}