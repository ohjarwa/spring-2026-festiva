package org.example.newyear.service.algorithm;

import lombok.Data;

/**
 * 声音克隆请求
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class VoiceCloneRequest {

    /**
     * 原始音频URL
     */
    private String audioUrl;

    /**
     * 回调URL
     */
    private String callbackUrl;
}