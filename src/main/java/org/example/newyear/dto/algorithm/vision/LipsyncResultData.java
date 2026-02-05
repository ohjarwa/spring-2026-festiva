package org.example.newyear.dto.algorithm.vision;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Lipsync 口型同步结果数据（data 内层）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LipsyncResultData extends AlgorithmResultBase {

    /**
     * 生成视频URL
     */
    private String videoUrl;

    /**
     * 透明视频URL（可能为null）
     */
    private String alphaVideoUrl;

    /**
     * 结果信息
     */
    private ResultInfo resultInfo;

    @Data
    public static class ResultInfo {
        /**
         * 总帧数
         */
        private Integer totalFrames;

        /**
         * 侧脸帧数
         */
        private Integer sideFaceFrames;

        /**
         * 遮挡脸帧数
         */
        private Integer occlusionFaceFrames;

        /**
         * 是否存在侧脸
         */
        private Boolean sideFace;

        /**
         * 是否存在遮挡脸
         */
        private Boolean occlusionFace;
    }
}