package org.example.newyear.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.AlgorithmCallbackRequest;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.service.algorithm.*;
import org.example.newyear.util.JsonUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频处理服务（核心流程编排）
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    private final FaceSwapService faceSwapService;
    private final LipSyncService lipSyncService;
    private final VoiceCloneService voiceCloneService;
    private final VoiceTtsService voiceTtsService;
    private final TemplateService templateService;
    private final Spring2026CreationRecordMapper recordMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TASK_CACHE_PREFIX = "task:";
    private static final String CALLBACK_CACHE_PREFIX = "callback:";
    private static final int CACHE_TTL = 3600; // 1小时

    /**
     * 异步处理视频生成任务
     *
     * @param recordId 记录ID
     * @param userId   用户ID
     * @param dto      创建视频请求
     */
    @Async("videoTaskExecutor")
    public void processVideoCreation(String recordId, String userId, VideoCreateDTO dto) {
        log.info("开始处理视频生成任务: recordId={}, userId={}", recordId, userId);

        try {
            // 1. 更新状态：生成中
            updateRecordStatus(recordId, 1, 0);

            // 2. 获取模板配置
            Map<String, Object> taskConfig = templateService.getTaskConfig(dto.getTemplateId());
            List<Map<String, Object>> steps = (List<Map<String, Object>>) taskConfig.get("steps");

            if (steps == null || steps.isEmpty()) {
                throw new BusinessException(40007, "模板配置错误");
            }

            // 3. 准备上下文数据
            Map<String, Object> context = new HashMap<>();
            context.put("templateId", dto.getTemplateId());
            context.put("user_photo_url", dto.getMaterials().getPhotos().get(0));
            context.put("user_audio_url", dto.getMaterials().getAudios().get(0));
            // 从模板表获取template_video_url
            Spring2026Template template = templateService.getTemplateById(dto.getTemplateId());
            context.put("template_video_url", template.getTemplateUrl());

            // 4. 执行流程
            Map<String, Object> executionResult = executeSteps(recordId, steps, context);

            // 5. 更新最终结果
            if (executionResult.containsKey("final_video_url")) {
                String finalVideoUrl = (String) executionResult.get("final_video_url");
                updateRecordComplete(recordId, finalVideoUrl);
                log.info("视频生成完成: recordId={}, url={}", recordId, finalVideoUrl);
            } else {
                throw new BusinessException(40006, "视频生成失败：未生成最终结果");
            }

        } catch (Exception e) {
            log.error("视频生成失败: recordId={}", recordId, e);
            updateRecordError(recordId, e.getMessage());
        }
    }

    /**
     * 执行步骤流程
     */
    private Map<String, Object> executeSteps(String recordId, List<Map<String, Object>> steps, Map<String, Object> context) {
        Map<String, Object> executionResult = new HashMap<>();
        Map<String, CompletableFuture<Map<String, Object>>> futures = new ConcurrentHashMap<>();

        for (Map<String, Object> stepConfig : steps) {
            String stepName = (String) stepConfig.get("step_name");
            String stepType = (String) stepConfig.get("step_type");

            // 检查依赖
            List<String> dependsOn = (List<String>) stepConfig.get("depends_on");
            if (dependsOn != null && !dependsOn.isEmpty()) {
                // 等待依赖步骤完成
                for (String dep : dependsOn) {
                    CompletableFuture<Map<String, Object>> depFuture = futures.get(dep);
                    if (depFuture != null) {
                        try {
                            Map<String, Object> depResult = depFuture.get(5, TimeUnit.MINUTES);
                            executionResult.putAll(depResult);
                        } catch (Exception e) {
                            log.error("等待依赖步骤失败: dep={}", dep, e);
                            throw new RuntimeException("依赖步骤执行失败: " + dep, e);
                        }
                    }
                }
            }

            // 执行当前步骤
            if ("video_process".equals(stepType)) {
                futures.put(stepName, executeVideoStep(recordId, stepConfig, context, executionResult));
            } else if ("audio_process".equals(stepType)) {
                futures.put(stepName, executeAudioStep(recordId, stepConfig, context, executionResult));
            }

            // 更新进度
            updateProgress(recordId, steps.size(), executionResult.size());
        }

        // 等待所有步骤完成
        for (Map.Entry<String, CompletableFuture<Map<String, Object>>> entry : futures.entrySet()) {
            try {
                Map<String, Object> result = entry.getValue().get(5, TimeUnit.MINUTES);
                executionResult.putAll(result);
            } catch (Exception e) {
                log.error("步骤执行失败: step={}", entry.getKey(), e);
                throw new RuntimeException("步骤执行失败: " + entry.getKey(), e);
            }
        }

        return executionResult;
    }

    /**
     * 执行视频处理步骤（异步）
     */
    private CompletableFuture<Map<String, Object>> executeVideoStep(
            String recordId, Map<String, Object> stepConfig, Map<String, Object> context, Map<String, Object> executionResult) {

        return CompletableFuture.supplyAsync(() -> {
            String stepName = (String) stepConfig.get("step_name");
            String serviceName = (String) stepConfig.get("service");
            String methodName = (String) stepConfig.get("method");
            Map<String, String> inputMapping = (Map<String, String>) stepConfig.get("input_mapping");

            log.info("执行视频步骤: recordId={}, step={}, service={}, method={}",
                    recordId, stepName, serviceName, methodName);

            try {
                // 解析输入参数
                Map<String, String> params = resolveInputMapping(inputMapping, context, executionResult);

                // 调用服务
                AlgorithmResponse response;
                if ("faceSwapService".equals(serviceName)) {
                    FaceSwapRequest request = new FaceSwapRequest();
                    request.setVideoUrl(params.get("videoUrl"));
                    request.setFaceImageUrl(params.get("faceImageUrl"));
                    request.setCallbackUrl(buildCallbackUrl(recordId, stepName, "video"));

                    response = faceSwapService.swapFace(request);

                } else if ("lipSyncService".equals(serviceName)) {
                    LipSyncRequest request = new LipSyncRequest();
                    request.setVideoUrl(params.get("videoUrl"));
                    request.setAudioUrl(params.get("audioUrl"));
                    request.setCallbackUrl(buildCallbackUrl(recordId, stepName, "video"));

                    response = lipSyncService.syncLip(request);

                } else {
                    throw new IllegalArgumentException("未知服务: " + serviceName);
                }

                // 处理响应
                if (response.getCode() == 0 && response.getData() != null) {
                    String taskId = response.getData().getTaskId();

                    // 如果是异步任务，等待回调
                    if (taskId != null && !taskId.isEmpty()) {
                        Map<String, Object> result = waitForAsyncCallback(recordId, stepName, 300);
                        return Collections.singletonMap((String) stepConfig.get("output_key"), result.get("resultUrl"));
                    } else {
                        // 同步返回
                        return Collections.singletonMap((String) stepConfig.get("output_key"), response.getData().getResultUrl());
                    }
                } else {
                    throw new RuntimeException("服务调用失败: " + response.getMessage());
                }

            } catch (Exception e) {
                log.error("视频步骤执行失败: recordId={}, step={}", recordId, stepName, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 执行音频处理步骤（异步）
     */
    private CompletableFuture<Map<String, Object>> executeAudioStep(
            String recordId, Map<String, Object> stepConfig, Map<String, Object> context, Map<String, Object> executionResult) {

        return CompletableFuture.supplyAsync(() -> {
            String stepName = (String) stepConfig.get("step_name");
            String serviceName = (String) stepConfig.get("service");
            String methodName = (String) stepConfig.get("method");
            Map<String, String> inputMapping = (Map<String, String>) stepConfig.get("input_mapping");

            log.info("执行音频步骤: recordId={}, step={}, service={}, method={}",
                    recordId, stepName, serviceName, methodName);

            try {
                // 解析输入参数
                Map<String, String> params = resolveInputMapping(inputMapping, context, executionResult);

                // 调用服务
                AlgorithmResponse response;
                if ("voiceCloneService".equals(serviceName)) {
                    VoiceCloneRequest request = new VoiceCloneRequest();
                    request.setAudioUrl(params.get("audioUrl"));
                    request.setCallbackUrl(buildCallbackUrl(recordId, stepName, "audio"));

                    response = voiceCloneService.cloneVoice(request);

                } else if ("voiceTtsService".equals(serviceName)) {
                    VoiceTtsRequest request = new VoiceTtsRequest();
                    request.setVoiceId(params.get("voiceId"));
                    request.setText(params.get("text"));
                    request.setCallbackUrl(buildCallbackUrl(recordId, stepName, "audio"));

                    response = voiceTtsService.synthesizeVoice(request);

                } else {
                    throw new IllegalArgumentException("未知服务: " + serviceName);
                }

                // 处理响应
                if (response.getCode() == 0 && response.getData() != null) {
                    String taskId = response.getData().getTaskId();

                    // 如果是异步任务，等待回调
                    if (taskId != null && !taskId.isEmpty()) {
                        Map<String, Object> result = waitForAsyncCallback(recordId, stepName, 300);
                        return Collections.singletonMap((String) stepConfig.get("output_key"), result.get("resultUrl"));
                    } else {
                        // 同步返回
                        String outputKey = (String) stepConfig.get("output_key");
                        Object value = "voiceId".equals(outputKey) ? response.getData().getTaskId() : response.getData().getResultUrl();
                        return Collections.singletonMap(outputKey, value);
                    }
                } else {
                    throw new RuntimeException("服务调用失败: " + response.getMessage());
                }

            } catch (Exception e) {
                log.error("音频步骤执行失败: recordId={}, step={}", recordId, stepName, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 等待异步回调
     */
    private Map<String, Object> waitForAsyncCallback(String recordId, String stepName, int timeoutSeconds) {
        String cacheKey = CALLBACK_CACHE_PREFIX + recordId + ":" + stepName;

        // 轮询等待回调
        for (int i = 0; i < timeoutSeconds; i++) {
            Map<String, Object> callbackData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            if (callbackData != null) {
                log.info("收到异步回调: recordId={}, step={}", recordId, stepName);
                return callbackData;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待回调被中断", e);
            }
        }

        throw new RuntimeException("等待回调超时: recordId=" + recordId + ", step=" + stepName);
    }

    /**
     * 解析输入参数映射
     */
    private Map<String, String> resolveInputMapping(Map<String, String> inputMapping, Map<String, Object> context, Map<String, Object> executionResult) {
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, String> entry : inputMapping.entrySet()) {
            String value = entry.getValue();

            // 替换上下文变量
            if (value.startsWith("{{") && value.endsWith("}}")) {
                String key = value.substring(2, value.length() - 2);

                // 优先从executionResult获取（前面步骤的结果）
                Object result = executionResult.get(key);
                if (result != null) {
                    params.put(entry.getKey(), result.toString());
                    continue;
                }

                // 从context获取
                Object contextValue = context.get(key);
                if (contextValue != null) {
                    params.put(entry.getKey(), contextValue.toString());
                }
            } else {
                params.put(entry.getKey(), value);
            }
        }

        return params;
    }

    /**
     * 构建回调URL
     */
    private String buildCallbackUrl(String recordId, String stepName, String type) {
        // TODO: 从配置文件读取服务器地址
        return "http://your-domain.com/api/callback/" + type + "?recordId=" + recordId + "&step=" + stepName;
    }

    /**
     * 处理视频回调
     */
    public void handleVideoCallback(AlgorithmCallbackRequest request) {
        String recordId = request.getTaskId(); // 这里的taskId实际是recordId
        String stepName = request.getStepName();

        log.info("处理视频回调: recordId={}, step={}, status={}", recordId, stepName, request.getStatus());

        // 存储回调结果到Redis
        String cacheKey = CALLBACK_CACHE_PREFIX + recordId + ":" + stepName;
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("status", request.getStatus());
        callbackData.put("resultUrl", request.getResultUrl());
        callbackData.put("errorMessage", request.getErrorMessage());

        redisTemplate.opsForValue().set(cacheKey, callbackData, CACHE_TTL, TimeUnit.SECONDS);

        // 更新任务执行详情
        updateTaskExecution(recordId, stepName, "success".equals(request.getStatus()) ? "completed" : "failed", request.getResultUrl());
    }

    /**
     * 处理音频回调
     */
    public void handleAudioCallback(AlgorithmCallbackRequest request) {
        String recordId = request.getTaskId();
        String stepName = request.getStepName();

        log.info("处理音频回调: recordId={}, step={}, status={}", recordId, stepName, request.getStatus());

        // 存储回调结果到Redis
        String cacheKey = CALLBACK_CACHE_PREFIX + recordId + ":" + stepName;
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("status", request.getStatus());
        callbackData.put("resultUrl", request.getResultUrl());
        callbackData.put("errorMessage", request.getErrorMessage());

        redisTemplate.opsForValue().set(cacheKey, callbackData, CACHE_TTL, TimeUnit.SECONDS);

        // 更新任务执行详情
        updateTaskExecution(recordId, stepName, "success".equals(request.getStatus()) ? "completed" : "failed", request.getResultUrl());
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
     * 更新进度
     */
    private void updateProgress(String recordId, int totalSteps, int completedSteps) {
        int progress = (int) ((completedSteps / (double) totalSteps) * 100);
        updateRecordStatus(recordId, 1, progress);
        log.info("更新进度: recordId={}, progress={}%", recordId, progress);
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
        // 从数据库获取记录
        Spring2026CreationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            return;
        }

        // 解析现有执行详情
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

        // 更新步骤信息
        Map<String, Object> steps = (Map<String, Object>) execution.get("steps");
        Map<String, Object> stepInfo = new HashMap<>();
        stepInfo.put("status", status);
        stepInfo.put("result_url", resultUrl);
        stepInfo.put("end_time", System.currentTimeMillis());
        steps.put(stepName, stepInfo);

        // 保存回数据库
        record.setTaskExecution(JsonUtil.toJson(execution));
        recordMapper.updateById(record);
    }
}