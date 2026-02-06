package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class WanVideoFLFRequest {
    /**
     * 提示词
     * 必填，可以为空字符串
     */
    private String prompt;

    /**
     * 生成视频的宽
     * 必填，支持 832(480P) 或 1280(720P)，默认 832
     */
    private Integer width;

    /**
     * 生成视频的高
     * 必填，支持 480(480P) 或 720(720P)，默认 480
     */
    private Integer height;

    /**
     * 首帧图URL
     * 必填，图片的 http url 地址
     */
    private String firstImage;

    /**
     * 尾帧图URL
     * 必填，图片的 http url 地址
     */
    private String lastImage;

    /**
     * 随机种子
     * 非必填，默认为 -1
     */
    private Integer seed;

    /**
     * 扩展参数
     * 非必填
     */
    private Map<String, Object> extParams;

    /**
     * 业务透传数据（框架支持）
     */
    private String businessMessage;

    /**
     * 任务复杂度（框架支持，可选）
     */
    private TaskComplexity taskComplexity;
}