package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.service.algorithm.*;
import org.example.newyear.util.VideoProcessorUtil;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模板1-4流程处理器
 *
 * 完整流程：
 * 1. 调用语音服务（克隆 → 合成）
 * 2. 多图生图（图生图算法）→ aigc_person.jpg
 * 3. 人物替换（视频0）→ aigc_video_0.mp4
 * 4. 人物替换（视频2）→ aigc_video_2_step0.mp4
 * 5. 唇形同步（视频2）→ aigc_video_2_step1.mp4
 * 6. FFmpeg混音（混入BGM）
 * 7. 视频拼接（视频0 + 视频2）→ result.mp4
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service("template1to4Processor")
@RequiredArgsConstructor
public class Template1to4Processor implements ITemplateProcessor {

    private final FaceSwapService faceSwapService;
    private final VoiceCloneService voiceCloneService;
    private final VoiceTtsService voiceTtsService;
    private final LipSyncService lipSyncService;
    private final MultiImageGenerateService multiImageGenerateService;
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

            // ======================== 步骤2: 多图生图 ========================

            log.info("步骤2: 多图生图（图生图算法）");
            String aigcPersonUrl = performMultiImageGenerateAndWait(userPhotoUrl, recordId);
            log.info("多图生图完成: aigcPersonUrl={}", aigcPersonUrl);

            // ======================== 步骤3+4: 并行执行人物替换 ========================

            log.info("步骤3+4: 并行执行人物替换（视频0和视频2）");

            // 并行执行视频0和视频2的人脸替换
            CompletableFuture<String> video0Future = CompletableFuture.supplyAsync(() -> {
                String result = performFaceSwapAndWait(SRC_VIDEO_0_URL, aigcPersonUrl, recordId, "face_swap_0");
                log.info("视频0人脸替换完成: aigc_video_0.mp4");
                return result;
            });

            CompletableFuture<String> video2Future = CompletableFuture.supplyAsync(() -> {
                String result = performFaceSwapAndWait(SRC_VIDEO_2_URL, aigcPersonUrl, recordId, "face_swap_2");
                log.info("视频2人脸替换完成: aigc_video_2_step0.mp4");
                return result;
            });

            // 等待两个视频都完成人脸替换
            CompletableFuture.allOf(video0Future, video2Future).join();

            String aigcVideo0Url = video0Future.join();      // aigc_video_0.mp4
            String aigcVideo2Step0Url = video2Future.join(); // aigc_video_2_step0.mp4

            // ======================== 步骤5: 唇形同步（视频2）========================

            log.info("步骤5: 唇形同步（视频2 + vocal_2.wav）");
            String aigcVideo2Step1Url = performLipSyncAndWait(aigcVideo2Step0Url, vocal2Url, recordId);
            log.info("唇形同步完成: aigc_video_2_step1.mp4, url={}", aigcVideo2Step1Url);

            // ======================== 步骤6: FFmpeg混入背景音乐 ========================

            log.info("步骤6: FFmpeg混入背景音乐（BGM）");
            String aigcVideo2FinalUrl = performAudioMixing(aigcVideo2Step1Url, BGM_2_URL, recordId);
            log.info("背景音乐混合完成: url={}", aigcVideo2FinalUrl);

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
                    60  // 等待60秒
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
                    60  // 等待60秒
            );

            return (String) result.get("audioUrl");

        } catch (Exception e) {
            log.error("声音合成失败: recordId={}", recordId, e);
            throw new RuntimeException("声音合成失败", e);
        }
    }

    /**
     * 步骤2: 多图生图（同步调用）
     *
     * 注意：多图生图使用同步接口，直接返回结果
     */
    private String performMultiImageGenerateAndWait(String userPhotoUrl, String recordId) {
        try {
            log.info("开始多图生图: userPhotoUrl={}, recordId={}", userPhotoUrl, recordId);

            // 构建请求参数
            MultiImageGenerateRequest request = new MultiImageGenerateRequest();
            request.setImages(java.util.Arrays.asList(userPhotoUrl));  // 用户原始照片
            request.setPrompt(IMAGE_GEN_PROMPT);
            request.setWidth(1440);
            request.setHeight(1440);
            request.setNum(1);

            // 同步调用，直接返回结果
            java.util.List<String> generatedImages = multiImageGenerateService.generate(request);

            if (generatedImages == null || generatedImages.isEmpty()) {
                throw new RuntimeException("多图生图失败：未返回图片URL");
            }

            String aigcPersonUrl = generatedImages.get(0);  // aigc_person.jpg
            log.info("多图生图成功: aigcPersonUrl={}, recordId={}", aigcPersonUrl, recordId);

            return aigcPersonUrl;

        } catch (Exception e) {
            log.error("多图生图失败: recordId={}", recordId, e);
            throw new RuntimeException("多图生图失败", e);
        }
    }

    /**
     * 步骤3/4: 人脸替换并等待回调
     */
    private String performFaceSwapAndWait(String videoUrl, String faceImageUrl, String recordId, String stepName) {
        try {
            FaceSwapRequest request = new FaceSwapRequest();
            request.setVideoUrl(videoUrl);
            request.setFaceImageUrl(faceImageUrl);
            request.setCallbackUrl(buildCallbackUrl(recordId, stepName));

            Map<String, Object> result = videoProcessingService.callAndWaitForCallback(
                    recordId,
                    stepName,
                    () -> faceSwapService.swapFace(request),
                    90  // 等待90秒
            );

            return (String) result.get("targetVideoUrl");

        } catch (Exception e) {
            log.error("人脸替换失败: recordId={}, stepName={}", recordId, stepName, e);
            throw new RuntimeException("人脸替换失败", e);
        }
    }

    /**
     * 步骤5: 唇形同步并等待回调
     */
    private String performLipSyncAndWait(String videoUrl, String audioUrl, String recordId) {
        try {
            LipSyncRequest request = new LipSyncRequest();
            request.setVideoUrl(videoUrl);
            request.setAudioUrl(audioUrl);
            request.setCallbackUrl(buildCallbackUrl(recordId, "lip_sync"));

            Map<String, Object> result = videoProcessingService.callAndWaitForCallback(
                    recordId,
                    "lip_sync",
                    () -> lipSyncService.syncLip(request),
                    90  // 等待90秒
            );

            return (String) result.get("videoUrl");

        } catch (Exception e) {
            log.error("唇形同步失败: recordId={}", recordId, e);
            throw new RuntimeException("唇形同步失败", e);
        }
    }

    /**
     * 步骤6: FFmpeg混入背景音乐
     */
    private String performAudioMixing(String videoUrl, String bgmUrl, String recordId) {
        try {
            log.info("开始混音: videoUrl={}, bgmUrl={}", videoUrl, bgmUrl);

            // 输出URL
            String outputUrl = buildOutputUrl(recordId, "video2_with_bgm");

            // 使用JavaCV混入背景音乐
            videoProcessorUtil.mixAudioWithBgm(videoUrl, bgmUrl, outputUrl);

            log.info("混音完成: outputUrl={}", outputUrl);
            return outputUrl;

        } catch (Exception e) {
            log.error("混音失败: recordId={}", recordId, e);
            throw new RuntimeException("背景音乐混合失败", e);
        }
    }

    /**
     * 步骤7: 视频拼接
     */
    private String performVideoConcatenation(String video0Url, String video2Url, String recordId) {
        try {
            log.info("开始视频拼接: video0={}, video2={}", video0Url, video2Url);

            // 输出URL
            String outputUrl = buildOutputUrl(recordId, "final_result");

            // 拼接视频
            java.util.List<String> videoUrls = java.util.Arrays.asList(video0Url, video2Url);
            videoProcessorUtil.concatVideos(videoUrls, outputUrl);

            log.info("视频拼接完成: outputUrl={}", outputUrl);
            return outputUrl;

        } catch (Exception e) {
            log.error("视频拼接失败: recordId={}", recordId, e);
            throw new RuntimeException("视频拼接失败", e);
        }
    }

    // ======================== 辅助方法 ========================

    /**
     * 构建回调URL
     * 格式：http://your-domain.com/api/callback/xxx?callbackId=recordId:uuid
     *
     * 注意：多图生图使用同步接口，不需要回调URL
     */
    private String buildCallbackUrl(String recordId, String stepName) {
        String callbackId = recordId + ":" + java.util.UUID.randomUUID().toString();

        // TODO: 从配置文件读取服务器地址
        String serverDomain = "http://your-domain.com";

        switch (stepName) {
            case "voice_clone":
                return serverDomain + "/api/callback/voice-clone";
            case "voice_tts":
                return serverDomain + "/api/callback/voice-tts";
            case "face_swap_0":
            case "face_swap_2":
                return serverDomain + "/api/callback/video/face-swap?callbackId=" + callbackId;
            case "lip_sync":
                return serverDomain + "/api/callback/video/lip-sync?callbackId=" + callbackId;
            default:
                throw new IllegalArgumentException("未知的步骤名称: " + stepName);
        }
    }

    /**
     * 构建输出URL
     */
    private String buildOutputUrl(String recordId, String stage) {
        // TODO: 上传到OSS后返回URL
        return "https://your-oss-bucket.com/results/" + recordId + "/" + stage + ".mp4";
    }
}
