package org.example.newyear.exception;

import lombok.Getter;
import org.example.newyear.common.BusinessCode;

/**
 * 业务异常
 *
 * @author Claude
 * @since 2026-02-04
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(BusinessCode businessCode) {
        super(businessCode.getMessage());
        this.code = businessCode.getCode();
        this.message = businessCode.getMessage();
    }

    public BusinessException(BusinessCode businessCode, String message) {
        super(message);
        this.code = businessCode.getCode();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // 不填充堆栈跟踪，提升性能
        return this;
    }
}