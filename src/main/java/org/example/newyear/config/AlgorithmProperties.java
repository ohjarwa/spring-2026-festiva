package org.example.newyear.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 算法服务配置
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
@Component
@ConfigurationProperties(prefix = "algorithm")
public class AlgorithmProperties {

    /**
     * 人脸替换服务配置
     */
    private FaceSwapConfig faceSwap = new FaceSwapConfig();

    /**
     * 唇形同步服务配置
     */
    private LipSyncConfig lipSync = new LipSyncConfig();

    /**
     * 声音克隆服务配置
     */
    private VoiceCloneConfig voiceClone = new VoiceCloneConfig();

    /**
     * 声音合成服务配置
     */
    private VoiceTtsConfig voiceTts = new VoiceTtsConfig();

    /**
     * 多图生图服务配置
     */
    private MultiImageGenerateConfig multiImageGenerate = new MultiImageGenerateConfig();

    @Data
    public static class FaceSwapConfig {
        /**
         * 服务地址
         */
        private String url = "http://face-swap-service.com";

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 超时时间（毫秒）
         */
        private Integer timeout = 30000;
    }

    @Data
    public static class LipSyncConfig {
        /**
         * 服务地址
         */
        private String url = "http://lip-sync-service.com";

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 超时时间（毫秒）
         */
        private Integer timeout = 30000;
    }

    @Data
    public static class VoiceCloneConfig {
        /**
         * 服务地址
         */
        private String url = "http://voice-clone-service.com";

        /**
         * 超时时间（毫秒）
         */
        private Integer timeout = 60000;
    }

    @Data
    public static class VoiceTtsConfig {
        /**
         * 服务地址
         */
        private String url = "http://voice-tts-service.com";

        /**
         * 超时时间（毫秒）
         */
        private Integer timeout = 60000;
    }

    @Data
    public static class MultiImageGenerateConfig {
        /**
         * 服务地址
         */
        private String url = "http://multi-image-generate-service.com";

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 超时时间（毫秒）
         */
        private Integer timeout = 120000;  // 2分钟（图生图较慢）
    }
}