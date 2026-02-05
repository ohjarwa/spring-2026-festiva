package org.example.newyear.common;

import lombok.Getter;

/**
 * 业务错误码枚举
 *
 * @author Claude
 * @since 2026-02-04
 */
@Getter
public enum BusinessCode {

    // ========== 通用错误码 ==========
    SUCCESS(0, "success"),
    ERROR_INVALID_PARAMS(40004, "参数错误"),
    ERROR_RECORD_NOT_FOUND(40005, "记录不存在"),
    ERROR_INTERNAL_SERVER(50000, "服务器内部错误"),

    // ========== 用户相关 ==========
    ERROR_USER_NOT_FOUND(40101, "用户不存在"),
    ERROR_USER_BANNED(40102, "用户已被封禁"),
    ERROR_USER_RESTRICTED(40103, "用户已被限制"),

    // ========== 配额相关 ==========
    ERROR_QUOTA_NOT_ENOUGH(40001, "今日配额已用完"),
    ERROR_QUOTA_EXHAUSTED(40002, "配额已耗尽"),

    // ========== 视频生成相关 ==========
    ERROR_RETRY_LIMIT_EXCEEDED(40003, "重试次数已达上限"),
    ERROR_VIDEO_GENERATION_FAILED(40006, "视频生成失败"),
    ERROR_TEMPLATE_NOT_FOUND(40007, "模板不存在"),
    ERROR_RECORD_STATUS_INVALID(40008, "记录状态不正确"),

    // ========== 文件上传相关 ==========
    ERROR_FILE_UPLOAD_FAILED(40009, "文件上传失败"),
    ERROR_FILE_TYPE_INVALID(40010, "文件类型不支持"),
    ERROR_FILE_SIZE_EXCEEDED(40011, "文件大小超限"),

    // ========== AI回调相关 ==========
    ERROR_SIGNATURE_INVALID(40012, "签名验证失败"),
    ERROR_TIMESTAMP_EXPIRED(40013, "时间戳过期"),
    ERROR_CALLBACK_DUPLICATE(40014, "重复回调"),

    // ========== 审核相关 ==========
    ERROR_AUDIT_FAILED(40015, "审核失败"),
    ERROR_AUDIT_PENDING(40016, "审核中，请稍后"),
    ERROR_AUDIT_REJECTED(40017, "审核未通过"),
    ERROR_AUDIT_NOT_FOUND(40018, "审核记录不存在"),

    // ========== 风控相关 ==========
    ERROR_RISK_CONTROL_FAILED(41001, "风控校验失败"),
    ERROR_CONTENT_UNSAFE(41002, "内容不合规"),
    ERROR_AUDIO_VIOLATION(41003, "音频违规"),
    ERROR_IMAGE_VIOLATION(41004, "图片违规"),

    // ========== 管理员相关 ==========
    ERROR_PERMISSION_DENIED(40301, "权限不足"),
    ERROR_ADMIN_REQUIRED(40302, "需要管理员权限"),
    ERROR_NOT_SUPER_ADMIN(40303, "需要超级管理员权限"),
    ERROR_CANNOT_TAKE_DOWN(40304, "该作品无法下线");

    private final Integer code;
    private final String message;

    BusinessCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}