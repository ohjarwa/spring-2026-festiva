package org.example.newyear.dto.algorithm.vision;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Flux2 多图生图结果数据（data 内层）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Flux2ImageGenResultData extends AlgorithmResultBase {

    /**
     * 生成的结果图片URL列表
     */
    private List<String> targetImageUrls;

    /**
     * 单张结果图URL（兼容单图返回场景）
     */
    private String targetImageUrl;
}