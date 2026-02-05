package org.example.newyear.service.algorithm;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.AlgorithmProperties;
import org.springframework.stereotype.Service;

/**
 * 声音合成服务（无需鉴权）
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceTtsService {

    private final AlgorithmProperties properties;

    /**
     * 调用声音合成接口（无需鉴权）
     *
     * @param request 请求参数
     * @return 算法响应
     */
    public AlgorithmResponse synthesizeVoice(VoiceTtsRequest request) {
        log.info("调用声音合成: voiceId={}, text={}",
                request.getVoiceId(), request.getText());

        try {
            String url = properties.getVoiceTts().getUrl() + "/api/voice_tts";

            // 注意：语音组接口不需要鉴权，不传X-API-Key
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .timeout(properties.getVoiceTts().getTimeout())
                    .execute();

            String body = response.body();
            log.info("声音合成响应: {}", body);

            return JSONUtil.toBean(body, AlgorithmResponse.class);

        } catch (Exception e) {
            log.error("声音合成失败", e);
            AlgorithmResponse errorResponse = new AlgorithmResponse();
            errorResponse.setCode(-1);
            errorResponse.setMessage("声音合成失败: " + e.getMessage());
            return errorResponse;
        }
    }
}