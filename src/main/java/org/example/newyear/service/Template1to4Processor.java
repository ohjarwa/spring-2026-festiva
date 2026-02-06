package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.entity.algorithm.vision.ImageRatio;
import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.entity.task.TaskResultStatus;
import org.example.newyear.util.KeyGeneratorUtils;
import org.example.newyear.service.oss.OssService;
import org.example.newyear.util.VideoProcessorUtil;
import org.example.newyear.service.task.TaskOrchestrator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
 * 使用 TaskOrchestrator 系统管理 Vision 算法任务
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service("template1to4Processor")
@RequiredArgsConstructor
public class Template1to4Processor implements ITemplateProcessor {

    private final SongConversionFacade songConversionFacade;
    private final FeatureExtractionFacade featureExtractionFacade;
    private final VisionFacade visionFacade;
    private final VideoProcessorUtil videoProcessorUtil;
    private final TaskOrchestrator taskOrchestrator;
    private final OssService ossService;

    // ======================== 固定素材URL配置（后续从OSS获取）========================

    /**
     * 视频片段0 - 用于人物替换
     */
    private static final String SRC_VIDEO_0_PATH = "spring2026/source/template_1_video_1_silence.mp4";

    /**
     * 视频片段2 - 先人物替换，再Lipsync
     */
    private static final String SRC_VIDEO_2_PATH = "spring2026/source/template_1_video_2_silence.mp4";

    /**
     * 背景音乐 - 片段2对应的背景音乐
     */
    private static final String BGM_2_URL = "spring2026/source/template_1_audio_2.MP3";

    /**
     * 固定文案 - 用于语音合成
     */
    private static final String FIXED_TEXT = "春节快乐，万事如意，恭喜发财！";

    /**
     * 图生图预设提示词
     * 根据实际需求调整，描述生成图片的风格和内容
     */
    private static final String IMAGE_GEN_PROMPT = "Spring Festival theme, festive atmosphere, high quality portrait, Chinese New Year celebration";

    /**
     * 默认超时时间（30分钟）
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);

    @Override
    public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {
        log.info("开始处理模板1-4流程: recordId={}, templateId={}", recordId, template.getTemplateId());
        //任务数据结构化落库
        try {
            // 获取用户上传素材
            String userPhotoUrl = dto.getMaterials().getPhotos().get(0);  // src_person.jpg
            String userAudioUrl = dto.getMaterials().getAudios().get(0);  // 用户原始语音

            // ======================== 步骤1: 音频服务（两步）========================

            // 步骤1.1: 歌曲转换 → vocal_2.wav
            log.info("步骤1.1: 歌曲转换");
            String vocal2Url = performSongConversion(userAudioUrl);
            log.info("歌曲转换完成: vocal2Url={}", vocal2Url);

            // 步骤1.2: 人声转换（特征提取）→ featureId
            log.info("步骤1.2: 人声转换（特征提取）");
            String featureId = performVoiceConversion(userAudioUrl);
            log.info("人声转换完成: featureId={}", featureId);

            // ======================== 步骤2: Flux2多图生图 ========================

            log.info("步骤2: Flux2多图生图（图生图算法）");
            String aigcPersonUrl = performFlux2ImageGen(userPhotoUrl);
            log.info("Flux2多图生图完成: aigcPersonUrl={}", aigcPersonUrl);

            // ======================== 步骤3: WanAnimate人物替换（视频0）========================

            log.info("步骤3: WanAnimate人物替换（视频0）");
            String aigcVideo0Url = performWanAnimate(ossService.getAccessUrl(SRC_VIDEO_0_PATH), aigcPersonUrl);
            log.info("视频0人物替换完成: aigcVideo0Url={}", aigcVideo0Url);

            // ======================== 步骤4: WanAnimate人物替换（视频2）========================

            log.info("步骤4: WanAnimate人物替换（视频2）");
            String aigcVideo2Step0Url = performWanAnimate(ossService.getAccessUrl(SRC_VIDEO_2_PATH), aigcPersonUrl);
            log.info("视频2人物替换完成: aigcVideo2Step0Url={}", aigcVideo2Step0Url);

            // ======================== 步骤5: Lipsync唇形同步（视频2）========================

            log.info("步骤5: Lipsync唇形同步（视频2 + vocal_2.wav）");
            String aigcVideo2Step1Url = performLipsync(aigcVideo2Step0Url, vocal2Url);
            log.info("唇形同步完成: aigcVideo2Step1Url={}", aigcVideo2Step1Url);

            // ======================== 步骤6: FFmpeg混入背景音乐 ========================

            log.info("步骤6: FFmpeg混入背景音乐（BGM）");
            String aigcVideo2FinalUrl = performAudioMixing(aigcVideo2Step1Url, ossService.getAccessUrl(BGM_2_URL), recordId);
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
     * 步骤1.1: 歌曲转换
     * 使用 TaskOrchestrator 系统管理
     * 注意：这里需要提供预置的 bgmUrl 和 modelCode
     */
    private String performSongConversion(String userAudioUrl) throws TimeoutException {
        log.info("开始歌曲转换: userAudioUrl={}", userAudioUrl);

        // 1. 生成 taskId
        String taskId = KeyGeneratorUtils.taskIdGen();
        log.info("生成 taskId: {}, algorithm=song_conversion", taskId);

        // 2. 初始化任务（在 SongConversionFacade.submit 中完成）
        // taskOrchestrator.initTask(taskId, AlgorithmEnum.SONG_CONVERSION);

        // 3. 提交算法任务
        log.info("提交歌曲转换任务: taskId={}", taskId);

        // TODO: 需要提供预置的 bgmUrl 和 modelCode
        // 暂时使用占位符
        String bgmUrl = BGM_2_URL;  // 使用背景音乐URL
        String modelCode = "default_model";  // TODO: 需要配置预置模型

        songConversionFacade.submit(
                userAudioUrl,  // audioUrl - 人声下载地址
                bgmUrl,        // bgmUrl - 背景音下载地址
                null,          // voiceUrl - 提取特征的原音频（可选）
                modelCode,     // modelCode - 模型编号
                taskId         // taskId
        );

        // 4. 等待结果
        TaskResult result = taskOrchestrator.awaitTask(taskId, AlgorithmEnum.SONG_CONVERSION, DEFAULT_TIMEOUT);

        // 5. 检查结果
        if (!result.isSuccess()) {
            throw new RuntimeException("歌曲转换失败: " + result.getErrorMessage());
        }

        // 6. 获取生成的音频URL
        String resultUrl = result.getAudioUrl();
        if (resultUrl == null || resultUrl.isEmpty()) {
            throw new RuntimeException("歌曲转换失败：未返回音频URL");
        }

        log.info("歌曲转换成功: resultUrl={}", resultUrl);

        // 7. 清理任务
        taskOrchestrator.cleanupTask(taskId, AlgorithmEnum.SONG_CONVERSION);

        return resultUrl;
    }

    /**
     * 步骤1.2: 人声转换（特征提取）
     * 使用 TaskOrchestrator 系统管理
     */
    private String performVoiceConversion(String userAudioUrl) throws TimeoutException {
        log.info("开始人声转换（特征提取）: userAudioUrl={}", userAudioUrl);

        // 1. 生成 taskId
        String taskId = KeyGeneratorUtils.taskIdGen();
        log.info("生成 taskId: {}, algorithm=voice_conversion", taskId);

        // 2. 初始化任务（在 FeatureExtractionFacade.submit 中完成）
        // taskOrchestrator.initTask(taskId, AlgorithmEnum.VOICE_CONVERSION);

        // 3. 提交算法任务
        log.info("提交人声转换任务: taskId={}", taskId);
        featureExtractionFacade.submit(userAudioUrl, taskId);

        // 4. 等待结果
        TaskResult result = taskOrchestrator.awaitTask(taskId, AlgorithmEnum.VOICE_CONVERSION, DEFAULT_TIMEOUT);

        // 5. 检查结果
        if (!result.isSuccess()) {
            throw new RuntimeException("人声转换失败: " + result.getErrorMessage());
        }

        // 6. 获取生成的特征ID
        String featureId = result.getData("featureId", String.class);
        if (featureId == null || featureId.isEmpty()) {
            throw new RuntimeException("人声转换失败：未返回特征ID");
        }

        log.info("人声转换成功: featureId={}", featureId);

        // 7. 清理任务
        taskOrchestrator.cleanupTask(taskId, AlgorithmEnum.VOICE_CONVERSION);

        return featureId;
    }

    /**
     * 步骤2: Flux2多图生图
     * 使用 TaskOrchestrator 系统管理
     */
    private String performFlux2ImageGen(String userPhotoUrl) throws TimeoutException {
        log.info("开始Flux2多图生图: userPhotoUrl={}", userPhotoUrl);

        // 1. 生成 taskId
        String taskId = KeyGeneratorUtils.taskIdGen();
        log.info("生成 taskId: {}, algorithm=flux2_image_gen", taskId);

        // 2. 初始化任务
        taskOrchestrator.initTask(taskId, AlgorithmEnum.FLUX2_IMAGE_GEN);

        // 3. 提交算法任务
        log.info("提交 Flux2 图生图任务: taskId={}", taskId);
        visionFacade.submitImageToImageMulti(
                IMAGE_GEN_PROMPT,
                Arrays.asList(userPhotoUrl),
                ImageRatio.RATIO_1_1,
                taskId
        );

        // 4. 等待结果
        TaskResult result = taskOrchestrator.awaitTask(taskId, AlgorithmEnum.FLUX2_IMAGE_GEN, DEFAULT_TIMEOUT);

        // 5. 检查结果
        if (!result.isSuccess()) {
            throw new RuntimeException("Flux2生图失败: " + result.getErrorMessage());
        }

        // 6. 获取生成的图片URL
        @SuppressWarnings("unchecked")
        List<String> targetImageUrls = (List<String>) result.getData().get("targetImageUrls");

        if (targetImageUrls == null || targetImageUrls.isEmpty()) {
            throw new RuntimeException("Flux2生图失败：未返回图片URL");
        }

        String aigcPersonUrl = targetImageUrls.get(0);  // aigc_person.jpg
        log.info("Flux2生图成功: aigcPersonUrl={}", aigcPersonUrl);

        // 7. 清理任务
        taskOrchestrator.cleanupTask(taskId, AlgorithmEnum.FLUX2_IMAGE_GEN);

        return aigcPersonUrl;
    }

    /**
     * 步骤3/4: WanAnimate人物替换
     * 使用 TaskOrchestrator 系统管理
     *
     * @param videoUrl    源视频URL
     * @param faceImageUrl 人物图片URL
     * @return 替换后的视频URL
     */
    private String performWanAnimate(String videoUrl, String faceImageUrl) throws TimeoutException {
        log.info("开始WanAnimate人物替换: videoUrl={}, faceImageUrl={}", videoUrl, faceImageUrl);

        // 1. 生成 taskId
        String taskId = KeyGeneratorUtils.taskIdGen();
        log.info("生成 taskId: {}, algorithm=wan_animate", taskId);

        // 2. 初始化任务
        taskOrchestrator.initTask(taskId, AlgorithmEnum.WAN_ANIMATE);

        // 3. 提交算法任务
        log.info("提交 WanAnimate 人物替换任务: taskId={}", taskId);
        visionFacade.submitWanAnimate(faceImageUrl, videoUrl, taskId);

        // 4. 等待结果
        TaskResult result = taskOrchestrator.awaitTask(taskId, AlgorithmEnum.WAN_ANIMATE, DEFAULT_TIMEOUT);

        // 5. 检查结果
        if (!result.isSuccess()) {
            throw new RuntimeException("WanAnimate人物替换失败: " + result.getErrorMessage());
        }

        // 6. 获取生成的视频URL
        String targetVideoUrl = result.getVideoUrl();
        if (targetVideoUrl == null || targetVideoUrl.isEmpty()) {
            throw new RuntimeException("WanAnimate人物替换失败：未返回视频URL");
        }

        log.info("WanAnimate人物替换成功: targetVideoUrl={}", targetVideoUrl);

        // 7. 清理任务
        taskOrchestrator.cleanupTask(taskId, AlgorithmEnum.WAN_ANIMATE);

        return targetVideoUrl;
    }

    /**
     * 步骤5: Lipsync唇形同步
     * 使用 TaskOrchestrator 系统管理
     */
    private String performLipsync(String videoUrl, String audioUrl) throws TimeoutException {
        log.info("开始Lipsync唇形同步: videoUrl={}, audioUrl={}", videoUrl, audioUrl);

        // 1. 生成 taskId
        String taskId = KeyGeneratorUtils.taskIdGen();
        log.info("生成 taskId: {}, algorithm=lips_sync", taskId);

        // 2. 初始化任务
        taskOrchestrator.initTask(taskId, AlgorithmEnum.LIPS_SYNC);

        // 3. 提交算法任务
        log.info("提交 Lipsync 唇形同步任务: taskId={}", taskId);
        visionFacade.submitLipsync(videoUrl, audioUrl, taskId);

        // 4. 等待结果
        TaskResult result = taskOrchestrator.awaitTask(taskId, AlgorithmEnum.LIPS_SYNC, DEFAULT_TIMEOUT);

        // 5. 检查结果
        if (!result.isSuccess()) {
            throw new RuntimeException("Lipsync唇形同步失败: " + result.getErrorMessage());
        }

        // 6. 获取生成的视频URL
        String videoResultUrl = result.getVideoUrl();
        if (videoResultUrl == null || videoResultUrl.isEmpty()) {
            throw new RuntimeException("Lipsync唇形同步失败：未返回视频URL");
        }

        log.info("Lipsync唇形同步成功: videoResultUrl={}", videoResultUrl);

        // 7. 清理任务
        taskOrchestrator.cleanupTask(taskId, AlgorithmEnum.LIPS_SYNC);

        return videoResultUrl;
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
            List<String> videoUrls = Arrays.asList(video0Url, video2Url);
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
}
