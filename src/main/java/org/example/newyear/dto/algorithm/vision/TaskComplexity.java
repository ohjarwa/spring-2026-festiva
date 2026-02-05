package org.example.newyear.dto.algorithm.vision;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaskComplexity {
    /**
     * 输入素材信息列表
     */
    private List<MediaInfo> inputInfo;
    
    /**
     * 预期输出结果信息列表
     */
    private List<MediaInfo> outputInfo;
    
    @Data
    @Builder
    public static class MediaInfo {
        /**
         * 素材key
         */
        private String key;
        
        /**
         * 素材类型：video、audio、image
         */
        private String type;
        
        /**
         * 分辨率
         */
        private Resolution resolution;
        
        /**
         * 时长（毫秒）
         */
        private Long durationMs;
        
        /**
         * 帧率
         */
        private Double fps;
        
        /**
         * 比特率
         */
        private Long bitrate;
    }
    
    @Data
    @Builder
    public static class Resolution {
        private Integer width;
        private Integer height;
    }
}