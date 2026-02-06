package org.example.newyear.entity.task;

import lombok.Builder;
import lombok.Data;
import org.example.newyear.entity.enums.AlgorithmEnum;

import java.time.LocalDateTime;
import java.util.Map;
/**
 * 统一任务结果（存储到 Redis）
 */
@Data
@Builder
public class TaskResult {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 算法类型
     */
    private AlgorithmEnum algorithm;

    /**
     * 任务状态
     */
    private TaskResultStatus status;

    /**
     * 错误码
     */
    private Integer errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 结果数据（各算法特定字段）
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    /**
     * 业务透传数据
     */
    private String businessMessage;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 原始回调JSON（用于调试）
     */
    private String rawCallback;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status == TaskResultStatus.SUCCESS;
    }

    /**
     * 是否已完成（成功、失败、取消）
     */
    public boolean isCompleted() {
        return status == TaskResultStatus.SUCCESS
                || status == TaskResultStatus.FAILED
                || status == TaskResultStatus.CANCELLED
                || status == TaskResultStatus.TIMEOUT;
    }

    /**
     * 获取指定类型的数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> type) {
        if (data == null) return null;
        return (T) data.get(key);
    }

    /**
     * 获取视频URL（通用）
     */
    public String getVideoUrl() {
        String url = getData("videoUrl", String.class);
        if (url == null) url = getData("targetVideoUrl", String.class);
        return url;
    }

    /**
     * 获取图片URL（通用）
     */
    public String getImageUrl() {
        String url = getData("imageUrl", String.class);
        if (url == null) url = getData("targetImageUrl", String.class);
        return url;
    }

    /**
     * 获取音频URL（通用）
     */
    public String getAudioUrl() {
        String url = getData("audioUrl", String.class);
        if (url == null) url = getData("resultUrl", String.class);
        if (url == null) url = getData("allUrl", String.class);
        return url;
    }
}