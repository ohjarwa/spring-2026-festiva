package org.example.newyear.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.entity.Spring2026UserMaterial;
import org.example.newyear.mapper.Spring2026UserMaterialMapper;
import org.example.newyear.util.KeyGeneratorUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户素材服务
 *
 * @author Claude
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMaterialService {

    private final Spring2026UserMaterialMapper materialMapper;

    /**
     * 保存素材
     */
    public Spring2026UserMaterial saveMaterial(String userId, String materialType,
                                                String fileUrl, String originalFilename, Long fileSize) {
        String materialId = KeyGeneratorUtils.materialIdGen();

        Spring2026UserMaterial material = Spring2026UserMaterial.builder()
                .materialId(materialId)
                .userId(userId)
                .materialType(materialType)
                .fileUrl(fileUrl)
                .originalFilename(originalFilename)
                .fileSize(fileSize)
                .status(1)
                .uploadTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        materialMapper.insert(material);
        log.info("保存素材成功: materialId={}, userId={}, type={}", materialId, userId, materialType);

        return material;
    }

    /**
     * 根据素材ID查询素材
     */
    public Spring2026UserMaterial getMaterial(String materialId) {
        QueryWrapper<Spring2026UserMaterial> wrapper = new QueryWrapper<>();
        wrapper.eq("material_id", materialId);
        wrapper.eq("status", 1);
        return materialMapper.selectOne(wrapper);
    }

    /**
     * 查询用户的所有素材（按类型）
     */
    public List<Spring2026UserMaterial> getUserMaterialsByType(String userId, String materialType) {
        return materialMapper.selectByUserIdAndType(userId, materialType);
    }

    /**
     * 根据素材ID获取URL
     */
    public String getMaterialUrl(String materialId) {
        Spring2026UserMaterial material = getMaterial(materialId);
        if (material == null) {
            throw new RuntimeException("素材不存在: " + materialId);
        }
        return material.getFileUrl();
    }
}