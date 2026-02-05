package org.example.newyear.annotation;

import org.example.newyear.common.AdminLevel;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * 用于标记需要管理员权限才能访问的接口
 *
 * @author Claude
 * @since 2026-02-05
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {

    /**
     * 要求的管理员级别
     * 默认为ADMIN（管理员及以上）
     */
    AdminLevel value() default AdminLevel.ADMIN;
}
