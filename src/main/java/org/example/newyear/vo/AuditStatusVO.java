package org.example.newyear.vo;

import lombok.Data;

/**
 * 审核状态VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class AuditStatusVO {

    /**
     * 审核ID
     */
    private String auditId;

    /**
     * 审核状态: pending=审核中, pass=通过, reject=拒绝, review=人工审核
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 图片审核结果
     */
    private ResourceAuditResult imageAudit;

    /**
     * 音频审核结果
     */
    private ResourceAuditResult audioAudit;

    @Data
    public static class ResourceAuditResult {
        /**
         * 审核状态
         */
        private String status;

        /**
         * 拒绝原因
         */
        private String rejectReason;

        /**
         * 风险标签
         */
        private String[] riskLabels;
    }
}