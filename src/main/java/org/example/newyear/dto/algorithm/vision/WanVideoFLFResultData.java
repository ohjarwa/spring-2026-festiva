package org.example.newyear.dto.algorithm.vision;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WanVideo 首尾帧结果数据（data 内层）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WanVideoFLFResultData extends AlgorithmResultBase {

    /**
     * 生成的结果视频URL
     */
    private String targetVideoUrl;
}