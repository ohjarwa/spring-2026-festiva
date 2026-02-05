package org.example.newyear.service.oss;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 上传结果
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadResult {

    /**
     * 文件标识（用于后续获取URL、删除等操作）
     * 例如：spring2026/user123/image/20260205/abc123.jpg
     */
    private String fileKey;

    /**
     * 访问URL（已根据Bucket类型生成签名URL或公共URL）
     */
    private String accessUrl;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（MIME类型）
     */
    private String contentType;

    /**
     * 上传时间戳
     */
    private Long uploadTime;
}
