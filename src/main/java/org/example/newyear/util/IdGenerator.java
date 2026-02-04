package org.example.newyear.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * ID生成器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Component
public class IdGenerator {

    private final Snowflake snowflake;

    public IdGenerator() {
        // 使用机器ID和数据中心ID生成雪花ID
        this.snowflake = IdUtil.getSnowflake(1, 1);
    }

    /**
     * 生成记录ID
     */
    public String generateRecordId() {
        return "record_" + snowflake.nextId();
    }
}