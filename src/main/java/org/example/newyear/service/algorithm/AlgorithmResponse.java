package org.example.newyear.service.algorithm;

import lombok.Data;

/**
 * 算法服务响应
 *
 * @author Claude
 * @since 2026-02-04
 */
@Data
public class AlgorithmResponse {

    /**
     * 状态码: 0=成功, 非0=失败
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Data data;

    @lombok.Data
    public static class Data {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 结果URL
         */
        private String resultUrl;

        /**
         * 状态: pending=处理中, success=成功, failed=失败
         */
        private String status;
    }
}