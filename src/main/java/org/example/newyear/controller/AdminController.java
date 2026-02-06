package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.annotation.RequireAdmin;
import org.example.newyear.common.AdminLevel;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.common.Result;
import org.example.newyear.entity.Spring2026CreationRecord;
import org.example.newyear.entity.Spring2026User;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026CreationRecordMapper;
import org.example.newyear.mapper.Spring2026UserMapper;
import org.example.newyear.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员控制器
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final Spring2026CreationRecordMapper recordMapper;
    private final Spring2026UserMapper userMapper;

    /**
     * 下线作品（需要管理员权限）
     *
     * @param request  HTTP请求
     * @param recordId 记录ID
     * @param reason   下线原因
     * @return 操作结果
     */
    @RequireAdmin
    @PostMapping("/works/take-down")
    public Result<Void> takeDownWork(HttpServletRequest request,
                                     @RequestParam String recordId,
                                     @RequestParam String reason) {
        String adminId = request.getHeader("X-User-UUID");
        log.info("管理员下线作品: adminId={}, recordId={}, reason={}", adminId, recordId, reason);

        // 查询作品
        Spring2026CreationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(BusinessCode.ERROR_RECORD_NOT_FOUND);
        }

        // 检查作品状态（只能下线已完成的作品）
        if (!record.getStatus().equals(2)) {
            throw new BusinessException(BusinessCode.ERROR_CANNOT_TAKE_DOWN,
                    "只能下线已完成的作品");
        }

        // 获取管理员信息
        Spring2026User admin = userMapper.selectById(adminId);
        String adminName = admin != null ? admin.getUserId() : adminId;

        // 构建下线信息
        String takeDownInfo = String.format(
                "{\"admin\":\"%s\",\"reason\":\"%s\",\"take_down_time\":%d}",
                adminName, reason, System.currentTimeMillis()
        );

        // 更新作品状态为已下线
        record.setStatus(4); // 4 = 已下线
        record.setAuditInfo(takeDownInfo);
        recordMapper.updateById(record);

        log.info("作品已下线: recordId={}", recordId);
        return Result.success();
    }

    /**
     * 设置用户管理员级别（需要超级管理员权限）
     *
     * @param request   HTTP请求
     * @param targetId  目标用户ID
     * @param adminLevel 管理员级别
     * @return 操作结果
     */
    @RequireAdmin(AdminLevel.SUPER_ADMIN)
    @PostMapping("/users/set-admin")
    public Result<Void> setUserAdminLevel(HttpServletRequest request,
                                          @RequestParam String targetId,
                                          @RequestParam Integer adminLevel) {
        String superAdminId = request.getHeader("X-User-UUID");
        log.info("超级管理员设置权限: superAdminId={}, targetId={}, adminLevel={}",
                superAdminId, targetId, adminLevel);

        AdminLevel level = AdminLevel.fromCode(adminLevel);
        userService.setUserAdminLevel(superAdminId, targetId, level);

        return Result.success();
    }

    /**
     * 封禁用户（需要管理员权限）
     *
     * @param request    HTTP请求
     * @param targetId   目标用户ID
     * @param banReason  封禁原因
     * @param banDays    封禁天数（0表示永久封禁）
     * @return 操作结果
     */
    @RequireAdmin
    @PostMapping("/users/ban")
    public Result<Void> banUser(HttpServletRequest request,
                                @RequestParam String targetId,
                                @RequestParam String banReason,
                                @RequestParam(defaultValue = "0") Integer banDays) {
        String adminId = request.getHeader("X-User-UUID");
        log.info("管理员封禁用户: adminId={}, targetId={}, banReason={}, banDays={}",
                adminId, targetId, banReason, banDays);

        userService.banUser(adminId, targetId, banReason, banDays);

        return Result.success();
    }

    /**
     * 解封用户（需要管理员权限）
     *
     * @param request  HTTP请求
     * @param targetId 目标用户ID
     * @return 操作结果
     */
    @RequireAdmin
    @PostMapping("/users/unban")
    public Result<Void> unbanUser(HttpServletRequest request,
                                  @RequestParam String targetId) {
        String adminId = request.getHeader("X-User-UUID");
        log.info("管理员解封用户: adminId={}, targetId={}", adminId, targetId);

        userService.unbanUser(adminId, targetId);

        return Result.success();
    }
}
