package org.example.newyear.vo;

import lombok.Data;

import java.util.List;

/**
 * 作品列表VO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class WorkListVO {

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 生成中的数量
     */
    private Integer processingCount;

    /**
     * 作品列表
     */
    private List<WorkVO> items;
}