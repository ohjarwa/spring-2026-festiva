package org.example.newyear.dto.callback;

import lombok.Data;

/**
 * 视频算法回调响应（最外层结构）
 *
 * @author Claude
 * @since 2026-02-05
 */
@Data
public class VideoAlgorithmCallbackResponse {

    /**
     * 响应码 0=成功 其他=失败
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 数据内容（根据不同算法服务有不同的结构）
     */
    private Object data;
}
