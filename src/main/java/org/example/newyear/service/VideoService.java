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
     * 创建视频任务（异步模式：仅落库，由定时任务拉起执行）
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

        // 3. 创建创作记录（status=0 待执行，等待定时任务拉起）
        String materialsJson = JsonUtil.toJson(dto.getMaterials());
        String recordId = creationRecordService.createRecord(userId, dto.getTemplateId(), materialsJson);

        log.info("视频任务已创建，等待定时任务调度: recordId={}, userId={}, templateId={}",
            recordId, userId, dto.getTemplateId());

        // 4. 返回结果（不立即执行，由定时任务拉起）
        VideoCreateVO vo = new VideoCreateVO();
        vo.setRecordId(recordId);
        vo.setStatus("queued");
        vo.setEstimatedTime(estimatedTime);
        vo.setTips("视频任务已提交，预计" + (estimatedTime / 60) + "分钟后完成");

        return vo;
    }
}