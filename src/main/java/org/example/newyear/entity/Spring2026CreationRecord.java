package org.example.newyear.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户创作记录实体
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("spring_2026_creation_record")
public class Spring2026CreationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录唯一标识
     */
    private String recordId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 用户上传的素材（JSON）
     */
    private String userMaterials;

    /**
     * 状态 0=排队 1=生成中 2=已完成 3=失败
     */
    private Integer status;

    /**
     * 整体进度 0-100
     */
    private Integer progress;

    /**
     * 任务执行详情（JSON）
     */
    private String taskExecution;

    /**
     * 最终视频URL
     */
    private String resultUrl;

    /**
     * 缩略图
     */
    private String resultThumbnailUrl;

    /**
     * 时长(秒)
     */
    private BigDecimal resultDuration;

    /**
     * 文件大小
     */
    private Long resultFileSize;

    /**
     * 错误信息（JSON）
     */
    private String errorInfo;

    /**
     * 审核信息（JSON）
     */
    private String auditInfo;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 开始生成时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

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