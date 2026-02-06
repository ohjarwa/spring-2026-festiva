package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 多图生图 - Flux2 请求
 * Ability: Dreamface-Flux2-ImageGen-V1
 * YAPI: https://yapi.myhexin.com/yapi/service/313260/interface/api/852738
 *
 * 说明：
 * - 传入 images 字段：图生图模式，默认生成 1 张
 * - 不传 images 字段：文生图模式，默认生成 4 张
 * - 生成的结果图宽、高会裁剪成 16 的倍数
 * - 尺寸限制：不超过 1440*1440
 */
@Data
@Builder
public class Flux2ImageGenRequest {
    /**
     * 正向提示词
     * 必填
     */
    private String prompt;

    /**
     * 图片URL地址列表
     * 非必填，传入则为图生图模式，不传则为文生图模式
     * 多张图会按照提示词融合成一张图
     */
    private List<String> images;

    /**
     * 生成图片的比例
     * 非必填，可选值: 1:1, 4:3, 3:4, 16:9, 9:16，默认 4:3
     * 使用 ratio 时，生成图像的像素面积值为 1024*1024
     */
    private String ratio;

    /**
     * 结果图的宽
     * 文生图模式必填，图生图模式可选（与 ratio 二选一）
     * 要求不得大于 1440
     */
    private Integer width;

    /**
     * 结果图的高
     * 文生图模式必填，图生图模式可选（与 ratio 二选一）
     * 要求不得大于 1440
     */
    private Integer height;

    /**
     * 生图的数量
     * 非必填，图生图默认为 1，文生图默认为 4
     */
    private Integer num;

    /**
     * 业务透传数据（框架支持）
     */
    private String businessMessage;

    /**
     * 任务复杂度（框架支持，可选）
     */
    private TaskComplexity taskComplexity;
}