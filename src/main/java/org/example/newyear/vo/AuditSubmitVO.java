package org.example.newyear.vo;

import lombok.Data;

/**
 * 审核提交响应VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class AuditSubmitVO {

    /**
     * 审核ID
     */
    private String auditId;

    /**
     * 审核状态
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 预计审核时间（秒）
     */
    private Integer estimatedTime;
}