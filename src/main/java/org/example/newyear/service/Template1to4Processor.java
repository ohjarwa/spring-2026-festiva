package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.entity.algorithm.vision.ImageRatio;
import org.example.newyear.service.algorithm.VoiceCloneRequest;
import org.example.newyear.service.algorithm.VoiceCloneService;
import org.example.newyear.service.algorithm.VoiceTtsRequest;
import org.example.newyear.service.algorithm.VoiceTtsService;
import org.example.newyear.util.VideoProcessorUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * 模板1-4流程处理器
 *
 * 完整流程（顺次执行，严格遵守依赖顺序）：
 * 1. 调用语音服务（克隆 → 合成）
 * 2. 多图生图（Flux2图生图算法）→ aigc_person.jpg
 * 3. 人物替换（WanAnimate视频0）→ aigc_video_0.mp4
 * 4. 人物替换（WanAnimate视频2）→ aigc_video_2_step0.mp4
 * 5. 唇形同步（Lipsync视频2）→ aigc_video_2_step1.mp4
 * 6. FFmpeg混音（混入BGM）
 * 7. 视频拼接（视频0 + 视频2）→ result.mp4
 *
 * 使用VisionFacade调用算法服务，顺次执行
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service("template1to4Processor")
@RequiredArgsConstructor
public class Template1to4Processor implements ITemplateProcessor {

    private final VoiceCloneService voiceCloneService;
    private final VoiceTtsService voiceTtsService;
    private final VisionFacade visionFacade;
    private final VideoProcessingService videoProcessingService;
    private final CallbackResultManager callbackResultManager;
    private final VideoProcessorUtil videoProcessorUtil;

    // ======================== 固定素材URL配置（后续从OSS获取）========================

    /**
     * 视频片段0 - 用于人物替换
     */
    private static final String SRC_VIDEO_0_URL = "https://your-oss-bucket.com/templates/src_video_0.mp4";

    /**
     * 视频片段1 - 无须处理
     */
    private static final String SRC_VIDEO_1_URL = "https://your-oss-bucket.com/templates/src_video_1.mp4";

    /**
     * 视频片段2 - 先人物替换，再Lipsync
     */
    private static final String SRC_VIDEO_2_URL = "https://your-oss-bucket.com/templates/src_video_2.mp4";

    /**
     * 背景音乐 - 片段2对应的背景音乐
     */
    private static final String BGM_2_URL = "https://your-oss-bucket.com/templates/bgm_2.wav";

    /**
     * 固定文案 - 用于语音合成
     */
    private static final String FIXED_TEXT = "春节快乐，万事如意，恭喜发财！";

    /**
     * 图生图预设提示词
     * 根据实际需求调整，描述生成图片的风格和内容
     */
    private static final String IMAGE_GEN_PROMPT = "Spring Festival theme, festive atmosphere, high quality portrait, Chinese New Year celebration";

    @Override
    public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {
        log.info("开始处理模板1-4流程: recordId={}, templateId={}", recordId, template.getTemplateId());

        try {
            // 获取用户上传素材
            String userPhotoUrl = dto.getMaterials().getPhotos().get(0);  // src_person.jpg
            String userAudioUrl = dto.getMaterials().getAudios().get(0);  // 用户原始语音

            // ======================== 步骤1: 语音服务（两步）========================

            // 步骤1.1: 声音克隆 → voiceId
            log.info("步骤1.1: 声音克隆");
            String voiceId = performVoiceCloneAndWait(userAudioUrl, recordId);
            log.info("声音克隆完成: voiceId={}", voiceId);

            // 步骤1.2: 声音合成 → vocal_2.wav
            log.info("步骤1.2: 声音合成");
            String vocal2Url = performVoiceTtsAndWait(voiceId, recordId);
            log.info("声音合成完成: vocal2Url={}", vocal2Url);

            // ======================== 步骤2: Flux2多图生图 ========================

            log.info("步骤2: Flux2多图生图（图生图算法）");
            String aigcPersonUrl = performFlux2ImageGenAndWait(userPhotoUrl, recordId);
            log.info("Flux2多图生图完成: aigcPersonUrl={}", aigcPersonUrl);

            // ======================== 步骤3: WanAnimate人物替换（视频0）========================

            log.info("步骤3: WanAnimate人物替换（视频0）");
            String aigcVideo0Url = performWanAnimateAndWait(SRC_VIDEO_0_URL, aigcPersonUrl, recordId, "face_swap_0");
            log.info("视频0人物替换完成: aigcVideo0Url={}", aigcVideo0Url);

            // ======================== 步骤4: WanAnimate人物替换（视频2）========================

            log.info("步骤4: WanAnimate人物替换（视频2）");
            String aigcVideo2Step0Url = performWanAnimateAndWait(SRC_VIDEO_2_URL, aigcPersonUrl, recordId, "face_swap_2");
            log.info("视频2人物替换完成: aigcVideo2Step0Url={}", aigcVideo2Step0Url);

            // ======================== 步骤5: Lipsync唇形同步（视频2）========================

            log.info("步骤5: Lipsync唇形同步（视频2 + vocal_2.wav）");
            String aigcVideo2Step1Url = performLipsyncAndWait(aigcVideo2Step0Url, vocal2Url, recordId);
            log.info("唇形同步完成: aigcVideo2Step1Url={}", aigcVideo2Step1Url);

            // ======================== 步骤6: FFmpeg混入背景音乐 ========================

            log.info("步骤6: FFmpeg混入背景音乐（BGM）");
            String aigcVideo2FinalUrl = performAudioMixing(aigcVideo2Step1Url, BGM_2_URL, recordId);
            log.info("背景音乐混合完成: aigcVideo2FinalUrl={}", aigcVideo2FinalUrl);

            // ======================== 步骤7: 视频拼接 ========================

            log.info("步骤7: 视频拼接（视频0 + 视频2最终版）");
            String finalResultUrl = performVideoConcatenation(aigcVideo0Url, aigcVideo2FinalUrl, recordId);
            log.info("视频拼接完成: finalResultUrl={}", finalResultUrl);

            log.info("模板1-4流程处理完成: recordId={}, resultUrl={}", recordId, finalResultUrl);
            return finalResultUrl;

        } catch (Exception e) {
            log.error("模板1-4流程处理失败: recordId={}", recordId, e);
            throw new RuntimeException("模板处理失败: " + e.getMessage(), e);
        }
    }

    // ======================== 私有方法：各算法调用 ========================

    /**
     * 步骤1.1: 声音克隆并等待回调
     */
    private String performVoiceCloneAndWait(String userAudioUrl, String recordId) {
        try {
            VoiceCloneRequest request = new VoiceCloneRequest();
            request.setAudioUrl(userAudioUrl);
            request.setCallbackUrl(buildCallbackUrl(recordId, "voice_clone"));

            Map<String, Object> result = videoProcessingService.callAndWaitForCallback(
                    recordId,
                    "voice_clone",
                    () -> voiceCloneService.cloneVoice(request),
                    120  // 等待120秒
            );

            return (String) result.get("voiceId");

        } catch (Exception e) {
            log.error("声音克隆失败: recordId={}", recordId, e);
            throw new RuntimeException("声音克隆失败", e);
        }
    }

    /**
     * 步骤1.2: 声音合成并等待回调
     */
    private String performVoiceTtsAndWait(String voiceId, String recordId) {
        try {
            VoiceTtsRequest request = new VoiceTtsRequest();
            request.setVoiceId(voiceId);
            request.setText(FIXED_TEXT);
            request.setCallbackUrl(buildCallbackUrl(recordId, "voice_tts"));

            Map<String, Object> result = videoProcessingService.callAndWaitForCallback(
                    recordId,
                    "voice_tts",
                    () -> voiceTtsService.synthesizeVoice(request),
                    120  // 等待120秒
            );

            return (String) result.get("audioUrl");

        } catch (Exception e) {
            log.error("声音合成失败: recordId={}", recordId, e);
            throw new RuntimeException("声音合成失败", e);
        }
    }

    /**
     * 步骤2: Flux2多图生图并轮询等待结果
     * 使用VisionFacade调用算法服务
     */
    private String performFlux2ImageGenAndWait(String userPhotoUrl, String recordId) {
        try {
            log.info("开始Flux2多图生图: userPhotoUrl={}, recordId={}", userPhotoUrl, recordId);

            String stepName = "flux2_image_gen";

            // 使用pollForResult轮询等待结果
            Map<String, Object> result = videoProcessingService.pollForResult(
                    recordId,
                    stepName,
                    (taskId) -> {
                        // 提交Flux2图生图任务，传入用户照片作为参考图（图生图模式）
                        // taskId就是callbackId
                        return visionFacade.submitImageToImageMulti(
                                IMAGE_GEN_PROMPT,
                                java.util.Arrays.asList(userPhotoUrl),
                                ImageRatio.RATIO_1_1,  // 1:1比例
                                taskId  // 使用taskId作为callbackId
                        );
                    }
            );

            // 获取生成的图片URL
            @SuppressWarnings("unchecked")
            java.util.List<String> targetImageUrls = (java.util.List<String>) result.get("targetImageUrls");

            if (targetImageUrls == null || targetImageUrls.isEmpty()) {
                throw new RuntimeException("Flux2生图失败：未返回图片URL");
            }

            String aigcPersonUrl = targetImageUrls.get(0);  // aigc_person.jpg
            log.info("Flux2生图成功: aigcPersonUrl={}, recordId={}", aigcPersonUrl, recordId);

            return aigcPersonUrl;

        } catch (Exception e) {
            log.error("Flux2生图失败: recordId={}", recordId, e);
            throw new RuntimeException("Flux2生图失败", e);
        }
    }

    /**
     * 步骤3/4: WanAnimate人物替换并轮询等待结果
     * 使用VisionFacade调用算法服务
     *
     * @param videoUrl    源视频URL
     * @param faceImageUrl 人物图片URL
     * @param recordId    记录ID
     * @param stepName    步骤名称（face_swap_0 或 face_swap_2）
     * @return 替换后的视频URL
     */
    private String performWanAnimateAndWait(String videoUrl, String faceImageUrl, String recordId, String stepName) {
        try {
            log.info("开始WanAnimate人物替换: videoUrl={}, faceImageUrl={}, stepName={}",
                    videoUrl, faceImageUrl, stepName);

            // 使用pollForResult轮询等待结果
            Map<String, Object> result = videoProcessingService.pollForResult(
                    recordId,
                    stepName,
                    (taskId) -> {
                        // 提交WanAnimate任务，taskId就是callbackId
                        return visionFacade.submitWanAnimate(
                                faceImageUrl,   // 人物图片
                                videoUrl,       // 驱动视频
                                taskId          // 使用taskId作为callbackId
                        );
                    }
            );

            String targetVideoUrl = (String) result.get("targetVideoUrl");
            log.info("WanAnimate人物替换成功: stepName={}, targetVideoUrl={}", stepName, targetVideoUrl);

            return targetVideoUrl;

        } catch (Exception e) {
            log.error("WanAnimate人物替换失败: recordId={}, stepName={}", recordId, stepName, e);
            throw new RuntimeException("人物替换失败", e);
        }
    }

    /**
     * 步骤5: Lipsync唇形同步并轮询等待结果
     * 使用VisionFacade调用算法服务
     */
    private String performLipsyncAndWait(String videoUrl, String audioUrl, String recordId) {
        try {
            log.info("开始Lipsync唇形同步: videoUrl={}, audioUrl={}, recordId={}",
                    videoUrl, audioUrl, recordId);

            String stepName = "lipsync";

            // 使用pollForResult轮询等待结果
            Map<String, Object> result = videoProcessingService.pollForResult(
                    recordId,
                    stepName,
                    (taskId) -> {
                        // 提交Lipsync任务，taskId就是callbackId
                        return visionFacade.submitLipsync(videoUrl, audioUrl, taskId);
                    }
            );

            String videoResultUrl = (String) result.get("videoUrl");
            log.info("Lipsync唇形同步成功: recordId={}, videoUrl={}", recordId, videoResultUrl);

            return videoResultUrl;

        } catch (Exception e) {
            log.error("Lipsync唇形同步失败: recordId={}", recordId, e);
            throw new RuntimeException("唇形同步失败", e);
        }
    }

    /**
     * 步骤6: FFmpeg混入背景音乐
     */
    private String performAudioMixing(String videoUrl, String bgmUrl, String recordId) {
        try {
            log.info("开始混入背景音乐: videoUrl={}, bgmUrl={}, recordId={}", videoUrl, bgmUrl, recordId);

            // 使用JavaCV混入背景音乐，自动上传到OSS
            String ossUrl = videoProcessorUtil.mixAudioWithBgm(videoUrl, bgmUrl, recordId);

            log.info("背景音乐混合完成并上传到OSS: ossUrl={}", ossUrl);
            return ossUrl;

        } catch (Exception e) {
            log.error("背景音乐混合失败: recordId={}", recordId, e);
            throw new RuntimeException("背景音乐混合失败", e);
        }
    }

    /**
     * 步骤7: 视频拼接（上传到cv账户）
     */
    private String performVideoConcatenation(String video0Url, String video2Url, String recordId) {
        try {
            log.info("开始视频拼接: video0={}, video2={}, recordId={}", video0Url, video2Url, recordId);

            // 拼接视频，上传到cv账户的OSS
            java.util.List<String> videoUrls = Arrays.asList(video0Url, video2Url);
            String ossUrl = videoProcessorUtil.concatVideos(videoUrls, recordId, "cv");

            log.info("视频拼接完成并上传到OSS[cv]: ossUrl={}", ossUrl);
            return ossUrl;

        } catch (Exception e) {
            log.error("视频拼接失败: recordId={}", recordId, e);
            throw new RuntimeException("视频拼接失败", e);
        }
    }

    // ======================== 辅助方法 ========================

    /**
     * 构建回调URL
     * 格式：http://your-domain.com/api/callback/xxx?callbackId=recordId:uuid
     */
    private String buildCallbackUrl(String recordId, String stepName) {
        String callbackId = recordId + ":" + stepName + ":" + java.util.UUID.randomUUID();

        // TODO: 从配置文件读取服务器地址
        String serverDomain = "http://your-domain.com";

        switch (stepName) {
            case "voice_clone":
                return serverDomain + "/api/callback/voice-clone";
            case "voice_tts":
                return serverDomain + "/api/callback/voice-tts";
            default:
                throw new IllegalArgumentException("未知的步骤名称: " + stepName);
        }
    }
}
