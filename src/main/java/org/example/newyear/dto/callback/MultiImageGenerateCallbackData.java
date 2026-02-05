package org.example.newyear.dto.callback;

import lombok.Data;

import java.util.List;

/**
 * 多图生图回调数据
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class MultiImageGenerateCallbackData {

    /**
     * 生成的图片URL数组
     */
    private List<String> fileUrls;
}
