package org.example.newyear.dto.callback;

import lombok.Data;

/**
 * 视频处理回调DTO（人脸替换、唇形同步）
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class VideoProcessCallbackDTO {

    /**
     * 回调唯一标识（用户在请求时生成）
     */
    private String callbackId;

    /**
     * 任务ID（用户在请求时生成）
     */
    private String taskId;

    /**
     * 状态: success=成功, failed=失败
     */
    private String status;

    /**
     * 处理后的视频URL（成功时返回）
     */
    private String videoUrl;

    /**
     * 错误信息（失败时返回）
     */
    private String errorMsg;

    /**
     * 时间戳（10位秒级）
     */
    private Long timestamp;
}
