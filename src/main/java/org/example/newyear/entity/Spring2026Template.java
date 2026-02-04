package org.example.newyear.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模板实体
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
@TableName("spring_2026_template")
public class Spring2026Template implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板唯一标识
     */
    private String templateId;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 模板资源URL
     */
    private String templateUrl;

    /**
     * 任务配置（JSON）
     */
    private String taskConfig;

    /**
     * 使用次数
     */
    private Integer usedTimes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}