package org.example.newyear.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.BusinessCode;
import org.example.newyear.common.Constants;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.exception.BusinessException;
import org.example.newyear.mapper.Spring2026TemplateMapper;
import org.example.newyear.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板服务
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final Spring2026TemplateMapper templateMapper;

    /**
     * 获取模板列表
     */
    public List<Spring2026Template> getTemplateList() {
        return templateMapper.selectList(
                new LambdaQueryWrapper<Spring2026Template>()
                        .eq(Spring2026Template::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
                        .orderByDesc(Spring2026Template::getUsedTimes)
        );
    }

    /**
     * 根据模板ID获取模板
     */
    public Spring2026Template getTemplateById(String templateId) {
        Spring2026Template template = templateMapper.selectOne(
                new LambdaQueryWrapper<Spring2026Template>()
                        .eq(Spring2026Template::getTemplateId, templateId)
                        .eq(Spring2026Template::getActivityType, Constants.ACTIVITY_TYPE_SPRING_2026)
        );

        if (template == null) {
            throw new BusinessException(BusinessCode.ERROR_TEMPLATE_NOT_FOUND);
        }

        return template;
    }

    /**
     * 获取模板的任务配置
     */
    public Map<String, Object> getTaskConfig(String templateId) {
        Spring2026Template template = getTemplateById(templateId);

        if (template.getTaskConfig() == null || template.getTaskConfig().isEmpty()) {
            // 默认配置（Java 8兼容）
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("steps", Arrays.asList("faceSwap", "lipSync"));
            defaultConfig.put("estimated_time", 120);
            return defaultConfig;
        }

        try {
            return JsonUtil.fromJson(template.getTaskConfig(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("解析任务配置失败: templateId={}, config={}", templateId, template.getTaskConfig(), e);
            // 返回默认配置（Java 8兼容）
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("steps", Arrays.asList("faceSwap", "lipSync"));
            defaultConfig.put("estimated_time", 120);
            return defaultConfig;
        }
    }

    /**
     * 增加模板使用次数
     */
    public void incrementUsedTimes(String templateId) {
        templateMapper.update(null,
                new LambdaUpdateWrapper<Spring2026Template>()
                        .eq(Spring2026Template::getTemplateId, templateId)
                        .setSql("used_times = used_times + 1")
        );
    }
}