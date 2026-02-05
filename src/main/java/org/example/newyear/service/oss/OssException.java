package org.example.newyear.service.oss;

/**
 * OSS 服务异常
 *
 * @author Claude
 * @since 2026-02-05
 */
public class OssException extends RuntimeException {

    public OssException(String message) {
        super(message);
    }

    public OssException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 文件类型不支持
     */
    public static OssException unsupportedFileType(String filename) {
        return new OssException("不支持的文件类型: " + filename);
    }

    /**
     * 文件大小超限
     */
    public static OssException fileSizeExceeded(long size, long maxSize) {
        return new OssException(String.format("文件大小超限: %d bytes (最大: %d bytes)", size, maxSize));
    }

    /**
     * 上传失败
     */
    public static OssException uploadFailed(String message, Throwable cause) {
        return new OssException("文件上传失败: " + message, cause);
    }

    /**
     * 文件不存在
     */
    public static OssException fileNotFound(String fileKey) {
        return new OssException("文件不存在: " + fileKey);
    }
}
