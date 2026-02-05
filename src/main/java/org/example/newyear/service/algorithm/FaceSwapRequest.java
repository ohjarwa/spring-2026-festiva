package org.example.newyear.service.algorithm;

import lombok.Data;

/**
 * 人脸替换请求
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class FaceSwapRequest {

    /**
     * 原始视频URL
     */
    private String videoUrl;

    /**
     * 目标人脸图片URL
     */
    private String faceImageUrl;

    /**
     * 回调URL
     */
    private String callbackUrl;
}