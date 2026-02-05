package org.example.newyear.dto.algorithm.vision;

import lombok.Data;

/**
 * 算法结果基类（data 内层公共字段）
 */
@Data
public class AlgorithmResultBase {
    
    /**
     * 算法内部错误码
     */
    private Integer code;
    
    /**
     * 算法内部错误信息
     */
    private String message;
    
    /**
     * 额外的处理信息（执行耗时、资源等）
     */
    private AdditionalProcessInformation additionalProcessInformation;
    
    /**
     * 算法层是否成功
     */
    public boolean isAlgorithmSuccess() {
        return code != null && code >= 200 && code < 400;
    }
}