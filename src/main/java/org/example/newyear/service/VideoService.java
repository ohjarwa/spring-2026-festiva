package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.dto.VideoCreateDTO.MaterialsDTO;
import org.example.newyear.dto.VideoRegenerateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.util.JsonUtil;
import org.example.newyear.vo.VideoCreateVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 视频服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final UserService userService;
    private final CreationRecordService creationRecordService;
    private final TemplateService templateService;
    private final VideoProcessingService videoProcessingService;
    private final Spring2026CreationRecordMapper recordMapper;

    /**
     * 创建视频任务
     */
    @Transactional
    public VideoCreateVO createVideo(String userId, VideoCreateDTO dto) {
        // 1. 检查并扣减配额
        boolean deducted = userService.checkAndDeductQuota(userId);
        if (!deducted) {
            throw new BusinessException(BusinessCode.ERROR_QUOTA_NOT_ENOUGH);
        }

        // 2. 检查模板是否存在
        Map<String, Object> taskConfig = templateService.getTaskConfig(dto.getTemplateId());
        Integer estimatedTime = (Integer) taskConfig.get("estimated_time");

        // TODO: 检查素材审核状态（需要先提交审核）
        // String auditId = getAuditId(userId, dto.getMaterials());
        // if (!auditService.isAuditPassed(auditId)) {
        //     throw new BusinessException(BusinessCode.ERROR_AUDIT_PENDING, "素材审核未通过，请等待审核完成");
        // }

        // 3. 创建创作记录
        String materialsJson = JsonUtil.toJson(dto.getMaterials());
        String recordId = creationRecordService.createRecord(userId, dto.getTemplateId(), materialsJson);

        // 4. 异步处理视频生成
        try {
            videoProcessingService.processVideoCreation(recordId, userId, dto);
        } catch (Exception e) {
            log.error("启动视频处理失败: recordId={}", recordId, e);
        }

        // 5. 返回结果
        VideoCreateVO vo = new VideoCreateVO();
        vo.setRecordId(recordId);
        vo.setStatus("queued");
        vo.setEstimatedTime(estimatedTime);
        vo.setTips("视频生成需要约" + (estimatedTime / 60) + "分钟，完成后我们会通知您");

        return vo;
    }

    /**
     * 重新生成视频
     */
    @Transactional
    public VideoCreateVO regenerateVideo(String userId, VideoRegenerateDTO dto) {
        // 1. 查询原记录
        Spring2026CreationRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(BusinessCode.ERROR_RECORD_NOT_FOUND);
        }

        // 2. 检查重试次数
        if (record.getRetryCount() >= record.getMaxRetry()) {
            throw new BusinessException(BusinessCode.ERROR_RETRY_LIMIT_EXCEEDED);
        }

        // 3. 检查配额
        boolean deducted = userService.checkAndDeductQuota(userId);
        if (!deducted) {
            throw new BusinessException(BusinessCode.ERROR_QUOTA_NOT_ENOUGH);
        }

        // 4. 更新重试次数和状态
        record.setRetryCount(record.getRetryCount() + 1);
        record.setStatus(0); // 排队
        record.setProgress(0);
        recordMapper.updateById(record);

        // 5. 重新执行视频处理
        try {
            // 从userMaterials解析MaterialsDTO
            MaterialsDTO materials = JsonUtil.fromJson(record.getUserMaterials(), MaterialsDTO.class);

            // 构建VideoCreateDTO
            VideoCreateDTO createDto = new VideoCreateDTO();
            createDto.setTemplateId(record.getTemplateId());
            createDto.setMaterials(materials);

            videoProcessingService.processVideoCreation(record.getRecordId(), userId, createDto);
        } catch (Exception e) {
            log.error("重新生成视频失败: recordId={}", record.getRecordId(), e);
        }

        // 6. 返回结果
        Map<String, Object> taskConfig = templateService.getTaskConfig(record.getTemplateId());
        Integer estimatedTime = (Integer) taskConfig.get("estimated_time");

        VideoCreateVO vo = new VideoCreateVO();
        vo.setRecordId(record.getRecordId());
        vo.setStatus("queued");
        vo.setEstimatedTime(estimatedTime);
        vo.setTips("视频生成需要约" + (estimatedTime / 60) + "分钟，完成后我们会通知您");

        return vo;
    }
}