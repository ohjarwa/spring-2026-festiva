package org.example.newyear.dto.algorithm.vision;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 额外处理信息
 */
@Data
public class AdditionalProcessInformation {
    
    /**
     * 执行时间记录
     */
    private ExecutionTimeRecorder executionTimeRecorder;
    
    /**
     * 算法名
     */
    private String algorithm;
    
    /**
     * 能力名
     */
    private String ability;
    
    /**
     * 实例ID
     */
    private String instanceId;
    
    /**
     * 实例租户
     */
    private String instanceTenant;
    
    /**
     * 实例分组
     */
    private String instanceGroup;
    
    /**
     * 实例主机IP
     */
    private String instanceHostIp;
    
    /**
     * 实例部署名
     */
    private String instanceDeployment;
    
    /**
     * 实例集群
     */
    private String instanceCluster;
    
    /**
     * GPU型号
     */
    private String podGpu;
    
    /**
     * GPU显存
     */
    private String podGpuMemory;
    
    /**
     * 下载文件信息
     */
    private List<FileInfo> downloadFiles;
    
    /**
     * 上传文件信息
     */
    private List<FileInfo> uploadFiles;
    
    /**
     * 任务分组
     */
    private String taskGroup;
    
    /**
     * 执行分组
     */
    private String executeGroup;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 扩展信息
     */
    private Map<String, Object> extensions;
    
    @Data
    public static class ExecutionTimeRecorder {
        private Long created;
        private Long finish;
        private Long parameterPreprocess;
        private Long waitForWorkerThread;
        private Long parameterPostprocess;
        private Long acquireResource;
        private Long invocation;
        private Long resultPostprocess;
        
        /**
         * 获取总耗时（毫秒）
         */
        public Long getTotalDuration() {
            if (created != null && finish != null) {
                return finish - created;
            }
            return null;
        }
    }
    
    @Data
    public static class FileInfo {
        private String type;
        private Long size;
        private Integer imageWidth;
        private Integer imageHeight;
        private Boolean hasAudio;
        private Boolean hasVideo;
        private Long lengthInTime;
        private Integer audioChannels;
        private Integer audioBitrate;
        private Double frameRate;
        private Integer videoBitrate;
    }
}