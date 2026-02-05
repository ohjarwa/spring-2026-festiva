package org.example.newyear.service.algorithm;

import lombok.Data;

import java.util.List;

/**
 * 多图生图请求（图生图算法）
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class MultiImageGenerateRequest {

    /**
     * 输出目录
     */
    private String targetDir = "temp/";

    /**
     * 输入图片URL列表（多个图片）
     * 例如：[用户照片, 参考衣服图片]
     */
    private List<String> images;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 生成图片宽度
     */
    private Integer width = 1440;

    /**
     * 生成图片高度
     */
    private Integer height = 1440;

    /**
     * 生成数量
     */
    private Integer num = 1;
}
