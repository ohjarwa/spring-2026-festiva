package org.example.newyear.service.algorithm;

import lombok.Data;

/**
 * 声音合成请求
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class VoiceTtsRequest {

    /**
     * 克隆后的声音ID
     */
    private String voiceId;

    /**
     * 合成文本
     */
    private String text;

    /**
     * 回调URL
     */
    private String callbackUrl;
}