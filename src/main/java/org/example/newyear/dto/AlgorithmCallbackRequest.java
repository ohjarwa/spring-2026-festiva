package org.example.newyear.dto;

import lombok.Data;

/**
 * 算法回调请求
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class AlgorithmCallbackRequest {

    /**
     * 回调唯一标识
     */
    private String callbackId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 回调类型: video_process=视频处理, audio_process=音频处理
     */
    private String callbackType;

    /**
     * 状态: success=成功, failed=失败
     */
    private String status;

    /**
     * 结果URL
     */
    private String resultUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 额外数据
     */
    private Object extraData;
}