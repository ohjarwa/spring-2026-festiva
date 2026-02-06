package org.example.newyear.service.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.FeatureExtractionConfig;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionRequest;
import org.example.newyear.dto.algorithm.audio.FeatureExtractionSubmitResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureExtractionServiceImpl implements FeatureExtractionService {
    
    private static final String EXTRACTION_PATH = "/hapi/v1/svc_feature/extraction";
    
    private final RestTemplate restTemplate;
    private final FeatureExtractionConfig config;
    
    @Override
    public FeatureExtractionSubmitResponse submitAsync(FeatureExtractionRequest request) {
        String url = config.getBaseUrl() + EXTRACTION_PATH;
        
        // 构建 form-urlencoded 请求体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("businessTaskId", request.getBusinessTaskId());
        formData.add("callbackUrl", request.getCallbackUrl());
        formData.add("videoUrl", request.getVideoUrl());
        
        if (request.getSource() != null) {
            formData.add("source", request.getSource());
        }
        if (request.getFeatureName() != null) {
            formData.add("featureName", request.getFeatureName());
        }
        if (request.getDemusic() != null) {
            formData.add("demusic", request.getDemusic().toString());
        }
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        
        try {
            log.debug("提交特征提取任务, url={}, taskId={}", url, request.getBusinessTaskId());
            
            ResponseEntity<FeatureExtractionSubmitResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, FeatureExtractionSubmitResponse.class
            );
            
            FeatureExtractionSubmitResponse result = response.getBody();
            
            if (result != null && !result.isSuccess()) {
                log.warn("特征提取任务提交失败, taskId={}, code={}, msg={}", 
                    request.getBusinessTaskId(), result.getCode(), result.getMsg());
            }
            
            return result;
        } catch (Exception e) {
            log.error("提交特征提取任务异常, taskId={}", request.getBusinessTaskId(), e);
            throw new RuntimeException("提交特征提取任务失败: " + e.getMessage(), e);
        }
    }
}