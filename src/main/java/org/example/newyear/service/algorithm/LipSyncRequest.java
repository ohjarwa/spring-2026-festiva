package org.example.newyear.service.algorithm;

import lombok.Data;

/**
 * 唇形同步请求
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class LipSyncRequest {

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 音频URL
     */
    private String audioUrl;

    /**
     * 回调URL
     */
    private String callbackUrl;
}