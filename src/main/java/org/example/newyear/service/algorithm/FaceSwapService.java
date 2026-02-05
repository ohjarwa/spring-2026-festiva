package org.example.newyear.service.algorithm;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.AlgorithmProperties;
import org.springframework.stereotype.Service;

/**
 * 人脸替换服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceSwapService {

    private final AlgorithmProperties properties;

    /**
     * 调用人脸替换接口
     *
     * @param request 请求参数
     * @return 算法响应
     */
    public AlgorithmResponse swapFace(FaceSwapRequest request) {
        log.info("调用人脸替换: videoUrl={}, faceImageUrl={}",
                request.getVideoUrl(), request.getFaceImageUrl());

        try {
            String url = properties.getFaceSwap().getUrl() + "/api/face_swap";

            HttpResponse response = HttpRequest.post(url)
                    .header("X-API-Key", properties.getFaceSwap().getApiKey())
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .timeout(properties.getFaceSwap().getTimeout())
                    .execute();

            String body = response.body();
            log.info("人脸替换响应: {}", body);

            return JSONUtil.toBean(body, AlgorithmResponse.class);

        } catch (Exception e) {
            log.error("人脸替换失败", e);
            AlgorithmResponse errorResponse = new AlgorithmResponse();
            errorResponse.setCode(-1);
            errorResponse.setMessage("人脸替换失败: " + e.getMessage());
            return errorResponse;
        }
    }
}