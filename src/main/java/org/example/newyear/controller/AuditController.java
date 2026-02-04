package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.dto.AuditCallbackDTO;
import org.example.newyear.dto.AuditSubmitDTO;
import org.example.newyear.service.AuditService;
import org.example.newyear.vo.AuditStatusVO;
import org.example.newyear.vo.AuditSubmitVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 审核控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * 提交审核
     */
    @PostMapping("/submit")
    public Result<AuditSubmitVO> submitAudit(@Validated @RequestBody AuditSubmitDTO dto) {
        log.info("提交审核: userId={}, imageUrl={}, audioUrl={}",
                dto.getUserId(), dto.getImageUrl(), dto.getAudioUrl());

        AuditSubmitVO result = auditService.submitAudit(dto);
        return Result.success(result);
    }

    /**
     * 查询审核状态
     */
    @GetMapping("/status")
    public Result<AuditStatusVO> getAuditStatus(
            @RequestParam("auditId") String auditId,
            @RequestParam("userId") String userId) {

        log.info("查询审核状态: auditId={}, userId={}", auditId, userId);

        AuditStatusVO result = auditService.getAuditStatus(auditId, userId);
        return Result.success(result);
    }

    /**
     * 审核回调接口（审核系统调用）
     */
    @PostMapping("/callback")
    public Result<Void> handleAuditCallback(@RequestBody AuditCallbackDTO callback) {
        log.info("收到审核回调: callbackId={}, userId={}, status={}",
                callback.getCallbackId(), callback.getUserId(), callback.getAuditResult().getStatus());

        auditService.handleAuditCallback(callback);

        return Result.success();
    }
}