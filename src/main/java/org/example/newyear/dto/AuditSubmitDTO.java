package org.example.newyear.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 提交审核请求DTO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class AuditSubmitDTO {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 图片URL
     */
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    /**
     * 音频URL
     */
    @NotBlank(message = "音频URL不能为空")
    private String audioUrl;
}