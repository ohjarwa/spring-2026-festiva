package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.common.Result;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.service.TemplateService;
import org.example.newyear.vo.TemplateVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板控制器
 *
 * @author Claude
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 获取模板列表
     */
    @GetMapping("/list")
    public Result<List<TemplateVO>> getTemplateList() {
        log.info("获取模板列表");
        List<Spring2026Template> templates = templateService.getTemplateList();

        List<TemplateVO> voList = templates.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{templateId}/detail")
    public Result<TemplateVO> getTemplateDetail(@PathVariable("templateId") String templateId) {
        log.info("获取模板详情: templateId={}", templateId);
        Spring2026Template template = templateService.getTemplateById(templateId);
        return Result.success(convertToVO(template));
    }

    /**
     * 转换为VO
     */
    private TemplateVO convertToVO(Spring2026Template template) {
        TemplateVO vo = new TemplateVO();
        vo.setTemplateId(template.getTemplateId());
        vo.setName(template.getName());
        vo.setCoverUrl(template.getCoverUrl());

        // 解析任务配置获取预估时间
        Map<String, Object> taskConfig = templateService.getTaskConfig(template.getTemplateId());
        vo.setEstimatedTime((Integer) taskConfig.get("estimated_time"));

        return vo;
    }
}