package org.example.newyear.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.example.newyear.config.MultiOssConfig;
import org.example.newyear.config.MultiOssConfig.OssAccountConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OSS 客户端工厂
 *
 * 管理多个 OSS 账号的客户端实例
 *
 * @author Claude
 * @since 2026-02-05
 */
@Component
public class OssClientFactory {

    private final MultiOssConfig multiOssConfig;

    /**
     * OSS 客户端缓存
     * key: 账号标识
     * value: OSS 客户端包装器
     */
    private final Map<String, OssClientWrapper> clientCache = new ConcurrentHashMap<>();

    public OssClientFactory(MultiOssConfig multiOssConfig) {
        this.multiOssConfig = multiOssConfig;
    }

    /**
     * 初始化所有配置的 OSS 客户端
     */
    @PostConstruct
    public void init() {
        System.out.println("=== 初始化 OSS 客户端 ===");

        multiOssConfig.getAccounts().forEach((accountType, config) -> {
            System.out.println("初始化 OSS 账号: " + accountType);
            System.out.println("  Endpoint: " + config.getEndpoint());
            System.out.println("  Bucket: " + config.getBucket());

            OSS ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKeyId(),
                    config.getAccessKeySecret()
            );

            OssClientWrapper wrapper = new OssClientWrapper(ossClient, config);
            clientCache.put(accountType, wrapper);

            System.out.println("  状态: ✅ 初始化成功");
        });

        System.out.println("=== OSS 客户端初始化完成，共 " + clientCache.size() + " 个账号 ===");
    }

    /**
     * 获取指定账号的 OSS 客户端包装器
     */
    public OssClientWrapper getClient(String accountType) {
        OssClientWrapper wrapper = clientCache.get(accountType);
        if (wrapper == null) {
            throw new IllegalArgumentException("OSS 账号不存在: " + accountType + "，可用账号: " + clientCache.keySet());
        }
        return wrapper;
    }

    /**
     * 获取默认账号的 OSS 客户端包装器
     */
    public OssClientWrapper getDefaultClient() {
        return getClient("default");
    }

    /**
     * 销毁所有 OSS 客户端
     */
    @PreDestroy
    public void destroy() {
        System.out.println("=== 关闭 OSS 客户端 ===");
        clientCache.forEach((accountType, wrapper) -> {
            System.out.println("关闭 OSS 账号: " + accountType);
            wrapper.getOssClient().shutdown();
        });
        clientCache.clear();
        System.out.println("=== OSS 客户端已全部关闭 ===");
    }

    /**
     * OSS 客户端包装器
     * 包含客户端和配置信息
     */
    public static class OssClientWrapper {
        private final OSS ossClient;
        private final OssAccountConfig config;

        public OssClientWrapper(OSS ossClient, OssAccountConfig config) {
            this.ossClient = ossClient;
            this.config = config;
        }

        public OSS getOssClient() {
            return ossClient;
        }

        public OssAccountConfig getConfig() {
            return config;
        }

        public String getBucket() {
            return config.getBucket();
        }

        public String getEndpoint() {
            return config.getEndpoint();
        }

        public Boolean isPrivate() {
            return config.getPrivateAccess();
        }

        public Integer getSignedUrlExpire() {
            return config.getSignedUrlExpire();
        }
    }
}
