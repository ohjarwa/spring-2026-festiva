package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WanAnimateRequest {

    /**
     * 任务模式
     * 必填，当前只支持 replace_body 和 pose2v
     */
    private String taskMode;

    /**
     * 图片URL列表
     * 必填，最少一张图
     */
    private List<String> images;

    /**
     * 驱动视频URL
     * 必填，720P视频时长不能超过15s，480P不超过60s
     */
    private String video;

    /**
     * 模板类型
     * 可选，可选值: platform, user，默认为 user
     */
    private String templateType;

    /**
     * 推理种子值
     * 可选，默认使用 42
     */
    private Integer seed;

    /**
     * 生成视频的分辨率
     * 可选，可选值: 480P, 720P，默认为 480P
     */
    private String resolution;

    /**
     * 业务透传数据（框架支持）
     */
    private String businessMessage;

    /**
     * 任务复杂度（框架支持，可选）
     */
    private TaskComplexity taskComplexity;
}