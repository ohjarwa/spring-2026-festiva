package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.service.CreationRecordService;
import org.example.newyear.vo.WorkListVO;
import org.springframework.web.bind.annotation.*;

/**
 * 作品控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/user/works")
@RequiredArgsConstructor
public class WorkController {

    private final CreationRecordService creationRecordService;

    /**
     * 获取用户作品列表
     */
    @GetMapping
    public Result<WorkListVO> getUserWorks(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", defaultValue = "all") String status) {

        // 限制分页大小
        if (pageSize > 50) {
            pageSize = 50;
        }

        log.info("获取用户作品列表: userId={}, page={}, pageSize={}, status={}", userId, page, pageSize, status);

        WorkListVO result = creationRecordService.getUserWorks(userId, page, pageSize, status);
        return Result.success(result);
    }
}