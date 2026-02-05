package org.example.newyear.common;

/**
 * 管理员级别枚举
 *
 * @author Claude
 * @since 2026-02-05
 */
public enum AdminLevel {

    /**
     * 普通用户
     */
    USER(0, "普通用户"),

    /**
     * 审核员（可以审核内容）
     */
    AUDITOR(1, "审核员"),

    /**
     * 管理员（可以下线作品）
     */
    ADMIN(2, "管理员"),

    /**
     * 超级管理员（所有权限）
     */
    SUPER_ADMIN(3, "超级管理员");

    private final Integer code;
    private final String description;

    AdminLevel(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static AdminLevel fromCode(Integer code) {
        if (code == null) {
            return USER;
        }
        for (AdminLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return USER;
    }

    /**
     * 是否为管理员或更高
     */
    public boolean isAdmin() {
        return this.code >= ADMIN.code;
    }

    /**
     * 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return this.code == SUPER_ADMIN.code;
    }
}
