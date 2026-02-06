package org.example.newyear.service.algorithm;

import org.example.newyear.dto.algorithm.audio.FeatureExtractionRequest;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionSubmitResponse;

public interface FeatureExtractionService {
    
    /**
     * 异步提交特征提取任务
     *
     * @param request 请求参数
     * @return 提交结果
     */
    FeatureExtractionSubmitResponse submitAsync(FeatureExtractionRequest request);
}