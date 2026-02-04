package org.example.newyear.vo;

import lombok.Data;

/**
 * 创建视频响应VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class VideoCreateVO {

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 状态
     */
    private String status;

    /**
     * 预估时间（秒）
     */
    private Integer estimatedTime;

    /**
     * 提示信息
     */
    private String tips;
}