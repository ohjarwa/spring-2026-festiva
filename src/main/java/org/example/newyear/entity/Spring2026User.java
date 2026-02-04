package org.example.newyear.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息实体
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
@TableName("spring_2026_user")
public class Spring2026User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 来源
     */
    private String source;

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
     * 能否重试
     */
    private Integer canRetry;

    /**
     * 账号状态 1=正常 0=禁用
     */
    private Integer accountStatus;

    /**
     * 能否上传
     */
    private Integer canUpload;

    /**
     * 能否创建视频
     */
    private Integer canCreateVideo;

    /**
     * 限制原因
     */
    private String restrictionReason;

    /**
     * 限制结束时间
     */
    private LocalDateTime restrictionEndTime;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 封禁结束时间
     */
    private LocalDateTime banEndTime;

    /**
     * 违规次数
     */
    private Integer violationCount;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failedCount;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUseTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 扩展数据
     */
    private String extraData;
}
