package org.example.newyear.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作品VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class WorkVO {

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板封面
     */
    private String templateCover;

    /**
     * 状态（英文）
     */
    private String status;

    /**
     * 状态（中文）
     */
    private String statusText;

    /**
     * 进度 0-100
     */
    private Integer progress;

    /**
     * 当前步骤
     */
    private String currentStep;

    /**
     * 最终视频URL
     */
    private String resultUrl;

    /**
     * 缩略图URL
     */
    private String resultThumbnailUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}