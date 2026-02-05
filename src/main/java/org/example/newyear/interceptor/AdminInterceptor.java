package org.example.newyear.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.example.newyear.annotation.RequireAdmin;
import org.example.newyear.common.AdminLevel;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只检查方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法或类上是否有@RequireAdmin注解
        RequireAdmin methodAnnotation = handlerMethod.getMethodAnnotation(RequireAdmin.class);
        RequireAdmin classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireAdmin.class);

        RequireAdmin annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (annotation == null) {
            // 没有注解，直接放行
            return true;
        }

        // 获取用户ID
        String userId = request.getHeader("X-User-UUID");
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException(BusinessCode.ERROR_PERMISSION_DENIED, "未登录或登录已过期");
        }

        // 获取用户的管理员级别
        AdminLevel requiredLevel = annotation.value();
        AdminLevel userLevel = userService.getUserAdminLevel(userId);

        // 检查权限
        if (userLevel.getCode() < requiredLevel.getCode()) {
            log.warn("用户权限不足: userId={}, userLevel={}, requiredLevel={}",
                    userId, userLevel, requiredLevel);
            throw new BusinessException(BusinessCode.ERROR_ADMIN_REQUIRED,
                    "需要" + requiredLevel.getDescription() + "权限");
        }

        log.info("管理员权限验证通过: userId={}, level={}", userId, userLevel);
        return true;
    }
}
