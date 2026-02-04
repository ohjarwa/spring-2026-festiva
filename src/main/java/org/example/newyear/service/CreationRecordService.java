package org.example.newyear.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.common.Constants;
import org.example.newyear.common.RecordStatus;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.mapper.Spring2026TemplateMapper;
import org.example.newyear.util.IdGenerator;
import org.example.newyear.util.JsonUtil;
import org.example.newyear.vo.WorkVO;
import org.example.newyear.vo.WorkListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 创作记录服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreationRecordService {

    private final Spring2026CreationRecordMapper recordMapper;
    private final Spring2026TemplateMapper templateMapper;
    private final IdGenerator idGenerator;

    /**
     * 创建创作记录
     */
    @Transactional
    public String createRecord(String userId, String templateId, String materialsJson) {
        // 检查模板是否存在
        Spring2026Template template = templateMapper.selectOne(
                new LambdaQueryWrapper<Spring2026Template>()
                        .eq(Spring2026Template::getTemplateId, templateId)
                        .eq(Spring2026Template::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
        );

        if (template == null) {
            throw new BusinessException(BusinessCode.ERROR_TEMPLATE_NOT_FOUND);
        }

        // 创建记录
        Spring2026CreationRecord record = new Spring2026CreationRecord();
        record.setRecordId(idGenerator.generateRecordId());
        record.setUserId(userId);
        record.setActivityType(Constants.ACTIVITY_TYPE_SPRING_2026);
        record.setTemplateId(templateId);
        record.setUserMaterials(materialsJson);
        record.setStatus(RecordStatus.QUEUED.getCode());
        record.setProgress(0);
        record.setRetryCount(0);
        record.setMaxRetry(Constants.DEFAULT_MAX_RETRY);

        recordMapper.insert(record);
        log.info("创建创作记录成功: recordId={}, userId={}, templateId={}", record.getRecordId(), userId, templateId);

        return record.getRecordId();
    }

    /**
     * 获取用户作品列表
     */
    public WorkListVO getUserWorks(String userId, Integer page, Integer pageSize, String status) {
        // 构建查询条件
        LambdaQueryWrapper<Spring2026CreationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Spring2026CreationRecord::getUserId, userId);
        wrapper.eq(Spring2026CreationRecord::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026);

        // 状态过滤
        if (status != null && !"all".equalsIgnoreCase(status)) {
            Integer statusCode = getStatusCode(status);
            if (statusCode != null) {
                wrapper.eq(Spring2026CreationRecord::getStatus, statusCode);
            }
        }

        // 分页查询
        IPage<Spring2026CreationRecord> pageParam = new Page<>(page, pageSize);
        wrapper.orderByDesc(Spring2026CreationRecord::getCreateTime);

        IPage<Spring2026CreationRecord> recordPage = recordMapper.selectPage(pageParam, wrapper);

        // 统计生成中的数量
        Long processingCount = recordMapper.selectCount(
                new LambdaQueryWrapper<Spring2026CreationRecord>()
                        .eq(Spring2026CreationRecord::getUserId, userId)
                        .eq(Spring2026CreationRecord::getStatus, RecordStatus.PROCESSING.getCode())
        );

        // 转换为VO
        List<WorkVO> items = recordPage.getRecords().stream()
                .map(this::convertToWorkVO)
                .collect(Collectors.toList());

        // 组装结果
        WorkListVO vo = new WorkListVO();
        vo.setTotal(recordPage.getTotal());
        vo.setPage(page);
        vo.setPageSize(pageSize);
        vo.setProcessingCount(processingCount.intValue());
        vo.setItems(items);

        return vo;
    }

    /**
     * 获取作品详情
     */
    public Spring2026CreationRecord getRecordDetail(String recordId, String userId) {
        Spring2026CreationRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<Spring2026CreationRecord>()
                        .eq(Spring2026CreationRecord::getRecordId, recordId)
                        .eq(Spring2026CreationRecord::getUserId, userId)
        );

        if (record == null) {
            throw new BusinessException(BusinessCode.ERROR_RECORD_NOT_FOUND);
        }

        return record;
    }

    /**
     * 转换为WorkVO
     */
    private WorkVO convertToWorkVO(Spring2026CreationRecord record) {
        WorkVO vo = new WorkVO();
        vo.setRecordId(record.getRecordId());
        vo.setTemplateId(record.getTemplateId());

        // 查询模板信息
        Spring2026Template template = templateMapper.selectOne(
                new LambdaQueryWrapper<Spring2026Template>()
                        .eq(Spring2026Template::getTemplateId, record.getTemplateId())
        );

        if (template != null) {
            vo.setTemplateName(template.getName());
            vo.setTemplateCover(template.getCoverUrl());
        }

        // 状态
        vo.setStatus(RecordStatus.getEnglishByCode(record.getStatus()));
        vo.setStatusText(RecordStatus.getChineseByCode(record.getStatus()));

        // 进度
        vo.setProgress(record.getProgress());

        // 当前步骤
        if (record.getTaskExecution() != null && !record.getTaskExecution().isEmpty()) {
            try {
                Map<String, Object> execution = JsonUtil.fromJson(record.getTaskExecution(), new TypeReference<Map<String, Object>>() {});
                vo.setCurrentStep((String) execution.get("current_step"));
            } catch (Exception e) {
                log.warn("解析task_execution失败: recordId={}", record.getRecordId(), e);
            }
        }

        // 结果
        vo.setResultUrl(record.getResultUrl());
        vo.setResultThumbnailUrl(record.getResultThumbnailUrl());

        // 时间
        vo.setCreateTime(record.getCreateTime());
        vo.setCompleteTime(record.getCompleteTime());

        return vo;
    }

    /**
     * 获取状态码
     */
    private Integer getStatusCode(String status) {
        if ("queued".equalsIgnoreCase(status)) {
            return RecordStatus.QUEUED.getCode();
        } else if ("processing".equalsIgnoreCase(status)) {
            return RecordStatus.PROCESSING.getCode();
        } else if ("completed".equalsIgnoreCase(status)) {
            return RecordStatus.COMPLETED.getCode();
        } else if ("failed".equalsIgnoreCase(status)) {
            return RecordStatus.FAILED.getCode();
        }
        return null;
    }
}
