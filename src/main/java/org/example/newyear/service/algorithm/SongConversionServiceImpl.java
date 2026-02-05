package org.example.newyear.service.algorithm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.SongConversionConfig;
import org.example.newyear.dto.algorithm.audio.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongConversionServiceImpl implements SongConversionService {
    
    private static final String ASYNC_PATH = "/hapi/v1/song_conversion/post_json/conversion_async";
    private static final String QUERY_PATH = "/hapi/v1/song_conversion/post_json/get_result";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SongConversionConfig config;
    
    @Override
    public SongConversionSubmitResponse submitAsync(SongConversionRequest request) {
        String url = config.getBaseUrl() + ASYNC_PATH;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<SongConversionRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            log.debug("提交歌曲转换任务, url={}, taskId={}", url, request.getBusinessTaskId());
            
            ResponseEntity<SongConversionSubmitResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, SongConversionSubmitResponse.class
            );
            
            SongConversionSubmitResponse result = response.getBody();
            
            if (result != null && !result.isSuccess()) {
                log.warn("歌曲转换任务提交失败, taskId={}, code={}, msg={}", 
                    request.getBusinessTaskId(), result.getCode(), result.getMsg());
            }
            
            return result;
        } catch (Exception e) {
            log.error("提交歌曲转换任务异常, taskId={}", request.getBusinessTaskId(), e);
            throw new RuntimeException("提交歌曲转换任务失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public SongConversionQueryResponse queryResult(String businessTaskId) {
        String url = config.getBaseUrl() + QUERY_PATH;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        SongConversionQueryRequest request = SongConversionQueryRequest.builder()
            .businessTaskId(businessTaskId)
            .source(config.getSource())
            .build();
        
        HttpEntity<SongConversionQueryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            log.debug("查询歌曲转换结果, taskId={}", businessTaskId);
            
            ResponseEntity<SongConversionQueryResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, SongConversionQueryResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("查询歌曲转换结果异常, taskId={}", businessTaskId, e);
            throw new RuntimeException("查询歌曲转换结果失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public SongConversionResultWrapper queryAndParseResult(String businessTaskId) {
        SongConversionQueryResponse response = queryResult(businessTaskId);
        
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("查询歌曲转换结果失败");
        }
        
        SongConversionQueryData data = response.parseData(objectMapper);
        if (data == null) {
            throw new RuntimeException("解析歌曲转换结果失败");
        }
        
        SongConversionResultWrapper.SongConversionResultWrapperBuilder builder = 
            SongConversionResultWrapper.builder()
                .businessTaskId(data.getBusinessTaskId())
                .status(SongConversionStatus.of(data.getStatus()))
                .rawResponse(response);
        
        // 如果成功，解析URL
        if (data.isSuccess()) {
            SongConversionUrlData urlData = data.parseUrl(objectMapper);
            if (urlData != null) {
                builder.vocalUrl(urlData.getVocalUrl())
                       .bgmUrl(urlData.getBgmUrl())
                       .allUrl(urlData.getAllUrl());
            }
        }
        
        return builder.build();
    }
}