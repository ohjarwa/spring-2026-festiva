package org.example.newyear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.newyear.entity.Spring2026UserMaterial;

import java.util.List;

/**
 * 用户素材Mapper
 *
 * @author Claude
 * @since 2026-02-06
 */
@Mapper
public interface Spring2026UserMaterialMapper extends BaseMapper<Spring2026UserMaterial> {

    /**
     * 查询用户的所有素材（按类型和时间排序）
     */
    @Select("SELECT * FROM spring_2026_user_material " +
            "WHERE user_id = #{userId} AND status = 1 " +
            "AND material_type = #{materialType} " +
            "ORDER BY upload_time DESC")
    List<Spring2026UserMaterial> selectByUserIdAndType(
            @Param("userId") String userId,
            @Param("materialType") String materialType
    );
}