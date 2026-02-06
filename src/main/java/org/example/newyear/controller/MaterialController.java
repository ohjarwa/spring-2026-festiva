package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.entity.Spring2026UserMaterial;
import org.example.newyear.service.UserMaterialService;
import org.example.newyear.vo.MaterialVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材管理控制器
 *
 * @author Claude
 * @since 2026-02-06
 */
@Slf4j
@RestController
@RequestMapping("/material")
@RequiredArgsConstructor
public class MaterialController {

    private final UserMaterialService userMaterialService;

    /**
     * 查询用户的所有图片素材
     */
    @GetMapping("/photos/{userId}")
    public List<MaterialVO> getUserPhotos(@PathVariable String userId) {
        log.info("查询用户图片素材: userId={}", userId);

        List<Spring2026UserMaterial> materials = userMaterialService.getUserMaterialsByType(userId, "photo");

        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询用户的所有音频素材
     */
    @GetMapping("/audios/{userId}")
    public List<MaterialVO> getUserAudios(@PathVariable String userId) {
        log.info("查询用户音频素材: userId={}", userId);

        List<Spring2026UserMaterial> materials = userMaterialService.getUserMaterialsByType(userId, "audio");

        return materials.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据素材ID查询素材详情
     */
    @GetMapping("/info/{materialId}")
    public MaterialVO getMaterialInfo(@PathVariable String materialId) {
        log.info("查询素材详情: materialId={}", materialId);

        Spring2026UserMaterial material = userMaterialService.getMaterial(materialId);
        if (material == null) {
            throw new RuntimeException("素材不存在: " + materialId);
        }

        return convertToVO(material);
    }

    /**
     * 转换为VO
     */
    private MaterialVO convertToVO(Spring2026UserMaterial material) {
        return MaterialVO.builder()
                .materialId(material.getMaterialId())
                .materialType(material.getMaterialType())
                .fileUrl(material.getFileUrl())
                .originalFilename(material.getOriginalFilename())
                .fileSize(material.getFileSize())
                .uploadTime(material.getUploadTime())
                .build();
    }
}