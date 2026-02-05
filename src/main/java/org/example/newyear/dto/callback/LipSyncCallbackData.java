package org.example.newyear.dto.callback;

import lombok.Data;

/**
 * 唇形同步回调数据
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class LipSyncCallbackData {

    /**
     * 同步后的视频URL
     */
    private String videoUrl;

    /**
     * 状态码 0=成功
     */
    private Integer code;

    /**
     * 状态消息
     */
    private String message;
}
