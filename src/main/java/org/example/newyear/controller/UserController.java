package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.service.UserService;
import org.example.newyear.vo.UserQuotaVO;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户配额
     */
    @GetMapping("/quota")
    public Result<UserQuotaVO> getQuota(@RequestParam("userId") String userId) {
        log.info("获取用户配额: userId={}", userId);
        UserQuotaVO quota = userService.getUserQuota(userId);
        return Result.success(quota);
    }
}