package org.example.newyear.service.algorithm;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.AlgorithmProperties;
import org.springframework.stereotype.Service;

/**
 * 唇形同步服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LipSyncService {

    private final AlgorithmProperties properties;

    /**
     * 调用唇形同步接口
     *
     * @param request 请求参数
     * @return 算法响应
     */
    public AlgorithmResponse syncLip(LipSyncRequest request) {
        log.info("调用唇形同步: videoUrl={}, audioUrl={}",
                request.getVideoUrl(), request.getAudioUrl());

        try {
            String url = properties.getLipSync().getUrl() + "/api/lip_sync";

            HttpResponse response = HttpRequest.post(url)
                    .header("X-API-Key", properties.getLipSync().getApiKey())
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .timeout(properties.getLipSync().getTimeout())
                    .execute();

            String body = response.body();
            log.info("唇形同步响应: {}", body);

            return JSONUtil.toBean(body, AlgorithmResponse.class);

        } catch (Exception e) {
            log.error("唇形同步失败", e);
            AlgorithmResponse errorResponse = new AlgorithmResponse();
            errorResponse.setCode(-1);
            errorResponse.setMessage("唇形同步失败: " + e.getMessage());
            return errorResponse;
        }
    }
}