package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.vision.AsyncSubmitData;
import org.example.newyear.dto.algorithm.vision.AsyncSubmitResponse;
import org.example.newyear.dto.algorithm.vision.LipsyncRequest;
import org.example.newyear.dto.algorithm.vision.VisionResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisionAigcService {

    private final VisionFacade visionFacade;

    public void createLipsyncVideo(String audioUrl, String videoUrl) {
        String taskId = visionFacade.generateTaskId();

        LipsyncRequest request = LipsyncRequest.builder()
                .audioUrl(audioUrl)
                .srcVideoUrl(videoUrl)
                .businessMessage("订单号:12345")  // 透传业务数据
                .build();

        AsyncSubmitResponse response = visionFacade.submitLipsync(request, taskId);

        if (response.isSuccess()) {
            log.info("任务提交成功, 队列位置={}, 预计等待={}ms",
                    response.getData().getQueuePosition(),
                    response.getData().getEstimatedDurationMs());
        }
    }
}
