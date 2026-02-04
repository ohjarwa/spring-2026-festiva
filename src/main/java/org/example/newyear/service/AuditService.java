package org.example.newyear.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.dto.AuditCallbackDTO;
import org.example.newyear.dto.AuditSubmitDTO;
import org.example.newyear.entity.Spring2026User;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.mapper.Spring2026UserMapper;
import org.example.newyear.util.IdGenerator;
import org.example.newyear.util.JsonUtil;
import org.example.newyear.vo.AuditStatusVO;
import org.example.newyear.vo.AuditSubmitVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 审核服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final UserService userService;
    private final Spring2026CreationRecordMapper recordMapper;
    private final Spring2026UserMapper userMapper;
    private final IdGenerator idGenerator;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUDIT_CACHE_PREFIX = "audit:";
    private static final int AUDIT_CACHE_TTL = 3600; // 1小时

    /**
     * 提交审核
     */
    public AuditSubmitVO submitAudit(AuditSubmitDTO dto) {
        // 1. 检查用户状态
        Spring2026User user = userService.getOrCreateUser(dto.getUserId());
        if (user.getCanUpload() == 0) {
            throw new BusinessException(BusinessCode.ERROR_USER_RESTRICTED, "账号已被限制，无法提交审核");
        }

        // 2. 生成审核ID
        String auditId = "audit_" + idGenerator.generateRecordId().replace("record_", "");

        // 3. 构建审核数据
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("auditId", auditId);
        auditData.put("userId", dto.getUserId());
        auditData.put("imageUrl", dto.getImageUrl());
        auditData.put("audioUrl", dto.getAudioUrl());
        auditData.put("status", "pending");
        auditData.put("createTime", System.currentTimeMillis());

        // 4. 存储到Redis（模拟审核系统）
        String cacheKey = AUDIT_CACHE_PREFIX + auditId;
        redisTemplate.opsForValue().set(cacheKey, auditData, AUDIT_CACHE_TTL, TimeUnit.SECONDS);

        // 5. 调用审核服务（TODO: 实际调用审核系统接口）
        // 这里先模拟异步审核，2秒后自动通过
        simulateAudit(auditId, dto.getImageUrl(), dto.getAudioUrl());

        log.info("提交审核成功: auditId={}, userId={}, imageUrl={}, audioUrl={}",
                auditId, dto.getUserId(), dto.getImageUrl(), dto.getAudioUrl());

        // 6. 返回结果
        AuditSubmitVO vo = new AuditSubmitVO();
        vo.setAuditId(auditId);
        vo.setStatus("pending");
        vo.setMessage("审核提交成功，请等待审核结果");
        vo.setEstimatedTime(5); // 预计5秒

        return vo;
    }

    /**
     * 查询审核状态
     */
    public AuditStatusVO getAuditStatus(String auditId, String userId) {
        // 1. 从Redis获取审核数据
        String cacheKey = AUDIT_CACHE_PREFIX + auditId;
        Map<String, Object> auditData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

        if (auditData == null) {
            throw new BusinessException(BusinessCode.ERROR_RECORD_NOT_FOUND, "审核记录不存在");
        }

        // 2. 验证用户ID
        if (!userId.equals(auditData.get("userId"))) {
            throw new BusinessException(BusinessCode.ERROR_RECORD_NOT_FOUND, "无权查看该审核记录");
        }

        // 3. 构建响应
        AuditStatusVO vo = new AuditStatusVO();
        vo.setAuditId(auditId);
        vo.setStatus((String) auditData.get("status"));
        vo.setStatusText(getStatusText((String) auditData.get("status")));

        // 图片审核结果
        if (auditData.containsKey("imageAudit")) {
            Map<String, Object> imageAudit = (Map<String, Object>) auditData.get("imageAudit");
            AuditStatusVO.ResourceAuditResult imageResult = new AuditStatusVO.ResourceAuditResult();
            imageResult.setStatus((String) imageAudit.get("status"));
            imageResult.setRejectReason((String) imageAudit.get("rejectReason"));
            vo.setImageAudit(imageResult);
        }

        // 音频审核结果
        if (auditData.containsKey("audioAudit")) {
            Map<String, Object> audioAudit = (Map<String, Object>) auditData.get("audioAudit");
            AuditStatusVO.ResourceAuditResult audioResult = new AuditStatusVO.ResourceAuditResult();
            audioResult.setStatus((String) audioAudit.get("status"));
            audioResult.setRejectReason((String) audioAudit.get("rejectReason"));
            vo.setAudioAudit(audioResult);
        }

        return vo;
    }

    /**
     * 审核回调接口
     */
    public void handleAuditCallback(AuditCallbackDTO callback) {
        log.info("收到审核回调: auditId={}, userId={}, status={}",
                callback.getCallbackId(), callback.getUserId(), callback.getAuditResult().getStatus());

        // 1. 验证签名（TODO: 添加签名验证）

        // 2. 更新审核状态
        String cacheKey = AUDIT_CACHE_PREFIX + callback.getCallbackId();
        Map<String, Object> auditData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

        if (auditData != null) {
            auditData.put("status", callback.getAuditResult().getStatus());

            if ("image".equals(callback.getAuditType())) {
                Map<String, Object> imageAudit = new HashMap<>();
                imageAudit.put("status", callback.getAuditResult().getStatus());
                imageAudit.put("rejectReason", callback.getAuditResult().getRejectReason());
                auditData.put("imageAudit", imageAudit);
            } else if ("audio".equals(callback.getAuditType())) {
                Map<String, Object> audioAudit = new HashMap<>();
                audioAudit.put("status", callback.getAuditResult().getStatus());
                audioAudit.put("rejectReason", callback.getAuditResult().getRejectReason());
                auditData.put("audioAudit", audioAudit);
            }

            // 更新Redis
            redisTemplate.opsForValue().set(cacheKey, auditData, AUDIT_CACHE_TTL, TimeUnit.SECONDS);

            log.info("审核回调处理成功: auditId={}, status={}", callback.getCallbackId(), callback.getAuditResult().getStatus());
        }
    }

    /**
     * 检查审核是否通过
     */
    public boolean isAuditPassed(String auditId) {
        String cacheKey = AUDIT_CACHE_PREFIX + auditId;
        Map<String, Object> auditData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

        if (auditData == null) {
            return false;
        }

        return "pass".equals(auditData.get("status"));
    }

    /**
     * 模拟审核（实际应该调用审核系统）
     */
    private void simulateAudit(String auditId, String imageUrl, String audioUrl) {
        // 模拟异步审核，2秒后自动通过
        new Thread(() -> {
            try {
                Thread.sleep(2000);

                String cacheKey = AUDIT_CACHE_PREFIX + auditId;
                Map<String, Object> auditData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

                if (auditData != null) {
                    auditData.put("status", "pass");

                    Map<String, Object> imageAudit = new HashMap<>();
                    imageAudit.put("status", "pass");
                    auditData.put("imageAudit", imageAudit);

                    Map<String, Object> audioAudit = new HashMap<>();
                    audioAudit.put("status", "pass");
                    auditData.put("audioAudit", audioAudit);

                    redisTemplate.opsForValue().set(cacheKey, auditData, AUDIT_CACHE_TTL, TimeUnit.SECONDS);

                    log.info("模拟审核完成: auditId={}", auditId);
                }
            } catch (InterruptedException e) {
                log.error("模拟审核失败", e);
            }
        }).start();
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        return switch (status) {
            case "pending" -> "审核中";
            case "pass" -> "通过";
            case "reject" -> "拒绝";
            case "review" -> "人工审核";
            default -> "未知状态";
        };
    }
}
