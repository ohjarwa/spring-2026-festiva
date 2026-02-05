package org.example.newyear.service.algorithm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.VisionConfig;
import org.example.newyear.dto.algorithm.vision.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;


@Slf4j
@Service
public class VisionServiceImpl implements VisionService {

    private static final String BASE_URL = "http://vision-platform-hxapisix.hxapisix/infer/cn/access";
    private static final String ASYNC_PATH = "/assemble_common/algorithm/v2/async";
    private static final String RESULT_PATH = "/assemble_common/algorithm/result";
    private static final String CANCEL_PATH = "/assemble_common/algorithm/cancel";
    private static final String JUMP_PATH = "/assemble_common/algorithm/jump";

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final VisionConfig visionConfig;

    public VisionServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, VisionConfig visionConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.visionConfig = visionConfig;
    }

    @Override
    public AsyncSubmitResponse submitAsync(Object request, VisionRequestHeaders headers) {
        String url = BASE_URL + ASYNC_PATH;

        HttpHeaders httpHeaders = buildAsyncHttpHeaders(headers);
        HttpEntity<Object> entity = new HttpEntity<>(request, httpHeaders);

        try {
            log.debug("提交异步任务, url={}, ability={}, taskId={}",
                    url, headers.getAbility(), headers.getTaskId());

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            AsyncSubmitResponse result = objectMapper.readValue(
                    response.getBody(), AsyncSubmitResponse.class
            );

            if (!result.isSuccess()) {
                log.warn("异步任务提交失败, taskId={}, code={}, message={}",
                        headers.getTaskId(), result.getCode(), result.getMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("提交异步任务异常, ability={}, taskId={}",
                    headers.getAbility(), headers.getTaskId(), e);
            throw new RuntimeException("提交异步任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T extends AlgorithmResultBase> VisionCallbackResponse<T> queryResult(
            String taskId, String ability, Class<T> dataType) {

        String url = BASE_URL + RESULT_PATH + "?taskId=" + taskId;

        HttpHeaders httpHeaders = buildBasicHttpHeaders();
        httpHeaders.set("Ability", ability);

        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);

        try {
            log.debug("查询任务结果, taskId={}, ability={}", taskId, ability);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class
            );

            VisionCallbackResponse<T> result = objectMapper.readValue(
                    response.getBody(),
                    objectMapper.getTypeFactory().constructParametricType(
                            VisionCallbackResponse.class, dataType
                    )
            );

            return result;
        } catch (Exception e) {
            log.error("查询任务结果异常, taskId={}, ability={}", taskId, ability, e);
            throw new RuntimeException("查询任务结果失败: " + e.getMessage(), e);
        }
    }

    @Override
    public CancelTaskResponse cancelTask(String taskId, String ability) {
        String url = BASE_URL + CANCEL_PATH;

        HttpHeaders httpHeaders = buildBasicHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Ability", ability);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("taskId", taskId);

        HttpEntity<Object> entity = new HttpEntity<>(
                hashMap, httpHeaders
        );

        try {
            log.debug("取消任务, taskId={}, ability={}", taskId, ability);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            CancelTaskResponse result = objectMapper.readValue(
                    response.getBody(), CancelTaskResponse.class
            );

            if (!result.isSuccess()) {
                log.warn("取消任务失败, taskId={}, code={}, message={}",
                        taskId, result.getCode(), result.getMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("取消任务异常, taskId={}, ability={}", taskId, ability, e);
            throw new RuntimeException("取消任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建基础请求头（App-Id, App-Secret）
     */
    private HttpHeaders buildBasicHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("App-Id", visionConfig.getAppId());
        httpHeaders.set("App-Secret", visionConfig.getAppSecret());
        return httpHeaders;
    }

    /**
     * 构建异步提交请求头
     */
    private HttpHeaders buildAsyncHttpHeaders(VisionRequestHeaders headers) {
        HttpHeaders httpHeaders = buildBasicHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Ability", headers.getAbility());
        httpHeaders.set("Task-Id", headers.getTaskId());

        if (headers.getCallbackUrl() != null) {
            httpHeaders.set("Callback-Url", headers.getCallbackUrl());
        }
        if (headers.getProgressCallbackUrl() != null) {
            httpHeaders.set("Progress-Callback-Url", headers.getProgressCallbackUrl());
        }
        if (headers.getTraceId() != null) {
            httpHeaders.set("X-Trace-Id", headers.getTraceId());
        }
        if (headers.getTags() != null) {
            httpHeaders.set("Tags", headers.getTags());
        }
        if (headers.getGroup() != null) {
            httpHeaders.set("Group", headers.getGroup());
        }
        if (headers.getPosition() != null) {
            httpHeaders.set("Position", headers.getPosition().toString());
        }

        return httpHeaders;
    }
}