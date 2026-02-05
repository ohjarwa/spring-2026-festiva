package org.example.newyear.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 多 OSS 账号配置
 *
 * 支持配置多个 OSS 账号，根据业务需求切换
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class MultiOssConfig {

    /**
     * 多个 OSS 账号配置
     * key: 账号标识（如 default, secondary）
     * value: OSS 配置信息
     */
    private Map<String, OssAccountConfig> accounts = new HashMap<>();

    /**
     * 单个 OSS 账号配置
     */
    @Data
    public static class OssAccountConfig {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucket;
        private Boolean privateAccess = true;
        private Integer signedUrlExpire = 600;
    }

    /**
     * 获取指定账号配置
     */
    public OssAccountConfig getAccount(String accountType) {
        OssAccountConfig config = accounts.get(accountType);
        if (config == null) {
            throw new IllegalArgumentException("OSS 账号配置不存在: " + accountType);
        }
        return config;
    }

    /**
     * 获取默认账号配置
     */
    public OssAccountConfig getDefaultAccount() {
        return getAccount("default");
    }
}
