package org.example.newyear.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.service.VideoProcessingService;
import org.example.newyear.util.JsonUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 视频任务调度器
 * 定时拉起待执行的视频生成任务
 *
 * @author Claude
 * @since 2026-02-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTaskScheduler {

    private final Spring2026CreationRecordMapper recordMapper;
    private final VideoProcessingService videoProcessingService;

    /**
     * 定时拉起任务：每30秒执行一次
     * 查询最旧的1个待执行任务（status=0）并执行
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void scheduleVideoTasks() {
        try {
            // 1. 查询最旧的1个待执行任务（status=0）
            Spring2026CreationRecord record = recordMapper.selectOne(
                new QueryWrapper<Spring2026CreationRecord>()
                    .eq("status", 0)  // 待执行
                    .orderByAsc("create_time")  // 按创建时间升序，取最旧的
                    .last("LIMIT 1")
            );

            if (record == null) {
                log.debug("定时任务：暂无待执行的视频任务");
                return;
            }

            log.info("定时任务拉起视频任务: recordId={}, userId={}, templateId={}, createTime={}",
                record.getRecordId(), record.getUserId(), record.getTemplateId(), record.getCreateTime());

            // 2. 从数据库记录中解析出所需参数
            String recordId = record.getRecordId();
            String userId = record.getUserId();
            String templateId = record.getTemplateId();
            String userMaterialsJson = record.getUserMaterials();

            // 3. 构建VideoCreateDTO
            VideoCreateDTO dto = new VideoCreateDTO();
            dto.setTemplateId(templateId);

            // 解析用户素材
            VideoCreateDTO.MaterialsDTO materials = JsonUtil.fromJson(
                userMaterialsJson,
                VideoCreateDTO.MaterialsDTO.class

            );
            dto.setMaterials(materials);

            // 4. 调用视频处理服务（异步执行）
            videoProcessingService.processVideoCreation(recordId, userId, dto);

            log.info("定时任务成功提交视频处理: recordId={}", recordId);

        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }
}
