package org.example.newyear.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 算法任务配置
 *
 * @author Claude
 * @since 2026-02-06
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "algorithm")
public class AlgorithmTaskConfig {

    /**
     * 统一回调URL（用于所有算法任务回调）
     */
    private String unifiedCallbackUrl;

    /**
     * 轮询配置
     */
    private PollConfig poll = new PollConfig();

    @Data
    public static class PollConfig {
        /**
         * 轮询间隔（秒）
         */
        private Integer intervalSeconds = 5;

        /**
         * 超时时间（秒）
         */
        private Integer timeoutSeconds = 1800; // 30分钟
    }
}
