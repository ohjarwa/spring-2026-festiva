package org.example.newyear.dto.callback;

import lombok.Data;

/**
 * 人脸替换回调数据
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class FaceSwapCallbackData {

    /**
     * 替换后的视频URL
     */
    private String targetVideoUrl;
}
