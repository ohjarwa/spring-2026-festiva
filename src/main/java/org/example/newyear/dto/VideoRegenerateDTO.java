package org.example.newyear.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 重新生成视频请求DTO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class VideoRegenerateDTO {

    /**
     * 记录ID
     */
    @NotBlank(message = "记录ID不能为空")
    private String recordId;
}