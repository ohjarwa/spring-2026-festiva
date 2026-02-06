package org.example.newyear.common;

/**
 * 常量定义
 *
 * @author Claude
 * @since 2026-02-04
 */
public class Constants {

    /**
     * 活动类型
     */
    public static final int ACTIVITY_TYPE_SPRING_2026 = 1;

    /**
     * 默认每日配额
     */
    public static final int DEFAULT_DAILY_QUOTA = 10;

    /**
     * 默认最大重试次数
     */
    public static final int DEFAULT_MAX_RETRY = 3;

    /**
     * 用户ID请求头
     */
    public static final String HEADER_USER_ID = "X-User-UUID";

    /**
     * 签名请求头
     */
    public static final String HEADER_SIGNATURE = "X-Signature";

    /**
     * 时间戳请求头
     */
    public static final String HEADER_TIMESTAMP = "X-Timestamp";

    /**
     * API密钥请求头
     */
    public static final String HEADER_API_SECRET = "X-API-Secret";

    /**
     * Redis Key前缀
     */
    public static final String REDIS_KEY_CALLBACK = "callback:";

    public static final String REDIS_KEY_USER_QUOTA = "user:quota:";

    /**
     * OSS签名URL有效期（秒）
     */
    public static final int OSS_SIGNED_URL_EXPIRE = 600;

    /**
     * 时间戳容差（毫秒）
     */
    public static final long TIMESTAMP_TOLERANCE = 300000L;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 50;

    /**
     * 支持的图片格式
     */
    public static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    /**
     * 支持的音频格式
     */
    public static final String[] AUDIO_EXTENSIONS = {".mp3", ".wav", ".aac", ".m4a"};

    /**
     * 文件大小限制（字节）
     */
    public static final long MAX_IMAGE_SIZE = 50 * 1024 * 1024;  // 50MB

    public static final long MAX_AUDIO_SIZE = 50 * 1024 * 1024;  // 50MB
}
