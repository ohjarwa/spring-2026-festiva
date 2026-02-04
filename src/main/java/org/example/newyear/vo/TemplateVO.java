package org.example.newyear.vo;

import lombok.Data;

/**
 * 模板VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class TemplateVO {

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 预估时间（秒）
     */
    private Integer estimatedTime;
}