package org.example.newyear.vo;

import lombok.Data;

/**
 * 上传结果VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class UploadResultVO {

    /**
     * 文件URL
     */
    private String url;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 时长（音频）/ 宽度（图片）
     */
    private Object metadata;
}