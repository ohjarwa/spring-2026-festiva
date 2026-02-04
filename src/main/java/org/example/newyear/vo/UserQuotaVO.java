package org.example.newyear.vo;

import lombok.Data;

/**
 * 用户配额VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class UserQuotaVO {

    /**
     * 总配额
     */
    private Integer totalQuota;

    /**
     * 已使用配额
     */
    private Integer usedQuota;

    /**
     * 剩余配额
     */
    private Integer remainingQuota;

    /**
     * 能否上传
     */
    private Boolean canUpload;

    /**
     * 能否创建视频
     */
    private Boolean canCreateVideo;

    /**
     * 配额重置时间
     */
    private String resetTime;
}