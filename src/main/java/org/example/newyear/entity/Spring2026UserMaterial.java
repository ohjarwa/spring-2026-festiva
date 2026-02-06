package org.example.newyear.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户素材实体
 *
 * @author Claude
 * @since 2026-02-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("spring_2026_user_material")
public class Spring2026UserMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 素材唯一标识
     */
    private String materialId;

    /**
     * 用户ID
     */
    private String userId;

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
     * 状态: 1=正常 0=删除
     */
    private Integer status;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
