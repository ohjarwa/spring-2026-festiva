package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.service.algorithm.*;
import org.springframework.stereotype.Service;

/**
 * 模板1-4流程处理器
 *
 * 流程说明：
 * 1. 用户照片 + 模板视频 → 人脸替换 → 替换后视频
 * 2. 用户音频 → 声音克隆 → voice_id
 * 3. voice_id + 固定文案 → 声音合成 → 合成音频
 * 4. 替换后视频 + 合成音频 → 唇形同步 → 最终视频
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

    @Override
    public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {
        log.info("开始处理模板1-4流程: recordId={}, templateId={}", recordId, template.getTemplateId());

        try {
            String userPhotoUrl = dto.getMaterials().getPhotos().get(0);
            String userAudioUrl = dto.getMaterials().getAudios().get(0);
            String templateVideoUrl = template.getTemplateUrl();

            // 步骤1: 人脸替换
            log.info("步骤1: 人脸替换");
            String faceSwapVideoUrl = performFaceSwap(templateVideoUrl, userPhotoUrl, recordId);

            // 步骤2: 声音克隆
            log.info("步骤2: 声音克隆");
            String voiceId = performVoiceClone(userAudioUrl, recordId);

            // 步骤3: 声音合成
            log.info("步骤3: 声音合成");
            String synthesizedAudioUrl = performVoiceTts(voiceId, recordId);

            // 步骤4: 唇形同步
            log.info("步骤4: 唇形同步");
            String finalVideoUrl = performLipSync(faceSwapVideoUrl, synthesizedAudioUrl, recordId);

            log.info("模板1-4流程处理完成: finalVideoUrl={}", finalVideoUrl);
            return finalVideoUrl;

        } catch (Exception e) {
            log.error("模板1-4流程处理失败: recordId={}", recordId, e);
            throw new RuntimeException("模板处理失败", e);
        }
    }

    /**
     * 人脸替换
     */
    private String performFaceSwap(String videoUrl, String faceImageUrl, String recordId) {
        FaceSwapRequest request = new FaceSwapRequest();
        request.setVideoUrl(videoUrl);
        request.setFaceImageUrl(faceImageUrl);
        request.setCallbackUrl(buildCallbackUrl(recordId, "face_swap"));

        AlgorithmResponse response = faceSwapService.swapFace(request);

        if (response.getCode() == 0 && response.getData() != null) {
            return response.getData().getResultUrl();
        } else {
            throw new RuntimeException("人脸替换失败: " + response.getMessage());
        }
    }

    /**
     * 声音克隆
     */
    private String performVoiceClone(String audioUrl, String recordId) {
        VoiceCloneRequest request = new VoiceCloneRequest();
        request.setAudioUrl(audioUrl);
        request.setCallbackUrl(buildCallbackUrl(recordId, "voice_clone"));

        AlgorithmResponse response = voiceCloneService.cloneVoice(request);

        if (response.getCode() == 0 && response.getData() != null) {
            return response.getData().getTaskId(); // 返回voice_id
        } else {
            throw new RuntimeException("声音克隆失败: " + response.getMessage());
        }
    }

    /**
     * 声音合成
     */
    private String performVoiceTts(String voiceId, String recordId) {
        VoiceTtsRequest request = new VoiceTtsRequest();
        request.setVoiceId(voiceId);
        request.setText("春节快乐，万事如意，恭喜发财！"); // 固定文案
        request.setCallbackUrl(buildCallbackUrl(recordId, "voice_tts"));

        AlgorithmResponse response = voiceTtsService.synthesizeVoice(request);

        if (response.getCode() == 0 && response.getData() != null) {
            return response.getData().getResultUrl();
        } else {
            throw new RuntimeException("声音合成失败: " + response.getMessage());
        }
    }

    /**
     * 唇形同步
     */
    private String performLipSync(String videoUrl, String audioUrl, String recordId) {
        LipSyncRequest request = new LipSyncRequest();
        request.setVideoUrl(videoUrl);
        request.setAudioUrl(audioUrl);
        request.setCallbackUrl(buildCallbackUrl(recordId, "lip_sync"));

        AlgorithmResponse response = lipSyncService.syncLip(request);

        if (response.getCode() == 0 && response.getData() != null) {
            return response.getData().getResultUrl();
        } else {
            throw new RuntimeException("唇形同步失败: " + response.getMessage());
        }
    }

    /**
     * 构建回调URL
     */
    private String buildCallbackUrl(String recordId, String stepName) {
        // TODO: 从配置文件读取服务器地址
        return "http://your-domain.com/api/callback/video?recordId=" + recordId + "&step=" + stepName;
    }
}
