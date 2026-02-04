package org.example.newyear.common;

import lombok.Getter;

/**
 * 记录状态枚举
 *
 * @author Claude
 * @since 2026-02-04
 */
@Getter
public enum RecordStatus {

    /**
     * 排队中
     */
    QUEUED(0, "queued", "排队中"),

    /**
     * 生成中
     */
    PROCESSING(1, "processing", "生成中"),

    /**
     * 已完成
     */
    COMPLETED(2, "completed", "已完成"),

    /**
     * 失败
     */
    FAILED(3, "failed", "失败");

    private final Integer code;
    private final String english;
    private final String chinese;

    RecordStatus(Integer code, String english, String chinese) {
        this.code = code;
        this.english = english;
        this.chinese = chinese;
    }

    /**
     * 根据code获取枚举
     */
    public static RecordStatus getByCode(Integer code) {
        for (RecordStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据code获取英文描述
     */
    public static String getEnglishByCode(Integer code) {
        RecordStatus status = getByCode(code);
        return status != null ? status.getEnglish() : "";
    }

    /**
     * 根据code获取中文描述
     */
    public static String getChineseByCode(Integer code) {
        RecordStatus status = getByCode(code);
        return status != null ? status.getChinese() : "";
    }
}