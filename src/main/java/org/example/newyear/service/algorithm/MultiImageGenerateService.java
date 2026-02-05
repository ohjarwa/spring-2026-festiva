package org.example.newyear.service.algorithm;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.AlgorithmProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 多图生图服务（图生图算法）
 *
 * 注意：这个服务使用同步接口，直接返回结果
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiImageGenerateService {

    private final AlgorithmProperties properties;

    /**
     * 调用多图生图接口（同步）
     *
     * @param request 请求参数
     * @return 生成的图片URL列表
     */
    public List<String> generate(MultiImageGenerateRequest request) {
        log.info("调用多图生图: images={}, prompt={}",
                request.getImages(), request.getPrompt());

        try {
            String url = properties.getMultiImageGenerate().getUrl() + "/invoke/sync";

            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .timeout(properties.getMultiImageGenerate().getTimeout())
                    .execute();

            String body = response.body();
            log.info("多图生图响应: {}", body);

            // 解析响应
            JSONObject jsonResponse = JSONUtil.parseObj(body);

            // 假设响应格式为：{"images": ["url1", "url2"], ...}
            List<String> imageUrls = new ArrayList<>();
            if (jsonResponse.containsKey("images")) {
                imageUrls = jsonResponse.getBeanList("images", String.class);
            } else if (jsonResponse.containsKey("data")) {
                // 如果响应是统一格式 {"data": {"images": [...]}}
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null && data.containsKey("images")) {
                    imageUrls = data.getBeanList("images", String.class);
                }
            }

            log.info("多图生图成功，生成 {} 张图片", imageUrls.size());
            return imageUrls;

        } catch (Exception e) {
            log.error("多图生图失败", e);
            throw new RuntimeException("多图生图失败: " + e.getMessage(), e);
        }
    }
}
