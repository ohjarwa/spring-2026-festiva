package org.example.newyear.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.callback.*;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationContext applicationContext;

    // 存储等待回调的CountDownLatch
    private final Map<String, CountDownLatch> callbackLatches = new ConcurrentHashMap<>();
    // 存储回调结果
    private final Map<String, Object> callbackResults = new ConcurrentHashMap<>();

    private static final String CALLBACK_CACHE_PREFIX = "callback:";
    private static final int CACHE_TTL = 3600; // 1小时

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
    public void notifyFaceSwapCallback(VideoAlgorithmCallbackResponse response, FaceSwapCallbackData data) {
        String taskId = extractTaskId(response);
        log.info("处理人脸替换回调: taskId={}, targetVideoUrl={}", taskId, data.getTargetVideoUrl());

        // 存储回调结果
        storeCallbackResult(taskId, "face_swap", Map.of(
                "success", response.getCode() == 0,
                "targetVideoUrl", data.getTargetVideoUrl()
        ));

        // 唤醒等待
        wakeupLatch(taskId, "face_swap");
    }

    /**
     * 多图生图回调处理
     */
    public void notifyMultiImageGenerateCallback(VideoAlgorithmCallbackResponse response, MultiImageGenerateCallbackData data) {
        String taskId = extractTaskId(response);
        log.info("处理多图生图回调: taskId={}, fileUrls count={}",
                taskId, data.getFileUrls() != null ? data.getFileUrls().size() : 0);

        // 存储回调结果
        storeCallbackResult(taskId, "multi_image_generate", Map.of(
                "success", response.getCode() == 0,
                "fileUrls", data.getFileUrls()
        ));

        // 唤醒等待
        wakeupLatch(taskId, "multi_image_generate");
    }

    /**
     * 唇形同步回调处理
     */
    public void notifyLipSyncCallback(VideoAlgorithmCallbackResponse response, LipSyncCallbackData data) {
        String taskId = extractTaskId(response);
        log.info("处理唇形同步回调: taskId={}, videoUrl={}, code={}, message={}",
                taskId, data.getVideoUrl(), data.getCode(), data.getMessage());

        // 存储回调结果
        storeCallbackResult(taskId, "lip_sync", Map.of(
                "success", data.getCode() == 0,
                "videoUrl", data.getVideoUrl(),
                "code", data.getCode(),
                "message", data.getMessage()
        ));

        // 唤醒等待
        wakeupLatch(taskId, "lip_sync");
    }

    // ======================== 语音算法回调处理 ========================

    /**
     * 声音克隆回调处理
     */
    public void notifyVoiceCloneCallback(VoiceCloneCallbackDTO callback) {
        String taskId = callback.getTaskId();
        log.info("处理声音克隆回调: taskId={}, status={}, voiceId={}",
                taskId, callback.getStatus(), callback.getVoiceId());

        // 存储回调结果
        storeCallbackResult(taskId, "voice_clone", Map.of(
                "success", "success".equals(callback.getStatus()),
                "voiceId", callback.getVoiceId(),
                "errorMsg", callback.getErrorMsg()
        ));

        // 唤醒等待
        wakeupLatch(taskId, "voice_clone");
    }

    /**
     * 声音合成TTS回调处理
     */
    public void notifyVoiceTtsCallback(VoiceTtsCallbackDTO callback) {
        String taskId = callback.getTaskId();
        log.info("处理声音合成回调: taskId={}, status={}, audioUrl={}",
                taskId, callback.getStatus(), callback.getAudioUrl());

        // 存储回调结果
        storeCallbackResult(taskId, "voice_tts", Map.of(
                "success", "success".equals(callback.getStatus()),
                "audioUrl", callback.getAudioUrl(),
                "errorMsg", callback.getErrorMsg()
        ));

        // 唤醒等待
        wakeupLatch(taskId, "voice_tts");
    }

    /**
     * 歌曲特征提取回调处理
     */
    public void notifySongFeatureExtractCallback(SongFeatureExtractCallbackDTO callback) {
        String taskId = callback.getTaskId();
        log.info("处理歌曲特征提取回调: taskId={}, status={}, featureSize={}",
                taskId, callback.getStatus(),
                callback.getFeature() != null ? callback.getFeature().size() : 0);

        // 存储回调结果
        storeCallbackResult(taskId, "song_feature_extract", Map.of(
                "success", "success".equals(callback.getStatus()),
                "feature", callback.getFeature(),
                "errorMsg", callback.getErrorMsg()
        ));

        // 唤醒等待
        wakeupLatch(taskId, "song_feature_extract");
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
     * 存储回调结果到Redis和内存
     */
    private void storeCallbackResult(String taskId, String stepName, Map<String, Object> result) {
        // 存储到Redis（持久化）
        String cacheKey = CALLBACK_CACHE_PREFIX + taskId + ":" + stepName;
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);

        // 存储到内存（用于CountDownLatch等待获取）
        callbackResults.put(taskId + ":" + stepName, result);

        log.debug("存储回调结果: taskId={}, stepName={}", taskId, stepName);
    }

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
     * 从响应中提取taskId
     * （由于不同算法服务可能有不同的字段名，这里统一处理）
     */
    private String extractTaskId(VideoAlgorithmCallbackResponse response) {
        // 如果response中有taskId字段
        // TODO: 根据实际调整，这里假设taskId通过某种方式传递
        return "unknown_task_id";
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
