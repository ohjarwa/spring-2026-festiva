package org.example.newyear.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 素材VO
 *
 * @author Claude
 * @since 2026-02-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialVO {

    /**
     * 素材ID
     */
    private String materialId;

    /**
     * 素材类型: photo/audio
     */
    private String materialType;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}