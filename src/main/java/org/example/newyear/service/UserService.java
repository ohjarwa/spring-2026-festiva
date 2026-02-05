package org.example.newyear.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.AdminLevel;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.common.Constants;
import org.example.newyear.entity.Spring2026User;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026UserMapper;
import org.example.newyear.vo.UserQuotaVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final Spring2026UserMapper userMapper;

    /**
     * 获取或创建用户
     */
    public Spring2026User getOrCreateUser(String userId) {
        Spring2026User user = userMapper.selectOne(
                new LambdaQueryWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, userId)
                        .eq(Spring2026User::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
        );

        if (user == null) {
            // 创建新用户
            user = new Spring2026User();
            user.setUserId(userId);
            user.setActivityType(Constants.ACTIVITY_TYPE_SPRING_2026);
            user.setSource("in_app");
            user.setTotalQuota(Constants.DEFAULT_DAILY_QUOTA);
            user.setUsedQuota(0);
            user.setRemainingQuota(Constants.DEFAULT_DAILY_QUOTA);
            user.setCanRetry(1);
            user.setAccountStatus(1);
            user.setCanUpload(1);
            user.setCanCreateVideo(1);
            user.setViolationCount(0);
            user.setSuccessCount(0);
            user.setFailedCount(0);

            userMapper.insert(user);
            log.info("创建新用户: userId={}", userId);
        }

        return user;
    }

    /**
     * 获取用户配额
     */
    public UserQuotaVO getUserQuota(String userId) {
        Spring2026User user = getOrCreateUser(userId);

        // 检查用户状态
        if (user.getAccountStatus() == 0) {
            throw new BusinessException(BusinessCode.ERROR_USER_BANNED);
        }

        UserQuotaVO vo = new UserQuotaVO();
        vo.setTotalQuota(user.getTotalQuota());
        vo.setUsedQuota(user.getUsedQuota());
        vo.setRemainingQuota(user.getRemainingQuota());
        vo.setCanUpload(user.getCanUpload() == 1);
        vo.setCanCreateVideo(user.getCanCreateVideo() == 1);

        // 计算配额重置时间（明天凌晨）
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        vo.setResetTime(tomorrow.toString());

        return vo;
    }

    /**
     * 检查并扣减配额
     */
    public boolean checkAndDeductQuota(String userId) {
        Spring2026User user = getOrCreateUser(userId);

        // 检查配额
        if (user.getRemainingQuota() <= 0) {
            throw new BusinessException(BusinessCode.ERROR_QUOTA_NOT_ENOUGH);
        }

        // 检查能否创建视频
        if (user.getCanCreateVideo() == 0) {
            throw new BusinessException(BusinessCode.ERROR_USER_RESTRICTED, "账号已被限制，无法创建视频");
        }

        // 扣减配额
        int updated = userMapper.update(null,
                new LambdaUpdateWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, userId)
                        .eq(Spring2026User::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
                        .gt(Spring2026User::getRemainingQuota, 0)
                        .setSql("used_quota = used_quota + 1")
                        .setSql("remaining_quota = remaining_quota - 1")
                        .setSql("last_use_time = NOW()")
        );

        return updated > 0;
    }

    /**
     * 退还配额（失败时）
     */
    public void refundQuota(String userId) {
        userMapper.update(null,
                new LambdaUpdateWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, userId)
                        .eq(Spring2026User::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
                        .setSql("used_quota = used_quota - 1")
                        .setSql("remaining_quota = remaining_quota + 1")
        );
    }

    /**
     * 增加成功次数
     */
    public void incrementSuccessCount(String userId) {
        userMapper.update(null,
                new LambdaUpdateWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, userId)
                        .setSql("success_count = success_count + 1")
        );
    }

    /**
     * 增加失败次数
     */
    public void incrementFailedCount(String userId) {
        userMapper.update(null,
                new LambdaUpdateWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, userId)
                        .setSql("failed_count = failed_count + 1")
        );
    }

    /**
     * 获取用户的管理员级别
     *
     * @param userId 用户ID
     * @return 管理员级别
     */
    public AdminLevel getUserAdminLevel(String userId) {
        Spring2026User user = userMapper.selectOne(
                new LambdaQueryWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, userId)
                        .eq(Spring2026User::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
        );

        if (user == null) {
            return AdminLevel.USER;
        }

        Integer level = user.getAdminLevel();
        return AdminLevel.fromCode(level);
    }

    /**
     * 设置用户的管理员级别
     *
     * @param operatorId 操作者用户ID
     * @param targetId  目标用户ID
     * @param level     新的管理员级别
     */
    public void setUserAdminLevel(String operatorId, String targetId, AdminLevel level) {
        // 检查操作者权限（必须是超级管理员）
        AdminLevel operatorLevel = getUserAdminLevel(operatorId);
        if (!operatorLevel.isSuperAdmin()) {
            throw new BusinessException(BusinessCode.ERROR_NOT_SUPER_ADMIN,
                    "只有超级管理员可以设置管理员权限");
        }

        // 不能将自己降级
        if (operatorId.equals(targetId) && level.getCode() < operatorLevel.getCode()) {
            throw new BusinessException(BusinessCode.ERROR_PERMISSION_DENIED, "不能降低自己的管理员级别");
        }

        // 更新目标用户的管理员级别
        userMapper.update(null,
                new LambdaUpdateWrapper<Spring2026User>()
                        .eq(Spring2026User::getUserId, targetId)
                        .setSql("admin_level = " + level.getCode())
        );

        log.info("更新用户管理员级别: operatorId={}, targetId={}, newLevel={}",
                operatorId, targetId, level);
    }
}