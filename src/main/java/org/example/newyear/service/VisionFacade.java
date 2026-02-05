package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.config.VisionConfig;
import org.example.newyear.dto.algorithm.vision.*;
import org.example.newyear.entity.algorithm.vision.ImageRatio;
import org.example.newyear.entity.algorithm.vision.ImageSize;
import org.example.newyear.entity.algorithm.vision.VideoResolution;
import org.example.newyear.entity.algorithm.vision.WanAnimateTaskMode;
import org.example.newyear.entity.enums.VisionAbility;
import org.example.newyear.service.algorithm.VisionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class VisionFacade {

    private final VisionService visionService;
    private final VisionConfig visionConfig;

    // ==================== WanAnimate 人物替换 ====================

    public AsyncSubmitResponse submitWanAnimate(WanAnimateRequest request, String taskId) {
        validateWanAnimateRequest(request);
        VisionRequestHeaders headers = buildHeaders(VisionAbility.WAN_ANIMATE, taskId);
        log.info("提交人物替换任务, taskId={}, taskMode={}, resolution={}",
                taskId, request.getTaskMode(), request.getResolution());
        return visionService.submitAsync(request, headers);
    }

    public AsyncSubmitResponse submitWanAnimate(String imageUrl, String videoUrl, String taskId) {
        WanAnimateRequest request = WanAnimateRequestBuilder.builder()
                .image(imageUrl)
                .video(videoUrl)
                .taskMode(WanAnimateTaskMode.REPLACE_BODY)
                .resolution(VideoResolution.P480)
                .build();
        return submitWanAnimate(request, taskId);
    }

    public AsyncSubmitResponse submitWanAnimate720P(String imageUrl, String videoUrl, String taskId) {
        WanAnimateRequest request = WanAnimateRequestBuilder.builder()
                .image(imageUrl)
                .video(videoUrl)
                .taskMode(WanAnimateTaskMode.REPLACE_BODY)
                .resolution(VideoResolution.P720)
                .build();
        return submitWanAnimate(request, taskId);
    }

    public VisionCallbackResponse<WanAnimateResultData> queryWanAnimateResult(String taskId) {
        log.info("查询人物替换结果, taskId={}", taskId);
        return visionService.queryResult(
                taskId, VisionAbility.WAN_ANIMATE.getCode(), WanAnimateResultData.class
        );
    }

    public CancelTaskResponse cancelWanAnimate(String taskId) {
        log.info("取消人物替换任务, taskId={}", taskId);
        return visionService.cancelTask(taskId, VisionAbility.WAN_ANIMATE.getCode());
    }

    // ==================== Flux2 多图生图 ====================

    public AsyncSubmitResponse submitFlux2ImageGen(Flux2ImageGenRequest request, String taskId) {
        validateFlux2Request(request);
        VisionRequestHeaders headers = buildHeaders(VisionAbility.FLUX2_IMAGE_GEN, taskId);
        boolean isTextToImage = (request.getImages() == null || request.getImages().isEmpty());
        log.info("提交{}任务, taskId={}", isTextToImage ? "文生图" : "图生图", taskId);
        return visionService.submitAsync(request, headers);
    }

    public AsyncSubmitResponse submitTextToImage(String prompt, int width, int height, int num, String taskId) {
        Flux2ImageGenRequest request = Flux2RequestBuilder.textToImage(prompt)
                .size(width, height)
                .num(num)
                .build();
        return submitFlux2ImageGen(request, taskId);
    }

    public AsyncSubmitResponse submitTextToImage(String prompt, String taskId) {
        Flux2ImageGenRequest request = Flux2RequestBuilder.textToImage(prompt)
                .size(ImageSize.SIZE_1024)
                .build();
        return submitFlux2ImageGen(request, taskId);
    }

    public AsyncSubmitResponse submitImageToImage(String prompt, String imageUrl, ImageRatio ratio, String taskId) {
        Flux2ImageGenRequest request = Flux2RequestBuilder.imageToImage(prompt, imageUrl)
                .ratio(ratio)
                .build();
        return submitFlux2ImageGen(request, taskId);
    }

    public AsyncSubmitResponse submitImageToImage(String prompt, String imageUrl, String taskId) {
        return submitImageToImage(prompt, imageUrl, ImageRatio.RATIO_4_3, taskId);
    }

    public AsyncSubmitResponse submitImageToImageMulti(String prompt, List<String> imageUrls, ImageRatio ratio, String taskId) {
        Flux2ImageGenRequest request = Flux2RequestBuilder.imageToImage(prompt, imageUrls)
                .ratio(ratio)
                .build();
        return submitFlux2ImageGen(request, taskId);
    }

    public VisionCallbackResponse<Flux2ImageGenResultData> queryFlux2ImageGenResult(String taskId) {
        log.info("查询多图生图结果, taskId={}", taskId);
        return visionService.queryResult(
                taskId, VisionAbility.FLUX2_IMAGE_GEN.getCode(), Flux2ImageGenResultData.class
        );
    }

    public CancelTaskResponse cancelFlux2ImageGen(String taskId) {
        log.info("取消多图生图任务, taskId={}", taskId);
        return visionService.cancelTask(taskId, VisionAbility.FLUX2_IMAGE_GEN.getCode());
    }

    // ==================== WanVideo FLF 首尾帧 ====================

    public AsyncSubmitResponse submitWanVideoFLF(WanVideoFLFRequest request, String taskId) {
        validateWanVideoFLFRequest(request);
        VisionRequestHeaders headers = buildHeaders(VisionAbility.WAN_VIDEO_FLF, taskId);
        String resolution = WanVideoResolution.of(request.getWidth(), request.getHeight()).getDescription();
        log.info("提交首尾帧任务, taskId={}, resolution={}", taskId, resolution);
        return visionService.submitAsync(request, headers);
    }

    public AsyncSubmitResponse submitWanVideoFLF(String firstImageUrl, String lastImageUrl, String taskId) {
        WanVideoFLFRequest request = WanVideoFLFRequestBuilder.builder()
                .firstImage(firstImageUrl)
                .lastImage(lastImageUrl)
                .use480P()
                .build();
        return submitWanVideoFLF(request, taskId);
    }

    public AsyncSubmitResponse submitWanVideoFLF(String firstImageUrl, String lastImageUrl,
                                                 String prompt, WanVideoResolution resolution, String taskId) {
        WanVideoFLFRequest request = WanVideoFLFRequestBuilder.builder()
                .firstImage(firstImageUrl)
                .lastImage(lastImageUrl)
                .prompt(prompt)
                .resolution(resolution)
                .build();
        return submitWanVideoFLF(request, taskId);
    }

    public AsyncSubmitResponse submitWanVideoFLF720P(String firstImageUrl, String lastImageUrl, String taskId) {
        WanVideoFLFRequest request = WanVideoFLFRequestBuilder.builder()
                .firstImage(firstImageUrl)
                .lastImage(lastImageUrl)
                .use720P()
                .build();
        return submitWanVideoFLF(request, taskId);
    }

    public AsyncSubmitResponse submitWanVideoFLF720P(String firstImageUrl, String lastImageUrl,
                                                     String prompt, String taskId) {
        WanVideoFLFRequest request = WanVideoFLFRequestBuilder.builder()
                .firstImage(firstImageUrl)
                .lastImage(lastImageUrl)
                .prompt(prompt)
                .use720P()
                .build();
        return submitWanVideoFLF(request, taskId);
    }

    public VisionCallbackResponse<WanVideoFLFResultData> queryWanVideoFLFResult(String taskId) {
        log.info("查询首尾帧结果, taskId={}", taskId);
        return visionService.queryResult(
                taskId, VisionAbility.WAN_VIDEO_FLF.getCode(), WanVideoFLFResultData.class
        );
    }

    public CancelTaskResponse cancelWanVideoFLF(String taskId) {
        log.info("取消首尾帧任务, taskId={}", taskId);
        return visionService.cancelTask(taskId, VisionAbility.WAN_VIDEO_FLF.getCode());
    }

    // ==================== Lipsync 口型同步 ====================

    /**
     * 提交口型同步任务（完整参数）
     *
     * @param request 请求参数
     * @param taskId  任务ID
     */
    public AsyncSubmitResponse submitLipsync(LipsyncRequest request, String taskId) {
        validateLipsyncRequest(request);
        VisionRequestHeaders headers = buildHeaders(VisionAbility.LIPSYNC, taskId);
        log.info("提交口型同步任务, taskId={}", taskId);
        return visionService.submitAsync(request, headers);
    }

    /**
     * 提交口型同步任务（简化参数，使用默认视频参数）
     *
     * @param srcVideoUrl 源视频URL
     * @param audioUrl    音频URL
     * @param taskId      任务ID
     */
    public AsyncSubmitResponse submitLipsync(String srcVideoUrl, String audioUrl, String taskId) {
        LipsyncRequest request = LipsyncRequestBuilder.builder()
                .srcVideoUrl(srcVideoUrl)
                .audioUrl(audioUrl)
                .build();
        return submitLipsync(request, taskId);
    }

    /**
     * 提交口型同步任务（启用超分高清模式）
     *
     * @param srcVideoUrl 源视频URL
     * @param audioUrl    音频URL
     * @param taskId      任务ID
     */
    public AsyncSubmitResponse submitLipsyncHD(String srcVideoUrl, String audioUrl, String taskId) {
        LipsyncRequest request = LipsyncRequestBuilder.builder()
                .srcVideoUrl(srcVideoUrl)
                .audioUrl(audioUrl)
                .enableEnhance()
                .build();
        return submitLipsync(request, taskId);
    }

    /**
     * 提交口型同步任务（指定输出分辨率）
     *
     * @param srcVideoUrl 源视频URL
     * @param audioUrl    音频URL
     * @param width       输出宽度
     * @param height      输出高度
     * @param taskId      任务ID
     */
    public AsyncSubmitResponse submitLipsync(String srcVideoUrl, String audioUrl,
                                             int width, int height, String taskId) {
        LipsyncRequest request = LipsyncRequestBuilder.builder()
                .srcVideoUrl(srcVideoUrl)
                .audioUrl(audioUrl)
                .outputResolution(width, height)
                .build();
        return submitLipsync(request, taskId);
    }

    /**
     * 提交口型同步任务（宠物场景优化）
     *
     * @param srcVideoUrl 源视频URL
     * @param audioUrl    音频URL
     * @param taskId      任务ID
     */
    public AsyncSubmitResponse submitLipsyncForPet(String srcVideoUrl, String audioUrl, String taskId) {
        LipsyncRequest request = LipsyncRequestBuilder.builder()
                .srcVideoUrl(srcVideoUrl)
                .audioUrl(audioUrl)
                .enableShutupFirst()  // 首帧静音驱动，优化宠物效果
                .build();
        return submitLipsync(request, taskId);
    }

    /**
     * 查询口型同步结果
     */
    public VisionCallbackResponse<LipsyncResultData> queryLipsyncResult(String taskId) {
        log.info("查询口型同步结果, taskId={}", taskId);
        return visionService.queryResult(
                taskId, VisionAbility.LIPSYNC.getCode(), LipsyncResultData.class
        );
    }

    /**
     * 取消口型同步任务
     */
    public CancelTaskResponse cancelLipsync(String taskId) {
        log.info("取消口型同步任务, taskId={}", taskId);
        return visionService.cancelTask(taskId, VisionAbility.LIPSYNC.getCode());
    }

    /**
     * 校验 Lipsync 请求参数
     */
    private void validateLipsyncRequest(LipsyncRequest request) {
        if (request.getSrcVideoUrl() == null || request.getSrcVideoUrl().isEmpty()) {
            throw new IllegalArgumentException("srcVideoUrl 不能为空");
        }
        if (request.getAudioUrl() == null || request.getAudioUrl().isEmpty()) {
            throw new IllegalArgumentException("audioUrl 不能为空");
        }
        if (request.getVideoParams() == null) {
            throw new IllegalArgumentException("videoParams 不能为空");
        }
    }

    // ==================== 通用方法 ====================

    public String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== 私有方法 ====================

    private VisionRequestHeaders buildHeaders(VisionAbility ability, String taskId) {
        return VisionRequestHeaders.builder()
                .ability(ability.getCode())
                .taskId(taskId)
                .callbackUrl(visionConfig.getCallbackUrl())
                .progressCallbackUrl(visionConfig.getProgressCallbackUrl())
                .tags(visionConfig.getTags())
                .traceId(taskId)
                .build();
    }

    private void validateWanAnimateRequest(WanAnimateRequest request) {
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new IllegalArgumentException("images 不能为空");
        }
        if (request.getVideo() == null || request.getVideo().isEmpty()) {
            throw new IllegalArgumentException("video 不能为空");
        }
        if (request.getTaskMode() == null || request.getTaskMode().isEmpty()) {
            throw new IllegalArgumentException("taskMode 不能为空");
        }
    }

    private void validateFlux2Request(Flux2ImageGenRequest request) {
        if (request.getPrompt() == null || request.getPrompt().isEmpty()) {
            throw new IllegalArgumentException("prompt 不能为空");
        }
        boolean isTextToImage = (request.getImages() == null || request.getImages().isEmpty());
        if (isTextToImage && (request.getWidth() == null || request.getHeight() == null)) {
            throw new IllegalArgumentException("文生图模式必须指定 width 和 height");
        }
        if (request.getWidth() != null && request.getWidth() > 1440) {
            throw new IllegalArgumentException("width 不能超过 1440");
        }
        if (request.getHeight() != null && request.getHeight() > 1440) {
            throw new IllegalArgumentException("height 不能超过 1440");
        }
    }

    private void validateWanVideoFLFRequest(WanVideoFLFRequest request) {
        if (request.getFirstImage() == null || request.getFirstImage().isEmpty()) {
            throw new IllegalArgumentException("firstImage 不能为空");
        }
        if (request.getLastImage() == null || request.getLastImage().isEmpty()) {
            throw new IllegalArgumentException("lastImage 不能为空");
        }
        if (request.getWidth() == null || request.getHeight() == null) {
            throw new IllegalArgumentException("width 和 height 不能为空");
        }
        boolean isValid480P = (request.getWidth() == 832 && request.getHeight() == 480);
        boolean isValid720P = (request.getWidth() == 1280 && request.getHeight() == 720);
        if (!isValid480P && !isValid720P) {
            throw new IllegalArgumentException("仅支持 480P(832x480) 或 720P(1280x720) 分辨率");
        }
    }

}