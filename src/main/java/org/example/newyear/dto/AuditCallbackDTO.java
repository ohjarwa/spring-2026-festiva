package org.example.newyear.dto;

import lombok.Data;

/**
 * 审核回调DTO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class AuditCallbackDTO {

    /**
     * 回调唯一标识
     */
    private String callbackId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 审核类型
     */
    private String auditType;

    /**
     * 资源URL
     */
    private String resourceUrl;

    /**
     * 审核结果
     */
    private AuditResult auditResult;

    /**
     * 时间戳
     */
    private Long timestamp;

    @Data
    public static class AuditResult {
        /**
         * 审核状态: pass=通过, reject=拒绝, review=人工审核
         */
        private String status;

        /**
         * 审核建议
         */
        private String suggestion;

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