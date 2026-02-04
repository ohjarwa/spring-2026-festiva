package org.example.newyear.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

/**
 * 阿里云OSS工具类
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Component
public class OssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.signed-url-expire:600}")
    private int signedUrlExpire;

    /**
     * 获取OSS客户端
     */
    private OSS getOssClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 上传文件到OSS
     *
     * @param file     文件
     * @param objectKey 对象Key（路径）
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String objectKey) throws IOException {
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(bucket, objectKey, file.getInputStream());
            // 返回公网访问地址
            return "https://" + bucket + "." + endpoint + "/" + objectKey;
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成签名URL
     *
     * @param objectKey 对象Key
     * @return 签名URL
     */
    public String generateSignedUrl(String objectKey) {
        return generateSignedUrl(objectKey, signedUrlExpire);
    }

    /**
     * 生成签名URL（指定过期时间）
     *
     * @param objectKey        对象Key
     * @param expireInSeconds  过期时间（秒）
     * @return 签名URL
     */
    public String generateSignedUrl(String objectKey, int expireInSeconds) {
        OSS ossClient = getOssClient();
        try {
            Date expiration = Date.from(Instant.now().plusSeconds(expireInSeconds));

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey);
            request.setExpiration(expiration);
            request.setMethod(com.aliyun.oss.HttpMethod.GET);

            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 根据URL提取objectKey
     */
    public String extractObjectKey(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        // 去掉协议前缀
        String cleanUrl = url.replaceFirst("^https?://", "");

        // 去掉bucket和endpoint
        String prefix = bucket + "." + endpoint + "/";
        if (cleanUrl.startsWith(prefix)) {
            return cleanUrl.substring(prefix.length());
        }

        // 如果是签名URL，提取?之前的部分
        int queryIndex = cleanUrl.indexOf('?');
        String pathPart = queryIndex > 0 ? cleanUrl.substring(0, queryIndex) : cleanUrl;

        // 去掉域名部分
        int lastSlashIndex = pathPart.indexOf('/');
        if (lastSlashIndex > 0) {
            return pathPart.substring(lastSlashIndex + 1);
        }

        return pathPart;
    }
}