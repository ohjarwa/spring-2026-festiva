package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.dto.VideoRegenerateDTO;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.service.CreationRecordService;
import org.example.newyear.service.VideoService;
import org.example.newyear.vo.VideoCreateVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 视频控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final CreationRecordService creationRecordService;

    /**
     * 创建视频任务
     */
    @PostMapping("/create")
    public Result<VideoCreateVO> createVideo(
            @RequestParam("userId") String userId,
            @Validated @RequestBody VideoCreateDTO dto) {
        log.info("创建视频任务: userId={}, templateId={}", userId, dto.getTemplateId());
        VideoCreateVO result = videoService.createVideo(userId, dto);
        return Result.success(result);
    }

    /**
     * 重新生成视频
     */
    @PostMapping("/regenerate")
    public Result<VideoCreateVO> regenerateVideo(
            @RequestParam("userId") String userId,
            @Validated @RequestBody VideoRegenerateDTO dto) {
        log.info("重新生成视频: userId={}, recordId={}", userId, dto.getRecordId());
        VideoCreateVO result = videoService.regenerateVideo(userId, dto);
        return Result.success(result);
    }

    /**
     * 获取作品详情
     */
    @GetMapping("/detail")
    public Result<Spring2026CreationRecord> getDetail(
            @RequestParam("userId") String userId,
            @RequestParam("recordId") String recordId) {
        log.info("获取作品详情: userId={}, recordId={}", userId, recordId);
        Spring2026CreationRecord record = creationRecordService.getRecordDetail(recordId, userId);
        return Result.success(record);
    }
}