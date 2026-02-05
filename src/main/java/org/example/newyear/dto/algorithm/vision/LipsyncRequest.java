package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LipsyncRequest {
    /**
     * 视频链接
     * 必填
     */
    private String srcVideoUrl;

    /**
     * 原声音频URL
     * 必填
     */
    private String audioUrl;

    /**
     * 视频参数
     * 必填
     */
    private VideoParams videoParams;

    /**
     * 视频信息json链接
     * 非必填
     */
    private String faceJsonUrl;

    /**
     * 人声音频链接
     * 非必填，传入时使用此音频进行推理，使用 audioUrl 进行最终视频合成
     * 需要传入者保证两个音频文件长度一致
     */
    private String vocalAudioUrl;

    /**
     * 多人参数
     * 非必填，用于多人场景
     */
    private List<MultiPersonParam> multiPersonParams;

    /**
     * 校验参数
     * 非必填，传什么校验什么
     */
    private CheckParams checkParams;

    /**
     * 业务透传数据（框架支持）
     */
    private String businessMessage;

    /**
     * 任务复杂度（框架支持，可选）
     */
    private TaskComplexity taskComplexity;
}