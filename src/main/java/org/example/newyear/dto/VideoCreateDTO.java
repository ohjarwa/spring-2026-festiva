package org.example.newyear.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建视频请求DTO
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class VideoCreateDTO {

    /**
     * 模板ID
     */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;

    /**
     * 素材
     */
    @NotNull(message = "素材不能为空")
    private MaterialsDTO materials;

    @Data
    public static class MaterialsDTO {
        /**
         * 图片素材ID列表（优先使用materialId）
         */
        private List<String> photoMaterialIds;

        /**
         * 音频素材ID列表（优先使用materialId）
         */
        private List<String> audioMaterialIds;

        /**
         * 图片URL列表（向后兼容，直接传URL）
         * @deprecated 建议使用 photoMaterialIds
         */
        @Deprecated
        private List<String> photos;

        /**
         * 音频URL列表（向后兼容，直接传URL）
         * @deprecated 建议使用 audioMaterialIds
         */
        @Deprecated
        private List<String> audios;
    }
}