package org.example.newyear.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.vision.VisionCallbackResponse;
import org.example.newyear.dto.callback.*;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频处理服务（核心流程编排 + 回调处理）
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    private final Spring2026CreationRecordMapper recordMapper;
    private final TemplateService templateService;
    private final ApplicationContext applicationContext;
    private final CallbackResultManager callbackResultManager;

    // 存储等待回调的CountDownLatch
    private final Map<String, CountDownLatch> callbackLatches = new ConcurrentHashMap<>();
    // 存储回调结果（临时，用于CountDownLatch等待获取）
    private final Map<String, Object> callbackResults = new ConcurrentHashMap<>();

    // ======================== 视频生成流程 ========================

    /**
     * 异步处理视频生成任务
     */
    @Async("videoTaskExecutor")
    public void processVideoCreation(String recordId, String userId, VideoCreateDTO dto) {
        log.info("开始处理视频生成任务: recordId={}, userId={}, templateId={}",
                recordId, userId, dto.getTemplateId());

        try {
            // 1. 更新状态：生成中
            updateRecordStatus(recordId, 1, 0);

            // 2. 获取模板信息
            Spring2026Template template = templateService.getTemplateById(dto.getTemplateId());

            // 3. 根据模板ID选择对应的处理器
            ITemplateProcessor processor = getTemplateProcessor(template.getTemplateId());

            // 4. 执行模板流程
            String finalVideoUrl = processor.process(recordId, template, dto);

            // 5. 更新最终结果
            updateRecordComplete(recordId, finalVideoUrl);
            log.info("视频生成完成: recordId={}, url={}", recordId, finalVideoUrl);

        } catch (Exception e) {
            log.error("视频生成失败: recordId={}", recordId, e);
            updateRecordError(recordId, e.getMessage());
        }
    }

    /**
     * 根据模板ID获取对应的处理器
     */
    private ITemplateProcessor getTemplateProcessor(String templateId) {
        String templateNum = templateId.replace("tpl_", "");

        try {
            int num = Integer.parseInt(templateNum);

            if (num >= 1 && num <= 4) {
                return applicationContext.getBean("template1to4Processor", ITemplateProcessor.class);
            } else if (num >= 5 && num <= 8) {
                return applicationContext.getBean("template5to8Processor", ITemplateProcessor.class);
            } else {
                throw new BusinessException(40007, "不支持的模板编号: " + num);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(40007, "无效的模板ID格式: " + templateId);
        }
    }

    // ======================== 回调处理方法 ========================

    /**
     * 人脸替换回调处理
     */
    public void notifyFaceSwapCallback(VideoAlgorithmCallbackResponse response, FaceSwapCallbackData data, String callbackId) {
        // 从callbackId中提取recordId
        // 格式：callbackId = "recordId:uuid" 或 "recordId:stepName"
        String[] parts = callbackId.split(":");
        if (parts.length < 1) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = "face_swap";

        log.info("处理人脸替换回调: recordId={}, targetVideoUrl={}",
                recordId, data.getTargetVideoUrl());

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.getCode() == 0);
        result.put("targetVideoUrl", data.getTargetVideoUrl());
        result.put("timestamp", System.currentTimeMillis());

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        log.info("人脸替换产物已保存: recordId={}, stepName={}, url={}",
                recordId, stepName, data.getTargetVideoUrl());

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * 多图生图回调处理
     */
    public void notifyMultiImageGenerateCallback(VideoAlgorithmCallbackResponse response, MultiImageGenerateCallbackData data, String callbackId) {
        // 从callbackId中提取recordId
        String[] parts = callbackId.split(":");
        if (parts.length < 1) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = "multi_image_generate";

        log.info("处理多图生图回调: recordId={}, fileUrls count={}",
                recordId, data.getFileUrls() != null ? data.getFileUrls().size() : 0);

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.getCode() == 0);
        result.put("fileUrls", data.getFileUrls());
        result.put("timestamp", System.currentTimeMillis());

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * 唇形同步回调处理
     */
    public void notifyLipSyncCallback(VideoAlgorithmCallbackResponse response, LipSyncCallbackData data, String callbackId) {
        // 从callbackId中提取recordId
        String[] parts = callbackId.split(":");
        if (parts.length < 1) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = "lip_sync";

        log.info("处理唇形同步回调: recordId={}, videoUrl={}, code={}, message={}",
                recordId, data.getVideoUrl(), data.getCode(), data.getMessage());

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        result.put("success", data.getCode() == 0);
        result.put("videoUrl", data.getVideoUrl());
        result.put("code", data.getCode());
        result.put("message", data.getMessage());
        result.put("timestamp", System.currentTimeMillis());

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    // ======================== 语音算法回调处理 ========================

    /**
     * 声音克隆回调处理
     */
    public void notifyVoiceCloneCallback(VoiceCloneCallbackDTO callback) {
        String callbackId = callback.getCallbackId();

        // 从callbackId中提取recordId
        // 格式：callbackId = "recordId:uuid"
        String[] parts = callbackId.split(":");
        if (parts.length < 1) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = "voice_clone";

        log.info("处理声音克隆回调: recordId={}, status={}, voiceId={}",
                recordId, callback.getStatus(), callback.getVoiceId());

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        result.put("success", "success".equals(callback.getStatus()));
        result.put("voiceId", callback.getVoiceId());
        result.put("errorMsg", callback.getErrorMsg());
        result.put("timestamp", System.currentTimeMillis());

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * 声音合成TTS回调处理
     */
    public void notifyVoiceTtsCallback(VoiceTtsCallbackDTO callback) {
        String callbackId = callback.getCallbackId();

        // 从callbackId中提取recordId
        String[] parts = callbackId.split(":");
        if (parts.length < 1) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = "voice_tts";

        log.info("处理声音合成回调: recordId={}, status={}, audioUrl={}",
                recordId, callback.getStatus(), callback.getAudioUrl());

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        result.put("success", "success".equals(callback.getStatus()));
        result.put("audioUrl", callback.getAudioUrl());
        result.put("errorMsg", callback.getErrorMsg());
        result.put("timestamp", System.currentTimeMillis());

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * 歌曲特征提取回调处理
     */
    public void notifySongFeatureExtractCallback(SongFeatureExtractCallbackDTO callback) {
        String callbackId = callback.getCallbackId();

        // 从callbackId中提取recordId
        String[] parts = callbackId.split(":");
        if (parts.length < 1) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = "song_feature_extract";

        log.info("处理歌曲特征提取回调: recordId={}, status={}, featureSize={}",
                recordId, callback.getStatus(),
                callback.getFeature() != null ? callback.getFeature().size() : 0);

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        result.put("success", "success".equals(callback.getStatus()));
        result.put("feature", callback.getFeature());
        result.put("errorMsg", callback.getErrorMsg());
        result.put("timestamp", System.currentTimeMillis());

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    // ======================== Vision算法回调处理 ========================

    /**
     * WanAnimate人物替换回调处理
     */
    public void notifyWanAnimateCallback(VisionCallbackResponse<JsonNode> callback, String callbackId) {
        // 从callbackId中提取recordId和stepName
        // 格式：callbackId = "recordId:stepName" 或 "recordId:stepName:uuid"
        String[] parts = callbackId.split(":");
        if (parts.length < 2) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = parts[1];  // 例如: "face_swap_0" 或 "face_swap_2"

        log.info("处理WanAnimate回调: recordId={}, stepName={}", recordId, stepName);

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        boolean isSuccess = callback.isSuccess() && callback.getTaskStatus() == 3;

        result.put("success", isSuccess);
        result.put("timestamp", System.currentTimeMillis());

        if (isSuccess && callback.getData() != null) {
            JsonNode data = callback.getData();
            // 提取 targetVideoUrl
            if (data.has("targetVideoUrl")) {
                String targetVideoUrl = data.get("targetVideoUrl").asText();
                result.put("targetVideoUrl", targetVideoUrl);
                log.info("WanAnimate成功: recordId={}, stepName={}, url={}", recordId, stepName, targetVideoUrl);
            } else {
                log.warn("WanAnimate回调未包含targetVideoUrl: recordId={}, stepName={}", recordId, stepName);
                result.put("success", false);
            }
        } else {
            String errorMsg = callback.getMessage();
            result.put("errorMsg", errorMsg);
            log.warn("WanAnimate失败: recordId={}, stepName={}, errorMsg={}", recordId, stepName, errorMsg);
        }

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * Flux2多图生图回调处理
     */
    public void notifyFlux2ImageGenCallback(VisionCallbackResponse<JsonNode> callback, String callbackId) {
        // 从callbackId中提取recordId和stepName
        String[] parts = callbackId.split(":");
        if (parts.length < 2) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = parts[1];  // "flux2_image_gen"

        log.info("处理Flux2ImageGen回调: recordId={}, stepName={}", recordId, stepName);

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        boolean isSuccess = callback.isSuccess() && callback.getTaskStatus() == 3;

        result.put("success", isSuccess);
        result.put("timestamp", System.currentTimeMillis());

        if (isSuccess && callback.getData() != null) {
            JsonNode data = callback.getData();
            // 提取 targetImageUrls 或 targetImageUrl
            if (data.has("targetImageUrls")) {
                JsonNode urlsNode = data.get("targetImageUrls");
                List<String> urls = new ArrayList<>();
                if (urlsNode.isArray()) {
                    for (JsonNode urlNode : urlsNode) {
                        urls.add(urlNode.asText());
                    }
                }
                result.put("targetImageUrls", urls);
                log.info("Flux2ImageGen成功: recordId={}, stepName={}, count={}", recordId, stepName, urls.size());
            } else if (data.has("targetImageUrl")) {
                String targetImageUrl = data.get("targetImageUrl").asText();
                result.put("targetImageUrl", targetImageUrl);
                result.put("targetImageUrls", Arrays.asList(targetImageUrl));
                log.info("Flux2ImageGen成功(单图): recordId={}, stepName={}, url={}", recordId, stepName, targetImageUrl);
            } else {
                log.warn("Flux2ImageGen回调未包含图片URL: recordId={}, stepName={}", recordId, stepName);
                result.put("success", false);
            }
        } else {
            String errorMsg = callback.getMessage();
            result.put("errorMsg", errorMsg);
            log.warn("Flux2ImageGen失败: recordId={}, stepName={}, errorMsg={}", recordId, stepName, errorMsg);
        }

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * Lipsync唇形同步回调处理
     */
    public void notifyLipsyncCallback(VisionCallbackResponse<JsonNode> callback, String callbackId) {
        // 从callbackId中提取recordId和stepName
        String[] parts = callbackId.split(":");
        if (parts.length < 2) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = parts[1];  // "lipsync"

        log.info("处理Lipsync回调: recordId={}, stepName={}", recordId, stepName);

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        boolean isSuccess = callback.isSuccess() && callback.getTaskStatus() == 3;

        result.put("success", isSuccess);
        result.put("timestamp", System.currentTimeMillis());

        if (isSuccess && callback.getData() != null) {
            JsonNode data = callback.getData();
            // 提取 videoUrl
            if (data.has("videoUrl")) {
                String videoUrl = data.get("videoUrl").asText();
                result.put("videoUrl", videoUrl);
                log.info("Lipsync成功: recordId={}, stepName={}, url={}", recordId, stepName, videoUrl);
            } else {
                log.warn("Lipsync回调未包含videoUrl: recordId={}, stepName={}", recordId, stepName);
                result.put("success", false);
            }
        } else {
            String errorMsg = callback.getMessage();
            result.put("errorMsg", errorMsg);
            log.warn("Lipsync失败: recordId={}, stepName={}, errorMsg={}", recordId, stepName, errorMsg);
        }

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    /**
     * WanVideoFLF首尾帧回调处理
     */
    public void notifyWanVideoFLFCallback(VisionCallbackResponse<JsonNode> callback, String callbackId) {
        // 从callbackId中提取recordId和stepName
        String[] parts = callbackId.split(":");
        if (parts.length < 2) {
            log.warn("callbackId格式错误: {}", callbackId);
            return;
        }

        String recordId = parts[0];
        String stepName = parts[1];  // "wan_video_flf"

        log.info("处理WanVideoFLF回调: recordId={}, stepName={}", recordId, stepName);

        // 保存回调产物
        Map<String, Object> result = new HashMap<>();
        boolean isSuccess = callback.isSuccess() && callback.getTaskStatus() == 3;

        result.put("success", isSuccess);
        result.put("timestamp", System.currentTimeMillis());

        if (isSuccess && callback.getData() != null) {
            JsonNode data = callback.getData();
            // 提取 targetVideoUrl
            if (data.has("targetVideoUrl")) {
                String targetVideoUrl = data.get("targetVideoUrl").asText();
                result.put("targetVideoUrl", targetVideoUrl);
                log.info("WanVideoFLF成功: recordId={}, stepName={}, url={}", recordId, stepName, targetVideoUrl);
            } else {
                log.warn("WanVideoFLF回调未包含targetVideoUrl: recordId={}, stepName={}", recordId, stepName);
                result.put("success", false);
            }
        } else {
            String errorMsg = callback.getMessage();
            result.put("errorMsg", errorMsg);
            log.warn("WanVideoFLF失败: recordId={}, stepName={}, errorMsg={}", recordId, stepName, errorMsg);
        }

        // 存储到Redis（持久化）
        callbackResultManager.saveResult(recordId, stepName, result);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(recordId + ":" + stepName, result);

        // 唤醒等待
        wakeupLatch(recordId, stepName);
    }

    // ======================== 等待回调方法 ========================

    /**
     * 调用算法服务并等待回调（通用方法）
     *
     * @param recordId      记录ID
     * @param stepName      步骤名称
     * @param callSupplier  算法调用逻辑
     * @param timeoutSeconds 超时时间（秒）
     * @return 回调结果
     */
    public Map<String, Object> callAndWaitForCallback(
            String recordId,
            String stepName,
            java.util.function.Supplier<Object> callSupplier,
            int timeoutSeconds) throws Exception {

        // 1. 创建CountDownLatch
        String latchKey = recordId + ":" + stepName;
        CountDownLatch latch = new CountDownLatch(1);
        callbackLatches.put(latchKey, latch);

        try {
            // 2. 调用算法服务
            log.info("调用算法服务: recordId={}, stepName={}", recordId, stepName);
            Object response = callSupplier.get();

            // 3. 等待回调（阻塞当前线程）
            boolean success = latch.await(timeoutSeconds, TimeUnit.SECONDS);

            if (!success) {
                throw new RuntimeException("等待算法回调超时: recordId=" + recordId + ", step=" + stepName);
            }

            // 4. 获取回调结果
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) callbackResults.remove(latchKey);

            if (result == null) {
                throw new RuntimeException("未收到回调结果: recordId=" + recordId + ", step=" + stepName);
            }

            Boolean isSuccess = (Boolean) result.get("success");
            if (isSuccess == null || !isSuccess) {
                String errorMsg = (String) result.get("errorMsg");
                throw new RuntimeException("算法处理失败: " + errorMsg);
            }

            log.info("算法处理成功: recordId={}, stepName={}", recordId, stepName);
            return result;

        } finally {
            // 清理
            callbackLatches.remove(latchKey);
        }
    }

    // ======================== 辅助方法 ========================

    /**
     * 唤醒CountDownLatch
     */
    private void wakeupLatch(String taskId, String stepName) {
        String latchKey = taskId + ":" + stepName;
        CountDownLatch latch = callbackLatches.remove(latchKey);
        if (latch != null) {
            latch.countDown();
            log.info("唤醒等待线程: taskId={}, stepName={}", taskId, stepName);
        }
    }

    /**
     * 从recordId和stepName构建callbackId
     */
    private String buildCallbackId(String recordId, String stepName) {
        return recordId + ":" + stepName;
    }

    /**
     * 更新记录状态
     */
    private void updateRecordStatus(String recordId, int status, int progress) {
        recordMapper.updateById(
                Spring2026CreationRecord.builder()
                        .recordId(recordId)
                        .status(status)
                        .progress(progress)
                        .build()
        );
    }

    /**
     * 更新记录完成
     */
    private void updateRecordComplete(String recordId, String resultUrl) {
        recordMapper.updateById(
                Spring2026CreationRecord.builder()
                        .recordId(recordId)
                        .status(2)
                        .progress(100)
                        .resultUrl(resultUrl)
                        .build()
        );
    }

    /**
     * 更新记录错误
     */
    private void updateRecordError(String recordId, String errorMessage) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error_code", "PROCESSING_FAILED");
        errorMap.put("error_message", errorMessage);
        errorMap.put("can_retry", true);

        recordMapper.updateById(
                Spring2026CreationRecord.builder()
                        .recordId(recordId)
                        .status(3)
                        .errorInfo(JsonUtil.toJson(errorMap))
                        .build()
        );
    }

    /**
     * 更新任务执行详情
     */
    private void updateTaskExecution(String recordId, String stepName, String status, String resultUrl) {
        Spring2026CreationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            return;
        }

        Map<String, Object> execution;
        try {
            if (record.getTaskExecution() != null) {
                execution = JsonUtil.fromJson(record.getTaskExecution(), new TypeReference<Map<String, Object>>() {});
            } else {
                execution = new HashMap<>();
                Map<String, Object> stepsMap = new HashMap<>();
                execution.put("steps", stepsMap);
            }
        } catch (Exception e) {
            execution = new HashMap<>();
            Map<String, Object> stepsMap = new HashMap<>();
            execution.put("steps", stepsMap);
        }

        Map<String, Object> steps = (Map<String, Object>) execution.get("steps");
        Map<String, Object> stepInfo = new HashMap<>();
        stepInfo.put("status", status);
        stepInfo.put("result_url", resultUrl);
        stepInfo.put("end_time", System.currentTimeMillis());
        steps.put(stepName, stepInfo);

        record.setTaskExecution(JsonUtil.toJson(execution));
        recordMapper.updateById(record);
    }
}
