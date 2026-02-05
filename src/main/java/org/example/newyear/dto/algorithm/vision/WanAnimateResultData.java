package org.example.newyear.dto.algorithm.vision;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WanAnimate 人物替换结果数据（data 内层）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WanAnimateResultData extends AlgorithmResultBase {

    /**
     * 生成的结果视频URL
     */
    private String targetVideoUrl;
}