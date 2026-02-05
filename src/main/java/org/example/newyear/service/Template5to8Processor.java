package org.example.newyear.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026Template;
import org.example.newyear.util.VideoProcessorUtil;
import org.springframework.stereotype.Service;

/**
 * 模板5-8流程处理器（待实现）
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Service("template5to8Processor")
@RequiredArgsConstructor
public class Template5to8Processor implements ITemplateProcessor {

    private final VideoProcessorUtil videoProcessorUtil;

    @Override
    public String process(String recordId, Spring2026Template template, VideoCreateDTO dto) {
        log.info("开始处理模板5-8流程: recordId={}, templateId={}", recordId, template.getTemplateId());

        // TODO: 实现模板5-8的流程
        throw new UnsupportedOperationException("模板5-8流程待实现");
    }
}
