package org.example.newyear.dto.callback;

import lombok.Data;

import java.util.List;

/**
 * 歌曲特征提取回调DTO
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class SongFeatureExtractCallbackDTO {

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
     * 歌曲特征向量（成功时返回，float数组）
     */
    private List<Float> feature;

    /**
     * 错误信息（失败时返回）
     */
    private String errorMsg;

    /**
     * 时间戳（10位秒级）
     */
    private Long timestamp;
}
